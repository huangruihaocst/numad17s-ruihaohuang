package edu.neu.madcourse.ruihaohuang.scroggle;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import edu.neu.madcourse.ruihaohuang.R;

/**
 * Created by huangruihao on 2017/2/16.
 */

class BoardAssignHelper {
    private static final int BOARD_SIZE = Tile.BOARD_SIZE;
    private static final int AVAILABLE = -1;

    private Context context;

    BoardAssignHelper(Context context) {
        this.context = context;
    }

    void assignBoard(Tile board) {
        String[] words = generateWords();
        for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; ++i) {
            assignSmallTile(board.getSubTiles()[i], words[i]);
        }
    }

    // generate words with fixed length
    private String[] generateWords() {
        ArrayList<String> wordList = new ArrayList<>();
        switch (BOARD_SIZE) {
            case 3:  // currently only 3 * 3 small tiles
                // use preprocessed word list to speed up
                // reading all the words in is quite time consuming
                BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources()
                        .openRawResource(R.raw.nine_letters_word_list)));
                try {
                    String word;
                    while((word = reader.readLine()) != null) {
                        wordList.add(word);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
        // reference: http://stackoverflow.com/questions/8115722/generating-unique-random-numbers-in-java
        ArrayList<Integer> indexList = new ArrayList<>();
        for (int i = 0; i < wordList.size(); ++i) {
            indexList.add(i);
        }
        Collections.shuffle(indexList);
        String[] words = new String[BOARD_SIZE * BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; ++i) {
            words[i] = wordList.get(indexList.get(i));
        }
        return words;
    }

    private void assignSmallTile(Tile largeTile, String word) {
        int[] largeTileStatus = new int[BOARD_SIZE * BOARD_SIZE];
        for (int i = 0;i < largeTileStatus.length; ++i) {
            largeTileStatus[i] = AVAILABLE;
        }
        put(0, -1, largeTileStatus);  // -1 for no previous
        String content = "";
        for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; ++i) {
            content += word.charAt(largeTileStatus[i]);
        }
        largeTile.setContent(content);
    }

    /**
     * Put index on a certain position of the large board (0 ~ BOARD_SIZE * BOARD_SIZE - 1).
     * Depth First Search.
     * @param index the index that need to be assigned
     * @param previous the position that last index was assigned
     * @param largeTile the large tile status
     */
    private void put(int index, int previous, int[] largeTile) {
        if (index == BOARD_SIZE * BOARD_SIZE) {  // done
            return;
        } else if (index == 0) {  // no previous
            int position = (int) (BOARD_SIZE * BOARD_SIZE * Math.random());
            largeTile[position] = index;
            put(index + 1, position, largeTile);
        } else {
            ArrayList<Integer> tryList = new ArrayList<>();
            // (0, 0) is the upper left corner
            // x-axis right, y-axis down
            int x = previous % BOARD_SIZE;
            int y = previous / BOARD_SIZE;
            if (x != 0 && y != 0) {
                tryList.add((x - 1) + (y - 1) * BOARD_SIZE);
            }
            if (x != 0) {
                tryList.add((x - 1) + y * BOARD_SIZE);
            }
            if (x != 0 && y != BOARD_SIZE - 1) {
                tryList.add((x - 1) + (y + 1) * BOARD_SIZE);
            }
            if (y != 0) {
                tryList.add(x + (y - 1) * BOARD_SIZE);
            }
            if (y != BOARD_SIZE - 1) {
                tryList.add(x + (y + 1) * BOARD_SIZE);
            }
            if (x != BOARD_SIZE - 1 && y != 0) {
                tryList.add((x + 1) + (y - 1) * BOARD_SIZE);
            }
            if (x != BOARD_SIZE - 1) {
                tryList.add((x + 1) + y * BOARD_SIZE);
            }
            if (x != BOARD_SIZE - 1 && y != BOARD_SIZE - 1) {
                tryList.add((x + 1) + (y + 1) * BOARD_SIZE);
            }
            Collections.shuffle(tryList);
            for (int position: tryList) {
                if (largeTile[position] == AVAILABLE) {
                    largeTile[position] = index;
                    put(index + 1, position, largeTile);
                    if (hasDone(largeTile)) {
                        break;
                    } else {
                        largeTile[position] = AVAILABLE;
                    }
                }
            }
        }
    }

    private boolean hasDone(int[] largeTile) {
        for (int i = 0;i < BOARD_SIZE * BOARD_SIZE; ++i) {
            if (largeTile[i] == AVAILABLE) {
                return false;
            }
        }
        return true;
    }
}
