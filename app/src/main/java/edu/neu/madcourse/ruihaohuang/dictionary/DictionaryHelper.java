package edu.neu.madcourse.ruihaohuang.dictionary;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import edu.neu.madcourse.ruihaohuang.R;
import edu.neu.madcourse.ruihaohuang.scroggle.ScroggleGameActivity;
import edu.neu.madcourse.ruihaohuang.scroggle.ScroggleHelper;
import edu.neu.madcourse.ruihaohuang.twoplayerscroggle.TwoPlayerScroggleGameActivity;
import edu.neu.madcourse.ruihaohuang.twoplayerscroggle.TwoPlayerScroggleHelper;

/**
 * Created by huangruihao on 2017/2/1.
 */
public class DictionaryHelper {
    private static final String tag = "DictionaryHelper";
    private static final int ASCII_OF_A = 97;  // lowercase
    // this word length is different from the meaning of word length in MAX_WORD_LENGTH
    // same meaning with 64 bits CPU
    // it means a letter is contained of 5 bits
    private static final int WORD_LENGTH = 5;
    // this word length means a word that needs to be encoded can be at most 12 letters long
    static final int MAX_WORD_LENGTH = 12;
    private final String lastRecord = "28433685139";  // encoded last word in the word list
    private SQLiteDatabase db = null;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    private String callerTag;

    private static DictionaryHelper ourInstance;

    private static Context context;
    private Activity activity;

    public static DictionaryHelper getInstance(Context context, Activity activity, String callerTag) {
        if (ourInstance == null) {
            ourInstance = new DictionaryHelper(context, activity, callerTag);
        }
        if (context != DictionaryHelper.context) {
            DictionaryHelper.context = context;
        }
        if (activity != ourInstance.activity) {
            ourInstance.activity = activity;
        }
        return ourInstance;
    }

    private DictionaryHelper(Context context, Activity activity, String callerTag) {
        this.context = context;
        this.activity = activity;
        this.callerTag = callerTag;
    }

    public boolean wordExists(String word) {
        String query = "SELECT * FROM ";
        if (word.length() <= MAX_WORD_LENGTH) {
            query += DictionaryReaderContract.ShortWordsEntry.TABLE_NAME + " WHERE "
                    + DictionaryReaderContract.ShortWordsEntry.COLUMN_WORDS_NAME + " = "
                    + encodeWord(word);
        } else {
            query += DictionaryReaderContract.LongWordsEntry.TABLE_NAME + " WHERE "
                    + DictionaryReaderContract.LongWordsEntry.COLUMN_WORDS_NAME + " = "
                    + "'" + word + "'";
        }
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public void initializeDb() {
        try {
            // reference: http://stackoverflow.com/questions/9109438/how-to-use-an-existing-database-with-an-android-application
            String databasePath;
            if(android.os.Build.VERSION.SDK_INT >= 17){
                databasePath = context.getApplicationInfo().dataDir + "/databases/";
            } else {
                databasePath = "/data/data/" + context.getPackageName() + "/databases/";
            }
            db = SQLiteDatabase.openDatabase(databasePath + DictionaryDbHelper.DATABASE_NAME, null,
                    SQLiteDatabase.OPEN_READONLY);
            if (!checkDatabaseIntegrity()) {
                throw new SQLiteException();
            }
            if ((callerTag.equals(ScroggleGameActivity.tag) || callerTag.equals(ScroggleHelper.tag))
                    && Build.VERSION.SDK_INT < 23) {
                ((ScroggleGameActivity) activity).startGame();
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
            new InitializeDatabaseTask(context, activity, callerTag).execute();
        }
    }

    public boolean dbExists() {
        try {
            // reference: http://stackoverflow.com/questions/9109438/how-to-use-an-existing-database-with-an-android-application
            String databasePath;
            if(android.os.Build.VERSION.SDK_INT >= 17){
                databasePath = context.getApplicationInfo().dataDir + "/databases/";
            } else {
                databasePath = "/data/data/" + context.getPackageName() + "/databases/";
            }
            db = SQLiteDatabase.openDatabase(databasePath + DictionaryDbHelper.DATABASE_NAME, null,
                    SQLiteDatabase.OPEN_READONLY);
            if (!checkDatabaseIntegrity()) {
                throw new SQLiteException();
            }
            return true;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    void setDb(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * Initialize Dictionary Helper.
     * Open database if exists, otherwise create and open it.
     * Avoid duplicate insertion.
     * Reference: http://stackoverflow.com/questions/17034511/android-database-sqlite-sqlitecantopendatabaseexception-unknown-error-code-14
     */
    public void initializeHelper() {
        int permissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(context,
                        context.getString(R.string.toast_request_permission_write_external_storage),
                        Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        } else {  // database access
            initializeDb();
        }
    }

    static long encodeWord(String word) {
        int length = word.length();
        long code = 0;
        for (int i = length - 1; i >= 0; --i) {
            code |= encodeLetter(word.charAt(i)) << WORD_LENGTH * (length - i - 1);
        }
        return code;
    }

    private static long encodeLetter(char letter) {
        return (long) letter - ASCII_OF_A + 1;  // position in alphabet
    }

    // test speed of the database
    void testDb() {
        long start = System.currentTimeMillis();
        for(int i = 0;i < 1000; ++i) {
            wordExists("immew");
        }
        Toast.makeText(context, String.valueOf(System.currentTimeMillis() - start), Toast.LENGTH_LONG).show();
    }

    // if the exist database has the last word, it must be complete, otherwise incomplete
    private boolean checkDatabaseIntegrity() {
        String query = "SELECT * FROM ";
        query += DictionaryReaderContract.ShortWordsEntry.TABLE_NAME + " WHERE "
                + DictionaryReaderContract.ShortWordsEntry.COLUMN_WORDS_NAME + " = "
                + lastRecord;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }
}
