package edu.neu.madcourse.ruihaohuang.scroggle;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.neu.madcourse.ruihaohuang.R;
import edu.neu.madcourse.ruihaohuang.dictionary.DictionaryHelper;
import edu.neu.madcourse.ruihaohuang.utils.BoardAssignHelper;
import edu.neu.madcourse.ruihaohuang.utils.Tile;

public class ScroggleGameActivity extends AppCompatActivity {
    public static final String tag = "ScroggleGameActivity";
    private static final String TUTORIAL_KEY = "SingleTutorialKey";
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
    private Button controlButton;
    private Button hintsButton;

    private CountDownTimer phaseOneTimer;
    private CountDownTimer phaseTwoTimer;

    private MediaPlayer mediaPlayer;
    private boolean playMusic = true;

    private boolean needTutorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroggle_game);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences tutorialPreferences = this.getPreferences(Context.MODE_PRIVATE);
        needTutorial = tutorialPreferences.getBoolean(TUTORIAL_KEY, true);

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

        hintsButton = (Button) findViewById(R.id.button_hints);
        hintsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scroggleHelper.hintsAvailable()) {
                    scroggleHelper.showHints();
                }
                updateHintsState();
            }
        });

        final ImageButton volumeButton = (ImageButton) findViewById(R.id.button_volume);
        volumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playMusic) {
                    // reference: http://stackoverflow.com/questions/32210559/call-requires-api-level-16-current-min-is-14
                    if (Build.VERSION.SDK_INT >= 16) {
                        volumeButton.setBackground(ContextCompat.getDrawable(getApplicationContext(),
                                R.drawable.scroggle_volume_off));
                    } else {
                        volumeButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(),
                                R.drawable.scroggle_volume_off));
                    }
                    pauseMusic();
                } else {
                    if (Build.VERSION.SDK_INT >= 16) {
                        volumeButton.setBackground(ContextCompat.getDrawable(getApplicationContext(),
                                R.drawable.scroggle_volume_up));
                    } else {
                        volumeButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(),
                                R.drawable.scroggle_volume_up));
                    }
                    resumeMusic();
                }
                playMusic = !playMusic;
            }
        });

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

        initBoard();

        scroggleHelper = new ScroggleHelper(ScroggleGameActivity.this, this, board);

        phaseText = (TextView) findViewById(R.id.text_phase);
        scoreText = (TextView) findViewById(R.id.text_my_score);
        timeText = (TextView) findViewById(R.id.text_timer);
        // reference: http://stackoverflow.com/questions/6200533/set-textview-style-bold-or-italic
        phaseText.setTypeface(null, Typeface.BOLD);
        phaseText.setText(String.format(getString(R.string.text_phase), scroggleHelper.getPhase().toString()));
        scoreText.setText(String.format(getString(R.string.text_score),
                scroggleHelper.getScore()));
        timeText.setText(String.format(getString(R.string.text_timer),
                getResources().getInteger(R.integer.time_phase_one)));

        scroggleHelper.setTimeLeft(getResources().getInteger(R.integer.time_left_warning_phase_one)
                * MILLISECONDS_PER_SECOND);

        updateHintsState();

        if (Build.VERSION.SDK_INT >= 23) {
            int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                if (needTutorial) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(String.format(getString(R.string.text_tutorial), scroggleHelper.getPhase().toString()))
                            .setMessage(String.format(getString(R.string.text_tutorial_phase_one),
                                    getResources().getInteger(R.integer.time_phase_one)))
                            .setPositiveButton(getString(R.string.button_start), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    phaseOneTimer.start();
                                }
                            });
                    builder.create().show();
                } else {
                    phaseOneTimer.start();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (playMusic) {
            pauseMusic();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        setDoesNotNeedTutorial();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (playMusic) {
            resumeMusic();
        }
    }

    @Override
    public void onBackPressed() {
        setDoesNotNeedTutorial();
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (Build.VERSION.SDK_INT >= 23) {
            switch (requestCode) {
                case DictionaryHelper.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        scroggleHelper.initializeDb();
                    }
                    return;
                default:
                    break;
            }
        }
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
        updateHintsState();
        timeText.setText(String.format(getString(R.string.text_timer), 0));
        if (needTutorial && !isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(String.format(getString(R.string.text_tutorial), scroggleHelper.getPhase().toString()))
                    .setMessage(String.format(getString(R.string.text_tutorial_phase_two),
                            getResources().getInteger(R.integer.time_phase_two)))
                    .setPositiveButton(getString(R.string.button_start), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            phaseTwoTimer.start();
                        }
                    });
            builder.create().show();
        } else {
            phaseTwoTimer.start();
        }
    }

    private void phaseTwoFinished() {
        timeText.setText(String.format(getString(R.string.text_timer), TIME_IS_UP));
        if (!isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        setDoesNotNeedTutorial();
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int first = (int) (long) dataSnapshot.child("score").child("first").getValue();
                int second = (int) (long) dataSnapshot.child("score").child("second").getValue();
                int third = (int) (long) dataSnapshot.child("score").child("third").getValue();
                int score = scroggleHelper.getScore();
                if (score >= first) {
                    databaseReference.child("score").child("first").setValue(score);
                    databaseReference.child("score").child("second").setValue(first);
                    databaseReference.child("score").child("third").setValue(second);
                } else if (score < first && score >= second) {
                    databaseReference.child("score").child("second").setValue(score);
                    databaseReference.child("score").child("third").setValue(second);
                } else if (score < second && score >= third) {
                    databaseReference.child("score").child("third").setValue(score);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

    private void pauseMusic() {
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
    }

    private void resumeMusic() {
        mediaPlayer = MediaPlayer.create(this, R.raw.scroggle_bgm);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    private void updateHintsState() {
        if (!scroggleHelper.hintsAvailable()) {
            hintsButton.setEnabled(false);
        }
    }

    private void setDoesNotNeedTutorial() {
        needTutorial = false;
        SharedPreferences tutorialPreferences = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = tutorialPreferences.edit();
        editor.putBoolean(TUTORIAL_KEY, false);
        editor.apply();
    }

    public void startGame() {
        if (needTutorial && !isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(String.format(getString(R.string.text_tutorial), ScroggleHelper.Phase.ONE.toString()))
                    .setMessage(String.format(getString(R.string.text_tutorial_phase_one),
                            getResources().getInteger(R.integer.time_phase_one)))
                    .setPositiveButton(getString(R.string.button_start), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            phaseOneTimer.start();
                        }
                    });
            builder.create().show();
        } else {
            phaseOneTimer.start();
        }
    }
}
