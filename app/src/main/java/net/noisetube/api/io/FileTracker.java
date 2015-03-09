package net.noisetube.api.io;

import net.noisetube.api.util.CyclicQueue;
import net.noisetube.api.util.Logger;
import net.noisetube.api.util.TrackXMLHandler;
import net.noisetube.app.config.AndroidPreferences;
import net.noisetube.app.ui.model.TrackData;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * @author Humberto
 */
public class FileTracker {

    protected static Logger log = Logger.getInstance();

    public static File[] getTracksPendingToUpload() {
        AndroidPreferences preferences = AndroidPreferences.getInstance();
        String folderPath = preferences.getDataFolderPath();
        if (folderPath == null)
            throw new NullPointerException("folderPath is null");

        File tracesFolder = new File(folderPath);
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.startsWith("TrackID_PENDING");
            }
        };

        File[] traces = tracesFolder.listFiles(filter);

        return traces;

    }

    public static CyclicQueue<TrackData> loadUserTraces() {
        AndroidPreferences preferences = AndroidPreferences.getInstance();
        String folderPath = preferences.getDataFolderPath();
        if (folderPath == null)
            throw new NullPointerException("folderPath is null");

        File tracesFolder = new File(folderPath);
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.startsWith("TrackID_");
            }
        };

        File[] traces = tracesFolder.listFiles(filter);

        if (traces != null && traces.length > 0) {
            Comparator<File> comparator = new Comparator<File>() {
                @Override
                public int compare(File lhs, File rhs) {
                    if (lhs.lastModified() < rhs.lastModified()) {
                        return 1;
                    } else if (lhs.lastModified() > rhs.lastModified()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            };

            Comparator<TrackData> comparator2 = new Comparator<TrackData>() {
                @Override
                public int compare(TrackData lhs, TrackData rhs) {
                    if (lhs.getCreationDate().getTime() > rhs.getCreationDate().getTime()) {
                        return 1;
                    } else if (lhs.getCreationDate().getTime() < rhs.getCreationDate().getTime()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            };

            Arrays.sort(traces, comparator);

            int max = preferences.getTrackHistoryValue();
            final int length = traces.length;
            if (max > length) {
                max = length;
            } else {
                removeOlderTracks(traces, max, length);
            }

            CyclicQueue<TrackData> userTraces = new CyclicQueue<TrackData>(preferences.getTrackHistoryValue());

            ArrayList<TrackData> trackDataList = new ArrayList<TrackData>(max);

            try {
                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXParser sp = spf.newSAXParser();
                TrackData trackData;
                TrackXMLHandler handler;

                for (int i = 0; i < max; i++) {
                    handler = new TrackXMLHandler();
                    try {
                        sp.parse(traces[i], handler);
                        trackData = new TrackData(getTrackId(traces[i]), handler.getStartTime(), handler.getMeasurements());
                        trackDataList.add(trackData);
                    } catch (Exception e) {
                        log.error(e, "error reading file " + traces[i].getName());
                        traces[i].delete();
                    }


                }

                Collections.sort(trackDataList, comparator2);

                userTraces.setValues(trackDataList);


            } catch (ParserConfigurationException e) {
                log.error(e, "loadUserTraces");
            } catch (SAXException e) {
                log.error(e, "loadUserTraces");
            }

            return userTraces;

        }

        return new CyclicQueue<TrackData>(10);
    }

    private static void removeOlderTracks(final File[] tracks, final int from, final int to) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = from; i < to; i++) {
                    tracks[i].delete();
                }

            }
        });

        t.start();

    }


    public static void removeOlderLogs() {
        AndroidPreferences preferences = AndroidPreferences.getInstance();
        final int trackHistoryMaxValue = preferences.getTrackHistoryValue();
        final String folderPath = preferences.getDataFolderPath();
        if (folderPath == null)
            throw new NullPointerException("folderPath is null");

        File logsFolder = new File(folderPath);


        FilenameFilter logsFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.startsWith("Log");
            }
        };

        File[] logs = logsFolder.listFiles(logsFilter);
        remove(logs);

        FilenameFilter crashFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.startsWith("Crash");
            }
        };

        File[] crashes = logsFolder.listFiles(crashFilter);
        remove(crashes);

    }

    private static void remove(File[] logs) {
        if (logs != null && logs.length > 0) {

            Comparator<File> comparator = new Comparator<File>() {
                @Override
                public int compare(File lhs, File rhs) {
                    if (lhs.lastModified() < rhs.lastModified()) {
                        return 1;
                    } else if (lhs.lastModified() > rhs.lastModified()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            };

            Arrays.sort(logs, comparator);

            final int logsTotal = logs.length;
            final int logHistoryMaxValue = 3;
            if (logsTotal > logHistoryMaxValue) {
                for (int i = logHistoryMaxValue; i < logsTotal; i++) {
                    logs[i].delete();
                }
            }

        }
    }


    public static void removeFiles() {
        AndroidPreferences preferences = AndroidPreferences.getInstance();

        final String folderPath = preferences.getDataFolderPath();
        if (folderPath == null)
            throw new NullPointerException("folderPath is null");

        File appFolder = new File(folderPath);
        File[] files = appFolder.listFiles();

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                files[i].delete();
            }
        }


    }

    private static String getTrackId(File file) {
        return file.getName().split("_")[1];
    }


}




