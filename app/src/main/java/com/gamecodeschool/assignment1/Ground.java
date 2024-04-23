package com.gamecodeschool.assignment1;

import android.content.Context;
import android.util.Log;

import kotlin.NotImplementedError;

/**
 * Represents the ground in the game, extending the GameObject class. Different ground types are
 * represented using an enum.
 * @author Braeden Ruff
 */
public class Ground extends GameObject
{
    /**
     * Represents the different types of ground
     */
    public enum GroundType {GRASS, DIRT, SANDSTONE, MAGMASTONE, DEATH}

    //the type of ground for this instance
    private GroundType groundType;

    /**
     * Constructor for the Ground class, setting up its type, location, and texture.
     * @param context The application context, used for accessing game resources.
     * @param worldLocationX The X coordinate of the ground in the game world.
     * @param worldLocationY The Y coordinate of the ground in the game world.
     * @param groundType The type of ground to be represented.
     */
    public Ground(Context context, float worldLocationX, float worldLocationY, GroundType groundType)
    {
        super(context);
        this.groundType = groundType;
        GLManager.loadTexture(context, this);
        setMaxVelocity(0);

        setWorldLocation(worldLocationX, worldLocationY);
        float width = 1 * GameManager.getPixelsPerMeter();
        float height = 1 * GameManager.getPixelsPerMeter();
        setSize(width, height);

        setDefaultVertices();
    }
    /**
     * Returns the resource ID of the texture corresponding to the ground type.
     * @param context The application context, not used in this implementation but required by the interface.
     * @return The resource ID of the texture for this ground type.
     */
    @Override
    public int getTextureResourceId(Context context)
    {
        if(groundType == null)
        {
            return -1;
        }
        switch (groundType)
        {
            case GRASS:
                return R.drawable.grass_block;
            case DIRT:
                return R.drawable.dirt;
            case SANDSTONE:
                return R.drawable.sandstone;
            case MAGMASTONE:
                return R.drawable.magmastone;
            case DEATH:
                return R.drawable.death;
            default:
                throw new NotImplementedError();
        }
    }

    /**
     * Getter for the ground type.
     * @return The type of this ground instance.
     */
    public GroundType getGroundType()
    {
        return groundType;
    }
}
