package com.gamecodeschool.assignment1;
import java.util.ArrayList;

/**
 * This class is just a base class for other levels
 * @author Braeden Ruff
 */
public abstract class LevelData
{
    //an ArrayList of symbols representing what will be placed there
    //each String represents one row
    ArrayList<String> tiles;

    // Tile types
    // . = no tile
    // 1 = Grass Tile
    // 2 = Dirt Tile
    // 3 = Desert Tile
    // 4 = Magmabrick Tile
    // w = Breakable Wall
    // d = Death Tile

    // enemies
    // s = slime
    // g = goblin
    // m = monster spawner

    // collectibles
    // c = coin

    // Inactive objects
    // p = player spawn location
    // f = end location

}
