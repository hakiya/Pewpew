package com.pewpew.orbit_shooter;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Hiroshi on 5/10/2014.
 */
public class Game
{
	private static Context context;

    //Physics
    public Physics physics;

    //Object pool
    public static ObjectPool generalObjectsPool;
    public static ObjectPool backgroundObjectsPool;
    public static ObjectPool targetsPool;
    public static ObjectPool bulletSparkPool;
    public static ObjectPool weaponSelectPool;
    public static ObjectPool scorePool;
    public static ObjectPool hi_scorePool;
    public static ObjectPool livesPool;
    public static ObjectPool centerMessagePool;
    public static ObjectPool belowCenterMessagePool;
    public static ObjectPool explosionsPool;
    public static ObjectPool mDebugPool;

    //Object factory
    public static PoolObjectFactory generalObjectFactory;

	//Object storage
	public static List<GeneralObject> generalObjects;
    public static Object generalObjectsLock;
    public static List<GeneralObject> backgroundObjects;
	public static List<GeneralObject> targets;
    public static List<GeneralObject> bulletSpark;
	public static List<GeneralObject> weaponSelect;
	public static List<GeneralObject> score;
    public static List<GeneralObject> hi_score;
	public static List<GeneralObject> lives;
	public static List<GeneralObject> centerMessage;
    public static List<GeneralObject> belowCenterMessage;
	public static List<GeneralObject> explosions;
    public static List<GeneralObject> mDebug;

	//Enemy spawn control
	private static long mTimeGapSpawn = 3000;
	private static long mTimeToNextSpawn;
	private static long mTimeToNextWave;
	private static long mTimeGapNextWave = 30000;
	private int mScore;
	private boolean mMulti;
	private int mSpawnCycle;
    private static long display_next_level_wait = 1000;
    private static long display_next_level_time;

	//Lives
	private int mLivesRemaining;

	//Bullet types
	private BulletType mBulletType;
    private static long next_bullet_hold_off = 1000;
    private static long next_bullet_time;

	//Audio
	private SoundPool mSoundPool;
	private int mSoundShootBulletID;
	private int mSoundExplosionID;
	private float mStreamVolume;
	private AudioManager mAudioManager;

	//Window width and height
	private static float PPM = 128.0f;
	private static float mHeight;
	private static float mWidth;

    //Game over controls
    private static long mTimeDelayToWaitUntilNewGame = 3000;
    private static long mTimeToWaitUntilNewGame = 3000;
    private static long mTimeGameOver;

    //Random
    public Random rand;

    //Background objects
    public static final int BACKGROUND_OBJ_COUNT = 20;

    //High Score
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    //Ad hold off
    private int mAdHoldOff = 1000;
    private int mAdStartTime = 0;

	public enum BulletType {
        yellowStar,
		OrangeStar,
        GreenStar
	}

