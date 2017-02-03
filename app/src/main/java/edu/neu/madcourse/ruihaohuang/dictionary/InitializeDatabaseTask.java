package edu.neu.madcourse.ruihaohuang.dictionary;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.neu.madcourse.ruihaohuang.R;

/**
 * Created by huangruihao on 2017/2/2.
 */

class InitializeDatabaseTask extends AsyncTask <Void, Integer, Void> {
    private final String tag = "InitializeDatabaseTask";
    private final int ASCII_OF_A = 97;  // lowercase
    private ProgressDialog dialog;
    private Context context;
    private Activity activity;

    InitializeDatabaseTask(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;

        dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.dialog_wait_create_database));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
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
            while (left > 0) {
                String insertToShort = "INSERT INTO " + DictionaryReaderContract.ShortWordsEntry.TABLE_NAME
                        + " VALUES ";
                String insertToLong = "INSERT INTO " + DictionaryReaderContract.LongWordsEntry.TABLE_NAME
                        + " VALUES ";
                for (int i = 0; i < once; ++i) {
                    if ((word = reader.readLine()) != null) {
                        publishProgress((int) (100 * (count++) / total));
                        if (word.length() <= context.getResources().getInteger(R.integer.max_word_length)) {
                            insertToShort += "(" + encodeWord(word) + ")" + DictionaryDbHelper.COMMA_SEP;
                        } else {
                            insertToLong += "('" + word + "')" + DictionaryDbHelper.COMMA_SEP;
                        }
                    } else {
                        break;
                    }
                }
                insertToShort = insertToShort.substring(0, insertToShort.length() - 1) + ";";
                insertToLong = insertToLong.substring(0, insertToLong.length() - 1) + ";";
                db.execSQL(insertToShort);
                db.execSQL(insertToLong);
                left -= once;
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
        Toast.makeText(context, "Done.", Toast.LENGTH_LONG).show();
        String databasePath;
        if(android.os.Build.VERSION.SDK_INT >= 17){
            databasePath = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            databasePath = "/data/data/" + context.getPackageName() + "/databases/";
        }
        ((DictionaryActivity) activity).helper.setDb(SQLiteDatabase.openDatabase(databasePath
                + DictionaryDbHelper.DATABASE_NAME, null,
                SQLiteDatabase.OPEN_READONLY));
    }

    private long encodeWord(String word) {
        int length = word.length();
        long code = 0;
        for (int i = length - 1; i >= 0; --i) {
            code |= encodeLetter(word.charAt(i)) << (context.getResources()
                    .getInteger(R.integer.word_length) * (length - i - 1));
        }
        return code;
    }

    private long encodeLetter(char letter) {
        return (long) letter - ASCII_OF_A + 1;  // position in alphabet
    }
}
