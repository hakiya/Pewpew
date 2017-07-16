package com.pewpew.orbit_shooter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Hiroshi on 5/10/2014.
 * <p/>
 * Issues:
 * 2014-06-29: floating-point precision of calculations will cause errors over time
 * for scenarios expecting perfect orbits.
 */
public class Physics {

	private Game game;
	//private static ArrayList<GeneralObject> getObj();

	private float mG = 0.10f;
	private boolean mSimGravity = true;
	private float mG_neg = 0.1f;

	private final float MAX_FORCE = 1000.0f;
	private final float MAX_BOUNCE_FORCE = 100000.0f;
	private final float MIN_DIST = 0.001f;

    public final int EXPLOSION_PARTICLES = 20;

	public final float mT = 1.0f / 60.0f;
    private static ExecutorService executor;
    private static ArrayList<WorkerThread> workers;

	public Physics(Game g) {
        executor = Executors.newFixedThreadPool(4);
        workers = new ArrayList<WorkerThread>();
        game = g;
    }

	//Solve physics for given time step
	public void step() {
		updateState();
	}

	//Update the velocities and positions
	private void updateState() {

        synchronized(Game.generalObjectsLock) {
            List<GeneralObject> phys = getObj();
            int j_size = phys.size();
            int max_index = j_size - 1;
            int pool_num = 1 + max_index / 10;

            while (pool_num > workers.size()) {
                workers.add(new WorkerThread(0, 0));
                executor.execute(workers.get(workers.size() - 1));
            }

            for (int i = 0; i < pool_num; ++i) {
                if (i < pool_num - 1) {
                    //Runnable worker = new WorkerThread(i * 10, i * 10 + 10);
                    //executor.execute(worker);
                    workers.get(i).setStartEnd(i * 10, i * 10 + 10);

                } else if (i == pool_num - 1) {
                    //Runnable worker = new WorkerThread(i * 10, i * 10 + 1 + (max_index % 10));
                    //executor.execute(worker);
                    workers.get(i).setStartEnd(i * 10, i * 10 + 1 + (max_index % 10));
                    //executor.execute(workers.get(i));
                }
            }
            //executor.shutdown();
            while(true) {
                boolean done = true;
                for (WorkerThread work : workers) {
                    if (!work.done) {
                        done = false;
                    }
                }
                if (done) {
                    break;
                }
            }
            //while(!executor.isTerminated()) {
            //}

            int i = 0;
            // TODO: Clean up destroy routine

            //List<GeneralObject> phys = getObj();
            int i_size = phys.size();
            while (i < i_size) {
                if (phys.get(i).isMobile()) {
                    phys.get(i).posx = phys.get(i).posNextx;
                    phys.get(i).posy = phys.get(i).posNexty;

                    phys.get(i).velx = phys.get(i).velNextx;
                    phys.get(i).vely = phys.get(i).velNexty;
                }
                if (phys.get(i).isDestroy() &
                        !phys.get(i).type.is(GeneralObject.ObjectType.HomePlanet)) {
                    if (!(phys.get(i).type.is(GeneralObject.ObjectType.Bullet))) {
                        game.setScore(game.getKillCount() + 1);
                        game.explosions.add(Game.explosionsPool.newObject(game, true,
                                phys.get(i).posx, phys.get(i).posy,
                                0.4f, 0.4f,
                                GeneralObject.ObjectType.Explosion,
                                0.0f, 0.0f, 100.0f, false, false,
                                "abc"));
                        for (int ii = 0; ii < EXPLOSION_PARTICLES; ++ii ) {
                            game.explosions.add(Game.explosionsPool.newObject(game, true,
                                    phys.get(i).posx, phys.get(i).posy,
                                    0.2f, 0.2f,
                                    GeneralObject.ObjectType.Spark,
                                    game.rand.nextFloat() * 10.0f - 5.0f,
                                    game.rand.nextFloat() * 10.0f - 5.0f,
                                    0.0f, false, true, "abc"));
                        }
                        game.getSoundPool().play(game.getSoundExplosionID(),
                                game.getStreamVolume(), game.getStreamVolume(), 0, 0, 1);
                    }
                    Game.generalObjectsPool.freeObject(getObj().get(i));
                    getObj().remove(i);
                    --i_size;
                }
                ++i;
            }
        }
	}

	//Sets the pointer to the physical object array list
	private List<GeneralObject> getObj() {
		return game.getGeneralObjects();
	}

	//Calculates the distance between the two objects
	private float calcDist(GeneralObject a, GeneralObject b) {
		float dist = (float) Math.sqrt(
				(Math.pow((a.posx - b.posx), 2.0f)
						+ Math.pow((a.posy - b.posy), 2.0f))
		);
		if (dist <= MIN_DIST) {
			return MIN_DIST;
		} else {
			return dist;
		}
	}

	private float calcCenterDist(GeneralObject a, GeneralObject b) {
		return (float) Math.sqrt(
				(Math.pow(0.5f * (a.sizex + b.sizex), 2.0f)
						+ Math.pow(0.5f * (a.sizey + b.sizey), 2.0f))
		);
	}

