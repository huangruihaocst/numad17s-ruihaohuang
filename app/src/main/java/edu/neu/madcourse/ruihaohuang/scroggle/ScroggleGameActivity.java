package edu.neu.madcourse.ruihaohuang.scroggle;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import edu.neu.madcourse.ruihaohuang.R;

public class ScroggleGameActivity extends AppCompatActivity {
    private static final String tag = "ScroggleGameActivity";

    private static final int BOARD_SIZE = Tile.BOARD_SIZE;
    private static final int AVAILABLE = -1;

    private Tile board;
    // BOARD_SIZE * BOARD_SIZE large tiles
    private Tile[] largeTiles = new Tile[BOARD_SIZE * BOARD_SIZE];
    // BOARD_SIZE * BOARD_SIZE small tiles in each large tile
    private Tile[][] smallTiles = new Tile[BOARD_SIZE * BOARD_SIZE][BOARD_SIZE * BOARD_SIZE];

    private static int smallTileIds[] = {R.id.scroggle_small_0, R.id.scroggle_small_1,
            R.id.scroggle_small_2, R.id.scroggle_small_3, R.id.scroggle_small_4,
            R.id.scroggle_small_5, R.id.scroggle_small_6, R.id.scroggle_small_7,
            R.id.scroggle_small_8};
    private static int largeTileIds[] = {R.id.scroggle_large_0, R.id.scroggle_large_1, 
            R.id.scroggle_large_2, R.id.scroggle_large_3, R.id.scroggle_large_4, 
            R.id.scroggle_large_5, R.id.scroggle_large_6, R.id.scroggle_large_7, 
            R.id.scroggle_large_8};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroggle_game);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.button_submit);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: confirm the letters selected and check if they form a word
            }
        });

        initBoard();
    }

    void initBoard() {
        // initialize all the Tiles by calling their constructors
        board = new Tile();
        for (int large = 0; large < BOARD_SIZE * BOARD_SIZE; ++large) {
            largeTiles[large] = new Tile();
            for (int small = 0; small < BOARD_SIZE * BOARD_SIZE; ++small) {
                smallTiles[large][small] = new Tile();
            }
            largeTiles[large].setSubTiles(smallTiles[large]);
        }
        board.setSubTiles(largeTiles);

        // set views for all the Tiles
        View rootView = findViewById(R.id.scroggle_board);
        board.setView(rootView);
        for (int large = 0; large < BOARD_SIZE * BOARD_SIZE; ++large) {
            View outer = findViewById(largeTileIds[large]);
            largeTiles[large].setView(outer);
            for (int small = 0; small < BOARD_SIZE * BOARD_SIZE; ++small) {
                Button inner = (Button) outer.findViewById(smallTileIds[small]);
                inner.setText("A");
                smallTiles[large][small].setView(inner);
                final int l = large;
                final int s = small;
                final Tile smallTile = smallTiles[large][small];
                inner.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        smallTile.setSmallTileSelected();
                        // TODO: take all these data down for later check
                    }
                });
            }
        }

        // set content for all the tiles following the rule
        // that each BOARD_SIZE * BOARD_SIZE tile can form a word
        // TODO: write a DFS algorithm to fill in the tiles
        String[] words = generateWords();
        for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; ++i) {
            assignSmallTile(largeTiles[i], words[i]);
        }
    }

    // generate words with fixed length
    String[] generateWords() {
        ArrayList<String> wordList = new ArrayList<>();
        switch (BOARD_SIZE) {
            case 3:  // currently only 3 * 3 small tiles
                // use preprocessed word list to speed up
                // reading all the words in is quite time consuming
                BufferedReader reader = new BufferedReader(new InputStreamReader(getResources()
                        .openRawResource(R.raw.nine_letters_word_list)));
                try {
                    String word;
                    while((word = reader.readLine()) != null) {
                        wordList.add(word);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

    void assignSmallTile(Tile largeTile, String word) {
        int[] largeTileStatus = new int[BOARD_SIZE * BOARD_SIZE];
        for (int i = 0;i < largeTileStatus.length; ++i) {
            largeTileStatus[i] = AVAILABLE;
        }
        put(0, -1, largeTileStatus);  // -1 for no previous
        Log.i(tag, word);
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
