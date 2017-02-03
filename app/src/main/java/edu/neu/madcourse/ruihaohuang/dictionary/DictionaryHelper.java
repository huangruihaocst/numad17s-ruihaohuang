package edu.neu.madcourse.ruihaohuang.dictionary;

/**
 * Created by huangruihao on 2017/2/1.
 */
public class DictionaryHelper {
    private final int ASCII_OF_A = 97;  // lowercase
    // this word length is different from the meaning of word length in MAX_WORD_LENGTH
    // it means a letter is contained of 5 bits
    // same meaning with 64 bits CPU
    private final int WORD_LENGTH = 5;
    // this word length means a word can be at most 12 letters long
    private final int MAX_WORD_LENGTH = 12;  // 64 / 5 = 12

    private static DictionaryHelper ourInstance = new DictionaryHelper();

    static DictionaryHelper getInstance() {
        return ourInstance;
    }

    private DictionaryHelper() {
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
