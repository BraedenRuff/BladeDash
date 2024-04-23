package com.gamecodeschool.assignment1;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;
import android.util.Pair;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.orthoM;

import static com.gamecodeschool.assignment1.GLManager.A_POSITION;
import static com.gamecodeschool.assignment1.GLManager.A_TEXTURE_COORDINATES;
import static com.gamecodeschool.assignment1.GLManager.U_ALPHA_LOCATION;
import static com.gamecodeschool.assignment1.GLManager.U_COLOR;
import static com.gamecodeschool.assignment1.GLManager.U_GREY_SCALE_LOCATION;
import static com.gamecodeschool.assignment1.GLManager.U_MATRIX;
import static com.gamecodeschool.assignment1.GLManager.U_TEXTURE_UNIT;
import static com.gamecodeschool.assignment1.GLManager.aPositionLocation;
import static com.gamecodeschool.assignment1.GLManager.aTextureCoordinatesLocation;
import static com.gamecodeschool.assignment1.GLManager.uAlphaLocation;
import static com.gamecodeschool.assignment1.GLManager.uColorLocation;
import static com.gamecodeschool.assignment1.GLManager.uGreyScaleLocation;
import static com.gamecodeschool.assignment1.GLManager.uMatrixLocation;
import static com.gamecodeschool.assignment1.GLManager.uTextureUnit;

import java.util.ArrayList;

/**
 * This class handles the rendering and game flow logic
 */
public class BladeDashRenderer implements Renderer
{
    //this is the input controller, which is used for handling input
    private InputController ic;

    //this is the context of the program
    private Context context;

    //how many frames have passed
    private long fps;

    //for determining what to draw on the screen
    private final float[] viewportMatrix = new float[16];

    //this is to help manage our game objects
    private GameManager gm;

    //this is the menu when you pause the game
    private PauseMenu pauseMenu;

    //for temporary storing of a point so we don't have to create a new one so often
    PointF handyPointF;

    //an array to keep track of the game controls
    private final GameButton[] gameControls = new GameButton[3]; // 0 is jump, 1 is dash, 2 is slash

    /**
     * the blade dash renderer constructor
     * @param context the context of the program
     * @param gameManager helps manage game objects
     * @param inputController controls user input
     */
    public BladeDashRenderer(Context context, GameManager gameManager, InputController inputController)
    {
        gm = gameManager;
        ic = inputController;

        handyPointF = new PointF();

        this.context = context;
    }

    /**
     * We use this to initialize our GL programs, choose the color to clear the screen (we chose sky blue)
     * and to create our game objects
     * @param glUnused the GL interface. Use <code>instanceof</code> to
     * test if the interface supports GL11 or higher interfaces.
     * @param config the EGLConfig of the created surface. Can be used
     * to create matching pbuffers.
     */
    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {

        // The color that will be used to clear the
        // screen each frame in onDrawFrame()
        glClearColor(0.0f, 0.5f, 1.0f, 0.0f);

        // Get our GLManager to compile and link the shaders into an object
        GLManager.buildProgramTexture();
        GLManager.buildColorProgram();
        GLManager.buildAchievementProgram();

        uMatrixLocation = glGetUniformLocation(GLManager.getGLTextureProgram(), U_MATRIX);
        aPositionLocation = glGetAttribLocation(GLManager.getGLTextureProgram(), A_POSITION);
        aTextureCoordinatesLocation = glGetAttribLocation(GLManager.getGLTextureProgram(), A_TEXTURE_COORDINATES);
        uTextureUnit = glGetUniformLocation(GLManager.getGLTextureProgram(), U_TEXTURE_UNIT);

        uColorLocation = glGetUniformLocation(GLManager.getGLColorProgram(), U_COLOR);

        uAlphaLocation = glGetUniformLocation(GLManager.getGLAchievementProgram(), U_ALPHA_LOCATION);
        uGreyScaleLocation = glGetUniformLocation(GLManager.getGLAchievementProgram(), U_GREY_SCALE_LOCATION);

        createObjects();
    }

    /**
     * this method initializes our viewport
     * @param glUnused the GL interface. Use <code>instanceof</code> to
     * test if the interface supports GL11 or higher interfaces.
     * @param width the width of the screen
     * @param height the height of the screen
     */
    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {

        // Make full screen
        glViewport(0, 0, width, height);

        orthoM(viewportMatrix, 0, 0, gm.metresToShowX, 0, gm.metresToShowY, 0f, 1f);
    }

