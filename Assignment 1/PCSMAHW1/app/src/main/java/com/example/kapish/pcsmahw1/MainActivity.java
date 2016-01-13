package com.example.kapish.pcsmahw1;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //UI Elements
    private TextView xAxisTextView;
    private TextView yAxisTextView;
    private TextView zAxisTextView;
    private Button   startButton  ;
    private Button   stopButton   ;
    private Button   sendButton   ;

    //Sensors
    private SensorManager sensorManager;
    private long          lastUpdate   ;
    private String        xAxisValue   ;
    private String        yAxisValue   ;
    private String        zAxisValue   ;

   //file
    private String csvData             ;
            File   myFile              ;
    FileOutputStream  fileOutputStream ;

    //Variables - Sending files to Server
    ProgressDialog pDialog;
    private int flag      ;
    private String ipAddr ;
    private String portNo ;

    //Constants

    private static String IPADDR             = "192.168.57.26"                                                    ;
    private static String PORTNO             = "8097"                                                             ;
    private static String FILENAME           = "AccelerometerDataClientCopy.csv"                                  ;
    private static String DIALOGBOXTITLE     = "Server Details"                                                   ;
    private static String OK                 = "OK"                                                               ;
    private static String USEDEFAULT         = "USE DEFAULT"                                                      ;
    private static String IPADDRSTRING       = "Server IP Address"                                                ;
    private static String PORTNOSTRING       = "Server Port No"                                                   ;
    private static String SUCCESSFILESAVE    = "File saved successfully at Location:"                             ;
    private static String ERRORMESSAGEFILE   = "CSV Data file is not present"                                     ;
    private static String SUCCESSMESSAGEFILE = "CSV Data file transferred successfully on Server"                 ;
    private static String ERRORSERVERDETAILS = "Check Server Details entered"                                     ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize UI element
        xAxisTextView = (TextView) findViewById(R.id.dataX);
        yAxisTextView = (TextView) findViewById(R.id.dataY);
        zAxisTextView = (TextView) findViewById(R.id.dataZ);
        startButton   = (Button) findViewById(R.id.buttonStart);
        stopButton    = (Button) findViewById(R.id.buttonStop);
        sendButton    = (Button) findViewById(R.id.buttonSend);


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lastUpdate    = System.currentTimeMillis();


        String filename = FILENAME;
        myFile = new File(Environment
                .getExternalStorageDirectory(), filename);
        fileOutputStream = null;
        flag = 0;

        //set Default IP Addr and Port No of my own System
        ipAddr = IPADDR;
        portNo = PORTNO;

        // Setting On Click Listener of start Button and Registering Sensor Event Listener
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              //  Log.i("INFO",myFile.getAbsolutePath());
                if (myFile.exists()) {
                    myFile.delete();
                  //  Log.i("INFO","Deleting file");
                }
                try {
                    myFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                csvData = " TimeStamp ,X ,Y ,Z" + "\n";
                byte[] writeData = csvData.getBytes();
                try {
                  //  Log.i("INFO","I will write in file"+csvData);
                    fileOutputStream = new FileOutputStream(myFile);
                    fileOutputStream.write(writeData);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                // registering listener - accelerometer sensor
                sensorManager.registerListener(MainActivity.this,
                        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        SensorManager.SENSOR_DELAY_NORMAL);

            }
        });

        // Setting On Click Listener of stop Button and Un registering Sensor Event Listener
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // unregister listener   - not need to check whether sensorManager is registered. This is checked by Android itself
                sensorManager.unregisterListener(MainActivity.this);

                //Clear X,Y,Z Sensor Data from TextView
                xAxisTextView.setText(" ");
                yAxisTextView.setText(" ");
                zAxisTextView.setText(" ");


                try {
                    if(fileOutputStream != null) {
                        Toast.makeText(getApplicationContext(), SUCCESSFILESAVE+myFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                        fileOutputStream.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

        // Setting On Click Listener of Send Button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Alert Dialog Box to take Server IP Address and Port No
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

                alert.setTitle(DIALOGBOXTITLE);

                final LinearLayout layout = new LinearLayout(MainActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText inputIP = new EditText(MainActivity.this);
                inputIP.setHint(IPADDRSTRING);
                final EditText inputPort = new EditText(MainActivity.this);
                inputPort.setHint(PORTNOSTRING);


                layout.addView(inputIP);
                layout.addView(inputPort);
                alert.setView(layout);
                alert.setPositiveButton(OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        ipAddr = inputIP.getEditableText().toString();
                        portNo = inputPort.getEditableText().toString();
                     //   Log.i("INFO", ipAddr + Integer.parseInt(portNo));
                        if(ipAddr != null && portNo != null)
                        {
                            if(!ipAddr.isEmpty() && !portNo.isEmpty() && portNo.matches("[0-9]+"))
                            {
                                new SendFileToServer().execute();
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),ERRORSERVERDETAILS,Toast.LENGTH_LONG).show();
                            }
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),ERRORSERVERDETAILS,Toast.LENGTH_LONG).show();
                        }

                    }
                });

                alert.setNegativeButton(USEDEFAULT, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                        dialog.cancel();
                        new SendFileToServer().execute();
                    }
                });
                AlertDialog alertDialog = alert.create();
                alertDialog.show();

            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long actualTime = event.timestamp;
            if (actualTime - lastUpdate < 400) {
                return;
            }
            // Collecting Sensor Data if Delay is more than 400ms
            float []data = event.values;
            float x      = data[0]     ;
            float y      = data[1]     ;
            float z      = data[2]     ;
            xAxisValue   = "X: "+x;
            yAxisValue   = "Y: "+y;
            zAxisValue   = "Z: "+z;
            xAxisTextView.setText(xAxisValue);
            yAxisTextView.setText(yAxisValue);
            zAxisTextView.setText(zAxisValue);
            csvData=actualTime+" ,"+x+" ,"+y+" ,"+z+"\n";
            byte[] writeData = csvData.getBytes();
            try {
              //  Log.i("INFO","I will write in file"+csvData);
                fileOutputStream.write(writeData);

            } catch (IOException e) {
                e.printStackTrace();
            }

            //Updating lastUpdate Value
            lastUpdate   = actualTime;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    class SendFileToServer extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Sending file to Server..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            OutputStream os          = null;
            FileInputStream fis      = null;
            BufferedInputStream bis  = null;
            Socket client            = null;

            try {

                if(myFile.exists()) {
                    try {
                        int fileLength = (int) myFile.length();
                        byte[] accelerometerData = new byte[fileLength];

                            client = new Socket(ipAddr, Integer.parseInt(portNo));
                            fis = new FileInputStream(myFile);
                            bis = new BufferedInputStream(fis);
                            bis.read(accelerometerData, 0, fileLength);
                            os = client.getOutputStream();
                            os.write(accelerometerData, 0, fileLength);
                            os.flush();
                            flag = 1;

                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                    finally {
                        if(fis != null)
                         fis.close();
                        if (bis != null)
                         bis.close();
                        if(os != null)
                         os.close();
                        if(client != null)
                         client.close();
                    }
                }
                else
                {
                    flag = 0;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            pDialog.dismiss();
          //  Log.i("INFO", "Written on Input Stream");

            if(flag == 0)
            {
                Toast.makeText(getApplicationContext(),ERRORMESSAGEFILE,Toast.LENGTH_LONG).show();
            }
            else
            {
              //  Log.i("INFO","File Length is "+(int) myFile.length());
                Toast.makeText(getApplicationContext(),SUCCESSMESSAGEFILE,Toast.LENGTH_LONG).show();
            }

        }
    }
}


