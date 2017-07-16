package com.pewpew.orbit_shooter;

/**
 * Created by Hiroshi on 9/2/2014.
 */
public class ObjectPool {
    protected final int MAX_FREE_OBJECT_INDEX;

    protected PoolObjectFactory factory;
    protected GeneralObject[] freeObjects;
    protected int freeObjectIndex = -1;

    /**
     * Constructor.
     *
     * @param factory the object pool factory instance
     * @param maxSize the maximun number of instances stored in the pool
     */
    public ObjectPool(PoolObjectFactory factory, int maxSize)
    {
        this.factory = factory;
        this.freeObjects = new GeneralObject[maxSize];
        MAX_FREE_OBJECT_INDEX = maxSize - 1;
    }

    /**
     * Creates a new object or returns a free object from the pool.
     *
     * @return a PoolObject instance already initialized
     */
    public synchronized GeneralObject newObject(
            Game g, boolean used, float posx, float posy, float sizex, float sizey,
            GeneralObject.ObjectType t, float velx, float vely, float mass, boolean orbiting,
            boolean isMobile, String letter)
    {

        GeneralObject obj = null;
        if (freeObjectIndex == -1)
        {
            // There are no free objects so I just
            // create a new object that is not in the pool.
            obj = factory.createPoolObject(g, used, posx, posy, sizex, sizey, t,
                    velx, vely, mass, orbiting, isMobile, letter);
        }
        else
        {
            // Get an object from the pool
            obj = freeObjects[freeObjectIndex];

            freeObjectIndex--;
        }

        // Initialize the object
        obj.initializePoolObject(g, used, posx, posy, sizex, sizey, t, velx, vely, mass,
                orbiting, isMobile, letter);
        return obj;
    }

    /**
     * Stores an object instance in the pool to make it available for a subsequent
     * call to newObject() (the object is considered free).
     *
     * @param obj the object to store in the pool and that will be finalized
     */
    public synchronized void freeObject(GeneralObject obj)
    {
        if (obj != null)
        {
            // Finalize the object
            obj.finalizePoolObject();
            // I can put an object in the pool only if there is still room for it
            if (freeObjectIndex < MAX_FREE_OBJECT_INDEX)
            {
                freeObjectIndex++;

                // Put the object in the pool
                freeObjects[freeObjectIndex] = obj;
            }
        }
    }
}
