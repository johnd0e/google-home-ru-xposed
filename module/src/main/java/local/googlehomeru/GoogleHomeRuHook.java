package local.googlehomeru;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;
import org.luckypray.dexkit.result.MethodDataList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class GoogleHomeRuHook implements IXposedHookLoadPackage {
    private static final String MODULE_PACKAGE = "local.googlehomeru";
    private static final String REQUEST_NAME = "SetSurfaceAssistantLanguageRequest";
    private static final String REQUEST_PATH = "/set_assistant_language?rt=b";
    private static final XSharedPreferences MODULE_PREFERENCES =
            new XSharedPreferences(MODULE_PACKAGE, LanguageSettings.PREFERENCES_NAME);
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean();
    private static final AtomicBoolean DEXKIT_LIBRARY_LOADED = new AtomicBoolean();
    private static volatile Context applicationContext;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) {
        if (!HookTargetPolicy.isTargetProcess(lpparam.packageName, lpparam.processName)) {
            return;
        }

        XposedBridge.log("HOME_RU_LOADED process=" + lpparam.processName);
        try {
            Method attach = Application.class.getDeclaredMethod("attach", Context.class);
            XposedBridge.hookMethod(attach, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!INITIALIZED.compareAndSet(false, true)) {
                        return;
                    }
                    Context context = (Context) param.args[0];
                    applicationContext = context.getApplicationContext();
                    Thread discoveryThread = new Thread(
                            () -> discoverAndHook(lpparam.classLoader),
                            "Home-language-hook-discovery");
                    discoveryThread.start();
                }
            });
        } catch (NoSuchMethodException e) {
            logUnsupported("application_attach_not_found");
        } catch (LinkageError | SecurityException | IllegalArgumentException e) {
            logUnsupported("attach_hook_failed:" + e.getClass().getSimpleName());
        }
    }

    private static void discoverAndHook(ClassLoader loader) {
        try {
            loadDexKitLibrary();
            DexKitBridge bridge = DexKitBridge.create(loader, false);
            try {
                FindMethod query = new FindMethod().matcher(MethodMatcher.create()
                        .name("<init>")
                        .usingStrings(Arrays.asList(REQUEST_NAME, REQUEST_PATH),
                                StringMatchType.Equals));
                MethodDataList matches = bridge.findMethod(query);
                Constructor<?> targetConstructor = findUniqueRequestConstructor(matches, loader);
                if (targetConstructor == null) {
                    return;
                }
                XposedBridge.hookMethod(targetConstructor, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        interceptRequest(param);
                    }
                });
                XposedBridge.log("HOME_RU_REWRITE_HOOK_INSTALLED request=" + REQUEST_NAME
                        + " constructor=" + targetConstructor.getDeclaringClass().getName());
            } finally {
                bridge.close();
            }
        } catch (Exception e) {
            logUnsupported("request_discovery_failed:" + e.getClass().getSimpleName());
        } catch (LinkageError e) {
            logUnsupported("request_discovery_linkage_failed:" + e.getClass().getSimpleName());
        }
    }

    private static void loadDexKitLibrary() {
        if (DEXKIT_LIBRARY_LOADED.compareAndSet(false, true)) {
            try {
                System.loadLibrary("dexkit");
            } catch (LinkageError | SecurityException e) {
                DEXKIT_LIBRARY_LOADED.set(false);
                throw e;
            }
        }
    }

    private static Constructor<?> findUniqueRequestConstructor(
            MethodDataList matches, ClassLoader loader) throws ReflectiveOperationException {
        Constructor<?> result = null;
        for (MethodData match : matches) {
            List<String> strings = match.getUsingStrings();
            if (!match.isConstructor() || !strings.contains(REQUEST_NAME)
                    || !strings.contains(REQUEST_PATH)) {
                continue;
            }
            Constructor<?> candidate = match.getConstructorInstance(loader);
            Class<?>[] parameterTypes = candidate.getParameterTypes();
            if (parameterTypes.length == 0 || !hasProtobufLiteMethods(parameterTypes[0])) {
                continue;
            }
            if (result != null) {
                logUnsupported("request_constructor_ambiguous count=multiple");
                return null;
            }
            result = candidate;
        }
        if (result == null) {
            logUnsupported("request_constructor_not_found");
        }
        return result;
    }

    private static boolean hasProtobufLiteMethods(Class<?> type) {
        try {
            type.getMethod("toByteArray");
            type.getMethod("getParserForType");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static void interceptRequest(XC_MethodHook.MethodHookParam param) {
        try {
            Object message = param.args.length == 0 ? null : param.args[0];
            MODULE_PREFERENCES.reload();
            String configuredTarget = MODULE_PREFERENCES.getString(
                    LanguageSettings.TARGET_LANGUAGE_KEY, HookTargetPolicy.DEFAULT_TARGET_LANGUAGE);
            String targetLanguage = HookTargetPolicy.normalizeTargetLanguage(configuredTarget);
            ProtoLanguageRewriter.RewriteResult result = ProtoLanguageRewriter.rewrite(
                    message, HookTargetPolicy.SOURCE_LANGUAGE, targetLanguage);
            if (result.rewritten) {
                param.args[0] = result.message;
            }
            String original = result.originalLanguage == null
                    ? "<missing>" : result.originalLanguage;
            String resulting = result.resultingLanguage == null
                    ? "<unchanged>" : result.resultingLanguage;
            XposedBridge.log("HOME_RU_HOOK_INVOKED language=" + original
                    + " result=" + resulting + " rewritten=" + result.rewritten);
            if (result.rewritten) {
                XposedBridge.log("HOME_RU_LANGUAGE_REWRITTEN from=" + original
                        + " to=" + resulting);
            }
            showHookToast(original, resulting);
        } catch (Exception | LinkageError e) {
            XposedBridge.log("HOME_RU_REQUEST_UNCHANGED reason="
                    + e.getClass().getSimpleName());
            showHookToast("<parse error>", "<unchanged>");
        }
    }

    private static void logUnsupported(String reason) {
        XposedBridge.log("HOME_RU_UNSUPPORTED reason=" + reason);
    }

    private static void showHookToast(String originalLanguage, String resultingLanguage) {
        Context context = applicationContext;
        if (context == null) {
            return;
        }

        String message = "Home hook caught: " + originalLanguage + " → " + resultingLanguage;
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show());
    }
}
