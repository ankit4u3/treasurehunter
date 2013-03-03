
package org.treasurehunter.xmlimport;

import org.treasurehunter.xmlimport.ImportDirectoryTask.FileFilter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Iterates over the GPX/LOC files and entries in ZIP files in a certain
 * directory
 */
public class DirectoryIterator {
    public static class SourceReader {
        private final String mFilename;

        private final Reader mReader;

        public SourceReader(String filename, Reader reader) {
            mFilename = filename;
            mReader = reader;
        }

        public String getFilename() {
            return mFilename;
        }

        public Reader getReader() {
            return mReader;
        }
    }

    private static class ZipFileIter {
        private final AbortFlag mAbortFlag;

        private ZipEntry mNextZipEntry;

        private final ZipInputStream mZipInputStream;

        ZipFileIter(String filename, AbortFlag abortFlag) throws FileNotFoundException {
            BufferedInputStream bufferedStream = new BufferedInputStream(new FileInputStream(
                    filename));
            mZipInputStream = new ZipInputStream(bufferedStream);
            mNextZipEntry = null;
            mAbortFlag = abortFlag;
        }

        public boolean hasNext() throws IOException {
            // Iterate through zip file entries.
            if (mNextZipEntry == null) {
                do {
                    if (mAbortFlag.isAborted())
                        break;
                    mNextZipEntry = mZipInputStream.getNextEntry();
                } while (mNextZipEntry != null && !isValid(mNextZipEntry));
            }
            return mNextZipEntry != null;
        }

        private boolean isValid(ZipEntry zipEntry) {
            return (!zipEntry.isDirectory() && hasCleartextSuffix(zipEntry.getName()));
        }

        public SourceReader next() throws IOException {
            final String name = mNextZipEntry.getName();
            mNextZipEntry = null;
            return new SourceReader(name, new InputStreamReader(mZipInputStream));
        }
    }

    public static boolean hasCleartextSuffix(String name) {
        name = name.toLowerCase();
        return name.endsWith(".gpx") || name.endsWith(".loc");
    }

    // ///////////////////////////////////////////////////////////////

    private final String mDirName;

    private final AbortFlag mAbortFlag;

    private String[] mFiles = null;

    private int mFilesIx = 0;

    private ZipFileIter mZipFileIter = null;

    public DirectoryIterator(String dirName, AbortFlag abortFlag) {
        mDirName = dirName;
        mAbortFlag = abortFlag;
    }

    private static String[] initFiles(String dirName) throws IOException {
        File dir = new File(dirName);
        String[] files = dir.list(new FileFilter());
        if (files == null)
            throw new IOException("Could not list directory " + dirName);
        return files;
    }

    public SourceReader next() throws IOException, NoSuchElementException {
        if (mFiles == null)
            mFiles = initFiles(mDirName);

        if (mZipFileIter != null) {
            try {
                if (mZipFileIter.hasNext()) {
                    return mZipFileIter.next();
                }
            } catch (NoSuchElementException ex) {
            }
            // No more entries in the zip file
            mZipFileIter = null;
        }

        if (mFiles.length <= mFilesIx)
            throw new NoSuchElementException();
        String filename = mFiles[mFilesIx];
        mFilesIx += 1;

        if (filename.toLowerCase().endsWith(".zip")) {
            mZipFileIter = new ZipFileIter(mDirName + '/' + filename, mAbortFlag);
            return next();
        }

        // Not guarding against FileNotFoundException since we know the file
        // exists
        FileReader fileReader = new FileReader(mDirName + '/' + filename);
        return new SourceReader(filename, new BufferedReader(fileReader));
    }
}
