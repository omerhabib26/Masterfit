package www.tecjaunt.com.masterfit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.abbyy.mobile.rtr.Engine;
import com.abbyy.mobile.rtr.ITextCaptureService;
import com.abbyy.mobile.rtr.Language;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import www.tecjaunt.com.masterfit.networkConnection.ConnectionDetector;

public class OCRScreen extends AppCompatActivity {

    private static final String LINK_TAG = "http://app.masterfitlife.com/index.php/api/Data_Storage/getLink/?word=";
    static ConnectionDetector cd;
    public static SharedPreference pref;
    public static Context ctx;
    // Licensing
    private static final String licenseFileName = "AbbyyRtrSdk.license";

    ///////////////////////////////////////////////////////////////////////////////
    // Some application settings that can be changed to modify application behavior:
    // The camera zoom. Optically zooming with a good camera often improves results
    // even at close range and it is required at longer ranges.
    private static final int cameraZoom = 1;
    // Continuous autofocus is sometimes a problem. You can disable it if it is, or if you want
    // to experiment with a different approach (starting recognition in autofocus callback)
    private static final boolean disableContinuousAutofocus = false;
    // The default behavior in this sample is to start recognition when application is started or
    // resumed. You can turn off this behavior or remove it completely to simplify the application
    private static final boolean startRecognitionOnAppStart = true;
    // Area of interest specified through margin sizes relative to camera preview size
    private static final int areaOfInterestMargin_PercentOfWidth = 4;
    private static final int areaOfInterestMargin_PercentOfHeight = 25;
    // A subset of available languages shown in the UI. See all available languages in Language enum.
    // To show all languages in the UI you can substitute the list below with:
    // Language[] languages = Language.values();
    private Language[] languages = {
//		Language.ChineseSimplified,
//		Language.ChineseTraditional,
            Language.English,
//		Language.French,
//		Language.German,
//		Language.Italian,
//		Language.Japanese,
//		Language.Korean,
//		Language.Polish,
//		Language.PortugueseBrazilian,
//		Language.Russian,
//		Language.Spanish,
    };
    ///////////////////////////////////////////////////////////////////////////////

    // The 'Abbyy RTR SDK Engine' and 'Text Capture Service' to be used in this sample application
    private Engine engine;
    private ITextCaptureService textCaptureService;

    // The camera and the preview surface
    private Camera camera;
    private SurfaceViewWithOverlay surfaceViewWithOverlay;
    private SurfaceHolder previewSurfaceHolder;

    // Actual preview size and orientation
    private Camera.Size cameraPreviewSize;
    private int orientation;

    // Auxiliary variables
    private boolean inPreview = false; // Camera preview is started
    private boolean stableResultHasBeenReached; // Stable result has been reached
    private boolean startRecognitionWhenReady; // Start recognition next time when ready (and reset this flag)
    private Handler handler = new Handler(); // Posting some delayed actions;

    // UI components
    private Button startButton; // The start button
    private TextView warningTextView; // Show warnings from recognizer
    private TextView errorTextView; // Show errors from recognizer

    // Text displayed on start button
    private static final String BUTTON_TEXT_START = "Start";
    private static final String BUTTON_TEXT_STOP = "Stop";
    private static final String BUTTON_TEXT_STARTING = "Starting...";

