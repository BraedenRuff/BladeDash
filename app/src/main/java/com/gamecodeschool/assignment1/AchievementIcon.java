package com.gamecodeschool.assignment1;

import static android.opengl.GLES20.glUseProgram;
import static android.opengl.Matrix.orthoM;
import static com.gamecodeschool.assignment1.GLManager.uAlphaLocation;
import static com.gamecodeschool.assignment1.GLManager.uGreyScaleLocation;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import kotlin.NotImplementedError;

/**
 * This class contains the graphic icon of all the achievements
 * @author Braeden Ruff
 */
public class AchievementIcon implements Texturable
{
    //size of the icon
    private float sideLength;

    //enum to help us grab a bunch of achievement textures in the same class
    public enum AchievementIconTexture{ NODEATHS1, NODEATHS2, NODEATHS3, PROGAMER, EXPLORER1, EXPLORER2, EXPLORER3, EBENEZERKAITO, PACIFIST, GODGAMER}

    //the icon texture we are using for this specific instance
    private AchievementIconTexture iconTexture;

    //used for drawing (found by GLManager)
    private int textureID;

    //the screen we see
    private final float[] viewportMatrix = new float[16];

    //the glProgram we are using
    private static int glProgram = -1;

    //the 4 corners where we will draw the texture
    private FloatBuffer vertices;

    //this will be the viewport + translation
    float[] viewportModelMatrix = new float[16];

    /**
     * Constructor for the icon texture
     * @param context is the context of the program, since some instances of texturable need context (not this one though, but we still need it for the loadTexture method)
     * @param texture which specific texture you want to show
     * @param screenWidth size of screen on the x-axis
     * @param screenHeight size of screen on the y-axis
     */
    public AchievementIcon(Context context, AchievementIconTexture texture, float screenWidth, float screenHeight)
    {
        iconTexture = texture;
        if(glProgram == -1)
        {
            glProgram = GLManager.getGLAchievementProgram(); //allows textures, transparency, and grayscale
        }
        GLManager.loadTexture(context, this);

        orthoM(viewportMatrix, 0, 0, screenWidth, screenHeight, 0, 0f, 1f);

        sideLength = screenWidth / 30;

        //make it a square
        float[] modelVertices = new float[] {
                // Position                          // Texture Coordinates
                -sideLength, sideLength, 0,   0.0f, 1.0f,  // Bottom left corner
                sideLength, sideLength, 0,    1.0f, 1.0f,  // Bottom right corner
                sideLength, -sideLength, 0,     1.0f, 0.0f,  // Top right corner
                -sideLength, -sideLength, 0,    0.0f, 0.0f   // Top left corner
        };

        // Store number of vertices
        vertices = ByteBuffer.allocateDirect(modelVertices.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertices.put(modelVertices);
        vertices.position(0);


    }

    /**
     * This method gets the current instances iconTexture
     * @return enum value of the texture of the icon
     */
    public AchievementIconTexture getIconTexture()
    {
        return iconTexture;
    }

    /**
     * This method grabs the texture in R.drawable for the specified texture
     * @param context - context of the program, not used this time
     * @return pointer to where in R.drawable the texture file for the given enum of this class is
     */
    @Override
    public int getTextureResourceId(Context context)
    {
        switch (iconTexture)
        {
            case NODEATHS1:
                return R.drawable.no_deaths1;
            case NODEATHS2:
                return R.drawable.no_deaths2;
            case NODEATHS3:
                return R.drawable.no_deaths3;
            case PROGAMER:
                return R.drawable.progamer;
            case EXPLORER1:
                return R.drawable.explorer1;
            case EXPLORER2:
                return R.drawable.explorer2;
            case EXPLORER3:
                return R.drawable.explorer3;
            case EBENEZERKAITO:
                return R.drawable.ebenezerkaito;
            case PACIFIST:
                return R.drawable.pacifist;
            case GODGAMER:
                return R.drawable.godgamer;
            default:
                //This shouldn't be reached, but if I add more and forget
                throw new NotImplementedError();
        }
    }

    /**
     * gets the sideLength of the the square where the texture will be drawn
     * @return the sideLength of the square where the texture will be drawn
     */
    public float getSideLength()
    {
        return sideLength;
    }

    /**
     * Sets the textureID for this object (for openGL)
     * @param textureId - the textureID for the texture for this object
     */
    @Override
    public void setTextureID(int textureId)
    {
        textureID = textureId;
    }

    /**
     * responsible for using openGL to draw the achievement icon
     * @param x - where on the x-axis this will be drawn
     * @param y - where on the y-axis this will be drawn
     * @param greyScale - draw in greyScale or not
     * @param alphaValue - how transparent should it be
     */
    public void drawIcon(float x, float y, boolean greyScale, float alphaValue)
    {
        // tell openGL to use blending
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        // tell OpenGl to use the glProgram (achievementProgram)
        glUseProgram(glProgram);
        //use the GLManager to set up the drawing
        GLManager.setVertexAttribPointer(vertices);
        GLManager.translate(viewportModelMatrix, viewportMatrix, x, y);
        GLManager.setMatrix(viewportModelMatrix, textureID);
        //add the alphaValue and grayscale if applicable
        GLES20.glUniform1f(uAlphaLocation, alphaValue);
        GLES20.glUniform1i(uGreyScaleLocation, greyScale ? 1 : 0);
        //draw it and disable the vertex and blending
        GLManager.drawCleanup(4);
    }
}
