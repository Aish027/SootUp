package sootup.apk.frontend.main;
import sootup.apk.frontend.manifest.AndroidManifestParser;

import java.util.Collections;
import java.util.List;
import sootup.core.views.View;
import sootup.java.core.JavaSootMethod;

/**
 * Android Entry Point Creator for SootUp static analysis framework integration.
 * Provides compatibility wrapper for AndroidManifestParser with basic entry point creation.
 *
 * @author SootUp APK Frontend
 * @version 1.0
 */
public final class AndroidEntryPointCreator {

    private final View view;

    /**
     * Constructor for AndroidEntryPointCreator.
     * @param view SootUp view for accessing application classes (can be null for basic functionality)
     */
    public AndroidEntryPointCreator(View view) {
        this.view = view;
    }

    /**
     * Create dummy main method for SootUp compatibility.
     * @return null to delegate to existing SootUp infrastructure
     */
    public JavaSootMethod createDummyMain() {
        return null;
    }

    /**
     * Create dummy main method from parsed manifest information.
     * @param apkPath path to APK file for manifest parsing
     * @return null to delegate to existing SootUp infrastructure
     * @throws Exception if manifest parsing fails
     */
    public JavaSootMethod createDummyMainFromManifest(String apkPath) throws Exception {
        AndroidManifestParser parser = new AndroidManifestParser();
        AndroidManifestParser.AndroidManifest manifest = parser.parseManifest(apkPath);

        logManifestInformation(manifest);
        return null;
    }

    /**
     * Extract all entry points from APK manifest.
     * @param apkPath path to APK file
     * @return list of entry point class names
     */
    public List<String> getEntryPoints(String apkPath) {
        try {
            AndroidManifestParser parser = new AndroidManifestParser();
            AndroidManifestParser.AndroidManifest manifest = parser.parseManifest(apkPath);
            return manifest.getAllEntryPoints();
        } catch (Exception e) {
            System.err.println("Failed to extract entry points: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Extract primary entry point from APK manifest.
     * @param apkPath path to APK file
     * @return primary entry point class name or null if none found
     */
    public String getPrimaryEntryPoint(String apkPath) {
        try {
            AndroidManifestParser parser = new AndroidManifestParser();
            AndroidManifestParser.AndroidManifest manifest = parser.parseManifest(apkPath);
            return manifest.getPrimaryEntryPoint();
        } catch (Exception e) {
            System.err.println("Failed to extract primary entry point: " + e.getMessage());
            return null;
        }
    }

    /**
     * Log detailed manifest information for debugging purposes.
     * @param manifest parsed AndroidManifest object
     */
    private void logManifestInformation(AndroidManifestParser.AndroidManifest manifest) {
        System.out.println("=== Android Manifest Information ===");
        System.out.println("Package: " + manifest.getPackageName());
        System.out.println("Components:");
        System.out.println("  Activities: " + manifest.getActivities().size());
        System.out.println("  Services: " + manifest.getServices().size());
        System.out.println("  Receivers: " + manifest.getReceivers().size());
        System.out.println("  Permissions: " + manifest.getPermissions().size());
        System.out.println("Primary Entry Point: " + manifest.getPrimaryEntryPoint());

        if (!manifest.getLauncherActivities().isEmpty()) {
            System.out.println("Launcher Activities:");
            for (String launcher : manifest.getLauncherActivities()) {
                System.out.println("  " + launcher);
            }
        }
        System.out.println("=== End Manifest Information ===");
    }
}
