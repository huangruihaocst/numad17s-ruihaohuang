package edu.neu.madcourse.ruihaohuang.dictionary;

import android.provider.BaseColumns;

/**
 * Created by huangruihao on 2017/2/3.
 */

class DictionaryReaderContract {
    private DictionaryReaderContract(){}

    static class ShortWordsEntry implements BaseColumns {
        static final String TABLE_NAME = "shorts";
        static final String COLUMN_WORDS_NAME = "words";
        static final String INDEX_NAME = "short_index";
    }

    static class LongWordsEntry implements BaseColumns {
        static final String TABLE_NAME = "longs";
        static final String COLUMN_WORDS_NAME = "words";
        static final String INDEX_NAME = "long_index";
    }
}
