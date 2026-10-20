package local.googlehomeru;

final class HookTargetPolicy {
    static final String HOME_PACKAGE = "com.google.android.apps.chromecast.app";
    static final String DEFAULT_TARGET_LANGUAGE = "ru-RU";
    static final String SOURCE_LANGUAGE = "en-IN";
    private static final java.util.regex.Pattern LANGUAGE_TAG = java.util.regex.Pattern.compile(
            "^[a-z]{2,3}(?:-[A-Z][a-z]{3})?(?:-(?:[A-Z]{2}|[0-9]{3}))?$");

    private HookTargetPolicy() {
    }

    static boolean isTargetProcess(String packageName, String processName) {
        return HOME_PACKAGE.equals(packageName) && HOME_PACKAGE.equals(processName);
    }

    static String rewriteLanguage(String language, String configuredTarget) {
        return SOURCE_LANGUAGE.equals(language)
                ? normalizeTargetLanguage(configuredTarget)
                : language;
    }

    static boolean isValidTargetLanguage(String language) {
        return language != null && LANGUAGE_TAG.matcher(language).matches();
    }

    static String normalizeTargetLanguage(String language) {
        return isValidTargetLanguage(language) ? language : DEFAULT_TARGET_LANGUAGE;
    }
}
