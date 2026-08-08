package tw.nekomimi.nekogram.helpers.remote;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;

import java.util.ArrayList;

public abstract class BaseRemoteHelper {
    public interface Delegate {
        void onLoaded(JSONObject jsonObject);
        void onError(String error);
    }

    protected SharedPreferences preferences;

    public BaseRemoteHelper() {
        if (ApplicationLoader.applicationContext != null) {
            preferences = ApplicationLoader.applicationContext.getSharedPreferences("remote_config", Context.MODE_PRIVATE);
        }
    }

    protected abstract String getTag();

    protected void onError(String text, Delegate delegate) {
    }

    protected void onLoadSuccess(ArrayList<JSONObject> responses, Delegate delegate) {
    }

    protected void load() {
    }

    protected JSONObject getJSON() {
        if (preferences == null) {
            return null;
        }
        String data = preferences.getString(getTag(), null);
        if (data != null) {
            try {
                return new JSONObject(data);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
