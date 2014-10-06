/*
 * This activity is for a landlord to add properties to their account.
 * Properties can contain numerous units.
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

public class AddProperty extends Activity {
	
	EditText name;
	EditText numUnits;	// number of units
	EditText address;
	EditText state;
	EditText city;
	EditText zip;
	
	Button cancel;
	Button add;
	
	private String email;
	private String pass;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_property);
		
		// get users ID information for querying the database 
		email = getIntent().getExtras().getString("EMAIL");
		pass = getIntent().getExtras().getString("PASSWORD");
		
		name = (EditText) findViewById(R.id.add_property_name_EditText);
		address = (EditText) findViewById(R.id.add_property_address_EditText);
		state = (EditText) findViewById(R.id.add_property_state_EditText);
		city = (EditText) findViewById(R.id.add_property_city_EditText);
		zip = (EditText) findViewById(R.id.add_property_zip_EditText);
		
		cancel = (Button) findViewById(R.id.add_property_cancel_Button);
		add = (Button) findViewById(R.id.add_property_Button);
		
		
		add.setOnClickListener(addButtonListener);
		cancel.setOnClickListener(cancelButtonListener);
	}
	
	public OnClickListener addButtonListener = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			// checks to make sure no fields have been blank
			if(name.getText().length() > 0  && address.getText().length() > 0
					&& state.getText().length() > 0 && city.getText().length() > 0 
					&& zip.getText().length() > 0)
			{
				// task to add the information to the database
				new addProperty().execute(name.getText().toString(), address.getText().toString(),
						state.getText().toString(), city.getText().toString(), zip.getText().toString());
				
			}
			else
			{
				errorMsg (R.string.add_property_error, R.string.error_fields_left_blank);
			}
		}
		
	};
	
	public OnClickListener cancelButtonListener = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			Intent intent = new Intent(AddProperty.this,
			LandlordHomepage.class);
			intent.putExtra("EMAIL",getIntent().getExtras().getString("EMAIL"));
			intent.putExtra("PASSWORD",getIntent().getExtras().getString("PASSWORD"));
			startActivity(intent);
		}
		
	};
	
	public void errorMsg (int title, int msg)
	{
		// Create an alert dialog box
		AlertDialog.Builder builder = new AlertDialog.Builder(AddProperty.this);
		
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
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_property, menu);
		return true;
	}
	
	private class addProperty extends AsyncTask<String, String, Integer> {
		
		private static final String LOGIN_URL = "http://www.ezrentdata.com/webservice/index.php";
		
		private ProgressDialog pDialog;
		private String request = "addProperty";
		
		private JSONParser jsonParser = new JSONParser();
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(AddProperty.this);
			pDialog.setMessage("Adding Property...");
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
			params.add(new BasicNameValuePair("pname", args[0]));
			params.add(new BasicNameValuePair("addr", args[1]));
			params.add(new BasicNameValuePair("state", args[2]));
			params.add(new BasicNameValuePair("city", args[3]));
			params.add(new BasicNameValuePair("zip", args[4]));
		
			try
			{
			Log.d("addProperty Attempt", "started");
			JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);
			Log.d("getPropertyInfo response", json.toString());
			
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
			
			// if successful
			if(message == 1)
			{
				Toast.makeText(AddProperty.this, "Property Added.", Toast.LENGTH_SHORT).show();
				// go back to the landlord's home page
				Intent intent = new Intent(AddProperty.this, LandlordHomepage.class);
				intent.putExtra("EMAIL", email);
				intent.putExtra("PASSWORD",pass);
				startActivity(intent);
			}
			else if(message == 0)
				Toast.makeText(AddProperty.this, "Property already exists. Not added.", Toast.LENGTH_SHORT).show();
			else
				;
			
	     }
	 }
	

}
