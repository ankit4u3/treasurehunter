
package org.treasurehunter.activity.sources;

import org.treasurehunter.R;
import org.treasurehunter.Toaster;
import org.treasurehunter.database.DatabaseLocator;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.menuactions.MenuAction;
import org.treasurehunter.menuactions.StaticLabelMenu;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.io.File;
import java.io.IOException;

public class MenuActionNewDatabase extends StaticLabelMenu implements MenuAction {
    private final Activity mActivity;

    private final DbFrontend mDbFrontend;

    private Dialog mDialog;

    public MenuActionNewDatabase(Activity activity, DbFrontend dbFrontend, Resources resources,
            int labelId) {
        super(resources, labelId);
        mActivity = activity;
        mDbFrontend = dbFrontend;
    }

    private static final char[] invalidFilenameChars = {
            '*', '/', '\\'
    // '`', '~', '!', '@', '#', '$', '%', '^', '&', '*', '=',
    // '[', ']', '{', '}', '\\', '|', ';', ':', '\'', '"', '<', ',', '>', '?'
    };

    private boolean hasAny(String input, char[] characters) {
        for (char nextChar : characters) {
            StringBuilder charString = new StringBuilder(1);
            charString.append(nextChar);
            if (input.contains(charString)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void act() {
        final OnClickListener mOnCancel = new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        };

        final OnClickListener mOnCreate = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (createNewDatabase()) {
                    mDbFrontend.closeDatabase();
                    setDefaultDatabase();
                    mDbFrontend.openDatabase();
                    mDialog.dismiss();
                }
            }

            private boolean createNewDatabase() {
                String dbName = getNewDatabaseName();
                if ((null == dbName) || (hasAny(dbName, invalidFilenameChars))) {
                    (new Toaster(mActivity, R.string.invalid_filename, true)).showToast();
                    return false;
                }
                String dbPath = DatabaseLocator.getStoragePath() + File.separator + dbName;
                File newDbFile = new File(dbPath);
                try {
                    newDbFile.createNewFile();
                } catch (IOException ioException) {
                    (new Toaster(mActivity, R.string.error_creating_file, true)).showToast();
                    return false;
                }
                return true;
            }

            private String getNewDatabaseName() {
                EditText editText = (EditText)mDialog.findViewById(R.id.database_name);
                String dbName = editText.getText().toString();
                if (!dbName.endsWith(".db")) {
                    dbName += ".db";
                }
                return dbName;
            }

            private void setDefaultDatabase() {
                if (userWantsDefault()) {
                    SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(mActivity);
                    SharedPreferences.Editor prefsEditor = prefs.edit();
                    String dbName = getNewDatabaseName();
                    prefsEditor.putString("use-database", dbName);
                    prefsEditor.commit();
                }
            }

            private boolean userWantsDefault() {
                CheckBox makeDefault = (CheckBox)mDialog.findViewById(R.id.set_current_database);
                return makeDefault.isChecked();
            }

        };

        mDialog = new Dialog(mActivity);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.create_new_database);

        Button create = (Button)mDialog.findViewById(R.id.create_database_name);
        create.setOnClickListener(mOnCreate);

        Button cancel = (Button)mDialog.findViewById(R.id.cancel_database_name);
        cancel.setOnClickListener(mOnCancel);

        mDialog.show();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
