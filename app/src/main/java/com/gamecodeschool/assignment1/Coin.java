package com.gamecodeschool.assignment1;

import android.content.Context;

/**
 * This is the coin class. It is a collectable the player is meant to pick up
 * @author Braeden Ruff
 */
public class Coin extends GameObject
{
    /**
     * This is the constructor for a coin
     * @param context - context of the program, not used this time
     * @param worldLocationX - center of the block on the x-axis
     * @param worldLocationY - center of the block on the y-axis
     */
    public Coin(Context context, float worldLocationX, float worldLocationY)
    {
        super(context);

        //coins don't move
        setMaxVelocity(0);
        setWorldLocation(worldLocationX, worldLocationY);

        float width = 1 * GameManager.getPixelsPerMeter();
        float height = 1 * GameManager.getPixelsPerMeter();
        setSize(width, height);

        setDefaultVertices();
    }

    /**
     * This method grabs the texture in R.drawable for the specified texture
     * @param context - context of the program, not used this time
     * @return pointer to where in R.drawable the texture file for the given enum of this class is
     */
    @Override
    public int getTextureResourceId(Context context)
    {
        return R.drawable.coin;
    }
}
