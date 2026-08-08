package tw.nekomimi.nekogram.helpers;

import android.text.SpannableStringBuilder;

import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.UserConfig;

public class EntitiesHelper {
    public static CharSequence parseMarkdown(CharSequence text) {
        if (text == null) {
            return "";
        }
        SpannableStringBuilder builder = text instanceof SpannableStringBuilder ? (SpannableStringBuilder) text : new SpannableStringBuilder(text);
        MediaDataController.getInstance(UserConfig.selectedAccount).getEntities(new CharSequence[]{builder}, true);
        return builder;
    }
}
