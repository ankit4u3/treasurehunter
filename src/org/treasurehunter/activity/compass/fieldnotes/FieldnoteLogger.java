/*
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **     http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 */

package org.treasurehunter.activity.compass.fieldnotes;

import org.treasurehunter.Tags;
import org.treasurehunter.activity.main.GuiState;
import org.treasurehunter.database.DbFrontend;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.widget.EditText;

public class FieldnoteLogger {
    public static class OnClickCancel implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int whichButton) {
            dialog.dismiss();
        }
    }

    public static class OnClickOk implements OnClickListener {
        private final CacheLogger mCacheLogger;

        private final boolean mDnf;

        private final EditText mEditText;

        private final DbFrontend mDbFrontend;

        private final CharSequence mGeocacheId;

        private final GuiState mGuiState;

        public OnClickOk(CharSequence geocacheId, EditText editText, CacheLogger cacheLogger,
                DbFrontend dbFrontend, GuiState guiState, boolean dnf) {
            mGeocacheId = geocacheId;
            mEditText = editText;
            mCacheLogger = cacheLogger;
            mDbFrontend = dbFrontend;
            mGuiState = guiState;
            mDnf = dnf;
        }

        @Override
        public void onClick(DialogInterface arg0, int arg1) {
            mCacheLogger.log(mGeocacheId, mEditText.getText(), mDnf);
            mDbFrontend.setGeocacheTag(mGeocacheId, Tags.DNF, mDnf);
            mDbFrontend.setGeocacheTag(mGeocacheId, Tags.FOUND, !mDnf);
            mGuiState.notifyDataViewChanged();
        }
    }

    static final String FIELDNOTES_FILE = "/sdcard/TreasureHunterFieldNotes.txt";

    private final DialogHelperCommon mDialogHelperCommon;

    private final DialogHelperFile mDialogHelperFile;

    private final DialogHelperSms mDialogHelperSms;

    private final Dialog mDialog;

    public FieldnoteLogger(DialogHelperCommon dialogHelperCommon,
            DialogHelperFile dialogHelperFile, DialogHelperSms dialogHelperSms, Dialog dialog) {
        mDialogHelperSms = dialogHelperSms;
        mDialogHelperFile = dialogHelperFile;
        mDialogHelperCommon = dialogHelperCommon;
        mDialog = dialog;
    }

    public Dialog getDialog() {
        return mDialog;
    }

    public void onPrepareDialog(SharedPreferences defaultSharedPreferences, String localDate) {
        final boolean fieldNoteTextFile = defaultSharedPreferences.getBoolean(
                "field-note-text-file", false);
        DialogHelper dialogHelper = fieldNoteTextFile ? mDialogHelperFile : mDialogHelperSms;

        dialogHelper.configureDialogText(getDialog());
        mDialogHelperCommon.configureDialogText();

        dialogHelper.configureEditor();
        mDialogHelperCommon.configureEditor(localDate);
    }
}
