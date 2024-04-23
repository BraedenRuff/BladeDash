package com.gamecodeschool.assignment1;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a MonsterSpawner in the game, which is a type of Enemy.
 * It has the capability to spawn enemies, display messages, and manage its animations.
 * @author Braeden Ruff
 */
public class MonsterSpawner extends Enemy
{
    // Message object to display text on the screen, just a reference from GameManager
    Message message;

    /**
     * Constructor for MonsterSpawner.
     * @param context The application context, used for accessing resources.
     * @param worldLocationX The initial X coordinate of the spawner in the game world.
     * @param worldLocationY The initial Y coordinate of the spawner in the game world.
     * @param message The message object used for displaying text like "YOU WIN".
     */
    public MonsterSpawner(Context context, int worldLocationX, int worldLocationY, Message message)
    {
        super(context);
        this.message = message;
        setMaxVelocity(6 * GameManager.getPixelsPerMeter());
        setMaxAccel(GameManager.getPixelsPerMeter() / 5f);
        aggroRange = 0;

        setWorldLocation(worldLocationX, worldLocationY);
        float width = 2 * GameManager.getPixelsPerMeter();
        float height = 2 * GameManager.getPixelsPerMeter();
        setSize(width, height);

        float halfW = width / 2;
        float halfH = height / 2;

        float[] vertices = new float[] {
                // Position         // Texture Coordinates
                -halfW, -halfH, 0,   0.0f, 1.0f,  // Bottom left corner
                halfW, -halfH, 0,    1.0f, 1.0f,  // Bottom right corner
                halfW, halfH, 0,     1.0f, 0.0f,  // Top right corner
                -halfW, halfH, 0,    0.0f, 0.0f   // Top left corner
        };
        hp = 3;
        setVertices(vertices);

        Map<AnimationState, int[]> animations = new HashMap<>();
        animations.put(AnimationState.IDLE, new int[] {0});
        animations.put(AnimationState.HIT, new int[] {0,2,4});
        animations.put(AnimationState.DIE, new int[] {0,2,4});
        //maybe a hit animation?

        animator = new Animator(animations, 100f); // 0.1f is the time each frame is displayed
        animator.setState(AnimationState.IDLE);
    }

    /**
     * Determines the animation row based on the MonsterSpawners's current state.
     * @param state The current animation state of the MonsterSpawners.
     * @return The row index in the sprite sheet corresponding to the animation state.
     */
    @Override
    protected int getStateRow(AnimationState state)
    {
        switch(state)
        {
            case IDLE: return 0;
            case HIT: return 1;
            case DIE: return 2;
        }
        return 0;
    }

    /**
     * Basically only handles the animation, since it's stationary
     * @param fps helps determine how much time has passed since this was last called and therefore how much to move the enemy, not used
     * @param p a reference to the player, not used
     */
    @Override
    public void update(long fps, Player p)
    {
        animator.update();
        if(System.currentTimeMillis() - startInvincibility > invincibilityTime)
        {
            animator.setState(AnimationState.IDLE);
        }
    }

    /**
     * Retrieves the texture resource ID for the MonsterSpawner, setting up sprite sheet dimensions.
     * @param context The application context, used to access drawable resources.
     * @return The resource ID of the MonsterSpawner sprite sheet.
     */
    @Override
    public int getTextureResourceId(Context context)  {
        //animation stuff
        Drawable drawable = context.getDrawable(R.drawable.monsterspawner);
        spriteSheetWidth = drawable.getIntrinsicWidth();
        spriteSheetHeight = drawable.getIntrinsicHeight();
        numberOfColumnsInSpriteSheet = 5;
        numberOfRowsInSpriteSheet = 3;
        frameWidth = spriteSheetWidth / numberOfColumnsInSpriteSheet;
        frameHeight = spriteSheetHeight / numberOfRowsInSpriteSheet;
        return R.drawable.monsterspawner;
    }

    /**
     * Allows the monster spawner to take damage, and displays "YOU WIN" text
     */
    @Override
    public void takeDamage()
    {
        super.takeDamage();
        if(hp == 0)
        {
            message.generateText("YOU WIN");
        }
    }
}
