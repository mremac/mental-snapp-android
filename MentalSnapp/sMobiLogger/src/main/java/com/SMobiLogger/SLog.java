package com.SMobiLogger;

import java.io.File;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;

public class SLog {

	// #10939, Changes on Report issue screen
	private String lLogDate;
	// End #10939
	private long lIssueDate;
	private LogType lType;
	private String lTitle;
	private String lDescription;

	public SLog(String lLogDate, long lIssueDate, LogType lType, String lTitle,
				String lDescription) {
		super();
		// #10939, Changes on Report issue screen
		this.lLogDate = lLogDate;
		// End #10939
		this.lIssueDate = lIssueDate;
		this.lType = lType;
		this.lTitle = lTitle;
		this.lDescription = lDescription;
	}

	public long getlIssueDate() {
		return lIssueDate;
	}

	public void setlIssueDate(long lIssueDate) {
		this.lIssueDate = lIssueDate;
	}

	// #10939, Changes on Report issue screen
	public String getlLogDate() {
		return lLogDate;
	}

	public void setlLogDate(String lLogDate) {
		this.lLogDate = lLogDate;
	}

	// End #10939

	public LogType getlType() {
		return lType;
	}

	public void setlType(LogType lType) {
		this.lType = lType;
	}

	public String getlTitle() {
		return lTitle;
	}

	public void setlTitle(String lTitle) {
		this.lTitle = lTitle;
	}

	public String getlDescription() {
		return lDescription;
	}

	public void setlDescription(String lDescription) {
		this.lDescription = lDescription;
	}

	@Override
	public String toString() {
		// return lType + " on " + new Date(lIssueDate) + ": " + lTitle +" - " +
		// lDescription;
		// return new Date(lIssueDate) + " " + lType + " " + lTitle +" " +
		// lDescription + " ";
		// return "LogDate: " + new Date(lIssueDate) + ", Type: " + lType +
		// ", Title: " + lTitle +", Description: " + lDescription;
		// #10939, Changes on Report issue screen
		// return new Date(lIssueDate) + "- " + lType + ": " + lTitle + "- "
		// + lDescription + "\n\n";
		return lLogDate + new Date(lIssueDate) + "- " + lType + ": " + lTitle
				+ "- " + lDescription + "\n\n";
		// End #10939
	}

	// Save logs with particular types
	public static void a(Context context, String title, String description) {
		SLog dataLog = new SLog((new Date()).toString(),
				(new Date()).getTime(), LogType.ASSERT, title, description);
		new SLogManager(context).insert(dataLog);
	}

	public static void v(Context context, String title, String description) {
		SLog dataLog = new SLog((new Date()).toString(),
				(new Date()).getTime(), LogType.VERBOSE, title, description);
		new SLogManager(context).insert(dataLog);
	}

	public static void d(Context context, String title, String description) {
		SLog dataLog = new SLog((new Date()).toString(),
				(new Date()).getTime(), LogType.DEBUG, title, description);
		new SLogManager(context).insert(dataLog);
	}

	public static void i(Context context, String title, String description) {
		SLog dataLog = new SLog((new Date()).toString(),
				(new Date()).getTime(), LogType.INFO, title, description);
		new SLogManager(context).insert(dataLog);
	}

	public static void w(Context context, String title, String description) {
		SLog dataLog = new SLog((new Date()).toString(),
				(new Date()).getTime(), LogType.WARN, title, description);
		new SLogManager(context).insert(dataLog);
	}

	public static void e(Context context, String title, String description) {
		SLog dataLog = new SLog((new Date()).toString(),
				(new Date()).getTime(), LogType.ERROR, title, description);
		new SLogManager(context).insert(dataLog);
	}

	public static void other(Context context, String title, String description) {
		SLog dataLog = new SLog((new Date()).toString(),
				(new Date()).getTime(), LogType.OTHER, title, description);
		new SLogManager(context).insert(dataLog);
	}

	public static void setUpdateDBTime(Context context, int day) {
		SharedPreferences preferences = DatabaseHelper.getPreferences(context);
		preferences.edit().putInt(DatabaseHelper.UPDATE_DB_IN, day).commit();
	}

	public static File fetchLogs(Context context) {
		return new SLogManager(context).fetchLogs();
	}

	/*public static String generateDeviceSummary(Context context) {
		return new SLogManager(context).generateLogSummary();
	}*/

}