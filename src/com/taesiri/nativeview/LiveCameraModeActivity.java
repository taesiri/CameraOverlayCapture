package com.taesiri.nativeview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.R;
import android.R.string;
import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.almeros.android.multitouch.gesturedetectors.MoveGestureDetector;
import com.almeros.android.multitouch.gesturedetectors.RotateGestureDetector;

public class LiveCameraModeActivity extends Activity implements OnTouchListener {

    public static Bitmap camPhoto;
    
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
    
    

    public static LiveCameraModeActivity instance;
    
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
 		
 		
 		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 		
 		
 		Button captureBtn = (Button) findViewById(com.taesiri.coc.R.id.captureBtn);
 		captureBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
                Bitmap screen = takeScreenshot();
				LiveCameraModeActivity.saveBitmap(screen, "ghost");  
				mCamera.takePicture(null, null, mPicture);
			}
		});
 		
 		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 		
 		
 		instance = this;
    }
    
    public Bitmap takeScreenshot() {
 	   View rootView = findViewById(com.taesiri.coc.R.id.imageView);
 	   rootView.setDrawingCacheEnabled(true);
 	   return rootView.getDrawingCache();
 	}
    
    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
          Matrix matrix = new Matrix();
          matrix.postRotate(angle);
          return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
    
	 public static void saveBitmap(Bitmap bitmap,String name) {
	    File imagePath = new File(Environment.getExternalStorageDirectory() + "/" + name + ".png");
	    FileOutputStream fos;
	    try {
	        fos = new FileOutputStream(imagePath);
	        bitmap.compress(CompressFormat.PNG, 100, fos);
	        fos.flush();
	        fos.close();
	    } catch (FileNotFoundException e) {
	        Log.e("TAESIRI", e.getMessage(), e);
	    } catch (IOException e) {
	        Log.e("TAESIRI", e.getMessage(), e);
	    }
	}

    
    @SuppressWarnings("deprecation")
	public boolean onTouch(View v, MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        mRotateDetector.onTouchEvent(event);
        mMoveDetector.onTouchEvent(event);

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
        finish();
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
            mCamera.release();      
            mCamera = null;
        }
    }
    
    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                return;
            }
              
            Bitmap cameraPhoto = BitmapFactory.decodeByteArray(data, 0, data.length);
            cameraPhoto = LiveCameraModeActivity.RotateBitmap(cameraPhoto.copy(cameraPhoto.getConfig(), true),90);
            
            
            Bitmap ghost = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/ghost.png");
            Bitmap finalImage = ghost.copy(ghost.getConfig(), true);
            
            Rect dstRect = new Rect();
            
            Canvas canvas = new Canvas(finalImage);
            canvas.getClipBounds(dstRect);
            canvas.drawBitmap(cameraPhoto, null, dstRect, null);
            canvas.drawBitmap(ghost, new Matrix(), null);
            
            
 		
          	File imagePath = new File(Environment.getExternalStorageDirectory() + "/result.png");
	  	    FileOutputStream foss;
	  	    try {
		  	    	foss = new FileOutputStream(imagePath);
		  	    	finalImage.compress(CompressFormat.JPEG, 100, foss);
		  	        foss.flush();
		  	        foss.close();
		  	    }
	  	    catch (FileNotFoundException e) {
		  	        Log.e("TAESIRI", e.getMessage(), e);    
	  	    }
	  	    catch (IOException e) {
		  	        Log.e("TAESIRI", e.getMessage(), e);    
	  	    }
               
        }
    };
    
}
