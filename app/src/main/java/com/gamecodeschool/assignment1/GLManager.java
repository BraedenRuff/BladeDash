package com.gamecodeschool.assignment1;

import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.FloatBuffer;
import java.util.HashMap;

/**
 * This class manages openGL shader programs and texture loading.
 * Provides utility functions for compiling shaders, linking programs,
 * setting up vertex attributes, and handling textures.
 * @author Braeden Ruff
 */
public class GLManager {

    // Some constants to help count the number of bytes between
    // elements of our vertex data arrays
    // Update your constants

    //for size of floats in bytes
    public  static final int FLOAT_SIZE = 4;

    //for the size of the position attributes in our shaders (x, y, z for everythign)
    public static final int POSITION_ATTRIBUTE_SIZE = 3;

    //for the size of the texture coordinates (s, t)
    public static final int TEXTURE_COORDINATES_ATTRIBUTE_SIZE = 2;

    //for the stride, how many bytes inbetween vertices
    public static final int STRIDE = (POSITION_ATTRIBUTE_SIZE + TEXTURE_COORDINATES_ATTRIBUTE_SIZE) * FLOAT_SIZE;


    // to help represent variable types in our shaders code
    public static final String U_MATRIX = "u_Matrix";
    public static final String A_POSITION = "a_Position";
    public static final String U_COLOR = "u_Color";
    public static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";
    public static final String U_TEXTURE_UNIT = "u_TextureUnit";
    public static final String U_ALPHA_LOCATION = "u_Alpha";
    public static final String U_GREY_SCALE_LOCATION = "u_GreyScale";

    // matching int for the location in the openGL glProgram
    public static int uMatrixLocation;
    public static int aPositionLocation;
    public static int uColorLocation;
    public static int aTextureCoordinatesLocation;
    public static int uTextureUnit;
    public static int uAlphaLocation;
    public static int uGreyScaleLocation;

    //a map of textures so we don't have to regenerate every texture and can instead reuse many
    private static HashMap<Integer, Integer> textureMap = new HashMap<>();

    /**
     * Static method to reset the texture map (useful when closing the program and reopening
     * @param context the context of the program
     */
    public static void resetTextureMap(Context context)
    {
        textureMap = new HashMap<>();
    }

    //vertex shader in GLSL code
    private static String vertexShader =
            "uniform mat4 u_Matrix;" +
                    "attribute vec4 a_Position;" +
                    "attribute vec2 a_TextureCoordinates;" +
                    "varying vec2 v_TextureCoordinates;" +

                    "void main()" +
                    "{" +
                    "    v_TextureCoordinates = a_TextureCoordinates;" +
                    "    gl_Position = u_Matrix * a_Position;" +
                    "}";

    // the fragment shader for our textureProgram
    private static String textureFragmentShader =
            "precision mediump float;" +
                    "uniform sampler2D u_TextureUnit;" +
                    "varying vec2 v_TextureCoordinates;" +

                    "void main()" +
                    "{" +
                    "    vec4 texColor = texture2D(u_TextureUnit, v_TextureCoordinates);" +
                    "    if(texColor.a < 0.1) {" +
                    "         discard;" +
                    "     }" +
                    "     gl_FragColor = texColor;" +
                    "}";

    // the fragment shader for our colorProgram
    private static String colorFragmentShader =
            "precision mediump float;" +
                    "uniform vec4 u_Color;" +
                    "void main()" +
                    "{" +
                    "    gl_FragColor = u_Color;" +
                    "}";

    //the fragment shader for our achievementProgram
    private static String achievementFragmentShader =
            "precision mediump float;" +
                    "uniform sampler2D u_TextureUnit;" +
                    "uniform float u_Alpha;" + // uniform to adjust transparency
                    "uniform int u_GreyScale;" + // uniform to toggle grayscale
                    "varying vec2 v_TextureCoordinates;" +
                    "void main() {" +
                    "    vec4 texColor = texture2D(u_TextureUnit, v_TextureCoordinates);" +
                    "    if(u_GreyScale == 1) {" +
                    "        float gray = dot(texColor.rgb, vec3(0.299, 0.587, 0.114));" + //looked up how to change things grey scale, this is a common ratio
                    "        gl_FragColor = vec4(gray, gray, gray, texColor.a * u_Alpha);" +
                    "    } else {" +
                    "        gl_FragColor = vec4(texColor.rgb, texColor.a * u_Alpha);" + // Original color
                    "    }" +
                    "}";

    // A handle to the GL glProgram
    private static int textureProgram;
    private static int colorProgram;
    private static int achievementProgram;


    public static int getGLTextureProgram()
    {
        return textureProgram;
    }
    public static int getGLColorProgram()
    {
        return colorProgram;
    }
    public static int getGLAchievementProgram()
    {
        return achievementProgram;
    }

