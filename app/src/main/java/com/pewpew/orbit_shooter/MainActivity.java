package com.pewpew.orbit_shooter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity {

	private static Context mContext;
	private GLSurfaceView mGLView;
	private MyGLRenderer mRenderer;
    private static AdView adView;
    public static Handler handler;
    public static final int HANDLER_SHOW_AD = 1;
    public static final int HANDLER_HIDE_AD = -1;
    private static int mAdHoldoff = 10;
    private static final ScheduledExecutorService worker =
            Executors.newSingleThreadScheduledExecutor();

    private Game game;

    protected void onCreate(Bundle savedInstanceState) {
        //remove title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

		super.onCreate(savedInstanceState);

        handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HANDLER_SHOW_AD:
                        setShowAd(true);
                         break;
                    case HANDLER_HIDE_AD:
                        setShowAd(false);
                        break;
                }
            }
        };

        adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-8186450939087166/7182217623");

        RelativeLayout relativeLayout = new RelativeLayout(this);
        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        AdRequest adRequest = new AdRequest.Builder()
                .build();

        adView.loadAd(adRequest);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

		mContext = this;

        game = new Game(this);
        Display d = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        try {
            d.getSize(size);
        } catch (NoSuchMethodError ignore) {
            size.x = d.getWidth();
            size.y = d.getHeight();
        }

        game.setAdjustedPPM((float)size.x, (float)size.y);
        game.setWidth(Game.screenSizeToGamex((float)size.x));
        game.setHeight(Game.screenSizeToGamey((float)size.y));

		mGLView = new MyGLSurfaceView(mContext, game);
		mRenderer = new MyGLRenderer(mContext, game);

        RelativeLayout.LayoutParams adParams =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        adParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        relativeLayout.addView(mGLView);
        relativeLayout.addView(adView, adParams);

        showAd(false);
		setContentView(relativeLayout);

        game.start();
	}

    public static void showAd(boolean visible) {
        Message msg = new Message();
        if (visible) {
            msg.what = HANDLER_SHOW_AD;
        } else {
            msg.what = HANDLER_HIDE_AD;
        }
        handler.sendMessage(msg);
    }

    public void setShowAd(boolean visible) {
        if (visible) {
            adView.setVisibility(AdView.VISIBLE);
            adView.bringToFront();
        } else {
            adView.setVisibility(AdView.INVISIBLE);
        }
    }

    public static void showAdShortTime() {
        Message msg = new Message();
        msg.what = HANDLER_SHOW_AD;
        handler.sendMessage(msg);
        Runnable task = new Runnable() {
            public void run() {
                Message msg2 = new Message();
                msg2.what = HANDLER_HIDE_AD;
                handler.sendMessage(msg2);
            }
        };
        worker.schedule(task, mAdHoldoff, TimeUnit.SECONDS);
    }

	@Override
	protected void onPause() {
		super.onPause();
		mGLView.onPause();
		finish();
		//android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	protected void onResume() {
		super.onResume();
        game.newGame();
		mGLView.onResume();
	}

	public static Context getContext() {
		return mContext;
	}

    public Game getGame() {
        return game;
    }
}
