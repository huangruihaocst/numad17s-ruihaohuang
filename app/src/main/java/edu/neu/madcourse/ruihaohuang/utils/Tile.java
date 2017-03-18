package edu.neu.madcourse.ruihaohuang.utils;

import android.view.View;
import android.widget.Button;

/**
 * Created by huangruihao on 2017/2/15.
 */

public class Tile {
    public static final int BOARD_SIZE = 3;  // use constant value to avoid magic number

    private Tile subTiles[];
    private String content;  // a letter if it is the 1 * 1 tile, a word if it is a BOARD_SIZE * BOARD_SIZE tile
    private String word;  // only for large tile
    private View view;

    // small tile: 1 * 1
    // large tile: BOARD_SIZE * BOARD_SIZE
    // board: (BOARD_SIZE * BOARD_SIZE) * (BOARD_SIZE * BOARD_SIZE)
    private final static int SMALL_TILE_UNSELECTED = 0;
    private final static int SMALL_TILE_SELECTED = 1;
    private final static int SMALL_TILE_REMAINING = 2;

    private final static int LETTER_LENGTH = 1;
    private final static int WORD_LENGTH = BOARD_SIZE * BOARD_SIZE;

    public Tile() {
        content = "";
    }

    public void setSubTiles(Tile[] subTiles) {
        this.subTiles = subTiles;
    }

    public Tile[] getSubTiles() {
        return subTiles;
    }

    void setContent(String content) {
        this.content = content;
        if (content.length() == LETTER_LENGTH  // set content for a small tile
                || content.isEmpty()) {  // make the tile disappear
            ((Button) view).setText(content);
        } else if (content.length() == WORD_LENGTH) {  // set content for all the sub tiles
            for (int i = 0; i < WORD_LENGTH; ++i) {
                subTiles[i].setContent(String.valueOf(content.charAt(i)));
            }
        }
    }

    void setWord(String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }

    public String getContent() {
        return content;
    }

    public void setView(View view) {
        this.view = view;
    }

    public void setSelected() {
        if (subTiles == null) {  // small tile
            if (view != null) {
                view.getBackground().setLevel(SMALL_TILE_SELECTED);
            }
        } else {  // large tiles, do not call it on board
            for (Tile subTile: subTiles) {
                subTile.setSelected();
            }
        }
    }

    public void setUnselected() {
        if (subTiles == null) {  // small tile
            if (view != null) {
                view.getBackground().setLevel(SMALL_TILE_UNSELECTED);
            }
        } else {  // large tiles, do not call it on board
            for (Tile subTile: subTiles) {
                subTile.setUnselected();
            }
        }
    }

    public void setDisappeared() {
        if (subTiles == null) {  // small tile
            if (view != null) {
                setContent("");
            }
        } else {  // large tiles, do not call it on board
            for (Tile subTile: subTiles) {
                subTile.setDisappeared();
            }
        }
    }

    public void setRemaining() {
        if (subTiles == null) {  // small tile
            if (view != null) {
                view.getBackground().setLevel(SMALL_TILE_REMAINING);
            }
        } else {  // large tiles, do not call it on board
            for (Tile subTile: subTiles) {
                subTile.setRemaining();
            }
        }
    }

}
