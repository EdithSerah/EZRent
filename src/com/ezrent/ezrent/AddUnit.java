/*
 * This activity is for a landlord to add a unit (a single apartment) to a property.
 */

package com.ezrent.ezrent;
 
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
 
public class AddUnit extends Activity {
       
        EditText unit_numberTV;
        EditText start_dateTV;
        EditText end_dateTV;
        EditText monthly_rentTV;
        EditText tenant_emailTV;
       
        String unit_number;
        String start_date;
        String end_date;
        String monthly_rent;
        String tenant_email;
        
        String email;
        String pass;
        String pname;
        
        Button add_unit;
        Button cancel;
 
        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
        	super.onCreate(savedInstanceState);
            setContentView(R.layout.add_unit);
                
            add_unit = (Button) findViewById(R.id.unit_add_add_Button);
            cancel = (Button) findViewById(R.id.unit_add_cancel_Button);
                
            add_unit.setOnClickListener(addListener);
            cancel.setOnClickListener(cancelListener);
                
            unit_numberTV = (EditText) findViewById(R.id.unit_number_EditText);
            start_dateTV = (EditText) findViewById(R.id.unit_start_date_EditText);
            end_dateTV = (EditText) findViewById(R.id.unit_end_date_EditText);
            monthly_rentTV = (EditText) findViewById(R.id.unit_rent_EditText);
            tenant_emailTV = (EditText) findViewById(R.id.unit_tenant_email_EditText);
            
            // get landlord and property information for database
            email = getIntent().getExtras().getString("EMAIL");
        	pass = getIntent().getExtras().getString("PASSWORD");
        	pname = getIntent().getExtras().getString("PROPERTY_NAME");
        }
        
       public OnClickListener addListener = new OnClickListener()
    	{

			@Override
			public void onClick(View v)
			{
				if(monthly_rentTV.getText().length() > 0 && 
						tenant_emailTV.getText().length() > 0 && 
						unit_numberTV.getText().length() > 0 && 
						start_dateTV.getText().length() > 0 && 
						end_dateTV.getText().length() > 0)
				{
					unit_number = unit_numberTV.getText().toString();
	                start_date = start_dateTV.getText().toString();
	                end_date = end_dateTV.getText().toString();
					monthly_rent = monthly_rentTV.getText().toString();
					// emails are case insensitive so they are automatically converted to lower case
					tenant_email = tenant_emailTV.getText().toString().toLowerCase();
					// task to add unit information to database
					new addLease().execute(start_date, end_date, monthly_rent, unit_number, pname, tenant_email);
					
				}
				else
				{
					errorMsg (R.string.add_unit_error, R.string.error_fields_left_blank);
				}
			}
        	
    	};
    	
    	public OnClickListener cancelListener = new OnClickListener()
    	{

			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(AddUnit.this, PropertyPage.class);
				intent.putExtra("EMAIL", email);
				intent.putExtra("PASSWORD",pass);
				intent.putExtra("PROPERTY_NAME",pname);
				startActivity(intent);
				
			}
        	
    	};
 
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
                // Inflate the menu; this adds items to the action bar if it is present.
                getMenuInflater().inflate(R.menu.add_unit, menu);
                return true;
        }
 
        private class addLease extends AsyncTask<String, String, Integer>
        {
    		
    		private static final String LOGIN_URL = "http://www.ezrentdata.com/webservice/index.php";
    		
    		private ProgressDialog pDialog;
    		private String request = "addLease";
    		
    		private JSONParser jsonParser = new JSONParser();
    		
    		@Override
    		protected void onPreExecute() {
    			super.onPreExecute();
    			pDialog = new ProgressDialog(AddUnit.this);
    			pDialog.setMessage("Adding Lease...");
    			pDialog.setIndeterminate(false);
    			pDialog.setCancelable(true);
    			pDialog.show();
    		}
    		
    		@Override
    		protected Integer doInBackground(String... args) {
    			 

    			List<NameValuePair> params = new ArrayList<NameValuePair>();
    			params.add(new BasicNameValuePair("email", email));
    			params.add(new BasicNameValuePair("password", pass));
    			params.add(new BasicNameValuePair("request", request));
    			params.add(new BasicNameValuePair("sdate", args[0]));
    			params.add(new BasicNameValuePair("edate", args[1]));
    			params.add(new BasicNameValuePair("rent", args[2]));
    			params.add(new BasicNameValuePair("apno", args[3]));
    			params.add(new BasicNameValuePair("pname", args[4]));
    			params.add(new BasicNameValuePair("temail", args[5]));
    		
    			try
    			{
    			Log.d("addLease Attempt", "started");
    			JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);
    			Log.d("addLease response", json.toString());
    			
    				return json.getInt("success");
    			}
    			catch (JSONException e)
    			{
    		 	e.printStackTrace();
    			}
    			return 0;
    	     }


    		@Override
    	     protected void onPostExecute(Integer message) {
    			pDialog.dismiss();
    			
    			if(message == 1)
    			{
    				Toast.makeText(AddUnit.this, "Lease Created.", Toast.LENGTH_LONG).show();
    				Intent intent = new Intent(AddUnit.this, PropertyPage.class);
					intent.putExtra("EMAIL", email);
					intent.putExtra("PASSWORD",pass);
					intent.putExtra("PROPERTY_NAME",pname);
					startActivity(intent);
    			}
    			else if(message == 2)
    				Toast.makeText(AddUnit.this, "Cannot make lease. Specified Tenant does not exist.", Toast.LENGTH_LONG).show();
    			else if(message == 3)
    				Toast.makeText(AddUnit.this, "Cannot make lease. Specified Landlord does not exist.", Toast.LENGTH_LONG).show();
    			else if(message == 4)
    				Toast.makeText(AddUnit.this, "Cannot make lease. Specified Property does not exist.", Toast.LENGTH_LONG).show();
    			else if(message == 5)
    				Toast.makeText(AddUnit.this, "Cannot make lease. This Lease is already made.", Toast.LENGTH_LONG).show();
    			else if(message == 0)
    				Toast.makeText(AddUnit.this, "Cannot make lease. Fields left blank.", Toast.LENGTH_LONG).show();
    			else
    				Toast.makeText(AddUnit.this, "Cannot make lease. Please input YYYY-MM-DD format for dates.", Toast.LENGTH_LONG).show();;
    			
    	     }
    	 }
        
        public void errorMsg (int title, int msg)
    	{
    		// Create an alert dialog box
    		AlertDialog.Builder builder = new AlertDialog.Builder(AddUnit.this);
    		
    		// Set alert title 
    		builder.setTitle(title);
    		
    		// Set the value for the positive reaction from the user
    		// You can also set a listener to call when it is pressed
    		builder.setPositiveButton(R.string.ok, null);
    		
    		// The message
    		builder.setMessage(msg);
    		
    		// Create the alert dialog and display it
    		AlertDialog theAlertDialog = builder.create();
    		theAlertDialog.show();
    	};
        
}