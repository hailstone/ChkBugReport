package com.sonyericsson.chkbugreport;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Context contains various configurations which affects the whole processing.
 */
public class Context {

    // Log cache
    private Vector<String> mLogCache = new Vector<String>();
    private PrintStream mOut = null;
    private OutputListener mOutListener;
    // Time window markers
    private TimeWindowMarker mTimeWindowStart = new TimeWindowMarker();
    private TimeWindowMarker mTimeWindowEnd = new TimeWindowMarker();
    // GMT offset
    private int mGmtOffset = 0;
    // URL to ChkBugReport's homepage
    private String mHomePageUrl = "http://github.com/sonyxperiadev/ChkBugReport";

    /**
     * Returns the url to ChkBugReport's homepage
     * @return the url to ChkBugReport's homepage
     */
    public String getHomePageUrl() {
        return mHomePageUrl;
    }

    /**
     * Changes the url which will be used as ChkBugReport's homepage.
     * This could be used to redirect the link to internal website in organizations.
     * @param url The new url
     */
    public void setHomePageUrl(String url) {
        mHomePageUrl = url;
    }


    /**
     * Returns the starting point of the time window to limit the logs to.
     * If this value is set, the plugins processing the logs should ignore every line who's
     * timestamp is below this value.
     *
     * @return the starting point of the time window to limit the logs to.
     */
    public TimeWindowMarker getTimeWindowStart() {
        return mTimeWindowStart;
    }

    /**
     * Returns the ending point of the time window to limit the logs to.
     * If this value is set, the plugins processing the logs should ignore every line who's
     * timestamp is above this value.
     *
     * @return the ending point of the time window to limit the logs to.
     */
    public TimeWindowMarker getTimeWindowEnd() {
        return mTimeWindowEnd;
    }

    /**
     * Returns the GMT offset/timezone of the logs.
     * It's used to map the kernel timestamps with the log timestamps.
     * @return the GMT offset/timezone of the logs.
     */
    public int getGmtOffset() {
        return mGmtOffset;
    }

    /* package */ void parseTimeWindow(String timeWindow) {
        try {
            Matcher m = Pattern.compile("(.*)\\.\\.(.*)").matcher(timeWindow);
            if (!m.matches()) {
                throw new IllegalArgumentException("Incorrect time window range");
            }
            mTimeWindowStart = new TimeWindowMarker(m.group(1));
            mTimeWindowEnd = new TimeWindowMarker(m.group(2));
        } catch (Exception e) {
            System.err.println("Error parsing timewindow: `" + timeWindow + "': " + e);
            System.exit(1);
        }
    }

    /* package */ void parseGmtOffset(String param) {
        try {
            if (param.startsWith("+")) {
                param = param.substring(1);
            }
            mGmtOffset = Integer.parseInt(param);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing gmt offset: `" + param + "': " + e);
            System.exit(1);
        }
    }

    /* package */ void printOut(int level, String s) {
        String line = " <" + level + "> " + s;
        if (mOut == null) {
            mLogCache.add(line);
        } else {
            mOut.println(line);
        }
        if (mOutListener != null) {
            mOutListener.onPrint(level, OutputListener.TYPE_OUT, s);
        }
    }

    /* package */ void printErr(int level, String s) {
        String line = "!<" + level + "> " + s;
        if (mOut == null) {
            mLogCache.add(line);
        } else {
            mOut.println(line);
        }
        if (mOutListener != null) {
            mOutListener.onPrint(level, OutputListener.TYPE_ERR, s);
        }
    }

    /* package */ void setLogOutput(String logName) {
        if (mOut == null) {
            try {
                File f = new File(logName);
                f.getParentFile().mkdirs();
                mOut = new PrintStream(f);
                for (String line : mLogCache) {
                    mOut.println(line);
                }
                mLogCache.clear();
            } catch (IOException e) {
                System.err.println("Error opening output log file: " + e);
            }
        }
    }

    /* package */ void setOutputListener(OutputListener listener) {
        mOutListener = listener;
    }

}
