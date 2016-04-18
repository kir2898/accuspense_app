package com.kirschnertech.accuspense;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import java.math.BigInteger;
import java.net.*;
import java.io.*;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {
    //class global variables
    final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_patient);
        //set up all of the edit text fields to retrieve information from them
        final EditText ipEditText = (EditText) findViewById(R.id.search_patient_ip);
        final EditText nameEditText = (EditText) findViewById(R.id.search_patient_full_name);
        final EditText phoneEditText = (EditText) findViewById(R.id.search_patient_phone);

        //set up the button and button listener
        final Button connectButton = (Button) findViewById(R.id.search_patient_connect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                //perform action on click
                String command = "s";
                String name = nameEditText.getText().toString();            //get the name string
                String ipAddr = ipEditText.getText().toString();            //get the IP address string
                //String phoneNumber = phoneEditText.getText().toString();
                String args[] = {command,name,ipAddr};
                AttemptConnection attemptConn = new AttemptConnection();        //create a new attemptconnection class
                attemptConn.execute(args);          //execute the asyncTask
            }
        });

        //setup the toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * AttemptConnection
     * class that will attempt to connect to a server that is specified in the current working
     * layout that has an ip address specified
     * currently works with search_patient.xml
     */
    public class AttemptConnection extends AsyncTask<String,Void,String>
    {

        /**
         * doInBackground
         * This function will attempt to make a connection in the background, currently works with
         * the search_patient.xml file to search for a patient on the database
         * @paramater param params[0] command to send to server
         *                  params[1] command data to be sent to the server
         *                  params[2] ip address of the server to connect to
         * @return the response from the server
         */
        @Override
        protected String doInBackground(String... params){
            //get the ip address to connect to
            String ipAddr = params[2].toString();           //get the ip address from the parameters
            String command = params[0].toString();			//get the command to send to the server
            String message = params[1].toString();	        //get the paramter that gets sent after the command
            Log.v(LOG_TAG, "ipAddr: "+ipAddr);
            Log.v(LOG_TAG, "command: "+command);
            Log.v(LOG_TAG, "message: "+message);
            String ret;         //return of the doInBackground
            //create the socket
            Socket sock;
            try {
                sock = new Socket();
                sock.connect(new InetSocketAddress(ipAddr, 4404),3000);     //connect using a 3 second timeout
            }catch (UnknownHostException e){
                ret = "UnknownHostException";
                Log.e(LOG_TAG, "Error creating socket: "+ret);
                return ret;
            }catch (IOException e){
                ret = "IOException";
                Log.e(LOG_TAG, "Error creating socket: "+ret);
                return ret;
            }
            Log.v(LOG_TAG,"Created socket");
            PrintStream pStream;
            try {
                pStream = new PrintStream(sock.getOutputStream());
            }catch (IOException e){
                ret = "IOException";
                Log.e(LOG_TAG, "Error creating PrintStream "+ret);
                return ret;
            }

            //create the message to send to the host
            String buffer = command+" "+message+" "+'\0';	//concat everything together
            Log.v(LOG_TAG, "Sending command to client: "+buffer);
            pStream.print(buffer);			//send the message to the server

            //receive the response back
            BufferedReader clientIn;
            try {
                clientIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            }catch (IOException e){
                ret = "IOException";
                Log.e(LOG_TAG,"Error creating BufferedReader: "+ret);
                return ret;
            }
            try{
                ret = clientIn.readLine();		//get the response from the server
            } catch (IOException e){
                ret = "IOException";
                Log.e(LOG_TAG, "Error reading in from socket: "+ret);
                return ret;
            }
            try{
                sock.close();       //close the socket
            } catch(IOException e){
                ret = "IOException";
                Log.e(LOG_TAG, "Error closing socket: "+ret);
            }
            Log.v(LOG_TAG, "return: "+ret);
            return ret;
        }

        /**
         * onPostExecute
         * the function that gets called after doInBackground finishes processing
         * This will update the response.xml file to display data from the requested response
         * currently only works with the search patient command
         * @parameter param this parameter is passed in from the doInBackground method. This is the
         *      response from the server. And contains the information that will be written to the
         *      xml file.
         */
        @Override
        public void onPostExecute(String param){
            //we need to take the param and give it to the search_patient_response.xml page
            /*LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.search_patient_response,null);*/
            setContentView(R.layout.search_patient_response);
            final TextView responseTextView = (TextView) findViewById(R.id.search_patient_response);
            responseTextView.setText(param);
        }
    }
}
