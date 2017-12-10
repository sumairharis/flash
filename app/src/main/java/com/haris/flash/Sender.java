package com.haris.flash;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.List;


public class Sender extends AppCompatActivity {
    ImageButton select;
    ImageButton upload;
    Button connect;
    String FILEPATH;
    String FILESIZE;
    Socket sock;
    String recvmsg;
    ProgressBar progressBar;
    Handler handler;
    int progress;
    int time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);

        // hiding stuff okay
        View decorView = getWindow().getDecorView();    //hidid status bar ..
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        //hide actionbar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();


        select=(ImageButton)findViewById(R.id.imageButton8);
        upload=(ImageButton)findViewById(R.id.imageButton7);
        connect=(Button)findViewById(R.id.button3);

        progressBar=(ProgressBar) findViewById(R.id.progressBar);
        progress=0;
        /* some math time  */
        //typical speed is 3-4MBps Cpaital B okay ;P
        // time=FIlesize/4194304     4 mega Bytes
        // update +1 every t/100 seconds remember postdelayed uses millisecodns



        //select onee file fornow affan
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("*/*");
                startActivityForResult(Intent.createChooser(i, "File Upload"), 1);

                //onactivtyresult method later in class
            }
        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                try {
                    WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                    wifiManager.setWifiEnabled(false);

                    wifiManager.setWifiEnabled(true);


                    String networkSSID = "MytestAP";

                    WifiConfiguration conf = new WifiConfiguration();
                    conf.SSID = "\"" + networkSSID + "\"";

                    conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
                    wifiManager.addNetwork(conf);

                    List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                    for( WifiConfiguration i : list ) {
                        if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                            wifiManager.disconnect();
                            wifiManager.enableNetwork(i.networkId, true);
                            wifiManager.reconnect();

                            break;
                        }
                    }

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e + "", Toast.LENGTH_LONG).show();
                }


            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // we cant run socket programmming in main thread its forbidden in Android 3+

                //lets send metadata like filename and size
                // the reciever must know filesize hence we are sending File size before only



                FileMetaSender fileMetaSender=new FileMetaSender();
                fileMetaSender.execute();
                time=Integer.parseInt(FILESIZE)/3894304+1;


                //try to implement progress okay


                handler=new Handler();
                handler.postDelayed(progresser,0);
                Toast.makeText(getApplicationContext(),"post should work!"+Toast.LENGTH_SHORT,Toast.LENGTH_SHORT).show();


            }
        });





    }
    private Runnable progresser=new Runnable() {
        @Override
        public void run() {
            progress+=1;
            progressBar.setProgress(progress);
          if(progress<100)
            handler.postDelayed(progresser,time*1000/100);

        }
    };
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        FILEPATH = null;
        Uri uri = data.getData();

        try {

            FILEPATH=getPath(uri);
            File file=new File(FILEPATH);  //opent o know its filesize
            FILESIZE=file.length()+"";   //convert to string

        }catch(Exception e) {
            Toast.makeText(getApplicationContext(), FILEPATH+""+e, Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    // COntent uri path different from real path very messy
    public String getPath(Uri uri)
    {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index =  cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        cursor.close();
        return s;
    }

    private class FileMetaSender extends AsyncTask<String, Void, String> {
        @Override
        public String doInBackground(String[] params) {
            try {
                sock=new Socket("192.168.43.1",4858);
                OutputStream ostream=sock.getOutputStream();
                PrintWriter printWriter=new PrintWriter(ostream,true);
                printWriter.println(FILEPATH);
                printWriter.println(FILESIZE);
                printWriter.close();
                ostream.close();

                sock.close();

            } catch (Exception e) {
                recvmsg = "NOT SENT" + e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String message) {
            Toast.makeText(getApplicationContext(),""+FILEPATH,Toast.LENGTH_LONG).show();


            FileSender fileSender=new FileSender();
            fileSender.execute();

        }


    }


    private class FileSender extends AsyncTask<String, Void, String> {
        @Override
        public String doInBackground(String[] params) {
            try {

                sock=new Socket("192.168.43.1",4859);

                sendFile(FILEPATH);
            } catch (Exception e) {
                recvmsg = "NOT SENT" + e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String message) {
            Toast.makeText(getApplicationContext(),""+FILEPATH+" SENT"+FILESIZE,Toast.LENGTH_LONG).show();
        }


        public void sendFile(String file) throws IOException {
            File myfile=new File(file);
            DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
            FileInputStream fis = new FileInputStream(myfile);
            byte[] buffer = new byte[4096];


            while (fis.read(buffer) > 0) {
                dos.write(buffer);
            }

            fis.close();
            dos.close();
            sock.close();
        }
    }


}
