package local.googlehomeru;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HookTargetPolicyTest {
    @Test
    public void acceptsOnlyHomePackageInItsMainProcess() {
        assertTrue(HookTargetPolicy.isTargetProcess(
                "com.google.android.apps.chromecast.app",
                "com.google.android.apps.chromecast.app"));

        assertFalse(HookTargetPolicy.isTargetProcess(
                "com.google.android.googlequicksearchbox",
                "com.google.android.googlequicksearchbox"));
        assertFalse(HookTargetPolicy.isTargetProcess(
                "com.google.android.apps.chromecast.app",
                "com.google.android.apps.chromecast.app:remote"));
        assertFalse(HookTargetPolicy.isTargetProcess(
                "com.google.android.apps.chromecast.app",
                null));
        assertFalse(HookTargetPolicy.isTargetProcess(null,
                "com.google.android.apps.chromecast.app"));
    }

    @Test
    public void rewritesOnlyEnglishIndiaToRussian() {
        assertEquals("fr-FR", HookTargetPolicy.rewriteLanguage("en-IN", "fr-FR"));
        assertEquals("ru-RU", HookTargetPolicy.rewriteLanguage("en-IN", "ru-RU"));
        assertEquals("en-GB", HookTargetPolicy.rewriteLanguage("en-GB", "fr-FR"));
        assertEquals("ru-RU", HookTargetPolicy.rewriteLanguage("en-IN", "bad tag"));
    }

    @Test
    public void validatesAndDefaultsTargetLanguage() {
        assertTrue(HookTargetPolicy.isValidTargetLanguage("ru-RU"));
        assertTrue(HookTargetPolicy.isValidTargetLanguage("en-IN"));
        assertTrue(HookTargetPolicy.isValidTargetLanguage("zh-Hant-TW"));
        assertFalse(HookTargetPolicy.isValidTargetLanguage(""));
        assertFalse(HookTargetPolicy.isValidTargetLanguage("bad tag"));
        assertFalse(HookTargetPolicy.isValidTargetLanguage("../shared_prefs"));
        assertEquals("ru-RU", HookTargetPolicy.normalizeTargetLanguage("bad tag"));
        assertEquals("ru-RU", HookTargetPolicy.normalizeTargetLanguage(null));
    }

    @Test
    public void preservesMissingLanguage() {
        assertNull(HookTargetPolicy.rewriteLanguage(null, "ru-RU"));
    }
}
