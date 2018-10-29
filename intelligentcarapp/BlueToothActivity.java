package com.example.intelligentcarapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

/*
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-feature android:name="android.hardware.bluetooth_le" android:required="true" />
 */

@SuppressWarnings("unchecked")
public class BlueToothActivity extends BaseActivity implements AdapterView.OnItemClickListener,View.OnClickListener {

    private static final String TAG = "m2a";
    private static final int REQUEST_ENABLE_BT = 1;
    public BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> paireDevices;
    private BluetoothSocket socket = null;

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private AcceptThread mAcceptThread;
    private int mState = 0;

    public static final UUID MY_UUID =UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private List<BluetoothDevice> list;
    private ListView listView;
    private ListViewAdapter adapter;
    private ProgressDialog dialog;
    private Context context = this;

    public static final int STATE_NONE = 0;       // 初始状态
    public static final int STATE_LISTEN = 1;     // 等待连接
    public static final int STATE_CONNECTING = 2; // 正在连接
    public static final int STATE_CONNECTED = 3;  // 已经连接上设备

    private IntentFilter filter;


    private IntentFilter intentFilter;
    private LocalBroadcastManager localBroadcastManager;
    TextView heartTextView;
    private RealTimeDataReceiver realTimeDataReceiver;
    private List<Integer> new256Bits = new ArrayList<>();
    int cnt = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        for(int i=0;i<256;i++){
            new256Bits.add(0);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth);
        initView();//每次进入onCreate都要进行View按键的初始化
        //1、获取BluetoothAdapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //2、判断是否支持蓝牙，并打开蓝牙
        if(mBluetoothAdapter == null ||!mBluetoothAdapter.isEnabled()){
            //弹出对话框提示用户是后打开
            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabler,REQUEST_ENABLE_BT);

        }else{
            //注册
            initAdapter();start();
            filter = getIntentFilter();registerReceiver(receiver,filter);
        }

        //广播
        heartTextView = findViewById(R.id.heart_now);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        intentFilter = new IntentFilter();
        intentFilter.addAction("realtime.fragment.LOCAL_BROADCAST");
        realTimeDataReceiver = new RealTimeDataReceiver();
        localBroadcastManager.registerReceiver(realTimeDataReceiver,intentFilter);
    }

    //第一种：打开AcceptThread等待接受丛机连接
    public synchronized void start() {
//        Log.d(TAG, "start");
//        if (mConnectThread != null) {
//            mConnectThread.cancel();
//            mConnectThread = null;
//        }
//        if (mConnectedThread != null) {
//            mConnectedThread.cancel();
//            mConnectedThread = null;
//        }
//        if(mAcceptThread != null){
//            mAcceptThread.cancel();
//            mAcceptThread = null;
//        }
//        setState(STATE_LISTEN);
//
//        if (mAcceptThread == null) {
//            mAcceptThread = new AcceptThread();
//            mAcceptThread.start();
//        }
    }

    //用于接收连接请求
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("name",
                        MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmServerSocket = tmp;
        }
        public void run() {
            Log.d(TAG, "BEGIN mAcceptThread " + this);
            BluetoothSocket socket;
            // 在没有连接上的时候accept
            while (mState!=3) {
                try {
                    Log.d(TAG, "BEGIN accept " + this);
                    socket = mmServerSocket.accept();
                    Log.d(TAG, "END accept " + this);
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed ", e);
                    break;
                }

                if (socket != null) {
                    synchronized (BlueToothActivity.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // 准备通信
                                connected(socket);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread");
        }

        public void cancel() {
            Log.d(TAG, "Socket cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket close() of server failed", e);
            }
        }
    }

    //第二种：作为从机，点击某一个（通过地址）连接远端主机
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }
        //获取MAC地址，用于连接
        String address = list.get(i).getAddress();
        Log.i(TAG,address);
        BluetoothDevice btDev = mBluetoothAdapter.getRemoteDevice(address);
        connect(btDev);
    }

    private void connect(BluetoothDevice device){
        setState(STATE_CONNECTING);
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    //用于蓝牙连接的线程
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;


        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                //尝试建立安全的连接
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.i(TAG,"获取 BluetoothSocket失败");
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        @Override
        public void run() {
            if(mBluetoothAdapter.isDiscovering()){
                mBluetoothAdapter.cancelDiscovery();
            }
            try {
                mmSocket.connect();
            } catch (IOException e) {
                Log.i(TAG,"socket连接失败"+e.getMessage());
                setState(STATE_LISTEN);
                Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.TOAST,"Socket连接失败");
                msg.setData(bundle);
                mHandler.sendMessage(msg);
                return;
            }

            synchronized (BlueToothActivity.this){
                mConnectThread = null;
            }
            //启动用于传输数据的线程connectedThread
            connected(mmSocket);
        }

        public void cancel(){
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //前两种连接中任意一种完成后：启动ConnectedThread
    public synchronized void connected(BluetoothSocket socket){

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }


        setState(STATE_CONNECTED);
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
//        Intent intent = getIntent();
//        String responseData = intent.getStringExtra("responseData");
        Intent intent = new Intent(this,MainActivity.class);
//        intent.putExtra("connected","true");
//        intent.putExtra("responseData",responseData);
        startActivity(intent);

    }

    //蓝牙连接完成后进行输入输出流的绑定
    private class ConnectedThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        /*
        0xff(包头),心率，    体温，lose_face,体重(两个char)，血脂，
        血压(高)，  血压(低)，疲劳驾驶(char  0x01为疲劳  0x00为非疲劳),0x0d，0x0a(包尾)
         */
        @SuppressWarnings("unchecked")
        public void update(final ArrayList<Integer> list){
            new Thread() {
                public void run() {
                    try {
                        if (list.size() == 72) {
                            //这儿是耗时操作，完成之后更新UI；//根据bs更新ui====================================================
                            Intent intent = new Intent("realtime.fragment.LOCAL_BROADCAST");
                            intent.putExtra("heart",list.get(0));
                            if (cnt >= 49) {
                                cnt = 49;
                            }
                            float temp = list.get(1) / 10f;
                            if (list.get(1) / 10f < 2) {
                                temp = 0;
                            } else {
                                temp = 31f + temp;
                            }
                            intent.putExtra("temperature",temp);
                            intent.putExtra("lose_face",list.get(2));
                            intent.putExtra("micro_circle",list.get(3)); //微循环
                            intent.putExtra("blood_oxygen",Float.valueOf(list.get(4))); //血氧
                            intent.putExtra("pressure_high",list.get(5));
                            intent.putExtra("pressure_low",list.get(6));
                            intent.putExtra("fatigue_driving",list.get(7));

                            List<Integer> new64Bits = list.subList(8,72);
                            new256Bits.subList(0,64).clear();
                            new256Bits.addAll(new64Bits);
                            Log.d("pulse_values_256",Arrays.toString(new256Bits.toArray())+"  "+new256Bits.size());
                            float[] fs = new float[256];
                            for(int i=0;i<256;i++){
                                int cur = new256Bits.get(i);
                                if(cur<128)
                                    fs[i] = new256Bits.get(i);
                                else
                                    fs[i] = new256Bits.get(i)-256;
                                fs[i] += 128;
                            }
                            intent.putExtra("pulse_values_256",fs);
                            localBroadcastManager.sendBroadcast(intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            while(mState == STATE_CONNECTED){
                try {
                    // Read from the InputStream
//                    Scanner in = new Scanner(mmInStream,"GBK");
//                    String str = in.nextLine();
                    int cur;
                    ArrayList<Integer> list = new ArrayList<>();
                    while((cur = mmInStream.read())!=-1){
                        if(cur == 13){
                            int next;
                            if((next =mmInStream.read())!=10){
                                list.add(cur);list.add(next);
                            }else{
                                break;
                            }
                        }else if(cur == 255){
                            list = new ArrayList<>();
                        }else{
                            list.add(cur);
                        }
                    }
                    Log.i(TAG, "read: " + Arrays.toString(list.toArray()));

                    //利用handle传递数据
                    Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
                    Bundle bundle = new Bundle();
//                    bundle.putInt("heart",bs[1]);
                    bundle.putString(Constants.TOAST, ""+Arrays.toString(list.toArray()));
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                    update(list);
                } catch (Exception e) {
                    Log.e(TAG, "disconnected", e);
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    //扫描到设备或者扫描完成进行广播
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(!list.contains(device)){
                        adapter.addData(device);
                    }
                }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    dialog.dismiss();
                    Toast.makeText(context,"扫描完毕",Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };
    //子线程更新UI用的
    private final android.os.Handler mHandler = new android.os.Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case Constants.MESSAGE_TOAST:
                    try {
                        if(null!=context){
//                            Toast.makeText(context,msg.getData().getString(Constants.TOAST),
//                                    Toast.LENGTH_SHORT).show();
                            if (msg.getData().getString(Constants.TOAST) != null) {
                                Log.d("heart_now",msg.getData().getString(Constants.TOAST));
//                                heartTextView.setText(msg.getData().getString(Constants.TOAST) +
//                                        (int) (1 + Math.random() * (10 - 1 + 1)));
                            }
                            //final int heart = msg.getData().getInt("heart");
                            //heartTextView.setText(heart);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    private void initView() {
        Button scan = (Button)findViewById(R.id.btn_scan);
        scan.setOnClickListener(this);

        Button send = (Button)findViewById(R.id.btn_send);
        send.setOnClickListener(this);

        list = new ArrayList<BluetoothDevice>();
        listView = (ListView)findViewById(R.id.listView);
        adapter = new ListViewAdapter(BlueToothActivity.this);
        adapter.setDataSource(list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    private void initAdapter() {
        checkBluetoothPermission();
        //将配过对的设备加入list
        paireDevices = mBluetoothAdapter.getBondedDevices();
        if(paireDevices.size()>0){
            for(BluetoothDevice device: paireDevices){
                adapter.addData(device);
            }
        }
    }

    @Override//点击打开蓝牙对话框中的确定按钮后
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK){

            Toast.makeText(BlueToothActivity.this,"蓝牙已启用",Toast.LENGTH_SHORT).show();
            //注册
            initAdapter();start();filter = getIntentFilter();registerReceiver(receiver,filter);

        }else if(requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED){
            //未启用
            Toast.makeText(BlueToothActivity.this,"请启用蓝牙",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //校验蓝牙权限
    private void checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            //校验是否已具有模糊定位权限
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(BlueToothActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_ENABLE_BT);
            } else {
                //具有权限

            }
        } else {
            //系统不高于6.0直接执行

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //同意权限
                Toast.makeText(context,"获取蓝牙权限成功",Toast.LENGTH_LONG).show();

            } else {
                // 权限拒绝
                Toast.makeText(context,"没有蓝牙权限",Toast.LENGTH_LONG).show();
            }
        }
    }

    private IntentFilter getIntentFilter(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        return intentFilter;
    }

    @Override
    protected void onDestroy() {
        if(mBluetoothAdapter.isDiscovering()){
            //mBluetoothAdapter.cancelDiscovery();
        }
        //取消广播注册
        try {
            if (receiver != null) unregisterReceiver(receiver);
            //stop();
        }catch (Exception e){

        }finally {
            super.onDestroy();
        }

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    @Override//点击扫描按钮和send按钮
    public void onClick(View view) {
        Log.d(TAG,mBluetoothAdapter.getState()+"");
        switch (view.getId()){
            case R.id.btn_scan:
                //如果蓝牙功能被关闭则要求打开
                Log.d("sta1",mBluetoothAdapter.getState()+"");
                if(mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF||
                        mBluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_OFF){
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);

                }
                //开始扫描,在dialog中处理后退键事件，取消扫描
                mBluetoothAdapter.startDiscovery();
                dialog = new ProgressDialog(context);
                dialog.setMessage("正在扫描...");
                dialog.setCancelable(true);
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        if(i == KeyEvent.KEYCODE_BACK){
                            Log.i(TAG,"Back down");
                            dialog.dismiss();
                            mBluetoothAdapter.cancelDiscovery();
                            Log.i(TAG,"Cancel Discovery");
                        }
                        return false;
                    }
                });
                dialog.show();
                break;
            case R.id.btn_send:
                this.write(new String("hello").getBytes());
                break;
        }
    }

    //发送字符串
    private void write(byte[] out){
        ConnectedThread r = null;
        try{
            r = mConnectedThread;
            r.write(out);
        }catch (NullPointerException e){
            Toast.makeText(context,"无法发送",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    public synchronized void stop() {
        Log.d(TAG, "stop");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }

}

abstract class LBaseAdapter<E, V extends LBaseAdapter.BaseViewHolder> extends BaseAdapter {

    private Context context;
    private List<E> dataSource = new ArrayList<>(); //初始化一个防止getCount()空指针

    public LBaseAdapter(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    //替换原有数据源
    public void setDataSource(List<E> dataSource) {
        setDataSource(dataSource,true);
    }

    //如果isClear==true,则替换原有数据源，否则加到数据源后面
    public void setDataSource(List<E> dataSource, boolean isClear) {
        if (isClear) this.dataSource.clear();
        this.dataSource = dataSource;
        notifyDataSetChanged();
    }

    //只加一个数据
    public void addData(E data) {
        this.dataSource.add(data);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.dataSource.size();
    }


    @Override
    public E getItem(int position) {
        return this.dataSource.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        V viewHolder = null;
        if (convertView == null) {
            viewHolder = createViewHolder(position, parent);
            if (viewHolder == null || viewHolder.getRootView() == null) {
                throw new NullPointerException("createViewHolder不能返回null或view为null的实例");
            }
            convertView = viewHolder.getRootView();
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (V) convertView.getTag();
        }
        //给当前复用的holder一个正确的position
        viewHolder.setPosition(position);
        bindViewHolder(viewHolder,position,getItem(position));
        return viewHolder.getRootView();
    }

    protected abstract V createViewHolder(int position, ViewGroup parent);

    protected abstract void bindViewHolder(V holder,int position, E data);

    public static class BaseViewHolder {
        private View rootView;
        private SparseArray<View> viewCache = new SparseArray<>();
        private int position = -1;

        public View getRootView() {
            return rootView;
        }

        void setPosition(int position) {
            this.position = position;
        }

        public int getPosition() {
            return position;
        }

        public BaseViewHolder(View rootView) {
            this.rootView = rootView;
        }

        public <R> R getView(@IdRes int viewID) {
            View cachedView = viewCache.get(viewID);
            if(null == cachedView) {
                cachedView = rootView.findViewById(viewID);
                viewCache.put(viewID, cachedView);
            }
            return (R) cachedView;
        }
    }


}

//ListView适配器
class ListViewAdapter extends LBaseAdapter<BluetoothDevice,LBaseAdapter.BaseViewHolder> implements AdapterView.OnItemClickListener{
    private Context context;

    public ListViewAdapter(Context context){
        super(context);
        this.context = context;
    }

    @Override
    protected BaseViewHolder createViewHolder(int position, ViewGroup parent) {
        return new BaseViewHolder(View.inflate(getContext(),R.layout.device_item,null));
    }

    @Override
    protected void bindViewHolder(BaseViewHolder holder, int position, BluetoothDevice data) {
        TextView txtName = holder.getView(R.id.tVName);
        TextView txtAddress = holder.getView(R.id.tVAddress);

        String deviceName = data.getName();
        String deviceAddress = data.getAddress();

        if (deviceName != null && deviceName.length() > 0){
            txtName.setText(deviceName);
        } else{
            txtName.setText("unknown_device");
        }
        txtAddress.setText(deviceAddress);
    }

    @Override
    public void setDataSource(List<BluetoothDevice> dataSource) {
        super.setDataSource(dataSource);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(context,"点击了： "+position, Toast.LENGTH_SHORT).show();
    }


}