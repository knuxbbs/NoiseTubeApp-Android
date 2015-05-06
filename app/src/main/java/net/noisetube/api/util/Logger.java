/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation)
 *
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2012
 *  Portions contributed by University College London (ExCiteS group), 2012
 * --------------------------------------------------------------------------------
 *  This library is free software; you can redistribute it and/or modify it under
 *  the terms of the GNU Lesser General Public License, version 2.1, as published
 *  by the Free Software Foundation.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along
 *  with this library; if not, write to:
 *    Free Software Foundation, Inc.,
 *    51 Franklin Street, Fifth Floor,
 *    Boston, MA  02110-1301, USA.
 *
 *  Full GNU LGPL v2.1 text: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *  NoiseTube project source code repository: http://code.google.com/p/noisetube
 * --------------------------------------------------------------------------------
 *  More information:
 *   - NoiseTube project website: http://www.noisetube.net
 *   - Sony Computer Science Laboratory Paris: http://csl.sony.fr
 *   - VUB BrusSense team: http://www.brussense.be
 * --------------------------------------------------------------------------------
 */

package net.noisetube.api.util;

import net.noisetube.api.SLMClient;
import net.noisetube.api.io.FileIO;
import net.noisetube.api.io.FileWriter;

import java.io.Serializable;
import java.util.Enumeration;

/**
 * Logger
 *
 * @author maisonneuve, mstevens, humberto
 */
public class Logger implements Serializable {

    //STATIC---------------------------------------------------------
    static public final int DEFAULT_CAPACITY = 70;
    static public final int ERROR = 0; //least verbose (only errors)
    static public final int INFORMATION = 1;
    static public final int DEBUG = 2; //most verbose
    /**
     *
     */
    private static final long serialVersionUID = 2L;
    static private Logger instance; //Singleton
    //DYNAMIC--------------------------------------------------------
    private int level; //current level
    private CyclicQueue<LogEntry> lineBuffer;  //the last X lines
    private SLMClient client;
    private FileWriter logFileWriter = null;
    private String logFilePath = null;

    private Logger() {
        level = INFORMATION; //default for production versions
        enableLogBuffer(DEFAULT_CAPACITY);
    }

    public static Logger getInstance() {
        if (instance == null)
            instance = new Logger();
        return instance;
    }

    public static void dispose() {
        instance = null;
    }

    public void enableLogBuffer(int capacity) {
        lineBuffer = new CyclicQueue<LogEntry>(capacity);
    }

    public void disableLogBuffer() {
        lineBuffer = null;
    }

