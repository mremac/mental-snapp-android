package com.SMobiLogger;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SLogManager {

    private SQLiteDatabase db;
    private Context context;
    private static final int MAX_RECORDS_FROM_DB = 15000;
    private File logFile;
    private FileWriter fileWriter;
    private int mJSONExceptionCount;
    private int mNullPointerException;
    private int mSQLiteException;
    private int mIOException;
    private int mClassNotFoundException;
    private int mNameNotFoundException;
    private int mUnsupportedEncodingException;
    private int mIllegalStateException;
    private int mClientProtocolException;
    private int mUnknownHostException;
    private int mOtherExceptionCount;
    private String mOtherError;

    public SLogManager(Context context) {
        if (db == null)
            db = DatabaseHelper.getDatabase(context);

        boolean isServiceStarted = DatabaseHelper.getPreferences(context)
                .getBoolean(DatabaseHelper.IS_SERVICE_STARTED, false);
        if (!isServiceStarted) {
            // To start a service for finding updated DB, which deletes old logs
            Intent serviceIntent = new Intent(context, DBUpdater.class);
            context.startService(serviceIntent);
        }

        this.context = context;
    }

    // Save logs with particular type in DB
    public void insert(SLog dataLog) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.ISSUE_DATE, dataLog.getlIssueDate());
        values.put(DatabaseHelper.TYPE, dataLog.getlType().getType());
        values.put(DatabaseHelper.TITLE, dataLog.getlTitle());
        values.put(DatabaseHelper.DESCRIPTION, dataLog.getlDescription());
        // #10939, Changes on Report issue screen
        if (dataLog.getlLogDate() == null)
            dataLog.setlLogDate("");
        values.put(DatabaseHelper.LOG_DATE, dataLog.getlLogDate());
        // End #10939
        db.insert(DatabaseHelper.TABLE, null, values);
    }

    // To delete old logs from DB
    public void refreshLogs() {
        SharedPreferences preferences = DatabaseHelper.getPreferences(context);
        int days = preferences.getInt(DatabaseHelper.UPDATE_DB_IN,
                DatabaseHelper.UPDATE_DB_IN_VALUE);
        long millis = days * 24 * 60 * 60 * 1000;
        long updateTime = (new Date()).getTime() - millis;

        db.delete(DatabaseHelper.TABLE, DatabaseHelper.ISSUE_DATE + " < ? ",
                new String[]{Long.toString(updateTime)});
    }

    // To fetch all the logs from DB
    public File fetchLogs() {
        String projection[] = new String[]{DatabaseHelper.LOG_DATE,
                DatabaseHelper.TYPE, DatabaseHelper.TITLE,
                DatabaseHelper.DESCRIPTION};
        String dumpCursorString = "";
        Cursor cursor = db.query(DatabaseHelper.TABLE, projection, null, null,
                null, null, DatabaseHelper.ISSUE_DATE + " Desc",
                Integer.toString(MAX_RECORDS_FROM_DB));
        StringBuilder sb = new StringBuilder();
        sb.append(">>>>> Dumping cursor " + cursor + "\n");
        File root = new File(Environment.getExternalStorageDirectory(), "/Slog");
        if (!root.exists()) {
            root.mkdirs();
        }
        logFile = new File(root, "MentalSnappLogs.txt");
        File zipFile = new File(root, "MentalSnappLogs.zip");
        try {
            fileWriter = new FileWriter(logFile, true);
        } catch (IOException e1) {
            e1.printStackTrace();
            // #6911- Logger Integration
            StringWriter stackTrace = new StringWriter();
            e1.printStackTrace(new PrintWriter(stackTrace));
            SLog.e(context, "Exception while writing file", "Details:- [ "
                    + stackTrace + " ]");
        }
        try {
            dumpCursor(cursor, sb);
            // ByteArrayOutputStream byteArrayOutputStream = new
            // ByteArrayOutputStream();
            // PrintStream printStream = new PrintStream(byteArrayOutputStream);
            // DatabaseUtils.dumpCursor(cursor, printStream);
            // return byteArrayOutputStream;

            //To zip the created file
            zipIt(logFile, zipFile);
            logFile.delete();

        } catch (OutOfMemoryError error) {
            Log.i("OutOfMemoryError", "While fetcing records from DB");
            SLog.e(context, "OutOfMemoryError",
                    "Details:- [ While dumping cursor ]");
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter stackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTrace));
            SLog.e(context, "Exception while writing file", "Details:- [ "
                    + stackTrace + " ]");
        } finally {
            cursor.close();
        }
        Log.i("dump cursor", dumpCursorString);
        return zipFile;
    }

    // To send logs via email 
    void sendEmailLogs() {

    }

    private String appendPrefix() {
        String versioName = "";
        try {
            versioName = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;

        } catch (NameNotFoundException e) {
            Log.e("SLOG", e.toString(), e);
            SLog.e(context, "Exception while appending prefix in log file",
                    "Details:- NameNotFoundException");
        }
        SharedPreferences preferences = context.getSharedPreferences(
                "FeelShare", Context.MODE_PRIVATE);
        String deviceId = preferences.getString("device_id", "");
        String androidId = Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID);
        return "[Device Details: {" + "\n " + "Dash" + " APP VERSION= "
                + versioName + "\n DEVICE MANUFACTURER= "
                + android.os.Build.MANUFACTURER + ",\n DEVICE MODEL= "
                + android.os.Build.MODEL + ",\n Android OS VERSION= "
                + android.os.Build.VERSION.RELEASE
                + ",\n Android API VERSION= "
                + android.os.Build.VERSION.SDK_INT
                + ",\n GCM REGISTRATION ID= " + deviceId + ",\n Android ID= "
                + androidId + "}]\n\n";
    }

    public void dumpCursor(Cursor cursor, StringBuilder sb) {
        int MAX_LIMIT = 3000;
        int cursorLength = cursor.getCount();
        long startTime = System.currentTimeMillis();
        try {
            fileWriter.append(appendPrefix());
            /*fileWriter.append(generateLogSummary());*/
        } catch (IOException e) {
            StringWriter stackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTrace));
            SLog.e(context,
                    "Exception while executing fileWriter.append(appendPrefix()",
                    "Details:- [ " + stackTrace + " ]");
        }
        if (cursor != null) {
            int startPos = cursor.getPosition();
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                if (cursor.getPosition() == MAX_LIMIT
                        || cursor.getPosition() == (cursorLength - 1)) {
                    /*Log.i("total time in dumping 3000 rows",
							"abc"
									+ Long.toString(System.currentTimeMillis()
									- startTime));*/
                    try {
                        long startTime1 = System.currentTimeMillis();
                        fileWriter.append(sb.toString());
						/*Log.i("total time in writing 3000 rows in file",
								"abc"
										+ Long.toString(System
										.currentTimeMillis()
										- startTime1));*/
                    } catch (IOException e) {
                        StringWriter stackTrace = new StringWriter();
                        e.printStackTrace(new PrintWriter(stackTrace));
                        SLog.e(context,
                                "Exception while executing fileWriter.append(sb.toString());",
                                "Details:- [ " + stackTrace + " ]");
                    }
                    sb = new StringBuilder();
                    if (MAX_LIMIT >= 15000
                            || cursor.getPosition() == (cursorLength - 1))
                        break;
                    MAX_LIMIT = MAX_LIMIT + 3000;
                }
                DatabaseUtils.dumpCurrentRow(cursor, sb);
            }
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                StringWriter stackTrace = new StringWriter();
                e.printStackTrace(new PrintWriter(stackTrace));
                SLog.e(context,
                        "Exception while closing and flushing fileWriter",
                        "Details:- [ " + stackTrace + " ]");
            }
            cursor.moveToPosition(startPos);
        }
    }

    // TODO This method might be in use in future for improving writing
    // operation in file
    // @SuppressLint("NewApi")
    // public void writeToFileNIOWay2(String messageToWrite) throws IOException
    // {
    // final int numberOfIterations = 3000;
    // final byte[] messageBytes = messageToWrite.
    // getBytes();
    // final long appendSize = numberOfIterations * messageBytes.length;
    // final RandomAccessFile raf = new RandomAccessFile(gpxfile, "rw");
    // raf.seek(raf.length());
    // final FileChannel fc = raf.getChannel();
    // final MappedByteBuffer mbf = fc.map(FileChannel.MapMode.READ_WRITE, fc.
    // position(), appendSize);
    // fc.close();
    // for (int i = 1; i < numberOfIterations; i++) {
    // mbf.put(messageBytes);
    // }
    // }

    public String generateLogSummary() {
        if (db == null)
            db = DatabaseHelper.getDatabase(context);
        String summary = "";
        String whereClause = "lType = 'Error'";
        String projection[] = new String[]{DatabaseHelper.LOG_DATE,
                DatabaseHelper.TYPE, DatabaseHelper.TITLE,
                DatabaseHelper.DESCRIPTION};
        Cursor cursor = db.query(DatabaseHelper.TABLE, projection, whereClause,
                null, null, null, DatabaseHelper.ISSUE_DATE + " Desc",
                Integer.toString(1000));
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(2);
                if (title.equalsIgnoreCase("IOException")) {
                    mIOException++;
                } else if (title.equalsIgnoreCase("JSONException")) {
                    mJSONExceptionCount++;
                } else if (title.equalsIgnoreCase("SQLiteException")) {
                    mSQLiteException++;
                } else if (title.equalsIgnoreCase("ClassNotFoundException")) {
                    mClassNotFoundException++;
                } else if (title.equalsIgnoreCase("NameNotFoundException")) {
                    mNameNotFoundException++;
                } else if (title
                        .equalsIgnoreCase("UnsupportedEncodingException")) {
                    mUnsupportedEncodingException++;
                } else if (title.equalsIgnoreCase("IllegalStateException")) {
                    mIllegalStateException++;
                } else if (title.equalsIgnoreCase("ClientProtocolException")) {
                    mClientProtocolException++;
                } else if (title.equalsIgnoreCase("UnknownHostException")) {
                    mUnknownHostException++;
                } else if (title.equalsIgnoreCase("Exception")) {
                    mOtherExceptionCount++;
                }
//				else {
//					if (mOtherError == null) {
//						mOtherError = "Few other errors are following \n";
//					} else {
//						mOtherError = mOtherError + title + "\n";
//					}
//				}
            }
        }
        summary = "[log\'s summary: {" + "\n " + "IOException Count = "
                + mIOException + ",\n JSONExceptionCount = "
                + mJSONExceptionCount + ",\n SQLiteExceptionCount = "
                + mSQLiteException + ",\n ClassNotFoundExceptionCount = "
                + mClassNotFoundException + ",\n NameNotFoundExceptionCount = "
                + mNameNotFoundException
                + ",\n UnsupportedEncodingExceptionCount = "
                + mUnsupportedEncodingException
                + ",\n IllegalStateExceptionCount = " + mIllegalStateException
                + ",\n ClientProtocolExceptionCount = "
                + mClientProtocolException + ",\n UnknownHostExceptionCount = "
                + mUnknownHostException + ",\n OtherExceptionCount = "
                + mOtherExceptionCount + "}]\n\n";
//		if (mOtherError != null && mOtherError.length() > 0)
//			summary = summary + mOtherError + "\n\n";
        return summary;
    }

    /**
     * Zip it
     *
     * @param destFile output ZIP file location
     */
    public void zipIt(File sourceFile, File destFile) {

        byte[] buffer = new byte[1024];

        try {

            FileOutputStream fos = new FileOutputStream(destFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            System.out.println("Output to Zip : " + destFile);

            ZipEntry ze = new ZipEntry(sourceFile.getAbsolutePath());
            zos.putNextEntry(ze);
            FileInputStream in = new FileInputStream(sourceFile);

            int len;
            while ((len = in.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            zos.closeEntry();
            //remember close it
            zos.close();

            System.out.println("Done");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}