package com.gamecodeschool.assignment1;

/**
 * Interface definition for a prompter to tell the user to enter a player's name.
 * @author Braeden Ruff
 */
public interface PlayerNamePrompter
{
    /**
     * Prompts the user to enter a player's name. When the name is entered,
     * the provided callback is invoked with the entered name.
     * @param callback The callback to invoke with the entered player name.
     */
    void promptForPlayerName(PlayerNameCallback callback);
}
