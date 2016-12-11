package com.johan.jsqlite;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "jsqlite";
    private static final int DATABASE_VERSION = 1; 

    public DatabaseHelper(Context context) {
        this(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	List<String> createTableSQLList = getCreateTableSQLList();
    	for (String createTableSQL : createTableSQLList) {
    		db.execSQL(createTableSQL);
    	}
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private List<String> getCreateTableSQLList() {
    	try {
			Class tableConfig = Class.forName("com.johan.jsqlite.config.TableConfig");
			Object obejct = tableConfig.newInstance();
			Method method = tableConfig.getMethod("getTableCreateSQLList");
			List<String> createTableSQLList = (List<String>) method.invoke(obejct);
			return createTableSQLList;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
    	return null;
    }

}
