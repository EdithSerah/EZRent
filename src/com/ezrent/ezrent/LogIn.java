package com.ezrent.ezrent;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.json.JSONException;

public class LogIn extends Activity
{
	Button loginButton;
	Button registerLandlordButton;
	Button registerTenantButton;

	EditText emailET;
	EditText passwordET;
	
	JSONParser jsonParser;
	private ProgressDialog pDialog;

	private static final String LOGIN_URL = "http://www.ezrentdata.com/webservice/index.php";
	private static final String TAG_SUCCESS = "success";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log_in);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		jsonParser = new JSONParser();

		// Initializing Buttons
		loginButton = (Button) findViewById(R.id.login_Button);
		registerLandlordButton = (Button) findViewById(R.id.landlord_button);
		registerTenantButton = (Button) findViewById(R.id.tenant_button);

		// Initializing EditTexts
		emailET = (EditText) findViewById(R.id.email_editText);
		passwordET = (EditText) findViewById(R.id.password_editText);
		
		// REMOVE AFTER TESTING IS DONE!!
		emailET.setText("trump@trump.com");
		passwordET.setText("aaaaa");

		// Add ClickListeners to the buttons
		loginButton.setOnClickListener(loginButtonListener);
		registerLandlordButton.setOnClickListener(registerLandlordButtonListener);
		registerTenantButton.setOnClickListener(registerTenantButtonListener);
	}
	
	// makes hitting the back
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch(keyCode)
		{
			case KeyEvent.KEYCODE_BACK:
			{
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);           
				return false;
			}
		}
	    return super.onKeyDown(keyCode, event);
	}

	public OnClickListener registerTenantButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			Intent intent = new Intent(LogIn.this, Tenant_Register_Activity.class);
			intent.putExtra("type", "t");
			
			startActivity(intent);
		}
	};

	public OnClickListener registerLandlordButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			Intent intent = new Intent(LogIn.this, Landlord_Register_Activity.class);
			intent.putExtra("type", "l");
			
			startActivity(intent);
		}
	};

	public OnClickListener loginButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if (emailET.getText().length() > 0 && passwordET.getText().length() > 0)
			{
				new AttemptLogin().execute();
			} 
			else
			{
				// Create an alert dialog box
				AlertDialog.Builder builder = new AlertDialog.Builder(
						LogIn.this);

				// Set alert title
				builder.setTitle(R.string.login_error);

				// Set the value for the positive reaction from the user
				// You can also set a listener to call when it is pressed
				builder.setPositiveButton(R.string.ok, null);

				// The message
				builder.setMessage(R.string.error_invalid_email_or_password);

				// Create the alert dialog and display it
				AlertDialog theAlertDialog = builder.create();
				theAlertDialog.show();
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.log_in, menu);
		return true;
	}

	class AttemptLogin extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pDialog = new ProgressDialog(LogIn.this);
			pDialog.setMessage("Attempting login...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args)
		{
			String username = emailET.getText().toString().toLowerCase();
			String password = passwordET.getText().toString();
			
			try
			{
				// Building Parameters
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("email", username));
				params.add(new BasicNameValuePair("password", password));
				
				Log.d("request!", "starting");
				// getting product details by making HTTP request
				JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST",
						params);

				Log.d("Login attempt", json.toString());
	
				if (json.getInt(TAG_SUCCESS) == 1) 
				{
					Log.d("Login Successful!", json.toString());
					return json.getString("type");
					
				} 
				else 
				{
					Log.d("Login Failure!", json.toString());
					//errorMsg(R.string.login_error, R.string.error_invalid_email_or_password);
					return "blank";

				}
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}

			return "blank";
		}

		protected void onPostExecute(String message)
		{
			// dismiss the dialog once product deleted
			String status = "Loading...";
			
			pDialog.dismiss();
			String email = emailET.getText().toString().toLowerCase();
			String password = passwordET.getText().toString();				
			
			if(message.equals("tenant"))
			{
				Toast.makeText(LogIn.this, status, Toast.LENGTH_SHORT).show();
				tenantHome(email, password);
			}
					
			else if(message.equals("landlord"))
			{
				Toast.makeText(LogIn.this, status, Toast.LENGTH_SHORT).show();
				landlordHome(email, password);
			}
			else
			{
				Toast.makeText(LogIn.this, "Login Failed. Please Enter a valid Email and Password.", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	protected void landlordHome(String email, String pass)
	{
		Intent intent = new Intent(LogIn.this,
		LandlordHomepage.class);
		intent.putExtra("EMAIL",email);
		intent.putExtra("PASSWORD",pass);
		
		startActivity(intent);
	};
	
	protected void tenantHome(String email, String pass)
	{
		Intent intent = new Intent(LogIn.this, TenantHomepage.class);
		intent.putExtra("EMAIL",email);
		intent.putExtra("PASSWORD",pass);
		startActivity(intent);
	};
	
	public void errorMsg (int title, int msg)
	{
		// Create an alert dialog box
		AlertDialog.Builder builder = new AlertDialog.Builder(LogIn.this);
		
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