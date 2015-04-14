package com.mz.sticker.util;

import android.database.Cursor;

public abstract class CursorUtil {

    private static final int COLUMN_NOT_EXISTS = -1;

    /* Cursor getters methods
     * There is no way to check in Java if column is null or empty because below methods simply return 0 in such cases.
     * The only way to search for such values in columns is in SQL queries.
     * Exceptions are Blob and String values for which null is returned when column is null, but not when column is empty.
     * */

    public static Float getFloatFromCursor(Cursor cursor, String columnName) {
        int columnIndex;
        if((columnIndex = cursor.getColumnIndex(columnName)) != COLUMN_NOT_EXISTS) {
            return cursor.getFloat(columnIndex);
        }
        else {
            return null;
        }
    }

    public static Double getDoubleFromCursor(Cursor cursor, String columnName) {
        int columnIndex;
        if((columnIndex = cursor.getColumnIndex(columnName)) != COLUMN_NOT_EXISTS) {
            return cursor.getDouble(columnIndex);
        }
        else {
            return null;
        }
    }

    public static Integer getIntFromCursor(Cursor cursor, String columnName) {
        int columnIndex;
        if((columnIndex = cursor.getColumnIndex(columnName)) != COLUMN_NOT_EXISTS) {
            return cursor.getInt(columnIndex);
        }
        else {
            return null;
        }
    }

    public static Long getLongFromCursor(Cursor cursor, String columnName) {
        int columnIndex;
        if((columnIndex = cursor.getColumnIndex(columnName)) != COLUMN_NOT_EXISTS) {
            return cursor.getLong(columnIndex);
        }
        else {
            return null;
        }
    }

    public static String getStringFromCursor(Cursor cursor, String columnName) {
        int columnIndex;
        if((columnIndex = cursor.getColumnIndex(columnName)) != COLUMN_NOT_EXISTS) {
            return cursor.getString(columnIndex);
        }
        else {
            return null;
        }
    }

    public static Boolean getBooleanFromCursor(Cursor cursor, String columnName) {
        int columnIndex;
        if((columnIndex = cursor.getColumnIndex(columnName)) != COLUMN_NOT_EXISTS) {
            return (cursor.getInt(columnIndex) == 1) ? true : false;
        }
        else {
            return null;
        }
    }

    public static byte[] getByteArrayFromCursor(Cursor cursor, String columnName) {
        int columnIndex;
        if((columnIndex = cursor.getColumnIndex(columnName)) != COLUMN_NOT_EXISTS) {
            return cursor.getBlob(columnIndex);
        }
        else {
            return null;
        }
    }

}
