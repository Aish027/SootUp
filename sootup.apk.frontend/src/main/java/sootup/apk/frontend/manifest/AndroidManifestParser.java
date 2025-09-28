package sootup.apk.frontend.manifest;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

/**
 * Parses AndroidManifest.xml from an APK.
 */
public class AndroidManifestParser {

    /** Parses the manifest and returns an AndroidManifest model. */
    public AndroidManifest parseManifest(String apkPath) throws IOException {
        if (apkPath == null || apkPath.isBlank())
            throw new IllegalArgumentException("APK path cannot be null or empty");
        File f = new File(apkPath);
        if (!f.isFile()) throw new IOException("APK not found: " + apkPath);

        try (ApkFile apk = new ApkFile(f)) {
            ApkMeta meta = apk.getApkMeta();
            String xml = apk.getManifestXml();

            AndroidManifest m = new AndroidManifest();
            m.packageName = meta.getPackageName();
            m.versionName  = meta.getVersionName();
            m.versionCode  = meta.getVersionCode() == null ? 0L : meta.getVersionCode();

            parseXmlComponents(xml, m);
            return m;
        } catch (Exception e) {
            throw new IOException("Failed to parse manifest", e);
        }
    }

    private void parseXmlComponents(String xml, AndroidManifest m) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));
        String pkg = m.packageName != null ? m.packageName : "";

        // Activities
        NodeList acts = doc.getElementsByTagName("activity");
        for (int i = 0; i < acts.getLength(); i++) {
            Element e = (Element) acts.item(i);
            ActivityInfo ai = new ActivityInfo();
            ai.name      = resolve(e.getAttribute("android:name"), pkg);
            ai.exported  = "true".equals(e.getAttribute("android:exported"));
            ai.enabled   = !"false".equals(e.getAttribute("android:enabled"));
            ai.intentActions = parseIntentActions(e);
            m.activities.add(ai);
        }

        // Services
        NodeList svs = doc.getElementsByTagName("service");
        for (int i = 0; i < svs.getLength(); i++) {
            Element e = (Element) svs.item(i);
            ServiceInfo si = new ServiceInfo();
            si.name     = resolve(e.getAttribute("android:name"), pkg);
            si.exported = "true".equals(e.getAttribute("android:exported"));
            si.enabled  = !"false".equals(e.getAttribute("android:enabled"));
            m.services.add(si);
        }

        // Receivers
        NodeList rcv = doc.getElementsByTagName("receiver");
        for (int i = 0; i < rcv.getLength(); i++) {
            Element e = (Element) rcv.item(i);
            ReceiverInfo ri = new ReceiverInfo();
            ri.name     = resolve(e.getAttribute("android:name"), pkg);
            ri.exported = "true".equals(e.getAttribute("android:exported"));
            ri.enabled  = !"false".equals(e.getAttribute("android:enabled"));
            m.receivers.add(ri);
        }

        // Permissions
        NodeList perms = doc.getElementsByTagName("uses-permission");
        for (int i = 0; i < perms.getLength(); i++) {
            Element e = (Element) perms.item(i);
            String p = e.getAttribute("android:name");
            if (!p.isBlank()) m.permissions.add(p);
        }
    }

    private List<String> parseIntentActions(Element comp) {
        List<String> list = new ArrayList<>();
        NodeList filters = comp.getElementsByTagName("intent-filter");
        for (int i = 0; i < filters.getLength(); i++) {
            NodeList actions = ((Element) filters.item(i)).getElementsByTagName("action");
            for (int j = 0; j < actions.getLength(); j++) {
                String a = ((Element) actions.item(j)).getAttribute("android:name");
                if (!a.isBlank()) list.add(a);
            }
        }
        return list;
    }

    private String resolve(String cls, String pkg) {
        if (cls.startsWith(".")) return pkg + cls;
        if (!cls.contains(".")) return pkg + "." + cls;
        return cls;
    }

    /** Model representing parsed manifest data. */
    public static class AndroidManifest {
        String packageName;
        String versionName;
        long versionCode;
        List<ActivityInfo> activities = new ArrayList<>();
        List<ServiceInfo>   services   = new ArrayList<>();
        List<ReceiverInfo>  receivers  = new ArrayList<>();
        List<String>        permissions= new ArrayList<>();

        public String getPackageName()  { return packageName; }
        public String getVersionName()  { return versionName; }
        public long   getVersionCode()  { return versionCode; }
        public List<ActivityInfo> getActivities() { return activities; }
        public List<ServiceInfo>   getServices()   { return services; }
        public List<ReceiverInfo>  getReceivers()  { return receivers; }
        public List<String>        getPermissions(){ return permissions; }

        public List<String> getLauncherActivities() {
            return activities.stream()
                    .filter(a -> a.intentActions.contains("android.intent.action.MAIN"))
                    .map(a -> a.name)
                    .toList();
        }

        public String getPrimaryEntryPoint() {
            var l = getLauncherActivities();
            return l.isEmpty() ? null : l.get(0);
        }

        public List<String> getAllEntryPoints() {
            return activities.stream()
                    .filter(ActivityInfo::isExported)
                    .map(a -> a.name)
                    .toList();
        }
    }

    public static class ActivityInfo {
        String name;
        boolean exported;
        boolean enabled;
        List<String> intentActions = new ArrayList<>();
        public String getName() { return name; }
        public boolean isExported(){ return exported; }
        public boolean isEnabled() { return enabled; }
        public List<String> getIntentActions(){ return intentActions; }
    }
    public static class ServiceInfo {
        String name; boolean exported; boolean enabled;
        public String getName() { return name; }
        public boolean isExported(){ return exported; }
        public boolean isEnabled() { return enabled; }
    }
    public static class ReceiverInfo {
        String name; boolean exported; boolean enabled;
        public String getName() { return name; }
        public boolean isExported(){ return exported; }
        public boolean isEnabled() { return enabled; }
    }
}
