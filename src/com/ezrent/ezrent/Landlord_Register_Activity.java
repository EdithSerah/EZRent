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
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Landlord_Register_Activity extends Activity {
	
	Button landlord_registerButton;
	Button cancel;
	
	EditText landlord_firstName;
	EditText landlord_lastName;
	EditText landlord_email;
	EditText landlord_password;
	EditText landlord_confirmPassword;
	EditText landlord_phone_number;
	
	boolean emailExists;

	private ProgressDialog pDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.landlord_register_);
		
		// Button Initialization
		landlord_registerButton = (Button) findViewById(R.id.landlord_register_Button);
		cancel = (Button) findViewById(R.id.landlord_cancel_Button);
		
		// EditText Initialization
		landlord_firstName = (EditText) findViewById(R.id.landlord_first_name_EditText);
		landlord_lastName = (EditText) findViewById(R.id.landlord_last_name_EditText);
		landlord_email = (EditText) findViewById(R.id.landlord_email_EditText);
		landlord_password = (EditText) findViewById(R.id.landlord_password_EditText);
		landlord_confirmPassword = (EditText) findViewById(R.id.landlord_confirm_password_EditText);
		landlord_phone_number = (EditText) findViewById(R.id.landlord_phone_number_EditText);
		
		//This does not get added here.
		//landlord_unit_number = (EditText) findViewById(R.id.landlord_unit_number_EditText);
				
		// Add ClickListeners to the button
		landlord_registerButton.setOnClickListener(landlord_registerButtonListener);
		cancel.setOnClickListener(cancelButtonListener);
	}
	
	public OnClickListener cancelButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View arg0)
		{
			// TODO Auto-generated method stub
			Intent intent = new Intent(Landlord_Register_Activity.this, LogIn.class);
			startActivity(intent);
		}
		
	};
	
	public OnClickListener landlord_registerButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View arg0) {
			if(landlord_firstName.getText().length() > 0 && landlord_lastName.getText().length() > 0
					&& landlord_email.getText().length() > 0 && landlord_password.getText().length() > 0 
					&& landlord_confirmPassword.getText().length() > 0 && landlord_phone_number.getText().length() > 0)
			{
				if(landlord_confirmPassword.getText().toString().equals(landlord_password.getText().toString()) == false)
				{
					errorMsg (R.string.register_error, R.string.error_passwords_do_not_match);
				}
				else if(landlord_password.getText().length() < 5)
				{
					errorMsg (R.string.register_error, R.string.error_password_length);
				}
				else if(isEmailValid(landlord_email.getText()) == false)
				{
					errorMsg(R.string.register_error, R.string.error_invalid_email);
				}
				else if(landlord_phone_number.getText().length() != 10)	
				{
					errorMsg(R.string.register_error, R.string.error_invalid_phone_number);
				}		
				else
				{
					String type = getIntent().getExtras().getString("type");
					new registerLandlord().execute(landlord_email.getText().toString().toLowerCase(), landlord_password.getText().toString(), 
								landlord_firstName.getText().toString(), landlord_lastName.getText().toString(), type, landlord_phone_number.getText().toString());
				}
			}	
			else
			{
				errorMsg (R.string.register_error, R.string.error_fields_left_blank);
			}
			
		}
	};
	
	public void errorMsg (int title, int msg)
	{
		// Create an alert dialog box
		AlertDialog.Builder builder = new AlertDialog.Builder(Landlord_Register_Activity.this);
		
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.landlord__register_, menu);
		return true;
	}

	// task to talk to database
	class registerLandlord extends AsyncTask<String, String, String>
	{

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(Landlord_Register_Activity.this);
			pDialog.setMessage("Trying Registration");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args) {
			
			String LOGIN_URL = "http://www.ezrentdata.com/webservice/index.php";
			JSONObject json = new JSONObject();
			JSONParser jsonParser = new JSONParser();
			
			try {
					// Building Parameters
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("email", args[0]));
					params.add(new BasicNameValuePair("password", args[1]));
					params.add(new BasicNameValuePair("fname", args[2]));
					params.add(new BasicNameValuePair("lname", args[3]));
					params.add(new BasicNameValuePair("type", args[4]));
					params.add(new BasicNameValuePair("phone", args[5]));
					params.add(new BasicNameValuePair("request", "register"));
				
					// getting product details by making HTTP request
					json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);
					
					
					if(json.getInt("success") == 1)
					{
						emailExists = false;
						return json.getString("message");
					}
					else if(json.getInt("success") == 0)
					{
						emailExists = true;
						return null;
					}
					else
					{
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
		protected void onPostExecute(String message) {
			pDialog.dismiss();
			if (message != null)
			{
				Toast.makeText(Landlord_Register_Activity.this, message, Toast.LENGTH_LONG).show();			
			
				Intent intent = new Intent(Landlord_Register_Activity.this, LandlordHomepage.class);
				intent.putExtra("EMAIL", landlord_email.getText().toString());
				intent.putExtra("PASSWORD", landlord_password.getText().toString());
				startActivity(intent);
			}
			else if(emailExists = true)
			{
				errorMsg (R.string.register_error, R.string.error_email_exists);
			}
		}
	
	}
}
