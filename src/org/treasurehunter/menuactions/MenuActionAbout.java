
package org.treasurehunter.menuactions;

import org.treasurehunter.R;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;
import android.webkit.WebView;

public class MenuActionAbout extends StaticLabelMenu implements MenuAction {
    private final Activity mActivity;

    public MenuActionAbout(Activity activity) {
        super(activity.getResources(), R.string.menu_about);
        mActivity = activity;
    }

    @Override
    public void act() {
        final Dialog dialog = new Dialog(mActivity);
        // dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        dialog.setTitle(mActivity.getString(R.string.menu_about));
        dialog.setContentView(R.layout.about);
        dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon);

        WebView webView = (WebView)dialog.findViewById(R.id.about_contents);
        webView.loadUrl("file:///android_asset/about.html");
        dialog.show();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
