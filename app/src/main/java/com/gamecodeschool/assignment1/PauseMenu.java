package com.gamecodeschool.assignment1;

import android.content.Context;
import android.graphics.PointF;

/**
 * Represents the pause menu in the game, which can switch between different tabs like the main menu and achievements.
 * @author Braeden Ruff
 */
public class PauseMenu implements TabSwitcher
{
    // Tracks visibility of the pause menu.
    private boolean isVisible;

    // The currently active tab within the pause menu.
    private Tab currentTab;

    // Main tab for the pause menu.
    private MainTab mainTab;

    // Achievements tab for the pause menu.
    private AchievementsTab achievementsTab;

    // Button to close the pause menu.
    private GameButton closeButton;

    // Button to open the pause menu.
    private GameButton openButton;


    /**
     * Initializes the pause menu with tabs and buttons.
     * @param context The application context for resource access.
     * @param screenWidth The width of the screen.
     * @param screenHeight The height of the screen.
     * @param achievements The achievements to display in the achievements tab.
     */
    public PauseMenu(Context context, float screenWidth, float screenHeight, Achievements achievements) {
        isVisible = false; // The menu is not visible by default.
        mainTab = new MainTab(context, screenWidth, screenHeight, this);
        achievementsTab = new AchievementsTab(context, screenWidth, screenHeight, this, achievements);

        openButton = new GameButton(context, screenWidth, screenHeight, 14 * screenWidth / 15, screenHeight / 15,screenWidth/20, GameButton.ButtonType.OPEN);
        closeButton = new GameButton(context, screenWidth, screenHeight, 14 * screenWidth / 15, screenHeight / 15, screenWidth/20, GameButton.ButtonType.CLOSE);
    }

    /**
     * Draws the pause menu, including its tabs and buttons.
     */
    public void draw()
    {
        openButton.draw();
        if (!isVisible)
        {
            return; // Do not draw the rest of the menu if it's not visible.
        }
        // Draw the current tab and close button
        if (currentTab != null) {
            currentTab.draw();
            closeButton.draw();
        }
    }

    /**
     * Toggles the visibility of the pause menu.
     * @param gm The game manager to control game state.
     */
    public void toggleVisibility(GameManager gm)
    {
        if (!isVisible) {
            setCurrentTab(mainTab);
        }
        isVisible = !isVisible;
        gm.switchPlayingStatus();
    }

    /**
     * Sets the current active tab in the pause menu.
     * @param tab The tab to make active.
     */
    public void setCurrentTab(Tab tab) {
        this.currentTab = tab;
    }

    /**
     * Handles input for toggling the menu visibility and interacting with menu buttons.
     * @param point The point of input.
     * @param gm The game manager for state control.
     */
    public void handleInput(PointF point, GameManager gm)
    {
        // Implement input handling here
        if(openButton.isClicked(point))
        {
            toggleVisibility(gm);
        }
        else if(isVisible)
        {
            if(closeButton.isClicked(point))
            {
                toggleVisibility(gm); //this never gets called since it's drawn over top of openButton, but I'll keep it here in case I want to move the close button
            }
            currentTab.handleInput(point, gm);
        }
    }

    /**
     * Switches between the main and achievements tabs.
     */
    @Override
    public void switchTab() {
        // Logic to switch tabs
        if(currentTab instanceof MainTab)
        {
            setCurrentTab(achievementsTab);
        }
        else
        {
            setCurrentTab(mainTab);
        }
    }
}
