package edu.neu.madcourse.ruihaohuang.communication;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by huangruihao on 2017/3/4.
 */

public class CommunicationMessagingService extends FirebaseMessagingService {

    private final static String tag = "CommunicationMessagingService";
    Handler handler;
    String data;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
//                Toast.makeText(getApplicationContext(), data, Toast.LENGTH_LONG).show();
            }
        };
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Message message = new Message();
        data = remoteMessage.toString();
        handler.sendMessage(message);
    }
}
