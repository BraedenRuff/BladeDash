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
 * Represents a button within a tab, used in the game's pause menu or other UI components.
 * It can display as active or inactive and handle touch input.
 * @author Braeden RUff
 */
public class TabButton implements Texturable
{
    /**
     * Defines the type of the tab button, whether it is active or inactive
     */
    public enum TabType{active, inactive}

    // The tab type for this instance of a tab button
    private TabType tabType;

    // OpenGL texture ID for the button background
    private int textureID;

    // Orthographic projection matrix for rendering
    private final float[] viewportMatrix = new float[16];

    // Model matrix for transforming the button
    private final float[] viewportModelMatrix = new float[16];

    // OpenGL program ID
    private int glProgram;

    // Vertex buffer for the button's geometry
    private FloatBuffer vertices;

    // Location and size of the button
    PointF loc;
    float sizeX;
    float sizeY;

    // Message object for displaying text on the button
    private Message message;

    /**
     * Constructs a new TabButton with specified parameters.
     * @param context The application context for accessing resources.
     * @param screenWidth The width of the screen for proper scaling.
     * @param screenHeight The height of the screen for proper scaling.
     * @param x The X coordinate of the button's center.
     * @param y The Y coordinate of the button's center.
     * @param sizeX The width of the button.
     * @param sizeY The height of the button.
     * @param text The text to display on the button.
     * @param tabType The type of the tab button (active or inactive).
     */
    public TabButton(Context context, float screenWidth, float screenHeight, float x, float y, float sizeX, float sizeY, String text, TabType tabType)
    {
        this.tabType = tabType;
        GLManager.loadTexture(context, this);
        orthoM(viewportMatrix, 0, 0, screenWidth, screenHeight, 0, 0f, 1f);
        loc = new PointF(x, y);
        this.sizeX = sizeX;
        this.sizeY = sizeY;

        float[] modelVertices = new float[] {
                // Position                          // Texture Coordinates
                -sizeX, sizeY, 0,   0.0f, 1.0f,  // Bottom left corner
                sizeX, sizeY, 0,    1.0f, 1.0f,  // Bottom right corner
                sizeX, -sizeY, 0,     1.0f, 0.0f,  // Top right corner
                -sizeX, -sizeY, 0,    0.0f, 0.0f   // Top left corner
        };
        // Store number of vertices
        vertices = ByteBuffer.allocateDirect(modelVertices.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertices.put(modelVertices);
        vertices.position(0);

        glProgram = GLManager.getGLTextureProgram();
        message = new Message(context,screenWidth,screenHeight);
        message.generateText(text,x-sizeX, x+sizeX,x,y-sizeY/2);
        message.setPersistent(true);

    }

    /**
     * Gets the resource ID of the texture based on the tab type.
     * @param context The application context for accessing resources.
     * @return The resource ID of the texture.
     */
    @Override
    public int getTextureResourceId(Context context)
    {
        switch (tabType)
        {
            case active:
                return R.drawable.active_tab;
            case inactive:
                return R.drawable.unactive_tab;
        }
        throw new NotImplementedError();
    }

    /**
     * Sets the openGL texture ID for this button.
     * @param textureId The OpenGL texture ID.
     */
    @Override
    public void setTextureID(int textureId)
    {
        textureID = textureId;
    }

    /**
     * Renders the button and its message to the screen.
     */
    public void draw()
    {
        glUseProgram(glProgram);

        GLManager.setVertexAttribPointer(vertices);
        GLManager.translate(viewportModelMatrix, viewportMatrix, loc.x, loc.y);
        GLManager.setMatrix(viewportModelMatrix, textureID);
        GLManager.drawCleanup(4);
        message.draw();
    }
    /**
     * Determines if the button has been clicked based on the input touch point.
     * @param newPoint The point of touch input.
     * @return True if the button was clicked, false otherwise.
     */
    public boolean isClicked(PointF newPoint)
    {
        return Math.abs(newPoint.x - loc.x) <= sizeX && Math.abs(newPoint.y - loc.y) <= sizeY;
    }
}
