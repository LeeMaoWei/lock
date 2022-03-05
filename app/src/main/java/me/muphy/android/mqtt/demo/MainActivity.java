package me.muphy.android.mqtt.demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getSimpleName();
    private static MqttAndroidClient mqttAndroidClient;
    /*

    private Button bBtn;

    private EditText pubText;

    private EditText pubContent;
    private EditText pubTopic;
    private EditText subTopic;

    private TextView pubMsg;
    private TextView subMsg;

    private EditText passwordEt;
    private EditText usernameEt;
    private EditText hostEt;*/
    private TextView subText;
    private TextView status;
    private TextView timeText;
    private EditText clientIdEt;
    private Button connBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
        pubBtn = findViewById(R.id.pubBtn);


        pubText = findViewById(R.id.pubText);
        pubContent = findViewById(R.id.pubContent);
        subText = findViewById(R.id.subText);
        pubTopic = findViewById(R.id.pubTopic);


        pubMsg = findViewById(R.id.pubMsg);
        subMsg = findViewById(R.id.subMsg);*/
        status = findViewById(R.id.status);
        clientIdEt = findViewById(R.id.clientId);
        connBtn = findViewById(R.id.connBtn);
        subText = findViewById(R.id.subText);
        timeText = findViewById(R.id.timeText);

        connect();

        connBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topic = clientIdEt.getText().toString();
                if (topic.isEmpty()) {
                    topic = "test";
                }
                subscribeTopic(topic);

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
                    subText.setText("目前状态：lock");
                    timeText.setText("未确认");
//                    subscribeTopic("test");
//                    subscribeTopic("/device_online_status");
//                    subscribeTopic("/read-property");
//                    subscribeTopic("/report-property");
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
        char bind=msg.charAt(0);
        String lockText="";
        String time_Text="未设置，";
        if(bind=='0')
        {
            status.setText("绑定至"+msg.substring(1));
        }
        else if(bind=='1')
        {
            lockText="目前状态："+msg.substring(1);
            subText.setText(lockText);
        }
        else
        {
            time_Text="从"+msg.substring(1,3)+":"+msg.substring(3,5)+"解锁到"+msg.substring(5,7)+":"+msg.substring(7)+"锁住";
            timeText.setText(time_Text);
        }



    }

}