    // To communicate with the Text Capture Service we will need this callback:
    private ITextCaptureService.Callback textCaptureCallback = new ITextCaptureService.Callback() {

        @Override
        public void onRequestLatestFrame( byte[] buffer )
        {
            // The service asks to fill the buffer with image data for a latest frame in NV21 format.
            // Delegate this task to the camera. When the buffer is filled we will receive
            // Camera.PreviewCallback.onPreviewFrame (see below)
            camera.addCallbackBuffer( buffer );
        }

        @Override
        public void onFrameProcessed( ITextCaptureService.TextLine[] lines,
                                      ITextCaptureService.ResultStabilityStatus resultStatus, ITextCaptureService.Warning warning )
        {
            // Frame has been processed. Here we process recognition results. In this sample we
            // stop when we get stable result. This callback may continue being called for some time
            // even after the service has been stopped while the calls queued to this thread (UI thread)
            // are being processed. Just ignore these calls:
            if( !stableResultHasBeenReached ) {
                if( resultStatus.ordinal() >= 3 ) {
                    // The result is stable enough to show something to the user

                    surfaceViewWithOverlay.setLines( lines, resultStatus );
//                    String result="";
//                    for(int i=0; i< lines.length;i++){
//                        result = result+" "+lines[i].Text.toString();
//                    }
//                    Toast.makeText(OCRScreen.this, "Result"+ result, Toast.LENGTH_SHORT).show();

                } else {
                    // The result is not stable. Show nothing
                    surfaceViewWithOverlay.setLines( null, ITextCaptureService.ResultStabilityStatus.NotReady );
                }

                // Show the warning from the service if any. These warnings are intended for the user
                // to take some action (zooming in, checking recognition language, etc.)
                warningTextView.setText( warning != null ? warning.name() : "" );

                if( resultStatus == ITextCaptureService.ResultStabilityStatus.Stable ) {
                    // Stable result has been reached. Stop the service
                    stopRecognition();
                    stableResultHasBeenReached = true;

                    // Show result to the user. In this sample we whiten screen background and play
                    // the same sound that is used for pressing buttons
                    surfaceViewWithOverlay.setFillBackground( true );
                    startButton.playSoundEffect( android.view.SoundEffectConstants.CLICK );
                }
            }
        }

        @Override
        public void onError( Exception e )
        {
            // Some error occurred while processing. Log it. Processing will continue
            Log.e( getString( R.string.app_name ), "Error: " + e.getMessage() );
            if( BuildConfig.DEBUG ) {
                // Make the error easily visible to the developer
                String message = e.getMessage();
                if( message.contains( "ChineseJapanese.rom" ) ) {
                    message = "Chinese, Japanese and Korean are available in EXTENDED version only. Contact us for more information.";
                } if( message.contains( "Russian.edc" ) ) {
                    message = "Cyrillic script languages are available in EXTENDED version only. Contact us for more information.";
                } else if( message.contains( ".trdic" ) ) {
                    message = "Translation is available in EXTENDED version only. Contact us for more information.";
                }
                errorTextView.setText( message );
            }
        }
    };

