package com.example.yy.dashgraduationdesign.util.dipatchers;

import android.util.Log;

import com.example.yy.dashgraduationdesign.Celluar.GroupCell.GroupCell;
import com.example.yy.dashgraduationdesign.Entities.FileFragment;
import com.example.yy.dashgraduationdesign.Entities.Message;
import com.example.yy.dashgraduationdesign.Entities.Segment;
import com.example.yy.dashgraduationdesign.Integrity.IntegrityCheck;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zxc on 2016/9/12.
 */
public class MultiPullMessageHandler extends MessageHandler {
    private static final String TAG = MultiPullMessageHandler.class.getSimpleName();
    private ExecutorService threadPool = Executors.newFixedThreadPool(1);
    private Bus bus;
    public MultiPullMessageHandler(Bus bus) {
        this.bus = bus;
    }
    @Override
    void handleMessage(Message message) {
        if (message != null) {
            Log.d("ChatFragment", "receive message:" + String.valueOf(message.getMessage()));
            String msgR = message.getMessage();
            if (msgR.startsWith(Bus.SYSTEM_MESSAGE_SHARE_NETWORK)) {
                android.os.Message msg = new android.os.Message();
                msg.what = 1;
                String[] infos = msgR.split("~");
                msg.obj = infos[1];
                GroupCell.groupSession = infos[2];
//                bus.handle(msg);
            } else if (msgR.startsWith(Bus.SYSTEM_MESSAGE)) {
                int miss = Integer.parseInt(msgR.split("~")[2]);
                int url = Integer.parseInt(msgR.split("~")[1]);
                try {
                    InetAddress addr = InetAddress.getByName(msgR.split("~")[3].substring(1));
                    Message msg = new Message();
                    Segment segment = IntegrityCheck.getInstance().getSeg(url);
                    FileFragment frag;
                    if(segment == null || (frag = segment.getFragment(miss))==null) return;
                    msg.setFragment(frag);
                    Bus.sendMsgTo(msg, addr);
                } catch (FileFragment.FileFragmentException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
