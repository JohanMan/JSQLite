package com.johan.jsqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;


public class JSQLiteDatabase {

	private static JSQLiteDatabase INSTANCE = new JSQLiteDatabase();
	
	private DatabaseHelper databaseHelper;
	
	private JSQLiteDatabase() {
		
	}
	
	public static JSQLiteDatabase getInstance() {
		return INSTANCE;
	}
	
	public void init(Context context) {
		databaseHelper = new DatabaseHelper(context);
		getReadableDatabase();
	}
	
	public SQLiteDatabase getWritableDatabase() {
		if (databaseHelper == null) {
			throw new RuntimeException("please init JSQLiteDatabase on your Application before use JSQLiteDatabase");
		}
		return databaseHelper.getWritableDatabase();
	}
	
	public SQLiteDatabase getReadableDatabase() {
		if (databaseHelper == null) {
			throw new RuntimeException("please init JSQLiteDatabase on your Application before use JSQLiteDatabase");
		}
		return databaseHelper.getReadableDatabase();
	}
	
}
