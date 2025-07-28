package com.example.canpay_operator.config;


import android.util.Log;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class HiveMqttManager {
    private final String serverHost = "mqtt-v1-canpay.sehanw.com";
//    private final String serverHost = "10.0.2.2:8081"; // Use HiveMQ public broker for testing
    private final int serverPort = 1883;
    private final String clientId = "canpay-app-client";
    private final String username = "IBzyXIGuXFVRBkjV";
    private final String password = "6azxiqAQ3iRl7FAuGJax0Kt8BEbEQoKL";
    private final String topic;

    private final Mqtt3AsyncClient client;

    private boolean isConnected = false;
    private Consumer<String> pendingSubscription = null;

    public HiveMqttManager(String busId) {
        this.topic = "bus/" + busId + "/payment";
        client = MqttClient.builder()
                .useMqttVersion3()
                .identifier(clientId)
                .serverHost(serverHost)
                .serverPort(serverPort)
                .buildAsync();
    }

    public void connect(Runnable onSuccess, Consumer<Throwable> onError) {
        client.connectWith()
                .simpleAuth()
                .username(username)
                .password(StandardCharsets.UTF_8.encode(password))
                .applySimpleAuth()
                .send()
                .whenComplete((ack, throwable) -> {
                    if (throwable != null) {
                        Log.e("MQTT", "Connection failed", throwable);
                        if (onError != null) onError.accept(throwable);
                    } else {
                        Log.d("MQTT", "Connected to HiveMQ");
                        isConnected = true;
                        if (pendingSubscription != null) {
                            Log.d("MQTT", "Performing pending subscription after connect");
                            subscribe(pendingSubscription);
                            pendingSubscription = null;
                        }
                        if (onSuccess != null) onSuccess.run();
                    }
                });
    }

    public void subscribe(Consumer<String> onMessage) {
        if (!isConnected) {
            Log.w("MQTT", "Not connected yet, deferring subscription");
            pendingSubscription = onMessage;
            return;
        }
        Log.d("MQTT", "Subscribing to topic: " + topic);
        client.subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(publish -> {
                    String payload = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                    Log.d("MQTT", "Message callback triggered for topic: " + publish.getTopic() + ", payload: " + payload);
                    if (onMessage != null) onMessage.accept(payload);
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        Log.e("MQTT", "Subscription failed", throwable);
                    } else {
                        Log.d("MQTT", "Subscription successful for topic: " + topic);
                    }
                });
    }

    public void publish(String message) {
        client.publishWith()
                .topic(topic)
                .payload(message.getBytes(StandardCharsets.UTF_8))
                .qos(MqttQos.AT_LEAST_ONCE)
                .send()
                .whenComplete((pub, throwable) -> {
                    if (throwable != null) {
                        Log.e("MQTT", "Publish failed", throwable);
                    } else {
                        Log.d("MQTT", "Message published");
                    }
                });
    }

    public void disconnect() {
        if (client.getState() == MqttClientState.CONNECTED) {
            client.disconnect();
        }
    }

    public boolean isConnected() {
        return client.getState() == MqttClientState.CONNECTED;
    }
}