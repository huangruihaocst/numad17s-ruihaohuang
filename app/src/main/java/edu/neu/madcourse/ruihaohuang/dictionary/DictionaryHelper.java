package edu.neu.madcourse.ruihaohuang.dictionary;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.neu.madcourse.ruihaohuang.R;

/**
 * Created by huangruihao on 2017/2/1.
 */
public class DictionaryHelper {
    private final String tag = "DictionaryHelper";
    private final int ASCII_OF_A = 97;  // lowercase
    // this word length is different from the meaning of word length in MAX_WORD_LENGTH
    // it means a letter is contained of 5 bits
    // same meaning with 64 bits CPU
    private final int WORD_LENGTH = 5;
    // this word length means a word can be at most 12 letters long
    private final int MAX_WORD_LENGTH = 12;  // 64 / 5 = 12

    private Context context;

    private static DictionaryHelper ourInstance = new DictionaryHelper();

    static DictionaryHelper getInstance() {
        return ourInstance;
    }

    private DictionaryHelper() {
    }

    void setContext(Context context) {
        this.context = context;
    }

    boolean wordExists(String word) {
        return false;
    }

    // TODO: change it into private
    void createDatabase() {
        // references: about how to read lines from a file
        // [1] http://stackoverflow.com/questions/4081763/access-resource-files-in-android
        // [2] http://stackoverflow.com/questions/7666589/using-getresources-in-non-activity-class
        // [3] http://stackoverflow.com/questions/26419538/how-to-read-a-line-in-bufferedinputstream
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources()
                .openRawResource(R.raw.dictionary_word_list)));
        // reference: http://stackoverflow.com/questions/28977308/read-all-lines-with-bufferedreader
        try {
            String word;
            while ((word = reader.readLine()) != null) {
                Log.i(tag, word);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    long encodeWord(String word) {
        int length = word.length();
        long code = 0;
        for (int i = length - 1; i >= 0; --i) {
            code |= encodeLetter(word.charAt(i)) << (WORD_LENGTH * (length - i - 1));
        }
        return code;
    }

    private long encodeLetter(char letter) {
        return (long) letter - ASCII_OF_A + 1;  // position in alphabet
    }
}
