package edu.neu.madcourse.ruihaohuang.dictionary;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import edu.neu.madcourse.ruihaohuang.R;

/**
 * Created by huangruihao on 2017/2/1.
 */
class DictionaryHelper {
    private final String tag = "DictionaryHelper";
    private static final int ASCII_OF_A = 97;  // lowercase
    private SQLiteDatabase db = null;
    static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    private static DictionaryHelper ourInstance;

    private static Context context;
    private Activity activity;

    static DictionaryHelper getInstance(Context context, Activity activity) {
        if (ourInstance == null) {
            ourInstance = new DictionaryHelper(context, activity);
        }
        return ourInstance;
    }

    private DictionaryHelper(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    boolean wordExists(String word) {
        String query = "SELECT * FROM ";
        if (word.length() <= context.getResources().getInteger(R.integer.max_word_length)) {
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

    private void initializeDatabase() {
        new InitializeDatabaseTask(context, activity).execute();
    }

    void initializeDb() {
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
        } catch (SQLiteException e) {
            e.printStackTrace();
            initializeDatabase();
        }
    }

    void setDb(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * Open database if exists, otherwise create and open it.
     * Avoid duplicate insertion.
     * Reference: http://stackoverflow.com/questions/17034511/android-database-sqlite-sqlitecantopendatabaseexception-unknown-error-code-14
     */
    void initializeHelper() {
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
            code |= encodeLetter(word.charAt(i)) << (context.getResources()
                    .getInteger(R.integer.word_length) * (length - i - 1));
        }
        return code;
    }

    private static long encodeLetter(char letter) {
        return (long) letter - ASCII_OF_A + 1;  // position in alphabet
    }

    void testDatabase() {
        long start = System.currentTimeMillis();
        for(int i = 0;i < 1000; ++i) {
            wordExists("immew");
        }
        Toast.makeText(context, String.valueOf(System.currentTimeMillis() - start), Toast.LENGTH_LONG).show();

    }
}
