package edu.neu.madcourse.ruihaohuang.scroggle;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.neu.madcourse.ruihaohuang.R;

public class ScroggleGameActivity extends AppCompatActivity {
    private static final String tag = "ScroggleGameActivity";
    private static final String saveBoardKey = "saveBoardKey";
    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final int TIME_IS_UP = 0;

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

    private ScroggleHelper scroggleHelper;

    private TextView phaseText;
    private TextView scoreText;
    private TextView timeText;
    protected Button controlButton;

    private CountDownTimer phaseOneTimer;
    private CountDownTimer phaseTwoTimer;

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
                scroggleHelper.checkWord();
                scoreText.setText(String.format(getString(R.string.text_score), scroggleHelper.getScore()));
            }
        });

        controlButton = (Button) findViewById(R.id.button_control);
        controlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scroggleHelper.isPlaying()) {
                    pause();
                } else {
                    resume();
                 }
            }
        });

        findViewById(R.id.button_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scroggleHelper.isPlaying()) {
                    scroggleHelper.clearAllSelected();
                }
            }
        });

        findViewById(R.id.button_hint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scroggleHelper.isPlaying()) {
                    // TODO: show hint
                }
            }
        });

        initializeBoard();

        scroggleHelper = new ScroggleHelper(ScroggleGameActivity.this, this, board);

        phaseText = (TextView) findViewById(R.id.text_phase);
        scoreText = (TextView) findViewById(R.id.text_score);
        timeText = (TextView) findViewById(R.id.text_timer);
        // reference: http://stackoverflow.com/questions/6200533/set-textview-style-bold-or-italic
        phaseText.setTypeface(null, Typeface.BOLD);
        phaseText.setText(String.format(getString(R.string.text_phase), scroggleHelper.getPhase().toString()));
        scoreText.setText(String.format(getString(R.string.text_score),
                scroggleHelper.getScore()));

        scroggleHelper.setTimeLeft(getResources().getInteger(R.integer.time_left_warning_phase_one)
                * MILLISECONDS_PER_SECOND);

        phaseOneTimer = new CountDownTimer(getResources().getInteger(R.integer.time_phase_one) * MILLISECONDS_PER_SECOND,
                MILLISECONDS_PER_SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateTimeLeftEverySecond(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                phaseOneFinished();
            }
        };

        phaseTwoTimer = new CountDownTimer(getResources().getInteger(R.integer.time_phase_two) * MILLISECONDS_PER_SECOND,
                MILLISECONDS_PER_SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateTimeLeftEverySecond(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                phaseTwoFinished();
            }
        };

        phaseOneTimer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
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
                        scroggleHelper.selectSmallTile(l, s);
                    }
                });
            }
        }

        // set content for all the tiles following the rule
        // that each BOARD_SIZE * BOARD_SIZE tile can form a word
        BoardAssignHelper boardAssignHelper = new BoardAssignHelper(getApplicationContext());
        boardAssignHelper.assignBoard(board);
    }

    private void updateTimeLeftEverySecond(long millisUntilFinished) {
        int warningTime = 0;
        switch (scroggleHelper.getPhase()) {
            case ONE:
                warningTime = getResources().getInteger(R.integer.time_left_warning_phase_one);
                break;
            case TWO:
                warningTime = getResources().getInteger(R.integer.time_left_warning_phase_two);
                break;
            default:
                break;
        }
        if ((int) (millisUntilFinished / MILLISECONDS_PER_SECOND) <= warningTime) {
            timeText.setTextColor(ContextCompat.getColor(getApplicationContext(),
                    R.color.warning_color));
        }
        timeText.setText(String.format(getString(R.string.text_timer),
                millisUntilFinished / MILLISECONDS_PER_SECOND));
        scroggleHelper.setTimeLeft(millisUntilFinished);
    }

    private void phaseOneFinished() {
        scroggleHelper.clearAllSelected();
        scroggleHelper.nextPhase();
        timeText.setTextColor(ContextCompat.getColor(getApplicationContext(),
                android.R.color.tertiary_text_dark));
        phaseText.setText(String.format(getString(R.string.text_phase), scroggleHelper.getPhase().toString()));
        phaseTwoTimer.start();
    }

    private void phaseTwoFinished() {
        timeText.setText(String.format(getString(R.string.text_timer), TIME_IS_UP));
        AlertDialog.Builder builder = new AlertDialog.Builder(ScroggleGameActivity.this);
        builder.setTitle(getString(R.string.text_game_over))
                .setMessage(String.format(getString(R.string.text_show_score), scroggleHelper.getScore()))
                .setPositiveButton(getString(R.string.button_back), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onBackPressed();
                    }
                });
        builder.create().show();
    }

    private void pause() {
        findViewById(R.id.scroggle_board).setVisibility(View.INVISIBLE);
        controlButton.setText(getString(R.string.button_resume));
        // reference: http://stackoverflow.com/questions/4919703/how-to-set-property-androiddrawabletop-of-a-button-at-runtime
        controlButton.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getApplicationContext(),
                R.drawable.scroggle_resume), null, null);
        scroggleHelper.pause();
        switch (scroggleHelper.getPhase()) {
            case ONE:
                phaseOneTimer.cancel();
                break;
            case TWO:
                phaseTwoTimer.cancel();
                break;
            default:
                break;
        }
    }

    private void resume() {
        findViewById(R.id.scroggle_board).setVisibility(View.VISIBLE);
        controlButton.setText(getString(R.string.button_pause));
        controlButton.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getApplicationContext(),
                R.drawable.scroggle_pause), null, null);
        scroggleHelper.resume();
        switch (scroggleHelper.getPhase()) {
            case ONE:
                phaseOneTimer = new CountDownTimer(scroggleHelper.getTimeLeft(), MILLISECONDS_PER_SECOND) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        updateTimeLeftEverySecond(millisUntilFinished);
                    }

                    @Override
                    public void onFinish() {
                        phaseOneFinished();
                    }
                };
                phaseOneTimer.start();
                break;
            case TWO:
                phaseTwoTimer = new CountDownTimer(scroggleHelper.getTimeLeft(), MILLISECONDS_PER_SECOND) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        updateTimeLeftEverySecond(millisUntilFinished);
                    }

                    @Override
                    public void onFinish() {
                        phaseTwoFinished();
                    }
                };
                phaseTwoTimer.start();
                break;
            default:
                break;
        }
    }
}