    /**
     * this method creats all of our game objects, and resets the GLManager textureMap
     */
    private void createObjects()
    {
        //important to reset our texture map
        GLManager.resetTextureMap(context);

        // Create our game objects
        ArrayList<Pair<PointF, PointF>> joysticks = ic.getJoystick();
        int i = 0;
        for(Pair<PointF, PointF> points : joysticks)
        {
            gm.movementJoystick = new Joystick(points.first, points.second, gm); //only one joystick right now, just for extendability
            ++i;
        }

        ArrayList<PointF> buttonsToDraw = ic.getButtons();
        i = 0;
        for(PointF point : buttonsToDraw)
        {
            switch (i)
            {
                case 0:
                    gameControls[i] = new GameButton(context, gm.screenWidth, gm.screenHeight, point.x, point.y, ic.getOuterRadius(), GameButton.ButtonType.JUMP);
                    break;
                case 1:
                    gameControls[i] = new GameButton(context, gm.screenWidth, gm.screenHeight, point.x, point.y, ic.getOuterRadius(), GameButton.ButtonType.DASH);
                    break;
                case 2:
                    gameControls[i] = new GameButton(context, gm.screenWidth, gm.screenHeight, point.x, point.y, ic.getOuterRadius(), GameButton.ButtonType.SLASH);
                    break;
            }
            ++i;
        }
        //switch level will make all of the gm game objects according to map
        gm.switchLevel(context);

        ic.makePauseMenu(context, gm.screenWidth, gm.screenHeight, gm.achievements);
        pauseMenu = ic.getPauseMenu();
    }

    /**
     * this method handles updating the positions and drawing
     * @param glUnused the GL interface. Use <code>instanceof</code> to
     * test if the interface supports GL11 or higher interfaces.
     */
    @Override
    public void onDrawFrame(GL10 glUnused) {

        // Clear both color and depth buffer
        long startFrameTime = System.currentTimeMillis();

        if (gm.isPlaying())
        {
            update(fps);
        }

        draw();

        // Calculate the fps this frame
        // We can then use the result to
        // time animations and more.
        long timeThisFrame = System.currentTimeMillis() - startFrameTime;
        if (timeThisFrame >= 1)
        {
            fps = 1000 / timeThisFrame;
        }
    }

    /**
     * This method tells the player and enemies to update their positions, as well as handles collisions
     * @param fps frames per second, used to decide how far something moves
     */
    private void update(long fps)
    {
        //player has died for 3 seconds, so restart the level
        if(System.currentTimeMillis() - gm.player.getDeathStartTime() > 3000)
        {
            createObjects();
            return;
        }
        gm.player.update(fps);
        handleTileCollisionsEfficient(gm.player);
        handleBorderCollision();

        handyPointF = gm.player.getWorldLocation();
        float maxDistance = 4 * GameManager.getPixelsPerMeter();
        for(int i = 0; i < gm.enemies.size(); ++i) // ordered by left to right then up to down
        {
            Enemy enemy = gm.enemies.get(i);
            enemy.update(fps, gm.player);
            handleTileCollisionsEfficient(enemy);
            if(enemy.getWorldLocation().x - maxDistance <= handyPointF.x &&
                    enemy.getWorldLocation().x + maxDistance >= handyPointF.x) // only bother checking if the enemy is close enough
            {
                handlePlayerEnemyCollisions(enemy);
            }
            if(enemy.getHP() == 0)
            {
                //if the enemy has been dead for 2 seconds, remove it
                if(System.currentTimeMillis() - enemy.getDeathTime() > 2000)
                {
                    gm.enemies.remove(i);
                    --i;
                    //if the enemy is the monster spawner, you win, end the game
                    if(enemy instanceof MonsterSpawner)
                    {
                        gm.player.setControllable(false);
                        if(gm.enemies.size() != 0) //if there are still enemies, didn't defeat them all (requirement for this achievement)
                        {
                            gm.player.setMissedSlash(true);
                        }
                        gm.end(context);
                    }
                }

            }
        }
        handleCoinCollisions();
        handleTeleportCollision();
    }

