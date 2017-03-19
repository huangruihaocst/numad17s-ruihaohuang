package edu.neu.madcourse.ruihaohuang.utils;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by huangruihao on 2017/3/18.
 */

public class MyMessagingService extends FirebaseMessagingService {

    private final static String tag = "TwoPlayerScroggleMessagingService";
    static final public String COPA_RESULT = "com.controlj.copame.backend.COPAService.REQUEST_PROCESSED";
    static final public String COPA_MESSAGE = "com.controlj.copame.backend.COPAService.COPA_MSG";

    public static final String splitter = "///";

    LocalBroadcastManager broadcastManager;

    @Override
    public void onCreate() {
        super.onCreate();
        broadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        Intent intent = new Intent(COPA_RESULT);
        intent.putExtra(COPA_MESSAGE, title + splitter + body);
        Log.d("Received message", title + splitter + body);
        broadcastManager.sendBroadcast(intent);
    }
}
