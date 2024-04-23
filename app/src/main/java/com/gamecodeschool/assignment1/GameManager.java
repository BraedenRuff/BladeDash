package com.gamecodeschool.assignment1;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

import kotlin.NotImplementedError;

/**
 * This class is used to manage our game objects and has some useful information as well
 * @author Braeden Ruff
 */
public class GameManager
{
    //Contains a string that is used to load the level
    private LevelData levelData;

    //The width of the map (from levelData)
    int mapWidth;

    //The height of the map (from levelData)
    int mapHeight;

    //Whether we are playing the game or it's paused
    private boolean playing;

    //The player object
    Player player;

    //The saved player object (useful for when we want to reload)
    Player savedPlayer;

    //Our levels ground tiles (sparse array)
    Ground[][] groundTiles;

    //Our saved ground tiles (useful for when we want to reload)
    Ground[][] savedGroundTiles;

    //Our list of enemies
    ArrayList<Enemy> enemies;

    //Our list of enemies (useful for when we want to reload)
    ArrayList<Enemy> savedEnemies;

    //Our list of coins
    ArrayList<Coin> coins;

    //Our saved list of coins (useful for when we want to reload)
    ArrayList<Coin> savedCoins;

    //Our list of breakable walls
    ArrayList<Breakable> breakables;

    //Our saved list of breakable walls (useful for when we want to reload)
    ArrayList<Breakable> savedBreakables;

    //Our teleport/end point
    Teleport teleport;

    //Our saved teleport/end point
    Teleport savedTeleport;

    //Our movement joystick
    Joystick movementJoystick;

    //Our message that is used to display if we died or if we win + leaderboard
    Message message;

    //Our saved message that is used to display if we died or if we win + leaderboard
    Message savedMessage;

    //Our achievements that we earned
    Achievements achievements;

    //Our saved achievements that we earned
    Achievements savedAchievements;

    //This tells you if god mode is enabled
    Message godModeMessage;

    //This is the saved message that tells you if god mode is enabled
    Message savedGodModeMessage;

    //The width of the screen
    int screenWidth;

    //The height of the screen
    int screenHeight;

    // How many metres of our virtual world
    // we will show on screen at any time.
    float metresToShowX;
    float metresToShowY;

    //Which level we are on
    int level;

    //How many rows of tiles do we have on this map
    private int mapRows;

    //How many columns of tiles do we have on this map
    private int mapColumns;

    //helps tracks how long the user is playing
    private long startTime;

    //Helps with displaying the request to enter your name
    private PlayerNamePrompter prompter;

    //how many pixels there are per meter of screen (see metersToShow for how big or small you want it)
    private static int pixelsPerMeter = 14;

    //used to make sure we don't clip to far through a tile, since we can only move through half a tile
    private static float largestMovement = pixelsPerMeter/2;

    //used for the no deaths type of achievements
    private boolean died;

    //used for deleting the achievements.txt and the top_times.txt
    private boolean delete;

    //used for returning the player to where he last was
    private boolean reload;
    private boolean godMode;

    /**
     * this method gets how big one meter is in pixels
     * @return how big one meter is in pixels
     */
    public static int getPixelsPerMeter()
    {
        return pixelsPerMeter;
    }

    /**
     * this method gets the the max movement in one frame
     * @return the max movement in one frame
     */
    public static float getLargestMovement()
    {
        return largestMovement;
    }

    /**
     * this is the constructor for a game manager, which is a class that holds all of the game objects and useful information
     * @param x is the screenWidth
     * @param y is the screenHeight
     * @param prompter helps show the "enter player name: " prompt
     */
    public GameManager(int x, int y, PlayerNamePrompter prompter)
    {
        died = false;
        screenWidth = x;
        screenHeight = y;
        float aspectRatio = (float) screenWidth / (float) screenHeight;
        metresToShowX = 400; // just choosing random number
        metresToShowY = 400 / aspectRatio; // how much y depends on your phone resolution
        level = 0;
        playing = false;
        this.prompter = prompter;
        delete = false;
        godMode = false;
    }

    /**
     * this method gets the number of rows of tiles in the current map
     * @return the number of rows of tiles in the current map
     */
    public int getMapRows()
    {
        return mapRows;
    }

    /**
     * this method gets the number of columns of tiles in the current map
     * @return the number of columns of tiles in the current map
     */
    public int getMapColumns()
    {
        return mapColumns;
    }

