package com.gamecodeschool.assignment1;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glEnable;
import static com.gamecodeschool.assignment1.GLManager.FLOAT_SIZE;
import static com.gamecodeschool.assignment1.GLManager.POSITION_ATTRIBUTE_SIZE;
import static com.gamecodeschool.assignment1.GLManager.TEXTURE_COORDINATES_ATTRIBUTE_SIZE;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the player character in the game, handling movement, actions like dashing, jumping, and slashing,
 * and animations based on the player state.
 * @author Braeden Ruff
 */
public class Player extends GameObject
{
    // Indicates if the player is currently moving
    private boolean isMoving;

    // Indicates if the player is currently performing a dash action
    private boolean isDashing;

    // Ensures dash action can only be performed once per airtime
    private boolean dashed;

    // Distance covered during the dash action
    private float dashDistance;

    // Maximum distance the player can dash
    private float dashDistanceMax;

    // Maximum height the player can jump
    private float jumpMax;

    // Indicates if the player is sliding against a wall
    private boolean wallSliding;

    // Indicates if the player is airborne
    private boolean isAirborne;

    // Indicates if the player is performing a wall jump action
    private boolean isWallJumping;

    // Indicates if the player can be controlled (true) or is in a state that prevents control (false). This is mostly getting hit or dying
    private boolean controllable;

    // Duration the player is invincible after taking damage
    private float invincibilityTime;

    // Start time for invincibility period
    private long startInvincibility;

    // Player's health points
    private int hp;

    // Indicates if the player can take damage
    private boolean damageable;

    // Radius of the slashing action
    private float slashRadius;

    // Buffer for the vertex data of the slashing animation
    private FloatBuffer slashVertices;

    // Texture used for the slashing animation
    private int slashTexture;

    // Indicates if the player is currently slashing
    private boolean isSlashing;

    // Used because we only want to draw the slash for one frame
    private boolean isSlashDrawn;

    // Time required to recharge the slash action
    private int rechargeSlashTime = 100; // milliseconds

    // Start time for the slash action
    private long startSlashTime;

    // Number of vertices used for rendering the slash animation
    private int numSlashVertices;

    // Angle at which the slash is performed
    private double slashAngle;

    // Angular span of the slash effect
    private double slashSpan;

    // Animator for handling different player animations based on state
    private Animator animator;

    // Width and height of a single frame in the sprite sheet
    private float frameWidth, frameHeight;

    // Array holding texture coordinates for the current frame
    private float[] textureCoords;

    // Number of columns and rows in the player's sprite sheet
    private int numberOfColumnsInSpriteSheet;
    private int numberOfRowsInSpriteSheet;

    // Width and height of the player's sprite sheet
    private int spriteSheetWidth;
    private int spriteSheetHeight;

    // Flags for achievements
    private boolean slashedOnce;
    private boolean missedSlash;

    // Time when the player started dying
    private long deathStartTime;

    // This is used to activate god mode if the game is too hard
    private boolean godMode;

