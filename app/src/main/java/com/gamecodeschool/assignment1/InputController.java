package com.gamecodeschool.assignment1;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;

import java.util.ArrayList;

/**
 * Manages user input for the game, including touch events for movement, jumping, dashing, and slashing actions.
 * @author Braeden Ruff
 */
public class InputController
{
    // Represents the joystick for player movement.
    private Pair<PointF, PointF> movementJoystick;

    // Center point of the movement joystick.
    private PointF outerCenter;

    // Radius of the joystick's outer circle.
    private float outerRadius;

    // ID of the pointer currently controlling movement, -1 if none.
    int movingPointer = -1;

    // Location of the jump button.
    private PointF jump;

    // Location of the dash button.
    private PointF dash;

    // Location of the slash button.
    private PointF slash;

    // Width of the screen, used for UI element positioning.
    private int screenWidth;

    // Height of the screen, used for UI element positioning.
    private int screenHeight;

    // Represents the pause menu.
    private PauseMenu pauseMenu;

    /**
     * Initializes the controller with screen dimensions and positions UI elements.
     * @param screenWidth The width of the game screen.
     * @param screenHeight The height of the game screen.
     */
    public InputController(int screenWidth, int screenHeight)
    {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        int buttonWidth = screenWidth / 8;
        int buttonHeight = screenHeight / 5;
        int buttonPadding = Math.max(screenWidth / 80, screenHeight / 70);
        outerRadius = screenWidth/20;
        outerCenter = new PointF(buttonPadding + outerRadius + buttonWidth/2, screenHeight - buttonHeight/2 - buttonPadding - outerRadius);
        movementJoystick = new Pair(outerCenter, new PointF(outerRadius, outerRadius / 2));
        jump = new PointF(screenWidth - buttonPadding - outerRadius, screenHeight - buttonPadding - outerRadius);
        dash = new PointF(screenWidth - 2 * buttonPadding - 3 * outerRadius, screenHeight - buttonPadding - outerRadius);
        slash = new PointF(screenWidth - buttonPadding - outerRadius, screenHeight - 2 * buttonPadding - 3 * outerRadius);
    }

    /**
     * Returns the radius of the joystick's outer circle.
     * @return The outer radius.
     */
    public float getOuterRadius()
    {
        return outerRadius;
    }

    /**
     * Creates and sets up the pause menu.
     * @param context The application context.
     * @param screenWidth The width of the game screen.
     * @param screenHeight The height of the game screen.
     * @param achievements The achievements system to display in the pause menu.
     */
    public void makePauseMenu(Context context, float screenWidth, float screenHeight, Achievements achievements)
    {
        pauseMenu = new PauseMenu(context, screenWidth, screenHeight, achievements);
    }

    /**
     * Returns a list of PointF objects representing the positions of on-screen buttons.
     * @return A list of button positions.
     */
    public ArrayList getButtons(){
        //create an array of buttons for the draw method
        ArrayList<PointF> currentButtonList = new ArrayList<>();
        currentButtonList.add(jump);
        currentButtonList.add(dash);
        currentButtonList.add(slash);
        return  currentButtonList;
    }

    /**
     * Returns a list of joystick positions and sizes.
     * @return A list of joystick attributes.
     */
    public ArrayList getJoystick()
    {
        ArrayList<Pair<PointF, PointF>> listOfJoysticks = new ArrayList<>(); // one for now
        listOfJoysticks.add(movementJoystick);
        return listOfJoysticks;
    }

    /**
     * Returns the pause menu.
     * @return The pauseMenu object.
     */
    public PauseMenu getPauseMenu()
    {
        return pauseMenu;
    }

