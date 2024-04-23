package com.gamecodeschool.assignment1;

import android.graphics.PointF;

/**
 * Defines the interface for a tab in the game's pause menu.
 * Tabs are components that can be drawn on the screen and handle user input.
 * @author Braeden Ruff
 */
public interface Tab
{
    /**
     * Draws the tab and its contents on the screen.
     */
    void draw();

    /**
     * Handles user input, such as touch events, when the tab is active.
     * @param point The point of touch input.
     * @param gm The GameManager instance to interact with game logic.
     */
    void handleInput(PointF point, GameManager gm);
}