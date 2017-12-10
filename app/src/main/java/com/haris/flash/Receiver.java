package com.haris.flash;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Receiver extends AppCompatActivity {
    Button Start;
    Button Receive;
    ServerSocket sersock;
    Socket sock;
    String FILEPATH;
    String FILESIZE;
    String recvmsg;
    TextView msg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);


        // hiding stuff okay
        View decorView = getWindow().getDecorView();    //hidid status bar ..
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        //hide actionbar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();



        Start=(Button)findViewById(R.id.button2);
        Receive=(Button)findViewById(R.id.button);

        //lets switch on the hotspot
        Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    WifiConfiguration netConfig = new WifiConfiguration();



//wifi hotpsot RECIvevrr should make one.


                    netConfig.SSID = "MytestAP"; //later we can use regex
                    netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                    netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);



                    WifiManager wifiManager = (WifiManager)getBaseContext().getSystemService(Context.WIFI_SERVICE);
                    Method setWifiApMethod = wifiManager.getClass().getMethod("setWifiApEnabled",  WifiConfiguration.class, boolean.class);
                    setWifiApMethod.invoke(wifiManager, netConfig,false);
                    setWifiApMethod = wifiManager.getClass().getMethod("setWifiApEnabled",  WifiConfiguration.class, boolean.class);
                    setWifiApMethod.invoke(wifiManager, netConfig,true);

//                    wifiConfig.preSharedKey="1234567890";

                    // wifiConfig.SSID="HARIS";



                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e + "", Toast.LENGTH_LONG).show();
                }
            }
        });


        Receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //rCv metadata liek filesiz
                MetaReceiver metaReceiver=new MetaReceiver();
                metaReceiver.execute();


            }
        });

    }
    private class MetaReceiver extends AsyncTask<String, Void, String> {
        @Override
        public String doInBackground(String[] params) {
            try {
                sersock=new ServerSocket(4858);
                sock=sersock.accept();
                InputStream istream=sock.getInputStream();
                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(istream));
                FILEPATH=bufferedReader.readLine();
                FILESIZE=bufferedReader.readLine();

                recvmsg=FILESIZE+" "+FILEPATH;
                sersock.close();
                sock.close();
                bufferedReader.close();
                istream.close();

            }
            catch(Exception e)
            {
                recvmsg="NOT SENT"+e;

            }
            return null;
        }
        @Override
        protected void onPostExecute(String message) {
            FileReceiver fileReceiver=new FileReceiver();
            fileReceiver.execute();
        }



    }
    private class FileReceiver extends AsyncTask<String, Void, String> {
        @Override
        public String doInBackground(String[] params) {
            try {
                sersock=new ServerSocket(4859);
                sock=sersock.accept();

                saveFile(sock);

            }
            catch(Exception e)
            {
                recvmsg="NOT SENT"+e;

            }
            return null;
        }
        @Override
        protected void onPostExecute(String message) {

            Toast.makeText(getApplicationContext(),""+FILEPATH,Toast.LENGTH_LONG).show();

        }
        private void saveFile(Socket clientSock) throws IOException {

            File folder = new File(Environment.getExternalStorageDirectory() + "/FlashApp");
            if (!folder.exists()) {
                folder.mkdir();
            }


            DataInputStream dis = new DataInputStream(clientSock.getInputStream());
            String path=FILEPATH;
            String[] pathsplitted=path.split("/");
            String realFileName=pathsplitted[pathsplitted.length-1]; //last word is filename chuck the path

            File file = new File(Environment.getExternalStorageDirectory()+"/FlashApp/"+realFileName); //chnage it niw

            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[4096];


            //System.out.println(buffer);
            int filesize = Integer.parseInt(FILESIZE); // we shud sned file size in separate msg
            int read = 0;
            int totalRead = 0;

            int remaining = filesize;
            while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                totalRead += read;
                remaining -= read;
                //	System.out.println("read " + totalRead + " bytes.");
                fos.write(buffer, 0, read);
            }



            fos.close();
            dis.close();
            sersock.close();
            sock.close();

        }
    }

}
