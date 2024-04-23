package com.gamecodeschool.assignment1;

import android.content.Context;

/**
 * Interface for game objects that can be textured. This interface requires implementing classes to
 * provide functionality for getting a texture resource ID and setting a texture ID, facilitating
 * the management of textures in a consistent manner across different game objects.
 * @author Braeden Ruff
 */
public interface Texturable
{
    /**
     * Retrieves the resource ID of the texture associated with the implementing game object.
     * This method allows for dynamic texture assignment based on game state or object properties.
     * @param context The application context, used for accessing resources.
     * @return The resource ID of the texture.
     */
    int getTextureResourceId(Context context);

    /**
     * Assigns a texture ID to the implementing game object. This method is used in
     * conjunction with openGL.
     * @param textureId The ID of the texture to be assigned.
     */
    void setTextureID(int textureId);
}
