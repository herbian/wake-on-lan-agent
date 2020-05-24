package com.example.wolagent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.body.JSONObjectBody;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class MainActivity extends AppCompatActivity {

    private AsyncHttpServer server = new AsyncHttpServer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        server.post("/wakeup", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                try {
                    AsyncHttpRequestBody<JSONObject> body = request.getBody();
                    JSONObject object = body.get();
                    String password = object.getString("password");
                    if (password.equals(BuildConfig.WOL_PASSWORD)) {

                        wakeup("255.255.255.255", BuildConfig.WOL_MACADDRESS);

                        response.send("{\"status\": \"OK\"}");
                        return;
                    }
                    Log.d("RESULT", password);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                response.send("{\"status\": \"Failed\"}");
            }
        });

        // listen on port 5000
        server.listen(5000);

        try {
            Log.d("Server status", "SERVER STARTED");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void wakeup(String broadcastIP, String mac) {
        if (mac == null) {
            return;
        }

        try {
            byte[] macBytes = getMacBytes(mac);
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }

            InetAddress address = InetAddress.getByName(broadcastIP);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 5555);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static byte[] getMacBytes(String mac) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        byte[] bytes = new byte[6];
        if (mac.length() != 12)
        {
            throw new IllegalArgumentException("Invalid MAC address...");
        }
        try {
            String hex;
            for (int i = 0; i < 6; i++) {
                hex = mac.substring(i*2, i*2+2);
                bytes[i] = (byte) Integer.parseInt(hex, 16);
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit...");
        }
        return bytes;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        server.stop();
        Log.d("Server status", "SERVER STOPPED");
    }
}
