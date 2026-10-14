package org.justgram.messenger.xray;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.justgram.messenger.JustgramConfig;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.utils.proxy.ProxySettings;

import java.net.ServerSocket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import libv2ray.CoreCallbackHandler;
import libv2ray.CoreController;
import libv2ray.Libv2ray;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class XrayManager {

    private static volatile XrayManager Instance;

    public static XrayManager getInstance() {
        XrayManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (XrayManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new XrayManager();
                }
            }
        }
        return localInstance;
    }

    private final List<XrayProfile> profiles = new ArrayList<>();
    private final List<XraySubscription> subscriptions = new ArrayList<>();

    private boolean isRunning;
    private CoreController coreController;
    private String lastError;
    private int currentActivePort = 25565;
    private XrayProfile activeProfile;

    private final OkHttpClient httpClient;

    private XrayManager() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .followRedirects(true)
                .build();

        loadFromPrefs();
    }

    private synchronized void loadFromPrefs() {
        profiles.clear();
        subscriptions.clear();

        JustgramConfig.loadConfig();

        try {
            String profilesJson = JustgramConfig.xrayProfilesJson;
            if (TextUtils.isEmpty(profilesJson) || "[]".equals(profilesJson)) {
                SharedPreferences oldPrefs = ApplicationLoader.applicationContext.getSharedPreferences("justgram_xray", Context.MODE_PRIVATE);
                if (oldPrefs.contains("profiles")) {
                    profilesJson = oldPrefs.getString("profiles", "[]");
                    JustgramConfig.xraySubscriptionsJson = oldPrefs.getString("subscriptions", "[]");
                    JustgramConfig.xrayActiveProfileId = oldPrefs.getString("active_profile_id", null);
                    JustgramConfig.xrayRandomPort = oldPrefs.getBoolean("random_port", false);
                    JustgramConfig.xrayLocalPort = oldPrefs.getInt("local_port", 25565);
                    JustgramConfig.xrayProfilesJson = profilesJson;
                    JustgramConfig.saveConfig();
                }
            }

            JSONArray profArray = new JSONArray(profilesJson);
            for (int i = 0; i < profArray.length(); i++) {
                XrayProfile profile = XrayProfile.fromJsonObject(profArray.getJSONObject(i));
                if (profile != null) {
                    profiles.add(profile);
                }
            }

            String subsJson = JustgramConfig.xraySubscriptionsJson;
            if (!TextUtils.isEmpty(subsJson)) {
                JSONArray subArray = new JSONArray(subsJson);
                for (int i = 0; i < subArray.length(); i++) {
                    XraySubscription sub = XraySubscription.fromJsonObject(subArray.getJSONObject(i));
                    if (sub != null) {
                        if (TextUtils.isEmpty(sub.name) || sub.name.startsWith("Subscription ")) {
                            String nameFromUrl = extractSubscriptionName(null, null, sub.url);
                            if (!TextUtils.isEmpty(nameFromUrl)) {
                                sub.name = nameFromUrl;
                            }
                        }
                        subscriptions.add(sub);
                    }
                }
            }

            String activeId = JustgramConfig.xrayActiveProfileId;
            if (!TextUtils.isEmpty(activeId)) {
                for (XrayProfile p : profiles) {
                    if (activeId.equals(p.id)) {
                        activeProfile = p;
                        break;
                    }
                }
            }
            if (activeProfile == null && !profiles.isEmpty()) {
                activeProfile = profiles.get(0);
            }
        } catch (Exception ignored) {
        }
    }

    private synchronized void saveToPrefs() {
        try {
            JSONArray profArray = new JSONArray();
            for (XrayProfile p : profiles) {
                profArray.put(p.toJsonObject());
            }

            JSONArray subArray = new JSONArray();
            for (XraySubscription s : subscriptions) {
                subArray.put(s.toJsonObject());
            }

            JustgramConfig.xrayProfilesJson = profArray.toString();
            JustgramConfig.xraySubscriptionsJson = subArray.toString();
            JustgramConfig.xrayActiveProfileId = activeProfile != null ? activeProfile.id : null;
            JustgramConfig.saveConfig();
        } catch (Exception ignored) {
        }
    }

    public synchronized boolean isRunning() {
        return isRunning;
    }

    public synchronized boolean isRandomPort() {
        return JustgramConfig.xrayRandomPort;
    }

    public synchronized void setRandomPort(boolean random) {
        JustgramConfig.xrayRandomPort = random;
        JustgramConfig.saveConfig();
    }

    public synchronized int getLocalPort() {
        return JustgramConfig.xrayLocalPort;
    }

    public synchronized void setLocalPort(int port) {
        JustgramConfig.xrayLocalPort = port;
        JustgramConfig.saveConfig();
    }

    public synchronized int getCurrentActivePort() {
        return isRunning ? currentActivePort : getLocalPort();
    }

    public synchronized String getLastError() {
        return lastError;
    }

    public String getXrayVersion() {
        try {
            String v = Libv2ray.checkVersionX();
            return !TextUtils.isEmpty(v) ? v : "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }

    public synchronized List<XrayProfile> getProfiles() {
        return new ArrayList<>(profiles);
    }

    public synchronized List<XraySubscription> getSubscriptions() {
        return new ArrayList<>(subscriptions);
    }

    public synchronized XrayProfile getActiveProfile() {
        return activeProfile;
    }

    public synchronized void setActiveProfile(XrayProfile profile) {
        this.activeProfile = profile;
        saveToPrefs();
        if (isRunning) {
            stopService();
            startService();
        }
    }

    public synchronized boolean startService() {
        if (isRunning) return true;
        lastError = null;

        if (activeProfile == null) {
            lastError = "No profile selected";
            return false;
        }

        try {
            int port;
            if (isRandomPort()) {
                port = getRandomFreePort();
            } else {
                port = getLocalPort();
            }
            currentActivePort = port;

            String configJson = generateConfigJson();

            Context context = ApplicationLoader.applicationContext;
            Libv2ray.initCoreEnv(context.getFilesDir().getAbsolutePath(), "");

            coreController = Libv2ray.newCoreController(new CoreCallbackHandler() {
                @Override
                public long onEmitStatus(long code, String message) {
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("XrayCore: [" + code + "] " + message);
                    }
                    return 0;
                }

                @Override
                public long shutdown() {
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("XrayCore: shutdown");
                    }
                    return 0;
                }

                @Override
                public long startup() {
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("XrayCore: startup");
                    }
                    return 0;
                }
            });

            coreController.startLoop(configJson, currentActivePort);
            isRunning = true;

            enableTgProxy(currentActivePort);

            return true;
        } catch (Exception e) {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.e(e);
            }
            lastError = e.getMessage();
            if (TextUtils.isEmpty(lastError)) {
                lastError = e.toString();
            }
            isRunning = false;
            coreController = null;
            return false;
        }
    }

    public synchronized void stopService() {
        if (!isRunning) return;

        try {
            if (coreController != null) {
                coreController.stopLoop();
            }
        } catch (Exception ignored) {
        } finally {
            coreController = null;
            isRunning = false;
        }

        disableTgProxy();
    }

    private void enableTgProxy(int port) {
        try {
            ProxySettings proxySettings = ProxySettings.builder()
                    .setAddress("127.0.0.1")
                    .setPort(port)
                    .setUser("")
                    .setPassword("")
                    .setSecret("")
                    .setType(ProxySettings.Type.SOCKS5)
                    .build();
            SharedConfig.ProxyInfo proxyInfo = SharedConfig.addProxy(new SharedConfig.ProxyInfo(proxySettings));
            SharedConfig.currentProxy = proxyInfo;
            SharedConfig.saveProxyList();

            SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
            editor.putBoolean("proxy_enabled", true);
            editor.putString("proxy_ip", "127.0.0.1");
            editor.putInt("proxy_port", port);
            editor.putString("proxy_user", "");
            editor.putString("proxy_pass", "");
            editor.putString("proxy_secret", "");
            editor.putBoolean("proxy_enabled_calls", true);
            editor.commit();

            ConnectionsManager.setProxySettings(true, proxySettings);

            AndroidUtilities.runOnUIThread(() -> NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxySettingsChanged));
        } catch (Exception ignored) {
        }
    }

    private void disableTgProxy() {
        try {
            SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
            editor.putBoolean("proxy_enabled", false);
            editor.putBoolean("proxy_enabled_calls", false);
            editor.commit();

            ConnectionsManager.setProxySettings(false, null);

            AndroidUtilities.runOnUIThread(() -> NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxySettingsChanged));
        } catch (Exception ignored) {
        }
    }

    public String generateConfigJson() throws Exception {
        XrayProfile profile;
        synchronized (this) {
            profile = activeProfile;
        }
        if (profile == null) {
            throw new Exception("No active profile");
        }

        JSONObject config = new JSONObject();

        JSONObject log = new JSONObject();
        log.put("loglevel", BuildVars.LOGS_ENABLED ? "warning" : "none");
        config.put("log", log);

        // DNS
        JSONObject dns = new JSONObject();
        JSONArray dnsServers = new JSONArray();
        dnsServers.put("1.1.1.1");
        dnsServers.put("8.8.8.8");
        dnsServers.put("localhost");
        dns.put("servers", dnsServers);
        config.put("dns", dns);

        // Inbounds
        JSONArray inbounds = new JSONArray();
        JSONObject inbound = new JSONObject();
        inbound.put("tag", "socks-in");
        inbound.put("port", currentActivePort);
        inbound.put("listen", "127.0.0.1");
        inbound.put("protocol", "socks");

        JSONObject inboundSettings = new JSONObject();
        inboundSettings.put("auth", "noauth");
        inboundSettings.put("udp", true);
        inbound.put("settings", inboundSettings);

        JSONObject sniffing = new JSONObject();
        sniffing.put("enabled", true);
        JSONArray destOverride = new JSONArray();
        destOverride.put("http");
        destOverride.put("tls");
        destOverride.put("quic");
        sniffing.put("destOverride", destOverride);
        sniffing.put("routeOnly", false);
        inbound.put("sniffing", sniffing);

        inbounds.put(inbound);
        config.put("inbounds", inbounds);

        // Outbounds
        JSONArray outbounds = new JSONArray();
        outbounds.put(profile.buildOutboundJson());

        JSONObject directOutbound = new JSONObject();
        directOutbound.put("tag", "direct");
        directOutbound.put("protocol", "freedom");
        directOutbound.put("settings", new JSONObject());
        outbounds.put(directOutbound);

        JSONObject blockOutbound = new JSONObject();
        blockOutbound.put("tag", "block");
        blockOutbound.put("protocol", "blackhole");
        JSONObject blockSettings = new JSONObject();
        JSONObject response = new JSONObject();
        response.put("type", "none");
        blockSettings.put("response", response);
        blockOutbound.put("settings", blockSettings);
        outbounds.put(blockOutbound);

        config.put("outbounds", outbounds);

        // Routing
        JSONObject routing = new JSONObject();
        routing.put("domainStrategy", "AsIs");
        JSONArray rules = new JSONArray();
        JSONObject rule = new JSONObject();
        rule.put("type", "field");
        rule.put("outboundTag", "proxy");
        rule.put("network", "tcp,udp");
        rules.put(rule);
        routing.put("rules", rules);
        config.put("routing", routing);

        return config.toString(2);
    }

    public void addProfilesOrSubscription(String input, Utilities.Callback<Integer> onSuccess, Utilities.Callback<String> onError) {
        if (TextUtils.isEmpty(input)) {
            if (onError != null) onError.run("Input is empty");
            return;
        }

        String str = input.trim();
        if (str.startsWith("http://") || str.startsWith("https://")) {
            Utilities.globalQueue.postRunnable(() -> downloadSubscription(str, onSuccess, onError));
        } else {
            Utilities.globalQueue.postRunnable(() -> {
                List<XrayProfile> parsed = XrayParser.parseProfiles(str);
                if (parsed.isEmpty()) {
                    if (onError != null) {
                        AndroidUtilities.runOnUIThread(() -> onError.run("No valid profile found"));
                    }
                    return;
                }

                synchronized (XrayManager.this) {
                    for (XrayProfile p : parsed) {
                        p.subscriptionId = null;
                        profiles.add(p);
                    }
                    if (activeProfile == null && !profiles.isEmpty()) {
                        activeProfile = profiles.get(0);
                    }
                    saveToPrefs();
                }

                if (onSuccess != null) {
                    AndroidUtilities.runOnUIThread(() -> onSuccess.run(parsed.size()));
                }
            });
        }
    }

    private String extractSubscriptionName(Response response, String rawContent, String url) {
        String name = null;

        if (response != null) {
            String profileTitle = response.header("profile-title");
            if (TextUtils.isEmpty(profileTitle)) {
                profileTitle = response.header("subscription-title");
            }
            if (TextUtils.isEmpty(profileTitle)) {
                profileTitle = response.header("sub-title");
            }

            if (!TextUtils.isEmpty(profileTitle)) {
                name = decodeProfileTitle(profileTitle);
            }

            if (TextUtils.isEmpty(name)) {
                String contentDisposition = response.header("content-disposition");
                if (!TextUtils.isEmpty(contentDisposition)) {
                    name = extractFilenameFromContentDisposition(contentDisposition);
                }
            }
        }

        if (TextUtils.isEmpty(name) && !TextUtils.isEmpty(rawContent)) {
            name = extractNameFromContent(rawContent);
        }

        if (TextUtils.isEmpty(name) && !TextUtils.isEmpty(url)) {
            name = extractNameFromUrl(url);
        }

        if (TextUtils.isEmpty(name) && !TextUtils.isEmpty(url)) {
            try {
                Uri uri = Uri.parse(url);
                String host = uri.getHost();
                if (!TextUtils.isEmpty(host)) {
                    name = host;
                }
            } catch (Exception ignored) {
            }
        }

        return name != null ? name.trim() : null;
    }

    private static String decodeProfileTitle(String raw) {
        if (TextUtils.isEmpty(raw)) return null;
        String str = raw.trim();

        if (str.startsWith("\"") && str.endsWith("\"") && str.length() > 1) {
            str = str.substring(1, str.length() - 1).trim();
        }

        String lower = str.toLowerCase(Locale.US);
        if (lower.startsWith("base64:")) {
            String b64 = str.substring(7).trim();
            String decoded = XrayParser.safeBase64Decode(b64);
            if (!TextUtils.isEmpty(decoded)) {
                str = decoded.trim();
            }
        } else if (str.contains("%")) {
            try {
                str = URLDecoder.decode(str, "UTF-8");
            } catch (Exception ignored) {
            }
        } else {
            try {
                byte[] bytes = str.getBytes(StandardCharsets.ISO_8859_1);
                String utf8 = new String(bytes, StandardCharsets.UTF_8);
                if (!utf8.contains("\uFFFD")) {
                    str = utf8;
                }
            } catch (Exception ignored) {
            }
        }

        return str.trim();
    }

    private static String extractFilenameFromContentDisposition(String header) {
        if (TextUtils.isEmpty(header)) return null;

        try {
            int utfIdx = header.toLowerCase(Locale.US).indexOf("filename*=utf-8''");
            if (utfIdx != -1) {
                String val = header.substring(utfIdx + "filename*=utf-8''".length());
                int endIdx = val.indexOf(';');
                if (endIdx != -1) val = val.substring(0, endIdx);
                val = val.trim();
                if (val.startsWith("\"") && val.endsWith("\"") && val.length() > 1) {
                    val = val.substring(1, val.length() - 1);
                }
                val = URLDecoder.decode(val, "UTF-8");
                return cleanupFilename(val);
            }

            int fnIdx = header.toLowerCase(Locale.US).indexOf("filename=");
            if (fnIdx != -1) {
                String val = header.substring(fnIdx + "filename=".length());
                int endIdx = val.indexOf(';');
                if (endIdx != -1) val = val.substring(0, endIdx);
                val = val.trim();
                if (val.startsWith("\"") && val.endsWith("\"") && val.length() > 1) {
                    val = val.substring(1, val.length() - 1);
                }
                if (val.contains("%")) {
                    val = URLDecoder.decode(val, "UTF-8");
                }
                return cleanupFilename(val);
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private static String cleanupFilename(String filename) {
        if (TextUtils.isEmpty(filename)) return null;
        String fn = filename.trim();
        String lower = fn.toLowerCase(Locale.US);
        if (lower.endsWith(".txt") || lower.endsWith(".json") || lower.endsWith(".yaml") || lower.endsWith(".yml") || lower.endsWith(".conf")) {
            int dot = fn.lastIndexOf('.');
            if (dot > 0) {
                fn = fn.substring(0, dot);
            }
        }
        return fn.trim();
    }

    private static String extractNameFromUrl(String url) {
        if (TextUtils.isEmpty(url)) return null;

        try {
            int hashIdx = url.indexOf('#');
            if (hashIdx != -1 && hashIdx < url.length() - 1) {
                String fragment = url.substring(hashIdx + 1).trim();
                if (!TextUtils.isEmpty(fragment)) {
                    try {
                        fragment = URLDecoder.decode(fragment, "UTF-8");
                    } catch (Exception ignored) {
                    }
                    if (!TextUtils.isEmpty(fragment)) {
                        return fragment.trim();
                    }
                }
            }

            Uri uri = Uri.parse(url);
            String nameParam = uri.getQueryParameter("name");
            if (TextUtils.isEmpty(nameParam)) {
                nameParam = uri.getQueryParameter("title");
            }
            if (!TextUtils.isEmpty(nameParam)) {
                return nameParam.trim();
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private static String extractNameFromContent(String content) {
        if (TextUtils.isEmpty(content)) return null;

        String text = content.trim();
        if (!text.startsWith("vless://") && !text.startsWith("vmess://") && !text.startsWith("trojan://") && !text.startsWith("ss://") && !text.startsWith("hy2://")) {
            String decoded = XrayParser.safeBase64Decode(text);
            if (!TextUtils.isEmpty(decoded)) {
                text = decoded;
            }
        }

        String[] lines = text.split("[\\r\\n]+");
        int count = 0;
        for (String line : lines) {
            count++;
            if (count > 15) break;
            String l = line.trim();
            if (TextUtils.isEmpty(l)) continue;

            String lower = l.toLowerCase(Locale.US);

            if (lower.startsWith("#profile_title=") || lower.startsWith("//profile_title=")
                    || lower.startsWith("#name=") || lower.startsWith("//name=")
                    || lower.startsWith("#title=") || lower.startsWith("//title=")) {
                int eq = l.indexOf('=');
                if (eq != -1 && eq < l.length() - 1) {
                    String val = l.substring(eq + 1).trim();
                    if (val.startsWith("\"") && val.endsWith("\"") && val.length() > 1) {
                        val = val.substring(1, val.length() - 1);
                    }
                    if (!TextUtils.isEmpty(val)) {
                        return val.trim();
                    }
                }
            }
        }

        return null;
    }

    private void downloadSubscription(String url, Utilities.Callback<Integer> onSuccess, Utilities.Callback<String> onError) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "v2rayN/6.0.0 JustgramXray")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    if (onError != null) {
                        AndroidUtilities.runOnUIThread(() -> onError.run("HTTP error " + response.code()));
                    }
                    return;
                }

                String content = response.body().string();
                List<XrayProfile> parsed = XrayParser.parseProfiles(content);

                if (parsed.isEmpty()) {
                    if (onError != null) {
                        AndroidUtilities.runOnUIThread(() -> onError.run("No valid profiles found in subscription"));
                    }
                    return;
                }

                String extractedName = extractSubscriptionName(response, content, url);

                synchronized (XrayManager.this) {
                    XraySubscription sub = null;
                    for (XraySubscription s : subscriptions) {
                        if (url.equals(s.url)) {
                            sub = s;
                            break;
                        }
                    }

                    if (sub == null) {
                        sub = new XraySubscription();
                        sub.url = url;
                        if (!TextUtils.isEmpty(extractedName)) {
                            sub.name = extractedName;
                        } else {
                            sub.name = "Subscription " + (subscriptions.size() + 1);
                        }
                        subscriptions.add(sub);
                    } else if (!TextUtils.isEmpty(extractedName)) {
                        sub.name = extractedName;
                    }

                    sub.profileCount = parsed.size();
                    sub.lastUpdate = System.currentTimeMillis();

                    final String subId = sub.id;
                    profiles.removeIf(p -> subId.equals(p.subscriptionId));

                    for (XrayProfile p : parsed) {
                        p.subscriptionId = subId;
                        profiles.add(p);
                    }

                    if (activeProfile == null && !profiles.isEmpty()) {
                        activeProfile = profiles.get(0);
                    }

                    saveToPrefs();
                }

                if (onSuccess != null) {
                    AndroidUtilities.runOnUIThread(() -> onSuccess.run(parsed.size()));
                }
            }
        } catch (Exception e) {
            if (onError != null) {
                AndroidUtilities.runOnUIThread(() -> onError.run(e.getMessage() != null ? e.getMessage() : e.toString()));
            }
        }
    }

    public void updateSubscription(XraySubscription sub, Utilities.Callback<Integer> onSuccess, Utilities.Callback<String> onError) {
        if (sub == null || TextUtils.isEmpty(sub.url)) {
            if (onError != null) onError.run("Invalid subscription");
            return;
        }

        Utilities.globalQueue.postRunnable(() -> downloadSubscription(sub.url, onSuccess, onError));
    }

    public synchronized void updateSubscriptionName(XraySubscription sub, String newName) {
        if (sub == null || TextUtils.isEmpty(newName)) return;
        sub.name = newName.trim();
        saveToPrefs();
    }

    public void updateSubscriptions(Utilities.Callback<Integer> onSuccess, Utilities.Callback<String> onError) {
        List<XraySubscription> subs = getSubscriptions();
        if (subs.isEmpty()) {
            if (onError != null) onError.run("No subscriptions available");
            return;
        }

        Utilities.globalQueue.postRunnable(() -> {
            int totalCount = 0;
            String lastErr = null;

            for (XraySubscription sub : subs) {
                try {
                    Request request = new Request.Builder()
                            .url(sub.url)
                            .header("User-Agent", "v2rayN/6.0.0 JustgramXray")
                            .build();

                    try (Response response = httpClient.newCall(request).execute()) {
                        if (response.isSuccessful() && response.body() != null) {
                            String content = response.body().string();
                            List<XrayProfile> parsed = XrayParser.parseProfiles(content);
                            String extractedName = extractSubscriptionName(response, content, sub.url);

                            synchronized (XrayManager.this) {
                                if (!TextUtils.isEmpty(extractedName)) {
                                    sub.name = extractedName;
                                }
                                sub.profileCount = parsed.size();
                                sub.lastUpdate = System.currentTimeMillis();

                                final String subId = sub.id;
                                profiles.removeIf(p -> subId.equals(p.subscriptionId));

                                for (XrayProfile p : parsed) {
                                    p.subscriptionId = subId;
                                    profiles.add(p);
                                }
                                totalCount += parsed.size();
                            }
                        } else {
                            lastErr = "HTTP " + response.code();
                        }
                    }
                } catch (Exception e) {
                    lastErr = e.getMessage();
                }
            }

            synchronized (XrayManager.this) {
                if (activeProfile == null && !profiles.isEmpty()) {
                    activeProfile = profiles.get(0);
                }
                saveToPrefs();
            }

            final int count = totalCount;
            final String err = lastErr;
            AndroidUtilities.runOnUIThread(() -> {
                if (count > 0 && onSuccess != null) {
                    onSuccess.run(count);
                } else if (onError != null) {
                    onError.run(err != null ? err : "Failed to update subscriptions");
                }
            });
        });
    }

    public synchronized void deleteProfile(XrayProfile profile) {
        if (profile == null) return;
        profiles.removeIf(p -> p.id.equals(profile.id));
        if (activeProfile != null && activeProfile.id.equals(profile.id)) {
            activeProfile = profiles.isEmpty() ? null : profiles.get(0);
            if (isRunning) {
                stopService();
                if (activeProfile != null) startService();
            }
        }
        saveToPrefs();
    }

    public synchronized void deleteProfiles(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) return;
        profiles.removeIf(p -> ids.contains(p.id));
        if (activeProfile != null && ids.contains(activeProfile.id)) {
            activeProfile = profiles.isEmpty() ? null : profiles.get(0);
            if (isRunning) {
                stopService();
                if (activeProfile != null) startService();
            }
        }
        saveToPrefs();
    }

    public synchronized void deleteSubscription(XraySubscription sub) {
        if (sub == null) return;
        subscriptions.removeIf(s -> s.id.equals(sub.id));
        profiles.removeIf(p -> sub.id.equals(p.subscriptionId));
        if (activeProfile != null && sub.id.equals(activeProfile.subscriptionId)) {
            activeProfile = profiles.isEmpty() ? null : profiles.get(0);
            if (isRunning) {
                stopService();
                if (activeProfile != null) startService();
            }
        }
        saveToPrefs();
    }

    public void testPing(XrayProfile profile, Runnable onDone) {
        if (profile == null) {
            if (onDone != null) onDone.run();
            return;
        }

        Utilities.globalQueue.postRunnable(() -> {
            try {
                JSONObject outbound = profile.buildOutboundJson();
                JSONObject config = new JSONObject();
                JSONArray outbounds = new JSONArray();
                outbounds.put(outbound);

                JSONObject directOutbound = new JSONObject();
                directOutbound.put("tag", "direct");
                directOutbound.put("protocol", "freedom");
                outbounds.put(directOutbound);

                config.put("outbounds", outbounds);

                Context context = ApplicationLoader.applicationContext;
                Libv2ray.initCoreEnv(context.getFilesDir().getAbsolutePath(), "");

                long delay = Libv2ray.measureOutboundDelay(config.toString(), "http://cp.cloudflare.com/generate_204");
                profile.ping = (int) delay;
            } catch (Exception e) {
                profile.ping = -2;
            }

            synchronized (XrayManager.this) {
                saveToPrefs();
            }

            if (onDone != null) {
                AndroidUtilities.runOnUIThread(onDone);
            }
        });
    }

    public void testPingAll(Runnable onDone) {
        List<XrayProfile> profs = getProfiles();
        if (profs.isEmpty()) {
            if (onDone != null) onDone.run();
            return;
        }

        Utilities.globalQueue.postRunnable(() -> {
            Context context = ApplicationLoader.applicationContext;
            try {
                Libv2ray.initCoreEnv(context.getFilesDir().getAbsolutePath(), "");
            } catch (Exception ignored) {
            }

            for (XrayProfile p : profs) {
                try {
                    JSONObject outbound = p.buildOutboundJson();
                    JSONObject config = new JSONObject();
                    JSONArray outbounds = new JSONArray();
                    outbounds.put(outbound);

                    JSONObject directOutbound = new JSONObject();
                    directOutbound.put("tag", "direct");
                    directOutbound.put("protocol", "freedom");
                    outbounds.put(directOutbound);

                    config.put("outbounds", outbounds);

                    long delay = Libv2ray.measureOutboundDelay(config.toString(), "http://cp.cloudflare.com/generate_204");
                    p.ping = (int) delay;
                } catch (Exception e) {
                    p.ping = -2;
                }
            }

            synchronized (XrayManager.this) {
                saveToPrefs();
            }

            if (onDone != null) {
                AndroidUtilities.runOnUIThread(onDone);
            }
        });
    }

    private int getRandomFreePort() {
        try (ServerSocket ss = new ServerSocket(0)) {
            return ss.getLocalPort();
        } catch (Exception e) {
            return 25565;
        }
    }
}
