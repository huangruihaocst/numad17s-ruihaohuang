package edu.neu.madcourse.ruihaohuang.dictionary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by huangruihao on 2017/2/3.
 */

class DictionaryDbHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_SHORT_ENTRIES =
            "CREATE TABLE " + DictionaryReaderContract.ShortWordsEntry.TABLE_NAME + " ("
//            + DictionaryReaderContract.ShortWordsEntry._ID + " INTEGER PRIMARY KEY,"
            + DictionaryReaderContract.ShortWordsEntry.COLUMN_WORDS_NAME + INTEGER_TYPE
            + " )";
    private static final String SQL_CREATE_LONG_ENTRIES =
            "CREATE TABLE " + DictionaryReaderContract.LongWordsEntry.TABLE_NAME + " ("
//                    + DictionaryReaderContract.LongWordsEntry._ID + " INTEGER PRIMARY KEY,"
                    + DictionaryReaderContract.LongWordsEntry.COLUMN_WORDS_NAME + TEXT_TYPE
                    + " )";
    private static final String SQL_DELETE_SHORT_ENTRIES =
            "DROP TABLE IF EXISTS " + DictionaryReaderContract.ShortWordsEntry.TABLE_NAME;
    private static final String SQL_DELETE_LONG_ENTRIES =
            "DROP TABLE IF EXISTS " + DictionaryReaderContract.LongWordsEntry.TABLE_NAME;
    static final String SQL_CREATE_INDEX_ON_SHORT =
            "CREATE INDEX " + DictionaryReaderContract.ShortWordsEntry.INDEX_NAME + " ON "
            + DictionaryReaderContract.ShortWordsEntry.TABLE_NAME + "("
            + DictionaryReaderContract.ShortWordsEntry.COLUMN_WORDS_NAME + ")";
    static final String SQL_CREATE_INDEX_ON_LONG =
            "CREATE INDEX " + DictionaryReaderContract.LongWordsEntry.INDEX_NAME + " ON "
                    + DictionaryReaderContract.LongWordsEntry.TABLE_NAME + "("
                    + DictionaryReaderContract.LongWordsEntry.COLUMN_WORDS_NAME + ")";

    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "DictionaryReader.db";

    DictionaryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_SHORT_ENTRIES);
        db.execSQL(SQL_CREATE_LONG_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_SHORT_ENTRIES);
        db.execSQL(SQL_DELETE_LONG_ENTRIES);
        onCreate(db);
    }
}
