package fr.bouyguestelecom.sample;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import fr.bouyguestelecom.tv.datatypes.SurfaceContainer;
import fr.bouyguestelecom.tv.enumerators.PlayerErrorCode;
import fr.bouyguestelecom.tv.playermanager.IPlayer;

public class GraphicsLayerDialog extends Dialog implements SurfaceHolder.Callback, DialogInterface.OnKeyListener {

    private static final String TAG = "OpenApiGraphicsDialog";

	private SurfaceView mSurfaceView;
	private SurfaceContainer mSurfaceContainer;
	//private SurfaceHolder mSurfaceHolder;
	private IPlayer mPlayer;
    //private int mPosX;
    //private int mPosY;
    private OpenApiTestAppActivity activity;
    private boolean surfaceSizeChanged = false;
    private FrameLayout fl;
    private final OpenApiTestAppActivity.MyHandler mHandler;

	public GraphicsLayerDialog(Context context, IPlayer player, OpenApiTestAppActivity.MyHandler handler) { //, LayerType type) {
		super(context,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        activity = (OpenApiTestAppActivity) context;
		mPlayer = player;
        this.mHandler = handler;
        fl = new FrameLayout(context);
        setOnKeyListener(this);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	    mSurfaceView = new SurfaceView(this.getContext());
        fl.addView(mSurfaceView);
	    setContentView(fl);

        // Important !!!
        // we have to set TRANSLUCENT PixelFormat
        // if we don't the canvas will be black
        mSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
	    mSurfaceView.getHolder().addCallback(this);
	}
	
	@Override
	public void show() {
        Log.d(TAG, "show");
		super.show();
	}

	@Override
	public void hide() {
        Log.d(TAG,"hide");
       	super.hide();
		this.dismiss();
        activity.setVisibility(View.VISIBLE);
	}

    @Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
        Log.d(TAG,"surfaceChanged");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG,"surfaceCreated");
		mSurfaceContainer = new SurfaceContainer(mSurfaceView.getHolder().getSurface()); // holder.getSurface() is the same as mSurfaceView.getHolder().getSurface() I checked
		if (mPlayer != null) {
			try {
                PlayerErrorCode result = mPlayer.showSubtitle(mSurfaceContainer);
                Message msg = Message.obtain();
                msg.obj = result;
                mHandler.sendMessage(msg);
                Log.d(TAG,"showSubtitle: "+OpenApiTestAppActivity.printErrorCode(result));
                if (result != PlayerErrorCode.PLAYER_NO_ERROR) {
                    this.hide();
                }
			} catch (RemoteException e) {
                Log.e(TAG,"setVideoLayerSurface");
				e.printStackTrace();
			}
		}				
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG,"surfaceDestroyed");
	}

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == 19 &&
                event.getAction() == KeyEvent.ACTION_DOWN &&
                !event.isCanceled()) {
            if (surfaceSizeChanged) {
                mSurfaceView.setLayoutParams(new FrameLayout.LayoutParams(1920, 1080));
                clearSurface(mSurfaceView);
                try {
                    mPlayer.resize(1920,1080);
                } catch (RemoteException e) {
                    Log.e(TAG,"RemoteException at moveSubtitle");
                    e.printStackTrace();
                }
                surfaceSizeChanged = false;
            } else {
                mSurfaceView.setLayoutParams(new FrameLayout.LayoutParams(720, 576));
                clearSurface(mSurfaceView);
                try {
                    mPlayer.resize(720,576);
                } catch (RemoteException e) {
                    Log.e(TAG,"RemoteException at moveSubtitle");
                    e.printStackTrace();
                }
                surfaceSizeChanged = true;
            }
            Log.d(TAG,"mSurfaceView layout parameters changed width = "+ mSurfaceView.getLayoutParams().width +" height = "+mSurfaceView.getLayoutParams().height);
        }
        if (keyCode == 20 &&
                event.getAction() == KeyEvent.ACTION_DOWN &&
                !event.isCanceled()) {
                mSurfaceView.setLayoutParams(new FrameLayout.LayoutParams(640, 480));

                mSurfaceView.postInvalidate();
                try {
                    mPlayer.resize(640,480);
                } catch (RemoteException e) {
                    Log.e(TAG,"RemoteException at moveSubtitle");
                    e.printStackTrace();
                }
            Log.d(TAG,"mSurfaceView layout parameters changed width = "+ mSurfaceView.getLayoutParams().width +" height = "+mSurfaceView.getLayoutParams().height);
        }
        if (keyCode == 21 &&
                event.getAction() == KeyEvent.ACTION_DOWN &&
                !event.isCanceled()) {

            //mSurfaceView = null;
            //mSurfaceView.getHolder().getSurface() = null;
            fl.removeView(mSurfaceView);
            Log.d(TAG,"mSurfaceView = null");
        }
        /*
        boolean move = false;
        if (keyCode == 19 &&
                event.getAction() == KeyEvent.ACTION_DOWN &&
                !event.isCanceled()) {
            if (mPosX > 0) {
                Log.d(TAG,"move left");
                mPosX = mPosX - 50;
                move = true;
            }
        } else if (keyCode == 20 &&
                event.getAction() == KeyEvent.ACTION_DOWN &&
                !event.isCanceled()) {
            if (mPosX < 500) {
                Log.d(TAG,"move right");
                mPosX = mPosX + 50;
                move = true;
            }
        } else if (keyCode == 21 &&
                event.getAction() == KeyEvent.ACTION_DOWN &&
                !event.isCanceled()) {
            if (mPosY > 0) {
                Log.d(TAG,"move up");
                mPosY = mPosY - 50;
                move = true;
            }
        } else if (keyCode == 22 &&
                event.getAction() == KeyEvent.ACTION_DOWN &&
                !event.isCanceled()) {
            if (mPosY < 500) {
                Log.d(TAG,"move down");
                mPosY = mPosY + 50;
                move = true;
            }
        }
        if (move) {
            try {
                mPlayer.moveSubtitle(mPosX,mPosY);
                Log.d(TAG,"moveSubtitle");
            } catch (RemoteException e) {
                Log.e(TAG,"RemoteException at moveSubtitle");
                e.printStackTrace();
            }
        }
        */
        if (keyCode == KeyEvent.KEYCODE_BACK &&
                event.getAction() == KeyEvent.ACTION_UP &&
                     !event.isCanceled()) {
            Log.d(TAG,"Close subtitle dialog!");
            try {
                PlayerErrorCode result = mPlayer.hideSubtitle();
                Message msg = Message.obtain();
                msg.obj = result;
                mHandler.sendMessage(msg);
                Log.d(TAG,"hideSubtitle: "+OpenApiTestAppActivity.printErrorCode(result));
            } catch (RemoteException e) {
                Log.e(TAG,"RemoteException at hideSubtitle");
                e.printStackTrace();
            }
            this.hide();
            dialog.cancel();
            return true;
        } else {
            return false;
        }
    }

    public void clearSurface (SurfaceView surfaceView) {
        Surface surface = surfaceView.getHolder().getSurface();
        Canvas canvas = null;
        try {
            canvas = surface.lockCanvas(null);
            Log.e(TAG,"surface.lockCanvas");
            if (canvas != null) {
                Log.e(TAG,"canvas != null");
                synchronized (canvas) {
                    Log.e(TAG,"mCanvas.drawColor");
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                }
            } else {
                Log.e(TAG,"mCanvas is null");
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG,"IllegalArgumentException in draw()");
            e.printStackTrace();
        } catch (OutOfResourcesException e) {
            Log.e(TAG,"OutOfResourcesException in draw()");
            e.printStackTrace();
        } finally {
            // do this in a finally so that if an exception is thrown
            // during the above, we don't leave the Surface in an
            // inconsistent state
            if (canvas != null) {
                surface.unlockCanvasAndPost(canvas);
            }
        }
    }
}
