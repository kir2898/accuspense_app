package com.kirschnertech.accuspense;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by John on 4/18/2016.
 */
public class Restock extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dispense);
        //set up all of the edit text fields to retrieve information from them
        final EditText ipEditText = (EditText) findViewById(R.id.restock_ip);
        final EditText userText = (EditText) findViewById(R.id.restock_user);
        final EditText pwdText = (EditText) findViewById(R.id.restock_pwd);
        final EditText medText = (EditText) findViewById(R.id.restock_med);
        final EditText amountText = (EditText) findViewById(R.id.restock_amount);
        final EditText weightText = (EditText) findViewById(R.id.restock_weight);


        //set up the button and button listener
        final Button connectButton = (Button) findViewById(R.id.dispense_connect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                //perform action on click
                String command = "o";
                String user = userText.getText().toString();        //get the name string
                String ipAddr = ipEditText.getText().toString();    //get the IP address string
                String pwd = pwdText.getText().toString();          //get the password
                String med = medText.getText().toString();          //get the medication
                String amount = amountText.getText().toString();    //get the amount
                String weight = weightText.getText().toString();    //get the weight
                String args[] = {ipAddr,command,user,pwd,med,weight,amount};
                RestockAttemptConnection attemptConn = new RestockAttemptConnection();        //create a new attemptconnection class
                attemptConn.execute(args);          //execute the asyncTask
            }
        });
        //setup the toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

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
        if (id == R.id.search_patient_menu) {
            //the search patient button was clicked
            Intent intent = new Intent(Restock.this, MainActivity.class);
            startActivity(intent);
            return true;
        }else if(id == R.id.restock_drawer_menu){
            //the restock drawer button was clicked
            Intent intent = new Intent(Restock.this, Restock.class);
            startActivity(intent);
            return true;
        }else if(id== R.id.dispense_drawer_menu){
            //the dispense drawer button was clicked
            Intent intent = new Intent(Restock.this, Dispense.class);
            startActivity(intent);
            return true;
        }else if(id == R.id.add_patient_menu){
            //the add patient button was clicked
            Intent intent = new Intent(Restock.this, AddPatient.class);
            startActivity(intent);
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
    public class RestockAttemptConnection extends AsyncTask<String,Void,String>
    {
        final static String LOG_TAG = "dispense_Async";

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
            //{ipAddr,command,user,pwd,med,weight,amount};
            String ipAddr = params[0].toString();           //get the ip address from the parameters
            String command = "r";			//get the command to send to the server
            String user = params[2].toString();	            //get the user
            String pwd = params[3].toString();              //get the password
            String med = params[4].toString();              //get the medication
            String weight = params[5].toString();           //get the weight
            String amount = params[6].toString();           //get the amount
            Log.v(LOG_TAG, "ipAddr: " + ipAddr);
            Log.v(LOG_TAG, "command: "+command);
            Log.v(LOG_TAG, "user: "+user);
            Log.v(LOG_TAG, "pwd: "+pwd);
            Log.v(LOG_TAG, "med: "+med);
            Log.v(LOG_TAG, "weight: "+weight);
            Log.v(LOG_TAG, "amount: "+amount);
            //create the message to send to the server
            String msg = command+' '+user+' '+pwd+' '+med+' '+weight+' '+amount+'\0';
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
            String buffer = msg;        //already created it above doing this to conform to
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
            Log.v(LOG_TAG, "return: " +ret);
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
