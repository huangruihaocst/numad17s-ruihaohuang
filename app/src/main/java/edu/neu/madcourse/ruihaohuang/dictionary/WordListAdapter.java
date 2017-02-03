package edu.neu.madcourse.ruihaohuang.dictionary;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import edu.neu.madcourse.ruihaohuang.R;

/**
 * Created by huangruihao on 2017/2/3.
 */

public class WordListAdapter extends BaseAdapter {
    private final String tag = "WordListAdapter";
    private Context context;
    private ArrayList<String> wordList;

    WordListAdapter(Context context, ArrayList<String> wordList) {
        this.context = context;
        this.wordList = wordList;
    }

    @Override
    public int getCount() {
        return wordList.size();
    }

    @Override
    public String getItem(int position) {
        return wordList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO: use convertView to optimize
        TextView wordText = new TextView(context);
        wordText.setText(wordList.get(position));
        wordText.setTextSize(context.getResources().getDimension(R.dimen.dictionary_word_list_text_size));
        wordText.setTextColor(Color.BLACK);
        return wordText;
    }
}
