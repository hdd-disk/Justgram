package org.justgram.messenger.xray;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class XraySubscription {

    public String id;
    public String name;
    public String url;
    public int profileCount;
    public long lastUpdate;

    public XraySubscription() {
        this.id = UUID.randomUUID().toString();
    }

    public JSONObject toJsonObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
            obj.put("name", name);
            obj.put("url", url);
            obj.put("profileCount", profileCount);
            obj.put("lastUpdate", lastUpdate);
        } catch (JSONException ignored) {
        }
        return obj;
    }

    public static XraySubscription fromJsonObject(JSONObject obj) {
        if (obj == null) return null;
        XraySubscription sub = new XraySubscription();
        sub.id = obj.optString("id", UUID.randomUUID().toString());
        sub.name = obj.optString("name", "");
        sub.url = obj.optString("url", "");
        sub.profileCount = obj.optInt("profileCount", 0);
        sub.lastUpdate = obj.optLong("lastUpdate", 0);
        return sub;
    }
}
