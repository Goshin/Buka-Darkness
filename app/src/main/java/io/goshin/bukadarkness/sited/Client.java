package io.goshin.bukadarkness.sited;

import android.os.Message;

import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;

public class Client {
    public static String request(JSONObject params) throws IOException {
        if (!Server.isOnDuty()) {
            throw new ConnectException();
        }
        Message message = Message.obtain();
        Packet packet = new Packet(params);
        message.obj = packet;
        message.setTarget(Server.getProcessHandler());
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (packet) {
            message.sendToTarget();
            try {
                while (packet.getStatus() == Packet.Status.PENDING) {
                    packet.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String response = packet.getResponse();
        if (response == null || response.isEmpty()) {
            throw new IOException("没有数据返回，该订阅源可能暂时不可用");
        }
        return response;
    }
}
