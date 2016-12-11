package com.johan.jsqlite;

import java.sql.Date;

public class Utils {

	public static String UpAndLowToAllLow(String src) {
		int srcCount = src.length();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < srcCount; i++) {
			char c = src.charAt(i);
			if (i == 0) {
				builder.append(Character.toLowerCase(c));
				continue;
			}
			if (Character.isUpperCase(c)) {
				builder.append("_").append(Character.toLowerCase(c));
			} else {
				builder.append(c);
			}
		}
		return builder.toString();
	}
	
	public static String UpAndLowToAllUp(String src) {
		int srcCount = src.length();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < srcCount; i++) {
			char c = src.charAt(i);
			if (i == 0) {
				builder.append(Character.toUpperCase(c));
				continue;
			}
			if (Character.isUpperCase(c)) {
				builder.append("_").append(Character.toUpperCase(c));
			} else {
				builder.append(Character.toUpperCase(c));
			}
		}
		return builder.toString();
	}
	
	public static String getColumnTypeFromFieldType(String fieldType) {
		if (fieldType.equals(Integer.class.getName()) || fieldType.equals(int.class.getName())) {
			return "INTEGER";
		} else if (fieldType.equals(String.class.getName())) {
			return "TEXT";
		} else if (fieldType.equals(Float.class) || fieldType.equals(float.class.getName())) {
			return "REAL";
		} else if (fieldType.equals(Double.class) || fieldType.equals(double.class.getName())) {
			return "REAL";
		} else if (fieldType.equals(Long.class.getName()) || fieldType.equals(long.class.getName())) {
			return "INTEGER";
		} else if (fieldType.equals(Boolean.class.getName()) || fieldType.equals(boolean.class.getName())) {
			return "INTEGER";
		} else if (fieldType.equals(Date.class.getName())) {
			return "DATE";
		} else {
			return "BLOB";
		}
	}
	
	public static String getCursorDataMethod(String columnName, String fieldType) {
		if (fieldType.equals(Integer.class.getName()) || fieldType.equals(int.class.getName())) {
			return "cursor.getInt(cursor.getColumnIndex(\"" + columnName + "\"))";
		} else if (fieldType.equals(String.class.getName())) {
			return "cursor.getString(cursor.getColumnIndex(\"" + columnName + "\"))";
		} else if (fieldType.equals(Float.class) || fieldType.equals(float.class.getName())) {
			return "cursor.getFloat(cursor.getColumnIndex(\"" + columnName + "\"))";
		} else if (fieldType.equals(Double.class) || fieldType.equals(double.class.getName())) {
			return "cursor.getDouble(cursor.getColumnIndex(\"" + columnName + "\"))";
		} else if (fieldType.equals(Long.class.getName()) || fieldType.equals(long.class.getName())) {
			return "cursor.getLong(cursor.getColumnIndex(\"" + columnName + "\"))";
		} else if (fieldType.equals(Boolean.class.getName()) || fieldType.equals(boolean.class.getName())) {
			return "cursor.getInt(cursor.getColumnIndex(\"" + columnName + "\")) != 0 ? true : false";
		} 
		else if (fieldType.equals(Date.class.getName())) {
			return "new java.sql.Date(cursor.getLong(cursor.getColumnIndex(\"" + columnName + "\")))";
		} else {
			return "cursor.getBlob(cursor.getColumnIndex(\"" + columnName + "\"))";
		}
	}
	
}
