package org.justgram.messenger.xray;

import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XrayParser {

    public static List<XrayProfile> parseProfiles(String input) {
        List<XrayProfile> list = new ArrayList<>();
        if (TextUtils.isEmpty(input)) {
            return list;
        }

        String content = input.trim();

        // If whole input is a base64 string without protocol prefix, try to decode first
        if (!isLink(content)) {
            String decoded = safeBase64Decode(content);
            if (!TextUtils.isEmpty(decoded) && isLink(decoded)) {
                content = decoded;
            }
        }

        String[] lines = content.split("[\\r\\n]+");
        for (String line : lines) {
            String l = line.trim();
            if (TextUtils.isEmpty(l)) continue;

            if (!isLink(l)) {
                String dec = safeBase64Decode(l);
                if (!TextUtils.isEmpty(dec) && isLink(dec)) {
                    l = dec.trim();
                }
            }

            XrayProfile profile = parseSingleLink(l);
            if (profile != null) {
                list.add(profile);
            }
        }

        return list;
    }

    public static XrayProfile parseSingleLink(String link) {
        if (TextUtils.isEmpty(link)) return null;
        String l = link.trim();

        if (l.startsWith("vless://")) {
            return parseVless(l);
        } else if (l.startsWith("vmess://")) {
            return parseVmess(l);
        } else if (l.startsWith("trojan://")) {
            return parseTrojan(l);
        } else if (l.startsWith("ss://") || l.startsWith("shadowsocks://")) {
            return parseShadowsocks(l);
        } else if (l.startsWith("hy2://") || l.startsWith("hysteria2://")) {
            return parseHysteria2(l);
        }

        return null;
    }

    private static boolean isLink(String s) {
        if (TextUtils.isEmpty(s)) return false;
        String l = s.toLowerCase();
        return l.startsWith("vless://") || l.startsWith("vmess://")
                || l.startsWith("trojan://") || l.startsWith("ss://")
                || l.startsWith("shadowsocks://") || l.startsWith("hy2://")
                || l.startsWith("hysteria2://");
    }

    private static XrayProfile parseVless(String url) {
        try {
            XrayProfile profile = new XrayProfile();
            profile.type = "vless";
            profile.rawUrl = url;

            String remark = "";
            String main = url.substring("vless://".length());
            int hashIdx = main.indexOf('#');
            if (hashIdx != -1) {
                remark = urlDecode(main.substring(hashIdx + 1));
                main = main.substring(0, hashIdx);
            }

            int atIdx = main.indexOf('@');
            if (atIdx == -1) return null;

            profile.uuid = urlDecode(main.substring(0, atIdx));
            String hostAndQuery = main.substring(atIdx + 1);

            String queryStr = "";
            int qIdx = hostAndQuery.indexOf('?');
            if (qIdx != -1) {
                queryStr = hostAndQuery.substring(qIdx + 1);
                hostAndQuery = hostAndQuery.substring(0, qIdx);
            }

            parseHostAndPort(hostAndQuery, profile);
            Map<String, String> query = parseQuery(queryStr);

            profile.network = getQueryValue(query, "type", "network");
            if (TextUtils.isEmpty(profile.network)) profile.network = "tcp";

            profile.security = getQueryValue(query, "security");
            if (TextUtils.isEmpty(profile.security)) profile.security = "none";

            profile.flow = getQueryValue(query, "flow");
            profile.encryption = getQueryValue(query, "encryption");
            if (TextUtils.isEmpty(profile.encryption)) profile.encryption = "none";

            profile.sni = getQueryValue(query, "sni", "servername");
            profile.alpn = getQueryValue(query, "alpn");
            profile.fingerprint = getQueryValue(query, "fp", "fingerprint");
            profile.path = getQueryValue(query, "path");
            profile.host = getQueryValue(query, "host", "headerType");
            profile.serviceName = getQueryValue(query, "serviceName", "grpc-service-name");
            profile.publicKey = getQueryValue(query, "pbk", "publickey");
            profile.shortId = getQueryValue(query, "sid", "shortid");
            profile.spiderX = getQueryValue(query, "spx");

            profile.name = TextUtils.isEmpty(remark) ? ("VLESS - " + profile.server + ":" + profile.port) : remark;
            return profile;
        } catch (Exception e) {
            return null;
        }
    }

    private static XrayProfile parseVmess(String url) {
        try {
            String body = url.substring("vmess://".length());
            String decoded = safeBase64Decode(body);

            if (!TextUtils.isEmpty(decoded) && decoded.trim().startsWith("{")) {
                JSONObject json = new JSONObject(decoded);
                XrayProfile profile = new XrayProfile();
                profile.type = "vmess";
                profile.rawUrl = url;

                profile.name = json.optString("ps", "");
                profile.server = json.optString("add", "");
                profile.port = json.optInt("port", 443);
                profile.uuid = json.optString("id", "");
                profile.alterId = json.optInt("aid", 0);
                profile.cipher = json.optString("scy", "auto");
                profile.network = json.optString("net", "tcp");
                profile.security = json.optString("tls", "none");
                profile.sni = json.optString("sni", "");
                profile.alpn = json.optString("alpn", "");
                profile.fingerprint = json.optString("fp", "");
                profile.path = json.optString("path", "");
                profile.host = json.optString("host", "");

                if (TextUtils.isEmpty(profile.name)) {
                    profile.name = "VMess - " + profile.server + ":" + profile.port;
                }
                return profile;
            } else {
                // Alternative standard URI format: vmess://uuid@server:port?query#remark
                String remark = "";
                String main = body;
                int hashIdx = main.indexOf('#');
                if (hashIdx != -1) {
                    remark = urlDecode(main.substring(hashIdx + 1));
                    main = main.substring(0, hashIdx);
                }

                int atIdx = main.indexOf('@');
                if (atIdx == -1) return null;

                XrayProfile profile = new XrayProfile();
                profile.type = "vmess";
                profile.rawUrl = url;
                profile.uuid = urlDecode(main.substring(0, atIdx));

                String hostAndQuery = main.substring(atIdx + 1);
                String queryStr = "";
                int qIdx = hostAndQuery.indexOf('?');
                if (qIdx != -1) {
                    queryStr = hostAndQuery.substring(qIdx + 1);
                    hostAndQuery = hostAndQuery.substring(0, qIdx);
                }

                parseHostAndPort(hostAndQuery, profile);
                Map<String, String> query = parseQuery(queryStr);

                profile.network = getQueryValue(query, "type", "network");
                if (TextUtils.isEmpty(profile.network)) profile.network = "tcp";
                profile.security = getQueryValue(query, "security", "tls");
                if (TextUtils.isEmpty(profile.security)) profile.security = "none";

                profile.cipher = getQueryValue(query, "scy", "cipher");
                profile.sni = getQueryValue(query, "sni", "servername");
                profile.path = getQueryValue(query, "path");
                profile.host = getQueryValue(query, "host");

                profile.name = TextUtils.isEmpty(remark) ? ("VMess - " + profile.server + ":" + profile.port) : remark;
                return profile;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static XrayProfile parseTrojan(String url) {
        try {
            XrayProfile profile = new XrayProfile();
            profile.type = "trojan";
            profile.rawUrl = url;

            String remark = "";
            String main = url.substring("trojan://".length());
            int hashIdx = main.indexOf('#');
            if (hashIdx != -1) {
                remark = urlDecode(main.substring(hashIdx + 1));
                main = main.substring(0, hashIdx);
            }

            int atIdx = main.indexOf('@');
            if (atIdx == -1) return null;

            profile.password = urlDecode(main.substring(0, atIdx));
            String hostAndQuery = main.substring(atIdx + 1);

            String queryStr = "";
            int qIdx = hostAndQuery.indexOf('?');
            if (qIdx != -1) {
                queryStr = hostAndQuery.substring(qIdx + 1);
                hostAndQuery = hostAndQuery.substring(0, qIdx);
            }

            parseHostAndPort(hostAndQuery, profile);
            Map<String, String> query = parseQuery(queryStr);

            profile.network = getQueryValue(query, "type", "network");
            if (TextUtils.isEmpty(profile.network)) profile.network = "tcp";

            profile.security = getQueryValue(query, "security");
            if (TextUtils.isEmpty(profile.security)) profile.security = "tls";

            profile.sni = getQueryValue(query, "sni", "servername");
            profile.alpn = getQueryValue(query, "alpn");
            profile.fingerprint = getQueryValue(query, "fp", "fingerprint");
            profile.path = getQueryValue(query, "path");
            profile.host = getQueryValue(query, "host");
            profile.serviceName = getQueryValue(query, "serviceName", "grpc-service-name");

            profile.name = TextUtils.isEmpty(remark) ? ("Trojan - " + profile.server + ":" + profile.port) : remark;
            return profile;
        } catch (Exception e) {
            return null;
        }
    }

    private static XrayProfile parseShadowsocks(String url) {
        try {
            XrayProfile profile = new XrayProfile();
            profile.type = "shadowsocks";
            profile.rawUrl = url;

            String prefix = url.startsWith("shadowsocks://") ? "shadowsocks://" : "ss://";
            String main = url.substring(prefix.length());

            String remark = "";
            int hashIdx = main.indexOf('#');
            if (hashIdx != -1) {
                remark = urlDecode(main.substring(hashIdx + 1));
                main = main.substring(0, hashIdx);
            }

            String userInfo = "";
            String hostAndQuery = "";

            int atIdx = main.indexOf('@');
            if (atIdx != -1) {
                userInfo = main.substring(0, atIdx);
                hostAndQuery = main.substring(atIdx + 1);

                if (!userInfo.contains(":")) {
                    userInfo = safeBase64Decode(userInfo);
                }
            } else {
                // Entire string before # is base64 encoded
                String decoded = safeBase64Decode(main);
                if (!TextUtils.isEmpty(decoded)) {
                    int decAt = decoded.indexOf('@');
                    if (decAt != -1) {
                        userInfo = decoded.substring(0, decAt);
                        hostAndQuery = decoded.substring(decAt + 1);
                    }
                }
            }

            if (TextUtils.isEmpty(userInfo) || TextUtils.isEmpty(hostAndQuery)) {
                return null;
            }

            int colonIdx = userInfo.indexOf(':');
            if (colonIdx != -1) {
                profile.cipher = urlDecode(userInfo.substring(0, colonIdx));
                profile.password = urlDecode(userInfo.substring(colonIdx + 1));
            } else {
                profile.cipher = "aes-256-gcm";
                profile.password = urlDecode(userInfo);
            }

            String queryStr = "";
            int qIdx = hostAndQuery.indexOf('?');
            if (qIdx != -1) {
                queryStr = hostAndQuery.substring(qIdx + 1);
                hostAndQuery = hostAndQuery.substring(0, qIdx);
            }

            parseHostAndPort(hostAndQuery, profile);
            Map<String, String> query = parseQuery(queryStr);
            String plugin = getQueryValue(query, "plugin");
            if (!TextUtils.isEmpty(plugin)) {
                if (plugin.contains("path=")) {
                    for (String part : plugin.split(";")) {
                        if (part.startsWith("path=")) {
                            profile.path = part.substring(5);
                        } else if (part.startsWith("host=")) {
                            profile.host = part.substring(5);
                        } else if (part.contains("tls")) {
                            profile.security = "tls";
                        }
                    }
                }
            }

            profile.name = TextUtils.isEmpty(remark) ? ("Shadowsocks - " + profile.server + ":" + profile.port) : remark;
            return profile;
        } catch (Exception e) {
            return null;
        }
    }

    private static XrayProfile parseHysteria2(String url) {
        try {
            XrayProfile profile = new XrayProfile();
            profile.type = "hysteria2";
            profile.rawUrl = url;

            String prefix = url.startsWith("hysteria2://") ? "hysteria2://" : "hy2://";
            String main = url.substring(prefix.length());

            String remark = "";
            int hashIdx = main.indexOf('#');
            if (hashIdx != -1) {
                remark = urlDecode(main.substring(hashIdx + 1));
                main = main.substring(0, hashIdx);
            }

            int atIdx = main.indexOf('@');
            if (atIdx == -1) return null;

            profile.password = urlDecode(main.substring(0, atIdx));
            String hostAndQuery = main.substring(atIdx + 1);

            String queryStr = "";
            int qIdx = hostAndQuery.indexOf('?');
            if (qIdx != -1) {
                queryStr = hostAndQuery.substring(qIdx + 1);
                hostAndQuery = hostAndQuery.substring(0, qIdx);
            }

            parseHostAndPort(hostAndQuery, profile);
            Map<String, String> query = parseQuery(queryStr);

            profile.network = "hysteria2";
            profile.security = "tls";

            String insecure = getQueryValue(query, "insecure", "skip-cert-verify");
            profile.allowInsecure = "1".equals(insecure) || "true".equalsIgnoreCase(insecure);

            profile.sni = getQueryValue(query, "sni");
            profile.alpn = getQueryValue(query, "alpn");
            profile.obfs = getQueryValue(query, "obfs");
            profile.obfsPassword = getQueryValue(query, "obfs-password", "obfs_password");

            profile.name = TextUtils.isEmpty(remark) ? ("Hysteria2 - " + profile.server + ":" + profile.port) : remark;
            return profile;
        } catch (Exception e) {
            return null;
        }
    }

    private static void parseHostAndPort(String hostAndPort, XrayProfile profile) {
        if (TextUtils.isEmpty(hostAndPort)) return;
        String hp = hostAndPort.trim();

        if (hp.startsWith("[")) { // IPv6
            int closingBracket = hp.indexOf(']');
            if (closingBracket != -1) {
                profile.server = hp.substring(1, closingBracket);
                int colon = hp.indexOf(':', closingBracket);
                if (colon != -1) {
                    profile.port = parseInt(hp.substring(colon + 1), 443);
                } else {
                    profile.port = 443;
                }
                return;
            }
        }

        int colonIdx = hp.lastIndexOf(':');
        if (colonIdx != -1) {
            profile.server = hp.substring(0, colonIdx);
            profile.port = parseInt(hp.substring(colonIdx + 1), 443);
        } else {
            profile.server = hp;
            profile.port = 443;
        }
    }

    private static Map<String, String> parseQuery(String queryStr) {
        Map<String, String> map = new HashMap<>();
        if (TextUtils.isEmpty(queryStr)) return map;

        String[] pairs = queryStr.split("&");
        for (String pair : pairs) {
            if (TextUtils.isEmpty(pair)) continue;
            int eqIdx = pair.indexOf('=');
            if (eqIdx != -1) {
                String key = urlDecode(pair.substring(0, eqIdx)).toLowerCase();
                String val = urlDecode(pair.substring(eqIdx + 1));
                map.put(key, val);
            } else {
                map.put(urlDecode(pair).toLowerCase(), "");
            }
        }
        return map;
    }

    private static String getQueryValue(Map<String, String> query, String... keys) {
        if (query == null || keys == null) return null;
        for (String k : keys) {
            if (k != null) {
                String v = query.get(k.toLowerCase());
                if (!TextUtils.isEmpty(v)) {
                    return v;
                }
            }
        }
        return null;
    }

    public static String safeBase64Decode(String str) {
        if (TextUtils.isEmpty(str)) return "";
        try {
            String s = str.trim().replaceAll("\\s+", "");
            int pad = s.length() % 4;
            if (pad == 2) s += "==";
            else if (pad == 3) s += "=";

            byte[] data;
            try {
                data = Base64.decode(s, Base64.URL_SAFE | Base64.NO_WRAP);
            } catch (Exception e) {
                data = Base64.decode(s, Base64.DEFAULT | Base64.NO_WRAP);
            }
            return new String(data, "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }

    private static String urlDecode(String str) {
        if (TextUtils.isEmpty(str)) return "";
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (Exception e) {
            return str;
        }
    }

    private static int parseInt(String str, int def) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return def;
        }
    }
}
