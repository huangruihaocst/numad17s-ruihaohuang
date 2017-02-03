package edu.neu.madcourse.ruihaohuang.dictionary;

import android.provider.BaseColumns;

/**
 * Created by huangruihao on 2017/2/3.
 */

public class DictionaryReaderContract {
    private DictionaryReaderContract(){}

    public static class ShortWordsEntry implements BaseColumns {
        public static final String TABLE_NAME = "short";
        public static final String COLUMN_WORDS_NAME = "words";
    }

    public static class LongWordsEntry implements BaseColumns {
        public static final String TABLE_NAME = "longs";
        public static final String COLUMN_WORDS_NAME = "words";
    }
}
