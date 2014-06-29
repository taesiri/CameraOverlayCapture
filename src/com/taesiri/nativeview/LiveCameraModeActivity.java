package com.taesiri.nativeview;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.R;
import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.almeros.android.multitouch.gesturedetectors.MoveGestureDetector;
import com.almeros.android.multitouch.gesturedetectors.RotateGestureDetector;
import com.almeros.android.multitouch.gesturedetectors.ShoveGestureDetector;

public class LiveCameraModeActivity extends Activity implements OnTouchListener {


	private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    
    private Matrix mMatrix = new Matrix();
    private float mScaleFactor = 4f;
    private float mRotationDegrees = 0.f;
    private float mFocusX = 0.f;
    private float mFocusY = 0.f;  
    private int mAlpha = 255;
    private int mImageHeight, mImageWidth;

    private ScaleGestureDetector mScaleDetector;
    private RotateGestureDetector mRotateDetector;
    private MoveGestureDetector mMoveDetector;
    private ShoveGestureDetector mShoveDetector; 
    
    public static String handle_imageUrl = "default";
    
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.taesiri.coc.R.layout.livecamerafeed_layout);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);

        FrameLayout preview = (FrameLayout) findViewById(com.taesiri.coc.R.id.camera_preview);
        preview.addView(mPreview);

 		// Set this class as touchListener to the ImageView
 		ImageView view = (ImageView) findViewById(com.taesiri.coc.R.id.imageView);
 		view.setOnTouchListener(this);

 		Drawable d;
		 
        // Read a Bitmap from Assets
//        try {
        	
        	
        	//InputStream ims = getAssets().open(handle_imageUrl.replace("file:///android_asset/", ""));
        	//d = Drawable.createFromStream(ims, null);
            
        	d = this.getResources().getDrawable(com.taesiri.coc.R.drawable.brongold);
        	view.setImageDrawable(d);
            
            mImageHeight 	= d.getIntrinsicHeight();
 	 		mImageWidth 	= d.getIntrinsicWidth();
 	 		
            //ims.close();
            
//        } catch (IOException e) {
//            e.printStackTrace();
//        } 

 	 	// Determine the center of the screen to center 'earth'
 		Display display = getWindowManager().getDefaultDisplay();
 		mFocusX = display.getWidth()/2f;
 		mFocusY = display.getHeight()/2f;

 		// View is scaled and translated by matrix, so scale and translate initially
        float scaledImageCenterX = (mImageWidth*mScaleFactor)/2; 
        float scaledImageCenterY = (mImageHeight*mScaleFactor)/2;
         
 		mMatrix.postScale(mScaleFactor, mScaleFactor);
 		mMatrix.postTranslate(mFocusX - scaledImageCenterX, mFocusY - scaledImageCenterY);
 		view.setImageMatrix(mMatrix);

 		// Setup Gesture Detectors
 		mScaleDetector 	= new ScaleGestureDetector(getApplicationContext(), new ScaleListener());
 		mRotateDetector = new RotateGestureDetector(getApplicationContext(), new RotateListener());
 		mMoveDetector 	= new MoveGestureDetector(getApplicationContext(), new MoveListener());
 		mShoveDetector 	= new ShoveGestureDetector(getApplicationContext(), new ShoveListener());
 		
 		
 		
 		
 		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 		
 		
 		
 		
 		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }
    
    @SuppressWarnings("deprecation")
	public boolean onTouch(View v, MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        mRotateDetector.onTouchEvent(event);
        mMoveDetector.onTouchEvent(event);
        mShoveDetector.onTouchEvent(event);

        float scaledImageCenterX = (mImageWidth*mScaleFactor)/2;
        float scaledImageCenterY = (mImageHeight*mScaleFactor)/2;
        
        mMatrix.reset();
        mMatrix.postScale(mScaleFactor, mScaleFactor);
        mMatrix.postRotate(mRotationDegrees,  scaledImageCenterX, scaledImageCenterY);
        mMatrix.postTranslate(mFocusX - scaledImageCenterX, mFocusY - scaledImageCenterY);
        
		ImageView view = (ImageView) v;
		view.setImageMatrix(mMatrix);
		//view.setAlpha(mAlpha);

		return true; // indicate event was handled
	}
    
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mScaleFactor *= detector.getScaleFactor(); // scale change since previous event
			
			// Don't let the object get too small or too large.
			mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f)); 

			return true;
		}
	}
	
	private class RotateListener extends RotateGestureDetector.SimpleOnRotateGestureListener {
		@Override
		public boolean onRotate(RotateGestureDetector detector) {
			mRotationDegrees -= detector.getRotationDegreesDelta();
			return true;
		}
	}	
	
	private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
		@Override
		public boolean onMove(MoveGestureDetector detector) {
			PointF d = detector.getFocusDelta();
			mFocusX += d.x;
			mFocusY += d.y;		

			// mFocusX = detector.getFocusX();
			// mFocusY = detector.getFocusY();
			return true;
		}
	}		
	
	private class ShoveListener extends ShoveGestureDetector.SimpleOnShoveGestureListener {
		@Override
		public boolean onShove(ShoveGestureDetector detector) {
			mAlpha += detector.getShovePixelsDelta();
			if (mAlpha > 255)
				mAlpha = 255;
			else if (mAlpha < 0)
				mAlpha = 0;
			
			return true;
		}
	}	
	

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
        c = Camera.open(); // attempt to get a Camera instance
        if (c != null){
            Camera.Parameters params = c.getParameters();
            c.setParameters(params);
        }
    }
    catch (Exception e){
        Log.d("DEBUG", "Camera did not open");
        // Camera is not available (in use or does not exist)
    }
        return c; // returns null if camera is unavailable
    }

    private boolean prepareVideoRecorder(){


        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.stopPreview();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);



        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));


        // Step 4: Set output file
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("DEBUG", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("DEBUG", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
          return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                  Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.setPreviewCallback(null);
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            isRecording = false;
            mCamera.stopPreview();
        mCamera.setPreviewCallback(null);
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
}
