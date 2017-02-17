package edu.neu.madcourse.ruihaohuang.scroggle;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import edu.neu.madcourse.ruihaohuang.R;

public class ScroggleGameActivity extends AppCompatActivity {
    private static final String tag = "ScroggleGameActivity";

    private static final int BOARD_SIZE = Tile.BOARD_SIZE;

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

    private PlayerStatusHelper statusHelper = new PlayerStatusHelper();

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

        initializeBoard();
    }

    void initializeBoard() {
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
                smallTiles[large][small].setView(inner);
                final int l = large;
                final int s = small;
                inner.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        statusHelper.selectSmallTile(l, s, board);
                    }
                });
            }
        }

        // set content for all the tiles following the rule
        // that each BOARD_SIZE * BOARD_SIZE tile can form a word
        BoardAssignHelper boardAssignHelper = new BoardAssignHelper(getApplicationContext());
        boardAssignHelper.assignBoard(board);
    }
}
