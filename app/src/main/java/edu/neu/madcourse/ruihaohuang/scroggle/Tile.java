package edu.neu.madcourse.ruihaohuang.scroggle;

import android.view.View;
import android.widget.Button;

/**
 * Created by huangruihao on 2017/2/15.
 */

public class Tile {
    static final int BOARD_SIZE = 3;

    private Tile subTiles[];
    private String content;  // a letter if it is the 1 * 1 tile, a word if it is a BOARD_SIZE * BOARD_SIZE tile
    private View view;

    private final static int SMALL_TILE_UNSELECTED = 0;
    private final static int SMALL_TILE_SELECTED = 1;

    private final static int LETTER_LENGTH = 1;
    private final static int WORD_LENGTH = BOARD_SIZE * BOARD_SIZE;

    Tile() {
        content = "";
    }

    void setSubTiles(Tile[] subTiles) {
        this.subTiles = subTiles;
    }

    void setContent(String content) {
        this.content = content;
        if (content.length() == LETTER_LENGTH) {
            ((Button) view).setText(content);
        }
    }

    String getContent() {
        return content;
    }

    void setView(View view) {
        this.view = view;
    }

    void setSmallTileSelected() {
        if (view != null) {
            view.getBackground().setLevel(SMALL_TILE_SELECTED);
        }
    }

    void setSmallTileUnselected() {
        if (view != null) {
            view.getBackground().setLevel(SMALL_TILE_UNSELECTED);
        }
    }

}
