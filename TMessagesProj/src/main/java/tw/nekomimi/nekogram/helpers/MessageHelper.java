package tw.nekomimi.nekogram.helpers;

import android.text.TextUtils;

import org.telegram.messenger.FileLoader;
import org.telegram.messenger.MessageObject;
import org.telegram.tgnet.TLRPC;

import java.io.File;

public class MessageHelper {
    public static String getPathToMessage(MessageObject messageObject) {
        if (messageObject == null) {
            return null;
        }
        if (messageObject.messageOwner != null && !TextUtils.isEmpty(messageObject.messageOwner.attachPath)) {
            File f = new File(messageObject.messageOwner.attachPath);
            if (f.exists()) {
                return f.getAbsolutePath();
            }
        }
        File f = FileLoader.getInstance(messageObject.currentAccount).getPathToMessage(messageObject.messageOwner);
        if (f != null && f.exists()) {
            return f.getAbsolutePath();
        }
        return f != null ? f.getAbsolutePath() : null;
    }
}