    /**
     * Constructs a player instance with initial settings at the specified location
     * @param context The application context, used for accessing resources.
     * @param worldLocationX The initial X coordinate of the Goblin in the game world.
     * @param worldLocationY The initial Y coordinate of the Goblin in the game world.
     * @param godMode Whether god mode is toggle on or off
     */
    public Player(Context context, float worldLocationX, float worldLocationY, boolean godMode) {
        super(context);
        resetState();
        this.godMode = godMode;
        //adjustable block for playability tweeks
        setMaxVelocity(12 * GameManager.getPixelsPerMeter());
        setMaxAccel(GameManager.getPixelsPerMeter() / 5f);
        jumpMax = 16 * GameManager.getPixelsPerMeter();
        // dash 5 player lengths
        dashDistanceMax = 2 * GameManager.getPixelsPerMeter();
        invincibilityTime = 3000;
        slashRadius = 2 * GameManager.getPixelsPerMeter();
        slashSpan = Math.PI / 6 * 5;

        //Do some setup needed for player
        controllable = true;
        damageable = true;
        setWorldLocation(worldLocationX, worldLocationY);
        float width = 1 * GameManager.getPixelsPerMeter();
        float height = 1.8f * GameManager.getPixelsPerMeter();
        setSize(width, height);

        setDefaultVertices();
        setSlashVertices();

        Map<AnimationState, int[]> animations = new HashMap<>();
        animations.put(AnimationState.IDLE, new int[] {0, 2, 4, 6, 8, 10}); // Indices of frames for idling
        animations.put(AnimationState.WALK, new int[] {0, 2, 4, 6, 8, 10, 12, 14, 16}); // Indices of frames for walking
        animations.put(AnimationState.RUN, new int[] {0, 2, 4, 6, 8, 10, 12, 14}); // Indices of frames for running
        animations.put(AnimationState.JUMP, new int[] {4, 6, 8, 10}); // Indices of frames for jumping
        animations.put(AnimationState.DIE, new int[] {0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 4, 4, 4,4 ,4, 4, 6, 6, 6, 6, 6, 8, 8, 8, 8, 8, 10}); // Indices of frames for dying
        animations.put(AnimationState.HIT, new int[] {0, 2, 4}); // Indices of frames for taking damage
        animations.put(AnimationState.ATTACK, new int[] {0, 8}); // Indices of frames for attacking
        animations.put(AnimationState.WALLRIDE, new int[] {0, 2, 4}); // Indices of frames for wallriding
        animations.put(AnimationState.DASH, new int[] {0, 2, 4, 6, 8, 10}); // Indices of frames for dashing

        animator = new Animator(animations, 50f); // 0.1f is the time each frame is displayed
        textureCoords = new float[8];
    }

    /**
     * Initializes the vertices for the slash effect, shaping it and setting its position and size
     */
    private void setSlashVertices()
    {
        numSlashVertices = 30;

        float[] slash = new float[numSlashVertices * 5];
        int firstHalf = numSlashVertices / 2;
        float increment = (float) slashSpan / firstHalf;
        float halfSpan = (float) slashSpan / 2;

        //this is all for design
        slash[0] = (slashRadius) * (float)Math.cos(-halfSpan);
        slash[1] = (slashRadius) * (float) Math.sin(-halfSpan);
        slash[2] = 0;
        slash[3] = 0;
        slash[4] = 0;
        for (int i = 1; i < firstHalf; i++) {
            float angle = i * increment - halfSpan;
            int vertexIndex = i * 5; // Each vertex has 5 components (x, y, z), (s, t)

            slash[vertexIndex] = (slashRadius-10*i/firstHalf) * (float) Math.cos(angle); // x
            slash[vertexIndex + 1] = (slashRadius-10*i/firstHalf) * (float) Math.sin(angle); // y
            slash[vertexIndex + 2] = 10; // z

            slash[vertexIndex + 3] = 0.0f;//texture x
            slash[vertexIndex + 4] = 0.0f;//texture y
        }
        for (int i = firstHalf; i < numSlashVertices-1; i++)
        {
            float angle = (i-firstHalf) * increment - halfSpan;
            int vertexIndex = i * 5; // Each vertex has 5 components (x, y, z), (s, t)

            slash[vertexIndex] = slashRadius * (float) Math.cos(angle); // x
            slash[vertexIndex + 1] = slashRadius * (float) Math.sin(angle); // y
            slash[vertexIndex + 2] = 0; // z

            float textureY = (float) i / (numSlashVertices - 1); // Spread texture X from 0 to 1
            slash[vertexIndex + 3] = 0.5f;//texture x
            slash[vertexIndex + 4] = textureY;//texture y
        }
        slash[numSlashVertices-1 * 5] = 0;
        slash[numSlashVertices-1 * 5+1] = 0;
        slash[numSlashVertices-1 * 5+2] = 10;
        slash[numSlashVertices-1 * 5+3] = 0;
        slash[numSlashVertices-1 * 5+4] = 0;

        // Store how many vertices and elements there is for future use
        int numElements = numSlashVertices * 5;

        //Log.e("numElements",""+numElements);
        numSlashVertices = numElements/(POSITION_ATTRIBUTE_SIZE + TEXTURE_COORDINATES_ATTRIBUTE_SIZE);
        // Initialize the vertices ByteBuffer object based on the
        // number of vertices in the ship design and the number of
        // bytes there are in the float type
        slashVertices = ByteBuffer.allocateDirect(
                        numElements
                                * FLOAT_SIZE)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        // Add the ship into the ByteBuffer object
        slashVertices.put(slash);
    }

