package edu.neu.madcourse.ruihaohuang.dictionary;

import android.content.Context;

import edu.neu.madcourse.ruihaohuang.R;

/**
 * Created by huangruihao on 2017/2/1.
 */
public class DictionaryHelper {
    private final String tag = "DictionaryHelper";

    private static DictionaryHelper ourInstance;

    private Context context;

    public static DictionaryHelper getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new DictionaryHelper(context);
        }
        return ourInstance;
    }

    private DictionaryHelper(Context context) {
        this.context = context;
    }

    boolean wordExists(String word) {
        return false;
    }

    // TODO: change it into private
    void initializeDatabase() {
        new InitializeDatabaseTask(context).execute();
    }
}
