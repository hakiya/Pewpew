package com.pewpew.orbit_shooter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Hiroshi on 7/7/2014.
 */
public class GeneralObject {

    public enum ObjectType {
        Letter(null),
        Explosion(null),
        Spark(null),
        Star(null),
        DropPoint(null),
        PhysicalObject(null),
            Enemy(PhysicalObject),
                Can(Enemy),
                RedHexStar(Enemy),
                CrescentMoon(Enemy),
                Donut(Enemy),
                LightBlueCross(Enemy),
                BlueDiamond(Enemy),
                GreenSquare(Enemy),
            HomePlanet(PhysicalObject),
            Bullet(PhysicalObject),
                OrangeStar(Bullet),
                YellowStar(Bullet),
                GreenStar(Bullet);
        private ObjectType parent = null;

        private ObjectType(ObjectType parent) {
            this.parent = parent;
        }

        public boolean is(ObjectType other) {
            if (other == null) {
                return false;
            }
            for (ObjectType t = this; t != null; t = t.parent) {
                if (other == t) {
                    return true;
                }
            }
            return false;
        }
    }

    public Game game;
    public ObjectType type;
    public boolean used;
	public float mAngle;    //Angle of Object
	public float posx;    //Position of Object
    public float posy;    //Position of Object
    public float mass;    //Mass of Object
    public float posNextx;    //Next Position of Object (only used for physics)
    public float posNexty;    //Next Position of Object (only used for physics)
    public float velx;    //Velocity of Object
    public float vely;    //Velocity of Object
    public float velNextx;
    public float velNexty;
    public float sizex;
    public float sizey;
	public float atlasx;    //Upper left corner position in image atlas
    public float atlasy;    //Upper left corner position in image atlas
	public float atlasLenx;	//Relative width and length inside image atlas
    public float atlasLeny;	//Relative width and length inside image atlas
    public float alpha;
	public static int[] mTextures;
	public FloatBuffer mTextureBuffer;    //Texture buffer
	public float[] mTextureCoordinates;    //Texture coordinates
	public boolean mDestroy;	//Destroy Object on next iteration
	public FloatBuffer mVertBuffer;	//Vertices buffer
	public float[] mVertices;	//Vertices for location of image
	public float[] animatex;	//Array to store atlas map locations of frames
    public float[] animatey;	//Array to store atlas map locations of frames
	public int mAnimateIndex;	//Index of current animation frame
	public long mAnimateDelayPerFrame;	//Delay between frames
	public long mAnimateNextFrameTime;	//Time to draw next frame
	public boolean mAnimateEnable;	//Set whether to animate the frame or not
	public boolean mAnimateEndReached;	//Flag to tell end of animation has reached
    public boolean mHasShadow;    //Has shadow
    public float SHADOW_SHIFT;  //Amount to shift shadow by
    public boolean mIsOrbiting;    //private int mRadius;
    public long bulletSparkNextTime = 100;     //time to make next spark
    public long bulletSparkNextTimeDelta = 50;    //time delta between bullet sparks
    //Random
    public Random rand;

	public GeneralObject(Game g, boolean used, float posx, float posy, float sizex, float sizey, ObjectType t,
                         float velx, float vely, float mass, boolean orbiting, boolean isMobile,
                         String letter) {
        game = g;
        SHADOW_SHIFT = game.getWidth();
        mVertices = new float[12];
        rand = new Random();
        atlasx = 5.0f;
        atlasy = 0.0f;
        atlasLenx = 1.0f;
        atlasLeny = 1.0f;
        this.velx = velx;
        this.vely = vely;
        velNextx = 0.0f;
        velNexty = 0.0f;
        this.posx = posx;
        this.posy = posy;
        posNextx = 0.0f;
        posNexty = 0.0f;
        this.sizex = sizex;
        this.sizey = sizey;
        animatex = new float[] {
                4.125f, 4.625f, 4.125f, 4.625f
        };
        animatey = new float[] {
                2.125f, 2.125f, 2.625f, 2.625f
        };
        mTextureCoordinates = new float[] {
                ((atlasx + 0.0f) / 8.0f) + 0.0f / 128.0f,
                ((atlasy + atlasLeny) / 8.0f) - 0.0f / 128.0f,

                ((atlasx + 0.0f) / 8.0f) + 0.0f / 128.0f,
                ((atlasy + 0.0f) / 8.0f) + 0.0f / 128.0f,

                ((atlasx + atlasLenx) / 8.0f) + 0.0f / 128.0f,
                ((atlasy + atlasLeny) / 8.0f) + 0.0f / 128.0f,

                ((atlasx + atlasLenx) / 8.0f) + 0.0f / 128.0f,
                ((atlasy + 0.0f) / 8.0f) + 0.0f / 128.0f
        };
        mTextures =  MyGLRenderer.mTextures;
        mDestroy = false;
        initializePoolObject(g, used, posx, posy, sizex, sizey, t, velx, vely, mass, orbiting,
                isMobile, letter);
		refreshVertices();

	}