    /**
     * this method saves the achievements
     * @param context - context of the program, used to open and save the achievements file
     */
    public void setSavedAchievements(Context context)
    {
        try (FileOutputStream fos = context.openFileOutput("achievements.txt", Context.MODE_PRIVATE);
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos)))
        {
            writer.println(achievements.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tells the gamemanager if we should reload the previous game positions or start from the beginning of the level
     * @param b true if we start from previous game positions, false if from the beginning of the level
     */
    public void setReload(boolean b)
    {
        reload = b;
    }

    /**
     * This method saves the gameObjects that may change as the game progresses so when we reload after the screen is closed the game state is restored
     */
    public void saveGameObjectsState()
    {
        if(reload) //helps the objects stay in the last place
        {
            savedGroundTiles = groundTiles;
            savedCoins = coins;
            savedBreakables = breakables;
            savedEnemies = enemies;
            savedPlayer = player;
            savedTeleport = teleport;
            savedMessage = message;
            savedAchievements = achievements;
            savedGodModeMessage = godModeMessage;
        }
        else
        {
            savedGroundTiles = null;
            savedCoins = null;
            savedBreakables = null;
            savedEnemies = null;
            savedPlayer = null;
            savedTeleport = null;
            savedMessage = null;
            savedAchievements = null;
            savedGodModeMessage = null;
        }
    }

    /**
     * this method loads the saved achievements
     * @param context - context of the program, used to open and save the achivements file
     */
    public void loadSavedAchievements(Context context)
    {
        try (FileInputStream fis = context.openFileInput("achievements.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            if ((line = reader.readLine()) != null) {
                achievements.setBooleans(line);
            }
            else
            {
                Achievements a = new Achievements(context, screenWidth, screenHeight);
                achievements.setBooleans(a.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method reloads textures after the phone has been turned off or the application was exited
     * @param context is the context of the program
     */
    public void reloadTextures(Context context, Texturable texturable)
    {
        GLManager.loadTexture(context, texturable);
    }

    /**
     * this method loads the map data
     * @param context - context of the program, used by the objects, specifically those that need animated, to determine sprite sheet width and height
     */
    private void loadMapData(Context context)
    {
        playing = false;

        mapRows = levelData.tiles.size();
        mapColumns =  levelData.tiles.get(0).length(); // all will be the same length or issue will occur
        if(reload)
        {
            groundTiles = savedGroundTiles;
            enemies = savedEnemies;
            coins = savedCoins;
            breakables = savedBreakables;
            player = savedPlayer;
            teleport = savedTeleport;
            message = savedMessage;
            godModeMessage = savedGodModeMessage;

            //can't directly change reference to achievements since achievementsTab uses this and they need to point to the same object
            achievements.setBooleans(savedAchievements.toString());
            //openGL needs to reload all the textures, since genTexture is non-deterministic and clears when you close your phone
            for (int j = 0; j < mapColumns; j++)
            {
                for (int i = 0; i < mapRows; i++)
                {
                    if(groundTiles[i][j] != null)
                    {
                        reloadTextures(context, groundTiles[i][j]);
                    }
                }
            }
            for(Enemy enemy : enemies)
            {
                reloadTextures(context, enemy);
            }
            for(Coin coin : coins)
            {
                reloadTextures(context, coin);
            }
            for(Breakable breakable : breakables)
            {
                reloadTextures(context, breakable);
            }
            reloadTextures(context, player);
            reloadTextures(context, teleport);
            reloadTextures(context, message);
            reloadTextures(context, achievements);
            ArrayList<AchievementIcon> icons = achievements.getIcons();
            for(AchievementIcon icon : icons)
            {
                reloadTextures(context, icon);
            }
            reloadTextures(context, achievements.message);
            reloadTextures(context, godModeMessage);

            reload = true;
            playing = true;
            return;
        }

        if(delete)
        {
            clearSaveAchievements(context);
            clearTopTimes(context);
            delete = false;
        }
        godModeMessage = new Message(context, screenWidth, screenHeight);
        godModeMessage.setPersistent(true);
        generateGodModeText();
        char c;

        //enforce this
        for(int i = 0; i < mapRows; ++i)
        {
            if(levelData.tiles.get(i).length() != mapColumns)
            {
                Log.e("Not Rect", "The levelData must be a rectangle");
                throw new NotImplementedError();
            }
        }

        groundTiles = new Ground[mapRows][mapColumns];
        enemies = new ArrayList<Enemy>();
        coins = new ArrayList<Coin>();
        breakables = new ArrayList<Breakable>();
        message = new Message(context, screenWidth, screenHeight);
        if(achievements == null)
        {
            achievements = new Achievements(context, screenWidth, screenHeight);
        }
        loadSavedAchievements(context);

        message.generateText("");
        mapWidth = 0;
        mapHeight = 0; // reset these

        for (int j = 0; j < mapColumns; j++) //i want to populate our array list from left to right, for logic in BladeDashRenderer's handlePlayerEnemyCollisions()
        {
            int x = j * pixelsPerMeter;
            mapWidth = Math.max(mapWidth, x);
            for (int i = 0; i < mapRows; i++)
            {
                int y = i * pixelsPerMeter;
                mapHeight = Math.max(mapHeight, y);

                c = levelData.tiles.get(i).charAt(j);
                if (c != '.') {// Don't want to load the empty spaces
                    switch (c)
                    {
                        // Ground
                        case '1':
                            // Add a tile to the tiles
                            groundTiles[i][j] = new Ground(context, x, y,Ground.GroundType.GRASS);
                            break;

                        case '2':
                            // Add a ground block to tiles
                            groundTiles[i][j] = new Ground(context, x, y, Ground.GroundType.DIRT);
                            break;

                        case '3':
                            // Add a Sandstone block to tiles
                            groundTiles[i][j] = new Ground(context, x, y, Ground.GroundType.SANDSTONE);
                            break;

                        case '4':
                            // Add a Sandstone block to tiles
                            groundTiles[i][j] = new Ground(context, x, y, Ground.GroundType.MAGMASTONE);
                            break;

                        case 'w':
                            // Add a breakable wall to tiles, depending on the parent block
                            groundTiles[i][j] = new Breakable(context, x, y, groundTiles[i - 1][j]);
                            breakables.add((Breakable) (groundTiles[i][j]));
                            break;
                        case 'd':
                            // Add an instant death block tiles
                            groundTiles[i][j] = new Ground(context, x, y, Ground.GroundType.DEATH);
                            break;

                        // Enemies
                        case 's':
                            // Add a slime to the enemies

                            enemies.add(new Slime(context, x, y));

                            break;

                        case 'g':
                            // Add a goblin to the enemies
                            enemies.add(new Goblin(context, x, y));
                            break;
                        case 'm':
                            enemies.add(new MonsterSpawner(context, x, y, message));
                            break;

                        // Collectibles
                        case 'c':
                            // Add a coin to the coins if not already collected
                            coins.add(new Coin(context, x, y));
                            break;

                        // Start and end
                        case 'p':// player start
                            //Make the player
                            // by teleporters pixelsPerMeter * 276, pixelsPerMeter * 15

                            player = new Player(context, x, y, godMode);
                            break;
                        case 'f': //player end
                            teleport = new Teleport(context, x, y);

                    }
                }
            }
        }

        reload = true;
        playing = true;
        saveGameObjectsState();
    }

    /**
     * This method clears achievments obtained by the player
     * @param context - the the context of the program, used to open achievements.txt and clear it
     */
    private void clearSaveAchievements(Context context)
    {
        try (FileOutputStream fos = context.openFileOutput("achievements.txt", Context.MODE_PRIVATE)) {
            // Opening in MODE_PRIVATE without writing anything will clear the file.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method handles the logic after a level is completed
     * @param context is the context of the program. Needed resave achievements if acquired any
     */
    private void handleAchievementsEndOfLevel(Context context)
    {
        if(!player.getMissedSlash())
        {
            achievements.setGodGamer();
            setSavedAchievements(context);
        }
        if(!player.getSlashedOnce())
        {
            achievements.setPacifist();
            setSavedAchievements(context);
        }
        if(!died)
        {
            achievements.setNoDeaths(level);
            setSavedAchievements(context);
        }
    }

    /**
     * This method goes to the next level and handles some logic regarding switching levels
     * @param context - the context of the program, used for resaving achievements if you got any
     */
    public void nextLevel(Context context)
    {
        playing = false;
        handleAchievementsEndOfLevel(context);
        died = false;
        ++level;
        reload = false;
        saveGameObjectsState();
        switchLevel(context);
    }

    /**
     * This method switches the level
     * @param context - the context of the program, needed to load resources of the new level
     */
    public void switchLevel(Context context)
    {
        switch (level)
        {
            case 0:
                levelData = new LevelForest();
                startTime = System.currentTimeMillis(); //start time here, so we know how long it took to beat the game
                break;
            case 1:
                levelData = new LevelDesert();
                break;
            case 2:
                levelData = new LevelMagma();
                break;
            default:
                throw new NotImplementedError();
        }
        loadMapData(context);
    }

    /**
     * This method saves the top times the player has achieved
     * @param topTimes - the top three times achieved by the player
     * @param context - is the context of the program, needed to save top_times.txt
     */
    public void saveTopTimes(ArrayList<TimeRecord> topTimes, Context context) {
        try (FileOutputStream fos = context.openFileOutput("top_times.txt", Context.MODE_PRIVATE);
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos))) {
            for (TimeRecord record : topTimes) {
                writer.println(record.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method clears the player's top times
     * @param context - is the context of the program. Needed to open and clear top_times.txt
     */
    public void clearTopTimes(Context context)
    {
        try (FileOutputStream fos = context.openFileOutput("top_times.txt", Context.MODE_PRIVATE)) {
            // Opening in MODE_PRIVATE without writing anything will clear the file.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method loads the players top times
     * @param context is the context of the program. Needed to open top_times.txt
     * @return maximum of 3 top time records achieved by the player in an ArrayList
     */
    public ArrayList<TimeRecord> loadTopTimes(Context context)
    {
        ArrayList<TimeRecord> topTimes = new ArrayList<>();
        try (FileInputStream fis = context.openFileInput("top_times.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            while ((line = reader.readLine()) != null) {
                topTimes.add(TimeRecord.fromString(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(topTimes);
        return topTimes;
    }

    /**
     * This method handles the logic after the game is completed
     * 1. prompt player for a name
     * 2. display leaderboard
     * 3. save top time if applicable
     * 4. do achievement stuff
     * @param context
     */
    public void end(Context context)
    {
        //clearTopTimes(context);
        switchPlayingStatus();
        message.setPersistent(true);
        handleAchievementsEndOfLevel(context);
        prompter.promptForPlayerName(new PlayerNameCallback()
        {
            @Override
            public void onNameEntered(String name) {
                // Now you have the name, proceed with the logic that depends on the name
                ArrayList<TimeRecord> topTimes = loadTopTimes(context);

                TimeRecord currentRecord = new TimeRecord(System.currentTimeMillis() - startTime, name);
                topTimes.add(currentRecord);
                Collections.sort(topTimes);

                String times = "";
                int longestNumChars = 0;
                for(TimeRecord time : topTimes)
                {
                    String timeString = formatTime((time.getTimeMillis()));
                    times += time.getPlayerName() + ": " + timeString + "\n";
                    int tempLongest = 0;
                    tempLongest += time.getPlayerName().length() + 2 + timeString.length();
                    if(tempLongest > longestNumChars)
                    {
                        longestNumChars = tempLongest;
                    }
                }
                if (topTimes.size() > 3) {
                    topTimes.subList(3, topTimes.size()).clear(); // Keep only top three
                }

                saveTopTimes(topTimes, context);
                float char_width = screenWidth / 20;
                float x = screenWidth/2 - char_width/2 * longestNumChars;
                while(x < 0) // keep it all on screen in case they have a long name
                {
                    char_width *= 0.9;
                    x = screenWidth/2 - char_width/2 * longestNumChars;
                }
                float y = screenHeight/2;
                message.generateText(times, char_width, x, y);
            }
        });
    }

    /**
     * This method formats the time in the format minutes:seconds:milliseconds, where seconds and milliseconds have leading 0s
     * @param milliseconds - the duration we want to format (in milliseconds obviously)
     * @return the formated string of the milliseconds
     */
    public String formatTime(long milliseconds)
    {
        // Convert the total milliseconds to total seconds
        long totalSeconds = milliseconds / 1000;
        // Calculate total minutes and remaining seconds
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        long leftoverMilliseconds = milliseconds % 1000;
        // Format the string to display minutes and seconds and milliseconds without leading zeros for minutes
        // Note: %d automatically does not prepend zeros for numbers. Use %02d for seconds to ensure two digits.
        return String.format("%dm:%02ds:%03dms", minutes, seconds, leftoverMilliseconds);
    }

    /**
     * This method switches the playing status, which will pause the game or resume the game
     */
    public void switchPlayingStatus()
    {
        playing = !playing;
    }

    /**
     * This method checks if the game is paused or not
     * @return whether the game is paused or not
     */
    public boolean isPlaying()
    {
        return playing;
    }

    /**
     * This method sets the died boolean in GameManager. Used for NoDeaths achievements. Also use this method when the player wants to reset the level
     * I kill the player and then setDied to false for a quick reset since it was the easiest to implement
     * @param b whether to set true or false
     */
    public void setDied(boolean b)
    {
        died = b;
    }

    /**
     * This method sets up the program to delete the top times and achievements the next time you load the level
     */
    public void setDelete()
    {
        delete = true;
        reload = false;
    }

    /**
     * Generates the correct god mode text based on godMode
     */
    private void generateGodModeText()
    {
        if(godMode)
        {
            godModeMessage.generateText("GOD MODE ENABLED",screenHeight/30, screenWidth/30 , screenHeight/30 * 2);
        }
        else
        {
            godModeMessage.generateText("");
        }
    }

    /**
     * This method toggle god mode
     */
    public void toggleGodMode()
    {
        godMode = !godMode;
        generateGodModeText();

    }
}
