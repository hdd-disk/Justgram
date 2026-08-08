package tw.nekomimi.nekogram;

import android.content.Context;
import android.text.method.LinkMovementMethod;

import androidx.appcompat.widget.AppCompatTextView;

import org.telegram.ui.ActionBar.Theme;

public class TextViewEffects extends AppCompatTextView {
    public TextViewEffects(Context context) {
        this(context, null);
    }

    public TextViewEffects(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        setMovementMethod(LinkMovementMethod.getInstance());
    }
}
