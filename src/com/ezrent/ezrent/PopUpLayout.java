package com.ezrent.ezrent;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class PopUpLayout extends LinearLayout
{
	public PopUpLayout(Context context)
	{
        super(context);
    }
	
	public PopUpLayout(Context context, AttributeSet attrs, int defStyle)
	{
        super(context, attrs, defStyle);
    }
	
	public PopUpLayout(Context context, AttributeSet attrs)
	{
        super(context, attrs);
    }
	
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
	    super.onMeasure(widthMeasureSpec, widthMeasureSpec);
	    int width = MeasureSpec.getSize(widthMeasureSpec);
	    int height = MeasureSpec.getSize(heightMeasureSpec);
	    
	    setMeasuredDimension(width-10, height-30);
	}
}
