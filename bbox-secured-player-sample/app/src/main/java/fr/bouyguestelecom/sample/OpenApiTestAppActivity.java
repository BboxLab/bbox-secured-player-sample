package fr.bouyguestelecom.sample;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Random;

import fr.bouyguestelecom.tv.capability.ICapability;
import fr.bouyguestelecom.tv.cas.ICAS;
import fr.bouyguestelecom.tv.datatypes.AudioLanguage;
import fr.bouyguestelecom.tv.datatypes.Subtitle;
import fr.bouyguestelecom.tv.enumerators.CASError;
import fr.bouyguestelecom.tv.enumerators.CASUniverse;
import fr.bouyguestelecom.tv.enumerators.PlayerErrorCode;
import fr.bouyguestelecom.tv.enumerators.PlayerState;
import fr.bouyguestelecom.tv.enumerators.StreamError;
import fr.bouyguestelecom.tv.playermanager.IPlayer;
import fr.bouyguestelecom.tv.playermanager.IPlayerCallback;
import fr.bouyguestelecom.tv.playermanager.IPlayerManager;
import fr.bouyguestelecom.tv.ISecuredPlayer;


public class OpenApiTestAppActivity extends ActionBarActivity {

    private static final String TAG = "OpenApiTestAppActivity";

    private boolean mBound = false;
    private IPlayerManager mPlayerManager;
    private ICAS mCAS;
    private ICapability mCapability;
    private IPlayer mPlayer;
    private IPlayer mPlayer1;
    private GraphicsLayerDialog mGraphicsLayerDialog;

    public static class Channel {
        private String name;
        private String url;

        public Channel (String n, String u) {
            name = n;
            url = u;
        }

        public String getName() {return name;}
        public String getUrl() {return url;}

    };

    public static class MyHandler extends Handler {
        private final WeakReference<OpenApiTestAppActivity> mActivity;

        public MyHandler(OpenApiTestAppActivity activity) {
            mActivity = new WeakReference<OpenApiTestAppActivity>(activity);
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


    private Button btnPlayRtp;
    private Button btnPlayHls;
    private Button btnPlayVod;
    private Button btnPlayMedia;
    private Button btnStop;
    private Button btnSeek;
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
    private Button btnSetSpeed;
    private Button btnGetCurrentPosition;
    private Button btnGetDuration;
    private Button btnGetUniverse;
    private Button btnSetUniverse;

    public TextView getTxtInfo() {
        return txtInfo;
    }

    private TextView txtInfo;
    private TextView txtCallbackState;
    private LinearLayout mainLinearLayout;

    //private AudioLanguage[] availableLanguagesArray = null;
    //private AudioLanguage currentLanguage = null;

    private Subtitle[] availableSubtitlesArray = null;
    private int currentSubtitleIndex = 0;

    private boolean resized = false;
    private boolean moved = false;

    private int hlsStreamsCounter = 0;
    private Channel[] hlsStreams = {
            new Channel("Sampl1", "http://255.255.255.255/Sampl1.m3u8"),
            new Channel("Sampl2", "http://255.255.255.255/Sampl2.m3u8"),
            new Channel("Sampl3", "http://255.255.255.255/Sampl3.m3u8")
    };

    private int rtpStreamsCounter = 0;
    private Channel[] rtpStreams =
            {
                    new Channel("Sampl1", "http://255.255.255.255:9999"),
                    new Channel("Sampl2", "http://255.255.255.255:9999"),
                    new Channel("Sampl3", "http://255.255.255.255:9999")
            };

    private int vodStreamsCounter = 0;
    private String[] vodStreams =
            {
                    "rtsp://Product0/3955652?type=7&srv=1",
                    "rtsp://Product1/1918161?type=7&srv=1",
                    "rtsp://Product2/1854854?type=7&srv=1"
            };

    private int fileStreamsCounter = 0;
    private String[] fileStreams =
            {
                    "/mnt/media/usb.MyUsb0/movieSample1.mp4" ,
                    "/mnt/media/usb.MyUsb0/movieSample2.mkv" ,
            };

    private int mSpeed[] = {1, 2, 4, 8, 16, 32, 64, 128};
    private int mSpeedIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_open_api_test_app);

        // bind onCreate, unbind onDestroy
        bindService();

