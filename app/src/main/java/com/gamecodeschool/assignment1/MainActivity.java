package com.gamecodeschool.assignment1;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
/**
 * The main activity for the game that initializes and manages the openGL view and player name prompt.
 * @author Braeden Ruff
 */
public class MainActivity extends AppCompatActivity implements PlayerNamePrompter, UiThreadExecutor
{
    // The openGL view for rendering the game.
    private GLSurfaceView bladeDashView;

    /**
     * This method sets up the activity and gets the resolution of the screen, makes a bladeDashView, and sets it as the contentView
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the resolution (screen width and height)
        Display display = getWindowManager().getDefaultDisplay();
        Point resolution = new Point();
        display.getSize(resolution);

        bladeDashView = new BladeDashView(this, resolution.x, resolution.y);

        setContentView(bladeDashView);
    }

    /**
     * This method handles pausing
     */
    @Override
    protected void onPause()
    {
        super.onPause();

        bladeDashView.onPause();

    }

    /**
     * This method handles resuming
     */
    @Override
    protected void onResume()
    {
        super.onResume();

        bladeDashView.onResume();

    }

    /**
     * Prompts the user to enter their name through an AlertDialog.
     * @param callback Interface callback for when the name is entered.
     */
    @Override
    public void promptForPlayerName(PlayerNameCallback callback)
    {
        execute(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Enter Your Name");

                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", null); // Set to null initially
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                builder.setCancelable(false);

                AlertDialog dialog = builder.create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        okButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String playerName = input.getText().toString();
                                if (!playerName.trim().isEmpty()) {
                                    callback.onNameEntered(playerName);
                                    dialog.dismiss(); // Dismiss dialog only if input is not empty
                                }
                            }
                        });
                    }
                });

                dialog.show();

                // Initially disable the OK button
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                input.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        // Enable the OK button only if there's text
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(charSequence.length() > 0);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {}
                });
            }
        });
    }

    /**
     * Executes the given action on the UI thread.
     * @param action The action to be executed.
     */
    @Override
    public void execute(Runnable action) {
        super.runOnUiThread(action);
    }
}