    public void initializePoolObject(Game g, boolean used, float posx, float posy, float sizex,
                                     float sizey, ObjectType t, float velx, float vely, float mass,
                                     boolean orbiting, boolean isMobile, String letter) {
        this.game = g;
        this.used = used;
        type = t;
        this.posx = posx;
        this.posy = posy;
        this.sizex = sizex;
        this.sizey = sizey;
        SHADOW_SHIFT = game.getWidth();
        this.velx = velx;
        this.vely = vely;
        mAngle = 0;
        this.sizex = sizex;
        this.sizey = sizey;
        mAnimateIndex = 0;
        mAnimateEnable = false;
        mAnimateEndReached = false;
        mHasShadow = false;
        mDestroy = false;
        alpha = 1.0f;
        switch (type) {
            case RedHexStar:
                atlasx = 5.0f;
                atlasy = 0.0f;
                atlasLenx = 1.0f;
                atlasLeny = 1.0f;
                mHasShadow = true;
                break;
            case Can:
                atlasx = 2.0f;
                atlasy = 1.0f;
                atlasLenx = 1.0f;
                atlasLeny = 1.0f;
                mHasShadow = true;
                break;
            case CrescentMoon:
                atlasx = 4.0f;
                atlasy = 1.0f;
                atlasLenx = 1.0f;
                atlasLeny = 1.0f;
                mHasShadow = true;
                break;
            case Donut:
                atlasx = 3.0f;
                atlasy = 1.0f;
                atlasLenx = 1.0f;
                atlasLeny = 1.0f;
                mHasShadow = true;
                break;
            case DropPoint:
                atlasx = 1.0f;
                atlasy = 1.0f;
                atlasLenx = 1.0f;
                atlasLeny = 1.0f;
                break;
            case Explosion:
                atlasx = 0.0f;
                atlasy = 2.0f;
                atlasLenx = 1.0f;
                atlasLeny = 1.0f;
                break;
            case Spark:
                int r = rand.nextInt(4);
                switch(r) {
                    case 0:
                        atlasx = 5.125f;
                        atlasy = 2.125f;
                        break;
                    case 1:
                        atlasx = 5.625f;
                        atlasy = 2.125f;
                        break;
                    case 2:
                        atlasx = 5.125f;
                        atlasy = 2.625f;
                        break;
                    case 3:
                        atlasx = 5.625f;
                        atlasy = 2.625f;
                        break;
                }
                atlasLenx = 0.25f;
                atlasLeny = 0.25f;
                mHasShadow = true;
                break;
            case Star:
                atlasx = 4.125f;
                atlasy = 2.125f;
                atlasLenx = 0.25f;
                atlasLeny = 0.25f;
                mHasShadow = true;
                break;
            case HomePlanet:
                atlasx = 2.0f;
                atlasy = 0.0f;
                atlasLenx = 1.0f;
                atlasLeny = 1.0f;
                mHasShadow = true;
                break;
            case Letter:
                atlasx = 6.0f;
                atlasy = 0.0f;
                atlasLenx = 0.5f;
                atlasLeny = 0.5f;
                break;
            case OrangeStar:
                atlasx = 0.0f;
                atlasy = 1.0f;
                atlasLenx = 1.0f;
                atlasLeny = 1.0f;
                mHasShadow = true;
                break;
            case LightBlueCross:
                atlasx = 4.0f;
                atlasy = 0.0f;
                atlasLenx = 1.0f;
                atlasLeny = 1.0f;
                mHasShadow = true;
                break;
            case BlueDiamond:
                atlasx = 5.0f;
                atlasy = 1.0f;
                atlasLenx = 1.0f;
                atlasLeny = 1.0f;
                mHasShadow = true;
                break;
            case GreenSquare:
                atlasx = 3.0f;
                atlasy = 0.0f;
                atlasLenx = 1.0f;
                atlasLeny = 1.0f;
                mHasShadow = true;
                break;
            case YellowStar:
                atlasx = 0.0f;
                atlasy = 0.0f;
                atlasLenx = 1.0f;
                atlasLeny = 1.0f;
                mHasShadow = true;
                break;
            case GreenStar:
                atlasx = 1.0f;
                atlasy = 0.0f;
                atlasLenx = 1.0f;
                atlasLeny = 1.0f;
                mHasShadow = true;
        }


        if (t.is(ObjectType.PhysicalObject)) {
            this.velx = velx;
            this.vely = vely;
            velNextx = 0.0f;
            velNexty = 0.0f;
            posNextx = 0.0f;
            posNexty = 0.0f;
            this.mass = mass;
            mIsOrbiting = orbiting;
            mMobile = isMobile;
        }
        if (t.is(ObjectType.Enemy)) {
            mIsOrbiting = orbiting;
            mMobile = isMobile;
        }
        if (t.is(ObjectType.HomePlanet)) {
            mMobile = isMobile;
            mIsOrbiting = orbiting;
        }
        if (t.is(ObjectType.Explosion)) {
            animatex[0] = 0.0f;
            animatex[1] = 1.0f;
            animatex[2] = 2.0f;
            animatex[3] = 3.0f;
            animatey[0] = 2.0f;
            animatey[1] = 2.0f;
            animatey[2] = 2.0f;
            animatey[3] = 2.0f;
            mAnimateEnable = true;
            mAnimateIndex = 0;
            mAnimateDelayPerFrame = 50;
            mAnimateNextFrameTime = System.currentTimeMillis() + mAnimateDelayPerFrame;
        }
        if (t.is(ObjectType.Spark)) {
            mMobile = isMobile;
        }
        if (t.is(ObjectType.Star)) {
            animatex[0] = 4.125f;
            animatex[1] = 4.625f;
            animatex[2] = 4.125f;
            animatex[3] = 4.625f;
            animatey[0] = 2.125f;
            animatey[1] = 2.125f;
            animatey[2] = 2.625f;
            animatey[3] = 2.625f;
            mAnimateEnable = true;
            mAnimateIndex = 0;
            mAnimateDelayPerFrame = 250;
            mAnimateNextFrameTime = System.currentTimeMillis() + mAnimateDelayPerFrame;
        }
        if (t.is(ObjectType.Letter)) {

            if ((letter.charAt(0) >= 65) && (letter.charAt(0) <= 90)) {
                int x_val = letter.charAt(0) - 65;
                int y_val = x_val / 6;
                x_val = x_val - y_val * 6;
                atlasx = ((float) x_val * 0.5f);
                atlasy = (float) y_val * 0.5f + 3.0f;
            } else if ((letter.charAt(0) >= 48) && (letter.charAt(0) <= 57)) {
                int x_val = letter.charAt(0) - 48 + 2;
                int y_val = x_val / 6;
                x_val = x_val - y_val * 6;
                atlasx = ((float)x_val * 0.5f);
                atlasy = (float) y_val * 0.5f + 5.0f;
            }

            atlasLenx = 0.5f;
            atlasLeny = 0.5f;
            setTextureCoordinates();
        }

        refreshVertices();


    }

