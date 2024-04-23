package com.gamecodeschool.assignment1;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Goblin enemy in the game, inheriting from the Enemy class.
 * Goblins have specific behaviors like jumping and animations for idling, running, attacking, and dying.
 * @author Braeden Ruff
 */
public class Goblin extends Enemy
{
    // Tracks if the Goblin is currently jumping
    private boolean jumping;

    // Tracks if the Goblin is airborne to prevent double jumps, useful if they walk off an edge
    private boolean isAirborne;

    /**
     * Constructs a Goblin instance with initial settings like velocity, acceleration, and animations.
     * @param context The application context, used for accessing resources.
     * @param worldLocationX The initial X coordinate of the Goblin in the game world.
     * @param worldLocationY The initial Y coordinate of the Goblin in the game world.
     */
    public Goblin(Context context, float worldLocationX, float worldLocationY)
    {
        super(context);
        setMaxVelocity(6 * GameManager.getPixelsPerMeter());
        setMaxAccel(GameManager.getPixelsPerMeter() / 5f);
        aggroRange = 7; // 7 Goblin widths aggro range

        setWorldLocation(worldLocationX, worldLocationY);
        float width = 1 * GameManager.getPixelsPerMeter();
        float height = 1 * GameManager.getPixelsPerMeter();
        setSize(width, height);

        setDefaultVertices();

        Map<AnimationState, int[]> animations = new HashMap<>();
        animations.put(AnimationState.IDLE, new int[] {0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38}); // Indices of frames for idling
        animations.put(AnimationState.RUN, new int[] {0, 2, 4, 6, 8, 10, 12, 14, 16, 18}); // Indices of frames for running
        animations.put(AnimationState.ATTACK, new int[] {0, 2, 4, 6, 8, 10, 12, 14, 16, 18}); // Indices of frames for attacking
        animations.put(AnimationState.DIE, new int[] {0, 2, 4, 6, 8, 10, 12, 14, 16, 18}); // Indices of frames for dashing

        animator = new Animator(animations, 50f); // 0.1f is the time each frame is displayed
    }

    /**
     * Updates the Goblin's state each frame, handling movements, animations, and interactions with the player.
     * @param fps The current frames per second, affecting movement calculations.
     * @param p The player instance, used to check distance and interactions.
     */
    public void update(long fps, Player p)
    {
        if(fps == 0) //fixes the start before fps is initialized because we divide by it
        {
            return;
        }
        animator.update();
        applyGravity(fps);
        if(getHP() == 0)
        {
            comeToStop(fps);
            move(fps);
            return;
        }
        if(isJumping())
        {
            setyVelocity(getMaxVelocity());
            setJumping(false);
        }

        if(playerInAggroRange(p))
        {
            //the pursue player
            if(animator.getCurrentState() == AnimationState.IDLE)
            {
                animator.setState(AnimationState.RUN);
            }
            if (InputController.getFacingRight(getFacingAngle()))
            {
                setxVelocity(Math.min(getxVelocity() + (float) Math.cos(getFacingAngle()) * (getMaxVelocity() * getMaxAccel() / fps), getMaxVelocity()));
            }
            else // left
            {
                setxVelocity(Math.max(getxVelocity() - (float) Math.cos(getFacingAngle()) * (-getMaxVelocity() * getMaxAccel() / fps), -getMaxVelocity()));
            }
        }
        else
        {
            //not chasing player
            comeToStop(fps);
            if (getxVelocity() == 0 && animator.getCurrentState() == AnimationState.RUN)
            {
                animator.setState(AnimationState.IDLE);
            }
        }

        move(fps);
    }

    /**
     * Determines the animation row based on the Goblin's current state.
     * @param state The current animation state of the Goblin.
     * @return The row index in the sprite sheet corresponding to the animation state.
     */
    @Override
    protected int getStateRow(AnimationState state)
    {
        switch(state)
        {
            case IDLE: return 0;
            case RUN: return 1;
            case ATTACK: return 2;
            case DIE: return 3;
            default: return 0; // Default to the first row i.e. idle
        }
    }

    /**
     * This method gets whether the goblin is jumping or not
     * @return whether the goblin is jumping or not
     */
    public boolean isJumping()
    {
        return jumping;
    }

    /**
     * This method gets whether the goblin is airborne or not
     * @return whether the goblin is airborne or not
     */
    public boolean getIsAirborne()
    {
        return isAirborne;
    }

    /**
     * This method sets the goblin to be jumping
     * @param jumping whether the goblin is jumping or not
     */
    public void setJumping(boolean jumping)
    {
        if(!isAirborne)
        {
            this.jumping = true;
            isAirborne = true;
        }
        else
        {
            this.jumping = false;
        }
    }

    /**
     * This method sets whether the goblin is airborne or not
     * @param isAirborne whether the goblin is airborne or not
     */
    public void setIsAirborne(boolean isAirborne)
    {
        this.isAirborne = isAirborne;
    }

    /**
     * Retrieves the texture resource ID for the Goblin, setting up sprite sheet dimensions.
     * @param context The application context, used to access drawable resources.
     * @return The resource ID of the Goblin sprite sheet.
     */
    @Override
    public int getTextureResourceId(Context context){
        Drawable drawable = context.getDrawable(R.drawable.goblin);
        spriteSheetWidth = drawable.getIntrinsicWidth();
        spriteSheetHeight = drawable.getIntrinsicHeight();
        numberOfColumnsInSpriteSheet = 40;
        numberOfRowsInSpriteSheet = 4;
        frameWidth = spriteSheetWidth / numberOfColumnsInSpriteSheet;
        frameHeight = spriteSheetHeight / numberOfRowsInSpriteSheet;
        return R.drawable.goblin;
    }
}