    /**
     * Compiles and links shader programs for the textureProgram.
     * @return The handle to the textureProgram.
     */
    public static int buildProgramTexture()
    {
        int vertexShaderId = compileShader(GL_VERTEX_SHADER, vertexShader);
        int colorFragmentShaderId = compileShader(GL_FRAGMENT_SHADER, textureFragmentShader);
        return linkProgramTexture(vertexShaderId,colorFragmentShaderId);
    }

    /**
     * Compiles and links shader programs for the colorProgram.
     * @return The handle to the colorProgram.
     */
    public static int buildColorProgram()
    {
        int vertexShaderId = compileShader(GL_VERTEX_SHADER, vertexShader);
        int colorFragmentShaderId = compileShader(GL_FRAGMENT_SHADER, colorFragmentShader);
        return linkProgramColor(vertexShaderId, colorFragmentShaderId);
    }

    /**
     * Compiles and links shader programs for the achievementProgram.
     * @return The handle to the achievementProgram.
     */
    public static int buildAchievementProgram()
    {
        int vertexShaderId = compileShader(GL_VERTEX_SHADER, vertexShader);
        int achievementFragmentShaderId = compileShader(GL_FRAGMENT_SHADER, achievementFragmentShader);
        return linkProgramAchievement(vertexShaderId, achievementFragmentShaderId);
    }

    /**
     * Compiles a shader
     * @param type the GL shader type (we only used GL_VERTEX_SHADER and GL_FRAGMENT_SHADER)
     * @param shaderCode the string of GLSL code
     * @return the compiled shader id
     */
    private static int compileShader(int type, String shaderCode)
    {

        // Create a shader object and store its ID
        final int shader = glCreateShader(type);

        // Pass in the code then compile the shader
        glShaderSource(shader, shaderCode);
        glCompileShader(shader);

        return shader;
    }

    /**
     * This links the shaders into a newly created program
     * @param vertexShader the id of the vertex shader we want to link
     * @param fragmentShader the id of the fragment shader we want to link
     * @return the newly created program id
     */
    private static int linkShadersIntoProgram(int vertexShader, int fragmentShader)
    {
        // Create a new GL program
        int programId = glCreateProgram();

        // Attach the vertex shader to the program.
        glAttachShader(programId, vertexShader);

        // Attach the fragment shader to the program.
        glAttachShader(programId, fragmentShader);

        // Link the two shaders together into the program.
        glLinkProgram(programId);

        // Optionally, add error checking here to validate the linking process

        return programId;
    }

    /**
     * this creates and links the shaders into the textureProgram
     * @param vertexShader the id of the vertex shader we want to link
     * @param fragmentShader the id of the fragment shader we want to link
     * @return textureProgram's id
     */
    private static int linkProgramTexture(int vertexShader, int fragmentShader)
    {
        textureProgram = linkShadersIntoProgram(vertexShader, fragmentShader);
        return textureProgram;
    }

    /**
     * this creates and links the shaders into the colorProgram
     * @param vertexShader the id of the vertex shader we want to link
     * @param fragmentShader the id of the fragment shader we want to link
     * @return colorProgram's id
     */
    private static int linkProgramColor(int vertexShader, int fragmentShader)
    {
        colorProgram = linkShadersIntoProgram(vertexShader, fragmentShader);
        return colorProgram;
    }

    /**
     * this creates and links the shaders into the achievementProgram
     * @param vertexShader the id of the vertex shader we want to link
     * @param fragmentShader the id of the fragment shader we want to link
     * @return achievementProgram's id
     */
    private static int linkProgramAchievement(int vertexShader, int fragmentShader)
    {
        achievementProgram = linkShadersIntoProgram(vertexShader, fragmentShader);
        return achievementProgram;
    }

    /**
     * Loads a texture from resources and caches it.
     * If the texture is already loaded, it returns the existing texture ID.
     * @param context The application context.
     * @param texturable An object that implements the Texturable interface.
     */
    public static void loadTexture(Context context, Texturable texturable)
    {
        int resourceId = texturable.getTextureResourceId(context);
        int textureId = getOrLoadTexture(context, resourceId);

        if (textureId != -1)
        {
            texturable.setTextureID(textureId);
        }
        // Check if object is an instance of Player and load specific texture
        if (texturable instanceof Player)
        {
            Player player = (Player) texturable;
            int slashTextureId = loadTextureFromResourceId(context, player.getSlashTextureId());
            player.setSlashTexture(slashTextureId);
        }
    }

    /**
     * Retrieves or loads a texture identified by a resource ID.
     * @param context The application context.
     * @param resourceId The resource ID of the texture.
     * @return The openGL texture ID.
     */
    private static int getOrLoadTexture(Context context, int resourceId)
    {
        if (resourceId == -1) {
            return -1; // Early return if the resource ID is invalid
        }

        if (textureMap.containsKey(resourceId)) {
            return textureMap.get(resourceId); // Return existing texture ID if already loaded
        } else {
            // Load the texture and add it to the map
            int textureId = loadTextureFromResourceId(context, resourceId);
            textureMap.put(resourceId, textureId);
            return textureId;
        }
    }