    /**
     * Handles touch input, interpreting it as player movement, actions, or menu interactions.
     * @param motionEvent The MotionEvent object containing touch details.
     * @param gm The GameManager handling game logic.
     */
    public void handleInput(MotionEvent motionEvent, GameManager gm) //add sound here later
    {
        if(gm.player == null || pauseMenu == null)
        {
            return;
        }
        int action = motionEvent.getActionMasked();
        int actionIndex = motionEvent.getActionIndex(); // Get index for down/up events.
        int actionId = motionEvent.getPointerId(actionIndex); // Get ID for consistent tracking.

        int pointerCount = motionEvent.getPointerCount();

        for (int i = 0; i < pointerCount; i++)
        {

            int pointerId = motionEvent.getPointerId(i);
            int x = (int) motionEvent.getX(i);
            int y = (int) motionEvent.getY(i);
            PointF point = new PointF(x, y);
            float distanceToJoystick = distanceToCircle(movementJoystick.first, point);
            switch (action)
            {

                case MotionEvent.ACTION_DOWN: case MotionEvent.ACTION_POINTER_DOWN:
                    pauseMenu.handleInput(point, gm);
                    if(gm.player.getControllable())
                    {
                        if (distanceToJoystick < gm.screenWidth / 16) //clicked down on somewhere on the outer radius
                        {
                            //find direction
                            double angle = getScreenAngle(outerCenter, point);
                            gm.movementJoystick.setInnerCenter(point);
                            gm.player.setFacingAngle(angle);
                            gm.player.setMoving(true);
                            movingPointer = pointerId;
                        }
                        else if (distanceToCircle(jump, point) < outerRadius)
                        {
                            if (!gm.player.getIsAirborne())
                            {
                                gm.player.jump();
                            }
                            if (gm.player.getWallSliding())
                            {
                                gm.player.wallJump();
                            }
                        }
                        else if (distanceToCircle(dash, point) < outerRadius)
                        {
                            gm.player.dash();
                        }
                        else if (distanceToCircle(slash, point) < outerRadius)
                        {
                            gm.player.slash(gm);
                        }
                    }
                    break;


                case MotionEvent.ACTION_UP: case MotionEvent.ACTION_POINTER_UP:
                    if(movingPointer == actionId)
                    {
                        movingPointer = -1;
                        gm.movementJoystick.setInnerCenter(outerCenter);
                        gm.player.setMoving(false);
                    }

                    break;

                case MotionEvent.ACTION_MOVE:
                    if (gm.player.getMoving() && movingPointer == pointerId) //clicked down on somewhere on the outer radius
                    {
                        //find direction
                        double angle = getScreenAngle(outerCenter, point);
                        if(distanceToJoystick < movementJoystick.second.x)
                        {
                            gm.movementJoystick.setInnerCenter(point);
                            if(gm.player.getControllable()) {
                                gm.player.setFacingAngle(angle);
                                gm.player.setMoving(true);
                            }
                        }
                        else
                        {

                            //get outside edge of circle
                            float new_x = outerCenter.x + outerRadius * (float) Math.cos(angle); // x
                            float new_y= outerCenter.y + outerRadius * (float) Math.sin(angle); // y
                            gm.movementJoystick.setInnerCenter(new PointF(new_x, new_y));
                            if(gm.player.getControllable())
                            {
                                gm.player.setFacingAngle(angle);
                                gm.player.setMoving(true);
                            }
                        }
                    }
            }
        }
    }

    /**
     * Calculates the angle between two screen points.
     * @param point1 The first point.
     * @param point2 The second point.
     * @return The angle in radians.
     */
    private double getScreenAngle(PointF point1, PointF point2)
    {
        float ndcX1 = (2.0f * point1.x) / screenWidth - 1;
        float ndcY1 = 1-(2.0f * point1.y) / screenHeight; // Flipping the y-coordinate;


        float ndcX2 = (2.0f * point2.x) / screenWidth - 1;
        float ndcY2 = 1-(2.0f * point2.y) / screenHeight; // Flipping the y-coordinate;

        return getAngle(new PointF(ndcX1, ndcY1), new PointF(ndcX2, ndcY2));
    }

    /**
     * Calculates the distance from a point to the center of a circle.
     * @param center The center of the circle.
     * @param point The point outside the circle.
     * @return The distance between the center and the point.
     */
    public static float distanceToCircle(PointF center, PointF point)
    {
        // Calculate the difference in x and y coordinates
        float dx = center.x - point.x;
        float dy = center.y - point.y;

        // Calculate the Euclidean distance
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        return distance;
    }

    /**
     * Calculates the angle between two points.
     * @param pointFrom Starting point.
     * @param pointTo Ending point.
     * @return The angle in radians.
     */
    public static double getAngle(PointF pointFrom, PointF pointTo)
    {
        return Math.atan2((-pointTo.y + pointFrom.y),(pointTo.x - pointFrom.x));
    }

    /**
     * Determines if the facing direction is right based on the angle.
     * Used to draw the animation in reverse if false
     * @param angle The angle in radians.
     * @return True if facing right, false otherwise.
     */
    public static boolean getFacingRight(double angle)
    {
        return angle >= -Math.PI/2 && angle <= Math.PI/2;
    }
}