    public void setClient(SLMClient client) {
        this.client = client;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isFileModeActive() {
        return (logFileWriter != null);
    }

    public void dumpCrashLog(String folderPath) throws Exception {
        if (client == null)
            throw new NullPointerException("NTClient cannot be null (Logger.dumpCrashLog())");
        try {
            logFileWriter = client.getFileWriter(folderPath + "Crash_" + StringUtils.formatDateTime(System.currentTimeMillis(), "-", "", "T") + ".log");
            logFileWriter.open(FileIO.FILE_EXISTS_STRATEGY_REPLACE, FileIO.FILE_DOES_NOT_EXIST_STRATEGY_CREATE);
            debug("Dumping crash log...");
            logFileWriter.write(getBuffer(true));
            logFileWriter.close();
            logFileWriter.dispose();
        } catch (Exception ignore) {
        } finally {
            logFileWriter = null;
        }
    }

    public void enableFileMode() {
        if (!isFileModeActive()) {
            if (client == null)
                throw new NullPointerException("NTClient cannot be null (Logger.enableFileMode())");
            try {
                String folderPath = client.getDataFolderPath();
                if (folderPath == null)
                    throw new Exception("No accessible data folder");
                logFilePath = folderPath + "Log_";
                if (client.isRestartingModeEnabled()) {
                    logFilePath += "RUNNING.log"; //will be renamed after last run
                    logFileWriter = client.getFileWriter(logFilePath);
                    if (client.isFirstRun() && logFileWriter.fileExists()) {    //This is a log file of an older unresumed running session, let's close it:
                        debug("Found unresumed \"RUNNING\" log file, renaming");
                        long lastChanged = logFileWriter.fileLastChanged();
                        logFileWriter.open(FileIO.FILE_EXISTS_STRATEGY_APPEND, FileIO.FILE_DOES_NOT_EXIST_STRATEGY_CREATE);
                        logFileWriter.writeLine("[LATER: " + StringUtils.formatDateTime(System.currentTimeMillis(), "/", ":", " ") + "] Session was never resumed, closing.");
                        logFileWriter.rename("Log_" + StringUtils.formatDateTime(lastChanged, "-", "", "T") + ".log", FileIO.FILE_EXISTS_STRATEGY_REPLACE);
                        logFileWriter.dispose(); //also calls close()
                        //Again take the (new) log file:
                        logFileWriter = client.getFileWriter(logFilePath);
                    }
                    logFileWriter.open(FileIO.FILE_EXISTS_STRATEGY_APPEND, FileIO.FILE_DOES_NOT_EXIST_STRATEGY_CREATE);
                } else {
                    logFilePath += StringUtils.formatDateTime(System.currentTimeMillis(), "-", "", "T") + ".log";
                    logFileWriter = client.getFileWriter(logFilePath);
                    logFileWriter.open(FileIO.FILE_EXISTS_STRATEGY_CREATE_RENAMED_FILE, FileIO.FILE_DOES_NOT_EXIST_STRATEGY_CREATE);
                }
                //write log buffer:
                logFileWriter.write(getBuffer(true));
                debug("Logger file mode enabled");
            } catch (Exception e) {
                disableFileMode();
                error(e, "Failed to enable file mode in Logger");
            }
        }
    }

    public void disableFileMode() {
        if (isFileModeActive()) {
            try {
                logFileWriter.close();
                if (client.isRestartingModeEnabled() && client.isLastRun())
                    logFileWriter.rename("Log_" + StringUtils.formatDateTime(System.currentTimeMillis(), "-", "", "T") + ".log", FileIO.FILE_EXISTS_STRATEGY_CREATE_RENAMED_FILE); //rename file to seal it
                logFileWriter.dispose();
            } catch (Exception ignore) {
            } finally {
                logFileWriter = null;
                logFilePath = null;
            }
        }
    }

    /**
     * @return the logFilePath
     */
    public String getLogFilePath() {
        return logFilePath;
    }

    public void debug(String msg) {
        if (level >= DEBUG)
            save("DEBUG: " + msg);
    }

    public void info(String msg) {
        if (level >= INFORMATION)
            save("INFO: " + msg);
    }

    public void error(String msg) {
        save("ERROR: " + msg);
    }

    public void error(Exception e, String comment) {
        save("[EXCEPTION]: " + e.getMessage() + " (" + comment + ")");
        if (SLMClient.ENVIRONMENT != SLMClient.PHONE_PROD_ENV)
            e.printStackTrace();
        if (client != null) {
            String info = client.additionalErrorReporting(e);
            if (info != null)
                save("Additional info: " + info);
        }
    }

    public void save(String msg) {
//        Log.e("Debugging", msg);
        if (SLMClient.ENVIRONMENT != SLMClient.PHONE_PROD_ENV)
            System.out.println(msg);
        LogEntry entry = new LogEntry(msg);
        if (lineBuffer != null)
            lineBuffer.offer(entry);
        if (isFileModeActive()) {
            synchronized (logFileWriter) {
                try {
                    logFileWriter.write(entry.toString() + "\n");
                } catch (Exception e) {
                    disableFileMode();
                }
            }
        }
    }

    public String getBuffer() {
        return getBuffer(false);
    }

    /**
     * @return log buffer contents as String
     */
    public String getBuffer(boolean withTimeStamps) {
        if (lineBuffer == null)
            return null;
        StringBuffer buf = new StringBuffer();
        Enumeration<LogEntry> lines = lineBuffer.getEnumeration();
        while (lines.hasMoreElements())
            buf.append((withTimeStamps ? (lines.nextElement()).toString() : (lines.nextElement()).getMessage()) + "\n");
        return buf.toString();
    }

    /**
     * @author mstevens
     */
    public class LogEntry implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 6341939536024379119L;
        private long timeStamp;
        private String msg;

        public LogEntry(String msg) {
            this.timeStamp = System.currentTimeMillis();
            this.msg = msg;
        }

        /**
         * @return the timeStamp
         */
        public long getTimeStamp() {
            return timeStamp;
        }

        /**
         * @return the msg
         */
        public String getMessage() {
            return msg;
        }

        public String toString() {
            return "[" + StringUtils.formatDateTime(timeStamp, "/", ":", " ") + "] " + msg;
        }

    }

}
