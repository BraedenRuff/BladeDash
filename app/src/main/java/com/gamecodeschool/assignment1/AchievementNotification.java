package com.gamecodeschool.assignment1;

/**
 * This class is a helper class so that we can show achievements one by one (used for a Queue in Achievements.java)
 * @author Braeden Ruff
 */
class AchievementNotification
{
    //The text we want to show
    String message;

    //The texture we want to show
    AchievementIcon icon;

    /**
     * This is the constructor for AchievementNotification, which contains all the data needed to show upon getting an achievement
     * @param message - text part of what we will show
     * @param icon - the icon of the achievement we will show
     */
    public AchievementNotification(String message, AchievementIcon icon) {
        this.message = message;
        this.icon = icon;
    }

}