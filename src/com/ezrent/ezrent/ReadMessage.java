package com.ezrent.ezrent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.util.Linkify;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class ReadMessage extends Activity
{
	private String email;
	private Integer requestID;
	private String tenant_email;
	private String tenant_first_name;
	private String tenant_last_name;
	private String pass;
	private String unit_number;
	private String subject;
	private String message;
	private String property_name;
	private String tenant_phone;
	private String image;
	private String rotate;
	
	TextView attached_TV;
	TextView attached_TV2;
	ImageView preview;
	
	Bitmap imageBM;
	
	LinearLayout layout;
	
	FileOutputStream out;
	
	TextView tenant_emailTV;
	TextView tenant_first_nameTV;
	TextView tenant_last_nameTV;
	TextView unit_numberTV;
	TextView subjectTV;
	TextView messageTV;
	TextView property_nameTV;
	TextView message_numberTV;
	TextView tenant_phoneTV;
	
	Button inbox, reply, delete;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.read_message);
		
		email = getIntent().getExtras().getString("EMAIL");
		pass = getIntent().getExtras().getString("PASSWORD");
		requestID = getIntent().getExtras().getInt("REQUESTID");
		tenant_email = getIntent().getExtras().getString("TEMAIL");
		tenant_first_name = getIntent().getExtras().getString("FIRSTNAME");
		tenant_last_name = getIntent().getExtras().getString("LASTNAME");
		unit_number = getIntent().getExtras().getString("UNIT");
		subject = getIntent().getExtras().getString("SUBJECT");
		message = getIntent().getExtras().getString("MESSAGE");
		property_name = getIntent().getExtras().getString("PNAME");
		tenant_phone = getIntent().getExtras().getString("TENANT_PHONE");
		image = getIntent().getExtras().getString("IMAGE");
		rotate = getIntent().getExtras().getString("ROTATE");
		
		tenant_emailTV = (TextView) findViewById(R.id.message_email_TextView);
		tenant_first_nameTV = (TextView) findViewById(R.id.message_first_name_TextView);
		tenant_last_nameTV = (TextView) findViewById(R.id.message_last_name_TextView);
		unit_numberTV = (TextView) findViewById(R.id.message_unit_number_TextView);
		subjectTV = (TextView) findViewById(R.id.message_subject_display_TextView);
		messageTV = (TextView) findViewById(R.id.message_message_text_TextView);
		property_nameTV = (TextView) findViewById(R.id.message_property_name_TextView);
		tenant_phoneTV = (TextView) findViewById(R.id.message_tenant_phone_number_TextView);
		attached_TV = (TextView) findViewById(R.id.inbox_attached_image_TV);
		attached_TV2 = (TextView) findViewById(R.id.inbox_attached_image_TV2);
		
		preview = (ImageView) findViewById(R.id.inbox_picture_preview);
		
		layout = (LinearLayout) findViewById(R.id.read_message_linearlayout);
		
		tenant_emailTV.setText(tenant_email);
		tenant_first_nameTV.setText(tenant_first_name);
		tenant_last_nameTV.setText(tenant_last_name);
		unit_numberTV.setText(unit_number);
		subjectTV.setText(subject);
		messageTV.setText(message);
		property_nameTV.setText(property_name);
		tenant_phoneTV.setText(tenant_phone);
		
		Linkify.addLinks(tenant_phoneTV, Linkify.PHONE_NUMBERS);
		
		inbox = (Button) findViewById(R.id.message_inbox_Button);
		reply = (Button) findViewById(R.id.message_home_Button);
		delete = (Button) findViewById(R.id.message_delete_Button);
		
		inbox.setOnClickListener(inboxButtonListener);
		delete.setOnClickListener(deleteButtonListener);
		reply.setOnClickListener(homeButtonListener);
		
		Linkify.addLinks(tenant_emailTV, Linkify.EMAIL_ADDRESSES);
		
		if(image.length() == 0)
		{
			attached_TV.setVisibility(View.GONE);
			attached_TV2.setVisibility(View.GONE);
			preview.setVisibility(View.GONE);
		}
		else
		{
			preview.setOnClickListener(previewButtonListener);
		}
		
		new tagMessage().execute(requestID);
	}

    public Object fetch(String address) throws MalformedURLException,IOException
    {
        URL url = new URL(address);
        Object content = url.getContent();
        return content;
    }
    
    public OnClickListener previewButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
		    View popupView = layoutInflater.inflate(R.layout.image_popup, null);
		    
		    int width = layout.getWidth() - 15;
		    int height = layout.getHeight() - 15;
		    
		    final PopupWindow popupWindow = new PopupWindow(popupView, width, height); 
		    
			ImageView x_button = (ImageView)popupView.findViewById(R.id.x_image_button);
			ImageView bigPic = (ImageView)popupView.findViewById(R.id.image_big_view);
			Button download = (Button)popupView.findViewById(R.id.download_Button);
			bigPic.setImageBitmap(imageBM);
			     
			x_button.setOnClickListener(new Button.OnClickListener()
			{
			     @Override
			     public void onClick(View v)
			     {
			    	 popupWindow.dismiss();
			     }
			});
			
			download.setOnClickListener(new Button.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					try
					{
						boolean down = false;
						Bitmap bmp = imageBM;
						String path = Environment.getExternalStorageDirectory().toString() + "/EZ-Rent/";
						String image_name = image.substring(image.lastIndexOf("/") + 1,image.length());
						File dir = new File(path);
						if(dir.exists())
						{
							down = true;
						}
						else if(dir.mkdirs())
						{
							down = true;
						}
						else
						{
							Toast.makeText(ReadMessage.this, "Could not make directory. File not downloaded.", Toast.LENGTH_LONG).show();
						}
						
						if(down == true)
						{
							File file = new File(dir, image_name);
							out = new FileOutputStream(file);
							bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
							MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
							out.flush();
							out.close();
							Toast.makeText(ReadMessage.this, "File downloaded to:\n" + path + image_name, Toast.LENGTH_SHORT).show();
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					finally
					{
						try
						{
							out.close();
						}
						catch(Throwable ignore) {}
					}
			     }
			});
			
			popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
		}
	};
	
	public OnClickListener homeButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			Intent intent = new Intent(ReadMessage.this, LandlordHomepage.class);
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
			new deleteMessage().execute(requestID);
		}
	};
	
	public OnClickListener inboxButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			Intent intent = new Intent(ReadMessage.this, LandlordInbox.class);
			intent.putExtra("EMAIL",email);
			intent.putExtra("PASSWORD",pass);
			startActivity(intent);
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.read_message, menu);
		return true;
	}
	
	private class deleteMessage extends AsyncTask<Integer, String, Integer>
	{
        private static final String LOGIN_URL = "http://www.ezrentdata.com/webservice/index.php";
       
        private ProgressDialog pDialog;
        private String request = "deleteMessage";
       
        private JSONParser jsonParser = new JSONParser();
       
        @Override
        protected void onPreExecute()
        {
        	super.onPreExecute();
        	pDialog = new ProgressDialog(ReadMessage.this);
        	pDialog.setMessage("Deleting Message...");
        	pDialog.setIndeterminate(false);
        	pDialog.setCancelable(true);
        	pDialog.show();
        }
       
        @Override
        protected Integer doInBackground(Integer... args)
        {
        	List<NameValuePair> params = new ArrayList<NameValuePair>();
        	params.add(new BasicNameValuePair("email", email));
        	params.add(new BasicNameValuePair("password", pass));
        	params.add(new BasicNameValuePair("request", request));
        	params.add(new BasicNameValuePair("idreq", Integer.toString(args[0])));
        	try
        	{
        		Log.d("deleteMessage", "started");
        		JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);
        		Log.d("deleteMessage response", json.toString());
               
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
        		Toast.makeText(ReadMessage.this, "Message Deleted.", Toast.LENGTH_SHORT).show();
        	}
        	else if(message == 2)
        	{
        		Toast.makeText(ReadMessage.this, "Cannot delete message. Specified message does not exist.", Toast.LENGTH_LONG).show();
        	}
        	else
        	{
        		Toast.makeText(ReadMessage.this, "Message not deleted.", Toast.LENGTH_LONG).show();
        	}
        	
        	Intent intent = new Intent(ReadMessage.this, LandlordInbox.class);
        	intent.putExtra("EMAIL", email);
        	intent.putExtra("PASSWORD",pass);
        	startActivity(intent);      
        }
	}
	
	private class tagMessage extends AsyncTask<Integer, String, Integer>
	{
		private static final String LOGIN_URL = "http://www.ezrentdata.com/webservice/index.php";
		private ProgressDialog pDialog;
		private String request = "tagMessage";
		private JSONParser jsonParser = new JSONParser();
		private Drawable d;
		
		@Override
        protected void onPreExecute()
        {
			super.onPreExecute();
			pDialog = new ProgressDialog(ReadMessage.this);
			pDialog.setMessage("Loading Message....");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
        }
       
		@Override
		protected Integer doInBackground(Integer... args)
		{
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("email", email));
			params.add(new BasicNameValuePair("password", pass));
			params.add(new BasicNameValuePair("request", request));
			params.add(new BasicNameValuePair("idreq", Integer.toString(args[0])));
			
			try
			{
				Log.d("tagMessage", "started");
				JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);
				Log.d("tagMessage response", json.toString());
				
				if(image.length() > 0)
				{
					image = "http://ezrentdata.com/uploads/" + image;

					try
					{
						InputStream inputStream = (InputStream) new URL(image).getContent();
						d = Drawable.createFromStream(inputStream, "srcname");
						imageBM = drawableToBitmap(d);
						// checks to see if image needs to be rotated
						if(rotate.equalsIgnoreCase("yes"))
						{
							Matrix matrix = new Matrix();
							matrix.postRotate(90);
							imageBM = Bitmap.createBitmap(imageBM, 0, 0, imageBM.getWidth(), imageBM.getHeight(), matrix, true);
						}
			        }
					catch (MalformedURLException e)
					{
						pDialog.setMessage("Error loading image.");
			        }
					catch (IOException e)
					{
						pDialog.setMessage("Error loading image.");
			        }
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
			// if there is an image, set the preview to it
			if(image.length() > 0)
			{
				preview.setImageBitmap(imageBM);
			}
			
			pDialog.dismiss();               
		}
		
		// used to convert a drawable to a bitmap
		public Bitmap drawableToBitmap (Drawable drawable)
		{
		    if (drawable instanceof BitmapDrawable) {
		        return ((BitmapDrawable)drawable).getBitmap();
		    }

		    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
		    Canvas canvas = new Canvas(bitmap); 
		    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		    drawable.draw(canvas);

		    return bitmap;
		}
	}
}