        mainLinearLayout = (LinearLayout) findViewById(R.id.mainLinearLayout);
        btnPlayRtp = (Button) findViewById(R.id.btnPlayRtp);
        btnPlayHls = (Button) findViewById(R.id.btnPlayHls);
        btnPlayVod = (Button) findViewById(R.id.btnPlayVod);
        btnPlayMedia = (Button) findViewById(R.id.btnPlayMedia);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnSeek = (Button) findViewById(R.id.btnSeek);
        btnResize = (Button) findViewById(R.id.btnResize);
        btnMove = (Button) findViewById(R.id.btnMove);
        btnGetState = (Button) findViewById(R.id.btnGetState);
        btnGetAvailableLanguages = (Button) findViewById(R.id.btnGetAvailableLanguages);
        btnGetCurrentLanguage = (Button) findViewById(R.id.btnGetCurrentLanguage);
        btnSetCurrentLanguage = (Button) findViewById(R.id.btnSetCurrentLanguage);
        btnUnlockMorality = (Button) findViewById(R.id.btnUnlockMorality);
        btnGetAvailableSubtitles = (Button) findViewById(R.id.btnGetAvailableSubtitles);
        btnGetCurrentSubtitle = (Button) findViewById(R.id.btnGetCurrentSubtitle);
        btnSetCurrentSubtitle = (Button) findViewById(R.id.btnSetCurrentSubtitle);
        btnShowSubtitles = (Button) findViewById(R.id.btnShowSubtitles);
        btnGetSpeed = (Button) findViewById(R.id.btnGetSpeed);
        btnSetSpeed = (Button) findViewById(R.id.btnSetSpeed);
        btnGetCurrentPosition = (Button) findViewById(R.id.btnGetCurrentPosition);
        btnGetDuration = (Button) findViewById(R.id.btnGetDuration);
        btnGetUniverse = (Button) findViewById(R.id.btnGetUniverse);
        btnSetUniverse = (Button) findViewById(R.id.btnSetUniverse);

        txtInfo = (TextView) findViewById(R.id.txtInfo);
        txtCallbackState = (TextView) findViewById(R.id.txtCallbackState);

        resized = false;
        moved = false;

        btnPlayRtp.setFocusable(true);
        btnPlayRtp.setFocusableInTouchMode(true);
        btnPlayRtp.requestFocus();

        btnPlayRtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mBound) {

                        Channel channel = rtpStreams[rtpStreamsCounter];

                        Log.d(TAG, "play rtp (" + channel.getName() + " : " + channel.getUrl() + ")");
                        PlayerErrorCode result = mPlayer.play(channel.getUrl(), 0);
                        txtInfo.setText("Play : "+ printErrorCode(result));

                        rtpStreamsCounter = (++rtpStreamsCounter) % rtpStreams.length;

                        btnShowSubtitles.setEnabled(false);
                        currentSubtitleIndex = 0;
                    } else {
                        Log.d(TAG, "Not bound to OpenApi in play method");
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "RemoteException on play rtp");
                    e.printStackTrace();
                } catch (Exception ex) {
                    Log.d(TAG, "Exception on play rtp");
                    ex.printStackTrace();
                }
            }
        });

        btnPlayHls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mBound) {

                        Channel channel = hlsStreams[hlsStreamsCounter];

                        Log.d(TAG, "play hls (" + channel.getName() + " : " + channel.getUrl() + ")");
                        PlayerErrorCode result = mPlayer.play(channel.getUrl(), 0);
                        txtInfo.setText("Play : "+ printErrorCode(result));

                        hlsStreamsCounter = (++hlsStreamsCounter) % hlsStreams.length;

                        btnShowSubtitles.setEnabled(false);
                        currentSubtitleIndex = 0;
                    } else {
                        Log.d(TAG, "Not bound to OpenApi in play method");
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "RemoteException on play hls");
                    e.printStackTrace();
                } catch (Exception ex) {
                    Log.d(TAG, "Exception on play hls");
                    ex.printStackTrace();
                }
            }
        });

        btnPlayVod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mBound) {
                        Log.d(TAG, "play vod (Url : " + vodStreams[vodStreamsCounter] + ")");
                        PlayerErrorCode result = mPlayer.play(vodStreams[vodStreamsCounter],0);
                        vodStreamsCounter = (++vodStreamsCounter) % vodStreams.length;
                        txtInfo.setText("Play: "+printErrorCode(result));
                        btnShowSubtitles.setEnabled(false);
                        currentSubtitleIndex = 0;
                    } else {
                        Log.d(TAG, "Not bound to OpenApi in vod");
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "RemoteException on play vod");
                    e.printStackTrace();
                }
            }
        });

        btnPlayMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mBound) {
                        Log.d(TAG, "play media (Url : " + fileStreams[fileStreamsCounter] + ")");
                        PlayerErrorCode result = mPlayer.play(fileStreams[fileStreamsCounter],0);
                        fileStreamsCounter = (++fileStreamsCounter) % fileStreams.length;
                        txtInfo.setText("Play: "+ printErrorCode(result));
                        btnShowSubtitles.setEnabled(false);
                        currentSubtitleIndex = 0;
                    } else {
                        Log.d(TAG, "Not bound to OpenApi in play media");
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "RemoteException on play media");
                    e.printStackTrace();
                }
            }
        });

        btnSeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mBound) {
                        Log.d(TAG, "seek");
                        PlayerErrorCode result = mPlayer.seek(31);
                        txtInfo.setText("Seek: "+printErrorCode(result));
                    } else {
                        Log.d(TAG, "Not bound to OpenApi in seek");
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "RemoteException on seek");
                    e.printStackTrace();
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
                    Log.d(TAG, "RemoteException on stop");
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
                    Log.d(TAG, "RemoteException on resize");
                    e.printStackTrace();
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
                    Log.d(TAG, "RemoteException on move");
                    e.printStackTrace();
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
                    Log.d(TAG, "RemoteException on getState");
                    e.printStackTrace();
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
                                    availableLanguagesString += String.valueOf(availableLanguages[i].getIndex() + 1) + " " + availableLanguages[i].getCode() + ", ";
                                } else {
                                    availableLanguagesString += String.valueOf(availableLanguages[i].getIndex() + 1) + " " + availableLanguages[i].getCode();
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
                    Log.d(TAG, "RemoteException on getAvailableLanguages");
                    e.printStackTrace();
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
                            currentLanguageString += String.valueOf(currentLanguage.getIndex() + 1) + " " + currentLanguage.getCode();
                        } else {
                            currentLanguageString += "None";
                        }
                        txtInfo.setText(currentLanguageString);
                    } else {
                        Log.d(TAG, "Not bound to OpenApi");
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "RemoteException on getCurrentLanguage");
                    e.printStackTrace();
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
                                currentLanguageString += String.valueOf(nextLanguage.getIndex() + 1) + " " + nextLanguage.getCode();
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
                    Log.d(TAG, "RemoteException on setCurrentLanguage");
                    e.printStackTrace();
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
                    Log.d(TAG, "RemoteException on unlockMoralityLevel");
                    e.printStackTrace();
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
                        if (availableSubtitles != null && availableSubtitles.length != 0) {
                            for (int i = 0; i < availableSubtitles.length; i++) {
                                availableSubtitlesString += availableSubtitles[i].getDescription();
                                if (i < availableSubtitles.length - 1) {
                                    availableSubtitlesString += ", ";
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
                    Log.d(TAG, "RemoteException on getAvailableSubtitles");
                    e.printStackTrace();
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
                            currentSubtitleString += currentSubtitle.getDescription();
                        } else {
                            currentSubtitleString += "None";
                        }
                        txtInfo.setText(currentSubtitleString);
                    } else {
                        Log.d(TAG, "Not bound to OpenApi");
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "RemoteException on getCurrentSubtitle");
                    e.printStackTrace();
                }
            }
        });

        btnSetCurrentSubtitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mBound) {
                        Log.d(TAG, "setCurrentSubtitle");
                        availableSubtitlesArray = mPlayer.getAvailableSubtitles();
                        String setCurrentLanguageString = "Current language set at: ";
                        if (availableSubtitlesArray != null) {
                            PlayerErrorCode result = mPlayer.setCurrentSubtitle(availableSubtitlesArray[currentSubtitleIndex]);
                            if (result == PlayerErrorCode.PLAYER_NO_ERROR) {
                                setCurrentLanguageString += availableSubtitlesArray[currentSubtitleIndex].getDescription();
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
                    Log.d(TAG, "RemoteException on setCurrentLanguage");
                    e.printStackTrace();
                }
            }
        });

        btnShowSubtitles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound) {
                    Log.d(TAG, "ShowSubtitles");
                    mGraphicsLayerDialog = new GraphicsLayerDialog(OpenApiTestAppActivity.this,mPlayer,mHandler); //GraphicsLayerDialog.LayerType.SUBTITLE);
                    mGraphicsLayerDialog.show();
                    setVisibility(View.GONE);
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
                    Log.d(TAG, "RemoteException on getSpeed");
                    e.printStackTrace();
                }
            }
        });

        btnSetSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mBound) {
                        Log.d(TAG, "setSpeed");
                        PlayerErrorCode result = mPlayer.setSpeed(mSpeed[mSpeedIndex]);
                        if (result == PlayerErrorCode.PLAYER_NO_ERROR) {
                            if (mSpeedIndex < mSpeed.length - 1) {
                                mSpeedIndex++;
                            } else {
                                mSpeedIndex = 0;
                            }
                            String resultTxt;
                            int newSpeed = mPlayer.getSpeed();
                            if (newSpeed == -1) {
                                resultTxt = "Error, speed could not be set.";
                            } else {
                                resultTxt = String.valueOf(newSpeed);
                            }
                            txtInfo.setText("Set speed: " + resultTxt);
                        } else {
                            txtInfo.setText("Set speed: "+printErrorCode(result));
                        }
                    } else {
                        Log.d(TAG, "Not bound to OpenApi");
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "RemoteException on setSpeed");
                    e.printStackTrace();
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
                    Log.d(TAG, "RemoteException on getCurrentPosition");
                    e.printStackTrace();
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
                    Log.d(TAG, "RemoteException on getDuration");
                    e.printStackTrace();
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
                    Log.d(TAG, "RemoteException on getUniverse");
                    e.printStackTrace();
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
                    Log.d(TAG, "RemoteException on setUniverse");
                    e.printStackTrace();
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
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on onStop");
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            try {
                mPlayer.release();
                //mPlayer1.release();
                boolean result = mPlayerManager.releasePlayer(mPlayer.getID());
                if (result) {
                    Log.d(TAG, "releasePlayer successful for mPlayer "+mPlayer.getID());
                } else {
                    Log.d(TAG, "ERROR: releasePlayer failed for mPlayer "+mPlayer.getID());
                }
                //mPlayerManager.releasePlayer(mPlayer1.getID());
                mCallback = null;
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on onStop");
                e.printStackTrace();
            }
            unbindService(mConnection);
        }

    }


    private void bindService()
    {
        Log.d(TAG, "bindService");
        Intent remoteServiceIntent = new Intent();
        remoteServiceIntent.setAction("fr.bouyguestelecom.bboxapi.BBOX_SECURED_PLAYER");
        remoteServiceIntent.putExtra("PACKAGE_NAME",getPackageName());
        //Intent intent = new Intent(this, OpenApiService.class);
        // should this flag be Context.BIND_IMPORTANT ???...
        bindService(remoteServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
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
                mCAS = binder.getCAS();
                //mPlayer.resize(1920,1080);
                //ni mPlayer.move(0,0);
                mPlayer1 = mPlayerManager.createPlayer(mCallback1);
                if (mPlayer1 == null) {
                    Log.d(TAG, "ERROR: mPlayer is null createPlayer failed!");
                }
                mBound = true;
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException on onServiceConnected", e);
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


    private IPlayerCallback.Stub mCallback1 = new IPlayerCallback.Stub() {

        @Override
        public void onStreamProgress(int time) throws RemoteException {
            //Log.d(TAG,"onStreamProgress - time = "+time);
        }

        @Override
        public void onStreamStarted() throws RemoteException {
            Log.d(TAG, "onStreamStarted");
            setCallbackTextAndColor("onStreamStarted",Color.GREEN,txtCallbackState);
        }

        @Override
        public void onStreamEnded() throws RemoteException {
            Log.d(TAG, "onStreamEnded");
            setCallbackTextAndColor("onStreamEnded",Color.RED,txtCallbackState);
        }

        @Override
        public void onStreamFailed(StreamError code) throws RemoteException {
            Log.d(TAG, "onStreamFailed");
            setCallbackTextAndColor("onStreamFailed",Color.RED,txtCallbackState);
        }

        @Override
        public void onConditionalAccessError(CASError code)
                throws RemoteException {
            Log.d(TAG, "onConditionalAccessError");
            String error = "Unknown";
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
            setCallbackTextAndColor("onConditionalAccessError: "+error,Color.RED,txtCallbackState);
        }

    };

    private IPlayerCallback.Stub mCallback = new IPlayerCallback.Stub() {

        @Override
        public void onStreamProgress(int time) throws RemoteException {
            //Log.d(TAG,"onStreamProgress - time = "+time);
        }

        @Override
        public void onStreamStarted() throws RemoteException {
            Log.d(TAG, "onStreamStarted");
            setCallbackTextAndColor("onStreamStarted",Color.GREEN,txtCallbackState);
        }

        @Override
        public void onStreamEnded() throws RemoteException {
            Log.d(TAG, "onStreamEnded");
            setCallbackTextAndColor("onStreamEnded",Color.RED,txtCallbackState);
        }

        @Override
        public void onStreamFailed(StreamError code) throws RemoteException {
            Log.d(TAG, "onStreamFailed");
            setCallbackTextAndColor("onStreamFailed",Color.RED,txtCallbackState);
        }

        @Override
        public void onConditionalAccessError(CASError code)
                throws RemoteException {
            Log.d(TAG, "onConditionalAccessError");
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
            setCallbackTextAndColor("onConditionalAccessError: "+error,Color.RED,txtCallbackState);
        }

    };

    public String randomStream(String[] streamArray) {

        int min = 0;
        int max = streamArray.length - 1;

        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return streamArray[randomNum];
    }

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
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_open_api_test_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */
}