    // This callback will be used to obtain frames from the camera
    private Camera.PreviewCallback cameraPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame( byte[] data, Camera camera )
        {
            // The buffer that we have given to the camera in ITextCaptureService.Callback.onRequestLatestFrame
            // above have been filled. Send it back to the Text Capture Service
            textCaptureService.submitRequestedFrame( data );
        }
    };

    // This callback is used to configure preview surface for the camera
    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated( SurfaceHolder holder )
        {
            // When surface is created, store the holder
            previewSurfaceHolder = holder;
        }

        @Override
        public void surfaceChanged( SurfaceHolder holder, int format, int width, int height )
        {
            // When surface is changed (or created), attach it to the camera, configure camera and start preview
            if( camera != null ) {
                setCameraPreviewDisplayAndStartPreview();
            }
        }

        @Override
        public void surfaceDestroyed( SurfaceHolder holder )
        {
            // When surface is destroyed, clear previewSurfaceHolder
            previewSurfaceHolder = null;
        }
    };

    // Start recognition when autofocus completes (used when continuous autofocus is disabled)
    private Camera.AutoFocusCallback startRecognitionCameraAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus( boolean success, Camera camera )
        {
            startRecognition();
        }
    };

    // Enable 'Start' button when autofocus completes (used when continuous autofocus is disabled)
    private Camera.AutoFocusCallback enableStartButtonCameraAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus( boolean success, Camera camera )
        {
            startButton.setText( BUTTON_TEXT_START );
            startButton.setEnabled( true );
        }
    };

    // Start autofocus (used when continuous autofocus is disabled)
    private void autoFocus( Camera.AutoFocusCallback callback )
    {
        if( camera != null ) {
            try {
                camera.autoFocus( callback );
            } catch( Exception e ) {
                Log.e( getString( R.string.app_name ), "Error: " + e.getMessage() );
            }
        }
    }

    // Attach the camera to the surface holder, configure the camera and start preview
    private void setCameraPreviewDisplayAndStartPreview()
    {
        try {
            camera.setPreviewDisplay( previewSurfaceHolder );
        } catch( Throwable t ) {
            Log.e( getString( R.string.app_name ), "Exception in setPreviewDisplay()", t );
        }
        configureCameraAndStartPreview( camera );
    }

    // Stop preview and release the camera
    private void stopPreviewAndReleaseCamera()
    {
        if( camera != null ) {
            camera.setPreviewCallbackWithBuffer( null );
            if( inPreview ) {
                camera.stopPreview();
                inPreview = false;
            }
            camera.release();
            camera = null;
        }
    }

    // Show error on startup if any
    private void showStartupError( String message )
    {
        new AlertDialog.Builder( this )
                .setTitle( "ABBYY RTR SDK" )
                .setMessage( message )
                .setIcon( android.R.drawable.ic_dialog_alert )
                .show()
                .setOnDismissListener( new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss( DialogInterface dialog )
                    {
                        OCRScreen.this.finish();
                    }
                } );
    }

    // Load ABBYY RTR SDK engine and configure the text capture service
    private boolean createTextCaptureService()
    {
        // Initialize the engine and text capture service
        try {
            engine = Engine.load( this, licenseFileName );
            textCaptureService = engine.createTextCaptureService( textCaptureCallback );

            return true;
        } catch( java.io.IOException e ) {
            // Troubleshooting for the developer
            Log.e( getString( R.string.app_name ), "Error loading ABBYY RTR SDK:", e );
            showStartupError( "Could not load some required resource files. Make sure to configure " +
                    "'assets' directory in your application and specify correct 'license file name'. See logcat for details." );
        } catch( Engine.LicenseException e ) {
            // Troubleshooting for the developer
            Log.e( getString( R.string.app_name ), "Error loading ABBYY RTR SDK:", e );
            showStartupError( "License not valid. Make sure you have a valid license file in the " +
                    "'assets' directory and specify correct 'license file name' and 'application id'. See logcat for details." );
        } catch( Throwable e ) {
            // Troubleshooting for the developer
            Log.e( getString( R.string.app_name ), "Error loading ABBYY RTR SDK:", e );
            showStartupError( "Unspecified error while loading the engine. See logcat for details." );
        }

        return false;
    }

    // Start recognition
    public void startRecognition()
    {
        // Do not switch off the screen while text capture service is running
        previewSurfaceHolder.setKeepScreenOn( true );
        // Get area of interest (in coordinates of preview frames)
        Rect areaOfInterest = new Rect( surfaceViewWithOverlay.getAreaOfInterest() );
        // Start the service
        textCaptureService.start( cameraPreviewSize.width, cameraPreviewSize.height, orientation, areaOfInterest );
        // Change the text on the start button to 'Stop'
        startButton.setText( BUTTON_TEXT_STOP );
        startButton.setEnabled( true );
    }

    // Stop recognition
    void stopRecognition()
    {
        // Disable the 'Stop' button
        startButton.setEnabled( false );

        // Stop the service asynchronously to make application more responsive. Stopping can take some time
        // waiting for all processing threads to stop
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground( Void... params )
            {
                textCaptureService.stop();
                return null;
            }

            protected void onPostExecute( Void result )
            {
                // Restore normal power saving behaviour
                previewSurfaceHolder.setKeepScreenOn( false );
                // Change the text on the stop button back to 'Start'
                startButton.setText( BUTTON_TEXT_START );
                startButton.setEnabled( true );
            }
        }.execute();
    }

    // Clear recognition results
    void clearRecognitionResults()
    {
        stableResultHasBeenReached = false;
        surfaceViewWithOverlay.setLines( null, ITextCaptureService.ResultStabilityStatus.NotReady );
        surfaceViewWithOverlay.setFillBackground( false );
    }

    private void configureCameraAndStartPreview( Camera camera )
    {
        // Configure camera orientation. This is needed for both correct preview orientation
        // and recognition

        Display display = getWindowManager().getDefaultDisplay();
        switch( display.getRotation() ) {
            case Surface.ROTATION_0:
                orientation = 90;
                break;
            case Surface.ROTATION_90:
                orientation = 0;
                break;
            case Surface.ROTATION_180:
                orientation = 270;
                break;
            case Surface.ROTATION_270:
                orientation = 180;
                break;
        }
        camera.setDisplayOrientation( orientation );

        // Configure camera parameters
        Camera.Parameters parameters = camera.getParameters();

        // Select preview size. The preferred size is 1080x720 or just below this
        cameraPreviewSize = null;
        for( Camera.Size size : parameters.getSupportedPreviewSizes() ) {
            if( size.height <= 720 || size.width <= 720 ) {
                if( cameraPreviewSize == null ) {
                    cameraPreviewSize = size;
                } else {
                    int resultArea = cameraPreviewSize.width * cameraPreviewSize.height;
                    int newArea = size.width * size.height;
                    if( newArea > resultArea ) {
                        cameraPreviewSize = size;
                    }
                }
            }
        }
        parameters.setPreviewSize( cameraPreviewSize.width, cameraPreviewSize.height );

        // Zoom
        parameters.setZoom( cameraZoom );
        // Buffer format. The only currently supported format is NV21
        parameters.setPreviewFormat( ImageFormat.NV21 );
        // Default focus mode
        parameters.setFocusMode( Camera.Parameters.FOCUS_MODE_AUTO );

        // Done
        camera.setParameters( parameters );

        // The camera will fill the buffers with image data and notify us through the callback.
        // The buffers will be sent to camera on requests from text capture service (see implementation
        // of ITextCaptureService.Callback.onRequestLatestFrame above)
        camera.setPreviewCallbackWithBuffer( cameraPreviewCallback );

        // Clear the previous recognition results if any
        clearRecognitionResults();

        // Configure the view scale and area of interest (camera sees it as rotated 90 degrees, so
        // there's some confusion with what is width and what is height)
        surfaceViewWithOverlay.setScaleX( surfaceViewWithOverlay.getWidth(), cameraPreviewSize.height );
        surfaceViewWithOverlay.setScaleY( surfaceViewWithOverlay.getHeight(), cameraPreviewSize.width );
        // Area of interest
        int marginWidth = ( areaOfInterestMargin_PercentOfWidth * cameraPreviewSize.height ) / 100;
        int marginHeight = ( areaOfInterestMargin_PercentOfHeight * cameraPreviewSize.width ) / 100;
        surfaceViewWithOverlay.setAreaOfInterest(
                new Rect( marginWidth, marginHeight, cameraPreviewSize.height - marginWidth,
                        cameraPreviewSize.width - marginHeight ) );

        // Start preview
        camera.startPreview();

        // Choosing autofocus or continuous focus and whether to start recognition immediately or after autofocus or manually
        if( disableContinuousAutofocus ||
                !parameters.getSupportedFocusModes().contains( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO ) ) {
            // No continuous focus
            if( startRecognitionWhenReady ) {
                // Start recognition (if you look inside onStartButtonClick it is actually delayed
                // till autofocus completes
                onStartButtonClick( startButton );
                startRecognitionWhenReady = false;
            } else {
                // Just focus and enable 'Start' button
                autoFocus( enableStartButtonCameraAutoFocusCallback );
            }
        } else {
            // Continuous focus. Have to use some Magic. Some devices expect preview to actually
            // start before enabling continuous focus has any effect. So we wait for the camera to
            // actually start preview
            handler.postDelayed( new Runnable() {
                public void run()
                {
                    Camera _camera = OCRScreen.this.camera;
                    Camera.Parameters parameters = _camera.getParameters();
                    parameters.setFocusMode( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO );
                    _camera.setParameters( parameters );
                    if( startRecognitionWhenReady ) {
                        // Give some time for the camera to focus and start recognition
                        handler.postDelayed( new Runnable() {
                            public void run()
                            {
                                onStartButtonClick( startButton );
                            }

                        }, 300 );
                        startRecognitionWhenReady = false;
                    } else {
                        // Just enable 'Start' button
                        startButton.setEnabled( true );
                        startButton.setText( BUTTON_TEXT_START );
                    }
                }
            }, 300 );
        }

        inPreview = true;
    }



    // The 'Start' and 'Stop' button
    public void onStartButtonClick( View view )
    {
        if( startButton.getText().equals( BUTTON_TEXT_STOP ) ) {
            stopRecognition();
        } else {
            clearRecognitionResults();
            startButton.setEnabled( false );
            startButton.setText( BUTTON_TEXT_STARTING );
            if( disableContinuousAutofocus ) {
                autoFocus( startRecognitionCameraAutoFocusCallback );
            } else {
                startRecognition();
            }
        }
    }

    public void onStartBtnClick()
    {
        if( startButton.getText().equals( BUTTON_TEXT_STOP ) ) {
            stopRecognition();
        } else {
            clearRecognitionResults();
            startButton.setEnabled( false );
            startButton.setText( BUTTON_TEXT_STARTING );
            if( disableContinuousAutofocus ) {
                autoFocus( startRecognitionCameraAutoFocusCallback );
            } else {
                startRecognition();
            }
        }
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_screen);
        ctx=OCRScreen.this;
        cd=new ConnectionDetector(this);
        pref=new SharedPreference(this);
        // Retrieve some ui components
        warningTextView = (TextView) findViewById( R.id.warningText );
        errorTextView = (TextView) findViewById( R.id.errorText );
        startButton = (Button) findViewById( R.id.startButton );


        // Manually create preview surface. The only reason for this is to
        // avoid making it public top level class
        RelativeLayout layout = (RelativeLayout) startButton.getParent();

        surfaceViewWithOverlay = new SurfaceViewWithOverlay( this );
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT );
        surfaceViewWithOverlay.setLayoutParams( params );
        // Add the surface to the layout as the bottom-most view filling the parent
        layout.addView( surfaceViewWithOverlay, 0 );

        // Create text capture service
        if( createTextCaptureService() ) {
            // Set the callback to be called when the preview surface is ready.
            // We specify it as the last step as a safeguard so that if there are problems
            // loading the engine the preview will never start and we will never attempt calling the service
            surfaceViewWithOverlay.getHolder().addCallback( surfaceCallback );
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        // Reinitialize the camera, restart the preview and recognition if required
        startButton.setEnabled( false );
        clearRecognitionResults();
        startRecognitionWhenReady = startRecognitionOnAppStart;
        camera = Camera.open();
        if( previewSurfaceHolder != null ) {
            setCameraPreviewDisplayAndStartPreview();
        }
    }

    @Override
    public void onPause()
    {
        // Clear all pending actions
        handler.removeCallbacksAndMessages( null );
        // Stop the text capture service
        if( textCaptureService != null ) {
            textCaptureService.stop();
        }
        startButton.setText( BUTTON_TEXT_START );
        // Clear recognition results
        clearRecognitionResults();
        stopPreviewAndReleaseCamera();
        super.onPause();
    }

    // Surface View combined with an overlay showing recognition results and 'progress'
    static class SurfaceViewWithOverlay extends SurfaceView {
        private Point[] quads;
        private String[] lines;
        private Rect areaOfInterest;
        private int stability;
        private int scaleNominatorX = 1;
        private int scaleDenominatorX = 1;
        private int scaleNominatorY = 1;
        private int scaleDenominatorY = 1;
        private Paint textPaint;
        private Paint lineBoundariesPaint;
        private Paint backgroundPaint;
        private Paint areaOfInterestPaint;

        public SurfaceViewWithOverlay( Context context )
        {
            super( context );
            this.setWillNotDraw( false );

            lineBoundariesPaint = new Paint();
            lineBoundariesPaint.setStyle( Paint.Style.STROKE );
            lineBoundariesPaint.setARGB( 255, 128, 128, 128 );
            textPaint = new Paint();
            areaOfInterestPaint = new Paint();
            areaOfInterestPaint.setARGB( 100, 0, 0, 0 );
            areaOfInterestPaint.setStyle( Paint.Style.FILL );
        }

        public void setScaleX( int nominator, int denominator )
        {
            scaleNominatorX = nominator;
            scaleDenominatorX = denominator;
        }

        public void setScaleY( int nominator, int denominator )
        {
            scaleNominatorY = nominator;
            scaleDenominatorY = denominator;
        }

        public void setFillBackground( Boolean newValue )
        {
            if( newValue ) {
                backgroundPaint = new Paint();
                backgroundPaint.setStyle( Paint.Style.FILL );
                backgroundPaint.setARGB( 100, 255, 255, 255 );
            } else {
                backgroundPaint = null;
            }
            invalidate();
        }

        public void setAreaOfInterest( Rect newValue )
        {
            areaOfInterest = newValue;
            invalidate();
        }

        public Rect getAreaOfInterest()
        {
            return areaOfInterest;
        }

        public void setLines( ITextCaptureService.TextLine[] lines,
                              ITextCaptureService.ResultStabilityStatus resultStatus )
        {
            String result="";
            if( lines != null && scaleDenominatorX > 0 && scaleDenominatorY > 0 ) {
                this.quads = new Point[lines.length * 4];
                this.lines = new String[lines.length];
                for( int i = 0; i < lines.length; i++ ) {
                    ITextCaptureService.TextLine line = lines[i];
                    for( int j = 0; j < 4; j++ ) {
                        this.quads[4 * i + j] = new Point(
                                ( scaleNominatorX * line.Quadrangle[j].x ) / scaleDenominatorX,
                                ( scaleNominatorY * line.Quadrangle[j].y ) / scaleDenominatorY
                        );
                    }
                    this.lines[i] = line.Text;
                    if(result.length()==0) {
                        result = result + line.Text;
                    }else{
                        result = result +" "+ line.Text;
                    }
                }
                switch( resultStatus ) {
                    case NotReady:
                        textPaint.setARGB( 255, 128, 0, 0 );
                        Log.d("resultStatus","Not Ready");
                        break;
                    case Tentative:
                        textPaint.setARGB( 255, 128, 0, 0 );
                        Log.d("resultStatus","Tentative");
                        break;
                    case Verified:
                        textPaint.setARGB( 255, 128, 64, 0 );
                        Log.d("resultStatus","Verified");
                        break;
                    case Available:
                        textPaint.setARGB( 255, 128, 128, 0 );
                        Log.d("resultStatus","Available");
                        break;
                    case TentativelyStable:
                        textPaint.setARGB( 255, 64, 128, 0 );
                        Log.d("resultStatus","TentativelyStable");
                        break;
                    case Stable:
                        textPaint.setARGB( 255, 0, 128, 0 );
                        Log.d("resultStatus","Stable");
                        showPopup(result);
                        break;
                }
                stability = resultStatus.ordinal();

            } else {
                stability = 0;
                this.lines = null;
                this.quads = null;
            }
            this.invalidate();
        }



        @Override
        protected void onDraw( Canvas canvas )
        {
            super.onDraw( canvas );
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            canvas.save();
            if( areaOfInterest != null ) {
                // Shading and clipping the area of interest
                int left = ( areaOfInterest.left * scaleNominatorX ) / scaleDenominatorX;
                int right = ( areaOfInterest.right * scaleNominatorX ) / scaleDenominatorX;
                int top = ( areaOfInterest.top * scaleNominatorY ) / scaleDenominatorY;
                int bottom = ( areaOfInterest.bottom * scaleNominatorY ) / scaleDenominatorY;
                canvas.drawRect( 0, 0, width, top, areaOfInterestPaint );
                canvas.drawRect( 0, bottom, width, height, areaOfInterestPaint );
                canvas.drawRect( 0, top, left, bottom, areaOfInterestPaint );
                canvas.drawRect( right, top, width, bottom, areaOfInterestPaint );
                canvas.drawRect( left, top, right, bottom, lineBoundariesPaint );
                canvas.clipRect( left, top, right, bottom );
            }
            // If there is any result
            if( lines != null ) {
                // Shading (whitening) the background when stable
                if( backgroundPaint != null ) {
                    canvas.drawRect( 0, 0, width, height, backgroundPaint );
                }
                // Drawing the text lines
                for( int i = 0; i < lines.length; i++ ) {
                    // The boundaries
                    int j = 4 * i;
                    Path path = new Path();
                    Point p = quads[j + 0];
                    path.moveTo( p.x, p.y );
                    p = quads[j + 1];
                    path.lineTo( p.x, p.y );
                    p = quads[j + 2];
                    path.lineTo( p.x, p.y );
                    p = quads[j + 3];
                    path.lineTo( p.x, p.y );
                    path.close();
                    canvas.drawPath( path, lineBoundariesPaint );

                    // The skewed text (drawn by coordinate transform)
                    canvas.save();
                    Point p0 = quads[j + 0];
                    Point p1 = quads[j + 1];
                    Point p3 = quads[j + 3];

                    int dx1 = p1.x - p0.x;
                    int dy1 = p1.y - p0.y;
                    int dx2 = p3.x - p0.x;
                    int dy2 = p3.y - p0.y;

                    int sqrLength1 = dx1 * dx1 + dy1 * dy1;
                    int sqrLength2 = dx2 * dx2 + dy2 * dy2;

                    double angle = 180 * Math.atan2( dy2, dx2 ) / Math.PI;
                    double xskew = ( dx1 * dx2 + dy1 * dy2 ) / Math.sqrt( sqrLength2 );
                    double yskew = Math.sqrt( sqrLength1 - xskew * xskew );

                    textPaint.setTextSize( (float) yskew );
                    String line = lines[i];
                    Rect textBounds = new Rect();
                    textPaint.getTextBounds( lines[i], 0, line.length(), textBounds );
                    double xscale = Math.sqrt( sqrLength2 ) / textBounds.width();

                    canvas.translate( p0.x, p0.y );
                    canvas.rotate( (float) angle );
                    canvas.skew( -(float) ( xskew / yskew ), 0.0f );
                    canvas.scale( (float) xscale, 1.0f );

                    canvas.drawText( lines[i], 0, 0, textPaint );
                    canvas.restore();
                }
            }
            canvas.restore();

            // Drawing the 'progress'
            if( stability > 0 ) {
                int r = width / 50;
                int y = height - 175 - 2 * r;
                for( int i = 0; i < stability; i++ ) {
                    int x = width / 2 + 3 * r * ( i - 2 );
                    canvas.drawCircle( x, y, r, textPaint );
                }
            }
        }
    }

    public static void showPopup(final String result){
        new AlertDialog.Builder(ctx)
                .setTitle("Result")
                .setMessage(result)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(cd.isConnectingToInternet()){
                            try {
                                new www.tecjaunt.com.masterfit.actions.AsyncTask.Signup(ctx,"OCR").execute(LINK_TAG+ URLEncoder.encode(result, "UTF-8"));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }else{
                            Toast.makeText(ctx, "No Internet Connection Available", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Try Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((OCRScreen)ctx).onStartBtnClick();
                    }
                }).create().show();
    }

    public void searchResult(JSONObject object){
        try{
            String link_01=parseJSON(object,"link1");
            String link_02=parseJSON(object,"link2");
            String link_03=parseJSON(object,"link3");
            pref.setLink_01(link_01);
            pref.setLink_02(link_02);
            pref.setLink_03(link_03);
            startActivity(new Intent(getApplicationContext(),ResultScreen.class));
        }catch(Exception e){
            Toast.makeText(this, "Data Error :"+ e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }


    }

    public static String parseJSON(JSONObject object, String key){
        String result="error";
        try{
            result=object.getString(key);
            Log.d("MasterFitLife_debug",key+" : "+result);
        }catch (Exception e){
            Log.e("parsingJSONError",key+" : "+e.getMessage());
        }
        return result;
    }

}