    public void finalizePoolObject() {
        game = null;
        SHADOW_SHIFT = 0;
        used = false;
        type = ObjectType.Letter;
        posx = 0.0f;
        posy = 0.0f;
        velx = 0.0f;
        vely = 0.0f;
        sizex = 0.0f;
        sizey = 0.0f;
        mAnimateIndex = 0;
        mAnimateEnable = false;
        mAnimateEndReached = false;
        atlasx = 0.0f;
        atlasy = 0.0f;
        atlasLenx = 0.5f;
        atlasLeny = 0.5f;
        mHasShadow = false;
        alpha = 1.0f;
        mIsOrbiting = false;
        mMobile = false;
        velNextx = 0.0f;
        velNexty = 0.0f;
        posNextx = 0.0f;
        posNexty = 0.0f;
        animatex[0] = 0.0f;
        animatex[1] = 1.0f;
        animatex[2] = 2.0f;
        animatex[3] = 3.0f;
        animatey[0] = 2.0f;
        animatey[1] = 2.0f;
        animatey[2] = 2.0f;
        animatey[3] = 2.0f;
        mAnimateIndex = 0;
        mAnimateDelayPerFrame = 50;
        mAnimateNextFrameTime = System.currentTimeMillis() + mAnimateDelayPerFrame;
        setTextureCoordinates();
        refreshVertices();
    }

