package com.gamecodeschool.assignment1;

import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.Matrix.orthoM;
import static com.gamecodeschool.assignment1.GLManager.FLOAT_SIZE;
import static com.gamecodeschool.assignment1.GLManager.POSITION_ATTRIBUTE_SIZE;
import static com.gamecodeschool.assignment1.GLManager.TEXTURE_COORDINATES_ATTRIBUTE_SIZE;
import static com.gamecodeschool.assignment1.GLManager.aPositionLocation;
import static com.gamecodeschool.assignment1.GLManager.aTextureCoordinatesLocation;
import static com.gamecodeschool.assignment1.GLManager.uAlphaLocation;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Handles displaying messages on the screen (text), including fading animations and positioning.
 * @author Braeden Ruff
 */
public class Message implements Texturable
{
    // Projection matrix for 2D orthographic projection
    private final float[] viewportMatrix = new float[16];

    // openGL shader program ID
    private int glProgram;

    // Vertex buffer for rendering the text
    private FloatBuffer vertices;

    // Dimensions of the sprite sheet containing characters
    private int spriteSheetWidth;
    private int spriteSheetHeight;

    // Dimensions for individual character frames
    private int frameWidth;
    private int frameHeight;

    // Sprite sheet divisions
    private int numberOfColumnsInSpriteSheet;
    private int numberOfRowsInSpriteSheet;

    // Timestamp for message start time to control fading
    private long startTime;

    // Flag to indicate if the message should stay visible
    private boolean persistent;

    // Texture ID for the text atlas sheet
    private int textureID;

    // The message to display
    private String message;

    // Number of vertices for rendering the message
    private int numVertices;

    // Default size and position for the message
    private float defaultSize;
    private float defaultX;
    private float defaultY;

    /**
     * Constructs a message object with default settings and initializes openGL resources.
     * @param context The application context for loading resources.
     * @param screenWidth The width of the screen for viewport calculation.
     * @param screenHeight The height of the screen for viewport calculation.
     */
    public Message(Context context, float screenWidth, float screenHeight)
    {
        message = "";
        GLManager.loadTexture(context, this);

        orthoM(viewportMatrix, 0, 0, screenWidth, screenHeight, 0, 0f, 1f);

        //define center of object as
        float halfH = screenHeight / 4;

        defaultSize = halfH;
        defaultX = screenWidth/2; //center is halfway across the screen
        defaultY = screenHeight/8; //center is top 1/8 of screen

        glProgram = GLManager.getGLAchievementProgram();
    }

    /**
     * Sets the persistence of the message. If true, the message stays on screen.
     * @param b Boolean indicating message persistence.
     */
    public void setPersistent(boolean b)
    {
        persistent = b;
    }

    /**
     * Gets the coordinates based off the text atlas
     * @param c the character we want
     * @return the 8 texture coordinates that represent the bottom-left, top-left, top-right, and bottom-right respectively
     */
    private float[] getTexCoordsForChar(char c)
    {
        // Assuming the first character in the atlas is '!' and has ASCII value 33
        int ascii = (int) c;
        int index = ascii - 32; // Adjust the starting index based on your texture atlas

        // Calculate the row and column in the atlas
        int row = (index / numberOfRowsInSpriteSheet);
        int col = (index % numberOfColumnsInSpriteSheet);

        // Calculate the normalized texture coordinates
        float s = (float)(col * frameWidth) / (spriteSheetWidth);
        float t = (float)(row * frameHeight) / (spriteSheetHeight);
        float sMax = s + (float)(frameWidth) / spriteSheetWidth;
        float tMax = t + (float)(frameHeight) / spriteSheetHeight;

        // OpenGL ES texture coordinates start at the bottom-left corner
        // so we need to invert the y-coords by subtracting from 1
        float[] texCoords = {
                s, t, // Bottom-left corner
                s, tMax,    // Top-left corner
                sMax, tMax, // Top-right corner
                sMax, t    // Bottom-right corner
        };

        return texCoords;
    }

