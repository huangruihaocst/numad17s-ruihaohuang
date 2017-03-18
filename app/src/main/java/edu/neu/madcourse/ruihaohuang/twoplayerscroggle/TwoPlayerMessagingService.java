package edu.neu.madcourse.ruihaohuang.twoplayerscroggle;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by huangruihao on 2017/3/17.
 */

public class TwoPlayerMessagingService extends FirebaseMessagingService {

    private final static String tag = "TwoPlayerMessagingService";
    static final public String COPA_RESULT = "com.controlj.copame.backend.COPAService.REQUEST_PROCESSED";
    static final public String COPA_MESSAGE = "com.controlj.copame.backend.COPAService.COPA_MSG";

    LocalBroadcastManager broadcastManager;

    @Override
    public void onCreate() {
        super.onCreate();
        broadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String message = remoteMessage.getNotification().getBody();
        Intent intent = new Intent(COPA_RESULT);
        intent.putExtra(COPA_MESSAGE, message);
        broadcastManager.sendBroadcast(intent);
    }
}
