package sootup.apk.frontend.runner;

import sootup.apk.frontend.manifest.AndroidManifestParser;
import sootup.apk.frontend.main.AndroidEntryPointCreator;
import java.util.List;

/**
 * APK Runner - Strict analysis with no demo data
 */
public class ApkRunner {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java ApkRunner <path-to-apk>");
            System.err.println("runner: java ApkRunner ./my-app.apk");
            System.exit(1);
        }

        String apkPath = args[0];
        System.out.println("=== Android APK Analysis ===");
        System.out.println("APK: " + apkPath);

        try {
            performStrictAnalysis(apkPath);
            System.out.println("\n✓ Analysis completed successfully");
        } catch (Exception e) {
            System.err.println("✗ Analysis failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void performStrictAnalysis(String apkPath) throws Exception {
        // Parse manifest - fails if no manifest or parsing error
        AndroidManifestParser parser = new AndroidManifestParser();
        AndroidManifestParser.AndroidManifest manifest = parser.parseManifest(apkPath);

        System.out.println("\n=== Manifest Analysis ===");
        System.out.println("Package: " + nullSafe(manifest.getPackageName()));
        System.out.println("Activities: " + manifest.getActivities().size());
        System.out.println("Services: " + manifest.getServices().size());
        System.out.println("Receivers: " + manifest.getReceivers().size());
        System.out.println("Permissions: " + manifest.getPermissions().size());

        // Show permissions if any exist
        if (!manifest.getPermissions().isEmpty()) {
            System.out.println("\nDeclared Permissions:");
            for (String permission : manifest.getPermissions()) {
                System.out.println("  " + permission);
            }
        }

        // Show activities if any exist
        if (!manifest.getActivities().isEmpty()) {
            System.out.println("\nActivities:");
            for (AndroidManifestParser.ActivityInfo activity : manifest.getActivities()) {
                System.out.println("  " + activity.getName() + " (exported: " + activity.isExported() + ")");
            }
        }

        // Show services if any exist
        if (!manifest.getServices().isEmpty()) {
            System.out.println("\nServices:");
            for (AndroidManifestParser.ServiceInfo service : manifest.getServices()) {
                System.out.println("  " + service.getName() + " (exported: " + service.isExported() + ")");
            }
        }

        // Show receivers if any exist
        if (!manifest.getReceivers().isEmpty()) {
            System.out.println("\nBroadcast Receivers:");
            for (AndroidManifestParser.ReceiverInfo receiver : manifest.getReceivers()) {
                System.out.println("  " + receiver.getName() + " (exported: " + receiver.isExported() + ")");
            }
        }

        // Entry point analysis
        AndroidEntryPointCreator creator = new AndroidEntryPointCreator(null);
        System.out.println("\n=== Entry Point Analysis ===");

        String primaryEntry = creator.getPrimaryEntryPoint(apkPath);
        System.out.println("Primary Entry Point: " + nullSafe(primaryEntry));

        List<String> allEntries = creator.getEntryPoints(apkPath);
        System.out.println("All Entry Points (" + allEntries.size() + " total):");
        if (allEntries.isEmpty()) {
            System.out.println("  (none found)");
        } else {
            for (String entryPoint : allEntries) {
                System.out.println("  " + entryPoint);
            }
        }

        List<String> launchers = manifest.getLauncherActivities();
        System.out.println("Launcher Activities (" + launchers.size() + " total):");
        if (launchers.isEmpty()) {
            System.out.println("  (none found)");
        } else {
            for (String launcher : launchers) {
                System.out.println("  " + launcher);
            }
        }

        // Create dummy main for SootUp
        creator.createDummyMainFromManifest(apkPath);
    }

    private static String nullSafe(String value) {
        return value != null ? value : "(not specified)";
    }
}
