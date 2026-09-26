package org.justgram.messenger.xray;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class XrayProfile {

    public String id;
    public String subscriptionId;
    public String name;
    public String type; // vless, vmess, trojan, shadowsocks, hysteria2
    public String server;
    public int port;
    public int ping = -1; // -1: un-tested, -2: error, >=0: ping ms
    public String rawUrl;

    public String uuid;
    public String password;
    public String cipher; // method for ss, security/scy for vmess
    public int alterId = 0;
    public String flow;
    public String encryption = "none";

    public String network = "tcp"; // tcp, ws, grpc, http, hysteria2
    public String security = "none"; // none, tls, reality
    public String sni;
    public String alpn;
    public String fingerprint;
    public String path;
    public String host;
    public String serviceName;
    public String publicKey;
    public String shortId;
    public String spiderX;

    public String obfs;
    public String obfsPassword;
    public boolean allowInsecure;

    public String customJson;

    public XrayProfile() {
        this.id = UUID.randomUUID().toString();
    }

    public JSONObject buildOutboundJson() throws JSONException {
        if (!TextUtils.isEmpty(customJson)) {
            return new JSONObject(customJson);
        }

        JSONObject outbound = new JSONObject();
        outbound.put("tag", "proxy");

        String proto = type == null ? "" : type.toLowerCase();
        outbound.put("protocol", proto);

        JSONObject settings = new JSONObject();

        String net = TextUtils.isEmpty(network) ? "tcp" : network.toLowerCase();
        String sec = TextUtils.isEmpty(security) ? "none" : security.toLowerCase();

        if ("vless".equals(proto)) {
            JSONArray vnext = new JSONArray();
            JSONObject serverObj = new JSONObject();
            serverObj.put("address", server != null ? server : "");
            serverObj.put("port", port);

            JSONArray users = new JSONArray();
            JSONObject user = new JSONObject();
            user.put("id", uuid != null ? uuid : "");
            user.put("encryption", TextUtils.isEmpty(encryption) ? "none" : encryption);
            if (!TextUtils.isEmpty(flow) && ("tls".equals(sec) || "reality".equals(sec)) && "tcp".equals(net)) {
                user.put("flow", flow);
            }
            user.put("level", 0);
            users.put(user);

            serverObj.put("users", users);
            vnext.put(serverObj);
            settings.put("vnext", vnext);
        } else if ("vmess".equals(proto)) {
            JSONArray vnext = new JSONArray();
            JSONObject serverObj = new JSONObject();
            serverObj.put("address", server != null ? server : "");
            serverObj.put("port", port);

            JSONArray users = new JSONArray();
            JSONObject user = new JSONObject();
            user.put("id", uuid != null ? uuid : "");
            user.put("alterId", alterId);
            user.put("security", TextUtils.isEmpty(cipher) ? "auto" : cipher);
            user.put("level", 0);
            users.put(user);

            serverObj.put("users", users);
            vnext.put(serverObj);
            settings.put("vnext", vnext);
        } else if ("trojan".equals(proto)) {
            JSONArray servers = new JSONArray();
            JSONObject serverObj = new JSONObject();
            serverObj.put("address", server != null ? server : "");
            serverObj.put("port", port);
            serverObj.put("password", password != null ? password : "");
            serverObj.put("level", 0);
            servers.put(serverObj);
            settings.put("servers", servers);
        } else if ("shadowsocks".equals(proto) || "ss".equals(proto)) {
            outbound.put("protocol", "shadowsocks");
            JSONArray servers = new JSONArray();
            JSONObject serverObj = new JSONObject();
            serverObj.put("address", server != null ? server : "");
            serverObj.put("port", port);
            serverObj.put("method", cipher != null ? cipher : "aes-256-gcm");
            serverObj.put("password", password != null ? password : "");
            serverObj.put("level", 0);
            servers.put(serverObj);
            settings.put("servers", servers);
        } else if ("hysteria2".equals(proto) || "hy2".equals(proto)) {
            outbound.put("protocol", "hysteria2");
            JSONArray servers = new JSONArray();
            JSONObject serverObj = new JSONObject();
            serverObj.put("address", server != null ? server : "");
            serverObj.put("port", port);
            serverObj.put("password", password != null ? password : "");
            servers.put(serverObj);
            settings.put("servers", servers);
        }

        outbound.put("settings", settings);

        // streamSettings
        JSONObject streamSettings = new JSONObject();
        streamSettings.put("network", net);
        streamSettings.put("security", sec);

        String effectiveSni = !TextUtils.isEmpty(sni) ? sni : server;
        String fp = !TextUtils.isEmpty(fingerprint) ? fingerprint : "chrome";

        if ("tls".equals(sec)) {
            JSONObject tlsSettings = new JSONObject();
            if (!TextUtils.isEmpty(effectiveSni)) {
                tlsSettings.put("serverName", effectiveSni);
            }
            tlsSettings.put("allowInsecure", allowInsecure);
            tlsSettings.put("fingerprint", fp);
            if (!TextUtils.isEmpty(alpn)) {
                JSONArray alpnArray = new JSONArray();
                for (String a : alpn.split(",")) {
                    if (!TextUtils.isEmpty(a.trim())) {
                        alpnArray.put(a.trim());
                    }
                }
                tlsSettings.put("alpn", alpnArray);
            }
            streamSettings.put("tlsSettings", tlsSettings);
        } else if ("reality".equals(sec)) {
            JSONObject realitySettings = new JSONObject();
            if (!TextUtils.isEmpty(effectiveSni)) {
                realitySettings.put("serverName", effectiveSni);
            }
            realitySettings.put("fingerprint", fp);
            realitySettings.put("show", false);
            if (!TextUtils.isEmpty(publicKey)) {
                realitySettings.put("publicKey", publicKey);
            }
            if (!TextUtils.isEmpty(shortId)) {
                realitySettings.put("shortId", shortId);
            }
            if (!TextUtils.isEmpty(spiderX)) {
                realitySettings.put("spiderX", spiderX);
            }
            streamSettings.put("realitySettings", realitySettings);
        }

        if ("ws".equals(net)) {
            JSONObject wsSettings = new JSONObject();
            wsSettings.put("path", !TextUtils.isEmpty(path) ? path : "/");
            String hostHeader = !TextUtils.isEmpty(host) ? host : effectiveSni;
            if (!TextUtils.isEmpty(hostHeader)) {
                JSONObject headers = new JSONObject();
                headers.put("Host", hostHeader);
                wsSettings.put("headers", headers);
            }
            streamSettings.put("wsSettings", wsSettings);
        } else if ("grpc".equals(net)) {
            JSONObject grpcSettings = new JSONObject();
            if (!TextUtils.isEmpty(serviceName)) {
                grpcSettings.put("serviceName", serviceName);
            }
            grpcSettings.put("multiMode", false);
            streamSettings.put("grpcSettings", grpcSettings);
        } else if ("http".equals(net) || "h2".equals(net)) {
            JSONObject httpSettings = new JSONObject();
            httpSettings.put("path", !TextUtils.isEmpty(path) ? path : "/");
            String hostHeader = !TextUtils.isEmpty(host) ? host : effectiveSni;
            if (!TextUtils.isEmpty(hostHeader)) {
                JSONArray hostArr = new JSONArray();
                hostArr.put(hostHeader);
                httpSettings.put("host", hostArr);
            }
            streamSettings.put("httpSettings", httpSettings);
        }

        if ("hysteria2".equals(proto) || "hy2".equals(proto)) {
            streamSettings.put("network", "hysteria2");
            if (!"reality".equals(sec)) {
                streamSettings.put("security", "tls");
                JSONObject tlsSettings = streamSettings.optJSONObject("tlsSettings");
                if (tlsSettings == null) {
                    tlsSettings = new JSONObject();
                    if (!TextUtils.isEmpty(sni)) {
                        tlsSettings.put("serverName", sni);
                    }
                    tlsSettings.put("allowInsecure", allowInsecure);
                    streamSettings.put("tlsSettings", tlsSettings);
                }
            }
            if (!TextUtils.isEmpty(obfs)) {
                JSONObject hy2Settings = new JSONObject();
                JSONObject obfsObj = new JSONObject();
                obfsObj.put("type", obfs);
                if (!TextUtils.isEmpty(obfsPassword)) {
                    obfsObj.put("password", obfsPassword);
                }
                hy2Settings.put("obfs", obfsObj);
                streamSettings.put("hysteria2Settings", hy2Settings);
            }
        }

        outbound.put("streamSettings", streamSettings);
        return outbound;
    }

    public JSONObject toJsonObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
            obj.put("subscriptionId", subscriptionId);
            obj.put("name", name);
            obj.put("type", type);
            obj.put("server", server);
            obj.put("port", port);
            obj.put("ping", ping);
            obj.put("rawUrl", rawUrl);
            obj.put("uuid", uuid);
            obj.put("password", password);
            obj.put("cipher", cipher);
            obj.put("alterId", alterId);
            obj.put("flow", flow);
            obj.put("encryption", encryption);
            obj.put("network", network);
            obj.put("security", security);
            obj.put("sni", sni);
            obj.put("alpn", alpn);
            obj.put("fingerprint", fingerprint);
            obj.put("path", path);
            obj.put("host", host);
            obj.put("serviceName", serviceName);
            obj.put("publicKey", publicKey);
            obj.put("shortId", shortId);
            obj.put("spiderX", spiderX);
            obj.put("obfs", obfs);
            obj.put("obfsPassword", obfsPassword);
            obj.put("allowInsecure", allowInsecure);
            obj.put("customJson", customJson);
        } catch (JSONException ignored) {
        }
        return obj;
    }

    public static XrayProfile fromJsonObject(JSONObject obj) {
        if (obj == null) return null;
        XrayProfile profile = new XrayProfile();
        profile.id = obj.optString("id", UUID.randomUUID().toString());
        profile.subscriptionId = obj.optString("subscriptionId", null);
        profile.name = obj.optString("name", "");
        profile.type = obj.optString("type", "");
        profile.server = obj.optString("server", "");
        profile.port = obj.optInt("port", 443);
        profile.ping = obj.optInt("ping", -1);
        profile.rawUrl = obj.optString("rawUrl", "");
        profile.uuid = obj.optString("uuid", null);
        profile.password = obj.optString("password", null);
        profile.cipher = obj.optString("cipher", null);
        profile.alterId = obj.optInt("alterId", 0);
        profile.flow = obj.optString("flow", null);
        profile.encryption = obj.optString("encryption", "none");
        profile.network = obj.optString("network", "tcp");
        profile.security = obj.optString("security", "none");
        profile.sni = obj.optString("sni", null);
        profile.alpn = obj.optString("alpn", null);
        profile.fingerprint = obj.optString("fingerprint", null);
        profile.path = obj.optString("path", null);
        profile.host = obj.optString("host", null);
        profile.serviceName = obj.optString("serviceName", null);
        profile.publicKey = obj.optString("publicKey", null);
        profile.shortId = obj.optString("shortId", null);
        profile.spiderX = obj.optString("spiderX", null);
        profile.obfs = obj.optString("obfs", null);
        profile.obfsPassword = obj.optString("obfsPassword", null);
        profile.allowInsecure = obj.optBoolean("allowInsecure", false);
        profile.customJson = obj.optString("customJson", null);
        return profile;
    }
}