	public Game(Context c) {
		initSounds();
        context = c;

        physics = new Physics(this);

        generalObjectFactory = new PoolObjectFactory();
        generalObjectsPool= new ObjectPool(generalObjectFactory, 100);
        backgroundObjectsPool= new ObjectPool(generalObjectFactory, 50);
        targetsPool= new ObjectPool(generalObjectFactory, 5);
        bulletSparkPool= new ObjectPool(generalObjectFactory, 100);
        weaponSelectPool= new ObjectPool(generalObjectFactory, 10);
        scorePool= new ObjectPool(generalObjectFactory, 15);
        hi_scorePool= new ObjectPool(generalObjectFactory, 20);
        livesPool= new ObjectPool(generalObjectFactory, 10);
        centerMessagePool= new ObjectPool(generalObjectFactory, 20);
        belowCenterMessagePool= new ObjectPool(generalObjectFactory, 20);
        explosionsPool= new ObjectPool(generalObjectFactory, 100);
        mDebugPool= new ObjectPool(generalObjectFactory, 10);

		generalObjects = new ArrayList<GeneralObject>();
        generalObjectsLock = new Object();
		targets = Collections.synchronizedList(new ArrayList<GeneralObject>());
		weaponSelect = Collections.synchronizedList(new ArrayList<GeneralObject>());
        bulletSpark = Collections.synchronizedList(new ArrayList<GeneralObject>());
        backgroundObjects = Collections.synchronizedList(new ArrayList<GeneralObject>());
		score = Collections.synchronizedList(new ArrayList<GeneralObject>());
        hi_score = Collections.synchronizedList(new ArrayList<GeneralObject>());
		lives = Collections.synchronizedList(new ArrayList<GeneralObject>());
		centerMessage = Collections.synchronizedList(new ArrayList<GeneralObject>());
        belowCenterMessage = Collections.synchronizedList(new ArrayList<GeneralObject>());
		explosions = Collections.synchronizedList(new ArrayList<GeneralObject>());
        mDebug = Collections.synchronizedList(new ArrayList<GeneralObject>());
		mTimeToNextSpawn = System.currentTimeMillis() + mTimeGapSpawn;
        mSpawnCycle = 0;
        mScore = 0;
        mLivesRemaining = 3;
        mMulti = false;
        mBulletType = BulletType.yellowStar;
        mTimeToNextWave = System.currentTimeMillis() + mTimeGapNextWave;
        rand = new Random();
        prefs = MainActivity.getContext()
                .getSharedPreferences("myPrefsKey", Context.MODE_PRIVATE);
    }

    public void newGame() {
        generalObjectFactory = new PoolObjectFactory();
        generalObjectsPool= new ObjectPool(generalObjectFactory, 50);
        backgroundObjectsPool= new ObjectPool(generalObjectFactory, 50);
        targetsPool= new ObjectPool(generalObjectFactory, 50);
        bulletSparkPool= new ObjectPool(generalObjectFactory, 100);
        weaponSelectPool= new ObjectPool(generalObjectFactory, 50);
        scorePool= new ObjectPool(generalObjectFactory, 50);
        hi_scorePool= new ObjectPool(generalObjectFactory, 50);
        livesPool= new ObjectPool(generalObjectFactory, 50);
        centerMessagePool= new ObjectPool(generalObjectFactory, 50);
        belowCenterMessagePool= new ObjectPool(generalObjectFactory, 50);
        explosionsPool= new ObjectPool(generalObjectFactory, 50);
        mDebugPool= new ObjectPool(generalObjectFactory, 50);
        generalObjects.clear();
        targets.clear();
        weaponSelect.clear();
        score.clear();
        hi_score.clear();
        bulletSpark.clear();
        lives.clear();
        centerMessage.clear();
        belowCenterMessage.clear();
        backgroundObjects.clear();
        explosions.clear();
        mScore = 0;
        mSpawnCycle = 0;
        mLivesRemaining = 3;
        updateScore(mScore);
        updateLives(mLivesRemaining);
        mBulletType = BulletType.yellowStar;
        mMulti = false;
        mTimeGapSpawn = 3000;
        next_bullet_hold_off = 1000;
        mTimeToNextSpawn = System.currentTimeMillis() + mTimeGapSpawn;
        mTimeToNextWave = System.currentTimeMillis() + mTimeGapNextWave;
        initConfig();
    }

    public void start() {
        initConfig();
        updateScore(mScore);
        updateLives(mLivesRemaining);
    }

    public void initConfig() {
        physics.setSimGravity(true);
        synchronized(generalObjectsLock) {
            generalObjects.add(generalObjectsPool.newObject(
                    this,
                    true,
                    getWidth() * (0.5f),
                    getHeight() * (0.2f),
                    0.4f, 0.4f,
                    GeneralObject.ObjectType.HomePlanet,
                    0.0f, 0.0f, 100.0f,
                    true, false, "abc"
            ));
            //generalObjects.add(pool.newObject(true, ))
        }
        synchronized(backgroundObjects) {
            for (int i = 0; i < BACKGROUND_OBJ_COUNT; ++i) {
                backgroundObjects.add(backgroundObjectsPool.newObject(
                        this,
                        true,
                        getWidth() * rand.nextFloat(),
                                getHeight() * rand.nextFloat(),
                        0.1f, 0.1f,
                        GeneralObject.ObjectType.Star,
                        0.0f, 0.0f, 100.0f,
                        false, false, "abc"
                ));
                backgroundObjects.get(i).mAnimateNextFrameTime = System.currentTimeMillis() +
                        (long) (rand.nextFloat() * (1000.0f));
            }
        }

	}

