package com.SMobiLogger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	Context context;

	// any time you make changes to your database objects, you may have to
	// increase the database version
	private static final String DATABASE_NAME = "SMobiLogger.sqlite";
	private static final int DATABASE_VERSION = 2;

	// DB details
	public static final String TABLE = "SLog";
	public static final String ISSUE_DATE = "lIssueDate";
	public static final int COL_ISSUE_DATE = 0;
	public static final String TYPE = "lType";
	public static final int COL_TYPE = 1;
	public static final String TITLE = "lTitle";
	public static final int COL_TITLE = 2;
	public static final String DESCRIPTION = "lDescription";
	public static final int COL_DESCRIPTION = 3;
	//#10939, Changes on Report issue screen
	public static final String LOG_DATE = "lLogDate";
	public static final int COL_LOG_DATE = 4;
	//End #10939

	// the common db object
	private static SQLiteDatabase db;

	// Shared Preference name
	private static final String SHARED_PREF = "SMobiLogger";
	public static final String UPDATE_DB_IN = "UpdateDatabaseInDays";
	public static final String IS_SERVICE_STARTED = "ServiceStarted";
	public static final int UPDATE_DB_IN_VALUE = 7;

	// the common Shared Preference object
	private static SharedPreferences preferences;

	private DatabaseHelper(Context context, String name, CursorFactory factory,
						   int version) {
		super(context, name, factory, version);
		this.context = context;
	}

	public static SQLiteDatabase getDatabase(Context context) {
		getPreferences(context);
		if (db == null) {
			db = (new DatabaseHelper(context, DATABASE_NAME, null,
					DATABASE_VERSION)).getWritableDatabase();
		}
		return db;
	}

	public static SharedPreferences getPreferences(Context context) {
		if (preferences == null) {
			preferences = context.getSharedPreferences(SHARED_PREF,
					Context.MODE_PRIVATE);
		}
		return preferences;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//#10939, Changes on Report issue screen
		//db.execSQL("CREATE TABLE " + TABLE + " ( " + ISSUE_DATE + " INTEGER, "
		//		+ TYPE + " VARCHAR, " + TITLE + " VARCHAR, " + DESCRIPTION
		//		+ " VARCHAR)");
		db.execSQL("CREATE TABLE " + TABLE + " ( "  + ISSUE_DATE + " INTEGER, "
				+ TYPE + " VARCHAR, " + TITLE + " VARCHAR, " + DESCRIPTION
				+ " VARCHAR, "+ LOG_DATE + " VARCHAR)");
		//End #10939
		// To start a service for finding updated DB, which deletes old logs
		Intent serviceIntent = new Intent(context, DBUpdater.class);
		context.startService(serviceIntent);

		// Default Values for Shared Preference
		preferences.edit().putInt(UPDATE_DB_IN, UPDATE_DB_IN_VALUE).commit();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// #10939, Changes on Report issue screen
		try {
			if (oldVersion == 1) {
				String alterQuery = "ALTER TABLE " + TABLE + " ADD COLUMN "
						+ LOG_DATE + " VARCHAR";
				db.execSQL(alterQuery);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		// #10939
	}
}