    /**
     * Loads a texture from a resource ID.
     * Initializes OpenGL texture parameters and binds the texture.
     * @param context The application context.
     * @param resourceId The resource ID of the texture.
     * @return The OpenGL texture ID.
     */
    private static int loadTextureFromResourceId(Context context, int resourceId)
    {
        final int[] textureObjectIds = new int[1];
        GLES20.glGenTextures(1, textureObjectIds, 0);

        if (textureObjectIds[0] == 0) {
            Log.e("GLManager", "Could not generate a new OpenGL texture object.");
            return 0;
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = true; // No pre-scaling

        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

        if (bitmap == null)
        {
            Log.e("GLManager", "Resource ID " + resourceId + " could not be decoded.");
            GLES20.glDeleteTextures(1, textureObjectIds, 0);
            return 0;
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectIds[0]);

        //adjusting the parameters a bit
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        bitmap.recycle();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        //put in textureMap so we don't have to regenerate
        textureMap.put(resourceId, textureObjectIds[0]);

        return textureObjectIds[0];
    }

    /**
     * Sets up vertex attribute pointers for position and texture coordinates.
     * @param vertices The vertex buffer containing position and texture coordinates.
     */
    public static void setVertexAttribPointer(FloatBuffer vertices)
    {
        // Set vertices to the first byte
        vertices.position(0);

        glVertexAttribPointer(aPositionLocation, POSITION_ATTRIBUTE_SIZE, GLES20.GL_FLOAT, false, STRIDE, vertices);
        glEnableVertexAttribArray(aPositionLocation);

        // Set texture coordinates attribute
        vertices.position(POSITION_ATTRIBUTE_SIZE); // Move position to the start of texture coordinates
        glVertexAttribPointer(aTextureCoordinatesLocation, TEXTURE_COORDINATES_ATTRIBUTE_SIZE, GLES20.GL_FLOAT, false, STRIDE, vertices);
        glEnableVertexAttribArray(aTextureCoordinatesLocation);
    }

    /**
     * Passes matrix and texture information to the shader program.
     * @param viewportMatrix The combined model-view-projection matrix.
     * @param textureID The OpenGL texture ID.
     */
    public static void setMatrix(float[] viewportMatrix, int textureID)
    {
        // Give the matrix to OpenGL
        glUniformMatrix4fv(uMatrixLocation, 1, false, viewportMatrix, 0);
        // Assign a color to the fragment shader
        glActiveTexture(GLES20.GL_TEXTURE0);
        glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
        glUniform1i(uTextureUnit, 0);
    }

    /**
     * Cleans up after drawing. Disables vertex attributes to keep state clean.
     * @param numVertices The number of vertices to draw.
     */
    public static void drawCleanup(int numVertices)
    {
        glDrawArrays(GL_TRIANGLE_FAN, 0, numVertices);
        // Disable blending and vertex array after drawing
        glDisableVertexAttribArray(aPositionLocation);
        glDisableVertexAttribArray(aTextureCoordinatesLocation);
    }

    /**
     * Applies translation and rotation to the model matrix and combines it with the viewport matrix.
     * Useful for positioning and rotating game objects.
     * @param viewportModelMatrix The matrix to store the result.
     * @param viewportMatrix The viewport matrix.
     * @param translateX X translation.
     * @param translateY Y translation.
     * @param angle Rotation angle in degrees.
     */
    public static void translateAndRotate(float[] viewportModelMatrix, float[] viewportMatrix, float translateX, float translateY, float angle)
    {
        float[] modelMatrix = new float[16];
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, translateX, translateY, 0);
        // Apply rotation here if needed
        rotateM(modelMatrix, 0, -angle, 0, 0, 1); // note the negative angle

        // Combine model with viewport matrix
        multiplyMM(viewportModelMatrix, 0, viewportMatrix, 0, modelMatrix, 0);
    }

    /**
     * Applies translation to the model matrix and combines it with the viewport matrix.
     * Useful for positioning game objects.
     * @param viewportModelMatrix The matrix to store the result.
     * @param viewportMatrix The viewport matrix.
     * @param translateX X translation.
     * @param translateY Y translation.
     */
    public static void translate(float[] viewportModelMatrix, float[] viewportMatrix, float translateX, float translateY)
    {
        float[] modelMatrix = new float[16];
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, translateX, translateY, 0);

        // Combine model with viewport matrix
        multiplyMM(viewportModelMatrix, 0, viewportMatrix, 0, modelMatrix, 0);
    }
}