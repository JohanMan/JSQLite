package com.johan.jsqlite;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class JSQLiteDao <T extends JSQLiteModel> {
	
	private static final String _EQ = "=";
	private static final String _LT = "<";
	private static final String _GT = ">";
	private static final String _LTEQ = "<=";
	private static final String _GTEQ = ">=";
	private static final String _ASC = "acs";
	private static final String _DESC = "desc";
	
	private static final String _WHERE = "where";
	private static final String _ORDERBY = "order by";
	private static final String _LIMIT = "limit";
	
	
	public static final String KEY_ID = "id";
	
	protected JSQLiteDatabase database;
	
	private StringBuilder conditionKeyBuilder;
	private List<String> conditionValueList;
    
    public JSQLiteDao() {
    	conditionKeyBuilder = new StringBuilder();
		conditionValueList = new ArrayList<String>();
    	database = JSQLiteDatabase.getInstance();
    }
    
    public boolean insert(T insertData) {
        ContentValues contentValues = getContentValues(insertData);
        long insertRow = database.getWritableDatabase().insert(getTableName(), null, contentValues);
        return insertRow != -1;
    }
    
    @SuppressWarnings("deprecation")
	public boolean insert(List<T> insertDataList) {
    	SQLiteDatabase sqLiteDatabase = database.getReadableDatabase();
    	try {
    		boolean insertResult = false;
    		sqLiteDatabase.beginTransaction();
            sqLiteDatabase.setLockingEnabled(false);
            for (T insertData: insertDataList) {
            	insertResult = insert(insertData);
            	if (!insertResult) {
					break;
				}
            }
            if (insertResult) {
				sqLiteDatabase.setTransactionSuccessful();
				return true;
            } else {
				return false;
			}
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.setLockingEnabled(true);
        }
    }
    
    public boolean update(ContentValues contentValues) {
    	int effectRow = database.getWritableDatabase().update(getTableName(), contentValues, getConditionSQLWithoutWhere(), getConditionArgs());
        reset();
        return effectRow != 0;
    }
    
    public boolean update(String updateKey, String updateValue) {
    	ContentValues contentValues = new ContentValues();
    	contentValues.put(updateKey, updateValue);
    	return update(contentValues);
    }
    
    public boolean update(T updateData) {
    	reset();
    	eq(KEY_ID, updateData.getId());
        ContentValues contentValues = getContentValues(updateData);
        return update(contentValues);
    }
    
    @SuppressWarnings("deprecation")
    public boolean update(List<T> updateDataList) {
    	SQLiteDatabase sqLiteDatabase = database.getReadableDatabase();
    	try {
    		boolean updateResult = false;
    		sqLiteDatabase.beginTransaction();
            sqLiteDatabase.setLockingEnabled(false);
            for (T updateData: updateDataList) {
            	updateResult = update(updateData);
            	if (!updateResult) {
					break;
				}
            }
            if (updateResult) {
				sqLiteDatabase.setTransactionSuccessful();
				return true;
            } else {
				return false;
			}
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.setLockingEnabled(true);
        }
    }
    
    public void delete() {
    	database.getWritableDatabase().delete(getTableName(), getConditionSQLWithoutWhere(), getConditionArgs());
    	reset();
    }
    
    public void delete(T deleteData) {
    	reset();
    	eq(KEY_ID, deleteData.getId());
    	delete();
    }
    
    @SuppressWarnings("deprecation")
    public void delete(List<T> deleteDataList) {
    	SQLiteDatabase sqLiteDatabase = database.getReadableDatabase();
    	try {
    		sqLiteDatabase.beginTransaction();
            sqLiteDatabase.setLockingEnabled(false);
            for (T deleteData: deleteDataList) {
            	delete(deleteData);
            }
			sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.setLockingEnabled(true);
        }
    }
    
    public T find() {
    	T data = null;
    	Cursor cursor = database.getReadableDatabase().rawQuery("select * from " + getTableName() + getConditionSQL(), getConditionArgs());
        while (cursor.moveToNext()) {
            data = cursorToData(cursor);
            break;
        }
        cursor.close();
        reset();
        return data;
    }
    
    public T findById(int id) {
    	reset();
    	eq(KEY_ID, id);
        return find();
    }
    
    public List<T> list() {
    	List<T> dataList = new ArrayList<T>();
        Cursor cursor = database.getReadableDatabase().rawQuery("select * from " + getTableName() + getConditionSQL(), getConditionArgs());
        while (cursor.moveToNext()) {
            T data = cursorToData(cursor);
            dataList.add(data);
        }
        cursor.close();
        reset();
        return dataList;
    }

	public abstract String getTableName();
	
	public abstract T cursorToData(Cursor cursor);
	
	public abstract ContentValues getContentValues(T data);
	
	/**
	 * Condition Begin ===============================================
	 */
	
	public JSQLiteDao<T> eq(String conditionKey, Object conditionValue) {
		condition(_EQ, conditionKey, conditionValue);
		return this;
	}
	
	public JSQLiteDao<T> lt(String conditionKey, Object conditionValue) {
		condition(_LT, conditionKey, conditionValue);
		return this;
	}
	
	public JSQLiteDao<T> gt(String conditionKey, Object conditionValue) {
		condition(_GT, conditionKey, conditionValue);
		return this;
	}
	
	public JSQLiteDao<T> lteq(String conditionKey, Object conditionValue) {
		condition(_LTEQ, conditionKey, conditionValue);
		return this;
	}
	
	public JSQLiteDao<T> gteq(String conditionKey, Object conditionValue) {
		condition(_GTEQ, conditionKey, conditionValue);
		return this;
	}
	
	public JSQLiteDao<T> orEq(String conditionKey, Object conditionValue) {
		orCondition(_EQ, conditionKey, conditionValue);
		return this;
	}
	
	public JSQLiteDao<T> orLt(String conditionKey, Object conditionValue) {
		orCondition(_LT, conditionKey, conditionValue);
		return this;
	}
	
	public JSQLiteDao<T> orGt(String conditionKey, Object conditionValue) {
		orCondition(_GT, conditionKey, conditionValue);
		return this;
	}
	
	public JSQLiteDao<T> orLtEq(String conditionKey, Object conditionValue) {
		orCondition(_LTEQ, conditionKey, conditionValue);
		return this;
	}
	
	public JSQLiteDao<T> orGtEq(String conditionKey, Object conditionValue) {
		orCondition(_GTEQ, conditionKey, conditionValue);
		return this;
	}
	
	public JSQLiteDao<T> andEq(String conditionKey, Object conditionValue) {
		andCondition(_EQ, conditionKey, conditionValue);
		return this;
	}
	
	public JSQLiteDao<T> andLt(String conditionKey, Object conditionValue) {
		andCondition(_LT, conditionKey, conditionValue);
		return this;
	}
	
	public JSQLiteDao<T> andGt(String conditionKey, Object conditionValue) {
		andCondition(_GT, conditionKey, conditionValue);
		return this;
	}
	
	public JSQLiteDao<T> andLtEq(String conditionKey, Object conditionValue) {
		andCondition(_LTEQ, conditionKey, conditionValue);
		return this;
	}
	
	public JSQLiteDao<T> andGtEq(String conditionKey, Object conditionValue) {
		andCondition(_GTEQ, conditionKey, conditionValue);
		return this;
	}
	
	public JSQLiteDao<T> orderByAsc(String... fields) {
		orderBy(_ASC, fields);
		return this;
	}
	
	public JSQLiteDao<T> orderByDesc(String... fields) {
		orderBy(_DESC, fields);
		return this;
	}
	
	public JSQLiteDao<T> andOrderByAsc(String... fields) {
		andOrderBy(_ASC, fields);
		return this;
	}
	
	public JSQLiteDao<T> andOrderByDesc(String... fields) {
		andOrderBy(_DESC, fields);
		return this;
	}
	
	public JSQLiteDao<T> limit(int start, int row) {
		conditionKeyBuilder.append(" limit ").append(start).append(",").append(row);
		return this;
	}
	
	private void condition(String symbol, String conditionKey, Object conditionValue) {
		boolean isContainsLimit = isContainsKeyword(_LIMIT);
		boolean isCOntainsOrderBy = isContainsKeyword(_ORDERBY);
		if (isContainsLimit || isCOntainsOrderBy) {
			throw new IllegalArgumentException("keyword 'limit' and 'order by' must be appean after 'where'");
		}
		boolean isContainsWhere = isContainsKeyword(_WHERE);
		if (isContainsWhere) {
			throw new IllegalArgumentException("keyword 'where' can't appean twice");
		}
		conditionKeyBuilder.append(" where ").append(conditionKey).append(symbol).append("?");
		conditionValueList.add(getString(conditionValue));
	}
	
	private void andCondition(String symbol, String conditionKey, Object conditionValue) {
		boolean isLast = isLastKeyword(_WHERE);
		if (!isLast) {
			throw new IllegalArgumentException("you should execute XXX method before execute andXXX method");
		}
		conditionKeyBuilder.append(" and").append(conditionKey).append(symbol).append("?");
		conditionValueList.add(getString(conditionValue));
	}
	
	private void orCondition(String symbol, String conditionKey, Object conditionValue) {
		boolean isLast = isLastKeyword(_WHERE);
		if (!isLast) {
			throw new IllegalArgumentException("you should execute XXX method before execute orXXX method");
		}
		conditionKeyBuilder.append(" or").append(conditionKey).append(symbol).append("?");
		conditionValueList.add(getString(conditionValue));
	}
	
	private void orderBy(String symbol, String... fields) {
		boolean isContainsLimit = isContainsKeyword(_LIMIT);
		if (isContainsLimit) {
			throw new IllegalArgumentException("limit method must be executed after execute orderBy method");
		}
		for (int i = 0; i < fields.length; i++) {
			if (i == 0) {
				conditionKeyBuilder.append(" order by ").append(fields[i]).append(" ").append(symbol);
			} else {
				conditionKeyBuilder.append(", ").append(fields[i]).append(" ").append(symbol);
			}
		}
	}
	
	private void andOrderBy(String symbol, String... fields) {
		boolean isLast = isLastKeyword(_ORDERBY);
		if (!isLast) {
			throw new IllegalArgumentException("you should execute orderByXXX method before execute andOrderByXXX method");
		}
		for (int i = 0; i < fields.length; i++) {
			conditionKeyBuilder.append(", ").append(fields[i]).append(" ").append(symbol);
		}
	}
	
	private void reset() {
		int length = conditionKeyBuilder.length();
		if (length > 0) {
			conditionKeyBuilder.delete(0, length);
		}
		conditionValueList.clear();
	}
	
	private String getString(Object object) {
		try {
			return String.valueOf(object);
		} catch (Exception e) {
			return "";
		}
	}
	
	public String getConditionSQL() {
		return conditionKeyBuilder.toString();
	}
	
	public String getConditionSQLWithoutWhere() {
		if (isContainsKeyword(_WHERE)) {
			return conditionKeyBuilder.toString().substring(7);
		}
		return conditionKeyBuilder.toString();
	}
	
	public String[] getConditionArgs() {
		return conditionValueList.toArray(new String[conditionValueList.size()]);
	}
	
	private boolean isContainsKeyword(String keyword) {
		return conditionKeyBuilder.toString().contains(keyword);
	}
	
	private boolean isLastKeyword(String keyword) {
		String conditionKey = conditionKeyBuilder.toString();
		int whereIndex = conditionKey.lastIndexOf(_WHERE);
		int orderByIndex = conditionKey.lastIndexOf(_ORDERBY);
		int limitIndex = conditionKey.lastIndexOf(_LIMIT);
		if (_WHERE.equals(keyword)) {
			return whereIndex > orderByIndex && whereIndex > limitIndex;
		}
		if (_ORDERBY.equals(keyword)) {
			return orderByIndex > whereIndex && orderByIndex > limitIndex;
		}
		if (_LIMIT.equals(keyword)) {
			return limitIndex > whereIndex && limitIndex > orderByIndex;
		}
		return false;
	}
	
	/**
	 * Condition End ===============================================
	 */
	
}
