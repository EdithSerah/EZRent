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
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TenantHomepage extends Activity
{
	// TextViews that display information
	TextView unit_numberTV;
	TextView property_nameTV;
	TextView tenant_emailTV;
	TextView tenant_first_nameTV;
	TextView tenant_last_nameTV;
	TextView monthly_rentTV;
	TextView start_dateTV;
	TextView end_dateTV;
	
	TextView landlord_first_nameTV;
	TextView landlord_last_nameTV;
	TextView landlord_emailTV;
	TextView landlord_phone_numberTV;
	
	// TextViews that display info titles (eg. Landlord E-Mail:)
	TextView display1;
	TextView display2;
	TextView display3;
	TextView display4;
	TextView display5;
	TextView display6;
	TextView display7;
	TextView display8;
	TextView display9;
	
	// Strings I need
	String unit_number;
	String property_name;
	String tenant_first_name;
	String tenant_last_name;
	String monthly_rent;
	String start_date;
	String end_date;
	
	String landlord_first_name;
	String landlord_last_name;
	String landlord_email;
	String landlord_phone_number;

	Button logout;
	Button edit;
	Button request;
	
	private String email;
	private String pass;
	private boolean info;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tenant_homepage);
		
		email = getIntent().getExtras().getString("EMAIL");
		pass = getIntent().getExtras().getString("PASSWORD");
		
		info = false;
		
		display1 = (TextView) findViewById(R.id.homepage_tentant_display1);
		display2 = (TextView) findViewById(R.id.homepage_tentant_display2);
		display3 = (TextView) findViewById(R.id.homepage_tentant_display3);
		display4 = (TextView) findViewById(R.id.homepage_tentant_display4);
		display5 = (TextView) findViewById(R.id.homepage_tentant_display5);
		display6 = (TextView) findViewById(R.id.homepage_tentant_display6);
		display7 = (TextView) findViewById(R.id.homepage_tentant_display7);
		display8 = (TextView) findViewById(R.id.homepage_tentant_display8);
		display9 = (TextView) findViewById(R.id.homepage_tentant_display9);
		
		unit_numberTV = (TextView) findViewById(R.id.homepage_tentant_unit_number_TextView);
		property_nameTV = (TextView) findViewById(R.id.homepage_tentant_property_name_TextView);
		tenant_emailTV = (TextView) findViewById(R.id.homepage_tenant_email_TextView);
		tenant_first_nameTV = (TextView) findViewById(R.id.homepage_tenant_first_name_TextView);
		tenant_last_nameTV = (TextView) findViewById(R.id.homepage_tenant_last_name_TextView);
		monthly_rentTV = (TextView) findViewById(R.id.homepage_tentant_monthly_rent_TextView);
		start_dateTV = (TextView) findViewById(R.id.homepage_tentant_start_date_TextView);
		end_dateTV = (TextView) findViewById(R.id.homepage_tentant_end_date_TextView);
		
		landlord_first_nameTV = (TextView) findViewById(R.id.homepage_tentant_landlord_first_name_TextView);
		landlord_last_nameTV = (TextView) findViewById(R.id.homepage_tentant_landlord_last_name_TextView);
		landlord_emailTV = (TextView) findViewById(R.id.homepage_tentant_landlord_email_TextView);
		landlord_phone_numberTV = (TextView) findViewById(R.id.homepage_tentant_landlord_phone_number_TextView);
		
		logout = (Button) findViewById(R.id.homepage_tenant_logout_button);
		edit = (Button) findViewById(R.id.homepage_tenant_edit_button);
		request = (Button) findViewById(R.id.homepage_tenant_request_button);
		
		tenant_emailTV.setText(email);
		
		logout.setOnClickListener(logoutListener);
		edit.setOnClickListener(editListener);
		request.setOnClickListener(requestListener);
		
		// loads fields with info from database
		new getTenantLease().execute();
	}
	
	public OnClickListener editListener = new OnClickListener()
	{
		@Override
		public void onClick(View arg0)
		{
			Intent intent = new Intent(TenantHomepage.this, Landlord_Edit_Info.class);
			intent.putExtra("EMAIL", email);
			intent.putExtra("PASSWORD", pass);
			intent.putExtra("FIRSTNAME", tenant_first_name);
			intent.putExtra("LASTNAME", tenant_last_name);
			intent.putExtra("TYPE", "t");
			startActivity(intent);
		}
	};
	
	public OnClickListener requestListener = new OnClickListener()
	{
		@Override
		public void onClick(View arg0)
		{
			if(info == true)
			{
				Intent intent = new Intent(TenantHomepage.this, Request.class);
				intent.putExtra("UNIT_NUMBER", unit_number);
				intent.putExtra("EMAIL", email); // tenants email
				intent.putExtra("PASSWORD", pass);
				intent.putExtra("PROPERTY_NAME", property_name);	
				intent.putExtra("LANDLORD_EMAIL", landlord_email);
				intent.putExtra("TENANT_FIRST_NAME", tenant_first_name);
				intent.putExtra("TENANT_LAST_NAME", tenant_last_name);
				startActivity(intent);
			}
			else
			{
				errorMsg (R.string.request_error, R.string.error_no_landlord);
			}
		}
		
	};
	
	public OnClickListener logoutListener = new OnClickListener()
	{
		@Override
		public void onClick(View arg0)
		{
			Intent intent = new Intent(TenantHomepage.this, LogIn.class);
			startActivity(intent);
		}
	};
	
	public void errorMsg (int title, int msg)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(TenantHomepage.this);
		builder.setTitle(title);
		builder.setPositiveButton(R.string.ok, null);
		builder.setMessage(msg);
		AlertDialog theAlertDialog = builder.create();
		theAlertDialog.show();
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tenant_homepage, menu);
		return true;
	}
	
	private class getTenantLease extends AsyncTask<String, String, Integer>
	{
        
        private static final String LOGIN_URL = "http://www.ezrentdata.com/webservice/index.php";
       
        private ProgressDialog pDialog;
        private String request = "getCurrentLease";
       
        private JSONParser jsonParser = new JSONParser();
       
        @Override
        protected void onPreExecute()
        {
                super.onPreExecute();
                pDialog = new ProgressDialog(TenantHomepage.this);
                pDialog.setMessage("Loading Tenant Information");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(true);
                pDialog.show();
        }
       
        @Override
        protected Integer doInBackground(String... args)
        {
        	List<NameValuePair> params = new ArrayList<NameValuePair>();
        	params.add(new BasicNameValuePair("email", email));
        	params.add(new BasicNameValuePair("password", pass));
        	params.add(new BasicNameValuePair("request", request));
         	try
        	{
         		Log.d("deleteMessage", "started");
	            JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);
	            Log.d("deleteMessage response", json.toString());
	            if(json.getInt("success") == 1)
	            {
	            	JSONObject func = json.getJSONObject(request);
	            	
	            	unit_number = func.getString("UNIT_NUMBER");
	                property_name = func.getString("PNAME");
	                tenant_first_name = func.getString("TENANT_FIRST_NAME");
	                tenant_last_name = func.getString("TENANT_LAST_NAME");
	                monthly_rent = func.getString("MONTHLY_RENT");
	                start_date = func.getString("START_DATE");
	                end_date = func.getString("END_DATE");
	                landlord_first_name= func.getString("LANDLORD_FIRST_NAME");
	                landlord_last_name= func.getString("LANDLORD_LAST_NAME");
	               	landlord_email= func.getString("LANDLORD_EMAIL");
	               	landlord_phone_number= func.getString("LANDLORD_PHONE_NUMBER");
	               	info = true;
	            }
	            else if(json.getInt("success") == 2)
	            {
	            	JSONObject func = json.getJSONObject(request);
	            	
	            	tenant_first_name = func.getString("TENANT_FIRST_NAME");
	                tenant_last_name = func.getString("TENANT_LAST_NAME");
	            }
	            
	            return json.getInt("success");
        	}
         	catch (JSONException e)
         	{
         		e.printStackTrace();
         	}
         	
         	return 0;
        }

        @Override
        protected void onPostExecute(Integer message)
        {
        	pDialog.dismiss();
               
        	if(message == 1)
        	{
        		unit_numberTV.setText(unit_number);
        		property_nameTV.setText(property_name);
        		tenant_first_nameTV.setText(tenant_first_name);
        		tenant_last_nameTV.setText(tenant_last_name);
        		monthly_rentTV.setText(monthly_rent);
        		start_dateTV.setText(start_date);
        		end_dateTV.setText(end_date);

        		landlord_first_nameTV.setText(landlord_first_name);
        		landlord_last_nameTV.setText(landlord_last_name);
        		landlord_emailTV.setText(landlord_email);
        		
        		String x = landlord_phone_number;
        		
        		landlord_phone_number = "(" + x.substring(0, 3) + ") " + x.substring(3, 6) + "-" + x.substring(6, x.length());
        		
        		landlord_phone_numberTV.setText(landlord_phone_number);
        		
        		Linkify.addLinks(landlord_phone_numberTV, Linkify.PHONE_NUMBERS);
        		Linkify.addLinks(landlord_emailTV, Linkify.EMAIL_ADDRESSES);
        		
                Toast.makeText(TenantHomepage.this, "Tenant Info Loaded.", Toast.LENGTH_SHORT).show();
            }
        	else if(message == 2)
        	{
        		info = false;
                display1.setText("No information to display. Your landlord has not added you to a unit or has deleted your property.");
        		display2.setText("");
        		display3.setText("");
        		display4.setText("");
        		display5.setText("");
        		display6.setText("");
        		display7.setText("");
        		display8.setText("");
        		display9.setText("");
        		
        		tenant_first_nameTV.setText(tenant_first_name);
        		tenant_last_nameTV.setText(tenant_last_name);
        	}
        	else
        	{
        		Toast.makeText(TenantHomepage.this, "Error Loading Tenant Information", Toast.LENGTH_SHORT).show();
        	}   
        }
	}
}
