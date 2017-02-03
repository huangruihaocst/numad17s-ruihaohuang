package edu.neu.madcourse.ruihaohuang.dictionary;

import android.app.ProgressDialog;
import android.content.Context;
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

public class InitializeDatabaseTask extends AsyncTask <Void, Integer, Void> {
    private final String tag = "InitializeDatabaseTask";
    private final int ASCII_OF_A = 97;  // lowercase
    private ProgressDialog dialog;
    private Context context;

    InitializeDatabaseTask(Context context) {
        this.context = context;

        dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.dialog_wait_create_database));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    }

    @Override
    protected Void doInBackground(Void... params) {
        // references: about how to read lines from a file
        // [1] http://stackoverflow.com/questions/4081763/access-resource-files-in-android
        // [2] http://stackoverflow.com/questions/7666589/using-getresources-in-non-activity-class
        // [3] http://stackoverflow.com/questions/26419538/how-to-read-a-line-in-bufferedinputstream
        int count = 0;
        int total = context.getResources().getInteger(R.integer.total_word_count);
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources()
                .openRawResource(R.raw.dictionary_word_list)));
        // reference: http://stackoverflow.com/questions/28977308/read-all-lines-with-bufferedreader
        try {
            String word;
            while ((word = reader.readLine()) != null) {
                publishProgress((int) (100 * (count++) / total));  // percent
                Log.i(tag, word);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    }

    long encodeWord(String word) {
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
