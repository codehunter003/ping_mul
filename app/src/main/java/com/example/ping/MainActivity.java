package com.example.ping;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    EditText editTextMessage, editTextIPAddress;
    Button buttonSend, buttonStartListening;
    TextView textViewMessages;
    int ping_port = 55555;

    ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextMessage = findViewById(R.id.editTextMessage);
        editTextIPAddress = findViewById(R.id.editTextIpAddress);
        buttonSend = findViewById(R.id.buttonSend);
        buttonStartListening = findViewById(R.id.buttonStartListening);
        textViewMessages = findViewById(R.id.textViewMessages);

        buttonSend.setOnClickListener(v -> sendMessage());
        buttonStartListening.setOnClickListener(v -> startListening());
    }

    private void sendMessage() {
        String message = editTextMessage.getText().toString();
        String ipAddress = editTextIPAddress.getText().toString();

        executorService.execute(() -> {
            try {
                InetAddress address = InetAddress.getByName(ipAddress);
                DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), address, ping_port);
                DatagramSocket datagramSocket = new DatagramSocket();
                datagramSocket.send(packet);
                datagramSocket.close();
                runOnUiThread(() -> textViewMessages.append("Sent: " + message + "\n"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void startListening() {
        executorService.execute(() -> {
            try {
                DatagramSocket datagramSocket = new DatagramSocket(ping_port);
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                String deviceIpAddress = InetAddress.getLocalHost().getHostAddress(); // Get device's IP address

                while (!Thread.currentThread().isInterrupted()) {
                    datagramSocket.receive(packet);
                    String senderIpAddress = packet.getAddress().getHostAddress(); // Get sender's IP address

                    System.out.println(deviceIpAddress + "..." + senderIpAddress);

                    if (!senderIpAddress.equals(deviceIpAddress)) { // Check if sender's IP is not the same as device's IP
                        String msg = new String(packet.getData(), 0, packet.getLength());
                        runOnUiThread(() -> textViewMessages.append("Received from " + senderIpAddress + ": " + msg + "\n"));
                    }
                }

                datagramSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }
}