    /**
     * This method ensures the player doesn't go off the map
     */
    private void handleBorderCollision()
    {
        handyPointF = gm.player.getWorldLocation();
        //don't care about non player border collisions

        //left border
        if(handyPointF.x < 0)
        {
            gm.player.setWorldLocation(1, -handyPointF.y);
        }
        //right border
        else if(handyPointF.x > gm.mapWidth)
        {
            gm.player.setWorldLocation(gm.mapWidth - 1, -handyPointF.y);
        }
        //top border
        if(handyPointF.y > 0)
        {
            gm.player.setWorldLocation(handyPointF.x, 0);
        }
        //bottom border (fully off screen)
        else if(handyPointF.y < -gm.mapHeight - gm.player.getHeight()/2)
        {
            if(gm.player.getHP() != 0)
            {
                gm.player.instaKill(gm);
            }
        }
        for(Enemy enemy : gm.enemies)
        {
            if(enemy.getWorldLocation().y < -gm.mapHeight - enemy.getHeight()/2)
            {
                enemy.takeDamage();
            }
        }
    }

    /**
     * This method handles coin collection logic
     */
    private void handleCoinCollisions()
    {
        Enum<GameObject.collisionType> collisionType;
        for(int i = 0; i < gm.coins.size(); ++i)
        {
            collisionType = gm.player.getCollisionDirection(gm.coins.get(i));
            if(collisionType != GameObject.collisionType.NONE)
            {
                gm.coins.remove(i);
                break;
            }
        }
        //if all coins collected, handle the achievement getting
        if(gm.coins.size() == 0)
        {
            gm.achievements.setExplorer(gm.level);
            gm.setSavedAchievements(context);
        }
    }

    /**
     * This method handles when the enemy hits the player
     * @param enemy is which enemy we are checking if it hits the player
     */
    private void handlePlayerEnemyCollisions(Enemy enemy)
    {
        Enum<GameObject.collisionType> collisionType = gm.player.getCollisionDirection(enemy);
        if(collisionType != GameObject.collisionType.NONE)
        {
            if(enemy.hp > 0)
            {
                gm.player.takeDamage(enemy.getWorldLocation().x > gm.player.getWorldLocation().x, gm);
                if(enemy instanceof Goblin)
                {
                    enemy.setAnimatorState(AnimationState.ATTACK);
                }
            }
        }

    }

    /**
     * This method checks if player collided with the teleporter (end of level)
     */
    private void handleTeleportCollision()
    {
        if(gm.teleport == null)
        {
            return;
        }
        Enum<GameObject.collisionType> collisionType = gm.player.getCollisionDirection(gm.teleport);
        if(collisionType != GameObject.collisionType.NONE)
        {
            if(gm.enemies.size() != 0) //if there are still enemies, didn't defeat them all (requirement for this achievement)
            {
                gm.player.setMissedSlash(true);
            }
            gm.nextLevel(context);
        }
    }

