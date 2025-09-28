package sootup.apk.frontend.test;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import sootup.apk.frontend.manifest.AndroidManifestParser;

/**
 * Unit tests for AndroidManifestParser functionality.
 *
 * @author SootUp APK Frontend
 */
public class AndroidManifestParserTest {

    private AndroidManifestParser parser;

    @BeforeEach
    void setUp() {
        parser = new AndroidManifestParser();
    }

    @Test
    public void testParserInstantiation() {
        assertNotNull(parser, "Parser should be instantiated successfully");
    }

    @Test
    public void testNullApkPath() {
        assertThrows(IllegalArgumentException.class, () -> {
            parser.parseManifest(null);
        }, "Parser should reject null APK path");
    }

    @Test
    public void testEmptyApkPath() {
        assertThrows(IllegalArgumentException.class, () -> {
            parser.parseManifest("");
        }, "Parser should reject empty APK path");
    }

    @Test
    public void testNonexistentApkPath() {
        assertThrows(IOException.class, () -> {
            parser.parseManifest("/nonexistent/path.apk");
        }, "Parser should reject nonexistent APK files");
    }
}