	private float calcDistNext(GeneralObject a, GeneralObject b) {
		return (float) Math.sqrt(
				(Math.pow((a.posNextx - b.posNextx), 2.0f)
						+ Math.pow((a.posNexty - b.posNexty), 2.0f))
		);
	}

	private float calcMag(float vecx, float vecy) {
		return (float) Math.sqrt( Math.pow(vecx, 2.0f) + Math.pow(vecy, 2.0f));
	}

	//Calculates the force between the two objects
	private float calcForce(GeneralObject a, GeneralObject b, float dist) {
        float centerDist = calcCenterDist(a, b);
		if (dist != 0) {
			if ((dist > centerDist) && mSimGravity) {
				return ((mG * a.getMass() * b.getMass()) / ((float) Math.pow(dist, 2.0f)));
			} else if (dist < centerDist) {
				a.collision(b);
				b.collision(a);
				return (-1) * ((mG_neg * a.getMass() * b.getMass()) / ((float) Math.pow(dist, 2.0f)));
			} else {
				return 0.0f;
			}
		} else {
			return MAX_FORCE;
		}
	}

	public boolean isSimGravity() {
		return mSimGravity;
	}

	public void setSimGravity(boolean simGravity) {
		mSimGravity = simGravity;
	}

    private class WorkerThread implements Runnable {

        private int i_start;
        private int i_end;
        private Object lock;

        public boolean done;

        public WorkerThread(int i_start, int i_end) {
            this.i_start = i_start;
            this.i_end = i_end;
            lock = new Object();
            done = true;
        }

        public void setStartEnd(int i_start, int i_end) {
            synchronized(lock) {
                this.i_start = i_start;
                this.i_end = i_end;
            }
            calc();
        }

        public void calc() {
            done = false;
            synchronized (lock) {
                List<GeneralObject> phys = getObj();
                //int j_size = phys.size();
                float height = game.getHeight();
                float width = game.getWidth();

                for (int j = i_start; j < i_end; ++j) {
                    float vx_temp = 0.0f;
                    float vy_temp = 0.0f;
                    int k_size = phys.size();
                    for (int k = 0; k < k_size; ++k) {
                        if ((j != k) &&
                                (phys.get(j).type.is(GeneralObject.ObjectType.PhysicalObject))
                                && (phys.get(k).type.is(GeneralObject.ObjectType.PhysicalObject))) {
                            float jx = phys.get(j).posx;
                            float jy = phys.get(j).posy;
                            float jm = getObj().get(j).getMass();
                            float kx = phys.get(k).posx;
                            float ky = phys.get(k).posy;

                            float dist = calcDist(getObj().get(j), getObj().get(k));

                            float force = calcForce(getObj().get(j), getObj().get(k), dist);

                            if (force > MAX_FORCE) {
                                force = MAX_FORCE;
                            }
                            if (force < -1.0f * MAX_BOUNCE_FORCE) {
                                force = -1.0f * MAX_BOUNCE_FORCE;
                            }
                            vx_temp += ((mT * force) / jm) * ((kx - jx) / dist);
                            vy_temp += ((mT * force) / jm) * ((ky - jy) / dist);
                        }
                    }
                    if (phys.get(j).type.is(GeneralObject.ObjectType.PhysicalObject)) {
                        float vx_old = phys.get(j).velx;
                        float vy_old = phys.get(j).vely;

                        phys.get(j).velNextx = vx_old + vx_temp;
                        phys.get(j).velNexty = vy_old + vy_temp;

                        if (phys.get(j).posy > height) {
                            phys.get(j).velNexty = Math.abs(phys.get(j).velNexty) * -0.5f;
                            if (phys.get(j).type.is(GeneralObject.ObjectType.Bullet)) {
                                phys.get(j).mDestroy = true;
                            }
                        }
                        if (phys.get(j).posy < 0.0f) {
                            phys.get(j).velNexty = Math.abs(phys.get(j).velNexty) * 0.5f;
                            if (phys.get(j).type.is(GeneralObject.ObjectType.Bullet)) {
                                phys.get(j).mDestroy = true;
                            }
                        }
                        if (phys.get(j).posx > width) {
                            phys.get(j).velNextx = Math.abs(phys.get(j).velNextx) * -0.5f;
                            if (phys.get(j).type.is(GeneralObject.ObjectType.Bullet)) {
                                phys.get(j).mDestroy = true;
                            }
                        }
                        if (phys.get(j).posx < 0.0f) {
                            phys.get(j).velNextx = Math.abs(phys.get(j).velNextx) * 0.5f;
                            if (phys.get(j).type.is(GeneralObject.ObjectType.Bullet)) {
                                phys.get(j).mDestroy = true;
                            }
                        }

                        phys.get(j).posNextx = phys.get(j).posx + (phys.get(j).velNextx * mT);
                        phys.get(j).posNexty = phys.get(j).posy + (phys.get(j).velNexty * mT);
                    }
                }
            }
            done = true;
        }

        @Override
        public void run() {
            //calc();
        }
    }
}