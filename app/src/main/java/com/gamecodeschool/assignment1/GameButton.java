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
 * This class is used to show the various buttons
 * @author Braeden Ruff
 */
public class GameButton implements Texturable
{
    /**
     * This enum is used to show the different button types we have, primarily just used for texturing
     */
    public enum ButtonType {RESTARTGAME, RESTARTLEVEL, DELETEDATA, DASH, SLASH, JUMP, OPEN, CLOSE, GODMODE}

    //The button type for this object, currently only used for texturing
    ButtonType buttonType;

    //the textureID, used for openGL
    private int textureID;

    //the viewport so we know what to draw
    private final float[] viewportMatrix = new float[16];

    //used to help us translate the button to where we want to display it
    private final float[] viewportModelMatrix = new float[16];

    //the glProgram we want to use(textureProgram)
    private int glProgram;

    //the vertices that represent the rectangle for our texture
    private FloatBuffer vertices;

    //the center of our button in (x, y) coordinates
    PointF loc;

    //the radius of our button
    float size;

    /**
     * This is the constructor for a game button
     * @param context - the context of the program, not used this time
     * @param screenWidth - the width of the screen
     * @param screenHeight - the height of the screen
     * @param x - where the button will be positioned horizontally on screen
     * @param y - where the button will be positioned vertically on screen
     * @param size - the radius of the button
     * @param type - what the button will be of (only for texture right now)
     */
    public GameButton(Context context, float screenWidth, float screenHeight, float x, float y, float size, ButtonType type)
    {
        buttonType = type;
        GLManager.loadTexture(context, this);
        orthoM(viewportMatrix, 0, 0, screenWidth, screenHeight, 0, 0f, 1f);
        loc = new PointF(x, y);
        this.size = size;

        float[] modelVertices = new float[] {
                // Position                          // Texture Coordinates
                -size, size, 0,   0.0f, 1.0f,  // Bottom left corner
                size, size, 0,    1.0f, 1.0f,  // Bottom right corner
                size, -size, 0,     1.0f, 0.0f,  // Top right corner
                -size, -size, 0,    0.0f, 0.0f   // Top left corner
        };
        // Store number of vertices
        vertices = ByteBuffer.allocateDirect(modelVertices.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertices.put(modelVertices);
        vertices.position(0);

        glProgram = GLManager.getGLTextureProgram();
    }

    /**
     * This method draws the button on the screen
     */
    public void draw()
    {
        glUseProgram(glProgram);

        GLManager.setVertexAttribPointer(vertices);
        GLManager.translate(viewportModelMatrix, viewportMatrix, loc.x, loc.y);
        GLManager.setMatrix(viewportModelMatrix, textureID);
        GLManager.drawCleanup(4);
    }

    /**
     * This method determines if the button is clicked.
     * The button is a circle so we check if the point is inside the radius, which we said was half the side length of the vertices
     * If the png is changed so the circle doesn't go to the edge, this will not work correclty
     * @param newPoint - the point we are checking if it clicked this button
     * @return whether the button is clicked or not
     */
    public boolean isClicked(PointF newPoint)
    {
        return InputController.distanceToCircle(newPoint, loc) <= size;
    }

    /**
     * This method grabs the texture in R.drawable for the specified texture
     * @param context - context of the program, not used this time
     * @return pointer to where in R.drawable the texture file for the given enum of this class
     */
    @Override
    public int getTextureResourceId(Context context)
    {
        switch (buttonType)
        {
            case RESTARTGAME:
                return R.drawable.restartgame;
            case RESTARTLEVEL:
                return R.drawable.restartlevel;
            case DELETEDATA:
                return R.drawable.delete;
            case DASH:
                return R.drawable.dash;
            case SLASH:
                return R.drawable.attack;
            case JUMP:
                return R.drawable.jump;
            case OPEN:
                return R.drawable.pause_button;
            case CLOSE:
                return R.drawable.close_button;
            case GODMODE:
                return R.drawable.god_mode;
            default:
                //This shouldn't be reached, but if I add more and forget
                throw new NotImplementedError();
        }
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
}
