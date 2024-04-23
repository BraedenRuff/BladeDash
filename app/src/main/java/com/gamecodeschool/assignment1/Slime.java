package com.gamecodeschool.assignment1;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Slime enemy in the game, inheriting from the Enemy class.
 * Slimes can jump to attack and have different animations for idling, charging, jumping, and dying.
 * @author Braeden Ruff
 */
public class Slime extends Enemy
{
    // Tracks if the Slime is currently preparing to jump or in mid-air
    private boolean jumping;
    private boolean isAirborne;

    // Time management for jump charging
    private long startFrameTime;
    private float jumpChargeTime;

    /**
     * Constructs a Slime instance with initial settings for movement, jump charge time, and animations.
     * @param context The application context, used for accessing resources.
     * @param worldLocationX The initial X coordinate of the Slime in the game world.
     * @param worldLocationY The initial Y coordinate of the Slime in the game world.
     */
    public Slime(Context context, float worldLocationX, float worldLocationY)
    {
        super(context);
        setMaxVelocity(10 * GameManager.getPixelsPerMeter());
        jumpChargeTime = 1000f; // Time in milliseconds to charge a jump
        aggroRange = 7; // Aggression range to start jumping towards the player (7 tiles)

        startFrameTime = -1;
        setWorldLocation(worldLocationX, worldLocationY);

        float width = 1 * GameManager.getPixelsPerMeter();
        float height = 1 * GameManager.getPixelsPerMeter();
        setSize(width, height);

        setDefaultVertices();

        // Define animations for different states
        Map<AnimationState, int[]> animations = new HashMap<>();
        animations.put(AnimationState.IDLE, new int[] {0, 2, 4, 6, 8, 10, 12, 14}); // Indices of frames for idling
        animations.put(AnimationState.CHARGE, new int[] {0, 0, 0, 0, 2}); // Indices of frames for charging a jump attack
        animations.put(AnimationState.JUMP, new int[] {4, 6, 8, 10, 12, 14, 16, 18}); // Indices of frames for jumping (aka attack, but easier with jump)
        animations.put(AnimationState.DIE, new int[] {0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 4}); // Indices of frames for dying
        animations.put(AnimationState.LAND, new int[] {0, 2, 4}); // Indices of frames for landing

        animator = new Animator(animations, 100f);
        setAnimatorState(AnimationState.IDLE);
    }

    /**
     * Updates the Slime's state each frame, handling movements, animations, and interactions with the player.
     * @param fps The current frames per second, affecting movement calculations.
     * @param p The player instance, used to check distance and interactions.
     */
    public void update(long fps, Player p)
    {
        if(fps == 0) //fixes the start before fps is initialized when it starts it goes to -infinity
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

        if(playerInAggroRange(p) && !isAirborne)
        {
            setAnimatorState(AnimationState.CHARGE);
            if(startFrameTime == -1)
            {
                startFrameTime = System.currentTimeMillis();
            }
            setJumping(true);
        }
        else
        {
            if(animator.getCurrentState() == AnimationState.CHARGE)
            {
                setAnimatorState(AnimationState.IDLE);
            }
            setJumping(false);
            startFrameTime = -1;
        }

        if(isJumping())
        {
            setyVelocity(getMaxVelocity());
            if (InputController.getFacingRight(getFacingAngle()))
            {
                setxVelocity(getMaxVelocity());
            } else
            {
                setxVelocity(-getMaxVelocity());
            }
            setJumping(false);
            startFrameTime = -1;
        }
        if(!isAirborne)
        {
            if(animator.getCurrentState() == AnimationState.JUMP)
            {
                setAnimatorState(AnimationState.LAND);
            }
            comeToStop(fps);
            if(getxVelocity() == 0 && getyVelocity() == 0 && !jumping)
            {
                setAnimatorState(AnimationState.IDLE);
            }
        }
        move(fps);
    }

    /**
     * Determines the animation row based on the slime's current state.
     * @param state The current animation state of the slime.
     * @return The row index in the sprite sheet corresponding to the animation state.
     */
    @Override
    protected int getStateRow(AnimationState state) {
        // Map each state to its corresponding row in the sprite sheet
        switch(state) {
            case IDLE: return 0;
            case CHARGE: return 1;
            case JUMP: return 2;
            case DIE: return 3;
            case LAND: return 4;
            default: return 0; // Default to the first row i.e. idle
        }
    }

    /**
     * getter for if the slime is jumping
     * @return if the slime is jumping
     */
    public boolean isJumping()
    {
        return jumping;
    }

    /**
     * setter for making the slime jump or not
     * @param jumping if the slime is jumping or not
     */
    public void setJumping(boolean jumping)
    {
        if(jumping)
        {
            if(System.currentTimeMillis() - startFrameTime >= jumpChargeTime)
            {
                setAnimatorState(AnimationState.JUMP);
                this.jumping = true;
                isAirborne = true;
            }
        }
        else
        {
            this.jumping = false;
        }
    }

    /**
     * Setter for if the slime is in the air
     * @param isAirborne if the slime is in the air
     */
    public void setIsAirborne(boolean isAirborne)
    {
        this.isAirborne = isAirborne;
    }

    /**
     * Retrieves the texture resource ID for the slime, setting up sprite sheet dimensions.
     * @param context The application context, used to access drawable resources.
     * @return The resource ID of the slime sprite sheet.
     */
    @Override
    public int getTextureResourceId(Context context) {
        Drawable drawable = context.getDrawable(R.drawable.slime);
        spriteSheetWidth = drawable.getIntrinsicWidth();
        spriteSheetHeight = drawable.getIntrinsicHeight();
        numberOfColumnsInSpriteSheet = 20;
        numberOfRowsInSpriteSheet = 5;
        frameWidth = spriteSheetWidth / numberOfColumnsInSpriteSheet;
        frameHeight = spriteSheetHeight / numberOfRowsInSpriteSheet;
        return R.drawable.slime;
    }
}