	public void draw(GL10 gl) {


		if (mAnimateEnable) {
			if (mAnimateNextFrameTime < System.currentTimeMillis()) {
				if (animatex.length - 1 <= mAnimateIndex) {
					mAnimateEndReached = true;
					endOfAnimation();
				} else {
					mAnimateIndex++;
					mAnimateNextFrameTime = System.currentTimeMillis() + mAnimateDelayPerFrame;
					atlasx = animatex[mAnimateIndex];
                    atlasy = animatey[mAnimateIndex];
					setTextureCoordinates();
				}
			}
		}
        if (type.is(ObjectType.Spark)) {
            posx = posx + velx * game.physics.mT;
            posy = posy + vely * game.physics.mT;
            if (alpha > 0.0f) {
                alpha -= 0.04f;
            } else {
                mDestroy = true;
            }
        }
        if (type.is(ObjectType.Bullet)) {
            if (bulletSparkNextTime < System.currentTimeMillis()) {
                game.bulletSpark.add(Game.bulletSparkPool.newObject(game, true,
                        posx, posy,
                        0.2f, 0.2f,
                        GeneralObject.ObjectType.Spark,
                        velx * (-1.0f) + game.rand.nextFloat() * 3.0f - 1.5f,
                                vely * (-1.0f) + game.rand.nextFloat() * 3.0f - 1.5f,
                        0.0f, false, true, "abc"));
                bulletSparkNextTime = System.currentTimeMillis() + bulletSparkNextTimeDelta;
            }
        }

		gl.glPushMatrix();

		gl.glTranslatef(Game.gameSizeToScreenx(posx),
                Game.gameSizeToScreeny(posy), 1.0f);

		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextures[0]);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        if(mHasShadow) {
            gl.glTranslatef(SHADOW_SHIFT, -1.0f * SHADOW_SHIFT, 0.0f);
            gl.glRotatef(mAngle * 57.295779579f, 0.0f, 0.0f, 1.0f);
            gl.glColor4f(0.9f, 0.9f, 0.9f, alpha * 0.5f);
            gl.glFrontFace(GL10.GL_CW);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertBuffer);
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextureBuffer);
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, mVertices.length / 3);
            gl.glRotatef(mAngle * -57.295779579f, 0.0f, 0.0f, 1.0f);
            gl.glTranslatef(-1.0f * SHADOW_SHIFT, SHADOW_SHIFT, 0.0f);
        }
        gl.glRotatef(mAngle * 57.295779579f, 0.0f, 0.0f, 1.0f);
        gl.glColor4f(1.0f, 1.0f, 1.0f, alpha);
        gl.glFrontFace(GL10.GL_CW);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextureBuffer);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, mVertices.length / 3);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glPopMatrix();
	}

	public void setTextureCoordinates() {
//		mTextureCoordinates = new float[] {
//				((mAtlas.x + 0.0f) / 8.0f) + 0.0f / 128.0f,
//				((mAtlas.y + mAtlasLen.y) / 8.0f) - 0.0f / 128.0f,
//
//				((mAtlas.x + 0.0f) / 8.0f) + 0.0f / 128.0f,
//				((mAtlas.y + 0.0f) / 8.0f) + 0.0f / 128.0f,
//
//				((mAtlas.x + mAtlasLen.x) / 8.0f) + 0.0f / 128.0f,
//				((mAtlas.y + mAtlasLen.y) / 8.0f) + 0.0f / 128.0f,
//
//				((mAtlas.x + mAtlasLen.x) / 8.0f) + 0.0f / 128.0f,
//				((mAtlas.y + 0.0f) / 8.0f) + 0.0f / 128.0f
//		};
        mTextureCoordinates[0] = ((atlasx + 0.0f) / 8.0f) + 0.0f / 128.0f;
        mTextureCoordinates[1] = ((atlasy + atlasLeny) / 8.0f) - 0.0f / 128.0f;

        mTextureCoordinates[2] = ((atlasx + 0.0f) / 8.0f) + 0.0f / 128.0f;
        mTextureCoordinates[3] = ((atlasy + 0.0f) / 8.0f) + 0.0f / 128.0f;

        mTextureCoordinates[4] = ((atlasx + atlasLenx) / 8.0f) + 0.0f / 128.0f;
        mTextureCoordinates[5] = ((atlasy + atlasLeny) / 8.0f) + 0.0f / 128.0f;

        mTextureCoordinates[6] = ((atlasx + atlasLenx) / 8.0f) + 0.0f / 128.0f;
        mTextureCoordinates[7] = ((atlasy + 0.0f) / 8.0f) + 0.0f / 128.0f;

		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(mTextureCoordinates.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		mTextureBuffer = byteBuffer.asFloatBuffer();
		mTextureBuffer.put(mTextureCoordinates);
		mTextureBuffer.position(0);
	}

	private void refreshVertices() {
		mVertices[0] = Game.gameSizeToScreenx(sizex) * -1.0f;
		mVertices[1] = Game.gameSizeToScreeny(sizey) * -1.0f;
		mVertices[2] = 0.0f;

		mVertices[3] = Game.gameSizeToScreenx(sizex) * -1.0f;
		mVertices[4] = Game.gameSizeToScreeny(sizey) * 1.0f;
		mVertices[5] = 0.0f;

		mVertices[6] = Game.gameSizeToScreenx(sizex) * 1.0f;
		mVertices[7] = Game.gameSizeToScreeny(sizey) * -1.0f;
		mVertices[8] = 0.0f;

		mVertices[9] = Game.gameSizeToScreenx(sizex) * 1.0f;
		mVertices[10] = Game.gameSizeToScreeny(sizey) * 1.0f;
		mVertices[11] = 0.0f;

		setVertices(mVertices);

		setTextureCoordinates();
	}

	public void setVertices(float[] vertices) {
		mVertices = vertices;
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(mVertices.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		mVertBuffer = byteBuffer.asFloatBuffer();
		mVertBuffer.put(mVertices);
		mVertBuffer.position(0);
	}

	protected void endOfAnimation() {
        if (type.is(ObjectType.Explosion)) {
            mDestroy = true;
        } else if (type.is(ObjectType.Star)) {
            mAnimateIndex = 0;
        }
	}

	//Getters and Setters

	public void setAtlas(float atlasx, float atlasy) {
		this.atlasx = atlasx;
        this.atlasy = atlasy;
	}

	public void setAtlasLen(float atlasLenx, float atlasLeny) {
		this.atlasLenx = atlasLenx;
        this.atlasLeny = atlasLeny;
	}

	public float getAngle() {
		return mAngle;
	}

	public void setAngle(float angle) {
		mAngle = angle;
	}

	public boolean isDestroy() {
		return mDestroy;
	}

    //Object moves
    protected boolean mMobile = true;

    public void collision(GeneralObject a) {
        if (type.is(ObjectType.Enemy)) {
            if (a.type.is(ObjectType.Bullet)) {
                mDestroy = true;
            }
            if (a.type.is(ObjectType.HomePlanet)) {
                if (!mDestroy) {
                    game.setLivesRemaining(game.getLivesRemaining() - 1);
                    game.updateLives(game.getLivesRemaining());
                }
                mDestroy = true;
            }
        }
        if (type.is(ObjectType.Bullet)) {
            if (a.type.is(ObjectType.Bullet)) {
                mDestroy = true;
            }
            if (a.type.is(ObjectType.Enemy)) {
                mDestroy = true;
            }
        }
    }

    //Getters and Setters

    public float getMass() {
        return mass;
    }

    public boolean isMobile() {
        return mMobile;
    }
}
