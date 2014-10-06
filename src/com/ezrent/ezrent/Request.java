package com.ezrent.ezrent;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class Request extends Activity
{
	// tenant writes in these
	EditText subject_ET;
	EditText message_ET;
	
	TextView attached_TV;
	TextView attached_TV2;
	
	Button cancel;
	Button send;
	
	String unit_number;
	String property_name;
	String tenant_first_name;
	String tenant_last_name;
	String landlord_email;
	String message;
	String subject;
	String image_str;
	String imgPath;
	
	ImageView camera_button;
	ImageView preview;
	ImageView x_button;
	
	private String email;
	private String pass;
	private static final int SELECT_PICTURE = 1;
	private Uri outputFileUri;
	
	boolean attach = false;
	boolean rotate = false;
	
	private Bitmap image;
	
	private String upLoadServerUri = null;
    private String imagepath = null;
    private String image_name = null;
    
    private int serverResponseCode = 0;
    
    private ProgressDialog dialog = null;
    
    FileOutputStream out;
    
    LinearLayout layout;
    
    // used for clearing text instructions on focus changes
    // between subject and message text fields
    int count;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.request);
		
		// gets information passed from previous activity
		email = getIntent().getExtras().getString("EMAIL");
		pass = getIntent().getExtras().getString("PASSWORD");
		unit_number = getIntent().getExtras().getString("UNIT_NUMBER");
		property_name = getIntent().getExtras().getString("PROPERTY_NAME");	
		landlord_email = getIntent().getExtras().getString("LANDLORD_EMAIL");
		tenant_first_name = getIntent().getExtras().getString("TENANT_FIRST_NAME");
		tenant_last_name = getIntent().getExtras().getString("TENANT_LAST_NAME");
		
		subject_ET = (EditText) findViewById(R.id.request_subject_EditText);
		message_ET = (EditText) findViewById(R.id.request_message_EditText);
		
		attached_TV = (TextView) findViewById(R.id.attached_image_TV);
		attached_TV2 = (TextView) findViewById(R.id.attached_image_TV2);
		
		cancel = (Button) findViewById(R.id.request_cancel_Button);
		send = (Button) findViewById(R.id.request_send_Button);
		
		camera_button = (ImageView) findViewById(R.id.camera_button);
		preview = (ImageView) findViewById(R.id.picture_preview);
		x_button = (ImageView) findViewById(R.id.x_preview_button);
		
		attached_TV.setVisibility(View.GONE);
		attached_TV2.setVisibility(View.GONE);
		preview.setVisibility(View.GONE);
		x_button.setVisibility(View.GONE);
		
		cancel.setOnClickListener(cancelListener);
		x_button.setOnClickListener(xListener);
		send.setOnClickListener(sendListener);
		preview.setOnClickListener(previewButtonListener);
		
		layout = (LinearLayout) findViewById(R.id.request_linearlayout);
		
		// listener for choosing/taking a picture
		camera_button.setOnClickListener(new OnClickListener()
		{
            public void onClick(View v)
            {
                final File root = new File(Environment.getExternalStorageDirectory()
                					+ File.separator + "MyDir" + File.separator);
                
                root.mkdirs();

                // Camera.
                final List<Intent> cameraIntents = new ArrayList<Intent>();
                final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                final PackageManager packageManager = getPackageManager();
                final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
                for(ResolveInfo res : listCam)
                {
                	final String packageName = res.activityInfo.packageName;
                	final Intent intent = new Intent(captureIntent);
                	intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                	intent.setPackage(packageName);
                	intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                	cameraIntents.add(intent);
                }

                // Filesystem.
                final Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                // Chooser of filesystem options.
                final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

                // Add the camera options.
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));

                startActivityForResult(chooserIntent, SELECT_PICTURE);
            }
		});
		
		subject_ET.setCursorVisible(false);
		count = 0;
		subject_ET.setOnFocusChangeListener(SUBlistener);
		message_ET.setOnFocusChangeListener(MESlistener);
		subject_ET.setOnClickListener(subjectListener);
		upLoadServerUri = "http://ezrentdata.com/uploads/UploadToServer.php";
	}
	
	public OnClickListener previewButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			// adds pop-up to display the image full sized
			LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
		    View popupView = layoutInflater.inflate(R.layout.image_popup, null);
		    
		    int width = layout.getWidth() - 15;
		    int height = layout.getHeight() - 15;
		    
		    final PopupWindow popupWindow = new PopupWindow(popupView, width, height); 
		    
			ImageView x_button = (ImageView)popupView.findViewById(R.id.x_image_button);
			ImageView bigPic = (ImageView)popupView.findViewById(R.id.image_big_view);
			Button download = (Button)popupView.findViewById(R.id.download_Button);
			
			download.setVisibility(View.GONE);
			
			bigPic.setImageBitmap(image);
			     
			x_button.setOnClickListener(new Button.OnClickListener()
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
	
	// used so if a user deletes "Subject" from the edit text and changes focus
	// it will reappear and be a lighter color
	public OnFocusChangeListener SUBlistener = new OnFocusChangeListener()
	{
		@Override
		public void onFocusChange(View arg0, boolean arg1)
		{
			if(count >= 2)
			{
				subject_ET.setCursorVisible(true);
				
				if(subject_ET.getText().length() == 0)
				{
					subject_ET.setText("Subject");
					subject_ET.setTextColor(Color.parseColor("#66000000"));
				}
				else if(subject_ET.getText().toString().equals("Subject"))
				{
					subject_ET.setText("");
					subject_ET.setTextColor(Color.parseColor("#000000"));
				}
			}
			
			++count;
		}
	};
	
	// used so if a user deletes the message instructions from the edit text and changes focus
	// it will reappear and be a lighter color
	public OnFocusChangeListener MESlistener = new OnFocusChangeListener()
	{
		@Override
		public void onFocusChange(View arg0, boolean arg1)
		{		
			if(message_ET.getText().length() == 0)
			{
				message_ET.setText("Detailed description of the problem.");
				message_ET.setTextColor(Color.parseColor("#66000000"));
			}
			else if(message_ET.getText().toString().equals("Detailed description of the problem."))
			{
				message_ET.setText("");
				message_ET.setTextColor(Color.parseColor("#000000"));
			}
			
			++count;
		}
	};
	
	// if a user taps the edit text it will delete the subject title so they
	// can immediately start typing
	public OnClickListener subjectListener = new OnClickListener()
	{
		@SuppressLint("ResourceAsColor")
		@Override
		public void onClick(View arg0)
		{
			if(count < 2)
			{
				subject_ET.setCursorVisible(true);
				subject_ET.setText("");
				subject_ET.setTextColor(Color.parseColor("#000000"));
				count++;
			}
		}
	};
	
	// goes back to tenant home page
	public OnClickListener cancelListener = new OnClickListener()
	{
		@Override
		public void onClick(View arg0)
		{
			Intent intent = new Intent(Request.this, TenantHomepage.class);
			intent.putExtra("REGISTER", "NO");
			intent.putExtra("EMAIL", email);
			intent.putExtra("PASSWORD", pass);
			startActivity(intent);
		}		
	};
	
	// removes the attached image
	public OnClickListener xListener = new OnClickListener()
	{
		@Override
		public void onClick(View arg0)
		{
			attached_TV.setVisibility(View.GONE);
			attached_TV2.setVisibility(View.GONE);
			preview.setVisibility(View.GONE);
			x_button.setVisibility(View.GONE);
			attach = false;
			image_name = null;
			rotate = false;
		}		
	};
	
	// once a picture has been taken/chosen
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode == RESULT_OK)
		{
			Uri selectedImageUri = null;
			if(requestCode == SELECT_PICTURE)
			{
				final boolean isCamera;
				if(data == null)
				{
					isCamera = true;
				}
				else
				{
					final String action = data.getAction();
					if(action == null)
					{
						isCamera = false;
					}
					else
					{
						isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
					}
				}

				if(isCamera)
				{
					selectedImageUri = outputFileUri;
				}
				else
				{
					selectedImageUri = data == null ? null : data.getData();
				}
			}
			try
			{
				imagepath = getPath(selectedImageUri);
				
				image_name = imagepath.substring(imagepath.lastIndexOf("/") + 1,imagepath.length());
				image_name = image_name.replaceAll(" ", "-");
				image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
				
				// determines if camera rotated it or not
				if(getOrientation(selectedImageUri) == 90)
				{
					rotate = true;
					Matrix matrix = new Matrix();
					matrix.postRotate(90);
					image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
				}
				
				attach = true;
				attached_TV.setVisibility(View.VISIBLE);
				attached_TV2.setVisibility(View.VISIBLE);
				preview.setVisibility(View.VISIBLE);
				x_button.setVisibility(View.VISIBLE);
				
				preview.setImageBitmap(image);
			}
			
			catch (Exception e){};
        }
	}
	
	// used to get a string of the path to where the image is on the device
	public String getPath(Uri uri)
	{
        Cursor cursor = getContentResolver().query(uri, 
        			 new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        
        return cursor.getString(column_index);
    }
	
	// function to determine if the camera rotated the image
	public int getOrientation(Uri selectedImage)
	{
	    int orientation = 0;
	    final String[] projection = new String[]{MediaStore.Images.Media.ORIENTATION};      
	    final Cursor cursor = this.getContentResolver().query(selectedImage, projection, null, null, null);
	    if(cursor != null)
	    {
	        final int orientationColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);
	        if(cursor.moveToFirst())
	        {
	            orientation = cursor.isNull(orientationColumnIndex) ? 0 : cursor.getInt(orientationColumnIndex);
	        }
	        cursor.close();
	    }
	    
	    return orientation;
	}
	
	public OnClickListener sendListener = new OnClickListener()
	{
		@Override
		public void onClick(View arg0)
		{
			message = message_ET.getText().toString();
            subject = subject_ET.getText().toString();
            if(message.length() == 0 || subject.length() == 0)
            {
            	errorMsg(R.string.request_error, R.string.error_fields_left_blank);
            }
            else
            {
            	// if there is a picture attached creates new thread to upload it
            	// to the server
            	if(attach == true)
                {
                	dialog = ProgressDialog.show(Request.this, "", "Uploading file...", true);
                	
                	new Thread(new Runnable()
        			{
                        public void run()
                        {          
                             uploadFile(imagepath);                      
                        }
        			}).start();
                }
            	// task to actually make the maintenance request
            	new makeRequest().execute(subject, message);
            }
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.request, menu);
		return true;
	}
	
	private class makeRequest extends AsyncTask<String, String, Integer>
	{
        private static final String LOGIN_URL = "http://www.ezrentdata.com/webservice/index.php";
       
        private ProgressDialog pDialog;
        private String request = "postMaintenanceRequest";
       
        private JSONParser jsonParser = new JSONParser();
        String r = "no";
        
        @Override
        protected void onPreExecute()
        {
                super.onPreExecute();
                pDialog = new ProgressDialog(Request.this);
                pDialog.setMessage("Posting maintenance request...");
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
        	params.add(new BasicNameValuePair("subject", args[0]));
        	params.add(new BasicNameValuePair("problem", args[1]));
        	params.add(new BasicNameValuePair("image", image_name));
        	// checks to see if image needs to be rotated
        	// this is used when the landlord views the image
        	// r was initially set to "no"
        	if(rotate == true)
        	{
        		r = "yes";
        	}
        	params.add(new BasicNameValuePair("rotate", r));
        	
        	try
        	{
        		Log.d("postMaintenanceRequest", "started");
        		JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);
        		Log.d("addNote response", json.toString());
        		
        		// has the value of whether it was successful or not
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
        		Toast.makeText(Request.this, "Maintenance Request Posted.", Toast.LENGTH_SHORT).show();
        		// goes back to tenants home page upon completion
            	Intent intent = new Intent(Request.this, TenantHomepage.class);
            	intent.putExtra("EMAIL", email);
            	intent.putExtra("PASSWORD", pass);
            	startActivity(intent);
        	}
        	else if(message == 2)
        	{
        		Toast.makeText(Request.this, "Tenant is not engaged in any Lease. No Maintenance Request can be posted.",
        				Toast.LENGTH_LONG).show();
        	}
        	else
        	{
        		Toast.makeText(Request.this, "Connection problem, Maintenance Request Not Added", Toast.LENGTH_LONG).show();
        	}
        }
	}
	
	// function for uploading the file
	// takes in the path on the device to the file
	public int uploadFile(String sourceFileUri)
	{
		String fileName = sourceFileUri;
		
		HttpURLConnection conn = null;
		DataOutputStream dos = null;  
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024; 
		File sourceFile = new File(sourceFileUri); 
         
		if (!sourceFile.isFile())
		{
			dialog.dismiss();   
			Log.e("uploadFile", "Source File not exist :" + imagepath); 
             return 0;     
		}
		else
		{
			try
			{ 
				// open a URL connection to the Servlet
				FileInputStream fileInputStream = new FileInputStream(sourceFile);
				URL url = new URL(upLoadServerUri);
                  
				// Open a HTTP  connection to  the URL
				conn = (HttpURLConnection) url.openConnection(); 
				conn.setDoInput(true); // Allow Inputs
				conn.setDoOutput(true); // Allow Outputs
				conn.setUseCaches(false); // Don't use a Cached Copy
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("ENCTYPE", "multipart/form-data");
				conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
				conn.setRequestProperty("uploaded_file", fileName);
				conn.setRequestProperty("tenant_email", email);
                 
				dos = new DataOutputStream(conn.getOutputStream());
        
				dos.writeBytes(twoHyphens + boundary + lineEnd); 
				dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);
                  
				dos.writeBytes(lineEnd);
        
				// create a buffer of  maximum size
				bytesAvailable = fileInputStream.available(); 
				
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];
        
				// read file and write it into form...
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);  
                    
				while (bytesRead > 0)
				{
					dos.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);   
				}
        
				// send multipart form data necesssary after file data...
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
        
				// Responses from the server (code and message)
				serverResponseCode = conn.getResponseCode();
				String serverResponseMessage = conn.getResponseMessage();
                   
				Log.i("uploadFile", "HTTP Response is : "
                         + serverResponseMessage + ": " + serverResponseCode);
                  
				if(serverResponseCode == 200)
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
										+" http://ezrentdata.com/uploads/"
										+imagepath;
							
							message_ET.setText(msg);
							Toast.makeText(Request.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();
						}
					});                
				}    
				
				// close the streams
				fileInputStream.close();
				dos.flush();
				dos.close();        
            }
			catch (MalformedURLException ex)
			{
				dialog.dismiss();  
				ex.printStackTrace();
				
				runOnUiThread(new Runnable()
				{
                    public void run()
                    {
                    	message_ET.setText("MalformedURLException Exception : check script url.");
                    	Toast.makeText(Request.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
                    }
				});
                 
				Log.e("Upload file to server", "error: " + ex.getMessage(), ex);  
			}
			catch (Exception e)
			{ 
				dialog.dismiss();  
				e.printStackTrace();
                 
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						message_ET.setText("Got Exception : see logcat ");
						Toast.makeText(Request.this, "Got Exception : see logcat ", Toast.LENGTH_SHORT).show();
					}
				});
				
				Log.e("Upload file to server Exception", "Exception : " + e.getMessage(), e);  
            }
			
			dialog.dismiss();   
            
			return serverResponseCode;         
        } // End else block 
	}
	
	// function used for displaying error messages to users
	public void errorMsg (int title, int msg)
	{
		// Create an alert dialog box
		AlertDialog.Builder builder = new AlertDialog.Builder(Request.this);
		
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
