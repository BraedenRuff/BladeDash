package com.gamecodeschool.assignment1;

/**
 * Interface for executing actions on the UI thread.
 * This is useful for tasks that need to be run on the UI thread, such as updating UI components
 * from a background thread. For our purposes, we need it to show an AlertDialog (needs main thread for this functionality)
 * @author Braeden Ruff
 */
public interface UiThreadExecutor
{
    /**
     * Executes the given action on the UI thread.
     * @param action The action to be executed.
     */
    void execute(Runnable action);
}
