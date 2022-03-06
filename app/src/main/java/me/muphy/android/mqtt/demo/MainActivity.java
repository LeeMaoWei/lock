package me.muphy.android.mqtt.demo;

import android.annotation.SuppressLint;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import me.muphy.android.mqtt.demo.MySQL.dao.LockDao;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private static MqttAndroidClient mqttAndroidClient;

    private TextView subText;
    private TextView status;
    private TextView timeText;
    private EditText clientIdEt;
    private Button connBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = findViewById(R.id.status);
        clientIdEt = findViewById(R.id.clientId);
        connBtn = findViewById(R.id.connBtn);
        subText = findViewById(R.id.subText);
        timeText = findViewById(R.id.timeText);

        connect();

        connBtn.setOnClickListener(v -> {
            String topic = clientIdEt.getText().toString();
            if (clientIdEt.isEnabled()) {
                if (topic.isEmpty()) {
                    topic = "test";
                    clientIdEt.setText(topic);
                }
                clientIdEt.setEnabled(false);
                clientIdEt.setTextColor(Color.rgb(128,128, 128));
                connBtn.setText("关机");
                subscribeTopic(topic);
            } else {
                clientIdEt.setEnabled(true);
                clientIdEt.setTextColor(Color.rgb(0,0, 0));
                connBtn.setText("开机");
                unsubscribeTopic(topic);
            }




        });




    }

    /**
     * String host = "tcp://58.16.134.114:48989";
     * String clientId = "android_mqtt_client";
     * String userName = "admin";
     * String passWord = "mf@123";
     */
    public void connect() {
        String host = "tcp://124.222.110.226:1883";
        String clientId = clientIdEt.getText().toString();
        String userName = "admin";
        String passWord = "public";
        if (clientId.isEmpty()) {
            clientId = "android_mqtt_client";
        }

        /* 创建MqttConnectOptions对象，并配置username和password。 */
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();

        mqttConnectOptions.setUserName(userName);


        mqttConnectOptions.setPassword(passWord.toCharArray());


        if (mqttAndroidClient != null) {
            try {
                mqttAndroidClient.disconnect(); //断开连接
            } catch (MqttException e) {
                e.printStackTrace();
            }
            mqttAndroidClient = null;
        }

        /* 创建MqttAndroidClient对象，并设置回调接口。 */
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), host, clientId);
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                String msg = "";
                if (cause != null) {
                    msg = "," + cause.getMessage();
                }
                Log.i(TAG, "连接掉线>host：" + host + msg);
                status.setText("未连接互联网");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                Log.i(TAG, "topic: " + topic + ", msg: " + new String(message.getPayload()));


                statusChange(new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "已发送");
            }
        });



        /* 建立MQTT连接。 */
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "连接成功：" + host);
                    status.setText("未绑定" );
                    subText.setText("lock");
                    timeText.setText("未确认");

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    String msg = "";
                    if (exception != null) {
                        msg = "," + exception.getMessage();
                    }
                    Log.i(TAG, "连接失败>host：" + host + msg);
                    status.setText("未连接至服务器" );
                    assert exception != null;
                    exception.printStackTrace();
                }
            });

        } catch (MqttException e) {
            Log.i(TAG, "连接失败：" + e.getMessage());
            status.setText("未连接" );
            e.printStackTrace();
        }
    }



    public void subscribeTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "订阅成功>topic:" + topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "订阅失败>topic:" + topic);
                }
            });

        } catch (MqttException e) {
            Log.i(TAG, "订阅失败>topic:" + topic);
        }
    }

    public void unsubscribeTopic(String topic) {
        try {
            mqttAndroidClient.unsubscribe(topic);

        } catch (MqttException e) {
            Log.i(TAG, "退订失败>topic:" + topic);
        }
    }

    @Override
    public void onDestroy() {
        if (mqttAndroidClient != null) {
            try {
                mqttAndroidClient.disconnect(); //断开连接
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
    @SuppressLint("SetTextI18n")
    public void statusChange(String msg){
        LockDao lockDao = new LockDao();
        char bind=msg.charAt(0);
        String lockText;
        String time_Text;
        if(bind=='0')
        {
            new Thread(() -> {

                clientIdEt.findViewById(R.id.clientId);
                boolean aa = lockDao.login(Integer.parseInt(clientIdEt.getText().toString()),msg.substring(1),judgestate(subText.getText().toString()));
                if(aa){
                    hand1.sendEmptyMessage(1);
                }else {
                    hand1.sendEmptyMessage(0);
                }
            }).start();
            status.setText("绑定至"+msg.substring(1));
        }else if(bind=='1')
        {
            lockText=msg.substring(1);
            new Thread(() -> {

                clientIdEt.findViewById(R.id.clientId);
                lockDao.update(Integer.parseInt(clientIdEt.getText().toString()),judgestate(lockText));
            }).start();

            subText.setText(lockText);
        }
        else
        {
            time_Text="从"+msg.substring(1,3)+":"+msg.substring(3,5)+"解锁到"+msg.substring(5,7)+":"+msg.substring(7)+"锁住";
            timeText.setText(time_Text);
        }



    }
    public int judgestate(String l){
        if(l.equals("lock"))
        {
            return 1;
        }else {
            return 0;
        }
    }
    @SuppressLint("HandlerLeak")
    final Handler hand1 = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(Message msg) {

            if(msg.what == 1)
            {
                Toast.makeText(getApplicationContext(),"绑定成功",Toast.LENGTH_LONG).show();

            }
            else if(msg.what == 0)
            {
                Toast.makeText(getApplicationContext(),"绑定失败",Toast.LENGTH_LONG).show();
            }

        }
    };

}