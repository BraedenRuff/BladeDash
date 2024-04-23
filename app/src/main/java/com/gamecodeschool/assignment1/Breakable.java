package com.gamecodeschool.assignment1;

import android.content.Context;

import kotlin.NotImplementedError;

/**
 * This class is for groundTiles that are breakable by the player
 * @author Braeden Ruff
 */
public class Breakable extends Ground
{
    //This tracks what the parents groundType is, where the parent is defined as the block directly above it
    GroundType parentGroundType;

    /**
     * This is the constructor for the breakable block
     * @param context - context of the program, not used this time
     * @param worldLocationX - center of the block on the x-axis
     * @param worldLocationY - center of the block on the y-axis
     * @param parentBlock - the Ground block directly above it
     */
    public Breakable(Context context, float worldLocationX, float worldLocationY, Ground parentBlock)
    {
        super(context, worldLocationX, worldLocationY, parentBlock.getGroundType());

        //set the parentGroundType
        parentGroundType = parentBlock.getGroundType();
        if(parentBlock.getGroundType() == GroundType.GRASS)
        {
            //we don't want to draw a grass block
            parentGroundType = GroundType.DIRT;
        }

        //have to reload the texture since the super() constructor loads it right away before we can set the parentGroundType
        GLManager.loadTexture(context, this);
    }

    /**
     * This method grabs the texture in R.drawable for the specified texture
     * @param context - context of the program, not used this time
     * @return pointer to where in R.drawable the texture file for the given enum of this class is
     */
    @Override
    public int getTextureResourceId(Context context)
    {
        if(parentGroundType == GroundType.DIRT)
        {
            return R.drawable.dirt_breakable;
        }
        else if(parentGroundType == GroundType.SANDSTONE)
        {
            return R.drawable.sandstone_breakable;
        }
        else if(parentGroundType == GroundType.MAGMASTONE)
        {
            return R.drawable.magmastone_breakable;
        }
        else
        {
            //skip, we will reload it when we know what it is, this is the loadTexture from Ground
            return -1;
        }
    }
}
