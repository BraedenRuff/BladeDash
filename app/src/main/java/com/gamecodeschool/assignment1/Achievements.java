package com.gamecodeschool.assignment1;

import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.Matrix.orthoM;
import static com.gamecodeschool.assignment1.GLManager.uAlphaLocation;
import static com.gamecodeschool.assignment1.GLManager.uGreyScaleLocation;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import kotlin.NotImplementedError;

/**
 * This class is the holder for whether we got the achievement or not, and handles setting achievements, which achievement to show when you get it,
 * helps with loading and saving achievements, handles the drawing, deciding the transparency and grayscale
 * @author Braeden Ruff
 */
public class Achievements implements Texturable
{
    //false for the next booleans means no achievement, true means achieved
    private boolean[] noDeaths;
    private boolean[] explorer;
    private boolean proGamer;
    private boolean ebenezerKaito;
    private boolean pacifist;
    private boolean godGamer;

    //this is in milliseconds from System.currentTimeMillis(), used for calculating transparency
    private long startTime;

    //the viewport to decide what to draw
    private final float[] viewportMatrix = new float[16];

    //the glProgram we are using (achievementProgram)
    private int glProgram;

    //the 4 corners where we will draw the texture
    private FloatBuffer vertices;

    //used for drawing (found by GLManager)
    private int textureID;

    //used to display the acquiring method
    Message message;

    //the left boundary of the text inside message when drawing
    private float leftBound;

    //the right boundary of the text inside message when drawing
    private float rightBound;

    //the center on the x-axis of the whole achievement texture
    private float centerX;

    //the center on the y-axis of the whole achievement texture
    private float centerY;

    //half of the height of the whole achievement texture
    private float halfH;

    //contains the icons for every achievement type
    private ArrayList<AchievementIcon> icons;

    //used to display achievements one after another, in case 2 are acquired at once
    private Queue<AchievementNotification> achievementQueue = new LinkedList<>();

    //a single achievement notification so we can display one at a time
    private AchievementNotification currentAchievement = null;

