package com.ezrent.ezrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class LandlordInbox extends Activity
{
	private String email;
	private String pass;
	
	Button home;
	
	RadioGroup sortRadioGroup;
	
	RadioButton sortDate;
	RadioButton sortProperty;
	RadioButton sortRead;
	
	// this is the main array to use (similar to the properties array in the LandlordHomepage activity
	message[] messages;
	
	// fills these arrays with database info
	String[] 	property_name;
	String[]	subjects;
	String[]	message_date;
	int[] 		requestID;
	boolean[]	read;
	boolean messages_exist;
	
	private TableLayout requestTableScrollView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.landlord_inbox);
		
		email = getIntent().getExtras().getString("EMAIL");
		pass = getIntent().getExtras().getString("PASSWORD");
		
		sortRadioGroup = (RadioGroup) findViewById(R.id.sort_radioGroup);
		
		sortDate = (RadioButton) findViewById(R.id.sort_radio_date);
		sortProperty = (RadioButton) findViewById(R.id.sort_radio_property);
		sortRead =  (RadioButton) findViewById(R.id.sort_radio_read);
		
		home = (Button) findViewById(R.id.inbox_home_Button);
		
		home.setOnClickListener(homeButtonListener);
		
		requestTableScrollView = (TableLayout) findViewById(R.id.request_TableScrollView);
		
		messages_exist = false;
		
		new getRequests().execute();
		addChangeListenerToRadios();
	}
	
	class getRequests extends AsyncTask<String, String, Integer>
	{
		private static final String LOGIN_URL = "http://www.ezrentdata.com/webservice/index.php";
		
		private ProgressDialog pDialog;
		private String request = "getMessages";
		private JSONParser jsonParser = new JSONParser();
		
		private JSONArray msg = new JSONArray();
		private JSONArray pname = new JSONArray();
		private JSONArray tenant = new JSONArray();
		private JSONArray postdate = new JSONArray();
		private JSONArray ids = new JSONArray();
		private JSONArray seen = new JSONArray();
		private JSONArray subject = new JSONArray();
		private JSONArray fname = new JSONArray();
		private JSONArray lname = new JSONArray();
		private JSONArray apno = new JSONArray();
		private JSONArray tphone = new JSONArray();
		private JSONArray image = new JSONArray();
		private JSONArray rotate = new JSONArray();

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pDialog = new ProgressDialog(LandlordInbox.this);
			pDialog.setMessage("Maintenance Requests");
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
				
				// getting product details by making HTTP request
				Log.d("getRequests", "started");
				JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);
				Log.d("getRequests Info", json.toString());
				
				if(json.getInt("success") == 1)
				{
					JSONObject func = json.getJSONObject(request);
					
					msg = func.getJSONArray("PROBLEM");
					pname = func.getJSONArray("PNAME");
					tenant = func.getJSONArray("TEMAIL");
					postdate = func.getJSONArray("POSTDATE");
					ids = func.getJSONArray("ID");
					seen = func.getJSONArray("SEEN");
					subject = func.getJSONArray("SUBJECT");
					fname = func.getJSONArray("FIRSTNAME");
					lname = func.getJSONArray("LASTNAME");
					apno = func.getJSONArray("APARTMENTNO");
					tphone = func.getJSONArray("TENANT_PHONE");
					image = func.getJSONArray("IMAGE");
					rotate = func.getJSONArray("ROTATE");
					
					messages = new message[msg.length()];
					for(int i = 0; i < msg.length(); i++)
					{	
						messages[i] = new message(msg.getString(i),pname.getString(i), subject.getString(i), 
									postdate.getString(i), Integer.parseInt(ids.getString(i)), Integer.parseInt(seen.getString(i)),
									tenant.getString(i), fname.getString(i), lname.getString(i), apno.getString(i), tphone.getString(i), 
									image.getString(i), rotate.getString(i));
					}
					
					return 1;
				}
				else if(json.getInt("success") == 2)
				{
					return 2;
				}
				else
				{
					return 0;
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
				Toast.makeText(LandlordInbox.this, "Loaded Messages.", Toast.LENGTH_SHORT).show();
				sortRadioGroup.check(R.id.sort_radio_date);
				messages_exist = true;
				updateRequestList(messages);
			}
			else if(message == 2)
			{
				Toast.makeText(LandlordInbox.this, "No messages.", Toast.LENGTH_SHORT).show();
				messages_exist = false;
			}
			else if(message == 0)
			{
				Toast.makeText(LandlordInbox.this, "Bad call to function. Parameters left blank.", Toast.LENGTH_SHORT).show();
				messages_exist = false;
			}
		}
	}
	
	public OnClickListener homeButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View arg0)
		{
			Intent intent = new Intent(LandlordInbox.this, LandlordHomepage.class);
			intent.putExtra("EMAIL",email);
			intent.putExtra("PASSWORD",pass);
			startActivity(intent);
		}
		
	};
	
	private void updateRequestList(message [] Messages)
	{
		// sort properties in alphabetical order
		Arrays.sort(Messages);
		
		int i;
		for(i = 0; i < Messages.length; i++)
		{
			insertPropertyInScrollView(Messages[i], i);
		}
	}
	
	private void insertPropertyInScrollView(message Message, int arrayIndex)
	{
		// Get the LayoutInflator service
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View newPropertyRow;
		// Use the inflater to inflate a stock row from stock_quote_row.xml
		// Use different layout depending if message was read or not
		if(messages[arrayIndex].isRead() == 1)
		{
			newPropertyRow = inflater.inflate(R.layout.request_row, null);
		}
		else
		{
			newPropertyRow = inflater.inflate(R.layout.request_row_unread, null);
		}
		
		// Create the TextView for the ScrollView Row
		TextView new_dateTextView = (TextView) newPropertyRow.findViewById(R.id.request_row_date_TextView);
		TextView new_propertyTextView = (TextView) newPropertyRow.findViewById(R.id.request_row_property_TextView);
		TextView new_subjectTextView = (TextView) newPropertyRow.findViewById(R.id.request_row_subject_TextView);
		TextView new_message_numberTV = (TextView) newPropertyRow.findViewById(R.id.request_row_message_numberTV);
		
		// Add the property name to the TextView
		
		//do later to make rows  equal height
		/*property = Message.getProperty();
		if(property.length() > 12)
		{
			property.substring(0, 12);
			property += "...";
		}
		
		subj = Message.getSubject();
		if(subj.length() > 13)
		{
			subj.substring(0, 13);
			subj += "...";
		}
		
		new_propertyTextView.setText(property);
		new_subjectTextView.setText(subj);*/
		
		new_dateTextView.setText(Message.getDate());
		new_propertyTextView.setText(Message.getProperty());
		new_subjectTextView.setText(Message.getSubject());
		
		new_message_numberTV.setTag(arrayIndex);
		
		new_dateTextView.setOnClickListener(selectPropertyButtonListener);
		new_propertyTextView.setOnClickListener(selectPropertyButtonListener);
		new_subjectTextView.setOnClickListener(selectPropertyButtonListener);
		
		requestTableScrollView.addView(newPropertyRow, arrayIndex);
	}
	
	public OnClickListener selectPropertyButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			// Get the text saved in the TextView next to the clicked button
			// with the id stockSymbolTextView

			TableRow linearLayout = (TableRow) v.getParent();
			TextView message_numberTV = (TextView) linearLayout.findViewById(R.id.request_row_message_numberTV);
			int message_number = (Integer) message_numberTV.getTag();
			
            // An intent is an object that can be used to start another activity
            Intent intent = new Intent(LandlordInbox.this, ReadMessage.class);
            
            // Add the property to the intent
            intent.putExtra("EMAIL",email);
    		intent.putExtra("PASSWORD",pass);
            intent.putExtra("REQUESTID",messages[message_number].returnID());
            intent.putExtra("FIRSTNAME",messages[message_number].getFirstName());
            intent.putExtra("LASTNAME",messages[message_number].getLastName());
            intent.putExtra("TEMAIL",messages[message_number].gettenantemail());
            intent.putExtra("PNAME",messages[message_number].getProperty());
            intent.putExtra("UNIT",messages[message_number].getUnit());
            intent.putExtra("MESSAGE",messages[message_number].getMessage());
            intent.putExtra("SUBJECT",messages[message_number]. getSubject());
            intent.putExtra("TENANT_PHONE",messages[message_number].getTenantPhone());
            intent.putExtra("IMAGE",messages[message_number].getImage());
            intent.putExtra("ROTATE",messages[message_number].getRotate());
            
            startActivity(intent);
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.landlord_inbox, menu);
		return true;
	}
	
	private void addChangeListenerToRadios()
	{
		// Setting the listeners on the RadioGroups and handling them
		// in the same location
		
		sortRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() 
	    {
			// checkedId is the RadioButton selected
	        public void onCheckedChanged(RadioGroup group, int checkedId)
	        {
	        	if(messages_exist == true)
	        	{
		        	int i;
		        	requestTableScrollView.removeAllViews();
		        	for(i = 0; i < messages.length; i++)
		        	{
		        		if(sortDate.isChecked())
		        		{
		        			messages[i].sortBy(0);
		        		}
		        		
		        		else if(sortProperty.isChecked())
		        		{
		        			messages[i].sortBy(1);
		        		}
		        		
		        		else if(sortRead.isChecked())
		        		{
		        			messages[i].sortBy(2);
		        		}
		        	}
		        	
		        	updateRequestList(messages);
	        	}
	        }
	    });
	}
	
	class message implements Comparable<message>
	{
		// used to determine what to sort the message by
		// 0 == date (the default), 1 == property, 2 == read
		
		private String property;
		private String subject;
		private String DATE;
		private String message;
		private String[] date;
		private String firstname;
		private String lastname;
		private String unit;
		private String tenant_phone;
		private String tenantemail;
		private String image;
		private String rotate;
		
		private int sort;
		private int requestID;
		private int month;
		private int year;
		private int day;
		
		private int read;
		
		public message(String msg, String prop, String sub, String d8, int ID, int seen2, String temail,
					String fname, String lname, String apno, String tphone, String img, String rot)
		{
			sort = 0;
			property = prop;
			DATE = d8;
			date = d8.split("-");
			requestID = ID;
			read = seen2;
			subject = sub;
			message = msg;
			tenantemail = temail;
			image = img;
			rotate = rot;
					
			firstname = fname;
			lastname = lname;
			unit = apno;
			tenant_phone = tphone;
			year = Integer.parseInt(date[0]);
			month = Integer.parseInt(date[1]);
			day = Integer.parseInt(date[2]);
		}
		
		public String getMessage()
		{
			return message;
		}
		
		public String getRotate()
		{
			return rotate;
		}
		
		public String getImage()
		{
			if(image.length() <= 3)
			{
				return "";
			}
			else
			{
				return image;
			}
		}
		
		public String gettenantemail()
		{
			return tenantemail;
		}
		
		public String getDate()
		{
			return DATE;
		}
		
		public String getSubject()
		{
			return subject;
		}
		
		public String getProperty()
		{
			return property;
		}
		
		public int isRead()
		{
			return read;
		}
		
		public int returnID()
		{
			return requestID;
		}
		
		public String getFirstName()
		{
			return firstname;
		}
		
		public String getLastName()
		{
			return lastname;
		}
		
		public String getUnit()
		{
			return unit;
		}
		
		public String getTenantPhone()
		{
			String x = tenant_phone;
			x = "(" + x.substring(0, 3) + ") " + x.substring(3, 6) + "-" + x.substring(6, x.length());
			return x;
		}
		
		public void sortBy(int s)
		{
			sort = s;
		}
		
		public int compareDate(message x)
		{
			if(year < x.year)
			{
				return 1;
			}
			else if(year > x.year)
			{
				return -1;
			}
			else if(month < x.month)
			{
				return 1;
			}
			else if(month > x.month)
			{
				return -1;
			}
			else if(day < x.day)
			{
				return 1;
			}
			else if(day > x.day)
			{
				return -1;
			}
			else
			{
				return 0;
			}
		}

		@Override
		public int compareTo(message another)
		{
			// sort by date
			if(sort == 0)
			{
				return this.compareDate(another);
			}
			// sort by property (if same name sort by date)
			else if (sort == 1)
			{
				if(property.toUpperCase().compareTo(another.property.toUpperCase()) == 0)
				{
					return this.compareDate(another);
				}
				
				else return property.toUpperCase().compareTo(another.property.toUpperCase());
					
			}
			// sort by read (if both read/unread sort by date)
			else
			{
				if(read == 0 && another.read == 1)
				{
					return -2;
				}
				else if(read == 1  && another.read == 0)
				{
					return 1;
				}
				else
				{
					return this.compareDate(another);
				}
			}
		}	
	}
}
