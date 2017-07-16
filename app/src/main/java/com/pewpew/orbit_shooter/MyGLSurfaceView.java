package com.pewpew.orbit_shooter;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;

/**
 * Created by Hiroshi on 5/10/2014.
 */
public class MyGLSurfaceView extends GLSurfaceView
{
	private final MyGLRenderer mRenderer;
    public Game game;

	public MyGLSurfaceView(Context context, Game g) {
		super(context);

        game = g;
		mRenderer = new MyGLRenderer(context, g);
		setRenderer(mRenderer);
	}

	private float x_val_start = 0.0f;
	private float y_val_start = 0.0f;
	private final float VEL_GAIN = 4.0f;

	@Override
	public boolean onTouchEvent(MotionEvent e) {

        float windowSizey = Game.gameSizeToScreeny(game.getHeight());
		float touchGamePosx = Game.screenSizeToGamex(e.getX());
        float touchGamePosy = Game.screenSizeToGamey(windowSizey - e.getY());

		switch (e.getAction()) {
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_DOWN:
				x_val_start = touchGamePosx;
				y_val_start = touchGamePosy;
                synchronized(Game.targets) {
                    game.targets.add(new GeneralObject(game, true,
                            touchGamePosx, touchGamePosy,
                            0.4f, 0.4f,
                    GeneralObject.ObjectType.DropPoint,
                    0.4f, 0.4f, 100.0f, false, false, "abc"));
                }
				break;
			case MotionEvent.ACTION_UP:
                game.makeBullet(touchGamePosx, touchGamePosy,
                        VEL_GAIN * (x_val_start - touchGamePosx),
                                VEL_GAIN * (y_val_start - touchGamePosy));
                synchronized(Game.targets) {
                    game.targets.clear();
                }
				break;
		}
		return true;
	}

}