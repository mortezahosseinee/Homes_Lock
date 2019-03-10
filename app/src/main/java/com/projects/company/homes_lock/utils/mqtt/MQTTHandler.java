package com.projects.company.homes_lock.utils.mqtt;

import android.content.Context;
import android.util.Log;

import com.projects.company.homes_lock.models.datamodels.mqtt.FailureModel;
import com.projects.company.homes_lock.models.datamodels.mqtt.MessageModel;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MQTTHandler {
    private MqttAndroidClient client;
    private static String TAG = "MQTTHandler Class";

    public void setup(final IMQTTListener mIMQTTListener, Context mContext, String deviceObjectId) {
        client = new MqttAndroidClient(
                mContext,
                "tcp://185.208.175.56",
                deviceObjectId);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                mIMQTTListener.onConnectionToBrokerLost(new FailureModel(cause.getMessage()));
                Log.w(TAG, "Connection to broker Lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                try {
                    mIMQTTListener.onMessageArrived(new MessageModel(topic, message));
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                mIMQTTListener.onDeliveryMessageComplete(token);
                Log.i(TAG, "Receiving Message Completed.");
            }
        });

        connect(mIMQTTListener);
    }

    private void connect(final IMQTTListener mIMQTTListener) {
        try {
            MqttConnectOptions mMqttOptions = new MqttConnectOptions();
            mMqttOptions.setAutomaticReconnect(true);
            mMqttOptions.setCleanSession(false);
            mMqttOptions.setKeepAliveInterval(10);
            if (client != null)
                client.connect(mMqttOptions).setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        if (mIMQTTListener != null)
                            mIMQTTListener.onConnectionSuccessful(asyncActionToken);
                        Log.i(TAG, "Connection to broker Success.");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        // Something went wrong e.g. connection timeout or firewall problems
                        if (mIMQTTListener != null)
                            mIMQTTListener.onConnectionFailure(new FailureModel(exception.getMessage()));
                        Log.e(TAG, "Connection to broker Failed");
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void subscribe(final IMQTTListener mIMQTTListener) {
        try {
            if (client != null) {
                IMqttToken subToken = client.subscribe("response", 0);

                subToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        if (mIMQTTListener != null)
                            mIMQTTListener.onSubscribeSuccessful(asyncActionToken);
                        Log.i(TAG, "Subscribing Done.");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        if (mIMQTTListener != null)
                            mIMQTTListener.onSubscribeFailure(new FailureModel(exception.getMessage()));
                        Log.e(TAG, "Subscribe Failed.");
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void toggle(final IMQTTListener mIMQTTListener, String mLockSerialNumber, byte[] command) {
        IMqttDeliveryToken publishToken = null;

        try {
            if (client != null)
                publishToken = client.publish("cmd", new MqttMessage(command));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        if (publishToken != null)
            publishToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    if (mIMQTTListener != null)
                        mIMQTTListener.onPublishSuccessful(asyncActionToken);
                    Log.i(TAG, "Publishing Done.");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    if (mIMQTTListener != null)
                        mIMQTTListener.onPublishFailure(new FailureModel(exception.getMessage()));
                    Log.e(TAG, "Publishing Failed.");
                }
            });
        else
            Log.e(TAG, "publishToken is null.");
    }

    public void disconnect() {
        if (client != null) {
            try {
                client.disconnectForcibly();
                client.close();
                client = null;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}
