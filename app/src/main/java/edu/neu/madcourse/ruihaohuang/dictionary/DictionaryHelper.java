package edu.neu.madcourse.ruihaohuang.dictionary;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.widget.TextView;
import android.widget.Toast;

import edu.neu.madcourse.ruihaohuang.R;

/**
 * Created by huangruihao on 2017/2/1.
 */
class DictionaryHelper {
    private final String tag = "DictionaryHelper";
    private SQLiteDatabase db = null;
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private static DictionaryHelper ourInstance;

    private Context context;
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
}
