package com.gamecodeschool.assignment1;

import static android.opengl.GLES20.glUseProgram;
import static android.opengl.Matrix.orthoM;

import android.content.Context;
import android.graphics.PointF;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import kotlin.NotImplementedError;

/**
 * This class is for the pause menu, when displaying the achievements tab
 * It holds and sets up the achievements class to display the icons of all achievements
 * @author Braeden Ruff
 */
public class AchievementsTab implements Tab, Texturable
{
    //the glProgram we are using (textureProgram)
    private int glProgram;

    //a button that doesn't do anything and is slightly lighter
    private TabButton activeTabButton;

    //a button that is slightly darker and when clicked transfers tabs
    private TabButton inactiveTabButton;

    //the 4 corners where we will draw the tab texture
    private FloatBuffer vertices;

    //used for drawing (found by GLManager)
    private int textureID;

    //the viewport to decide what to draw
    private final float[] viewportMatrix = new float[16];

    //a TabSwitcher so we can communicate with the pauseMenu when we want to switch tabs
    private TabSwitcher tabSwitcher;

    //a reference to achievements so we can display gotten achievements
    private Achievements achievements;

    //the width of the screen (x-axis)
    float screenWidth;

    //the height of the screen (y-axis)
    float screenHeight;

    //the top bound of where to draw achievements
    float achievementTop;

    //the left bound of where to draw achievements
    float achievementLeft;

    //the right bound of where to draw achievements
    float achievementRight;

    //how far apart to draw achievements
    float stepLength;

    /**
     * This is the construtor to build an achievements tab
     * @param context is the context of the program, since some instances of texturable need context (not this one though, but we still need it for the loadTexture method)
     * @param screenWidth size of screen on the x-axis
     * @param screenHeight size of screen on the y-axis
     * @param tabSwitcher reference to the pauseMenu so we can switch tabs
     * @param achievements reference to the achievement so we can display them
     */
    public AchievementsTab(Context context, float screenWidth, float screenHeight, TabSwitcher tabSwitcher, Achievements achievements)
    {
        glProgram = GLManager.getGLTextureProgram();
        GLManager.loadTexture(context, this);
        orthoM(viewportMatrix, 0, 0, screenWidth, screenHeight, 0, 0f, 1f);

        float halfW = screenWidth / 4; // half width of achievement pane
        float halfH = 2 * screenHeight / 5; // half the height of achievement pane
        float acrossHalfScreen = screenWidth/2; // positioning
        float upHalfScreen = screenHeight/2; //positioning

        float[] modelVertices = new float[] {
                // Position                          // Texture Coordinates
                acrossHalfScreen-halfW, upHalfScreen + halfH, 0,   0.0f, 1.0f,  // Bottom left corner
                acrossHalfScreen+halfW, upHalfScreen + halfH, 0,    1.0f, 1.0f,  // Bottom right corner
                acrossHalfScreen+halfW, upHalfScreen - halfH, 0,     1.0f, 0.0f,  // Top right corner
                acrossHalfScreen-halfW, upHalfScreen - halfH, 0,    0.0f, 0.0f   // Top left corner
        };

        this.tabSwitcher = tabSwitcher;
        this.achievements = achievements;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        // Store number of vertices
        vertices = ByteBuffer.allocateDirect(modelVertices.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertices.put(modelVertices);
        vertices.position(0);

        //original rectangle position in image coordinates
        int originalRectTopLeftX = 6; // 6 pixels to the right is the top left corner
        int originalRectTopLeftY = 7; // 6 pixels down + 1 for help so it looks nice

        //original size of the tab image
        int originalImagePixelsX = 198;
        int originalImagePixelsY = 192;


        //rectangle dimensions in original image coordinates
        int rectWidth = (originalImagePixelsX - originalRectTopLeftX * 2) / 2; // same size tabs
        int rectHeight = 24; // height we want in the original image

        //pane size in screen coordinates
        float paneWidth = 2 * halfW; // full width of the pane
        float paneHeight = 2 * halfH; // full height of the pane

        //calculate the scale factors
        float scaleX = paneWidth / originalImagePixelsX;
        float scaleY = paneHeight / originalImagePixelsY;

        //scale the rectangle's position to pane size
        float scaledTopLeftX = originalRectTopLeftX * scaleX;
        float scaledTopLeftY = originalRectTopLeftY * scaleY;

        float scaledCenterRectX = rectWidth * scaleX/2;
        float scaledCenterRectY = rectHeight * scaleY/2;

        //convert the scaled rectangle's position to screen coordinates
        float screenRectX = acrossHalfScreen - halfW + scaledTopLeftX + scaledCenterRectX;
        float screenRectY = upHalfScreen - halfH + scaledTopLeftY + scaledCenterRectY; // Subtract because screen coordinates go down as Y increases
        float sizeX = scaleX * rectWidth/2;
        float sizeY = scaleY * rectHeight/2;

        //top left corner of where we want to draw achievements
        float originalLeft = 11;
        float originalTop = 47;

        if(achievements.getIcons().size() == 0)
        {
            //always should be achievements
            throw new NotImplementedError();
        }

        //how far apart are the icons
        stepLength = achievements.getIcons().get(0).getSideLength()*2;
        stepLength += stepLength/10; //buffer

        achievementLeft = acrossHalfScreen - halfW + originalLeft * scaleX + stepLength/2; //leftBound
        achievementTop =  upHalfScreen - halfH + originalTop * scaleY + stepLength/2; //topBound
        achievementRight = acrossHalfScreen - halfW + (originalImagePixelsX - originalLeft) * scaleX - stepLength/2; //rightBound

        inactiveTabButton = new TabButton(context, screenWidth, screenHeight, screenRectX, screenRectY, sizeX, sizeY, "CONTROLS", TabButton.TabType.inactive);
        activeTabButton = new TabButton(context, screenWidth, screenHeight, screenRectX + rectWidth * scaleX, screenRectY, sizeX, sizeY, "ACHIEVEMENTS", TabButton.TabType.active);
    }

    /**
     * this method draws the tab, the buttons, and the achievements
     */
    @Override
    public void draw()
    {
        glUseProgram(glProgram);

        GLManager.setVertexAttribPointer(vertices);
        GLManager.setMatrix(viewportMatrix, textureID);
        GLManager.drawCleanup(4);
        activeTabButton.draw();
        inactiveTabButton.draw();
        achievements.drawAchievements(achievementLeft, achievementRight, achievementTop, stepLength);
    }

    /**
     * this method handles userInput when the tab is open
     * @param point where the user clicked
     * @param gm the game manager (not needed in this method, but in the other method that implements Tab)
     */
    @Override
    public void handleInput(PointF point, GameManager gm)
    {
        if(inactiveTabButton.isClicked(point))
        {
            tabSwitcher.switchTab();
        }
    }

    /**
     * Gets the paused menu tab base
     * @param context is the context of the program, since some instances of texturable need context (not this one though, but we still need it for the loadTexture method)
     * @return pointer to where in R.drawable the texture file for the tab base
     */
    @Override
    public int getTextureResourceId(Context context) {
        return R.drawable.main_tab;
    }

    /**
     * sets the textureID for this object (for openGL)
     * @param textureId - the textureID for the texture for the tab base
     */
    @Override
    public void setTextureID(int textureId) {
        textureID = textureId;
    }
}
