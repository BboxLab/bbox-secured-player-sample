package fr.bouyguestelecom.sample;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import fr.bouyguestelecom.tv.datatypes.SurfaceContainer;
import fr.bouyguestelecom.tv.enumerators.PlayerErrorCode;
import fr.bouyguestelecom.tv.playermanager.IPlayer;

/**
 * Created by btx68782 on 08/03/16.
 */
public class SubtitleLayout extends FrameLayout implements SurfaceHolder.Callback {
    private static  final   String  LOG_TAG = SubtitleLayout.class.getSimpleName();

    private SurfaceView         mSubtitleSurface;
    private SurfaceContainer    mSurfaceContainer;
    private IPlayer             mPlayer;

    public SubtitleLayout (Context context) {
        this(context, null, 0);
    }

    public SubtitleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SubtitleLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setPlayer (IPlayer player) {
        if (null != mPlayer) {
            hideSubtitle();
        }

        mPlayer = player;
        showSubtitle();
    }

    public void setVisibility (int visibility) {
        if (View.GONE == visibility) {
            hideSubtitle();
        } else if (View.INVISIBLE == visibility) {
            hideSubtitle();
        } else if (View.VISIBLE == visibility) {
            showSubtitle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mSubtitleSurface        = new SurfaceView(this.getContext());
        this.addView(mSubtitleSurface);
        SurfaceHolder holder    = mSubtitleSurface.getHolder();
        mSurfaceContainer       = new SurfaceContainer(holder.getSurface());

        // Important !!!
        // we have to set TRANSLUCENT PixelFormat
        // if we don't the canvas will be black
        holder.setFormat(PixelFormat.TRANSLUCENT);
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(LOG_TAG, "SurfaceView create");
        showSubtitle();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(LOG_TAG, "SurfaceView change  \n{\n\tformat:" + format + ",\n\twidth:" + width + ",\n\theight:" + height + "\n}");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(LOG_TAG, "SurfaceView Destroy");
    }

    private void hideSubtitle () {
        if (View.GONE == getVisibility() || View.INVISIBLE == getVisibility()) {
            return;
        }

        if (null != mPlayer) {
            try {
                PlayerErrorCode result = mPlayer.hideSubtitle();
                Log.d(LOG_TAG, "hideSubtitle: " + OpenApiTestAppActivity.printErrorCode(result));
            } catch (RemoteException rex) {
                Log.e(LOG_TAG, "RemoteException at hideSubtitle", rex);
            }
        }
    }

    private void showSubtitle () {
        if (null != mPlayer) {
            try {
                if (View.VISIBLE == getVisibility()) {
                    clearFocus();
                }

                if (null != mPlayer.getCurrentSubtitle()) {
                    PlayerErrorCode result = mPlayer.showSubtitle(mSurfaceContainer);
                    Log.d(LOG_TAG, "showSubtitle: " + OpenApiTestAppActivity.printErrorCode(result));
                }

            } catch (RemoteException rex) {
                Log.e(LOG_TAG, "setVideoLayerSurface", rex);
            }
        }
    }
}