	public void onSurfaceChanged(int w, int h) {
		mWidth = screenSizeToGamex(w);
		mHeight = screenSizeToGamey(h);
	}

	public void onDrawFrame(GL10 gl) {
		resolveAddSpawn();
		resolveAddExplosions();
        resolveBackgroundObjects();

		resolveDeleteExplosions();
		resolveAddOrangeStar();

        physics.step();
        resolveDrawNewGame();
        resolveDraw(gl);
	}

	private void resolveAddSpawn() {
		if (mTimeToNextSpawn < System.currentTimeMillis()) {
			addSpawn();
			mTimeToNextSpawn = System.currentTimeMillis() + mTimeGapSpawn;
		}
		if ((mTimeToNextWave < System.currentTimeMillis()) &&
                (mTimeGapSpawn >= 500)) {
			mTimeGapSpawn = (long) (mTimeGapSpawn * 0.75);
			mTimeToNextWave = System.currentTimeMillis() + mTimeGapNextWave;
//            if (mLivesRemaining > 0) {
//                makeText(centerMessage, "NEXT WAVE",
//                        new MyVec2(mWidth * 0.5f - 1.8f, mHeight * 0.5f));
//            }
		}
	}

	private void resolveAddExplosions() {
		//mSoundPool.play(mSoundExplosionID,  mStreamVolume, mStreamVolume, 0, 0, 1);
	}

	private void resolveDeleteExplosions() {
        synchronized(explosions) {
            for (int i = 0; i < explosions.size(); ++i) {
                if (explosions.get(i).isDestroy()) {
                    explosionsPool.freeObject(explosions.get(i));
                    explosions.remove(i);
                }
            }
        }
        synchronized(bulletSpark) {
            for (int i = 0; i < bulletSpark.size(); ++i) {
                if (bulletSpark.get(i).isDestroy()) {
                    bulletSparkPool.freeObject(bulletSpark.get(i));
                    bulletSpark.remove(i);
                }
            }
        }
	}

	private void resolveAddOrangeStar() {
		if ((mScore >= 10) && (mScore < 40) && (!mMulti)){
			//makeText(weaponSelect, "ORANGE", new MyVec2(0.4f, 1.2f));
            synchronized (generalObjectsLock) {
                for (int i = 0; i < generalObjects.size(); ++i) {
                    if (generalObjects.get(i).type.is(GeneralObject.ObjectType.HomePlanet)) {
                        generalObjects.get(i).mass = 500.0f;
                    }
                }
            }
			mMulti = true;
			mBulletType = BulletType.OrangeStar;
		} else if ((mScore >= 80) && (mMulti)) {
            //makeText(weaponSelect, "GREEN", new MyVec2(0.4f, 1.2f));
            synchronized (generalObjectsLock) {
                for (int i = 0; i < generalObjects.size(); ++i) {
                    if (generalObjects.get(i).type.is(GeneralObject.ObjectType.HomePlanet)) {
                        generalObjects.get(i).mass = 2000.0f;
                    }
                }
            }
            mBulletType = BulletType.GreenStar;
            mMulti = false;
        }
    }

    private void resolveBackgroundObjects() {
        synchronized(backgroundObjects) {
            for (GeneralObject object : backgroundObjects) {
                if (object.posy < 0.0f) {
                    object.posy = mHeight;
                }
                object.posy += -0.01f;
            }
        }
    }

