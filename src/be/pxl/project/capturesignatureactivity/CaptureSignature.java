package be.pxl.project.capturesignatureactivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class CaptureSignature extends Activity {
	//GUI Components
    LinearLayout mContent;
    signature mSignature;
    Button mClear, mGetSign, mCancel;
    View mView;
    
    //local components
    File directory;
    File myPath;
    
    //public components
    public static String tempDir;
    public int count = 1;
    public String current = null;
    public byte[] byteImg = null;

    //private components
    private Bitmap mBitmap;
    private String uniqueId;
    private EditText yourName;
    
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
    	//standard stuff
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.signature);
        
    //  tempDir = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.external_dir) + "/";
    // 	ContextWrapper cw = new ContextWrapper(getApplicationContext());
	//  File directory = cw.getDir(getResources().getString(R.string.external_dir), Context.MODE_PRIVATE);
        
        directory = new File(Environment.getExternalStorageDirectory() + "/Signatures");

        if(!directory.exists())
         
            directory.mkdir(); //directory is created;

        //prepareDirectory();
        
        
        
        //declare the gui components
        mContent = (LinearLayout) findViewById(R.id.linearLayout);
        mSignature = new signature(this, null);
        mSignature.setBackgroundColor(Color.WHITE);
        mContent.addView(mSignature, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        mClear = (Button)findViewById(R.id.clear);
        mGetSign = (Button)findViewById(R.id.getsign);
        mGetSign.setEnabled(false);
        mCancel = (Button)findViewById(R.id.cancel);
        mView = mContent;
        yourName = (EditText) findViewById(R.id.yourName);
        
        mClear.setOnClickListener(new OnClickListener()
        {       
            public void onClick(View v)
            {
                Log.v("log_tag", "Panel Cleared");
                mSignature.clear();
                mGetSign.setEnabled(false);
            }
        });

        mGetSign.setOnClickListener(new OnClickListener()
        {       
            public void onClick(View v)
            {
                Log.v("log_tag", "Panel Saved");
                boolean error = captureSignature();
                //only when everything is ok, the result will be returnes
                if(!error){
                	//fix the canvas
                    mView.setDrawingCacheEnabled(true);
                    //save the image
                    uniqueId = getTodaysDate() + "_" + yourName.getText();
                    current = uniqueId + ".png";
                    myPath= new File(directory,current);
                    
                    mSignature.getSaveBitmap(mView);
                    //mSignature.getSaveBitmap(mView);
                    //open bundle for return value
                    Bundle b = new Bundle();
                    //return string
                    b.putString("status", "done");
                    
                    //open intent
                    Intent intent = new Intent();
                    //add bundle
                    intent.putExtras(b);
                    intent.putExtra("imgByte", byteImg);
                    setResult(RESULT_OK,intent);
                    //finish this intent
                    finish();
                }
            }
        });

        mCancel.setOnClickListener(new OnClickListener()
        {       
            public void onClick(View v)
            {
                Log.v("log_tag", "Panel Canceled");
                Bundle b = new Bundle();
                b.putString("status", "cancel");
                Intent intent = new Intent();
                intent.putExtras(b);
                setResult(RESULT_OK,intent); 
                finish();
            }
        });

    }

    @Override
    protected void onDestroy() {
        Log.w("GetSignature", "onDestory");
        super.onDestroy();
    }

    private boolean captureSignature() {

        boolean error = false;
        String errorMessage = "";
        
        if(yourName.getText().toString().equalsIgnoreCase("")){
            errorMessage = errorMessage + "Please enter your Name\n";
            error = true;
        }  

        if(error){
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 105, 50);
            toast.show();
        }

        return error;
    }

    private String getTodaysDate() {

        final Calendar c = Calendar.getInstance();
        int todaysDate =     (c.get(Calendar.YEAR) * 10000) +
        ((c.get(Calendar.MONTH) + 1) * 100) +
        (c.get(Calendar.DAY_OF_MONTH));
        Log.w("DATE:",String.valueOf(todaysDate));
        return(String.valueOf(todaysDate));

    }

    private String getCurrentTime() {
        final Calendar c = Calendar.getInstance();
        int currentTime =     (c.get(Calendar.HOUR_OF_DAY) * 10000) +
        (c.get(Calendar.MINUTE) * 100) +
        (c.get(Calendar.SECOND));
        Log.w("TIME:",String.valueOf(currentTime));
        return(String.valueOf(currentTime));

    }


    private boolean prepareDirectory()
    {
        try
        {
            if (makedirs())
            {
                return true;
            } else {
                return false;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(this, "Could not initiate File System.. Is Sdcard mounted properly?", 1000).show();
            return false;
        }
    }

    private boolean makedirs()
    {
        File tempdir = new File(tempDir);
        if (!tempdir.exists())
            tempdir.mkdirs();

        if (tempdir.isDirectory())
        {
            File[] files = tempdir.listFiles();
            for (File file : files)
            {
                if (!file.delete())
                {
                    System.out.println("Failed to delete " + file);
                }
            }
        }
        return (tempdir.isDirectory());
    }

    public class signature extends View
    {
    	//width of pencil (thickness)
        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        //paint canvas
        private Paint paint = new Paint();
        //holds the shape of the drawn figure
        private Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs)
        {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }
        /*
		public void save(View v)
        {
            Log.v("log_tag", "Width: " + v.getWidth());
            Log.v("log_tag", "Height: " + v.getHeight());
            if(mBitmap == null)
            {
                mBitmap =  Bitmap.createBitmap (mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);;
                
                //converting to byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                mBitmap.compress(CompressFormat.PNG, 0, bos);
                byteImg = bos.toByteArray();
                
            }
            Canvas canvas = new Canvas(mBitmap);
            try
            {
            	String tempFile = mypath.getAbsolutePath();
                FileOutputStream mFileOutStream = openFileOutput(tempFile, Context.MODE_PRIVATE);

                v.draw(canvas);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream);
               
                //Converting to byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                mBitmap.compress(CompressFormat.PNG, 0, bos);
                byteImg = bos.toByteArray();
                
                
                
                mFileOutStream.flush();
                mFileOutStream.close();
                String url = Images.Media.insertImage(getContentResolver(), mBitmap, "title", null);
                Log.v("log_tag","url: " + url);
               

            }
            catch(Exception e)
            {
                Log.v("log_tag", e.toString());
            }
        }
        */
        public Bitmap getSaveBitmap(View v)
        {
            Log.v("log_tag", "Width: " + v.getWidth());
            Log.v("log_tag", "Height: " + v.getHeight());
            if(mBitmap == null)
            {
                mBitmap =  Bitmap.createBitmap (mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);;
                
            }
            Canvas canvas = new Canvas(mBitmap);
            try
            {
                FileOutputStream mFileOutStream = new FileOutputStream(myPath);

                v.draw(canvas);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream);
                String url = myPath.getAbsolutePath();
                mFileOutStream.flush();
                mFileOutStream.close();
                //String url = Images.Media.insertImage(CaptureSignature.this.getContentResolver(), mBitmap, "title", null);
                Log.v("log_tag","url: " + url);
              
            }
            catch(Exception e)
            {
                Log.v("log_tag", e.toString());
            }
            return mBitmap;
        }
		
		//method for clearing the canvas
        public void clear()
        {
            path.reset();
            invalidate();
        }
        
        //for drawing the path on the canvas
        @Override
        protected void onDraw(Canvas canvas)
        {
            canvas.drawPath(path, paint);
        }

        //method for every touch event
        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            float eventX = event.getX();
            float eventY = event.getY();
            mGetSign.setEnabled(true);

            switch (event.getAction())
            {
            //case for first press (1 finger)
            case MotionEvent.ACTION_DOWN:
                path.moveTo(eventX, eventY);
                lastTouchX = eventX;
                lastTouchY = eventY;
                return true;

            case MotionEvent.ACTION_MOVE:
            //if thumb goes up (stopping the signature)
            case MotionEvent.ACTION_UP:
            	//reset canvas
                resetDirtyRect(eventX, eventY);
                //get all movement points from move (signing)
                int historySize = event.getHistorySize();
                //for every point
                for (int i = 0; i < historySize; i++)
                {
                	//get values
                    float historicalX = event.getHistoricalX(i);
                    float historicalY = event.getHistoricalY(i);
                    expandDirtyRect(historicalX, historicalY);
                    //draw line to path
                    path.lineTo(historicalX, historicalY);
                }
                //draw line to endpoint
                path.lineTo(eventX, eventY);
                break;

            default:
                debug("Ignored touch event: " + event.toString());
                return false;
            }
            //set part to be drawn (make it able to draw again)
            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void debug(String string){
        }

        private void expandDirtyRect(float historicalX, float historicalY)
        {
            if (historicalX < dirtyRect.left)
            {
                dirtyRect.left = historicalX;
            }
            else if (historicalX > dirtyRect.right)
            {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top)
            {
                dirtyRect.top = historicalY;
            }
            else if (historicalY > dirtyRect.bottom)
            {
                dirtyRect.bottom = historicalY;
            }
        }
        
        //method for clearing the panel
        private void resetDirtyRect(float eventX, float eventY)
        {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }
}