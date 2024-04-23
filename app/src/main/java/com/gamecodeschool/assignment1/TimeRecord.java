package com.gamecodeschool.assignment1;

/**
 * Represents a record of a player's time. This class is used to manage time records,
 * including the player's name and the time in milliseconds. It supports comparison based on time
 * to facilitate sorting and leaderboard functionality.
 * @author Braeden Ruff
 */
public class TimeRecord implements Comparable<TimeRecord>
{
    //The time in milliseconds the game was completed in
    private long timeMillis;

    //The player's input name who got this time
    private String playerName;

    /**
     * Constructs a TimeRecord with the specified time in milliseconds and player name.
     * @param timeMillis The time in milliseconds associated with the record.
     * @param playerName The name of the player associated with the record.
     */
    public TimeRecord(long timeMillis, String playerName)
    {
        this.timeMillis = timeMillis;
        this.playerName = playerName;
    }

    /**
     * Gets the time in milliseconds associated with the record
     * @return The time in milliseconds.
     */
    public long getTimeMillis() {
        return timeMillis;
    }

    /**
     * Gets the name of the player associated with the record
     * @return The player's name.
     */
    public String getPlayerName() { return playerName; }

    /**
     * Compares this TimeRecord with another based on time in milliseconds for sorting purposes
     * @param other The other TimeRecord to compare to.
     * @return A negative integer, zero, or a positive integer as this record is less than, equal to,
     * or greater than the specified record.
     */
    @Override
    public int compareTo(TimeRecord other) {
        return Long.compare(this.timeMillis, other.timeMillis);
    }

    /**
     * Returns a string representation of the TimeRecord, useful for file writing/reading.
     * @return A string representation of the TimeRecord.
     */
    @Override
    public String toString() {
        return playerName + ": " + timeMillis;
    }

    /**
     * Parses a TimeRecord from a string representation. This is useful for loading records from a file.
     * @param recordString The string representation of a TimeRecord.
     * @return A TimeRecord object.
     * @throws IllegalArgumentException if the record string is in an incorrect format.
     */
    public static TimeRecord fromString(String recordString) {
        // Split the string by ": " to separate playerName and timeMillis
        String[] parts = recordString.split(": ");
        if (parts.length < 2) {
            throw new IllegalArgumentException("The record string is in an incorrect format.");
        }
        String playerName = parts[0];
        long timeMillis;
        try {
            timeMillis = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The timeMillis part of the record string is not a valid long.");
        }
        // Assuming TimeRecord has a constructor that accepts both playerName and timeMillis
        return new TimeRecord(timeMillis, playerName);
    }


}