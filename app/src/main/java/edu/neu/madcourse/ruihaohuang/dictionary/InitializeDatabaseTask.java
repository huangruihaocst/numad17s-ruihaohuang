package edu.neu.madcourse.ruihaohuang.dictionary;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.neu.madcourse.ruihaohuang.R;
import edu.neu.madcourse.ruihaohuang.scroggle.ScroggleGameActivity;
import edu.neu.madcourse.ruihaohuang.scroggle.ScroggleHelper;
import edu.neu.madcourse.ruihaohuang.twoplayerscroggle.TwoPlayerScroggleGameActivity;
import edu.neu.madcourse.ruihaohuang.twoplayerscroggle.TwoPlayerScroggleHelper;

/**
 * Created by huangruihao on 2017/2/2.
 */

class InitializeDatabaseTask extends AsyncTask <Void, Integer, Void> {
    private static final int MAX_WORD_LENGTH = DictionaryHelper.MAX_WORD_LENGTH;
    private final String tag = "InitializeDatabaseTask";
    private ProgressDialog dialog;
    private Context context;
    private DictionaryHelper helper;
    private Activity activity;
    private String callerTag;

    InitializeDatabaseTask(Context context, Activity activity, String callerTag) {
        this.context = context;
        this.activity = activity;
        this.callerTag = callerTag;

        dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.dialog_wait_create_database));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        helper = DictionaryHelper.getInstance(context, activity, callerTag);
    }

    @Override
    protected Void doInBackground(Void... params) {
        int count = 0;
        int total = context.getResources().getInteger(R.integer.total_word_count);
        int left = total;
        int once = context.getResources().getInteger(R.integer.insert_once);

        // references: about how to read lines from a file
        // [1] http://stackoverflow.com/questions/4081763/access-resource-files-in-android
        // [2] http://stackoverflow.com/questions/7666589/using-getresources-in-non-activity-class
        // [3] http://stackoverflow.com/questions/26419538/how-to-read-a-line-in-bufferedinputstream
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources()
                .openRawResource(R.raw.dictionary_word_list)));

        // initialize database
        DictionaryDbHelper dbHelper = new DictionaryDbHelper(context.getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // reference: http://stackoverflow.com/questions/28977308/read-all-lines-with-bufferedreader
        try {
            String word;
            // reference: http://stackoverflow.com/questions/29040089/getting-android-sqlite-syntax-error-running-in-api-14
            if (android.os.Build.VERSION.SDK_INT >= 16) {
                while (left > 0) {
                    String insertIntoShort = "INSERT OR IGNORE INTO " + DictionaryReaderContract.ShortWordsEntry.TABLE_NAME
                            + " VALUES ";
                    String insertIntoLong = "INSERT OR IGNORE INTO " + DictionaryReaderContract.LongWordsEntry.TABLE_NAME
                            + " VALUES ";

                    for (int i = 0; i < once; ++i) {
                        if ((word = reader.readLine()) != null) {
                            publishProgress((int) (100 * (count++) / total));
                            if (word.length() <= MAX_WORD_LENGTH) {
                                insertIntoShort += "(" + DictionaryHelper.encodeWord(word) + ")" + DictionaryDbHelper.COMMA_SEP;
                            } else {
                                insertIntoLong += "('" + word + "')" + DictionaryDbHelper.COMMA_SEP;
                            }
                        } else {
                            break;
                        }
                    }

                    insertIntoShort = insertIntoShort.substring(0, insertIntoShort.length() - 1) + ";";
                    insertIntoLong = insertIntoLong.substring(0, insertIntoLong.length() - 1) + ";";
                    db.execSQL(insertIntoShort);
                    db.execSQL(insertIntoLong);
                    left -= once;
                }
            } else {
                while (left > 0) {
                    // reference: http://stackoverflow.com/questions/1609637/is-it-possible-to-insert-multiple-rows-at-a-time-in-an-sqlite-database
                    String insertIntoShort = "INSERT OR IGNORE INTO " + DictionaryReaderContract.ShortWordsEntry.TABLE_NAME
                            + " SELECT ";
                    String insertIntoLong = "INSERT OR IGNORE INTO " + DictionaryReaderContract.LongWordsEntry.TABLE_NAME
                            + " SELECT ";

                    for (int i = 0; i < once; ++i) {
                        if ((word = reader.readLine()) != null) {
                            publishProgress((int) (100 * (count++) / total));
                            if (word.length() <= MAX_WORD_LENGTH) {
                                if (!insertIntoShort.contains("AS")) {  // new sentence
                                    insertIntoShort += DictionaryHelper.encodeWord(word) + " AS "
                                            + DictionaryReaderContract.ShortWordsEntry.COLUMN_WORDS_NAME + " ";
                                } else {
                                    insertIntoShort += "UNION ALL SELECT " + DictionaryHelper.encodeWord(word) + " ";
                                }
                            } else {
                                if (!insertIntoLong.contains("AS")) {  // new sentence
                                    insertIntoLong += "'" + word + "' AS "
                                            + DictionaryReaderContract.LongWordsEntry.COLUMN_WORDS_NAME + " ";
                                } else {
                                    insertIntoLong += "UNION ALL SELECT '" + word + "' ";
                                }
                            }
                        } else {
                            break;
                        }
                    }

                    insertIntoShort = insertIntoShort.substring(0, insertIntoShort.length() - 1) + ";";
                    insertIntoLong = insertIntoLong.substring(0, insertIntoLong.length() - 1) + ";";
                    // does not contain "AS" means nothing to insert
                    if (insertIntoShort.contains("AS")) {
                        db.execSQL(insertIntoShort);
                    }
                    if (insertIntoLong.contains("AS")) {
                        db.execSQL(insertIntoLong);
                    }
                    left -= once;
                }
            }
        } catch (SQLiteException | IOException e) {
            e.printStackTrace();
        }
        db.execSQL(DictionaryDbHelper.SQL_CREATE_INDEX_ON_SHORT);
        db.execSQL(DictionaryDbHelper.SQL_CREATE_INDEX_ON_LONG);
        db.close();
        return null;
    }

    @Override
    protected void onPreExecute() {
        dialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        dialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(Void result) {
        dialog.dismiss();
        Toast.makeText(context, context.getString(R.string.toast_initialization_done),
                Toast.LENGTH_LONG).show();
        String databasePath;
        if(android.os.Build.VERSION.SDK_INT >= 17){
            databasePath = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            databasePath = "/data/data/" + context.getPackageName() + "/databases/";
        }
        helper.setDb(SQLiteDatabase.openDatabase(databasePath
                + DictionaryDbHelper.DATABASE_NAME, null,
                SQLiteDatabase.OPEN_READONLY));
        if (callerTag.equals(ScroggleGameActivity.tag) || callerTag.equals(ScroggleHelper.tag)) {
            ((ScroggleGameActivity) activity).startGame();
        } else if (callerTag.equals(TwoPlayerScroggleGameActivity.tag) || callerTag.equals(TwoPlayerScroggleHelper.tag)) {
            ((TwoPlayerScroggleGameActivity) activity).pair();
        }
    }
}
