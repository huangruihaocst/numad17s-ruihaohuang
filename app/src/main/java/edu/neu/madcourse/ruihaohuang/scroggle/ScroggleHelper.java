package edu.neu.madcourse.ruihaohuang.scroggle;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

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

    enum Phase {
        ONE, TWO
    }

    private Phase phase;
    private int selectedLargeTile;
    private ArrayList<Integer> selectedSmallTiles;
    // used only in phase two
    // it has BOARD_SIZE * BOARD_SIZE different values
    private ArrayList<Integer> selectedTiles;
    private int score;
    private Context context;
    private Activity activity;
    private DictionaryHelper dictionaryHelper;
    private Tile board;
    private ArrayList<Integer> unavailableLargeTiles;
    // used only in phase two
    // already detected valid words
    private ArrayList<String> wordList;

    ScroggleHelper(Context context, Activity activity, Tile board) {
        phase = Phase.ONE;  // first enter phase 1
        selectedLargeTile = NO_SELECTED;
        selectedSmallTiles = new ArrayList<>();
        score = 0;
        this.context = context;
        this.activity = activity;
        dictionaryHelper = DictionaryHelper.getInstance(context, activity);
        dictionaryHelper.initializeHelper();
        this.board = board;
        unavailableLargeTiles = new ArrayList<>();
    }

    int getScore() {
        return score;
    }

    void selectSmallTile(int large, int small) {
        switch (phase){
            case ONE:
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
                    if (selectedLargeTile != NO_SELECTED && !unavailableLargeTiles.contains(selectedLargeTile)) {
                        board.getSubTiles()[selectedLargeTile].setUnselected();
                    }
                    selectedLargeTile = large;
                }
                break;
            case TWO:
                // selected small tiles must be from valid words in phase one
                if (board.getSubTiles()[large].getSubTiles()[small].getContent().isEmpty()) {
                    return;
                }
                if (selectedTiles.isEmpty()) {  // nothing selected
                    selectedTiles.add(large * BOARD_SIZE * BOARD_SIZE + small);
                    board.getSubTiles()[large].getSubTiles()[small].setSelected();
                } else {
                    int current = large * BOARD_SIZE * BOARD_SIZE + small;
                    int previous = selectedTiles.get(selectedTiles.size() - 1);
                    int previousLarge = previous / (BOARD_SIZE * BOARD_SIZE);
                    int previousSmall = previous % (BOARD_SIZE * BOARD_SIZE);
                    if (previousLarge == large) {
                        if (previousSmall == small) {  // select the same tile
                            selectedTiles.remove(selectedTiles.size() - 1);
                            board.getSubTiles()[large].getSubTiles()[small].setRemaining();
                        } else {
                            selectedTiles.set(selectedTiles.size() - 1, current);
                            board.getSubTiles()[previousLarge].getSubTiles()[previousSmall].setRemaining();
                            board.getSubTiles()[large].getSubTiles()[small].setSelected();
                        }
                    } else if (isNextTo(previousLarge, large)) {
                        selectedTiles.add(current);
                        board.getSubTiles()[large].getSubTiles()[small].setSelected();
                    }
                }
                break;
            default:
                break;
        }
    }

    Phase getPhase() {
        return phase;
    }

    void nextPhase() {
        switch (phase) {
            case ONE:
                phase = Phase.TWO;
                // initialize here to avoid typos in codes
                selectedTiles = new ArrayList<>();
                wordList = new ArrayList<>();
                for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; ++i) {
                    if (!unavailableLargeTiles.contains(i)) {
                        board.getSubTiles()[i].setDisappear();
                    }
                }
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
        String word = getWord();
        if (word.isEmpty()) {
            return;
        }
        switch (phase) {
            case ONE:
                if (selectedLargeTile == NO_SELECTED) {
                    return;
                }
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
                    selectedLargeTile = NO_SELECTED;
                    selectedSmallTiles.clear();
                } else {
                    score += context.getResources().getInteger(R.integer.penalization_score_phase_one);
                    clearAllSelected();
                    Toast.makeText(context, context.getString(R.string.word_does_not_exist),
                            Toast.LENGTH_LONG).show();
                }
                break;
            case TWO:
                if (dictionaryHelper.wordExists(word)) {
                    if (!wordList.contains(word)) {
                        wordList.add(word);
                        for (int i = 0; i < word.length(); ++i) {
                            score += scoreMap.get(word.charAt(i))
                                    * context.getResources().getInteger(R.integer.magnification);
                        }
                    } else {  // cannot detect the same word
                        Toast.makeText(context, context.getString(R.string.toast_same_word_detected),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    score += context.getResources().getInteger(R.integer.penalization_score_phase_two);
                    Toast.makeText(context, context.getString(R.string.word_does_not_exist),
                            Toast.LENGTH_LONG).show();
                }
                clearAllSelected();
                break;
            default:
                break;
        }
    }

    private String getWord() {
        String word = "";
        switch (phase) {
            case ONE:
                for (int selectedSmallTile: selectedSmallTiles) {
                    word += board.getSubTiles()[selectedLargeTile].getSubTiles()[selectedSmallTile].getContent();
                }
                break;
            case TWO:
                for (int position: selectedTiles) {
                    int large = position / (BOARD_SIZE * BOARD_SIZE);
                    int small = position % (BOARD_SIZE * BOARD_SIZE);
                    word += board.getSubTiles()[large].getSubTiles()[small].getContent();
                }
        }
        return word;
    }

    void clearAllSelected() {
        switch (phase) {
            case ONE:
                for (int selectedSmallTile: selectedSmallTiles) {
                    board.getSubTiles()[selectedLargeTile].getSubTiles()[selectedSmallTile].setUnselected();
                }
                selectedLargeTile = NO_SELECTED;
                selectedSmallTiles.clear();
                break;
            case TWO:
                for (int selectedTile: selectedTiles) {
                    int large = selectedTile / (BOARD_SIZE * BOARD_SIZE);
                    int small = selectedTile % (BOARD_SIZE * BOARD_SIZE);
                    board.getSubTiles()[large].getSubTiles()[small].setRemaining();
                }
                selectedTiles.clear();
                break;
            default:
                break;
        }
    }
}