    /**
     * This method handles the collisions with the ground, and the players/enemies
     * @param entity is the object we are seeing if it collided with ground tiles
     */
    private void handleTileCollisionsEfficient(GameObject entity)
    {
        // most efficient: find where in the gounndTiles array the entity is, check the column before till after, check rows before till after
        int columns = gm.getMapColumns();
        int rows = gm.getMapRows();
        int entityColumn = (int)(entity.getWorldLocation().x / GameManager.getPixelsPerMeter());
        if(entityColumn < 0 || entityColumn >= columns) // outside the border somehow
        {
            return;
        }
        int entityRow = (int)(-entity.getWorldLocation().y / GameManager.getPixelsPerMeter());
        if(entityRow < 0 || entityRow >= rows) // outside the border somehow
        {
            return;
        }
        //these are to make it less jank, since without them the player will go inside a block and touch the x of the next block, and the top of a previous block
        //and we'll therefore set the velocity to 0 for x and y even if it's flat ground
        //same when jumping the x and y will set to zero even if it's a flat wall
        boolean previousCollisionX = false;
        boolean previousCollisionY = false;
        boolean twoCollisions = false;
        float prevVelocityX = 0;
        float prevVelocityY = 0;

        //for animation
        boolean setIdle = false;

        //for double collisions
        boolean zeroY = false;
        boolean zeroX = false;
        float halfWidthPlayer = entity.getWidth()/2;
        float halfHeightPlayer = entity.getHeight()/2;
        handyPointF = entity.getWorldLocation();
        //check before and after
        for (int col = Math.max(0, entityColumn - 2); col <= Math.min(entityColumn + 2, columns - 1); col++)
        {
            for (int row = Math.max(0, entityRow - 2); row <= Math.min(entityRow + 2, rows - 1); row++)
            {
                if(gm.groundTiles[row][col] == null) //sparse array, has chance of null
                {
                    continue;
                }
                Ground ground = gm.groundTiles[row][col];

                //handle double collisions
                // if we're above or below something, it should be within halfWidthPlayer (assuming player > tile)
                float x = Math.abs(ground.getWorldLocation().x - handyPointF.x);
                // if we're hitting a wall, it should be within halfHeightPlayer (assuming player > tile)
                float y = Math.abs(ground.getWorldLocation().y - handyPointF.y);

                //see if the player is in the same column as the current tile (or close enough to the column to colide)
                if (x <= halfWidthPlayer) // assumes player is at least as thick as a tile
                {
                    if (y <= ground.getHeight() / 2 + halfHeightPlayer)
                    {
                        //then we can safely say that something above or below if the y value of the center difference is less than half the player height + half the tile height
                        zeroY = true;
                    }
                }

                //see if the player is in the same row as the current tile (or close enough to the row to colide)
                if (y <= halfHeightPlayer) // assumes player is at least as tall as a tile
                {
                    if (x <= ground.getWidth() / 2 + halfWidthPlayer)
                    {
                        //then we can safely say that something above or below if the x value of the center difference is less than half the player width + half the tile width
                        zeroX = true;
                    }
                }

                //handle collisions
                Enum<GameObject.collisionType> collisionType = entity.getCollisionDirection(ground);
                if(collisionType != GameObject.collisionType.NONE)
                {
                    if(ground.getGroundType() == Ground.GroundType.DEATH && entity instanceof Player) //insta death block sticks you into the spikes
                    {
                        if(gm.player.getHP() != 0)
                        {
                            gm.player.instaKill(gm);
                        }
                        gm.player.setWorldLocation(handyPointF.x, -ground.getWorldLocation().y-ground.getHeight()/2);
                        return;
                    }
                    if(collisionType == GameObject.collisionType.TOP)
                    {
                        prevVelocityY = entity.getyVelocity();
                        previousCollisionY = true;
                        if(previousCollisionX)
                        {
                            twoCollisions = true;
                        }
                        entity.setyVelocity(0);
                        entity.setWorldLocation(handyPointF.x, -(ground.getWorldLocation().y - ground.getHeight() / 2 - entity.getHeight() / 2));
                    }
                    else if(collisionType == GameObject.collisionType.BOTTOM)
                    {
                        prevVelocityY = entity.getyVelocity();
                        previousCollisionY = true;
                        if(previousCollisionX)
                        {
                            twoCollisions = true;
                        }
                        entity.setyVelocity(0);
                        entity.setWorldLocation(handyPointF.x, -(ground.getWorldLocation().y + ground.getHeight()/2 + entity.getHeight()/2));
                        if(entity instanceof Player)
                        {
                            gm.player.setIsAirborne(false);
                            gm.player.setWallSliding(false);
                            if(gm.player.getAnimator().getCurrentState() == AnimationState.JUMP || gm.player.getAnimator().getCurrentState() == AnimationState.DASH)
                            {
                                setIdle = true;
                            }
                        }
                        if(entity instanceof Slime)
                        {
                            ((Slime) entity).setIsAirborne(false);
                        }
                        if(entity instanceof Goblin)
                        {
                            ((Goblin) entity).setIsAirborne(false);
                        }
                    }
                    else if(collisionType == GameObject.collisionType.LEFT)
                    {
                        if(prevVelocityX == 0)
                        {
                            prevVelocityX = entity.getxVelocity();
                        }
                        previousCollisionX = true;
                        if(previousCollisionY)
                        {
                            twoCollisions = true;
                        }
                        if(entity instanceof Player && gm.player.getyVelocity() < 6 * GameManager.getPixelsPerMeter())
                        {
                            gm.player.setWallSliding(true);
                            if(gm.player.getyVelocity() < -2 * GameManager.getPixelsPerMeter())
                            {
                                gm.player.setyVelocity(-2 * GameManager.getPixelsPerMeter());
                            }
                        }
                        if(entity instanceof Goblin)
                        {
                            if(!((Goblin) entity).getIsAirborne())
                            {
                                ((Goblin) entity).setJumping(true);
                            }
                        }

                        entity.setxVelocity(0);
                        entity.setWorldLocation(ground.getWorldLocation().x - ground.getWidth()/2 - entity.getWidth()/2, -handyPointF.y);
                    }
                    else if(collisionType == GameObject.collisionType.RIGHT)
                    {
                        if(prevVelocityX == 0)
                        {
                            prevVelocityX = entity.getxVelocity();
                        }
                        previousCollisionX = true;
                        if(previousCollisionY)
                        {
                            twoCollisions = true;
                        }
                        if(entity instanceof Player && gm.player.getyVelocity() < 6 * GameManager.getPixelsPerMeter())
                        {
                            gm.player.setWallSliding(true);
                            if(gm.player.getyVelocity() < -2 * GameManager.getPixelsPerMeter())
                            {
                                gm.player.setyVelocity(-2 * GameManager.getPixelsPerMeter());
                            }
                        }
                        if(entity instanceof Goblin)
                        {
                            if(!((Goblin) entity).getIsAirborne())
                            {
                                ((Goblin) entity).setJumping(true);
                            }
                        }

                        entity.setxVelocity(0);
                        entity.setWorldLocation(ground.getWorldLocation().x + ground.getWidth()/2 + entity.getWidth()/2, -handyPointF.y);
                    }
                }
            }
        }

        if(entity instanceof Player)
        {
            // no y collsion? that means we walked into the air or are jumping
            if(!previousCollisionY)
            {
                gm.player.setIsAirborne(true);
            }
            // no x collision? we walked off the wall ride if there was one
            if(!previousCollisionX)
            {
                gm.player.setWallSliding(false);
            }
        }
        if(twoCollisions)
        {
            //just collided because you glitched into the ground
            if (!zeroX)
            {
                entity.setxVelocity(prevVelocityX);
            }
            else if (entity instanceof Player)
            {
                gm.player.setWallSliding(true);
            }
            //just collided because you glitched into the wall
            if (!zeroY)
            {
                entity.setyVelocity(prevVelocityY);
            }
            else if (entity instanceof Player)
            {
                gm.player.setIsAirborne(false);
                gm.player.setWallSliding(false);
                if(gm.player.getAnimator().getCurrentState() == AnimationState.JUMP || gm.player.getAnimator().getCurrentState() == AnimationState.WALLRIDE || gm.player.getAnimator().getCurrentState() == AnimationState.DASH)
                {
                    gm.player.setAnimatorState(AnimationState.IDLE);
                }
            }
            //in a corner
            if(zeroX && zeroY)
            {
                entity.setxVelocity(prevVelocityX);
            }
        }
        else
        {
            if(setIdle && (gm.player.getAnimator().getCurrentState() == AnimationState.JUMP || gm.player.getAnimator().getCurrentState() == AnimationState.DASH))
            {
                gm.player.setAnimatorState(AnimationState.IDLE);
            }
        }
    }

