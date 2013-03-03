
package org.treasurehunter.activity.sources;

import org.treasurehunter.R;
import org.treasurehunter.menuactions.MenuAction;
import org.treasurehunter.menuactions.StaticLabelMenu;

import android.content.res.Resources;

import java.io.File;

public class MenuActionDeleteFile extends StaticLabelMenu implements MenuAction {
    private final SourcesTab mSourcesTab;

    private GpxSourceRow mSourceRow;

    public MenuActionDeleteFile(SourcesTab sourcesTab, Resources resources) {
        super(resources, R.string.menu_delete_file);
        mSourcesTab = sourcesTab;
    }

    public void setSourceRow(GpxSourceRow gpxSourceRow) {
        mSourceRow = gpxSourceRow;
    }

    @Override
    public void act() {
        if (mSourceRow.mPath == null)
            return;

        File file = new File(mSourceRow.mPath);
        file.delete();
        mSourcesTab.startUpdatingData();
    }

    @Override
    public boolean isEnabled() {
        if (mSourceRow.mPath == null)
            return false;

        File file = new File(mSourceRow.mPath);
        return file.exists();
    }

}
