

package com.gamecodeschool.assignment1;

import static android.opengl.GLES20.glUseProgram;

import static com.gamecodeschool.assignment1.GLManager.*;

import android.content.Context;
import android.graphics.PointF;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import kotlin.NotImplementedError;

/**
 * This class is used as a base for all of our game objects, (something the player can interact with, or is the player themselves)
 * @author Braeden Ruff
 */
public abstract class GameObject implements Texturable
{
    //the texture id for openGL to draw
    private int textureID;

    //for how fast the object can move
    private float maxVelocity = 0f;

    //for how fast the object can accelerate
    private float maxAccel = 5f;

    //objects current velocity in the x-axis
    private float xVelocity = 0f;

    //objects current velocity in the y-axis
    private float yVelocity = 0f;

    //how fast gravity accelerates the object
    protected float gravity = 3;

    //the glProgram we are using
    private static int glProgram =-1;

    //the angle we are facing, 0 is to the right
    private double facingAngle = -Math.PI/2; //straight up

    // how many vertices does openGL need to draw
    private int numVertices;


    // the coordinates of the vertices (x,y,z,s,t) per vertex, and there is four for a default gameobject
    private float[] modelVertices;

    // the centre of the object in game world coordinates
    private PointF worldLocation = new PointF();

    // basically will have what is in modelVertices, but openGL likes FloatBuffer
    private FloatBuffer vertices;

    // intermediary matrix for openGL translation and rotation
    float[] viewportModelMatrix = new float[16];

    // dimensions of the game object
    private float width, height;

    /**
     * This enum is used to determine how two objects collided
     */
    public enum collisionType {NONE, TOP, BOTTOM, LEFT, RIGHT};

    /**
     * This is the constructor of the game object
     * @param context - the context of the program, needed for animation spritesheets to determine the height and width
     */
    public GameObject(Context context)
    {
        GLManager.loadTexture(context, this);
        if (glProgram == -1){
            setGLProgram();
        }
    }

    /**
     * This method is used to determine how two objects collided
     * @param other the object that this object is colliding with
     * @return how two objects collided. E.g: other is above this object and collided, it will return TOP
     */
    Enum<collisionType> getCollisionDirection(GameObject other)
    {
        PointF centerThis = this.getWorldLocation();
        PointF centerOther = other.getWorldLocation();

        float dx = centerOther.x - centerThis.x; //+
        float dy = centerOther.y - centerThis.y; //+

        float combinedHalfWidths = (this.width + other.width) / 2;
        float combinedHalfHeights = (this.height + other.height) / 2;

        if (Math.abs(dx) < combinedHalfWidths && Math.abs(dy) < combinedHalfHeights) //then they collided
        {
            float overlapX = combinedHalfWidths - Math.abs(dx); //how deep in the x direction
            float overlapY = combinedHalfHeights - Math.abs(dy); //how deep in the y direction

            if (overlapX >= overlapY) {
                return (dy > 0) ? collisionType.TOP : collisionType.BOTTOM;
            } else {
                return (dx > 0) ? collisionType.LEFT : collisionType.RIGHT;
            }
        }
        return collisionType.NONE;
    }

    /**
     * This method slows the object to a stop, using the maximum acceleration
     * @param fps how much time has passed (influences how much the velocity should be adjusted)
     */
    public void comeToStop(long fps)
    {
        if(getxVelocity() > 0)
        {
            setxVelocity(Math.max(getxVelocity() + (-getMaxVelocity() * getMaxAccel() / fps), -getMaxVelocity()));
            if(getxVelocity() < 0) //we passed 0
            {
                setxVelocity(0);
            }
        }
        else
        {
            setxVelocity(Math.min(getxVelocity() + (getMaxVelocity() * getMaxAccel() / fps), getMaxVelocity()));
            if(getxVelocity() > 0) //we passed 0
            {
                setxVelocity(0);
            }
        }
    }

