package com.ezrent.ezrent;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class ViewImage extends Activity
{
	Button back;
	ImageView picture;
	
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
	
    private Bitmap imageBM;
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_image);
		
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
		//imageBM = (Bitmap) getIntent().getParcelableExtra("IMAGE_BITMAP");
		
		back = (Button) findViewById(R.id.image_back_Button);
		picture = (ImageView) findViewById(R.id.ViewImage_ImageView);
		
		back.setOnClickListener(backListener);
		//picture.setImageBitmap(imageBM);
		
	}
	
	public OnClickListener backListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			Intent intent = new Intent(ViewImage.this, ReadMessage.class);
			intent.putExtra("EMAIL",email);
			intent.putExtra("PASSWORD",pass);
			intent.putExtra("REQUESTID", requestID);
			intent.putExtra("TEMAIL", tenant_email);
			intent.putExtra("FIRSTNAME", tenant_first_name);
			intent.putExtra("LASTNAME", tenant_last_name);
			intent.putExtra("UNIT", unit_number);
			intent.putExtra("SUBJECT", subject);
			intent.putExtra("MESSAGE", message);
			intent.putExtra("PNAME", property_name);
			intent.putExtra("TENANT_PHONE", tenant_phone);
			intent.putExtra("IMAGE", image);
			intent.putExtra("ROTATE", rotate);
			
			startActivity(intent);
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_image, menu);
		return true;
	}
};
