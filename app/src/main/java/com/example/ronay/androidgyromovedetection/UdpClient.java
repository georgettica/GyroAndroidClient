package com.example.ronay.androidgyromovedetection;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;


/**
 * Created by ronay on 4/29/2017.
 */
public class UdpClient {

    public float[] Message;

    private static final int ServerPort = 15170;
    private InetAddress BroadcastAddress;
    private AsyncTask<Void, Void, Void> async_client;
    private String last_ip = "";

    public UdpClient() throws UnknownHostException {
        BroadcastAddress = InetAddress.getByName("192.168.14.148");
    }

    public void updateIp(String ip) throws  UnknownHostException    {
        if (!ip.equals(last_ip)) {
            InetAddress tempAddress;
            AsyncTask<String, Void, InetAddress> task = new AsyncTask<String, Void, InetAddress>()
            {

                @Override
                protected InetAddress doInBackground(String... params)
                {
                    try
                    {
                        return InetAddress.getByName(params[0]);
                    }
                    catch (UnknownHostException e)
                    {
                        return null;
                    }
                }
            };
            try
            {
                tempAddress = task.execute(ip).get();
            }
            catch (InterruptedException e)
            {
                return;
            }
            catch (ExecutionException e)
            {
                return;
            }

            BroadcastAddress = tempAddress;
            last_ip = ip;
        }
    }

    @SuppressLint("NewApi")
    public void SendMessage() {
        async_client = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                DatagramSocket ds = null;

                try {
                    ds = new DatagramSocket();
                    DatagramPacket dp;
                    ByteBuffer bf = ByteBuffer.allocate(Message.length* 4);
                    for(int i=0; i< Message.length; i++)
                        bf.putFloat(Message[i]);

                    dp = new DatagramPacket(bf.array(), bf.array().length,  BroadcastAddress, ServerPort);
                    ds.setBroadcast(false);
                    ds.send(dp);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (ds != null) {
                        ds.close();
                    }
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        };

        if (Build.VERSION.SDK_INT >= 11)
            async_client.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else async_client.execute();
    }

    @SuppressLint("NewApi")
    public void SendCommand(final int command) {
        async_client = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                DatagramSocket ds = null;

                try {
                    ds = new DatagramSocket();
                    DatagramPacket dp;
                    ByteBuffer bf = ByteBuffer.allocate(4);
                    bf.putInt(command);

                    dp = new DatagramPacket(bf.array(), bf.array().length,  BroadcastAddress, ServerPort);
                    ds.setBroadcast(false);
                    ds.send(dp);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (ds != null) {
                        ds.close();
                    }
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        };

        if (Build.VERSION.SDK_INT >= 11)
            async_client.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else async_client.execute();
    }

}
