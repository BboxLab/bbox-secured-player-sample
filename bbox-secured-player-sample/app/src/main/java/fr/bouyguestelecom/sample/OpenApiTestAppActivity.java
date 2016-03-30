package fr.bouyguestelecom.sample;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import fr.bouyguestelecom.tv.ISecuredPlayer;
import fr.bouyguestelecom.tv.cas.ICAS;
import fr.bouyguestelecom.tv.datatypes.AudioLanguage;
import fr.bouyguestelecom.tv.datatypes.Subtitle;
import fr.bouyguestelecom.tv.enumerators.CASError;
import fr.bouyguestelecom.tv.enumerators.CASUniverse;
import fr.bouyguestelecom.tv.enumerators.PlayerErrorCode;
import fr.bouyguestelecom.tv.enumerators.PlayerState;
import fr.bouyguestelecom.tv.enumerators.StreamError;
import fr.bouyguestelecom.tv.middleware.IBytelMiddlewareService;
import fr.bouyguestelecom.tv.middleware.ViperSettings;
import fr.bouyguestelecom.tv.middleware.ViperSettingsType;
import fr.bouyguestelecom.tv.playermanager.IPlayer;
import fr.bouyguestelecom.tv.playermanager.IPlayerCallback;
import fr.bouyguestelecom.tv.playermanager.IPlayerManager;

public class OpenApiTestAppActivity extends ActionBarActivity {

    public class Channel {
        private String name;
        private String url;

        public Channel (String n, String u) {
            name = n;
            url = u;
        }

        public String getName() {return name;}
        public String getUrl() {return url;}

    };

    private static final String TAG = "OpenApiTestAppActivity";

    private static final int SEEK_JUMP_SEC = 30;

    private boolean mBound = false;
    private IPlayerManager mPlayerManager;
    private ICAS mCAS;
    private IPlayer mPlayer;

    private IBytelMiddlewareService _bytelMiddlewareService;
    private int screenHeight;
    private int screenWidth;

    public static class MyHandler extends Handler {
        private final WeakReference<OpenApiTestAppActivity> mActivity;

        public MyHandler(OpenApiTestAppActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            OpenApiTestAppActivity activity = mActivity.get();
            PlayerErrorCode errorCode = (PlayerErrorCode)msg.obj;
            if (activity != null) {
                if (errorCode != null) {
                    String resultString = "OK";
                    if (errorCode != PlayerErrorCode.PLAYER_NO_ERROR) {
                        resultString = printErrorCode(errorCode);
                    }
                    activity.getTxtInfo().setText("Subtitles: "+resultString);
                }
            }
        }
    }

    private final MyHandler mHandler = new MyHandler(this);

    private Button btnGetProfileRtp;
    private Button btnPlayExplicitIp;
    private Button btnPlayCanalPlusSD;
    private Button btnPlayCanalPlusHD;
    private Button btnPlayCanalCinemaSD;
    private Button btnPlayCanalCinemaHD;
    private Button btnPlayTF1HD;
    private Button btnPlayM6HD;
    private Button btnPlayDorcelHD;
    private Button btnPlayXXLHD;
    private Button btnPlayRtp;
    private Button btnPlayRtpAdult;
    private Button btnPlayRtpNoRights;
    private Button btnPlayHls;
    private Button btnPlayVod;
    private Button btnPlayMedia;
    private Button btnStop;
    private Button btnSeek;
    private Button btnMoveResize;
    private Button btnResize;
    private Button btnMove;
    private Button btnGetState;
    private Button btnUnlockMorality;
    private Button btnGetAvailableLanguages;
    private Button btnGetCurrentLanguage;
    private Button btnSetCurrentLanguage;
    private Button btnGetAvailableSubtitles;
    private Button btnGetCurrentSubtitle;
    private Button btnSetCurrentSubtitle;
    private Button btnShowSubtitles;
    private Button btnGetSpeed;
    private Button btnGetCurrentPosition;
    private Button btnGetDuration;
    private Button btnGetUniverse;
    private Button btnSetUniverse;
    private Button btnGetPlaneSize;
    private EditText mEditText;
    private SubtitleLayout mSubtitle;

    public TextView getTxtInfo() {
        return txtInfo;
    }

    private TextView txtInfo;
    private TextView txtCallbackState;
    private LinearLayout mainLinearLayout;

    private Subtitle[] availableSubtitlesArray = null;
    private int currentSubtitleIndex = 0;
     int callbackTest1Counter;

    private boolean resized = false;
    private boolean moved = false;

    private int hlsStreamsCounter = 0;
    private List<Channel> hlsStreams;

    private int rtpStreamsCounter = 0;
    private List<Channel> rtpStreams;

    private int vodStreamsCounter = 0;
    private List<String> vodStreams;

