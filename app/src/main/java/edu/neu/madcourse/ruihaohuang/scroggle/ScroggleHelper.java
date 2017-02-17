package edu.neu.madcourse.ruihaohuang.scroggle;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

import edu.neu.madcourse.ruihaohuang.R;
import edu.neu.madcourse.ruihaohuang.dictionary.DictionaryHelper;

/**
 * Created by huangruihao on 2017/2/16.
 */

class ScroggleHelper {
    private static final int BOARD_SIZE = Tile.BOARD_SIZE;
    private static final int NO_SELECTED = -1;
    private static final HashMap<Character, Integer> scoreMap = createScoreMap();

    // reference: http://stackoverflow.com/questions/6802483/how-to-directly-initialize-a-hashmap-in-a-literal-way
    private static HashMap<Character, Integer> createScoreMap() {
        HashMap<Character, Integer> scoreMap = new HashMap<>();
        // 1 point: E, A, I, O, N, R, T, L, S, U; 
        // 2 points: D, G; 
        // 3 points: B, C, M, P; 
        // 4 points: F, H, V, W, Y; 
        // 5 points: K; 
        // 8 points: J, X; 
        // 10 points: Q, Z.
        scoreMap.put('e', 1);
        scoreMap.put('a', 1);
        scoreMap.put('i', 1);
        scoreMap.put('o', 1);
        scoreMap.put('n', 1);
        scoreMap.put('r', 1);
        scoreMap.put('t', 1);
        scoreMap.put('l', 1);
        scoreMap.put('s', 1);
        scoreMap.put('u', 1);
        scoreMap.put('d', 2);
        scoreMap.put('g', 2);
        scoreMap.put('b', 3);
        scoreMap.put('c', 3);
        scoreMap.put('m', 3);
        scoreMap.put('p', 3);
        scoreMap.put('f', 4);
        scoreMap.put('h', 4);
        scoreMap.put('v', 4);
        scoreMap.put('w', 4);
        scoreMap.put('y', 4);
        scoreMap.put('k', 5);
        scoreMap.put('j', 8);
        scoreMap.put('x', 8);
        scoreMap.put('q', 10);
        scoreMap.put('z', 10);
        
        return scoreMap;
    }

    private enum Phase {
        ONE, TWO
    }

    private Phase phase;
    private int selectedLargeTile;
    private ArrayList<Integer> selectedSmallTiles;
    private String word;
    private int score;
    private Context context;
    private Activity activity;
    private DictionaryHelper dictionaryHelper;
    private Tile board;
    private ArrayList<Integer> unavailableLargeTiles;

    ScroggleHelper(Context context, Activity activity, Tile board) {
        phase = Phase.ONE;  // first enter phase 1
        selectedLargeTile = NO_SELECTED;
        selectedSmallTiles = new ArrayList<>();
        word = "";
        score = 0;
        this.context = context;
        this.activity = activity;
        dictionaryHelper = DictionaryHelper.getInstance(context, activity);
        dictionaryHelper.initializeHelper();
        this.board = board;
        unavailableLargeTiles = new ArrayList<>();
    }

    public String getWord() {
        return word;
    }

    public int getScore() {
        return score;
    }

    void selectSmallTile(int large, int small) {
        if (unavailableLargeTiles.contains(large)) {
            return;
        }
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
                if (selectedSmallTiles.isEmpty()) {
                    selectedLargeTile = NO_SELECTED;
                }
                word = word.substring(0, start);
            } else {
                if (isNextTo(selectedSmallTiles.get(selectedSmallTiles.size() - 1), small)) {
                    selectedSmallTiles.add(small);
                    board.getSubTiles()[large].getSubTiles()[small].setSelected();
                    word += board.getSubTiles()[large].getSubTiles()[small].getContent();
                }
            }
        } else {
            selectedSmallTiles.clear();
            selectedSmallTiles.add(small);
            board.getSubTiles()[large].getSubTiles()[small].setSelected();
            if (selectedLargeTile != NO_SELECTED && !unavailableLargeTiles.contains(selectedLargeTile)) {
                board.getSubTiles()[selectedLargeTile].setUnselected();
            }
            selectedLargeTile = large;
            word = board.getSubTiles()[large].getSubTiles()[small].getContent();
        }
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

    // check if the current word is a valid word and update score
    void checkWord() {
        if (dictionaryHelper.wordExists(word)) {
            for (int i = 0; i < word.length(); ++i) {
                score += scoreMap.get(word.charAt(i));
            }
            for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; ++i) {
                if (selectedSmallTiles.contains(i)) {
                    board.getSubTiles()[selectedLargeTile].getSubTiles()[i].setRemaining();
                } else {
                    board.getSubTiles()[selectedLargeTile].getSubTiles()[i].setDisappear();
                }
            }
            unavailableLargeTiles.add(selectedLargeTile);
        } else {
            score += context.getResources().getInteger(R.integer.penalization_score);
            board.getSubTiles()[selectedLargeTile].setUnselected();
            selectedLargeTile = NO_SELECTED;
            selectedSmallTiles.clear();
        }
    }
}
