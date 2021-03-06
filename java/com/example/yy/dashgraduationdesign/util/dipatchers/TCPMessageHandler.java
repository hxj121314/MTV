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
 * Created by zxc on 2016/8/25.
 */
public class TCPMessageHandler extends MessageHandler {
    private static final String TAG = TCPMessageHandler.class.getSimpleName();
    private ExecutorService threadPool = Executors.newFixedThreadPool(1);
    private Bus bus;
    public TCPMessageHandler(Bus bus) {
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
                bus.handle(msg);
//                    threadPool.execute(this);
            } else if (msgR.startsWith(Bus.SYSTEM_MESSAGE)) {
                //only owner can handle this.
                if(!Bus.isOwner) return;
                int miss = Integer.parseInt(msgR.split("~")[2]);
                int url = Integer.parseInt(msgR.split("~")[1]);
                try {
                    InetAddress addr = InetAddress.getByName(msgR.split("~")[3].substring(1));
                    Message msg = new Message();
                    Segment segment = IntegrityCheck.getInstance().getSeg(url);
                    FileFragment frag;
                    if(segment == null || (frag = segment.getFragment(miss))==null){
                        Log.d(TAG, "no fragment,send back to client");
                        msg.setMessage(Bus.SYSYTEM_BT_NO_SEGMENT+url+"~"+miss+"~"+addr);
                        Bus.sendMsgTo(msg,addr);
                        return;
                    }
                    msg.setFragment(frag);
                    Bus.sendMsgTo(msg, addr);
                } catch (FileFragment.FileFragmentException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            } else if (msgR.startsWith(Bus.SYSYTEM_BT_NO_SEGMENT)) {
                //only client can handle this.
                if(Bus.isOwner) return;
                //seeder tells that it doesn't has this segment,the client will fetch it again later while the queue is idle
                int miss = Integer.parseInt(msgR.split("~")[2]);
                int url = Integer.parseInt(msgR.split("~")[1]);
                try {
                    Message msg = new Message();
                    msg.setMessage(Bus.SYSTEM_MESSAGE+url+"~"+miss+"~"+msgR.split("~")[3]);
                    Bus.sendMsgTo(msg,InetAddress.getByName(Bus.HOST_IP));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
