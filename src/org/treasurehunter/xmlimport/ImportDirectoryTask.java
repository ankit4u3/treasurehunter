
package org.treasurehunter.xmlimport;

import org.treasurehunter.ErrorDisplayer;
import org.treasurehunter.R;
import org.treasurehunter.Source;
import org.treasurehunter.SourceFactory;
import org.treasurehunter.task.Task;
import org.treasurehunter.xmlimport.DirectoryIterator.SourceReader;
import org.xmlpull.v1.XmlPullParserException;

import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.NoSuchElementException;

public class ImportDirectoryTask extends Task {
    static class FileFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            if (name.startsWith("."))
                return false;
            name = name.toLowerCase();
            return name.endsWith(".zip") || DirectoryIterator.hasCleartextSuffix(name);
        }
    }

    private final ProcessStatus mProcessStatus;

    private final String mDirName;

    private final ErrorDisplayer mErrorDisplayer;

    private final SourceFactory mSourceFactory;

    private final String mUsername;

    public ImportDirectoryTask(ProcessStatus processStatus, String dir,
            ErrorDisplayer errorDisplayer, SourceFactory sourceFactory, String username) {
        mProcessStatus = processStatus;
        mDirName = dir;
        mErrorDisplayer = errorDisplayer;
        mSourceFactory = sourceFactory;
        mUsername = username;
    }

    protected void doInBackground(Handler handler) {
        mProcessStatus.willStartLoading();
        boolean success = false;
        try {
            tryFilesSync(mDirName);
            success = true; // No assertions thrown
        } catch (final FileNotFoundException e) {
            Log.w("TreasureHunter", "FileNotFoundException " + e.getMessage());
            mErrorDisplayer.displayError(R.string.error_opening_file, e.getMessage());
        } catch (IOException e) {
            Log.w("TreasureHunter", "IOException " + e.getMessage());
            mErrorDisplayer.displayError(R.string.error_reading_file, e.getMessage());
        } catch (XmlPullParserException e) {
            Log.w("TreasureHunter", "XmlPullParserException " + e.getMessage());
            mErrorDisplayer.displayError(R.string.error_parsing_file, e.getMessage());
        } finally {
            mProcessStatus.stoppedAllLoading(success);
        }
    }

    private void tryFilesSync(String dirName) throws IOException, XmlPullParserException {
        DirectoryIterator dirIter = new DirectoryIterator(dirName, this);
        boolean hasFiles = false;
        while (true) {
            if (isAborted())
                return;
            SourceReader reader;
            try {
                reader = dirIter.next();
            } catch (NoSuchElementException ex) {
                if (!hasFiles)
                    mErrorDisplayer.displayError(R.string.error_no_gpx_files, dirName);
                return;
            }
            hasFiles = true;
            try {
                Source source = mSourceFactory.fromFile(reader.getFilename());
                XmlFiniteStateMachine.importFromReader(source, reader.getReader(), mProcessStatus,
                        this, mUsername);
            } catch (XmlPullParserException ex) {
                throw new XmlPullParserException("File " + reader.getFilename() + " "
                        + ex.getMessage());
            }
        }
    }
}