    /**
     * This method sets the glProgram and the locations for our GLManager
     */
    public void setGLProgram()
    {
        glProgram = GLManager.getGLTextureProgram();
    }

    /**
     * This method sets the size of the GameObject
     * @param w is the width
     * @param h is the height
     */
    public void setSize(float w, float h)
    {
        width = w;
        height = h;

    }

    /**
     * This method sets the vertices for most of the game objects (which are rectangles)
     */
    public void setDefaultVertices()
    {
        //define center of object as worldlocationx and worldlocationy
        float halfW = width / 2;
        float halfH = height / 2;

        float[] vertices = new float[] {
                // Position         // Texture Coordinates
                -halfW, -halfH, 0,   0.0f, 1.0f,  // Bottom left corner
                halfW, -halfH, 0,    1.0f, 1.0f,  // Bottom right corner
                halfW, halfH, 0,     1.0f, 0.0f,  // Top right corner
                -halfW, halfH, 0,    0.0f, 0.0f   // Top left corner
        };

        setVertices(vertices);
    }

    /**
     * This method sets the width of the GameObject
     * @return the width
     */
    public float getWidth()
    {
        return width;
    }

    /**
     * This method sets the height of the GameObject
     * @return the height
     */
    public float getHeight()
    {
        return height;
    }

    /**
     * Sets the GameObject's facing angle. 0 is to the right, and the angle increases clockwise until PI, then switches to -PI and decreases.
     * @param angle the new facing angle in radians.
     */
    public void setFacingAngle(double angle)
    {
        facingAngle = angle;
    }

    /**
     * Retrieves the GameObject's current facing angle.
     * @return the facing angle in radians.
     */
    public double getFacingAngle()
    {
        return facingAngle;
    }

    /**
     * Retrieves the GameObject's current velocity along the x-axis.
     * @return the x-axis velocity.
     */
    public float getxVelocity()
    {
        return xVelocity;
    }

    /**
     * Sets the GameObject's velocity along the x-axis.
     * @param xVelocity the new x-axis velocity.
     */
    public void setxVelocity(float xVelocity)
    {
        this.xVelocity = xVelocity;
    }

    /**
     * Retrieves the GameObject's current velocity along the y-axis.
     * @return the y-axis velocity.
     */
    public float getyVelocity()
    {
        return yVelocity;
    }

    /**
     * Sets the GameObject's velocity along the y-axis.
     * @param yVelocity the new y-axis velocity.
     */
    public void setyVelocity(float yVelocity)
    {
        this.yVelocity = yVelocity;
    }

    /**
     * This method applies gravity to the object
     * @param fps the current frame rate (frames per second)
     */
    public void applyGravity(long fps)
    {
        setyVelocity(getyVelocity() - (getMaxVelocity() * gravity / fps));
    }

    /**
     * Retrieves the maximum acceleration of the GameObject.
     * @return the maximum acceleration.
     */
    float getMaxAccel()
    {
        return maxAccel;
    }

    /**
     * Sets the maximum acceleration for the GameObject.
     * @param maxAccel the new maximum acceleration.
     */
    public void setMaxAccel(float maxAccel)
    {
        this.maxAccel = maxAccel;
    }

    /**
     * Retrieves the maximum velocity of the GameObject.
     * @return the maximum velocity.
     */
    public float getMaxVelocity()
    {
        return maxVelocity;
    }

    /**
     * Moves the GameObject based on its velocity and the frame rate.
     * Ensures movement does not exceed the largest allowed per frame to prevent clipping.
     * @param fps the current frame rate.
     */
    void move(float fps)
    {
        // Calculate movement while considering the maximum possible movement to avoid clipping
        float xVel = xVelocity > 0 ? Math.min(xVelocity / fps, GameManager.getLargestMovement()) : Math.max(xVelocity / fps, -GameManager.getLargestMovement());
        float yVel = yVelocity > 0 ? Math.min(yVelocity / fps, GameManager.getLargestMovement()) : Math.max(yVelocity / fps, -GameManager.getLargestMovement());

        // Update position if there is movement
        if(xVelocity != 0)
        {
            worldLocation.x += xVel;
        }

        if(yVelocity != 0)
        {
            worldLocation.y += yVel;
        }
    }

