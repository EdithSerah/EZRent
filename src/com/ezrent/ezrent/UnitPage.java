package com.ezrent.ezrent;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.util.Linkify;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class UnitPage extends Activity
{
	TextView unit_numberTV;
	TextView property_nameTV;
	TextView tenant_emailTV;
	TextView tenant_firstnameTV;
	TextView tenant_lastnameTV;
	TextView monthly_rentTV;
	TextView start_dateTV;
	TextView end_dateTV;
	TextView tenant_phone_numberTV;
	
	EditText lease_notesTV;
	
	String unit_number;
	String property_name;
	String tenant_email;
	String tenant_firstname;
	String tenant_lastname;
	String monthly_rent;
	String start_date;
	String end_date;
	String lease_notes;
	String tenant_phone_number;
	
	Button back_button;
	Button delete;
	
	private String email;
	private String pass;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.unit_page);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		email = getIntent().getExtras().getString("EMAIL");
		pass = getIntent().getExtras().getString("PASSWORD");
		
		unit_number = getIntent().getExtras().getString("UNIT_NUMBER");
		property_name = getIntent().getExtras().getString("PROPERTY_NAME");
		tenant_email = getIntent().getExtras().getString("TENANT_EMAIL");
		tenant_firstname = getIntent().getExtras().getString("TENANT_FIRST_NAME");
		tenant_lastname = getIntent().getExtras().getString("TENANT_LAST_NAME");
		monthly_rent = getIntent().getExtras().getString("MONTHLY_RENT");
		start_date = getIntent().getExtras().getString("START_DATE");
		end_date = getIntent().getExtras().getString("END_DATE");
		lease_notes = getIntent().getExtras().getString("LEASE_NOTES");
		tenant_phone_number = getIntent().getExtras().getString("TENANT_PHONE_NUMBER");
		
		unit_numberTV = (TextView) findViewById(R.id.UNIT_PAGE_unit_number_TextView);
		property_nameTV = (TextView) findViewById(R.id.UNIT_PAGE_property_name_TextView);
		tenant_emailTV = (TextView) findViewById(R.id.UNIT_PAGE_tenant_email_TextView);
		tenant_firstnameTV = (TextView) findViewById(R.id.UNIT_PAGE_tenant_first_name_TextView);
		tenant_lastnameTV = (TextView) findViewById(R.id.UNIT_PAGE_tenant_last_name_TextView);
		monthly_rentTV = (TextView) findViewById(R.id.UNIT_PAGE_monthly_rent_TextView);
		start_dateTV = (TextView) findViewById(R.id.UNIT_PAGE_start_date_TextView);
		end_dateTV = (TextView) findViewById(R.id.UNIT_PAGE_end_date_TextView);
		tenant_phone_numberTV = (TextView) findViewById(R.id.UNIT_PAGE_tenant_phone_number_TextView);
		
		lease_notesTV = (EditText) findViewById(R.id.UNIT_PAGE_lease_notes_EditText);
		
		unit_numberTV.setText(unit_number);
		property_nameTV.setText(property_name);
		tenant_emailTV.setText(tenant_email);
		tenant_firstnameTV.setText(tenant_firstname);
		tenant_lastnameTV.setText(tenant_lastname);
		monthly_rentTV.setText(monthly_rent);
		start_dateTV.setText(start_date);
		end_dateTV.setText(end_date);
		tenant_phone_numberTV.setText(tenant_phone_number);
		
		Linkify.addLinks(tenant_phone_numberTV, Linkify.PHONE_NUMBERS);
		Linkify.addLinks(tenant_emailTV, Linkify.EMAIL_ADDRESSES);
		
		if(lease_notes.equals("null"))
		{
			lease_notesTV.setText("");
		}
		else
		{
			lease_notesTV.setText(lease_notes);
			lease_notesTV.setSelection(lease_notesTV.getText().length());
		}
		
		back_button = (Button) findViewById(R.id.UNIT_PAGE_back_Button);
		delete = (Button) findViewById(R.id.UNIT_PAGE_delete_Button);
		
		back_button.setOnClickListener(backListener);
		delete.setOnClickListener(deleteListener);
	}
	
	public OnClickListener deleteListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
		    View popupView = layoutInflater.inflate(R.layout.popup, null);  
		    final PopupWindow popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
		    
		    TextView txt = (TextView)popupView.findViewById(R.id.popup_text);
			Button yes = (Button)popupView.findViewById(R.id.property_yes_delete_Button);
			Button no = (Button)popupView.findViewById(R.id.property_no_delete_Button);
			
			txt.setText("Are you sure you want to delete this unit?");
			
			//popupWindow.showAsDropDown(popupView, 75, -100);
			
			yes.setOnClickListener(new Button.OnClickListener()
			{
			     @Override
			     public void onClick(View v)
			     {
			    	 new deleteUnit().execute();
			     }
			});
			
			     
			no.setOnClickListener(new Button.OnClickListener()
			{
			     @Override
			     public void onClick(View v)
			     {
			    	 popupWindow.dismiss();
			     }
			});
			
			popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
		}
	};
	
	public OnClickListener backListener = new OnClickListener()
	{
		@Override
		public void onClick(View arg0)
		{
			String notes = lease_notesTV.getText().toString();
			if(notes.length() != 0 && !notes.equals(lease_notes))
			{
				new addNote().execute(notes);
			}
			else
			{
				backToProperty();
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.unit_page, menu);
		return true;
	}
	
	private class deleteUnit extends AsyncTask<String, String, Integer>
	{
        private static final String LOGIN_URL = "http://www.ezrentdata.com/webservice/index.php";
       
        private ProgressDialog pDialog;
        private String request = "deleteUnit";
       
        private JSONParser jsonParser = new JSONParser();
       
        @Override
        protected void onPreExecute()
        {
                super.onPreExecute();
                pDialog = new ProgressDialog(UnitPage.this);
                pDialog.setMessage("Deleting Unit...");
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
        	params.add(new BasicNameValuePair("lease_id", Integer.toString(getIntent().getExtras().getInt("LEASEID"))));
        	try
        	{
        		Log.d("addNote", "started");
        		JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);
                Log.d("addNote response", json.toString());
                
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
        		Toast.makeText(UnitPage.this, "Unit Deleted.", Toast.LENGTH_SHORT).show();
        		backToProperty();
        	}
        	else
        	{
        		Toast.makeText(UnitPage.this, "Error: Unit could not be deleted.", Toast.LENGTH_LONG).show();
        		backToProperty();
        	}
        }
	}

	private class addNote extends AsyncTask<String, String, Integer>
	{
        private static final String LOGIN_URL = "http://www.ezrentdata.com/webservice/index.php";
       
        private ProgressDialog pDialog;
        private String request = "addNote";
       
        private JSONParser jsonParser = new JSONParser();
       
        @Override
        protected void onPreExecute()
        {
                super.onPreExecute();
                pDialog = new ProgressDialog(UnitPage.this);
                pDialog.setMessage("Adding Notes to the Lease...");
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
        	params.add(new BasicNameValuePair("notes", args[0]));
        	params.add(new BasicNameValuePair("idreq", Integer.toString(getIntent().getExtras().getInt("LEASEID"))));
        	try
        	{
        		Log.d("addNote", "started");
        		JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);
                Log.d("addNote response", json.toString());
                
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
        		Toast.makeText(UnitPage.this, "Note Added.", Toast.LENGTH_SHORT).show();
        		backToProperty();
        	}
        	else
        	{
        		Toast.makeText(UnitPage.this, "Error: Note couldn't be added.", Toast.LENGTH_LONG).show();
        		backToProperty();
        	}
        }
	}
	
	private void backToProperty()
	{
		Intent intent = new Intent(UnitPage.this, PropertyPage.class);
		intent.putExtra("EMAIL",email);
		intent.putExtra("PASSWORD",pass);
		intent.putExtra("PROPERTY_NAME", property_name);
		startActivity(intent);
	}
}
