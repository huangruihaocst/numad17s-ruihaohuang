package edu.neu.madcourse.ruihaohuang.scroggle;

import java.util.ArrayList;

/**
 * Created by huangruihao on 2017/2/16.
 */

class PlayerStatusHelper {
    private static final int BOARD_SIZE = Tile.BOARD_SIZE;

    private static final int NO_SELECTED = -1;

    private enum Phase {
        ONE, TWO
    }

    private Phase phase;
    private int selectedLargeTile;
    private ArrayList<Integer> selectedSmallTiles;

    public int getSelectedLargeTile() {
        return selectedLargeTile;
    }

    public ArrayList<Integer> getSelectedSmallTiles() {
        return selectedSmallTiles;
    }

    void selectSmallTile(int large, int small, Tile board) {
        if (large == selectedLargeTile) {
            if (selectedSmallTiles.contains(small)) {  // discard all the selected tiles start from this tile
                int start = selectedSmallTiles.indexOf(small);
                for (int i = start; i < selectedSmallTiles.size(); ++i) {
                    board.getSubTiles()[large].getSubTiles()[selectedSmallTiles.get(i)].setUnselected();
                }
                ArrayList<Integer> removeList = new ArrayList<>();
                for (int i = start; i < selectedSmallTiles.size(); ++i) {
                    removeList.add(selectedSmallTiles.get(i));
                }
                selectedSmallTiles.removeAll(removeList);
            } else {
                if (isNextTo(selectedSmallTiles.get(selectedSmallTiles.size() - 1), small)) {
                    selectedSmallTiles.add(small);
                    board.getSubTiles()[large].getSubTiles()[small].setSelected();
                }
            }
        } else {
            selectedSmallTiles.clear();
            selectedSmallTiles.add(small);
            board.getSubTiles()[large].getSubTiles()[small].setSelected();
            if (selectedLargeTile != NO_SELECTED) {
                board.getSubTiles()[selectedLargeTile].setUnselected();
            }
            selectedLargeTile = large;
        }
    }

    PlayerStatusHelper() {
        phase = Phase.ONE;  // first enter phase 1
        selectedLargeTile = NO_SELECTED;
        selectedSmallTiles = new ArrayList<>();
    }

    public Phase getPhase() {
        return phase;
    }
    public void nextPhase() {
        switch (phase) {
            case ONE:
                phase = Phase.TWO;
                break;
            case TWO:
            default:
                break;
        }
    }

    private boolean isNextTo(int previous, int current) {
        int x1 = previous % BOARD_SIZE;
        int y1 = previous / BOARD_SIZE;
        int x2 = current % BOARD_SIZE;
        int y2 = current / BOARD_SIZE;
        return ((x1 - 1 == x2 && y1 == y2) || (x1 - 1 == x2 && y1 - 1 == y2) || (x1 - 1 == x2 && y1 + 1 == y2)
            || (x1 == x2 && y1 - 1 == y2) || (x1 == x2 && y1 + 1 == y2)
            || (x1 + 1 == x2 && y1 == y2) || (x1 + 1 == x2 && y1 - 1 == y2) || (x1 + 1 == x2 && y1 + 1 == y2));
    }

}
