package com.gamecodeschool.assignment1;

/**
 * Interface definition for a callback to be invoked when a player's name is entered.
 * @author Braeden Ruff
 */
public interface PlayerNameCallback
{
    /**
     * Called when a player's name has been entered.
     * @param name The entered name of the player.
     */
    void onNameEntered(String name);
}
