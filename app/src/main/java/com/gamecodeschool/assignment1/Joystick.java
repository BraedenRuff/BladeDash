package com.gamecodeschool.assignment1;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINE_LOOP;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glLineWidth;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.Matrix.orthoM;
import static android.opengl.Matrix.translateM;

import static com.gamecodeschool.assignment1.GLManager.aPositionLocation;
import static com.gamecodeschool.assignment1.GLManager.aTextureCoordinatesLocation;
import static com.gamecodeschool.assignment1.GLManager.uColorLocation;
import static com.gamecodeschool.assignment1.GLManager.uMatrixLocation;

import android.graphics.PointF;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Renders a joystick using openGL. It consists of an outer and an inner circle to represent the joystick's movement range and position.
 * @author Braeden Ruff
 */
public class Joystick
{

    // Matrix for the viewport transformation.
    private final float[] viewportMatrix = new float[16];

    // openGL shader program for drawing (colorProgram).
    private static int glProgram;

    // Buffer for the inner circle's vertices.
    private FloatBuffer verticesInner;

    // Buffer for the outer circle's vertices.
    private FloatBuffer verticesOuter;

    // Number of sides to approximate the circles.
    int numberOfSides;

    // Model matrix for transformations.
    private float[] modelMatrix = new float[16];

    // Center of the inner circle (joystick position).
    private PointF innerCenter;

    // Center of the outer circle (joystick boundary).
    private PointF outerCenter;

    /**
     * Constructs a Joystick object with defined centers and radii.
     * @param centerOfJoyStick The center point of the joystick.
     * @param radii The radii of the inner and outer circles.
     * @param gm The GameManager for accessing screen properties.
     */
    public Joystick(PointF centerOfJoyStick, PointF radii, GameManager gm)
    {
        innerCenter = centerOfJoyStick;
        outerCenter = centerOfJoyStick;
        // The HUD needs its own viewport
        orthoM(viewportMatrix, 0, 0, gm.screenWidth, gm.screenHeight, 0, 0f, 1f);

        // Number of sides for the circle (more sides = smoother circle)
        numberOfSides = 10;
        int innerRadius = (int) radii.x;
        int outerRadius = (int) radii.y;
        int totalVertices = numberOfSides * 2; // Each circle has numberOfSides * 2 vertices, and we have 2 circles
        float[] modelVertices = new float[totalVertices * 3]; // x, y, z for each vertex
        // Calculate vertices for both the outer and inner circle
        float doublePiDividedBySides = (float) (2 * Math.PI / numberOfSides);
        // First, define vertices for the outer circle
        for (int i = 0; i < numberOfSides; i++) {
            float angle = i * doublePiDividedBySides;
            int vertexIndex = i * 3; // Each vertex has 3 components (x, y, z)

            modelVertices[vertexIndex] = centerOfJoyStick.x + outerRadius * (float) Math.cos(angle); // x
            modelVertices[vertexIndex + 1] = centerOfJoyStick.y + outerRadius * (float) Math.sin(angle); // y
            modelVertices[vertexIndex + 2] = 0.0f; // z
        }
        // Initialize the vertices ByteBuffer object
        verticesOuter = ByteBuffer.allocateDirect(modelVertices.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        verticesOuter.put(modelVertices);
        verticesOuter.position(0);
        // Next, define vertices for the inner circle
        for (int i = 0; i < numberOfSides; i++) {
            float angle = i * doublePiDividedBySides;
            int vertexIndex = i * 3; // Adjust the index for the inner circle

            modelVertices[vertexIndex] = centerOfJoyStick.x + innerRadius * (float) Math.cos(angle); // x
            modelVertices[vertexIndex + 1] = centerOfJoyStick.y + innerRadius * (float) Math.sin(angle); // y
            modelVertices[vertexIndex + 2] = 0.0f; // z
        }
        // Initialize the vertices ByteBuffer object
        verticesInner = ByteBuffer.allocateDirect(modelVertices.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        verticesInner.put(modelVertices);
        verticesInner.position(0);

        glProgram = GLManager.getGLColorProgram();
    }
    /**
     * Draws the joystick on the screen using openGL (no texture)
     */
    public void draw()
    {
        // And tell OpenGl to use the glProgram
        glUseProgram(glProgram);

        verticesInner.position(0);

        glVertexAttribPointer(
                aPositionLocation,
                3,
                GL_FLOAT,
                false,
                3*4,
                verticesInner);

        glEnableVertexAttribArray(aPositionLocation);

        // Just give the passed in matrix to OpenGL
        glUniformMatrix4fv(uMatrixLocation, 1, false, viewportMatrix, 0);
        // Assign a color to the fragment shader
        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        // Draw the lines
        // start at the first element of the vertices array and read in all vertices
        glLineWidth(8);
        glDrawArrays(GL_LINE_LOOP, 0, numberOfSides);

        verticesOuter.position(0);

        glVertexAttribPointer(
                aPositionLocation,
                3,
                GL_FLOAT,
                false,
                3*4,
                verticesOuter);

        glEnableVertexAttribArray(aPositionLocation);
        modelMatrix = viewportMatrix.clone();
        // Make a translation matrix
        translateM(modelMatrix, 0, innerCenter.x-outerCenter.x, -outerCenter.y + innerCenter.y, 0);

        // Give the matrix to OpenGL
        glUniformMatrix4fv(uMatrixLocation, 1, false, modelMatrix, 0);
        // Assign a color to the fragment shader
        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_LINE_LOOP, 0, numberOfSides);

        // Disable vertex array after drawing
        glDisableVertexAttribArray(aPositionLocation);
        glDisableVertexAttribArray(aTextureCoordinatesLocation);
    }

    /**
     * Sets the center position of the inner circle, representing the joystick's current position.
     * @param point The new center point for the inner circle.
     */
    public void setInnerCenter(PointF point)
    {
        innerCenter = point;
    }
}