    private int fileStreamsCounter = 0;
    private List<String> fileStreams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_api_test_app);

        // bind onCreate, unbind onDestroy
        bindService();

        mainLinearLayout            = (LinearLayout) findViewById(R.id.mainLinearLayout);

        btnGetProfileRtp            = (Button) findViewById(R.id.btnGetProfileRtp);
        btnPlayExplicitIp           = (Button) findViewById(R.id.btnPlayIp);
        btnPlayCanalPlusSD          = (Button) findViewById(R.id.btnPlayCanalPlusSD);
        btnPlayCanalPlusHD          = (Button) findViewById(R.id.btnPlayCanalPlusHD);
        btnPlayCanalCinemaSD        = (Button) findViewById(R.id.btnPlayCanalCinemaSD);
        btnPlayCanalCinemaHD        = (Button) findViewById(R.id.btnPlayCanalCinemaHD);
        btnPlayTF1HD                = (Button) findViewById(R.id.btnPlayTF1HD);
        btnPlayM6HD                 = (Button) findViewById(R.id.btnPlayM6HD);
        btnPlayDorcelHD             = (Button) findViewById(R.id.btnPlayDorcelHD);
        btnPlayXXLHD                = (Button) findViewById(R.id.btnPlayXXLHD);
        btnPlayRtp                  = (Button) findViewById(R.id.btnPlayRtp);
        btnPlayRtpAdult             = (Button) findViewById(R.id.btnPlayRtpAdult);
        btnPlayRtpNoRights          = (Button) findViewById(R.id.btnPlayRtpNoRights);
        btnPlayHls                  = (Button) findViewById(R.id.btnPlayHls);
        btnPlayVod                  = (Button) findViewById(R.id.btnPlayVod);
        btnPlayMedia                = (Button) findViewById(R.id.btnPlayMedia);
        btnStop                     = (Button) findViewById(R.id.btnStop);
        btnSeek                     = (Button) findViewById(R.id.btnSeek);
        btnMoveResize               = (Button) findViewById(R.id.btnMoveResize);
        btnResize                   = (Button) findViewById(R.id.btnResize);
        btnMove                     = (Button) findViewById(R.id.btnMove);
        btnGetState                 = (Button) findViewById(R.id.btnGetState);
        btnGetAvailableLanguages    = (Button) findViewById(R.id.btnGetAvailableLanguages);
        btnGetCurrentLanguage       = (Button) findViewById(R.id.btnGetCurrentLanguage);
        btnSetCurrentLanguage       = (Button) findViewById(R.id.btnSetCurrentLanguage);
        btnUnlockMorality           = (Button) findViewById(R.id.btnUnlockMorality);
        btnGetAvailableSubtitles    = (Button) findViewById(R.id.btnGetAvailableSubtitles);
        btnGetCurrentSubtitle       = (Button) findViewById(R.id.btnGetCurrentSubtitle);
        btnSetCurrentSubtitle       = (Button) findViewById(R.id.btnSetCurrentSubtitle);
        btnShowSubtitles            = (Button) findViewById(R.id.btnShowSubtitles);
        btnGetSpeed                 = (Button) findViewById(R.id.btnGetSpeed);
        btnGetCurrentPosition       = (Button) findViewById(R.id.btnGetCurrentPosition);
        btnGetDuration              = (Button) findViewById(R.id.btnGetDuration);
        btnGetUniverse              = (Button) findViewById(R.id.btnGetUniverse);
        btnSetUniverse              = (Button) findViewById(R.id.btnSetUniverse);
        btnGetPlaneSize             = (Button) findViewById(R.id.btnGetPlaneSize);
        mEditText                   = (EditText) findViewById(R.id.editPlayIp);
        mSubtitle                   = (SubtitleLayout)  findViewById(R.id.subtitle_Layout);
        txtInfo = (TextView) findViewById(R.id.txtInfo);
        txtCallbackState = (TextView) findViewById(R.id.txtCallbackState);

        resized = false;
        moved = false;

        btnPlayRtp.setFocusable(true);
        btnPlayRtp.setFocusableInTouchMode(true);
        btnPlayRtp.requestFocus();

        callbackTest1Counter = 0;

        DisplayMetrics metrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();
        display.getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        try {
            if (mBound) {
                mPlayer.resize(screenWidth, screenHeight);
            } else {
                Log.d(TAG, "Not bound to OpenApi in resize");
            }
        } catch (RemoteException e) {
            Log.d(TAG, "RemoteException on resize");
            e.printStackTrace();
        }

        String separator = getResources().getString(R.string.separator);

        String[] properties;
        vodStreams = new ArrayList<>();
        String[] vod_list   = getResources().getStringArray(R.array.vod_list);
        for(String item : vod_list){
            vodStreams.add(item);
        }
        fileStreams = new ArrayList<>();
        String[] file_list  = getResources().getStringArray(R.array.file_list);
        for(String item : file_list){
            fileStreams.add(item);
        }
        hlsStreams = new ArrayList<>();
        String[] hls_list   = getResources().getStringArray(R.array.hls_list);
        for(String item : hls_list){
            properties = item.split(separator);
            hlsStreams.add(new Channel(properties[0], properties[1]));
        }
        rtpStreams = new ArrayList<>();
        String[] rtp_list   = getResources().getStringArray(R.array.rtp_list);
        for(String item : rtp_list){
            properties = item.split(separator);
            rtpStreams.add(new Channel(properties[0], properties[1]));
        }

        btnGetProfileRtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_bytelMiddlewareService != null) {
                    ViperSettings mViperSettings = null;
                    Log.d(TAG, "Get Profile");
                    try {
                        mViperSettings = _bytelMiddlewareService.getViperSettings(ViperSettingsType.VIPER_SETTINGS_STREAM);
                    } catch (RemoteException e) {
                        Log.d(TAG, "RemoteException on get Viper settings");
                        e.printStackTrace();
                    }
                    txtInfo.setText("Profile : " + mViperSettings.getStream().getTvProfile() + " Bandwidth : " + mViperSettings.getStream().getDownstreamCurrent());
                } else {
                    Log.d(TAG, "Not connected to Bytel Middleware");
                }
            }
        });

        btnPlayCanalPlusSD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mBound) {
                        Log.d(TAG, "play CANAL+ SD");
                        PlayerErrorCode result = mPlayer.play(getResources().getString(R.string.CanalPlusSD), 0);
                        if (PlayerErrorCode.PLAYER_NO_ERROR == result) {
                            setPlayEnabled(false);
                            mSubtitle.setVisibility(View.GONE);
                        }
                        txtInfo.setText("Play: "+printErrorCode(result));
                        currentSubtitleIndex = 0;
                    } else {
                        Log.d(TAG, "Not bound to OpenApi in play method");
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "RemoteException on play CANAL+ SD");
                    e.printStackTrace();
                }
            }
        });

        btnPlayCanalPlusHD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mBound) {
                        Log.d(TAG, "play CANAL+ HD");
                        PlayerErrorCode result = mPlayer.play(getResources().getString(R.string.CanalPlusHD), 0);
                        if (PlayerErrorCode.PLAYER_NO_ERROR == result) {
                            setPlayEnabled(false);
                            mSubtitle.setVisibility(View.GONE);
                        }
                        txtInfo.setText("Play: "+printErrorCode(result));
                        currentSubtitleIndex = 0;
                    } else {
                        Log.d(TAG, "Not bound to OpenApi in play method");
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "RemoteException on play CANAL+ HD");
                    e.printStackTrace();
                }
            }
        });

        btnPlayCanalCinemaSD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mBound) {
                        Log.d(TAG, "play CANAL+ CINEMA SD");
                        PlayerErrorCode result = mPlayer.play(getResources().getString(R.string.CanalCinemaSD), 0);
                        txtInfo.setText("Play: "+printErrorCode(result));
                        if (PlayerErrorCode.PLAYER_NO_ERROR == result) {
                            setPlayEnabled(false);
                            mSubtitle.setVisibility(View.GONE);
                        }
                        currentSubtitleIndex = 0;
                    } else {
                        Log.d(TAG, "Not bound to OpenApi in play method");
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "RemoteException on play CANAL+ CINEMA SD");
                    e.printStackTrace();
                }
            }
        });

        btnPlayCanalCinemaHD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mBound) {
                        Log.d(TAG, "play CANAL+ CINEMA HD");
                        PlayerErrorCode result = mPlayer.play(getResources().getString(R.string.CanalCinemaHD), 0);
                        if (PlayerErrorCode.PLAYER_NO_ERROR == result) {
                            setPlayEnabled(false);
                            mSubtitle.setVisibility(View.GONE);
                        }
                        txtInfo.setText("Play: "+printErrorCode(result));
                        currentSubtitleIndex = 0;
                    } else {
                        Log.d(TAG, "Not bound to OpenApi in play method");
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "RemoteException on play CANAL+ CINEMA HD");
                    e.printStackTrace();
                }
            }
        });

        btnPlayTF1HD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mBound) {
                        Log.d(TAG, "play TF1 HD");
                        PlayerErrorCode result = mPlayer.play(getResources().getString(R.string.TF1HD), 0);
                        if (PlayerErrorCode.PLAYER_NO_ERROR == result) {
                            setPlayEnabled(false);
                            mSubtitle.setVisibility(View.GONE);
                        }
                        txtInfo.setText("Play: "+printErrorCode(result));
                        currentSubtitleIndex = 0;
                    } else {
                        Log.d(TAG, "Not bound to OpenApi in play method");
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "RemoteException on play TF1 HD");
                    e.printStackTrace();
                }
            }
        });

        btnPlayM6HD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mBound) {
                        Log.d(TAG, "play ARTE HD");
                        PlayerErrorCode result = mPlayer.play(getResources().getString(R.string.M6HD), 0);
                        if (PlayerErrorCode.PLAYER_NO_ERROR == result) {
                            setPlayEnabled(false);
                            mSubtitle.setVisibility(View.GONE);
                        }
                        txtInfo.setText("Play: "+printErrorCode(result));
                        currentSubtitleIndex = 0;
                    } else {
                        Log.d(TAG, "Not bound to OpenApi in play method");
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "RemoteException on play ARTE HD");
                    e.printStackTrace();
                }
            }
        });

        btnPlayDorcelHD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mBound) {
                        Log.d(TAG, "play DORCEL HD");
                        PlayerErrorCode result = mPlayer.play(getResources().getString(R.string.DorcelHD), 0);
                        if (PlayerErrorCode.PLAYER_NO_ERROR == result) {
                            setPlayEnabled(false);
                            mSubtitle.setVisibility(View.GONE);
                        }
                        txtInfo.setText("Play: "+printErrorCode(result));
                        currentSubtitleIndex = 0;
                    } else {
                        Log.d(TAG, "Not bound to OpenApi in play method");
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "RemoteException on play DORCEL HD");
                    e.printStackTrace();
                }
            }
        });

        btnPlayXXLHD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mBound) {
                        Log.d(TAG, "play XXL HD");
                        PlayerErrorCode result = mPlayer.play(getResources().getString(R.string.XXLHD), 0);
                        if (PlayerErrorCode.PLAYER_NO_ERROR == result) {
                            setPlayEnabled(false);
                            mSubtitle.setVisibility(View.GONE);
                        }
                        txtInfo.setText("Play: "+printErrorCode(result));
                        currentSubtitleIndex = 0;
                    } else {
                        Log.d(TAG, "Not bound to OpenApi in play method");
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "RemoteException on play XXL HD");
                    e.printStackTrace();
                }
            }
        });

        btnPlayRtpAdult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mBound) {
                        Log.d(TAG, "play rtp Adulte");
                        PlayerErrorCode result = mPlayer.play(getResources().getString(R.string.RtpAdult), 0);
                        if (PlayerErrorCode.PLAYER_NO_ERROR == result) {
                            setPlayEnabled(false);
                            mSubtitle.setVisibility(View.GONE);
                        }
                        txtInfo.setText("Play Adulte live stream: "+printErrorCode(result));

                        currentSubtitleIndex = 0;
                    } else {
                        Log.d(TAG, "Not bound to OpenApi in play method");
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "RemoteException on play rtp adulte");
                    e.printStackTrace();
                }
            }
        });

        btnPlayRtpNoRights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mBound) {
                        Log.d(TAG, "play rtp no rights");
                        PlayerErrorCode result = mPlayer.play(getResources().getString(R.string.RtpNoRights), 0);
                        if (PlayerErrorCode.PLAYER_NO_ERROR == result) {
                            setPlayEnabled(false);
                            mSubtitle.setVisibility(View.GONE);
                        }
                        txtInfo.setText("Play no subscribe stream: "+printErrorCode(result));

                        currentSubtitleIndex = 0;
                    } else {
                        Log.d(TAG, "Not bound to OpenApi in play method");
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "RemoteException on play rtp no subscribe");
                    e.printStackTrace();
                }
            }
        });

        btnPlayExplicitIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (mBound) {
                    Editable edit = mEditText.getText();
                    String url = edit.toString();

                    Log.d(TAG, "play ip (" + url + ")");
                    PlayerErrorCode result = mPlayer.play(url, 0);
                    if (PlayerErrorCode.PLAYER_NO_ERROR == result) {
                        setPlayEnabled(false);
                        mSubtitle.setVisibility(View.GONE);
                    }
                    txtInfo.setText("Play : " + printErrorCode(result));

                    btnShowSubtitles.setEnabled(false);
                    currentSubtitleIndex = 0;
                } else {
                    Log.d(TAG, "Not bound to OpenApi in play method");
                }
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on play ip", e);
            } catch (Exception ex) {
                Log.d(TAG, "Exception on play ip", ex);
            }
            }
        });

        btnPlayRtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (mBound) {
                    Log.d(TAG, "play rtp");

                    Channel channel = rtpStreams.get(rtpStreamsCounter);

                    Log.d(TAG, "play rtp (" + channel.getName() + " : " + channel.getUrl() + ")");
                    PlayerErrorCode result = mPlayer.play(channel.getUrl(), 0);
                    if (PlayerErrorCode.PLAYER_NO_ERROR == result) {
                        setPlayEnabled(false);
                        mSubtitle.setVisibility(View.GONE);
                    }
                    txtInfo.setText("Play : "+ printErrorCode(result));

                    rtpStreamsCounter = (++rtpStreamsCounter) % rtpStreams.size();

                    btnShowSubtitles.setEnabled(false);
                    currentSubtitleIndex = 0;
                } else {
                    Log.d(TAG, "Not bound to OpenApi in play method");
                }
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on play rtp", e);
            } catch (Exception ex) {
                Log.d(TAG, "Exception on play rtp", ex);
            }
            }
        });

        btnPlayHls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (mBound) {

                    Channel channel = hlsStreams.get(hlsStreamsCounter);

                    Log.d(TAG, "play hls (" + channel.getName() + " : " + channel.getUrl() + ")");
                    PlayerErrorCode result = mPlayer.play(channel.getUrl(), 0);
                    if (PlayerErrorCode.PLAYER_NO_ERROR == result) {
                        setPlayEnabled(false);
                        mSubtitle.setVisibility(View.GONE);
                    }
                    txtInfo.setText("Play : "+ printErrorCode(result));

                    hlsStreamsCounter = (++hlsStreamsCounter) % hlsStreams.size();

                    btnShowSubtitles.setEnabled(false);
                    currentSubtitleIndex = 0;
                } else {
                    Log.d(TAG, "Not bound to OpenApi in play method");
                }
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on play hls", e);
            } catch (Exception ex) {
                Log.d(TAG, "Exception on play hls", ex);
            }
            }
        });

        btnPlayVod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (mBound) {
                    Log.d(TAG, "play vod (Url : " + vodStreams.get(vodStreamsCounter) + ")");
                    PlayerErrorCode result = mPlayer.play(vodStreams.get(vodStreamsCounter),0);
                    if (PlayerErrorCode.PLAYER_NO_ERROR == result) {
                        setPlayEnabled(false);
                        mSubtitle.setVisibility(View.GONE);
                    }
                    txtInfo.setText("Play: "+printErrorCode(result));
                    vodStreamsCounter = (++vodStreamsCounter) % vodStreams.size();

                    btnShowSubtitles.setEnabled(false);
                    currentSubtitleIndex = 0;
                } else {
                    Log.d(TAG, "Not bound to OpenApi in vod");
                }
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on play vod", e);
            }
            }
        });

        btnPlayMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (mBound) {
                    Log.d(TAG, "play media (Url : " + fileStreams.get(fileStreamsCounter) + ")");
                    PlayerErrorCode result = mPlayer.play(fileStreams.get(fileStreamsCounter),0);
                    if (PlayerErrorCode.PLAYER_NO_ERROR == result) {
                        setPlayEnabled(false);
                        mSubtitle.setVisibility(View.GONE);
                    }
                    txtInfo.setText("Play: "+ printErrorCode(result));
                    fileStreamsCounter = (++fileStreamsCounter) % fileStreams.size();

                    btnShowSubtitles.setEnabled(false);
                    currentSubtitleIndex = 0;
                } else {
                    Log.d(TAG, "Not bound to OpenApi in play media");
                }
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on play media", e);
            }
            }
        });

        btnSeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (mBound) {
                    int seekPos = mPlayer.getCurrentPosition() + SEEK_JUMP_SEC;

                    Log.d(TAG, "[seek][duration=" + mPlayer.getDuration() + "][current_position=" + mPlayer.getCurrentPosition() + "][seek_position=" + seekPos + "]");

                    PlayerErrorCode result = mPlayer.seek(seekPos);
                    txtInfo.setText("Seek: "+printErrorCode(result));
                } else {
                    Log.d(TAG, "Not bound to OpenApi in seek");
                }
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on seek", e);
            }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (mBound) {
                    Log.d(TAG, "stop");
                    PlayerErrorCode result = mPlayer.stop();
                    txtInfo.setText("Stop: "+printErrorCode(result));
                } else {
                    Log.d(TAG, "Not bound to OpenApi in stop");
                }
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on stop", e);
            }
            }
        });

        btnMoveResize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mBound) {
                        if (!resized) {
                            Log.d(TAG, "Move to 100 100");
                            PlayerErrorCode result = mPlayer.move(100, 100);
                            Log.d(TAG, "Resize to 800 600");
                            PlayerErrorCode res = mPlayer.resize(800, 600);
                            txtInfo.setText("Move and Resize: "+printErrorCode(result)+printErrorCode(res));
                            Log.d(TAG, "Move and Resize: "+printErrorCode(result)+printErrorCode(res));
                            resized = true;
                        } else {
                            Log.d(TAG, "Move to 0 0");
                            PlayerErrorCode result = mPlayer.move(0, 0);
                            Log.d(TAG, "Resize to 1920 1080");
                            PlayerErrorCode res = mPlayer.resize(screenWidth, screenHeight);
                            txtInfo.setText("Move and Resize: "+printErrorCode(result)+printErrorCode(res));
                            Log.d(TAG, "Move and Resize: " + printErrorCode(result) + printErrorCode(res));
                            resized = false;
                        }
                    } else {
                        Log.d(TAG, "Not bound to OpenApi in resize");
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "RemoteException on resize");
                    e.printStackTrace();
                }
            }
        });

        btnResize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (mBound) {
                    Log.d(TAG, "resize");
                    if (!resized) {
                        PlayerErrorCode result = mPlayer.resize(800,600);
                        txtInfo.setText("Resize: "+printErrorCode(result));
                        resized = true;
                    } else {
                        PlayerErrorCode result = mPlayer.resize(1920,1080);
                        txtInfo.setText("Resize: "+printErrorCode(result));
                        resized = false;
                    }
                } else {
                    Log.d(TAG, "Not bound to OpenApi in resize");
                }
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on resize", e);
            }
            }
        });

        btnMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (mBound) {
                    Log.d(TAG, "move");
                    if (!moved) {
                        PlayerErrorCode result = mPlayer.move(100,100);
                        txtInfo.setText("Move: "+printErrorCode(result));
                        moved = true;
                    } else {
                        PlayerErrorCode result = mPlayer.move(0,0);
                        txtInfo.setText("Move: "+printErrorCode(result));
                        moved = false;
                    }
                } else {
                    Log.d(TAG, "Not bound to OpenApi in move");
                }
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on move", e);
            }
            }
        });

        btnGetState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (mBound) {
                    Log.d(TAG, "getState");
                    PlayerState state = mPlayer.getState();
                    if (state != null) {
                        txtInfo.setText("State: " + getStateString(state));
                    } else {
                        txtInfo.setText("State: Error, state is null.");
                    }
                } else {
                    Log.d(TAG, "Not bound to OpenApi");
                }
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on getState", e);
            }
            }
        });

        btnGetAvailableLanguages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (mBound) {
                    Log.d(TAG, "getAvailableLanguages");
                    AudioLanguage[] availableLanguages = mPlayer.getAvailableLanguages();
                    String availableLanguagesString = "Available: ";
                    if (availableLanguages != null) {
                        for (int i = 0; i < availableLanguages.length; i++) {
                            if (i < availableLanguages.length - 1) {
                                availableLanguagesString += String.valueOf(availableLanguages[i].getIndex() + 1) + "/" + availableLanguages[i].getCode() + "/" + availableLanguages[i].getDescription() + "; ";
                            } else {
                                availableLanguagesString += String.valueOf(availableLanguages[i].getIndex() + 1) + "/" + availableLanguages[i].getCode() + "/" + availableLanguages[i].getDescription();
                            }
                        }
                    } else {
                        availableLanguagesString += "None";
                    }
                    txtInfo.setText(availableLanguagesString);
                } else {
                    Log.d(TAG, "Not bound to OpenApi");
                }
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on getAvailableLanguages", e);
            }
            }
        });

        btnGetCurrentLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (mBound) {
                    Log.d(TAG, "getCurrentLanguage");
                    AudioLanguage currentLanguage = mPlayer.getCurrentLanguage();
                    String currentLanguageString = "Current language: ";
                    if (currentLanguageString != null) {
                        currentLanguageString += String.valueOf(currentLanguage.getIndex() + 1) + "/" + currentLanguage.getCode() + "/" + currentLanguage.getDescription();
                    } else {
                        currentLanguageString += "None";
                    }
                    txtInfo.setText(currentLanguageString);
                } else {
                    Log.d(TAG, "Not bound to OpenApi");
                }
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on getCurrentLanguage", e);
            }
            }
        });

        btnSetCurrentLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (mBound) {
                    Log.d(TAG, "setCurrentLanguage");
                    AudioLanguage[] availableLanguagesArray = mPlayer.getAvailableLanguages();
                    AudioLanguage currentLanguage = mPlayer.getCurrentLanguage();
                    String currentLanguageString = "Current language set at: ";
                    if (availableLanguagesArray != null && currentLanguage != null) {
                        AudioLanguage nextLanguage = new AudioLanguage();
                        for (int i = 0; i <= availableLanguagesArray.length - 1; i++) {
                            if (currentLanguage.getIndex() == availableLanguagesArray[i].getIndex()) {
                                if (i < availableLanguagesArray.length - 1) {
                                    nextLanguage = availableLanguagesArray[i + 1];
                                } else {
                                    nextLanguage = availableLanguagesArray[0];
                                }
                            }
                        }
                        PlayerErrorCode result = mPlayer.setCurrentLanguage(nextLanguage);
                        if (result == PlayerErrorCode.PLAYER_NO_ERROR) {
                            currentLanguageString += String.valueOf(nextLanguage.getIndex() + 1) + "/" + nextLanguage.getCode() + "/" + nextLanguage.getDescription();
                        } else {
                            currentLanguageString += printErrorCode(result);
                        }
                    } else {
                        currentLanguageString += "None";
                    }
                    txtInfo.setText(currentLanguageString);
                } else {
                    Log.d(TAG, "Not bound to OpenApi");
                }
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on setCurrentLanguage", e);
            }
            }
        });

        btnUnlockMorality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (mBound) {
                    Log.d(TAG, "unlockMoralityLevel");
                    PlayerErrorCode result = mPlayer.unlockMoralityLevel();
                    txtInfo.setText("Unlock morality: "+printErrorCode(result));
                } else {
                    Log.d(TAG, "Not bound to OpenApi");
                }
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on unlockMoralityLevel", e);
            }
            }
        });

        btnGetAvailableSubtitles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (mBound) {
                    Log.d(TAG, "getAvailableSubtitles");
                    Subtitle[] availableSubtitles = mPlayer.getAvailableSubtitles();
                    String availableSubtitlesString = "Available: ";
                    if (availableSubtitles != null) {
                        for (int i = 0; i < availableSubtitles.length; i++) {
                            if (i < availableSubtitles.length - 1) {
                                availableSubtitlesString += String.valueOf(availableSubtitles[i].getIndex() + 1) + "/" + availableSubtitles[i].getCode() + "/" + availableSubtitles[i].getDescription() + "; ";
                            } else {
                                availableSubtitlesString += String.valueOf(availableSubtitles[i].getIndex() + 1) + "/" + availableSubtitles[i].getCode() + "/" + availableSubtitles[i].getDescription();
                            }
                        }
                    } else {
                        availableSubtitlesString += "None";
                    }
                    txtInfo.setText(availableSubtitlesString);
                } else {
                    Log.d(TAG, "Not bound to OpenApi");
                }
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on getAvailableSubtitles", e);
            }
            }
        });

        btnGetCurrentSubtitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (mBound) {
                    Log.d(TAG, "getCurrentSubtitle");
                    Subtitle currentSubtitle = mPlayer.getCurrentSubtitle();
                    String currentSubtitleString = "Current language: ";
                    if (currentSubtitle != null) {
                        currentSubtitleString += String.valueOf(currentSubtitle.getIndex() + 1) + "/" + currentSubtitle.getDescription();
                    } else {
                        currentSubtitleString += "None";
                    }
                    txtInfo.setText(currentSubtitleString);
                } else {
                    Log.d(TAG, "Not bound to OpenApi");
                }
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on getCurrentSubtitle", e);
            }
            }
        });

        btnSetCurrentSubtitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (mBound) {
                    Log.d(TAG, "setCurrentSubtitle");
                    //create dialog (and surface)

                    availableSubtitlesArray = mPlayer.getAvailableSubtitles();
                    String setCurrentLanguageString = "Current language set at: ";
                    if (availableSubtitlesArray != null) {
                        PlayerErrorCode result = mPlayer.setCurrentSubtitle(availableSubtitlesArray[currentSubtitleIndex]);
                        if (result == PlayerErrorCode.PLAYER_NO_ERROR) {
                            setCurrentLanguageString += String.valueOf(availableSubtitlesArray[currentSubtitleIndex].getIndex() + 1) + "/" + availableSubtitlesArray[currentSubtitleIndex].getDescription();
                            if (currentSubtitleIndex >= 0 && currentSubtitleIndex < availableSubtitlesArray.length - 1) {
                                currentSubtitleIndex++;
                            } else {
                                currentSubtitleIndex = 0;
                            }
                            btnShowSubtitles.setEnabled(true);
                        } else {
                            setCurrentLanguageString += printErrorCode(result);
                        }
                    } else {
                        setCurrentLanguageString += "None";
                    }
                    txtInfo.setText(setCurrentLanguageString);
                } else {
                    Log.d(TAG, "Not bound to OpenApi");
                }
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on setCurrentLanguage", e);
            }
            }
        });

        btnShowSubtitles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if (mBound) {
                Log.d(TAG, "ShowSubtitles");
                mSubtitle.setVisibility(View.VISIBLE);
                mainLinearLayout.setVisibility(View.GONE);
            } else {
                Log.d(TAG, "Not bound to OpenApi");
            }
            }
        });

        btnGetSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (mBound) {
                    Log.d(TAG, "getSpeed");
                    int result = mPlayer.getSpeed();
                    String resultTxt;
                    if (result == -1) {
                        resultTxt = "Error, could not be retrieved.";
                    } else {
                        resultTxt = String.valueOf(result);
                    }
                    txtInfo.setText("Get speed: "+resultTxt);
                } else {
                    Log.d(TAG, "Not bound to OpenApi");
                }
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on getSpeed", e);
            }
            }
        });

        btnGetCurrentPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (mBound) {
                    Log.d(TAG, "getCurrentPosition");
                    int result = mPlayer.getCurrentPosition();
                    String resultTxt;
                    if (result == -1) {
                        resultTxt = "Error, could not be retrieved.";
                    } else {
                        resultTxt = String.valueOf(result);
                    }
                    txtInfo.setText("Current position: "+resultTxt);
                } else {
                    Log.d(TAG, "Not bound to OpenApi");
                }
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on getCurrentPosition", e);
            }
            }
        });

        btnGetDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (mBound) {
                    Log.d(TAG, "getDuration");
                    int result = mPlayer.getDuration();
                    String resultTxt;
                    if (result == -1) {
                        resultTxt = "Error, could not be retrieved.";
                    } else {
                        resultTxt = String.valueOf(result);
                    }
                    txtInfo.setText("Duration: "+resultTxt);
                } else {
                    Log.d(TAG, "Not bound to OpenApi");
                }
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on getDuration", e);
            }
            }
        });


        btnGetUniverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (mBound) {
                    Log.d(TAG, "getUniverse");
                    CASUniverse result = mCAS.getUniverse();
                    String resultTxt;
                    if (result != null) {
                        switch (result) {
                            case BOUYGUES:
                                resultTxt = "BOUYGUES";
                                break;
                            case CANALPLUS:
                                resultTxt = "CANALPLUS";
                                break;
                            case UNKNOWN:
                                resultTxt = "UNKNOWN";
                                break;
                            default:
                                resultTxt = "Error, could not be retrieved.";
                                break;
                        }
                    } else {
                        resultTxt = "Error, could not be retrieved.";
                    }
                    txtInfo.setText("CASUniverse: "+resultTxt);
                } else {
                    Log.d(TAG, "Not bound to OpenApi");
                }
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on getUniverse", e);
            }
            }
        });

        btnSetUniverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (mBound) {
                    Log.d(TAG, "setUniverse");
                    CASUniverse result = mCAS.getUniverse();
                    String resultTxt;
                    if (result != null) {
                        switch (result) {
                            case BOUYGUES:
                                mCAS.setUniverse(CASUniverse.CANALPLUS);
                                resultTxt = "CANALPLUS";
                                break;
                            case CANALPLUS:
                                mCAS.setUniverse(CASUniverse.BOUYGUES);
                                resultTxt = "BOUYGUES";
                                break;
                            case UNKNOWN:
                                mCAS.setUniverse(CASUniverse.CANALPLUS);
                                resultTxt = "CANALPLUS";
                                break;
                            default:
                                resultTxt = "Error, could not be set.";
                                break;
                        }
                    } else {
                        resultTxt = "Error, could not be set.";
                    }
                    txtInfo.setText("CASUniverse: "+resultTxt);
                } else {
                    Log.d(TAG, "Not bound to OpenApi");
                }
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on setUniverse", e);
            }
            }
        });

        btnGetPlaneSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mBound) {
                        Rect sizePos = mPlayer.getSizePosition();

                        if(sizePos != null){
                            txtInfo.setText("Video plane rect: size=" + sizePos.width() + "x" + sizePos.height() + " @ (" + sizePos.left + "," + sizePos.top + ")");
                        }
                        else{
                            txtInfo.setText("Error: Could not retrieve video plane dimensions");
                        }

                    } else {
                        Log.d(TAG, "Not bound to OpenApi");
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "RemoteException on getDuration", e);
                }
            }
        });
    }

    public void setVisibility(int visibility) {
        mainLinearLayout.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            mainLinearLayout.requestFocus();
            btnPlayRtp.requestFocus();
        }
    }

    @Override
    protected void onResume() {
        btnPlayRtp.requestFocus();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBound) {
            try {
                mPlayer.stop();
                mSubtitle.setVisibility(View.GONE);
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on onStop", e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            try {
                mPlayer.release();
                mSubtitle.setPlayer(null);
                boolean result = mPlayerManager.releasePlayer(mPlayer.getID());
                if (result) {
                    Log.d(TAG, "releasePlayer successful for mPlayer "+mPlayer.getID());
                } else {
                    Log.d(TAG, "ERROR: releasePlayer failed for mPlayer "+mPlayer.getID());
                }
                //mPlayerManager.releasePlayer(mPlayer1.getID());
                mCallback = null;
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on onStop", e);
            }
            unbindService(mConnection);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == event.getKeyCode()) {
                Log.d(TAG, "mPlayer.getSpeed() = " + mPlayer.getSpeed());
                mPlayer.setSpeed((0 == mPlayer.getSpeed()) ? 1 : 0);
                return true;
            } else if (KeyEvent.KEYCODE_BACK == event.getKeyCode()){
                if (View.VISIBLE == mSubtitle.getVisibility()) {
                    mSubtitle.setVisibility(View.GONE);
                    mainLinearLayout.setVisibility(View.VISIBLE);
                    btnStop.requestFocus();
                    return true;
                }
            }
        } catch (RemoteException ex) {
            Log.d(TAG, "RemoteException on onKeyDown", ex);
        }
        return false;
    }

    /***
     * Android L (lollipop, API 21) introduced a new problem when trying to
     * invoke implicit intent,
     * "java.lang.IllegalArgumentException: Service Intent must be explicit"
     */
    private static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    private void bindService()
    {
        // Bind to BBox api
        Intent implicitRemoteServiceIntent = new Intent();
        implicitRemoteServiceIntent.setAction("fr.bouyguestelecom.bboxapi.BBOX_SECURED_PLAYER");
        implicitRemoteServiceIntent.putExtra("PACKAGE_NAME", getPackageName());

        Intent explicitRemoteServiceIntent = createExplicitFromImplicitIntent(this, implicitRemoteServiceIntent);

        Log.d(TAG, "Binding to service -> " + explicitRemoteServiceIntent);

        bindService(explicitRemoteServiceIntent, mConnection, Context.BIND_AUTO_CREATE);

        // Bind to bytel middleware
        Intent implicitBytelMWServiceIntent = new Intent();
        implicitBytelMWServiceIntent.setAction("fr.bouyguestelecom.tv.middleware.BytelMiddlewareService");
        implicitBytelMWServiceIntent.putExtra("PACKAGE_NAME",getPackageName());

        Intent explicitBytelMWServiceIntent = createExplicitFromImplicitIntent(this, implicitBytelMWServiceIntent);

        Log.d(TAG, "Binding to service -> " + explicitBytelMWServiceIntent);

        bindService(explicitBytelMWServiceIntent, mConnectionBytelMW, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d(TAG, "onServiceConnected");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ISecuredPlayer binder = ISecuredPlayer.Stub.asInterface(service);
            try {
                mPlayerManager = binder.getPlayerManager();
                mPlayer = mPlayerManager.createPlayer(mCallback);
                if (mPlayer == null) {
                    Log.d(TAG, "ERROR: mPlayer is null createPlayer failed!");
                }
                mSubtitle.setPlayer(mPlayer);
                mCAS = binder.getCAS();
                mBound = true;
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on onServiceConnected");
                mBound = false;
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "onServiceDisconnected");
            mBound = false;
        }
    };

    private ServiceConnection mConnectionBytelMW = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d(TAG, "onServiceConnected");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            _bytelMiddlewareService = IBytelMiddlewareService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "onServiceDisconnected");
            _bytelMiddlewareService = null;
        }
    };

    private IPlayerCallback.Stub mCallback = new IPlayerCallback.Stub() {

        @Override
        public void onStreamProgress(int time) throws RemoteException {
            Log.d(TAG,"onStreamProgress - time = "+time);
        }

        @Override
        public void onStreamStarted() throws RemoteException {
            Log.d(TAG, "onStreamStarted");
            setPlayEnabled(true);
            setCallbackTextAndColor("onStreamStarted", Color.GREEN, txtCallbackState);
        }

        @Override
        public void onStreamEnded() throws RemoteException {
            Log.d(TAG, "onStreamEnded");
            setPlayEnabled(true);
            mPlayer.stop();
            setCallbackTextAndColor("onStreamEnded",Color.RED,txtCallbackState);
        }

        @Override
        public void onStreamFailed(StreamError code) throws RemoteException {
            Log.d(TAG, "onStreamFailed");
            setPlayEnabled(true);
            setCallbackTextAndColor("onStreamFailed",Color.RED,txtCallbackState);
        }

        @Override
        public void onConditionalAccessError(CASError code)
                throws RemoteException {
            String error = "Unkown";
            switch (code) {
                case CAS_BLACKOUT:
                    error = "CAS_BLACKOUT";
                    break;
                case CAS_DESCRAMBLING_KO:
                    error = "CAS_DESCRAMBLING_KO";
                    break;
                case CAS_NO_RIGHT:
                    error = "CAS_NO_RIGHT";
                    break;
                case CAS_NO_CREDIT:
                    error = "CAS_NO_CREDIT";
                    break;
                case CAS_INITIALIZE_ERROR:
                    error = "CAS_INITIALIZE_ERROR";
                    break;
                case CAS_ERROR_UNDEFINED:
                    error = "CAS_ERROR_UNDEFINED";
                    break;
                case CAS_MORALITY_KO:
                    error = "CAS_MORALITY_KO";
                    break;
                default:
                    break;
            }
            Log.d(TAG, "onConditionalAccessError : " + error);
            setCallbackTextAndColor("onConditionalAccessError: "+error,Color.RED,txtCallbackState);
        }

    };

    public String getStateString(PlayerState state) {
        if (state != null) {
            String stateString;
            switch (state) {
                case STREAM_PLAYING:
                    stateString = "STREAM_PLAYING";
                    break;
                case STREAM_STOPPED:
                    stateString = "STREAM_STOPPED";
                    break;
                case STREAM_FAILED:
                    stateString = "STREAM_FAILED";
                    break;
                default:
                    stateString = "Unknown";
                    break;
            }
            return stateString;
        } else {
            return "Error";
        }
    }

    public void clearInfoTextView() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                txtInfo.setText("");
            }
        });
    }

    public void setCallbackTextAndColor(String callbackName,int color,TextView txtView) {
        final TextView mTxtView = txtView;
        final String mCallbackName = callbackName;
        final int mColor = color;

        clearInfoTextView();

        if (callbackName != null) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mTxtView.setText(mCallbackName);
                    mTxtView.setTextColor(mColor);
                }
            });
        } else {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mTxtView.setTextColor(Color.RED);
                    mTxtView.setText("Error");
                }
            });
        }
    }


    public static String printErrorCode(PlayerErrorCode errorCode) {
        String errorCodeString = "";
        switch (errorCode) {
            case PLAYER_NO_ERROR:
                errorCodeString = "OK";
                break;
            case PLAYER_ERROR_METHOD_FAILED:
                errorCodeString = "PLAYER_ERROR_METHOD_FAILED";
                break;
            case PLAYER_ERROR_BAD_ARGUMENT:
                errorCodeString = "PLAYER_ERROR_BAD_ARGUMENT";
                break;
            case PLAYER_ERROR_IO:
                errorCodeString = "PLAYER_ERROR_IO";
                break;
            case PLAYER_ERROR_NOT_INITIALIZED:
                errorCodeString = "PLAYER_ERROR_NOT_INITIALIZED";
                break;
            case PLAYER_ERROR_SECURITY:
                errorCodeString = "PLAYER_ERROR_SECURITY";
                break;
            case RESOURCES_NOT_AVAILABLE:
                errorCodeString = "RESOURCES_NOT_AVAILABLE";
                break;
            case BANDWIDTH_NOT_AVAILABLE:
                errorCodeString = "BANDWIDTH_NOT_AVAILABLE";
                break;
            case PLAYER_ERROR_UNDEFINED:
                errorCodeString = "PLAYER_ERROR_UNDEFINED";
                break;
            default:
                errorCodeString = "Unhandled PlayerErrorCode case.";
                break;
        }
        return errorCodeString;
    }

    private void setPlayEnabled (boolean value) {
        final boolean enabled = value;
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                btnPlayExplicitIp.setEnabled(enabled);
                btnPlayCanalPlusSD.setEnabled(enabled);
                btnPlayCanalPlusHD.setEnabled(enabled);
                btnPlayCanalCinemaSD.setEnabled(enabled);
                btnPlayCanalCinemaHD.setEnabled(enabled);
                btnPlayTF1HD.setEnabled(enabled);
                btnPlayM6HD.setEnabled(enabled);
                btnPlayDorcelHD.setEnabled(enabled);
                btnPlayXXLHD.setEnabled(enabled);
                btnPlayRtp.setEnabled(enabled);
                btnPlayRtpAdult.setEnabled(enabled);
                btnPlayRtpNoRights.setEnabled(enabled);
                btnPlayHls.setEnabled(enabled);
                btnPlayVod.setEnabled(enabled);
                btnPlayMedia.setEnabled(enabled);
            }
        });
    }
}
