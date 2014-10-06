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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Landlord_Edit_Info extends Activity
{
	private String email;
	private String pass;
	
	private String new_email;
	private String new_pass;
	
	private String firstname;
	private String lastname;
	private String phone_number;
	
	private String type;
	
	EditText landlord_firstName;
	EditText landlord_lastName;
	EditText landlord_email;
	EditText landlord_current_password;
	EditText landlord_new_password;
	EditText landlord_confirm_new_password;
	EditText landlord_phone_number;
	
	Button cancel;
	Button save;
	
	float one = (float) 1.0;
	float three = (float) 0.3;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.landlord__edit__info);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		//Here is all the information that you need Kevin
		
		email = getIntent().getExtras().getString("EMAIL");
		pass = getIntent().getExtras().getString("PASSWORD");
		firstname = getIntent().getExtras().getString("FIRSTNAME");
		lastname = getIntent().getExtras().getString("LASTNAME");
		type = getIntent().getExtras().getString("TYPE");
		
		landlord_firstName = (EditText) findViewById(R.id.landlord_edit_first_name_EditText);
		landlord_lastName = (EditText) findViewById(R.id.landlord_edit_last_name_EditText);
		landlord_email = (EditText) findViewById(R.id.landlord_edit_email_EditText);
		landlord_current_password = (EditText) findViewById(R.id.landlord_current_password_EditText);
		landlord_new_password = (EditText) findViewById(R.id.landlord_new_password_EditText);
		landlord_confirm_new_password = (EditText) findViewById(R.id.landlord_confirm_new_password_EditText);
		landlord_phone_number = (EditText) findViewById(R.id.landlord_edit_phone_number_EditText);
		
		landlord_firstName.setText(firstname);
		
		landlord_lastName.setText(lastname);
		landlord_email.setText(email);
		
		new getUserInfo().execute();
		
		cancel = (Button) findViewById(R.id.landlord_edit_cancel_Button);
		save = (Button) findViewById(R.id.landlord_edit_save_Button);
		
		cancel.setOnClickListener(cancelButtonListener);
		save.setOnClickListener(saveButtonListener);
		
		landlord_firstName.addTextChangedListener(firstnameListener);
		landlord_email.addTextChangedListener(emailListener);
		landlord_lastName.addTextChangedListener(lastnameListener);
		landlord_phone_number.addTextChangedListener(phoneListener);
	}
	
	public OnClickListener saveButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View arg0)
		{
			String newfirstname, newlastname, newemail, newpass, newphone;
			
			boolean editINFO = true;
			boolean newINFO = true;
			newfirstname = firstname;
			newlastname = lastname;
			newemail = email;
			newpass = pass;
			newphone = phone_number;
			
			// sees if password is trying to be changed
			if((landlord_new_password.getText().length() > 0) || (landlord_confirm_new_password.getText().length() > 0) || (landlord_current_password.getText().length() > 0))
			{
				if((landlord_new_password.getText().length() > 0) && (landlord_confirm_new_password.getText().length() > 0) && (landlord_current_password.getText().length() > 0))
				{
					if(landlord_new_password.getText().length() < 5)
					{
						editINFO = false;
						errorMsg (R.string.edit_password_error, R.string.error_password_length);
					}
					
					else if(landlord_confirm_new_password.getText().toString().equals(landlord_new_password.getText().toString()) == false)
					{
						editINFO = false;
						errorMsg (R.string.register_error, R.string.error_new_passwords_do_not_match);
					}
					
					else if(landlord_current_password.getText().toString().equals(pass) == false)
					{
						editINFO = false;
						errorMsg (R.string.register_error, R.string.error_current_passwords_do_not_match);
					}
					else
					{
						editINFO = true;
						newpass = landlord_new_password.getText().toString();
					}
				}
				
				else
				{
					editINFO = false;
					errorMsg (R.string.edit_password_error, R.string.error_password_fields_left_blank);
				}
			}
			
			// sees if info needs to be updated
			
			if(landlord_firstName.getText().length() > 0 && landlord_lastName.getText().length() > 0 && landlord_email.getText().length() > 0 
					&& landlord_phone_number.getText().length() > 0)
			{
				if(editINFO == true)
				{
					if(landlord_firstName.getText().length() == 0 || landlord_lastName.getText().length() == 0 
							|| landlord_email.getText().length() == 0 || landlord_phone_number.getText().length() == 0)
					{
						editINFO = false;
						errorMsg (R.string.edit_error, R.string.error_fields_left_blank);
					}
					else
					{
						if(landlord_firstName.getText().toString().equals(firstname) && landlord_lastName.getText().toString().equals(lastname)
								&& landlord_email.getText().toString().equalsIgnoreCase(email) && landlord_phone_number.getText().toString().equals(phone_number))
						{
							if((landlord_new_password.getText().length() > 0) || (landlord_confirm_new_password.getText().length() > 0) 
									|| (landlord_current_password.getText().length() > 0))
							{
								editINFO = true;
							}
							else
							{
								newINFO = false;
								editINFO = false;
							}
						}
						else if(isEmailValid(landlord_email.getText()) == false)
						{
							editINFO = false;
							errorMsg(R.string.register_error, R.string.error_invalid_email);
						}
						else if(landlord_phone_number.getText().length() != 10)
						{
							editINFO = false;
							errorMsg(R.string.register_error, R.string.error_invalid_phone_number);
						}
						else
						{
							newfirstname = landlord_firstName.getText().toString();
							newlastname = landlord_lastName.getText().toString();
							newemail = landlord_email.getText().toString().toLowerCase();
							newphone = landlord_phone_number.getText().toString();
						}
					}
				}
			}
			else
			{
				editINFO = false;
				errorMsg (R.string.edit_error, R.string.error_fields_left_blank);
			}
			
			if(editINFO == true)
			{
				new_email = newemail;
				new_pass = newpass;
				
				new editUserInfo().execute(newfirstname, newlastname, newemail, newpass, newphone);
			}
			
			else if(newINFO == false)
			{
				Toast.makeText(Landlord_Edit_Info.this, "No new account Information to be updated.", Toast.LENGTH_SHORT).show();
				
				Intent intent;
				
				if(type.equals("l"))
				{
					intent = new Intent(Landlord_Edit_Info.this, LandlordHomepage.class);
				}
				else
				{
					intent = new Intent(Landlord_Edit_Info.this, TenantHomepage.class);
					intent.putExtra("TENANT_FIRST_NAME", newfirstname);
				}
				
				intent.putExtra("EMAIL", email);
				intent.putExtra("PASSWORD", pass);
				
				startActivity(intent);
			}
		}
	};
	
	private TextWatcher phoneListener = new TextWatcher()
	{

		@Override
		public void afterTextChanged(Editable s) {
			
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count)
		{
			if(landlord_phone_number.getText().toString().compareTo(phone_number) != 0)
			{
				landlord_phone_number.setAlpha(one);
			}
			else
			{
				landlord_phone_number.setAlpha(three);
			}
			
		}
	};
	
	private TextWatcher firstnameListener = new TextWatcher()
	{

		@Override
		public void afterTextChanged(Editable s) {
			
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count)
		{
			if(landlord_firstName.getText().toString().compareTo(firstname) != 0)
			{
				landlord_firstName.setAlpha(one);
			}
			else
			{
				landlord_firstName.setAlpha(three);
			}
		}
	};
	
	private TextWatcher lastnameListener = new TextWatcher(){

		@Override
		public void afterTextChanged(Editable s) {}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count)
		{
			if(landlord_lastName.getText().toString().compareTo(lastname) != 0)
			{
				landlord_lastName.setAlpha(one);
			}
			else
			{
				landlord_lastName.setAlpha(three);
			}
		}
	};
	
	private TextWatcher emailListener = new TextWatcher(){

		@Override
		public void afterTextChanged(Editable s) {}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count)
		{
			if(landlord_email.getText().toString().toLowerCase().compareTo(email) != 0)
			{
				landlord_email.setAlpha(one);
			}
			else
			{
				landlord_email.setAlpha(three);
			}
		}
	};
	
	public OnClickListener cancelButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View arg0)
		{
			Intent intent;
			
			if(type.equals("l"))
			{
				intent = new Intent(Landlord_Edit_Info.this, LandlordHomepage.class);
			}
			else
			{
				intent = new Intent(Landlord_Edit_Info.this, TenantHomepage.class);
				intent.putExtra("TENANT_FIRST_NAME", firstname);
				intent.putExtra("TENANT_LAST_NAME", lastname);
			}
			
			intent.putExtra("EMAIL", email);
			intent.putExtra("PASSWORD", pass);
			
			startActivity(intent);
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.landlord__edit__info, menu);
		return true;
	}
	
	// You have to enter all of those fields, if they dont' want to change anything in particular just
	// re-enter the old information that we passed into this java file.
	
	class editUserInfo extends AsyncTask<String, String, Integer>
	{
		private static final String LOGIN_URL = "http://www.ezrentdata.com/webservice/index.php";
		
		private ProgressDialog pDialog;
		private String request = "editUser";
		private JSONParser jsonParser = new JSONParser();
		
		// !! 		NONE of these can be blank.			!!
		//Just re-enter the old information if they want to keep it the same
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(Landlord_Edit_Info.this);
			pDialog.setMessage("Updating User Information...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected Integer doInBackground(String... args)
		{
			try
			{
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("email", email));
				params.add(new BasicNameValuePair("password", pass));
				params.add(new BasicNameValuePair("request", request));
				
				params.add(new BasicNameValuePair("fname", args[0]));
				params.add(new BasicNameValuePair("lname", args[1]));
				params.add(new BasicNameValuePair("nemail", args[2]));
				params.add(new BasicNameValuePair("npass", args[3]));
				params.add(new BasicNameValuePair("nphone", args[4]));
					
				Log.d("editUserInfo", "started");
				JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);
				Log.d("editUserInfo", json.toString());

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
				Toast.makeText(Landlord_Edit_Info.this, "Account Information Updated.", Toast.LENGTH_SHORT).show();
				Intent intent;
				
				if(type.equals("l"))
				{
					intent = new Intent(Landlord_Edit_Info.this, LandlordHomepage.class);
				}
				else
				{
					intent = new Intent(Landlord_Edit_Info.this, TenantHomepage.class);
				}
				
				intent.putExtra("EMAIL", new_email);
				intent.putExtra("PASSWORD", new_pass);
				
				startActivity(intent);
			}
			else
			{
				Toast.makeText(Landlord_Edit_Info.this, "Account not updated. New Email already in use.", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	class getUserInfo extends AsyncTask<String, String, Integer>
	{
		private static final String LOGIN_URL = "http://www.ezrentdata.com/webservice/index.php";
		
		private ProgressDialog pDialog;
		private String request = "getUserInfo";
		private JSONParser jsonParser = new JSONParser();
		
		// !! 		NONE of these can be blank.			!!
		//Just re-enter the old information if they want to keep it the same
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pDialog = new ProgressDialog(Landlord_Edit_Info.this);
			pDialog.setMessage("Getting User Information...");
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
				Log.d("getUserInfo", "started");
				JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);
				Log.d("getUserInfo", json.toString());
				
				if(json.getInt("success") == 1)
	            {
					JSONObject func = json.getJSONObject(request);
					
					phone_number = func.getString("PHONE");
					
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
		protected void onPostExecute(Integer message)
		{
			pDialog.dismiss();
			
			if(message == 1)
			{
				landlord_phone_number.setText(phone_number);
			}
			else
			{
				Toast.makeText(Landlord_Edit_Info.this, "Failed to get users information.", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	public void errorMsg (int title, int msg)
	{
		// Create an alert dialog box
		AlertDialog.Builder builder = new AlertDialog.Builder(Landlord_Edit_Info.this);
		
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
	
	public boolean isEmailValid(CharSequence email)
	{
		return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
	};
}