    /**
     * Sets the texture for the slashing effect based on the provided ID
     * @param slashTextureId the slash effect texture
     */
    public void setSlashTexture(int slashTextureId)
    {
        slashTexture = slashTextureId;;
    }

    /**
     * Returns the resource ID for the slash texture used in animations
     * @return the resource ID for the slash texture
     */
    public int getSlashTextureId()
    {
        return R.drawable.slash2;
    }

    /**
     * Updates the texture coordinates for the player based on the current animation frame
     */
    protected void updateTextureCoords() {
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
        else
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
     * Retrieves the resource ID for the player's sprite sheet and sets data about the sprite sheet
     * @param context is the context of the program, needed to get sprite sheet data
     * @return the resource ID fro the player's sprite sheet
     */
    @Override
    public int getTextureResourceId(Context context) {
        //animation stuff
        Drawable drawable = context.getDrawable(R.drawable.samurai);
        spriteSheetWidth = drawable.getIntrinsicWidth();
        spriteSheetHeight = drawable.getIntrinsicHeight();
        numberOfColumnsInSpriteSheet = 18;
        numberOfRowsInSpriteSheet = 9;
        frameWidth = spriteSheetWidth / numberOfColumnsInSpriteSheet;
        frameHeight = spriteSheetHeight / numberOfRowsInSpriteSheet;
        return R.drawable.samurai;
    }

    /**
     * Maps each animation state to its corresponding row in the sprite sheet for texture selection
     * @param state the animation we are currently trying to show
     * @return which row in the sprite sheet that animation is
     */
    private int getStateRow(AnimationState state) {
        // Map each state to its corresponding row in the sprite sheet
        switch(state) {
            case IDLE: return 0;
            case WALK: return 1;
            case RUN: return 2;
            case JUMP: return 3;
            case DIE: return 4;
            case HIT: return 5;
            case ATTACK: return 6;
            case WALLRIDE: return 7;
            case DASH: return 8;
            default: return 0; // Default to the first row i.e. idle
        }
    }

    /**
     * Updates the player's state, handling movement, jumping, dashing, and other actions
     * @param fps frames per second, used for determining how much the player should move and how much the velocity should change
     */
    public void update(long fps)
    {
        animator.update();
        if(fps == 0)//fixes the start before fps is initialized when it starts it goes to -infinity
        {
            return;
        }
        if(hp == 0)
        {
            if (deathStartTime == Long.MAX_VALUE)
            {
                deathStartTime = System.currentTimeMillis();
            }
            return; // don't move when dying
        }
        if(!controllable)
        {
            if(getyVelocity() == 0) // if you hit the ground (or ceiling i guess but that won't happen often)
            {
                comeToStop(fps);
            }
            if(getyVelocity() == 0 && getxVelocity() == 0 || System.currentTimeMillis() - startInvincibility > invincibilityTime / 6)
            {
               // you don't have control until you stop or enough time has passed
               controllable = true;
               setAnimatorState(AnimationState.IDLE);
            }

            applyGravity(fps);
            move(fps);
            return;
        }
        if(isDashing) // then we don't want to set the velocities other than max
        {
            dashDistance += getMaxVelocity() / fps; // might go slightly over the max dash distance but it's ok
            float angle = (float)getFacingAngle();
            float maxVelocity = getMaxVelocity();

            // Calculate the cos and sin once
            float cosAngle = (float) Math.cos(angle);
            float sinAngle = -(float) Math.sin(angle);

            // Set x and y velocities based on the angle
            setxVelocity(cosAngle * 2*maxVelocity);
            setyVelocity(sinAngle * jumpMax);

            move(fps);
            if(dashDistance > dashDistanceMax) // stop dashing
            {
                dashDistance = 0;
                isDashing = false;
                if(!godMode)
                {
                    dashed = true;
                }
                setxVelocity(cosAngle * maxVelocity);
                setyVelocity(sinAngle * maxVelocity);
            }
            return;
        }
        if(isWallJumping)
        {
            jump();
            if(getFacingAngle() >= -Math.PI / 2 && getFacingAngle() <= Math.PI / 2)
            {
                setxVelocity(-getMaxVelocity());
            }
            else
            {
                setxVelocity(getMaxVelocity());
            }
            wallSliding = false;
            isWallJumping = false;
            move(fps);
            return;
        }
        if(getMoving()) // moving joystick
        {
            if(InputController.getFacingRight(getFacingAngle())) // direction of joystick is to the right
            {

                setxVelocity(Math.min(getxVelocity() + (float) Math.cos(getFacingAngle()) * (getMaxVelocity() * getMaxAccel() / fps), (float) Math.cos(getFacingAngle())* getMaxVelocity()));
            }
            else // left
            {
                setxVelocity(Math.max(getxVelocity() - (float) Math.cos(getFacingAngle()) * (-getMaxVelocity() * getMaxAccel() / fps), -(float) Math.cos(getFacingAngle()) * -getMaxVelocity()));
            }
        }
        else // not moving joystick
        {
            comeToStop(fps);
            isWallJumping = false;
            if(getxVelocity() == 0 && getyVelocity() == 0 && (getAnimator().getCurrentState() == AnimationState.WALK || getAnimator().getCurrentState() == AnimationState.RUN))
            {
                setAnimatorState(AnimationState.IDLE);
            }
        }

        applyGravity(fps);
        move(fps);
    }

    /**
     * Returns whether the player missed their last slash attack.
     * @return true if the player missed the slash, false otherwise (and also false if they finished the level and didn't kill every enemy).
     */
    public boolean getMissedSlash()
    {
        return missedSlash;
    }

    /**
     * Returns whether the player has slashed at least once.
     * @return true if the player has slashed once, false otherwise.
     */
    public boolean getSlashedOnce()
    {
        return slashedOnce;
    }

    /**
     * Retrieves the animator managing player's animations based on state.
     * @return Animator object that controls player animations.
     */
    public Animator getAnimator()
    {
        return animator;
    }

    /**
     * Makes the player jump, setting the state to JUMP and applying vertical velocity.
     */
    public void jump()
    {
        setAnimatorState(AnimationState.JUMP);
        isAirborne = true;
        setyVelocity(jumpMax);
    }

    /**
     * Sets whether the player is currently sliding along a wall.
     * @param b true to enable wall sliding, false to disable it.
     */
    public void setWallSliding(boolean b)
    {
        boolean wasWallSliding = false;
        if(wallSliding)
        {
            wasWallSliding = true;
        }
        wallSliding = false;
        if(isAirborne)
        {
            wallSliding = b;
            if(b)
            {
                setAnimatorState(AnimationState.WALLRIDE);
            }
            else if(wasWallSliding)
            {
                setAnimatorState(AnimationState.IDLE);
            }
        }
    }

    /**
     * Sets whether the player is currently airborne.
     * @param b true to set the player as airborne, false otherwise.
     */
    public void setIsAirborne(boolean b)
    {
        isAirborne = b;
        if(!b)
        {
            dashed = false;
        }
    }

    /**
     * Returns whether the player is currently airborne.
     * @return true if the player is airborne, false otherwise.
     */
    public boolean getIsAirborne()
    {
        return isAirborne;
    }

    /**
     * Returns whether the player is currently sliding along a wall.
     * @return true if the player is wall sliding, false otherwise.
     */
    public boolean getWallSliding()
    {
        return wallSliding;
    }

    /**
     * Makes the player perform a wall jump, changing the state to JUMP and applying velocity.
     */
    public void wallJump()
    {
        setAnimatorState(AnimationState.JUMP);
        isWallJumping = true;
        dashed = false;
    }

    /**
     * Initiates a dashing action if the player is able to dash.
     */
    public void dash()
    {

        if(!dashed)
        {
            isDashing = true;
            isAirborne = true;
            wallSliding = false;
            setAnimatorState(AnimationState.DASH);
        }
    }

    /**
     * Returns whether the player is currently controllable.
     * @return true if the player can be controlled, false otherwise.
     */
    public boolean getControllable()
    {
        return controllable;
    }

    /**
     * Handles player taking damage, applying knockback and reducing health points.
     * @param goLeft true to knock the player left, false to knock right.
     * @param gm GameManager instance to interact with the game state (need to inform gm that player has died).
     */
    public void takeDamage(boolean goLeft, GameManager gm)
    {
        if(godMode)
        {
            return;
        }
        if(!damageable)
        {
            if(System.currentTimeMillis() - startInvincibility > invincibilityTime)
            {
                damageable = true;
            }
        }
        if(damageable && hp > 0)
        {
            --hp;
            // stun your character
            if (goLeft) {
                setxVelocity(-getMaxVelocity());
            } else {
                setxVelocity(getMaxVelocity());
            }
            setAnimatorState(AnimationState.HIT);
            setyVelocity(jumpMax/2);
            controllable = false;
            dashed = false;
            //give yourself invincibility to stop chain damage
            damageable = false;
            startInvincibility = System.currentTimeMillis();
            if(hp == 0)
            {
                instaKill(gm);
            }
        }
    }

    /**
     * Resets the player's state to default values.
     */
    private void resetState()
    {
        isMoving = false;
        isAirborne = false;
        wallSliding = false;
        isWallJumping = false;
        isDashing = false;
        dashed = false;
        isSlashing = true;
        isSlashDrawn = false;
        dashDistance = 0;
        slashedOnce = false;
        missedSlash = false;
        deathStartTime = Long.MAX_VALUE;
        hp = 3;
    }

    /**
     * Returns whether the player is currently moving.
     * @return true if the player is moving, false otherwise.
     */
    public boolean getMoving()
    {
        return isMoving;
    }

    /**
     * Sets the player's moving state and updates the animation state accordingly.
     * @param b true to set the player as moving, false otherwise.
     */
    public void setMoving(boolean b)
    {
        isMoving = b;
        AnimationState currState = animator.getCurrentState();
        if(b && (currState == AnimationState.IDLE || currState == AnimationState.WALK || currState == AnimationState.RUN))
        {
            if (getxVelocity() < getMaxVelocity() / 2 && getxVelocity() > -getMaxVelocity() / 2)
            {
                setAnimatorState(AnimationState.WALK);
            }
            else
            {
                setAnimatorState(AnimationState.RUN);
            }
        }
        else if(!b && (currState == AnimationState.WALK || currState == AnimationState.RUN))
        {
            setAnimatorState(AnimationState.IDLE);
        }
    }

    /**
     * Returns the player's current health points.
     * @return Current health points of the player.
     */
    public int getHP()
    {
        return hp;
    }

    /**
     * Sets the player's current animation state.
     * @param state The new animation state to set.
     */
    public void setAnimatorState(AnimationState state)
    {
        animator.setState(state);
    }

    /**
     * Executes a slash attack, checking for enemy hits and applying damage.
     * @param gm GameManager instance to interact with game entities.
     */
    public void slash(GameManager gm)
    {
        if(!isSlashing && controllable)
        {
            slashedOnce = true;
            isSlashing = true;
            isSlashDrawn = false;
            gm.player.setAnimatorState(AnimationState.ATTACK);
            startSlashTime = System.currentTimeMillis();
            slashAngle = getFacingAngle();
            boolean hitSlash = false;
            for (int i = 0; i < gm.enemies.size(); ++i)
            {
                Enemy enemy = gm.enemies.get(i);
                if (isEnemyWithinCone(enemy))
                {
                    hitSlash = true;
                    enemy.takeDamage();
                }
            }
            for(int i = 0; i < gm.breakables.size(); ++i)
            {
                if(isEnemyWithinCone(gm.breakables.get(i)))
                {
                    hitSlash = true;
                    PointF worldLoc = gm.breakables.get(i).getWorldLocation();
                    gm.groundTiles[(int)-worldLoc.y/GameManager.getPixelsPerMeter()][(int) worldLoc.x/GameManager.getPixelsPerMeter()] = null;
                    gm.breakables.remove(i);
                    --i;
                }
            }
            if(!hitSlash) // as long as it hits an enemy or breakable wall you can still get the achievement
            {
                missedSlash = true;
            }
        }
    }

    /**
     * Sets the missed slash flag for the player.
     * @param b true to indicate a missed slash, false otherwise.
     */
    public void setMissedSlash(boolean b)
    {
        missedSlash = b;
    }

    /**
     * Draws the player and their slash effect, if applicable.
     * @param viewportMatrix The current viewport matrix for rendering.
     */
    protected void draw(float[] viewportMatrix)
    {
        updateTextureCoords();
        super.draw(viewportMatrix);
        if(isSlashing)
        {
            if(System.currentTimeMillis() - startSlashTime > rechargeSlashTime)
            {
                isSlashing = false;
            }
            //only draw one time
            if(!isSlashDrawn)
            {
                isSlashDrawn = true;
                float slashAngleDegrees = (float) Math.toDegrees(slashAngle);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                GLManager.setVertexAttribPointer(slashVertices);
                GLManager.translateAndRotate(viewportModelMatrix, viewportMatrix, getWorldLocation().x, getWorldLocation().y, slashAngleDegrees);
                GLManager.setMatrix(viewportModelMatrix, slashTexture);
                GLManager.drawCleanup(numSlashVertices);
            }
        }
    }

    /**
     * Checks if an enemy is within the cone of the player's slash attack.
     * @param entity The target entity to check.
     * @return true if the entity is within the slash cone, false otherwise.
     */
    boolean isEnemyWithinCone(GameObject entity) {
        double distance = InputController.distanceToCircle(getWorldLocation(), entity.getWorldLocation());

        double angleToEnemy = InputController.getAngle(getWorldLocation(), entity.getWorldLocation());
        double widthError = Math.abs(entity.getWidth()/2 * Math.cos(angleToEnemy));
        double heightError = Math.abs(entity.getHeight()/2 * Math.sin(angleToEnemy));
        double graceBuffer = 1.2;

        if (distance > (slashRadius + widthError + heightError) * graceBuffer)
        {
            return false;
        }
        double anglePlus = normalizeAngle(getFacingAngle() + slashSpan / 2);
        double angleMinus = normalizeAngle(getFacingAngle() - slashSpan / 2);

        if(anglePlus > angleMinus)
        {
            return angleToEnemy <= anglePlus && angleToEnemy >= angleMinus;
        }
        return angleToEnemy <= -Math.PI + slashSpan / 2 || angleToEnemy >= Math.PI - slashSpan / 2;
    }

    /**
     * Normalizes an angle to the range [-PI, PI].
     * @param angle The angle to normalize.
     * @return The normalized angle.
     */
    private double normalizeAngle(double angle)
    {
        while (angle > Math.PI)
        {
            angle -= 2 * Math.PI;
        }
        while (angle < -Math.PI)
        {
            angle += 2 * Math.PI;
        }
        return angle;
    }

    /**
     * Instantly kills the player, triggering the death animation and game over state.
     * @param gm GameManager instance to interact with the game state (for achievement purposes).
     */
    public void instaKill(GameManager gm)
    {
        gm.setReload(false);
        gm.saveGameObjectsState();
        gm.setDied(true);
        setAnimatorState(AnimationState.DIE);
        gm.message.generateText("YOU DIED");
        controllable = false;
        hp = 0;
    }

    /**
     * Sets whether the player can be controlled.
     * @param b true to allow control, false to prevent it.
     */
    public void setControllable(boolean b)
    {
        controllable = b;
    }

    /**
     * Gets the time at which the player died
     * @return the time at which the player died
     */
    public long getDeathStartTime()
    {
        return deathStartTime;
    }

    /**
     * Sets the time at which the player died (only used to skip the dying animation and message)
     * @param time when the player died
     */
    public void setDeathStartTime(long time)
    {
        deathStartTime = time;
    }

    public void toggleGodMode()
    {
        godMode = !godMode;
    }
}
