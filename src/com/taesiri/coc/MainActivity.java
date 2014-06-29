package com.taesiri.coc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.taesiri.coc.R.layout;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        Button btnCam = (Button) findViewById(R.id.openActivityBtn);
        
        btnCam.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				Context context = getApplicationContext();
				
				Intent intent = new Intent( context, com.taesiri.nativeview.LiveCameraModeActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
		});
        
        
        Button captureB = (Button) findViewById(R.id.button1);
        
        captureB.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Bitmap screen = takeScreenshot();
				saveBitmap(screen);
			}
		});
        
        
        
    }

    
    public Bitmap takeScreenshot() {
    	   View rootView = findViewById(android.R.id.content).getRootView();
    	   rootView.setDrawingCacheEnabled(true);
    	   return rootView.getDrawingCache();
    	}

	 public void saveBitmap(Bitmap bitmap) {
	    File imagePath = new File(Environment.getExternalStorageDirectory() + "/screenshot.png");
	    FileOutputStream fos;
	    try {
	        fos = new FileOutputStream(imagePath);
	        bitmap.compress(CompressFormat.PNG, 100, fos);
	        fos.flush();
	        fos.close();
	    } catch (FileNotFoundException e) {
	        Log.e("GREC", e.getMessage(), e);
	    } catch (IOException e) {
	        Log.e("GREC", e.getMessage(), e);
	    }
	}
    	 

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
