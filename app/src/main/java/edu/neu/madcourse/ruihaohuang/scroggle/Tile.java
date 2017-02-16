package edu.neu.madcourse.ruihaohuang.scroggle;

import android.app.Activity;

/**
 * Created by huangruihao on 2017/2/15.
 */

public class Tile {
    private Activity activity;

    private enum Status {
        UNSELECTED, SELECTED, DISAPPEARED
    }

    Tile(Activity activity) {
        this.activity = activity;
    }


}