    /**
     * Generates vertex data for rendering text based on the provided string.
     * @param text The text string to render.
     */
    public void generateText(String text)
    {
        float char_width = defaultSize;
        float x = defaultX - char_width/2 * text.length();
        while(x < 0) // keep it all on screen in case they have a long name
        {
            char_width *= 0.9;
            x = defaultX - char_width/2 * text.length();
        }
        float y = defaultY;
        generateText(text, char_width, x, y);
    }
    /**
     * Overloaded method to generate text within specified bounds.
     * @param text The text string to render.
     * @param leftBound The left boundary for text rendering.
     * @param rightBound The right boundary for text rendering.
     * @param xCenter The center X coordinate for text placement.
     * @param yCenter The center Y coordinate for text placement.
     */
    public void generateText(String text, float leftBound, float rightBound, float xCenter, float yCenter)
    {
        float char_width = 100;
        int longestString = 0;
        int tempStringCounter = 0;
        for(int i = 0; i < text.length(); ++i)
        {
            if (text.charAt(i) != '\n')
            {
                ++tempStringCounter;
            }
            else
            {
                if(longestString < tempStringCounter)
                {
                    longestString = tempStringCounter;
                    tempStringCounter = 0;
                }
            }
        }

        longestString = Math.max(tempStringCounter,longestString);
        float x = xCenter - char_width/2 * longestString;
        float y = xCenter + char_width/2 * longestString;
        while(x < leftBound || y > rightBound) // keep it all in bounds
        {
            char_width *= 0.95;
            x = xCenter - char_width/2 * longestString;
            y = xCenter + char_width/2 * longestString;
        }

        generateText(text, char_width, xCenter - char_width/2 * longestString, yCenter);
    }
    /**
     * Method to generate text within specified bounds.
     * @param text The text string to render.
     * @param size the size of the characters
     * @param x the beginning left position
     * @param y the beginning right position
     */
    public void generateText(String text, float size, float x, float y)
    {
        final float CHAR_WIDTH = size; // Width of each character quad
        final float CHAR_HEIGHT = size; // Height of each character quad
        float cursorX = x;
        float cursorY = y;

        //if there is a new line character, we want to skip it and go down a line and start again at beginning x
        int newLines = 0;
        for(int i = 0; i < text.length(); ++i)
        {
            if (text.charAt(i) == '\n')
            {
                ++newLines;
            }
        }
        float[] modelVertices = new float[(text.length()-newLines) * 5 * 4]; // there is 5 floats per vertex and 4 vertices per char

        newLines = 0;
        vertices = null;
        // Iterate over each character in the text
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if(c == '\n')
            {
                //skip and go down and back to beginning left
                cursorY += CHAR_HEIGHT;
                cursorX = x;
                ++newLines;
                continue;
            }
            // Calculate texture coordinates for the character
            float[] texCoords = getTexCoordsForChar(c);

            float[] verticesPos = new float[]{
                    cursorX, cursorY, 0.0f, // Bottom left
                    cursorX, cursorY + CHAR_HEIGHT, 0.0f, // Top left
                    cursorX + CHAR_WIDTH, cursorY + CHAR_HEIGHT, 0.0f, // Top right
                    cursorX + CHAR_WIDTH, cursorY, 0.0f // Bottom right
            };

            // each vertex has 20 bytes (5 floats (x, y, z, s, t) * 4 bytes per float)
            int bytesPerVertex = 5 * FLOAT_SIZE;
            for (int j = 0; j < 4; j++)
            {
                // Position to start updating in the buffer for each vertex
                int bufferPosition = (i-newLines) * bytesPerVertex + j * (POSITION_ATTRIBUTE_SIZE + TEXTURE_COORDINATES_ATTRIBUTE_SIZE);

                // Update x coordinate
                modelVertices[bufferPosition] = verticesPos[j * POSITION_ATTRIBUTE_SIZE]; //times by just the POISITION_ATTRIBUTE_SIZE since that's what it is

                // Update y coordinate
                modelVertices[bufferPosition+1] = verticesPos[j * POSITION_ATTRIBUTE_SIZE + 1];

                // Update z coordinate
                modelVertices[bufferPosition+2] = verticesPos[j * POSITION_ATTRIBUTE_SIZE + 2];

                // Update s coordinate
                modelVertices[bufferPosition+3] = texCoords[j * TEXTURE_COORDINATES_ATTRIBUTE_SIZE]; //times by just the TEXTURE_COORDINATES_ATTRIBUTE_SIZE since that's what it is

                // Update t coordinate
                modelVertices[bufferPosition+4] = texCoords[j * TEXTURE_COORDINATES_ATTRIBUTE_SIZE + 1];
            }

            // Move cursor to the right for the next character
            cursorX += CHAR_WIDTH;
        }
        numVertices = modelVertices.length/5;
        vertices = ByteBuffer.allocateDirect(modelVertices.length * FLOAT_SIZE) // 5 vertices per char and float is 4 bytes
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertices.put(modelVertices);
        vertices.position(0);

        message = text;
        startTime = System.currentTimeMillis();
    }
    /**
     * Draws the generated text on the screen.
     */
    public void draw()
    {
        if(message == "")
        {
            return;
        }

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        // tell openGL to use the glProgram
        glUseProgram(glProgram);
        float alphaValue = calculateAlphaValue();
        if(persistent)
        {
            alphaValue = 1;
        }
        GLManager.setVertexAttribPointer(vertices);
        GLManager.setMatrix(viewportMatrix, textureID);

        GLES20.glUniform1f(uAlphaLocation, alphaValue);
        // Draw the point, lines or triangle one character at a time
        for(int i = 0; i < numVertices; i+=4)
        {
            glDrawArrays(GL_TRIANGLE_FAN, i, 4);
        }
        // Disable blending and vertex array after drawing
        GLES20.glDisable(GLES20.GL_BLEND);
        glDisableVertexAttribArray(aPositionLocation);
        glDisableVertexAttribArray(aTextureCoordinatesLocation);
    }

    /**
     * Calculates the alpha value for fading animations based on elapsed time.
     * @return The calculated alpha value.
     */
    public float calculateAlphaValue() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;
        float fadeDuration = 200; // 200ms for fade-in and fade-out
        float duration = 2800;
        if (elapsedTime < fadeDuration)
        {
            // Fade in
            return elapsedTime / fadeDuration;
        } else if (elapsedTime < (duration - fadeDuration))
        {
            // Fully visible
            return 1.0f;
        } else if (elapsedTime < duration)
        {
            // Fade out
            return (duration - elapsedTime) / fadeDuration;
        } else
        {
            // Fully transparent after animation
            return 0.0f;
        }
    }
    /**
     * Returns the resource ID of the texture containing the characters.
     * @param context The application context.
     * @return The resource ID for the texture.
     */
    public int getTextureResourceId(Context context)
    {
        Drawable drawable = context.getDrawable(R.drawable.characters);
        spriteSheetWidth = drawable.getIntrinsicWidth();
        spriteSheetHeight = drawable.getIntrinsicHeight();
        numberOfColumnsInSpriteSheet = 10;
        numberOfRowsInSpriteSheet = 10;
        frameWidth = spriteSheetWidth / numberOfColumnsInSpriteSheet;
        frameHeight = spriteSheetHeight / numberOfRowsInSpriteSheet;
        return R.drawable.characters;
    }

    /**
     * Sets the openGL texture ID for the text atlas.
     * @param id The OpenGL texture ID.
     */
    public void setTextureID(int id)
    {
        textureID = id;
    }
}
