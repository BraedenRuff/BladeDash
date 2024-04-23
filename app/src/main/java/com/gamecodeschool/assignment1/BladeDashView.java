package com.gamecodeschool.assignment1;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * This class is responsible for holding and creating the three big components in our game: the input controller, game manager, and renderer.
 * It also sets up openGL and captures touchEvents
 * @author Braeden Ruff
 */
public class BladeDashView extends GLSurfaceView
{
    //The game manager holds all of our game objects and key information about our game
    private GameManager gm;

    //The input controller handles all of our input
    private InputController ic;

    //The BladeDashRenderer is our custom renderer that updates and draws our game
    private BladeDashRenderer bladeDashRenderer;

    public BladeDashView(Context context, int screenX, int screenY)
    {
        super(context);

        if (context instanceof PlayerNamePrompter && context instanceof UiThreadExecutor)
        {
            gm = new GameManager(screenX, screenY, (PlayerNamePrompter) context);
        }
        else
        {
            throw new RuntimeException(context.toString() + " must implement PlayerNamePrompter and UiThreadExecutor");
        }
        ic = new InputController(screenX, screenY);

        // Request an OpenGL ES 3.0 compatible context.
        setEGLContextClientVersion(3);
        bladeDashRenderer = new BladeDashRenderer(context, gm, ic);
        setRenderer(bladeDashRenderer);
    }

    /**
     * This event is triggered when the user tabs off our game
     */
    @Override
    public void onPause()
    {
        super.onPause();
    }

    /**
     * This event is triggered when the user tabs back onto our game
     */
    @Override
    public void onResume()
    {
        super.onResume();
    }

    /**
     * This event is triggered when the user touches the screen
     * @param motionEvent The motion event.
     * @return true, always
     */
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent)
    {
        ic.handleInput(motionEvent, gm);
        return true;
    }

}