    private void resolveDrawNewGame() {
        if ((mLivesRemaining <= 0) && (belowCenterMessage.isEmpty()) &&
                System.currentTimeMillis() > mTimeToWaitUntilNewGame) {
            updateBelowCenterMessage();
        }
        if ((mLivesRemaining <= 0) &&
                System.currentTimeMillis() > mTimeToWaitUntilNewGame + 1000) {
            newGame();
        }
    }

	private void resolveDraw(GL10 gl) {
        synchronized(backgroundObjects) {
            for (GeneralObject object : backgroundObjects) {
                object.draw(gl);
            }
        }
        synchronized(bulletSpark) {
            for (GeneralObject object : bulletSpark) {
                object.draw(gl);
            }
        }
        synchronized(generalObjectsLock) {
            for (GeneralObject object : generalObjects) {
                object.setAngle(object.getAngle() + 0.01f);
                object.draw(gl);
            }
        }
        synchronized(targets) {
            for (GeneralObject object : targets) {
                object.draw(gl);
            }
        }
        synchronized(weaponSelect) {
            for (GeneralObject object : weaponSelect) {
                object.draw(gl);
            }
        }
        synchronized(score) {
            for (GeneralObject object : score) {
                object.draw(gl);
            }
        }
        synchronized(hi_score) {
            for (GeneralObject object : hi_score) {
                object.draw(gl);
            }
        }
        synchronized(lives) {
            for (GeneralObject object : lives) {
                object.draw(gl);
            }
        }
        synchronized(explosions) {
            for (GeneralObject object : explosions) {
                if (object.type.is(GeneralObject.ObjectType.Explosion)) {
                    object.setAngle(object.getAngle() + 1);
                }
                object.draw(gl);
            }
        }
        synchronized(centerMessage) {
            for (GeneralObject object : centerMessage) {
                object.draw(gl);
            }
        }
        synchronized(belowCenterMessage) {
            for (GeneralObject object : belowCenterMessage) {
                object.draw(gl);
            }
        }
	}

	private void initSounds() {
		mAudioManager = (AudioManager) MainActivity.getContext().getSystemService(Context.AUDIO_SERVICE);

		mStreamVolume = (float) mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) /
				mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

		mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {

			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				// TODO Auto-generated method stub

			}
		});

		mSoundShootBulletID = mSoundPool.load(MainActivity.getContext(), R.raw.shoot, 2);
		mSoundExplosionID = mSoundPool.load(MainActivity.getContext(), R.raw.hit, 2);
	}

	private void addSpawn() {
        synchronized(generalObjectsLock) {
            switch (mSpawnCycle) {
                case 0:
                    generalObjects.add(generalObjectsPool.newObject(
                            this,
                            true,
                            getWidth() * (rand.nextFloat()),
                            getHeight() * (1.0f),
                            0.4f, 0.4f,
                            GeneralObject.ObjectType.Can,
                            rand.nextFloat()*2.0f-1.0f, -2.0f-rand.nextFloat()*2.0f-1.0f, 100.0f,
                            true, true, "abc"
                    ));
                    ++mSpawnCycle;
                    break;
                case 1:
                    generalObjects.add(generalObjectsPool.newObject(
                            this,
                            true,
                            getWidth() * (rand.nextFloat()),
                                    getHeight() * (1.0f),
                            0.4f, 0.4f,
                            GeneralObject.ObjectType.CrescentMoon,
                            rand.nextFloat()*2.0f-1.0f, -2.0f-rand.nextFloat()*2.0f-1.0f, 10.0f,
                            true, true, "abc"
                    ));
                    ++mSpawnCycle;
                    break;
                case 2:
                    generalObjects.add(generalObjectsPool.newObject(
                            this,
                            true,
                            getWidth() * (rand.nextFloat()),
                                    getHeight() * (1.0f),
                            0.4f, 0.4f,
                            GeneralObject.ObjectType.Donut,
                            rand.nextFloat()*2.0f-1.0f, -2.0f-rand.nextFloat()*2.0f-1.0f, 50.0f,
                            true, true, "abc"
                    ));
                    ++mSpawnCycle;
                    break;
                case 3:
                    generalObjects.add(generalObjectsPool.newObject(
                            this,
                            true,
                            getWidth() * (rand.nextFloat()),
                                    getHeight() * (1.0f),
                            0.4f, 0.4f,
                            GeneralObject.ObjectType.LightBlueCross,
                            rand.nextFloat()*2.0f-1.0f, -2.0f-rand.nextFloat()*2.0f-1.0f, 100.0f,
                            true, true, "abc"
                    ));
                    ++mSpawnCycle;
                    break;
                case 4:
                    generalObjects.add(generalObjectsPool.newObject(
                            this,
                            true,
                            getWidth() * (rand.nextFloat()),
                                    getHeight() * (1.0f),
                            0.4f, 0.4f,
                            GeneralObject.ObjectType.BlueDiamond,
                            rand.nextFloat()*2.0f-1.0f, -2.0f-rand.nextFloat()*2.0f-1.0f, 250.0f,
                            true, true, "abc"
                    ));
                    ++mSpawnCycle;
                    break;
                case 5:
                    generalObjects.add(generalObjectsPool.newObject(
                            this,
                            true,
                            getWidth() * (rand.nextFloat()),
                            getHeight() * (1.0f),
                            0.4f, 0.4f,
                            GeneralObject.ObjectType.RedHexStar,
                            rand.nextFloat()*2.0f-1.0f, -2.0f-rand.nextFloat()*2.0f-1.0f, 500.0f,
                            true, true, "abc"
                    ));
                    ++mSpawnCycle;
                    break;
                case 6:
                    generalObjects.add(generalObjectsPool.newObject(
                            this,
                            true,
                            getWidth() * (rand.nextFloat()),
                                    getHeight() * (1.0f),
                            0.4f, 0.4f,
                            GeneralObject.ObjectType.GreenSquare,
                            rand.nextFloat()*2.0f-1.0f, -2.0f-rand.nextFloat()*2.0f-1.0f, 1000.0f,
                            true, true, "abc"
                    ));
                    mSpawnCycle = 0;
                    break;
            }
        }
	}

	public void makeBullet(float posx, float posy, float velx, float vely) {
        if (System.currentTimeMillis() > next_bullet_time) {
            next_bullet_time = System.currentTimeMillis() + next_bullet_hold_off;
            if (posy > mHeight * 0.25f) {
                posy = mHeight * 0.25f;
            }
            if (posx > mWidth * 0.6f) {
                posx = mWidth * 0.6f;
            }
            if (posx < mWidth * 0.4f) {
                posx = mWidth * 0.4f;
            }
            if (posy < mHeight * 0.15f) {
                posy = mHeight * 0.15f;
            }
            switch (mBulletType) {
                case yellowStar:
                    synchronized (generalObjectsLock) {
                        generalObjects.add(generalObjectsPool.newObject(
                                this,
                                true, posx, posy,
                                0.4f, 0.4f,
                                GeneralObject.ObjectType.YellowStar,
                                velx * 0.2f, vely * 0.2f, 400.0f,
                                true, true, "abc"
                        ));
                        next_bullet_hold_off = 1000;
                    }
                    break;
                case OrangeStar:
                    synchronized (generalObjectsLock) {
                        generalObjects.add(generalObjectsPool.newObject(
                                this,
                                true, posx, posy,
                                0.4f, 0.4f,
                                GeneralObject.ObjectType.OrangeStar,
                                velx * 0.3f, vely * 0.3f, 1000.0f,
                                true, true, "abc"
                        ));
                        next_bullet_hold_off = 500;
                    }
                    break;
                case GreenStar:
                    synchronized (generalObjectsLock) {
                        generalObjects.add(generalObjectsPool.newObject(
                                this,
                                true, posx, posy,
                                0.4f, 0.4f,
                                GeneralObject.ObjectType.GreenStar,
                                velx * 0.5f, vely * 0.5f, 3000.0f,
                                true, true, "abc"
                        ));
                        next_bullet_hold_off = 300;
                    }
                    break;
            }
            mSoundPool.play(mSoundShootBulletID, mStreamVolume, mStreamVolume, 0, 0, 1);
        }
	}

	public void updateLives(int lives) {
        synchronized(Game.lives) {
            Game.lives.clear();
        }
		makeText(Game.lives, "LIVES " + lives, mWidth - 3.0f, mHeight - 0.4f);
	}

	public void updateScore(int score) {
        synchronized(Game.score) {
            Game.score.clear();
        }
		makeText(Game.score, "SCORE " + score, 0.4f, mHeight - 0.4f);
	}

    public void updateHiScore(int score) {
        synchronized(Game.hi_score) {
            Game.hi_score.clear();
        }
        makeText(hi_score, "HI SCORE " + score, 0.4f, mHeight - 0.8f);
    }

	public void updateCenterMessage() {
        synchronized(centerMessage) {
            centerMessage.clear();
        }
		makeText(centerMessage, "GAME OVER", mWidth * 0.5f - 1.8f, mHeight * 0.5f);
    }

    public void updateBelowCenterMessage() {
        synchronized(belowCenterMessage) {
            belowCenterMessage.clear();
        }
        makeText(belowCenterMessage, "NEW GAME", mWidth * 0.5f - 1.6f, mHeight * 0.4f);
    }

    public void updateDebug(String s) {
        synchronized(mDebug) {
            mDebug.clear();
        }
        makeText(mDebug, s, mWidth * 0.5f, 0.4f);
    }

	public void makeText(List<GeneralObject> a, String msg, float posx, float posy) {
		float del = 0.4f;
        a.clear();
		for (int i = 0; i < msg.length(); ++i) {
			if (msg.charAt(i) != ' ') {
                a.add(new GeneralObject(this, true, posx + del * i, posy,
                        0.2f, 0.2f, GeneralObject.ObjectType.Letter,
                        0.0f, 0.0f, 100.0f, false, false,
                        Character.toString(msg.charAt(i))));
			}
		}
	}

	public static void setAdjustedPPM(float x, float y) {
		setPPM(x / 8.44f);
	}

    public static float screenSizeToGamex(float x) {
        return x / PPM;
    }

    public static float screenSizeToGamey(float y) {
        return y / PPM;
    }

    public static float gameSizeToScreenx(float x) {
        return x * PPM;
    }

    public static float gameSizeToScreeny(float y) {
        return y * PPM;
    }

	public static List<GeneralObject> getGeneralObjects() {
		return generalObjects;
	}

	public float getHeight() {
		return mHeight;
	}

	public void setHeight(float height) {
		mHeight = height;
	}

	public float getWidth() {
		return mWidth;
	}

	public void setWidth(float width) {
		mWidth = width;
	}

	public int getKillCount() {
		return mScore;
	}

	public void setScore(int set_score) {
        if (mLivesRemaining > 0) {
            mScore = set_score;
            updateScore(mScore);
        }
	}

	public static void setPPM(float PPM) {
		Game.PPM = PPM;
	}

	public SoundPool getSoundPool() {
		return mSoundPool;
	}

	public int getSoundExplosionID() {
		return mSoundExplosionID;
	}

	public float getStreamVolume() {
		return mStreamVolume;
	}

	public int getLivesRemaining() {
		return mLivesRemaining;
	}

	public void setLivesRemaining(int livesRemaining) {
        if (livesRemaining >= 0) {
            mLivesRemaining = livesRemaining;
            if ((mLivesRemaining <= 0) && (centerMessage.isEmpty())) {
                updateCenterMessage();
                MainActivity.showAdShortTime();
                mTimeGameOver = System.currentTimeMillis();
                mTimeToWaitUntilNewGame = mTimeGameOver + mTimeDelayToWaitUntilNewGame;
                editor = prefs.edit();
                int get_hi_score = prefs.getInt("key", 0);
                if (get_hi_score < mScore) {
                    editor.putInt("key", mScore);
                    editor.commit();
                }
                get_hi_score = prefs.getInt("key", 0);
                updateHiScore(get_hi_score);
            }
        }
	}
}