    /**
     * Sets the maximum velocity allowed by this object
     * @param maxSpeed the maximum velocity allowed of this object
     */
    public void setMaxVelocity(float maxSpeed) {
        this.maxVelocity = maxSpeed;
    }

    /**
     * Sets the texture ID for the GameObject.
     * The texture ID is used by OpenGL to bind the correct texture during drawing.
     * @param id the OpenGL texture ID.
     */
    public void setTextureID(int id)
    {
        textureID = id;
    }

    /**
     * Retrieves the world location of the GameObject.
     * @return the world location as a PointF object.
     */
    public PointF getWorldLocation()
    {
        return worldLocation;
    }

    /**
     * Sets the world location of the GameObject.
     * @param x the x-coordinate in world space.
     * @param y the y-coordinate in world space, negated to match OpenGL's coordinate system.
     */
    public void setWorldLocation(float x, float y)
    {
        this.worldLocation.x = x;
        this.worldLocation.y = -y;
    }

    /**
     * Sets float array into the FloatBuffer vertices for openGL.
     * Calculates the number of vertices
     * @param objectVertices the vertices for the GameObject in the order of [x, y, z, s, t, ....]
     */
    public void setVertices(float[] objectVertices)
    {
        // Store how many elements
        int numElements = objectVertices.length;
        // Store num vertices for future use
        numVertices = numElements/(POSITION_ATTRIBUTE_SIZE + TEXTURE_COORDINATES_ATTRIBUTE_SIZE);

        //initialize vertices using number of elements in the float array and the number of bytes in floats
        vertices = ByteBuffer.allocateDirect(
                        numElements
                                * FLOAT_SIZE)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        //add the vertices to FloatBuffer
        vertices.put(objectVertices);
        //reset the position to the start
        vertices.position(0);
    }

    /**
     * This method updates the texture coordinates. Used for spritesheets if we only want to show a specific square. Only works if there is 4 vertices 5 elements {x,y,z,s,t}
     * @param newTextureCoords
     */
    protected void updateVerticesTextureCoords(float[] newTextureCoords)
    {
        for (int i = 0; i < 4; i++) {
            // Position to start updating in the buffer for each vertex
            int bufferPosition = i * (POSITION_ATTRIBUTE_SIZE + TEXTURE_COORDINATES_ATTRIBUTE_SIZE) + POSITION_ATTRIBUTE_SIZE;

            // Update s coordinate
            vertices.put(bufferPosition, newTextureCoords[i * 2]);

            // Update t coordinate
            vertices.put(bufferPosition + 1, newTextureCoords[i * 2 + 1]);
        }

        vertices.position(0); // Reset buffer position for future use
    }

    /**
     * This method is the default drawing operation for GameObjects. It sets the vertices, translates to the world location, and draws it and cleans up
     * @param viewportMatrix the viewport, which tells us what we should draw on the screen
     */
    protected void draw(float[] viewportMatrix)
    {
        // tell OpenGl to use the glProgram
        glUseProgram(glProgram);

        GLManager.setVertexAttribPointer(vertices);
        GLManager.translate(viewportModelMatrix, viewportMatrix, worldLocation.x,worldLocation.y);
        GLManager.setMatrix(viewportModelMatrix, textureID);
        GLManager.drawCleanup(numVertices);
    }

    /**
     * This method needs to be implemented because it implements texturable, but we only use it on subclasses
     * @param context is the context of the program, since some instances of texturable need context
     * @return a not implemented exception, shouldn't be here
     */
     public int getTextureResourceId(Context context)
     {
         throw new NotImplementedError();
    }

}
