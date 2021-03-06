package me.muphy.android.mqtt.demo;

import android.annotation.SuppressLint;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
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

import java.sql.SQLException;

import me.muphy.android.mqtt.demo.MySQL.dao.LockDao;
import me.muphy.android.mqtt.demo.MySQL.enity.Lock;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = MainActivity.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private static MqttAndroidClient mqttAndroidClient;

    private TextView subText;
    private TextView status;
    private EditText clientIdEt;
    private Button connBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);

        status = findViewById(R.id.status);
        clientIdEt = findViewById(R.id.clientId);
        connBtn = findViewById(R.id.connBtn);
        subText = findViewById(R.id.subText);
        //timeText = findViewById(R.id.timeText);

        connect();

        connBtn.setOnClickListener(v -> {
            String topic = clientIdEt.getText().toString();
            Lock lock = new Lock();
            LockDao lockDao = new LockDao();
            try {
                lock=lockDao.getinfo(topic);
            } catch (
                    SQLException throwables) {
                throwables.printStackTrace();
            }
            status.setText(lock.getUsername());
            subText.setText(judgestate(String.valueOf(lock.getState())));
            if (clientIdEt.isEnabled()) {
                if (topic.isEmpty()) {
                    topic = "test";
                    clientIdEt.setText(topic);
                }
                clientIdEt.setEnabled(false);
                clientIdEt.setTextColor(Color.rgb(128,128, 128));
                connBtn.setText("??????");
                subscribeTopic(topic);
            } else {
                clientIdEt.setEnabled(true);
                clientIdEt.setTextColor(Color.rgb(0,0, 0));
                connBtn.setText("??????");
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

        /* ??????MqttConnectOptions??????????????????username???password??? */
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();

        mqttConnectOptions.setUserName(userName);


        mqttConnectOptions.setPassword(passWord.toCharArray());


        if (mqttAndroidClient != null) {
            try {
                mqttAndroidClient.disconnect(); //????????????
            } catch (MqttException e) {
                e.printStackTrace();
            }
            mqttAndroidClient = null;
        }

        /* ??????MqttAndroidClient????????????????????????????????? */
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), host, clientId);
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                String msg = "";
                if (cause != null) {
                    msg = "," + cause.getMessage();
                }
                Log.i(TAG, "????????????>host???" + host + msg);
                status.setText("??????????????????");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                Log.i(TAG, "topic: " + topic + ", msg: " + new String(message.getPayload()));


                statusChange(new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "?????????");
            }
        });



        /* ??????MQTT????????? */
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "???????????????" + host);

                    status.setText("?????????" );
                    subText.setText("lock");

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    String msg = "";
                    if (exception != null) {
                        msg = "," + exception.getMessage();
                    }
                    Log.i(TAG, "????????????>host???" + host + msg);
                    status.setText("?????????????????????" );
                    assert exception != null;
                    exception.printStackTrace();
                }
            });

        } catch (MqttException e) {
            Log.i(TAG, "???????????????" + e.getMessage());
            status.setText("?????????" );
            e.printStackTrace();
        }
    }



    public void subscribeTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "????????????>topic:" + topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "????????????>topic:" + topic);
                }
            });

        } catch (MqttException e) {
            Log.i(TAG, "????????????>topic:" + topic);
        }
    }

    public void unsubscribeTopic(String topic) {
        try {
            mqttAndroidClient.unsubscribe(topic);

        } catch (MqttException e) {
            Log.i(TAG, "????????????>topic:" + topic);
        }
    }

    @Override
    public void onDestroy() {
        if (mqttAndroidClient != null) {
            try {
                mqttAndroidClient.disconnect(); //????????????
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
   //     String time_Text;
        if(bind=='0')
        {
            new Thread(() -> {

                clientIdEt.findViewById(R.id.clientId);
                boolean aa = lockDao.login(Integer.parseInt(clientIdEt.getText().toString()),msg.substring(1), Integer.parseInt(judgestate(subText.getText().toString())));
                if(aa){
                    hand1.sendEmptyMessage(1);
                }else {
                    hand1.sendEmptyMessage(0);
                }
            }).start();
            status.setText("?????????"+msg.substring(1));
        }else if(bind=='1')
        {
            lockText=msg.substring(1);
            new Thread(() -> {
                clientIdEt.findViewById(R.id.clientId);
                lockDao.update(Integer.parseInt(clientIdEt.getText().toString()), Integer.parseInt(judgestate(lockText)));
            }).start();

            subText.setText(lockText);
        }




    }
    public String judgestate(String l){
        if(l.equals("lock"))
        {
            return "1";
        }else if (l.equals("unlock")){
            return "0";
        }else if (l.equals("0")){
            return "lock";
        }else {
            return "unlock";
        }
    }

    @SuppressLint("HandlerLeak")
    final Handler hand1 = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(Message msg) {

            if(msg.what == 1)
            {
                Toast.makeText(getApplicationContext(),"????????????",Toast.LENGTH_LONG).show();

            }
            else if(msg.what == 0)
            {
                Toast.makeText(getApplicationContext(),"????????????",Toast.LENGTH_LONG).show();
            }

        }
    };

}