    /**
     * The constructor for achievements without a save file
     * @param context is the context of the program, since some instances of texturable need context (not this one though, but we still need it for the loadTexture method)
     * @param screenWidth size of screen on the x-axis
     * @param screenHeight size of screen on the y-axis
     */
    public Achievements(Context context, float screenWidth, float screenHeight)
    {
        glProgram = GLManager.getGLAchievementProgram();
        GLManager.loadTexture(context, this);

        //initialize all icons
        icons = new ArrayList<>();
        for(AchievementIcon.AchievementIconTexture icon : AchievementIcon.AchievementIconTexture.values())
        {
            icons.add(new AchievementIcon(context, icon, screenWidth, screenHeight));
        }

        orthoM(viewportMatrix, 0, 0, screenWidth, screenHeight, 0, 0f, 1f);

        //all achievements are not acquired
        noDeaths = new boolean[3];
        explorer = new boolean[3];
        proGamer = false;
        ebenezerKaito = false;
        pacifist = false;
        godGamer = false;

        float halfW = screenWidth / 4; // half width of achievement pane
        halfH = screenHeight / 4; // half height of achievement pane
        float quarterH = screenHeight / 8; // quarter height of achievement pane

        float upHalfScreen = screenHeight/2; //middle on y axis (for the achievement pane)
        centerX = screenWidth/2; // middle on x-axis (for the achievement pane)

        centerY = upHalfScreen - quarterH; // middle on y-axis (for the message)
        leftBound = centerX-halfW/4*3; //first quarter on x-axis (for the message)
        rightBound = centerX+halfW/4*3; //last quarter on x-axis (for the message)

        float[] modelVertices = new float[] {
                // Position                          // Texture Coordinates
                centerX-halfW, quarterH + upHalfScreen, 0,   0.0f, 1.0f,  // Bottom left corner
                centerX+halfW, quarterH + upHalfScreen, 0,    1.0f, 1.0f,  // Bottom right corner
                centerX+halfW, -3*quarterH + upHalfScreen, 0,     1.0f, 0.0f,  // Top right corner
                centerX-halfW, -3*quarterH + upHalfScreen, 0,    0.0f, 0.0f   // Top left corner
        };

        // Store number of vertices
        vertices = ByteBuffer.allocateDirect(modelVertices.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertices.put(modelVertices);
        vertices.position(0);

        message = new Message(context, screenWidth, screenHeight);
    }

    /**
     * This method is used to set gotten achievements back to true
     * @param save is the text from the file achievements.txt, which was written to using the .toString() method
     */
    public void setBooleans(String save)
    {
        //achievements.txt is a space separated list of booleans (see .toString())
        String[] values = save.split(" ");

        //basic check to make sure since last time I loaded I didn't change the number of achievements
        if(values.length != AchievementIcon.AchievementIconTexture.values().length)
        {
            //if it gets here, try clearing your achievements.txt file and observe your .toString() method for any changes
            throw new NotImplementedError();
        }

        //a counter to track the position in the values array
        int i = 0;

        //this assumes the .toString still iterates through the same way, careful here
        for (AchievementIcon.AchievementIconTexture achievementType : AchievementIcon.AchievementIconTexture.values()) {
            boolean isUnlocked = Boolean.parseBoolean(values[i++]); // Increment i after each use

            //set the achievement state based on isUnlocked value
            switch (achievementType) {
                case NODEATHS1:
                    noDeaths[0] = isUnlocked;
                    break;
                case NODEATHS2:
                    noDeaths[1] = isUnlocked;
                    break;
                case NODEATHS3:
                    noDeaths[2] = isUnlocked;
                    break;
                case PROGAMER:
                    proGamer = isUnlocked;
                    break;
                case EXPLORER1:
                    explorer[0] = isUnlocked;
                    break;
                case EXPLORER2:
                    explorer[1] = isUnlocked;
                    break;
                case EXPLORER3:
                    explorer[2] = isUnlocked;
                    break;
                case EBENEZERKAITO:
                    ebenezerKaito = isUnlocked;
                    break;
                case PACIFIST:
                    pacifist = isUnlocked;
                    break;
                case GODGAMER:
                    godGamer = isUnlocked;
                    break;
                default:
                    throw new NotImplementedError();
            }
        }
    }
    /**
     * This method determines if an achievement is unlocked or not
     * @param achievementType the type of achievement you are checking if it is unlocked or not
     * @return true if the achievement is unlocked, false if not
     */
    public boolean checkAchievementUnlocked(AchievementIcon.AchievementIconTexture achievementType)
    {
        // Example check for a specific achievement
        switch (achievementType)
        {
            case NODEATHS1:
                return getNoDeaths(0);
            case NODEATHS2:
                return getNoDeaths(1);
            case NODEATHS3:
                return getNoDeaths(2);
            case PROGAMER:
                return getProGamer();
            case EXPLORER1:
                return getExplorer(0);
            case EXPLORER2:
                return getExplorer(1);
            case EXPLORER3:
                return getExplorer(2);
            case EBENEZERKAITO:
                return getEbenezerKaito();
            case PACIFIST:
                return getPacifist();
            case GODGAMER:
                return getGodGamer();
            default:
                throw new NotImplementedError();
        }
    }
    /**
     * This achievement is called after any achievement is set to true. It is responsible for adding a notification to the true, and if none are currently showing, display the next one
     * @param achievementText is the text that will show up in the achievement notification
     * @param icon is the icon that will show up in the achievement notification
     */
    private void showAchievement(String achievementText, AchievementIcon icon)
    {
        AchievementNotification newAchievement = new AchievementNotification(achievementText, icon);
        achievementQueue.add(newAchievement);
        if (currentAchievement == null)
        {
            displayNextAchievement();
        }
    }

    /**
     * this method sets up the message and icon to be shown, and also sets the timer for the transparency
     */
    private void displayNextAchievement()
    {
        if (!achievementQueue.isEmpty() && currentAchievement == null)
        {
            //grab and remove head of queue
            currentAchievement = achievementQueue.poll();
            //generate the message
            message.generateText(currentAchievement.message, leftBound, rightBound, centerX, centerY);
            //start the display timer
            startTime = System.currentTimeMillis();
        }
    }

    /**
     * This method grabs the specific icon we want from the list of all icons
     * @param icon is the specific icon we want
     * @return the specific icon we want from the list of initialized icons
     */
    private AchievementIcon grabIcon(AchievementIcon.AchievementIconTexture icon)
    {
        for(int i = 0; i < icons.size(); ++i)
        {
            if(icons.get(i).getIconTexture() == icon)
            {
                return icons.get(i);
            }
        }
        throw new NotImplementedError();
    }

    /**
     * This method indicates that you have got the pro-gamer achievement
     */
    private void setProGamer()
    {
        if(proGamer)
        {
            return;
        }
        proGamer = true;
        showAchievement("PRO-GAMER: COMPLETE ALL LEVELS\nWITHOUT LOSING A LIFE", grabIcon(AchievementIcon.AchievementIconTexture.PROGAMER));
    }

    /**
     * This method allows for the retrieval of the pro-gamer achievement
     * @return whether you have gotten the pro-gamer achievement
     */
    public boolean getProGamer()
    {
        return proGamer;
    }

    /**
     * This method indicates that you have got the Ebenezer Kaito achievement
     */
    public void setEbenezerKaito()
    {
        if(ebenezerKaito)
        {
            return;
        }
        ebenezerKaito = true;
        showAchievement("EBENEZER KAITO: COLLECT ALL COLLECTIBLES\nIN EVERY LEVEL", grabIcon(AchievementIcon.AchievementIconTexture.EBENEZERKAITO));
    }
    /**
     * This method allows for the retrieval of the Ebenezer-Kaito achievement
     * @return whether you have gotten the Ebenezer-Kaito achievement
     */
    public boolean getEbenezerKaito()
    {
        return ebenezerKaito;
    }

    /**
     * This method indicates the achievements that you have got the Explorer achievement on a specific level
     * @param index the level which you got the Explorer achievement
     */
    public void setExplorer(int index)
    {
        if(explorer[index])
        {
            return;
        }
        explorer[index] = true;
        switch(index)
        {
            case 0:
                showAchievement("FOREST EXPLORER: COLLECT ALL COLLECTIBLES\nIN THE FOREST", grabIcon(AchievementIcon.AchievementIconTexture.EXPLORER1));
                break;
            case 1:
                showAchievement("DESERT DRIFTER: COLLECT ALL COLLECTIBLES\nIN THE DESERT", grabIcon(AchievementIcon.AchievementIconTexture.EXPLORER2));
                break;
            case 2:
                showAchievement("TOASTY ADVENTURER: COLLECT ALL COLLECTIBLES\nIN THE VOLCANO", grabIcon(AchievementIcon.AchievementIconTexture.EXPLORER3));
                break;
        }
        boolean allFound = true;
        for(int i = 0; i < explorer.length; ++i)
        {
            if(!explorer[i])
            {
                allFound = false;
            }
        }
        if(allFound)
        {
            setEbenezerKaito();
        }
    }

    /**
     * This method allows for the retrieval of the Explorer achievement on a specific level
     * @param index the level which you are retrieving the Explorer achievement obtainment value
     * @return whether you got the Explorer achievement on a specific level or not
     */
    public boolean getExplorer(int index)
    {
        return explorer[index];
    }

    /**
     * This method indicates the achievements that you have got the No-Deaths achievement on a specific level
     * @param index the level which you got the No-Deaths achievement
     */
    public void setNoDeaths(int index)
    {
        if(noDeaths[index])
        {
            return;
        }
        noDeaths[index] = true;
        switch(index)
        {
            case 0:
                showAchievement("FOREST VETERAN: COMPLETE THE FOREST\nWITHOUT DYING ONCE", grabIcon(AchievementIcon.AchievementIconTexture.NODEATHS1));
                break;
            case 1:
                showAchievement("DESERT VETERAN: COMPLETE THE DESERT\nWITHOUT DYING ONCE", grabIcon(AchievementIcon.AchievementIconTexture.NODEATHS2));
                break;
            case 2:
                showAchievement("VOLCANO VETERAN: COMPLETE THE VOLCANO\nWITHOUT DYING ONCE", grabIcon(AchievementIcon.AchievementIconTexture.NODEATHS3));
                break;
        }
        boolean allFound = true;
        for(int i = 0; i < noDeaths.length; ++i)
        {
            if(!noDeaths[i])
            {
                allFound = false;
            }
        }
        if(allFound)
        {
            setProGamer();
        }
    }

    /**
     * This method allows for the retrieval of the No-Deaths achievement on a specific level
     * @param index the level which you are retrieving the No-Deaths achievement obtainment value
     * @return whether you got the No-Deaths achievement on a specific level or not
     */
    public boolean getNoDeaths(int index)
    {
        return noDeaths[index];
    }

    /**
     * This method indicates that you have got the God-Gamer achievement
     */
    public void setGodGamer()
    {
        if(godGamer)
        {
            return;
        }
        godGamer = true;
        showAchievement("GOD GAMER: COMPLETE EVERY LEVEL\nWITHOUT DYING ONCE", grabIcon(AchievementIcon.AchievementIconTexture.GODGAMER));
    }

    /**
     * This method allows for the retrieval of the God-Gamer achievement
     * @return whether you have gotten the God-Gamer achievement
     */
    public boolean getGodGamer()
    {
        return godGamer;
    }

    /**
     * This method indicates that you have got the Pacifist achievement
     */
    public void setPacifist()
    {
        if(pacifist)
        {
            return;
        }
        pacifist = true;
        showAchievement("PACIFIST: COMPLETE A LEVEL\nWITHOUT SLASHING YOUR SWORD ONCE", grabIcon(AchievementIcon.AchievementIconTexture.PACIFIST));
    }

    /**
     * This method allows for the retrieval of the Pacifist achievement
     * @return whether you have gotten the Pacifist achievement
     */
    public boolean getPacifist()
    {
        return pacifist;
    }

    /**
     * This method draws the achievement pane, and tells the message and icon to also draw themselves.
     * It also grabs the next achievement to show if the alphaValue falls to 0 (fully transparent.
     * It enforces no grey-scale
     */
    public void draw()
    {
        if (currentAchievement != null)
        {
            float alphaValue = message.calculateAlphaValue();

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            // tell OpenGl to use the glProgram
            glUseProgram(glProgram);
            //getTexture(context);
            // Set vertices to the first byte
            GLManager.setVertexAttribPointer(vertices);
            GLManager.setMatrix(viewportMatrix, textureID);
            glUniform1f(uAlphaLocation, alphaValue);
            glUniform1i(uGreyScaleLocation, 0);
            // Draw the point, lines or triangle
            GLManager.drawCleanup(4);

            message.draw();
            currentAchievement.icon.drawIcon(centerX, centerY-halfH,false, alphaValue);

            if (alphaValue <= 0)
            {
                currentAchievement = null;
                displayNextAchievement();
            }
        }

    }

    /**
     * This method gets all of the achievement icons
     * @return all of the achievement icons
     */
    public ArrayList<AchievementIcon> getIcons() {
        return icons;
    }

    /**
     * This method draws all of the achievement icons back to back (for the achievementTab)
     * It is important to note this isn't bound on the bottom, so it can spill over the achievement pane
     * @param xLeft this is the x-axis's left boundary
     * @param xRight this is the x-axis's right  boundary
     * @param yTop this is the y-axis's top boundary
     * @param stepLength this is how far each icon is
     */
    public void drawAchievements(float xLeft, float xRight, float yTop, float stepLength)
    {
        float xCurr = xLeft;
        float yCurr = yTop;
        for (AchievementIcon icon : icons) {
            boolean isUnlocked = checkAchievementUnlocked(icon.getIconTexture());
            // start xLeft, yTop, then go right until not within xLeft and xRight, yTop and yBottom
            // keep on going like that until the end

            if(xCurr > xRight)
            {
                xCurr = xLeft;
                yCurr += stepLength;
            }

            icon.drawIcon(xCurr, yCurr, !isUnlocked, 1); // Draw in grayscale if not unlocked

            xCurr += stepLength;
        }
    }

    /**
     * This method gets the achievement pane's base
     * @param context is the context of the program, since some instances of texturable need context (not this one though, but we still need it for the loadTexture method)
     * @return pointer to where in R.drawable the texture file for the achievement pane's base
     */
    public int getTextureResourceId(Context context)
    {
        return R.drawable.achievement_base;
    }

    /**
     * sets the textureID for this object (for openGL)
     * @param textureId - the textureID for the texture for this object
     */
    public void setTextureID(int textureId)
    {
        textureID = textureId;
    }

    /**
     * This method converts the achievements class's relevant information to a string
     * @return a comma-separated single line of boolean values that indicate whether the achievement is gotten or not
     */
    public String toString()
    {
        String result = "";
        for(AchievementIcon icon : icons)
        {
            result += checkAchievementUnlocked(icon.getIconTexture()) + " ";
        }
        return result;

    }
}
