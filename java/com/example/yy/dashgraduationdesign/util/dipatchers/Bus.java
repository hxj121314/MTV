package com.example.yy.dashgraduationdesign.util.dipatchers;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.ArraySet;

import com.example.yy.dashgraduationdesign.Celluar.BTDown;
import com.example.yy.dashgraduationdesign.Celluar.BTShare;
import com.example.yy.dashgraduationdesign.Celluar.MultiPullDown;
import com.example.yy.dashgraduationdesign.Celluar.MultiPullShare;
import com.example.yy.dashgraduationdesign.DASHProxyServer.DashProxyServer;
import com.example.yy.dashgraduationdesign.Entities.ConfigureData;
import com.example.yy.dashgraduationdesign.Entities.Message;
import com.example.yy.dashgraduationdesign.Entities.SendTask;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by zxc on 2016/8/25.
 */
public class Bus implements Dispatcher {
    //Dispatcher中注册用户的handler
    public static final String RECORD_TYPE = "bt_dynamic_leaving";
    private static final String TAG = Bus.class.getSimpleName();
    public static final String HOST_IP = "192.168.49.1";
    public static  String BROADCAST_IP;
    private  Handler currHandler;
    public void setHandler(Handler currHandler) {
        this.currHandler = currHandler;
    }
    //枚举单例模式
    private static Bus instance;
    public static boolean isConnected;
    private Dispatcher dispatcher = new MultiPullDispatcher(this);
    @Override
    public void dispatch(Message msg, InetAddress mClient) {
        dispatcher.dispatch(msg,mClient);
    }

    @Override
    public void handle(android.os.Message message) {
        currHandler.sendMessage(message);
    }


    public enum Singleton {
        Instance;

        Singleton() {
            if (instance == null) instance = new Bus();
            new DashProxyServer();
            configureData.setWorkingMode(ConfigureData.WorkMode.G_MDOE);
            configureData.setDispatcher(new MultiPullDispatcher(instance));
            userName = getRandomString(20);
            try {
                clientAddr = InetAddress.getByName("0.0.0.0");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        public Bus getInstance() {
            return instance;
        }
    }

    public static ConfigureData configureData = new ConfigureData.Builder().setCellularDownPolicy(new MultiPullDown())
            .setCellularSharePolicy(new MultiPullShare())
    .build();


    public static final BlockingQueue<SendTask> taskMessageQueue = new LinkedBlockingQueue<SendTask>();
    public static final BlockingQueue<SendTask> sendMessageQueue = new LinkedBlockingQueue<SendTask>();
    //接收到的系统消息
    public static final BlockingQueue<Message> receiveMessageQueue = new LinkedBlockingQueue<Message>();
    public static boolean isOwner = false;
    public static String userName;
    public static final String SYSTEM_MESSAGE_SHARE_NETWORK = "asdfxczv;asfde[asdfqwer324asfd~";
    public static final String SYSTEM_MESSAGE = "asfsadwaasdfxczvasdfqqweqwr~";
    public static final String SYSYTEM_BT_NO_SEGMENT = "asdfasdfezxcg2341!@$asdfrdt~";
    public static final Set<String> onLineUsers = new ConcurrentSkipListSet<>();
    private final static Lock lock = new ReentrantLock();
    private final static Condition condition = lock.newCondition();
    private static Set<InetAddress> mClients = new HashSet<>();
    public static InetAddress clientAddr;
    public static void sendMsgToAll(Message msg) {
        SendTask sendTask = new SendTask();
        sendTask.setMsg(msg);
        if (isOwner) {
            sendTask.setClients(mClients);
        }
        sendMessageQueue.add(sendTask);
    }

    public static void sendMsgTo(Message msg, InetAddress address) {
        SendTask sendTask = new SendTask();
        Set<InetAddress> sets = new HashSet<>();
        sets.add(address);
        sendTask.setClients(sets);
        sendTask.setMsg(msg);
        sendMessageQueue.add(sendTask);
    }
    public static synchronized Set<InetAddress> getClients() {
        return mClients;
    }

    public static synchronized void setClients(Set<InetAddress> clients) {
        mClients = clients;
    }

    public static void insertReceiveMQ(Message msg) {
        lock.lock();
        try {
            condition.signalAll();
            receiveMessageQueue.add(msg);
        } finally {
            lock.unlock();
        }
    }
    public  static Message getMsg() {
        lock.lock();
        try {
            while (receiveMessageQueue.size() == 0) try {
                condition.await();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return receiveMessageQueue.poll();
        }finally {
            lock.unlock();
        }
    }

    public static String getRandomString(int length) { //length表示生成字符串的长度
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

}
