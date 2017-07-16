package com.pewpew.orbit_shooter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Hiroshi on 5/10/2014.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer
{
	//private static float PPM
	// = MainActivity.getContext().getResources().getDisplayMetrics().density;
	//
	private static float PPM;

	private final Context mActivityContext;
    private Game game;
	public static int[] mTextures = new int[1];

	public MyGLRenderer(final Context activityContext, Game g) {
		mActivityContext = activityContext;
        game = g;
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		loadTexture(gl, mActivityContext, R.drawable.atlas_map);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glShadeModel(GL10.GL_SMOOTH);
		//gl.glClearColor(0.3f, 0.78f, 1.00f, 1.0f);
        //gl.glClearColor(0.15f, 0.39f, 0.50f, 1.0f);
        gl.glClearColor(0.075f, 0.195f, 0.250f, 1.0f);
        //gl.glClearColor(0.0f, 0.0f, 0.542f, 1.0f);
        //gl.glClearColor(0.1f, 0.1f, 0.1f, 0.5f);
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL10.GL_BLEND);
		Log.d("ORBITSHOOTER", "MyGLRenderer: onSurfaceCreated");

	}

	@Override
	public void onDrawFrame(GL10 gl) {
		long startTime = System.currentTimeMillis();
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		game.onDrawFrame(gl);
		//Log.d("ORBITSHOOTER", "MyGLRenderer: onDrawFrame");

		long drawTime = System.currentTimeMillis() - startTime;

		if (drawTime < 16) {
			try {
				Thread.sleep(16 - drawTime);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glOrthof(0, width, 0 , height, -10, 10);
		game.onSurfaceChanged(width, height);
	}

	public static void loadTexture(GL10 gl, final Context context,
	                               final int resourceId) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;

		final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
				resourceId, options);

		gl.glGenTextures(1, mTextures, 0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextures[0]);

		gl.glTexParameterf(GL10.GL_TEXTURE_2D,
				GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D,
				GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_NEAREST);

		gl.glTexParameterf(GL10.GL_TEXTURE_2D,
				GL10.GL_TEXTURE_WRAP_S,
				GL10.GL_REPEAT);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D,
				GL10.GL_TEXTURE_WRAP_T,
				GL10.GL_REPEAT);

		// Load the bitmap into the bound texture.
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

		// Recycle the bitmap, since its data has been loaded into OpenGL.
		bitmap.recycle();
	}
}
