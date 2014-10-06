package com.ezrent.ezrent;
import android.widget.Toast;
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
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.app.ProgressDialog;
public class Register_Activity extends Activity {
	
	Button tenant_registerButton;
	Button cancel;
	
	EditText tenant_firstName;
	EditText tenant_lastName;
	EditText tenant_email;
	EditText tenant_password;
	EditText tenant_confirmPassword;
	EditText tenant_unit_number;
	
	private ProgressDialog pDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tenant_register);
		
		// Button Initialization
		tenant_registerButton = (Button) findViewById(R.id.tenant_register_Button);
		cancel = (Button) findViewById(R.id.tenant_cancel_Button);
		
		// EditText Initialization
		tenant_firstName = (EditText) findViewById(R.id.tenant_first_name_EditText);
		tenant_lastName = (EditText) findViewById(R.id.tenant_last_name_EditText);
		tenant_email = (EditText) findViewById(R.id.tenant_email_EditText);
		tenant_password = (EditText) findViewById(R.id.tenant_password_EditText);
		tenant_confirmPassword = (EditText) findViewById(R.id.tenant_confirm_password_EditText);
		
		//This does not get added here.
		//tenant_unit_number = (EditText) findViewById(R.id.tenant_unit_number_EditText);
				
		// Add ClickListeners to the button
		tenant_registerButton.setOnClickListener(tenant_registerButtonListener);
		cancel.setOnClickListener(cancelButtonListener);
	}
	
	public OnClickListener cancelButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View arg0)
		{
			// TODO Auto-generated method stub
			Intent intent = new Intent(Register_Activity.this, LogIn.class);
			startActivity(intent);
		}
		
	};
	
	public OnClickListener tenant_registerButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View arg0) {
			if(tenant_firstName.getText().length() > 0 && tenant_lastName.getText().length() > 0
					&& tenant_email.getText().length() > 0 && tenant_password.getText().length() > 0 
					&& tenant_confirmPassword.getText().length() > 0)
			{
				if(tenant_confirmPassword.getText().toString().equals(tenant_password.getText().toString()) == false)
				{
					errorMsg (R.string.register_error, R.string.error_passwords_do_not_match);
				}
				
				else if(tenant_password.getText().length() < 5)
				{
					errorMsg (R.string.register_error, R.string.error_password_length);
				}
				
				else if(isEmailValid(tenant_email.getText()) == false)
				{
					errorMsg(R.string.register_error, R.string.error_invalid_email);
				}
						
				else
				{
					String type = getIntent().getExtras().getString("type");
					
					new registerTenant().execute(tenant_email.getText().toString(), tenant_password.getText().toString(), 
								tenant_firstName.getText().toString(), tenant_lastName.getText().toString(), type);
					
					Intent intent = new Intent(Register_Activity.this, LogIn.class);
					startActivity(intent);
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
		AlertDialog.Builder builder = new AlertDialog.Builder(Register_Activity.this);
		
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
		getMenuInflater().inflate(R.menu.tenant__register_, menu);
		return true;
	}

	class registerTenant extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(Register_Activity.this);
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
					params.add(new BasicNameValuePair("request", "register"));
				
					// getting product details by making HTTP request
					json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);
					
					
					if(json.getInt("success") == 1)
					{
						return json.getString("message");
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
			if (message != null) {
				Toast.makeText(Register_Activity.this, message, Toast.LENGTH_LONG).show();
			}
		}
	
}
}
