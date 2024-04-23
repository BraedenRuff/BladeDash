package com.gamecodeschool.assignment1;

import java.util.Map;

/**
 * This class is used to animate the player and enemies, though can be extended to whatever is needed
 * @author Braeden Ruff
 */
public class Animator
{
    //this is the map for the states and frames of each animation
    private Map<AnimationState, int[]> animations;

    //this is the current animation state
    private AnimationState currentState;

    //this is the index of the current frame we are animating
    private int currentFrameIndex;

    //this is the time each frame is displayed
    private float frameTime;

    //this is the time elapsed since last frame change
    private float elapsedTime;

    //this is the last time, aka the current time - this one will be added to the elapsedTiem
    private long lastTime;

    /**
     * this is the animator constructor
     * @param animations the animationState-frame map
     * @param frameTime how long frames last
     */
    public Animator(Map<AnimationState, int[]> animations, float frameTime)
    {
        this.animations = animations;
        this.frameTime = frameTime;
        this.currentState = AnimationState.IDLE; // default state
        this.currentFrameIndex = 0;
        this.elapsedTime = 0;
        lastTime = System.currentTimeMillis();
    }

    /**
     * This method changes the animation state, sets the frame index to 0, and resets the time
     * it does not change the state or reset the time, or reset the frame index if the state is already the desired state
     * @param state the state we want to change to
     */
    public void setState(AnimationState state)
    {
        if (this.currentState != state)
        {
            this.currentState = state;
            this.currentFrameIndex = 0; // reset frame index on state change
            this.elapsedTime = 0;
            lastTime = System.currentTimeMillis();
        }
    }

    /**
     * this method gets the current state
     * @return the current state
     */
    public AnimationState getCurrentState()
    {
        return currentState;
    }

    /**
     * this method handles the logic of which animation frame to display
     */
    public void update()
    {
        long currTime = System.currentTimeMillis();
        elapsedTime += currTime - lastTime;
        lastTime = currTime;
        if (elapsedTime >= frameTime)
        {
            ++currentFrameIndex;
            int[] currentFrames = animations.get(currentState);
            if (currentFrameIndex >= currentFrames.length)
            {
                if(currentState == AnimationState.JUMP || currentState == AnimationState.HIT || currentState == AnimationState.DASH ||
                        currentState == AnimationState.DIE || currentState == AnimationState.CHARGE)
                {
                    --currentFrameIndex; // stay on the last frame of the current state
                }
                else if(currentState == AnimationState.ATTACK || currentState == AnimationState.LAND)
                {
                    setState(AnimationState.IDLE);
                }
                else
                {
                    currentFrameIndex = 0;// loop back to the first frame of the current state
                }
            }
            elapsedTime = 0;
        }
    }

    /**
     * this method gets the frame from the state-frame map that you are currently on
     * @return the frame you want to display
     */
    public int getCurrentFrame()
    {
        return animations.get(currentState)[currentFrameIndex];
    }
}

/**
 * this enum are the possible animations in the game currently
 */
enum AnimationState
{
    IDLE, WALK, RUN, JUMP, DIE, HIT, ATTACK, WALLRIDE, DASH, CHARGE, LAND
}