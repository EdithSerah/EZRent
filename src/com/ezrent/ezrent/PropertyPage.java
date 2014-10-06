package com.ezrent.ezrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// background 1: edebdc




import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;

public class PropertyPage extends Activity
{
	private static final String LOGIN_URL = "http://www.ezrentdata.com/webservice/index.php";
	
	TextView propertyTV;
	TextView addressTV;
	TextView stateTV;
	TextView cityTV;
	TextView zipTV;
	
	private TableLayout unitTableScrollView;
	
	Button home;
	Button delete;
	Button add_unit;
	
	private ProgressDialog pDialog;

	Unit [] UNITS;
		
	LinearLayout layoutOfPopup;
	PopupWindow popupMessage;
	
	private String email;
	private String pass;
	private String pname;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.property_page);
		
		email = getIntent().getExtras().getString("EMAIL");
		pass = getIntent().getExtras().getString("PASSWORD");
		pname = getIntent().getExtras().getString("PROPERTY_NAME");
		
		//Intent intent = getIntent();
		
		propertyTV = (TextView) findViewById(R.id.property_name_TextView);
		addressTV = (TextView) findViewById(R.id.property_address_TextView);
		stateTV = (TextView) findViewById(R.id.property_state_TextView);
		zipTV = (TextView) findViewById(R.id.property_zip_TextView);
		cityTV = (TextView) findViewById(R.id.property_city_TextView);
		//number_of_unitsTV = (TextView) findViewById(R.id.property_unit_TextView);
		
		propertyTV.setText(pname);
		
		unitTableScrollView = (TableLayout) findViewById(R.id.unit_TableScrollView);
		
		home = (Button) findViewById(R.id.property_home_button);
		delete = (Button) findViewById(R.id.property_edit_info_button);
		add_unit = (Button) findViewById(R.id.property_add_unit_button);
		
		home.setOnClickListener(homeButtonListener);
		delete.setOnClickListener(deleteButtonListener);
		add_unit.setOnClickListener(addUnitButtonListener);
		
		new getPropertyInfo().execute();
		
		new getLeases().execute();
	}
	
	private void updateUnitList(Unit [] prop_units)
	{
		// sort properties in alphabetical order
		Arrays.sort(prop_units);
		
		int i;
		for(i = 0; i < prop_units.length; i++)
		{
			//THIS LINE HERE CAUSES A NULL POINTER EXCEPTION
			insertUnitInScrollView(prop_units[i], i);
		}
	}
	
	private void insertUnitInScrollView(Unit unit, int arrayIndex)
	{
		// Get the LayoutInflator service
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		// Use the inflater to inflate a property row from property_row.xml
		View newPropertyRow = inflater.inflate(R.layout.property_row, null);
		
		// Create the TextView for the ScrollView Row
		TextView newPropertyTextView = (TextView) newPropertyRow.findViewById(R.id.property_name_text);
		
		// Add the property name to the TextView
		newPropertyTextView.setText(unit.getNum());
		
		// for adding information into UnitPage activity from UNITS array
		newPropertyTextView.setTag(arrayIndex);
		
		Button select_property_button = (Button) newPropertyRow.findViewById(R.id.property_select_button);
		select_property_button.setOnClickListener(selectUnitButtonListener);
		
		unitTableScrollView.addView(newPropertyRow, arrayIndex);
	}
	
	public OnClickListener selectUnitButtonListener = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			TableRow tableRow = (TableRow) v.getParent();
			
			TextView unitTextView = (TextView) tableRow.findViewById(R.id.property_name_text);
			int i = (Integer) unitTextView.getTag();
			String unitNumber = unitTextView.getText().toString();
			
			Intent intent = new Intent(PropertyPage.this, UnitPage.class);
			intent.putExtra("UNIT_NUMBER", unitNumber);
			intent.putExtra("EMAIL", email);
			intent.putExtra("PASSWORD", pass);
			intent.putExtra("PROPERTY_NAME", pname);	
			intent.putExtra("TENANT_EMAIL",UNITS[i].getEmail());
			intent.putExtra("TENANT_FIRST_NAME",UNITS[i].getFirst());
			intent.putExtra("TENANT_LAST_NAME",UNITS[i].getLast());
			intent.putExtra("MONTHLY_RENT",UNITS[i].getRent());
			intent.putExtra("START_DATE",UNITS[i].getStart());
			intent.putExtra("END_DATE",UNITS[i].getEnd());
			intent.putExtra("LEASE_NOTES",UNITS[i].getNotes());
			intent.putExtra("LEASEID",UNITS[i].getId());
			intent.putExtra("TENANT_PHONE_NUMBER",UNITS[i].getTenantPhone());
			startActivity(intent);
		}
		
	};
	
	public OnClickListener homeButtonListener = new OnClickListener()
	{

		@Override
		public void onClick(View arg0)
		{
			Intent intent = new Intent(PropertyPage.this, LandlordHomepage.class);
			intent.putExtra("EMAIL",email);
			intent.putExtra("PASSWORD",pass);
			startActivity(intent);
		}
		
	};
	
	public OnClickListener deleteButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
		    View popupView = layoutInflater.inflate(R.layout.popup, null);  
		    final PopupWindow popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
		    
			Button yes = (Button)popupView.findViewById(R.id.property_yes_delete_Button);
			Button no = (Button)popupView.findViewById(R.id.property_no_delete_Button);
			
			
			//popupWindow.showAsDropDown(popupView, 75, -100);
			
			yes.setOnClickListener(new Button.OnClickListener()
			{
			     @Override
			     public void onClick(View v)
			     {
			    	 new deleteProperty().execute();
					
			    	 // goes back to landlord homepage after delection. Might need to move this
			    	 // in postexecute of crazy class to use json
			    	 Intent intent = new Intent(PropertyPage.this, LandlordHomepage.class);
			    	 intent.putExtra("EMAIL",email);
			    	 intent.putExtra("PASSWORD",pass);
			    	 startActivity(intent);
			    	 popupWindow.dismiss();
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
	
	public OnClickListener yesButtonListener = new OnClickListener()
	{

		@Override
		public void onClick(View v) {
			/*
			 * 
			 * 
			 * 	CODE TO DELETE PROPERTY
			 * 
			 * 
			 */
			
			Intent intent = new Intent(PropertyPage.this, LandlordHomepage.class);
			intent.putExtra("EMAIL",email);
			intent.putExtra("PASSWORD",pass);
			startActivity(intent);
		}
		
	};
	
	public OnClickListener noButtonListener = new OnClickListener()
	{

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(PropertyPage.this, PropertyPage.class);
			intent.putExtra("EMAIL",email);
			intent.putExtra("PASSWORD",pass);
			startActivity(intent);
			
		}
		
	};
	
	public OnClickListener addUnitButtonListener = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			Intent intent = new Intent(PropertyPage.this, AddUnit.class);
			intent.putExtra("EMAIL",email);
			intent.putExtra("PASSWORD",pass);
			intent.putExtra("PROPERTY_NAME",pname);
			startActivity(intent);
		}
		
	};
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.property_page, menu);
		return true;
	}
	
	private class getLeases extends AsyncTask<String, String, String>
	{
		private String request = "getLeases";
		boolean noProperties = true;
				
		public JSONParser jsonParser = new JSONParser();

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(PropertyPage.this);
			pDialog.setMessage("Loading Property Info...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		@Override
		protected String doInBackground(String... args) {
			 
			JSONArray apno = new JSONArray();
			JSONArray temail = new JSONArray();
			JSONArray tfirst = new JSONArray();
			JSONArray tlast = new JSONArray();
			JSONArray rentamt = new JSONArray();
			JSONArray sday = new JSONArray();
			JSONArray eday = new JSONArray();
			JSONArray notes = new JSONArray();
			JSONArray leaseids = new JSONArray();
			JSONArray tphone = new JSONArray();
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("email", email));
			params.add(new BasicNameValuePair("password", pass));
			params.add(new BasicNameValuePair("request", request));
			params.add(new BasicNameValuePair("pname", pname));
		
			try
			{
				Log.d("listProperties Attempt", "started");
				JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);
				
				if(json.getInt("success") == 1)
				{
					Log.d("getLeases response", json.toString());
				
					JSONObject func = json.getJSONObject(request);
					Log.d("getLeases response1", func.toString());
				
					apno = func.getJSONArray("APARTMENTNO");
					temail = func.getJSONArray("Tenant_EMAIL");
					
					tfirst = func.getJSONArray("FIRSTNAME");
					tlast = func.getJSONArray("LASTNAME");
					tphone = func.getJSONArray("PHONE");
					sday = func.getJSONArray("STARTDATE");
					eday = func.getJSONArray("ENDDATE");
					rentamt = func.getJSONArray("RENT");
					notes = func.getJSONArray("NOTES");
					leaseids = func.getJSONArray("ID");
					
					UNITS = new Unit[apno.length()];
				
					Log.d("String no problem", String.valueOf(apno.length()));
					
					for(int i = 0; i < apno.length(); i++)
					{
						UNITS[i] = new Unit(apno.getString(i), temail.getString(i), tfirst.getString(i), 
								tlast.getString(i), rentamt.getString(i), sday.getString(i), 
									eday.getString(i), notes.getString(i), leaseids.getInt(i), tphone.getString(i));
					}
					
					noProperties = false;
				}
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
			
			return "blank";
	     }


		@Override
	     protected void onPostExecute(String message) {
			if(noProperties == false)
			{
				updateUnitList(UNITS);
			}
			
			pDialog.dismiss();
	     }
	 }
	

	private class deleteProperty extends AsyncTask<String, String, Integer> {

		public JSONParser jsonParser = new JSONParser();
		private String request = "deleteProperty";

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(PropertyPage.this);
			pDialog.setMessage("Deleting Property... ");
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
			params.add(new BasicNameValuePair("pname", pname));
		
			try
			{
			Log.d("deleteProperty Attempt", "started");
			JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);
			if(json.getInt("success") == 1)
			{
				Log.d("deleteProperty response", json.toString());
				return json.getInt("success");
			}
			
			}
			
			catch (JSONException e)
			{
		 	e.printStackTrace();
			}
			return 0;
	     }
		
		@Override
	     protected void onPostExecute(Integer success) {
			if(success == 1)
			{
				Toast.makeText(PropertyPage.this, "Property successfully deleted.", Toast.LENGTH_LONG).show();
			}
			else
			{
				Toast.makeText(PropertyPage.this, "Property deletion failed.", Toast.LENGTH_LONG).show();
			}
			
			}
	}
		
		private class getPropertyInfo extends AsyncTask<String, String, String []> {

			public JSONParser jsonParser = new JSONParser();
			private String request = "getPropertyInfo";

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}
			
			@Override
			protected String [] doInBackground(String... args)
			{
				 
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("email", email));
				params.add(new BasicNameValuePair("password", pass));
				params.add(new BasicNameValuePair("request", request));
				params.add(new BasicNameValuePair("pname", pname));
			
				try
				{
					Log.d("getPropertyInfo Attempt", "started");
					JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);
					Log.d("deleteProperty response", json.toString());
					if(json.getInt("success") == 1)
					{
						JSONObject func = json.getJSONObject(request);
						System.out.println(func.getString("ADDRESS"));
						
						
						String [] info = new String[4];
						info[0] = func.getString("ADDRESS");
						info[1] = func.getString("PSTATE");
						info[2] = func.getString("CITY");
						info[3] = func.getString("ZIP");
						return info;
					}
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
				return null;
		     }

		@Override
	     protected void onPostExecute(String [] args ) {
			if(args != null)
			{
			addressTV.setText(args[0]);
			stateTV.setText(args[1]);
			cityTV.setText(args[2]);
			zipTV.setText(args[3]);
			}
			}
		}
		
		class Unit implements Comparable<Unit>
		{
			private String unit_number;
			private String tenant_email;
			private String tenant_first_name;
			private String tenant_last_name;
			private String monthly_rent;
			private String start_date;
			private String end_date;
			private String lease_notes;
			private String tenant_phone;
			private int id;
			
			
			public Unit(String unit_num, String t_email, String t_fist, String t_last, String rent, String start_d8, String end_d8, 
					String notes, Integer idin, String tphone)
			{
				if(notes.equals(null))
				{
					
				}
				unit_number = unit_num;
				tenant_email = t_email;
				tenant_first_name = t_fist;
				tenant_last_name = t_last;
				monthly_rent = rent;
				start_date = start_d8;
				end_date = end_d8;
				lease_notes = notes;
				id = idin;
				tenant_phone = tphone;
			}
			
			public Integer getId()
			{
				return id;
			}
			
			public String getNum()
			{
				return unit_number;
			}
			public String getEmail()
			{
				return tenant_email;
			}
			public String getFirst()
			{
				return tenant_first_name;
			}
			public String getLast()
			{
				return tenant_last_name;
			}
			public String getRent()
			{
				return monthly_rent;
			}
			public String getStart()
			{
				return start_date;
			}
			public String getEnd()
			{
				return end_date;
			}
			public String getNotes()
			{
				return lease_notes;
			}
			public String getTenantPhone()
			{
				String x = tenant_phone;
				x = "(" + x.substring(0, 3) + ") " + x.substring(3, 6) + "-" + x.substring(6, x.length());
				return x;
			}

			@Override
			public int compareTo(Unit arg0)
			{
				if(isInteger(unit_number) && isInteger(arg0.unit_number))
				{
					int thisNum = Integer.parseInt(unit_number);
					int thatNum = Integer.parseInt(arg0.unit_number);
					
					if(thisNum > thatNum)
					{
						return 1;
					}
					else if(thisNum < thatNum)
					{
						return -1;
					}
					else
					{
						return 0;
					}
				}
				else
				{
					return unit_number.compareTo(arg0.unit_number);
				}
			}
		}
		
		public static boolean isInteger(String str) {
			if (str == null) {
				return false;
			}
			int length = str.length();
			if (length == 0) {
				return false;
			}
			int i = 0;
			if (str.charAt(0) == '-') {
				if (length == 1) {
					return false;
				}
				i = 1;
			}
			for (; i < length; i++) {
				char c = str.charAt(i);
				if (c <= '/' || c >= ':') {
					return false;
				}
			}
			return true;
		}

}
