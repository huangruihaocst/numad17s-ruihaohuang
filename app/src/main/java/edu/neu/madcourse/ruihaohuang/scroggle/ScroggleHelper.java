package edu.neu.madcourse.ruihaohuang.scroggle;

import android.app.Activity;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import edu.neu.madcourse.ruihaohuang.R;
import edu.neu.madcourse.ruihaohuang.dictionary.DictionaryHelper;

/**
 * Created by huangruihao on 2017/2/16.
 */

public class ScroggleHelper {
    public static final String tag = "ScroggleHelper";
    private static final int BOARD_SIZE = Tile.BOARD_SIZE;
    private static final int NO_SELECTED = -1;
    private static final int ASCII_OF_A = 97;  // lowercase

    // actually it should be final, but its construction needs context
    private HashMap<Character, Integer> scoreMap;

    enum Phase {
        ONE, TWO
    }

    private Phase phase;
    private boolean playing;
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
    private long timeLeft;
    private int soundSelect, soundValidWord, soundInvalidWord;
    private SoundPool soundPool;
    private float volume = 1f;
    private int hintsLeft;

    ScroggleHelper(Context context, Activity activity, Tile board) {
        this.context = context;
        this.activity = activity;

        phase = Phase.ONE;  // first enter phase 1
        playing = true;
        selectedLargeTile = NO_SELECTED;
        selectedSmallTiles = new ArrayList<>();
        score = 0;

        dictionaryHelper = DictionaryHelper.getInstance(context, activity, tag);
        dictionaryHelper.initializeHelper();
        this.board = board;
        unavailableLargeTiles = new ArrayList<>();
        scoreMap = createScoreMap();
        hintsLeft = context.getResources().getInteger(R.integer.hints_amount);

        if (Build.VERSION.SDK_INT >= 21) {
            SoundPool.Builder builder = new SoundPool.Builder();
            // reference: http://stackoverflow.com/questions/28210921/set-audio-attributes-in-soundpool-builder-class-for-api-21
            builder.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            soundPool = builder.build();
        } else {
            soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        }
        soundSelect = soundPool.load(activity, R.raw.tictactoe_sergenious_movex, 1);
        soundValidWord = soundPool.load(activity, R.raw.tictactoe_erkanozan_miss, 1);
        soundInvalidWord = soundPool.load(activity, R.raw.tictactoe_joanne_rewind, 1);
    }

    void initializeDb() {
        dictionaryHelper.initializeDb();
    }

    // reference: http://stackoverflow.com/questions/6802483/how-to-directly-initialize-a-hashmap-in-a-literal-way
    private HashMap<Character, Integer> createScoreMap() {
        HashMap<Character, Integer> scoreMap = new HashMap<>();
        int[] scores = context.getResources().getIntArray(R.array.score_map);
        for (int i = 0; i < scores.length; ++i) {
            scoreMap.put((char) (ASCII_OF_A + i), scores[i]);
        }
        return scoreMap;
    }

    long getTimeLeft() {
        return timeLeft;
    }

    void setTimeLeft(long timeLeft) {
        this.timeLeft = timeLeft;
    }

    int getScore() {
        return score;
    }

    void pause() {
        playing = false;
    }

    void resume() {
        playing = true;
    }

    boolean isPlaying() {
        return playing;
    }

    void selectSmallTile(int large, int small) {
        switch (phase){
            case ONE:
                if (unavailableLargeTiles.contains(large)) {
                    return;
                }
                soundPool.play(soundSelect, volume, volume, 1, 0, 1f);
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
                soundPool.play(soundSelect, volume, volume, 1, 0, 1f);
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
                        board.getSubTiles()[i].setDisappeared();
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
                    soundPool.play(soundValidWord, volume, volume, 1, 0, 1f);
                    int add = 0;
                    for (int i = 0; i < word.length(); ++i) {
                        add += scoreMap.get(word.charAt(i));
                    }
                    Toast.makeText(context, String.format(context.getString(R.string.toast_word_found),
                            word, add), Toast.LENGTH_SHORT).show();
                    score += add;
                    for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; ++i) {
                        if (selectedSmallTiles.contains(i)) {
                            board.getSubTiles()[selectedLargeTile].getSubTiles()[i].setRemaining();
                        } else {
                            board.getSubTiles()[selectedLargeTile].getSubTiles()[i].setDisappeared();
                        }
                    }
                    unavailableLargeTiles.add(selectedLargeTile);
                    selectedLargeTile = NO_SELECTED;
                    selectedSmallTiles.clear();
                } else {
                    soundPool.play(soundInvalidWord, volume, volume, 1, 0, 1f);
                    score += context.getResources().getInteger(R.integer.penalization_score_phase_one);
                    clearAllSelected();
                    Toast.makeText(context, context.getString(R.string.toast_word_does_not_exist),
                            Toast.LENGTH_LONG).show();
                }
                break;
            case TWO:
                if (dictionaryHelper.wordExists(word)) {
                    if (!wordList.contains(word)) {
                        soundPool.play(soundValidWord, volume, volume, 1, 0, 1f);
                        wordList.add(word);
                        int add = 0;
                        for (int i = 0; i < word.length(); ++i) {
                            add += scoreMap.get(word.charAt(i))
                                    * context.getResources().getInteger(R.integer.magnification);
                        }
                        score += add;
                        Toast.makeText(context, String.format(context.getString(R.string.toast_word_found),
                                word, add), Toast.LENGTH_SHORT).show();
                    } else {  // cannot detect the same word
                        soundPool.play(soundInvalidWord, volume, volume, 1, 0, 1f);
                        Toast.makeText(context, context.getString(R.string.toast_same_word_detected),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    soundPool.play(soundInvalidWord, volume, volume, 1, 0, 1f);
                    score += context.getResources().getInteger(R.integer.penalization_score_phase_two);
                    Toast.makeText(context, context.getString(R.string.toast_word_does_not_exist),
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

    boolean hintsAvailable() {
        return phase == Phase.ONE && hintsLeft > 0;
    }

    void showHints() {
        if (hintsAvailable()) {
            ArrayList<Integer> allLargeTiles = new ArrayList<>();
            for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; ++i) {
                allLargeTiles.add(i);
            }
            allLargeTiles.removeAll(unavailableLargeTiles);
            if (!allLargeTiles.isEmpty()) {
                Collections.shuffle(allLargeTiles);
                Toast.makeText(context, String.format(context.getString(R.string.toast_hints),
                        board.getSubTiles()[allLargeTiles.get(0)].getContent()), Toast.LENGTH_LONG).show();
                --hintsLeft;
            }
        }
    }
}