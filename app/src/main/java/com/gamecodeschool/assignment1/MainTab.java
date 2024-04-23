package com.gamecodeschool.assignment1;

import static android.opengl.GLES20.glUseProgram;
import static android.opengl.Matrix.orthoM;

import android.content.Context;
import android.graphics.PointF;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * This class is used to show the main pause menu tab
 * @author Braeden Ruff
 */
public class MainTab implements Tab, Texturable
{
    // openGL program identifier
    private int glProgram;

    // Buttons for game actions
    private GameButton restartGameButton;
    private GameButton restartLevelButton;
    private GameButton deleteDataButton;
    private GameButton godModeButton;

    // Buttons to switch between tabs
    private TabButton activeTabButton;
    private TabButton inactiveTabButton;

    // Buffer for vertex data
    private FloatBuffer vertices;

    // Texture identifier for the tab background
    private int textureID;

    // Projection matrix for rendering
    private final float[] viewportMatrix = new float[16];

    // Interface for switching tabs
    private TabSwitcher tabSwitcher;

    /**
     * Constructs the main tab with buttons and initializes openGL rendering settings.
     * @param context Application context for resource access.
     * @param screenWidth Width of the screen.
     * @param screenHeight Height of the screen.
     * @param tabSwitcher Interface for handling tab switches.
     */
    public MainTab(Context context, float screenWidth, float screenHeight, TabSwitcher tabSwitcher)
    {
        glProgram = GLManager.getGLTextureProgram();
        GLManager.loadTexture(context, this);

        orthoM(viewportMatrix, 0, 0, screenWidth, screenHeight, 0, 0f, 1f);

        float halfW = screenWidth / 4; // half width of tab pane
        float halfH = 2 * screenHeight / 5; // half height of tab pane
        float acrossHalfScreen = screenWidth/2;
        float upHalfScreen = screenHeight/2;

        float[] modelVertices = new float[] {
                // Position                          // Texture Coordinates
                acrossHalfScreen-halfW, upHalfScreen + halfH, 0,   0.0f, 1.0f,  // Bottom left corner
                acrossHalfScreen+halfW, upHalfScreen + halfH, 0,    1.0f, 1.0f,  // Bottom right corner
                acrossHalfScreen+halfW, upHalfScreen - halfH, 0,     1.0f, 0.0f,  // Top right corner
                acrossHalfScreen-halfW, upHalfScreen - halfH, 0,    0.0f, 0.0f   // Top left corner
        };
        this.tabSwitcher = tabSwitcher;
        // Store number of vertices
        vertices = ByteBuffer.allocateDirect(modelVertices.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertices.put(modelVertices);
        vertices.position(0);

        float buttonSize = screenWidth/20;
        float bufferSize = buttonSize /5;
        float between = buttonSize + bufferSize;
        // decide where the x and y center are + size
        restartGameButton = new GameButton(context,  screenWidth, screenHeight, acrossHalfScreen-between,  upHalfScreen,buttonSize, GameButton.ButtonType.RESTARTGAME); // Example positions
        restartLevelButton = new GameButton(context,  screenWidth, screenHeight , acrossHalfScreen+between,  upHalfScreen, buttonSize, GameButton.ButtonType.RESTARTLEVEL);
        deleteDataButton = new GameButton(context, screenWidth, screenHeight, acrossHalfScreen-between, upHalfScreen +2*between, buttonSize, GameButton.ButtonType.DELETEDATA);
        godModeButton = new GameButton(context, screenWidth, screenHeight, acrossHalfScreen+between, upHalfScreen + 2*between, buttonSize, GameButton.ButtonType.GODMODE);


        // Next block is for getting the rectangle positions on the new tab

        // Original rectangle position in image coordinates
        int originalRectTopLeftX = 6; // 6 pixels to the right is the top left corner
        int originalRectTopLeftY = 7; // 7 pixels down

        int originalImagePixelsX = 198;
        int originalImagePixelsY = 192;


        // Rectangle dimensions in image coordinates (not needed for position calculation, but useful if scaling is to be considered)
        int rectWidth = (originalImagePixelsX - originalRectTopLeftX*2)/2;
        int rectHeight = 24; // height we want in the original image

        // Pane size in screen coordinates
        float paneWidth = 2 * halfW; // full width of the pane
        float paneHeight = 2 * halfH; // full height of the pane

        // Calculate the scale factors
        float scaleX = paneWidth / originalImagePixelsX;
        float scaleY = paneHeight / originalImagePixelsY;

        // Scale the rectangle's position to pane size
        float scaledTopLeftX = originalRectTopLeftX * scaleX;
        float scaledTopLeftY = originalRectTopLeftY * scaleY;

        float scaledCenterRectX = rectWidth * scaleX/2;
        float scaledCenterRectY = rectHeight * scaleY/2;

        // Convert the scaled rectangle's position to screen coordinates
        float screenRectX = acrossHalfScreen - halfW + scaledTopLeftX + scaledCenterRectX;
        float screenRectY = upHalfScreen - halfH + scaledTopLeftY + scaledCenterRectY; // Subtract because screen coordinates go down as Y increases
        float sizeX = scaleX * rectWidth/2;
        float sizeY = scaleY * rectHeight/2;
        activeTabButton = new TabButton(context, screenWidth, screenHeight, screenRectX, screenRectY, sizeX, sizeY, "CONTROLS", TabButton.TabType.active);
        inactiveTabButton = new TabButton(context, screenWidth, screenHeight, screenRectX + scaleX * rectWidth, screenRectY, sizeX, sizeY, "ACHIEVEMENTS", TabButton.TabType.inactive);
    }

    /**
     * Draws the tab and its buttons.
     */
    @Override
    public void draw()
    {
        glUseProgram(glProgram);

        GLManager.setVertexAttribPointer(vertices);
        GLManager.setMatrix(viewportMatrix, textureID);
        GLManager.drawCleanup(4);
        // Draw restart game and level buttons
        restartGameButton.draw();
        restartLevelButton.draw();
        deleteDataButton.draw();
        godModeButton.draw();
        activeTabButton.draw();
        inactiveTabButton.draw();
    }

    /**
     * Returns the texture resource ID for the tab background.
     * @param context The application context.
     * @return The resource ID for the background texture.
     */
    @Override
    public int getTextureResourceId(Context context) {
        return R.drawable.main_tab;
    }

    /**
     * Sets the texture ID for the tab background.
     * @param textureId The OpenGL texture ID.
     */
    @Override
    public void setTextureID(int textureId) {
        textureID = textureId;
    }

    /**
     * Handles user input, performing actions based on which button was clicked.
     * @param point The point of input.
     * @param gm The game manager to execute actions.
     */
    @Override
    public void handleInput(PointF point, GameManager gm)
    {
        if(restartGameButton.isClicked(point))
        {
            gm.level = 0;
            gm.player.instaKill(gm);
            gm.player.setDeathStartTime(0);
            gm.setDied(false);
            gm.switchPlayingStatus();
            gm.message.generateText("");
        }
        else if(restartLevelButton.isClicked(point))
        {
            gm.player.instaKill(gm);
            gm.player.setDeathStartTime(0);
            gm.setDied(false);
            gm.switchPlayingStatus();
            gm.message.generateText("");
        }
        else if(deleteDataButton.isClicked(point))
        {
            gm.level = 0;
            gm.player.instaKill(gm);
            gm.player.setDeathStartTime(0);
            gm.setDied(false);
            gm.switchPlayingStatus();
            gm.message.generateText("");
            gm.setDelete();
        }
        else if(godModeButton.isClicked(point))
        {
            gm.player.toggleGodMode();
            gm.toggleGodMode();
            tabSwitcher.toggleVisibility(gm);
        }
        else if(inactiveTabButton.isClicked(point))
        {
            tabSwitcher.switchTab();
        }
    }
}
