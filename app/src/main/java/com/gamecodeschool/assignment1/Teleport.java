package com.gamecodeschool.assignment1;

import android.content.Context;

/**
 * Represents a teleport object in the game, allowing for instant movement or level transition (only used for level transition right now)
 * @author Braeden Ruff
 */
public class Teleport extends GameObject
{
    /**
     * Constructs a Teleport instance at the specified location in the game world.
     * @param context The application context, used for accessing resources.
     * @param worldLocationX The X coordinate of the Teleport's location in the game world.
     * @param worldLocationY The Y coordinate of the Teleport's location in the game world.
     */
    public Teleport(Context context, float worldLocationX, float worldLocationY)
    {
        super(context);
        setMaxVelocity(0);

        float width = 1 * GameManager.getPixelsPerMeter();
        float height = 2 * GameManager.getPixelsPerMeter();
        setSize(width, height);
        setWorldLocation(worldLocationX, worldLocationY - height/4);

        setDefaultVertices();
    }

    /**
     * Returns the drawable resource ID for the Teleport texture.
     *
     * @param context The application context, used for accessing drawable resources.
     * @return The resource ID of the Teleport texture.
     */
    @Override
    public int getTextureResourceId(Context context) {
        return R.drawable.teleport;
    }
}
