package com.gamecodeschool.assignment1;

import android.content.Context;

/**
 * This class is an abstract class that holds many useful things for enemies
 * @author Braeden Ruff
 */
public abstract class Enemy extends GameObject
{
    //for animating enemy actions
    protected Animator animator;

    //how close the player can get without triggering them to attack the player
    protected float aggroRange;

    //how many hits the enemy can take before dying
    protected int hp;

    //for helping decide how long to show the enemies corpse
    private long deathTimeStart;

    //how wide the original sprite sheet is
    protected int spriteSheetWidth;

    //how high the original sprite sheet is
    protected int spriteSheetHeight;

    //how many columns that are frameWidth the sprite sheet is
    protected int numberOfColumnsInSpriteSheet;

    //how many rows that are frameHeight the sprite sheet is
    protected int numberOfRowsInSpriteSheet;

    //how wide a specific frame is
    protected float frameWidth;

    //how high a specific frame is
    protected float frameHeight;

    //the 8 coordinates that represent the s (x) and t (y) of four vertices
    protected float[] textureCoords;

    //how long before the enemy can be hit again
    protected float invincibilityTime;

    //when the enemy was last hit
    protected long startInvincibility;

    /**
     * This is the constructor for an enemy
     * @param context - context of the program, used to determine the spritesheet original width and height
     */
    public Enemy(Context context)
    {
        super(context);

        //default values
        aggroRange = 1;
        hp = 1;
        deathTimeStart = Long.MAX_VALUE;
        startInvincibility = 0;
        invincibilityTime = 2000f;

        //initialization
        textureCoords = new float[8];
    }

    /**
     * This method sets the facing angle to where the player is and determines if the player is in the aggroRange
     * @param p is the player
     * @return true if the player aggro'd the enemy, false if they didn't
     */
    public boolean playerInAggroRange(Player p)
    {
        setFacingAngle(InputController.getAngle(getWorldLocation(), p.getWorldLocation())); // note, this is the angle from a line straight up, so sin is x and cos is y
        float distanceX = p.getWorldLocation().x - getWorldLocation().x;
        float distanceY = p.getWorldLocation().y - getWorldLocation().y;
        if(distanceX * distanceX + distanceY * distanceY < getWidth() * getWidth() * aggroRange * aggroRange) // don't take sqrt cause it's slow, we just want to know if it's within a circle of aggroRange
        {
            return true;
        }
        return false;
    }

    /**
     * Updates the texture coordinates that allows you to draw the different frames of each enemy
     */
    protected void updateTextureCoords()
    {
        int frameIndex = animator.getCurrentFrame();
        AnimationState currentState = animator.getCurrentState();

        float textureX = frameIndex * frameWidth;

        // Determine the row in the sprite sheet based on the current state
        int row = getStateRow(currentState);
        float textureY = row * frameHeight;

        // Normalize texture coordinates
        float s = (textureX+2) / (spriteSheetWidth);
        float t = (textureY+2) / (spriteSheetHeight);
        float sMax = s + (frameWidth) / spriteSheetWidth;
        float tMax = t + (frameHeight) / spriteSheetHeight;
        if(InputController.getFacingRight(getFacingAngle()))
        {
            // Update textureCoords array
            textureCoords[0] = s; textureCoords[1] = tMax; // Bottom left corner
            textureCoords[2] = sMax; textureCoords[3] = tMax; // Bottom right corner
            textureCoords[4] = sMax; textureCoords[5] = t; // Top right corner
            textureCoords[6] = s; textureCoords[7] = t; // Top left corner
        }
        else //mirror horizontally
        {
            // Update textureCoords array
            textureCoords[0] = sMax; textureCoords[1] = tMax; // Bottom left corner
            textureCoords[2] = s; textureCoords[3] = tMax; // Bottom right corner
            textureCoords[4] = s; textureCoords[5] = t; // Top right corner
            textureCoords[6] = sMax; textureCoords[7] = t; // Top left corner
        }


        // Update textureCoords array
        updateVerticesTextureCoords(textureCoords);

    }

    /**
     * This method gets the row of the AnimationState that the enemy is currently in
     * @param state the AnimationState the enemy is currently in
     * @return the row that corresponds to that animation in the spritesheet
     */
    protected abstract int getStateRow(AnimationState state);

    /**
     * This method draws the enemy
     * @param viewportMatrix the viewport so openGL can decide to draw the enemy or if it's off screen
     */
    protected void draw(float[] viewportMatrix)
    {
        updateTextureCoords();
        super.draw(viewportMatrix);
    }

    /**
     * This method updates the enemy position and does some animation state logic
     * @param fps helps determine how much time has passed since this was last called and therefore how much to move the enemy
     * @param p a reference to the player
     */
    public abstract void update(long fps, Player p);

    /**
     * This method allows the enemy to take damage and handles whether it should accept that damage or ignore it
     */
    public void takeDamage()
    {
        //the enemy hasn't been hit in a while and isn't dead
        if(hp > 0 && System.currentTimeMillis() - startInvincibility > invincibilityTime)
        {
            --hp;
            startInvincibility = System.currentTimeMillis();
            if(this instanceof MonsterSpawner) //for now, the only character with over 0 hp is the monster spawner, so this is ok
            {
                setAnimatorState(AnimationState.HIT);
            }
        }
        //the enemy died
        if(hp == 0 && deathTimeStart == Long.MAX_VALUE)
        {
            setAnimatorState(AnimationState.DIE);
            deathTimeStart = System.currentTimeMillis();
        }
    }

    /**
     * This method sets which animation to show
     * @param state the animation we want to show
     */
    public void setAnimatorState(AnimationState state)
    {
        animator.setState(state);
    }

    /**
     * This method gets the enemy hp
     * @return the hp
     */
    public int getHP()
    {
        return hp;
    }

    /**
     * This method gets the death time
     * @return the death time
     */
    public long getDeathTime()
    {
        return deathTimeStart;
    }
}
