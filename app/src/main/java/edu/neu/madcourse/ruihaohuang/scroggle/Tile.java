package edu.neu.madcourse.ruihaohuang.scroggle;

import android.view.View;
import android.widget.Button;

/**
 * Created by huangruihao on 2017/2/15.
 */

class Tile {
    static final int BOARD_SIZE = 3;  // use constant value to avoid magic number

    private Tile subTiles[];
    private String content;  // a letter if it is the 1 * 1 tile, a word if it is a BOARD_SIZE * BOARD_SIZE tile
    private View view;

    // small tile: 1 * 1
    // large tile: BOARD_SIZE * BOARD_SIZE
    // board: (BOARD_SIZE * BOARD_SIZE) * (BOARD_SIZE * BOARD_SIZE)
    private final static int SMALL_TILE_UNSELECTED = 0;
    private final static int SMALL_TILE_SELECTED = 1;
    private final static int SMALL_TILE_REMAINING = 2;

    private final static int LETTER_LENGTH = 1;
    private final static int WORD_LENGTH = BOARD_SIZE * BOARD_SIZE;

    Tile() {
        content = "";
    }

    void setSubTiles(Tile[] subTiles) {
        this.subTiles = subTiles;
    }

    Tile[] getSubTiles() {
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

    String getContent() {
        return content;
    }

    void setView(View view) {
        this.view = view;
    }

    void setSelected() {
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

    void setUnselected() {
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

    void setDisappear() {
        if (subTiles == null) {  // small tile
            if (view != null) {
                setContent("");
            }
        } else {  // large tiles, do not call it on board
            for (Tile subTile: subTiles) {
                subTile.setDisappear();
            }
        }
    }

    void setRemaining() {
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
