package com.pewpew.orbit_shooter;

/**
 * Created by Hiroshi on 10/4/2014.
 */
public class PoolObjectFactory {
    public GeneralObject createPoolObject(
            Game g, boolean used, float posx, float posy, float sizex, float sizey,
            GeneralObject.ObjectType t, float velx, float vely, float mass, boolean orbiting,
            boolean isMobile, String letter) {
        return new GeneralObject(g, used, posx, posy, sizex, sizey, t,
                velx, vely, mass, orbiting, isMobile, letter);
    }
}
