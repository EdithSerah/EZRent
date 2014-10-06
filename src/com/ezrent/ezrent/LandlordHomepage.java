package com.ezrent.ezrent;

import java.util.Arrays;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
//import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
//import android.widget.Toast;



import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.json.JSONException;

public class LandlordHomepage extends Activity {
	
	public final static String PROPERTY_NAME = "com.example.myfirstapp.PROPERTY";
	private static final String LOGIN_URL = "http://www.ezrentdata.com/webservice/index.php";
	private static final String listprop = "listProperties";
	
	public JSONParser jsonParser2;
	
	private TableLayout propertyTableScrollView;
	
	//Button select_property_button;
	//Button delete_property_button;
	
	Button add_property_button;
	Button inbox_button;
	Button logout_button;
	Button edit_info_button;
	boolean properties = true;
	private ProgressDialog pDialog;
	
	private String email;
	private String pass;
	String firstname;
	String lastname;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.landlord_homepage);
		
		jsonParser2 = new JSONParser();

		
		email = getIntent().getExtras().getString("EMAIL");
		pass = getIntent().getExtras().getString("PASSWORD");
		
		// Initialize Components
		propertyTableScrollView = (TableLayout) findViewById(R.id.property_TableScrollView);
		
		//select_property_button = (Button) findViewById(R.id.property_select_button);
		//delete_property_button = (Button) findViewById(R.id.property_delete_button);
		inbox_button = (Button) findViewById(R.id.landlord_inbox_button);
		logout_button = (Button) findViewById(R.id.landlord_logout_button);
		edit_info_button = (Button) findViewById(R.id.landlord_edit_info_button);
		add_property_button = (Button) findViewById(R.id.landlord_add_property_button);
		
		// Add ClickListeners to the buttons
		//select_property_button.setOnClickListener(selectPropertyButtonListener);
		//delete_property_button.setOnClickListener(deleteStocksButtonListener);
		inbox_button.setOnClickListener(inboxButtonListener);
		logout_button.setOnClickListener(logoutButtonListener);
		edit_info_button.setOnClickListener(editInfoButtonListener);
		add_property_button.setOnClickListener(addPropertyButtonListener);
		
		new GetProperties().execute();
		new getUserInfo().execute(firstname, lastname);
	}
	
	class GetProperties extends AsyncTask<String, String, String> {

		// three methods get called, first preExecture, then do in background,
		// and once do
		// in back ground is completed, the onPost execute method will be
		// called.

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(LandlordHomepage.this);
			pDialog.setMessage("Loading Properties...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args) {

			try {
					// Building Parameters
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("email", email));
					params.add(new BasicNameValuePair("password", pass));
					params.add(new BasicNameValuePair("request", listprop));
				
					// getting product details by making HTTP request
					JSONObject json2 = jsonParser2.makeHttpRequest(LOGIN_URL, "POST", params);
					
					if(json2.getInt("success") == 1)
					{
						String parseme = json2.getString("listProperties");
						return parseme;
					}
					else if(json2.getInt("success") == 2)
					{
						properties = false;
						//Either the database failed or there's a bad login
						//This error needs to be handled somehow.
						//Probably best to just reload this page.
						return null;
					}
					

				}
			
			catch (JSONException e)
				{
			 	e.printStackTrace();
			}

			return null;

		}
		
		@Override
		protected void onPostExecute(String parseme) {
			if(properties == true)
			{
				String [] properties;
				properties = parseme.split("\\s*,\\s*");
			
				updatePropertyList(properties);
			}
			else if(properties == false)
			{
				// do something
			}
			pDialog.dismiss();
		}
	}
	
	
	private void updatePropertyList(String [] properties)
	{
		// sort properties in alphabetical order
		Arrays.sort(properties, String.CASE_INSENSITIVE_ORDER);
		
		int i;
		for(i = 1; i < properties.length; i++)
		{
			insertPropertyInScrollView(properties[i], i-1);
		}
	}
	
	private void insertPropertyInScrollView(String property, int arrayIndex)
	{
		// Get the LayoutInflator service
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		// Use the inflater to inflate a stock row from stock_quote_row.xml
		View newPropertyRow = inflater.inflate(R.layout.property_row, null);
		
		// Create the TextView for the ScrollView Row
		TextView newPropertyTextView = (TextView) newPropertyRow.findViewById(R.id.property_name_text);
		
		// Add the property name to the TextView
		newPropertyTextView.setText(property);
		
		Button select_property_button = (Button) newPropertyRow.findViewById(R.id.property_select_button);
		select_property_button.setOnClickListener(selectPropertyButtonListener);
		
		propertyTableScrollView.addView(newPropertyRow, arrayIndex);
	}
	
	public OnClickListener selectPropertyButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			// TODO Auto-generated method stub
			
			// Get the text saved in the TextView next to the clicked button
			// with the id stockSymbolTextView

			TableRow tableRow = (TableRow) v.getParent();
			TextView propertyTextView = (TextView) tableRow.findViewById(R.id.property_name_text);
            String propertyName = propertyTextView.getText().toString();
            
            // An intent is an object that can be used to start another activity
            Intent intent = new Intent(LandlordHomepage.this, PropertyPage.class);
            
            // Add the property to the intent
            intent.putExtra("EMAIL",email);
    		intent.putExtra("PASSWORD",pass);
            intent.putExtra("PROPERTY_NAME",propertyName);
            
            startActivity(intent);
		}
	};
	
	public OnClickListener addPropertyButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			// TODO Auto-generated method stub
			Intent intent = new Intent(LandlordHomepage.this,
			AddProperty.class);
			intent.putExtra("EMAIL",email);
			intent.putExtra("PASSWORD",pass);
			startActivity(intent);
		}
	};
	
	public OnClickListener editInfoButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			// TODO Auto-generated method stub
			
			if(!(firstname.equals("") || firstname.equals("")))
					{
				//Query succeeded. Let's pass the names into edit
				//And do our thing
					}
			
			else
			{
				//ABORT the query didnt get the names.
				//This would be a weird error so maybe just stay on this page
			}
			
			Intent intent = new Intent(LandlordHomepage.this, Landlord_Edit_Info.class);
			intent.putExtra("EMAIL",email);
			intent.putExtra("PASSWORD",pass);
			intent.putExtra("FIRSTNAME",firstname);
			intent.putExtra("LASTNAME",lastname);
			intent.putExtra("TYPE", "l");
			startActivity(intent);
		}
		
	};
	
	public OnClickListener logoutButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			// TODO Auto-generated method stub
			Intent intent = new Intent(LandlordHomepage.this,
			LogIn.class);

			startActivity(intent);
		}
	};
	
	public OnClickListener inboxButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			// TODO Auto-generated method stub
			Intent intent = new Intent(LandlordHomepage.this,
			LandlordInbox.class);
			intent.putExtra("EMAIL",email);
			intent.putExtra("PASSWORD",pass);
			startActivity(intent);
		}
	};
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.landlord_homepage, menu);
		return true;
	}
	class getUserInfo extends AsyncTask<String, String, Integer> {

		private static final String LOGIN_URL = "http://www.ezrentdata.com/webservice/index.php";
		
		private ProgressDialog pDialog;
		private String request = "getUserInfo";
		private JSONParser jsonParser = new JSONParser();
		

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(String... args) {
			
			try {
					 
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("email", email));
					params.add(new BasicNameValuePair("password", pass));
					params.add(new BasicNameValuePair("request", request));
					
					Log.d("getUserInfo", "started");
					JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);
					Log.d("getUserInfo", json.toString());

					
					if(json.getInt("success") == 1)
					{
						JSONObject func = json.getJSONObject(request);
						firstname = func.getString("FIRSTNAME");
						lastname = func.getString("LASTNAME");
						return 1;
					}
					else if(json.getInt("success") == 2)
					{
						return 2;
					}
					else
						return 0;
					

				}
			
			catch (JSONException e)
				{
			 	e.printStackTrace();
			}

			return 0;

		}
		
		@Override
		protected void onPostExecute(Integer message) {
			
		}
	}

}
