package com.gamecodeschool.assignment1;

/**
 * Interface for switching between different tabs in a UI component
 * @author Braeden Ruff
 */
public interface TabSwitcher
{
    /**
     * Switches the current tab to the next one.
     */
    void switchTab();
    void toggleVisibility(GameManager gm);
}