    /**
     * This method tells each object to get drawn, and also handles the viewport if you get close to the edge of the map
     */
    private void draw()
    {
        // Where is the player?
        handyPointF = gm.player.getWorldLocation();

        // Modify the viewport matrix orthographic projection, but keep it inside the map boundaries
        float left = handyPointF.x - gm.metresToShowX / 2;
        float right = handyPointF.x + gm.metresToShowX / 2;
        float bottom = handyPointF.y - gm.metresToShowY / 2;
        float top = handyPointF.y + gm.metresToShowY / 2;
        if(left < 0)
        {
            left = 0;
            right = gm.metresToShowX;
        }
        else if(right > gm.mapWidth)
        {
            left = gm.mapWidth - gm.metresToShowX;
            right = gm.mapWidth;
        }
        if(bottom < -gm.mapHeight)
        {
            bottom = -gm.mapHeight;
            top = -gm.mapHeight + gm.metresToShowY;
        }
        if(top > 0)
        {
            top = 0;
            bottom = -gm.metresToShowY;
        }
        orthoM(viewportMatrix, 0, left, right, bottom, top, 0f, 1f);

        // Clear the screen
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // Draw game objects
        for(int i = 0; i < gm.groundTiles.length; ++i)
        {
            for(int j = 0; j < gm.groundTiles[i].length; ++j)
            {
                if(gm.groundTiles[i][j] != null)
                {
                    gm.groundTiles[i][j].draw(viewportMatrix);
                }
            }
        }
        for(int i = 0; i < gm.coins.size(); ++i)
        {
            {
                gm.coins.get(i).draw(viewportMatrix);
            }
        }
        for(int i = 0; i < gm.enemies.size(); ++i)
        {
            {
                gm.enemies.get(i).draw(viewportMatrix);
            }
        }

        gm.player.draw(viewportMatrix);
        if(gm.teleport != null)
        {
            gm.teleport.draw(viewportMatrix);
        }

        //draw UI
        gm.movementJoystick.draw();
        for(int i = 0; i < gameControls.length; ++i)
        {
            {
                gameControls[i].draw();
            }
        }
        gm.message.draw();
        gm.achievements.draw();
        pauseMenu.draw();
        gm.godModeMessage.draw();
    }

}