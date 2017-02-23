package edu.washington.ksf7.awty;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    final static String TAG = "MainActivity";

    public enum ExecutionStatus {
        STARTED,
        STOPPED
    }

    public ExecutionStatus executionStatus = ExecutionStatus.STOPPED;

    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public void startService() {
        executionStatus = ExecutionStatus.STARTED;
    }

    public void stopService() {
        executionStatus = ExecutionStatus.STOPPED;
    }

    //----------------------------------------------------------------------------------------------
    // Implementation
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupViews();
    }

    private void setupViews() {

        findViewById(R.id.awty_start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (getExecutionStatus()) {
                    case STARTED:
                        stop();
                        break;

                    case STOPPED:
                        attemptToStart();
                        break;
                }
            }
        });
    }

    private void clicked() {
        // disable button
        // check state
        // if stopped -> start
        // if started -> stop
    }

    private void attemptToStart() {
        //
        // validate input
        //

        // message
        String messageText = ((EditText) findViewById(R.id.awty_message_text)).getText().toString();
        if (messageText.isEmpty()) {
            complain("Please enter an annoying message");
            return;
        }

        // phone number
        String phoneNumber = ((EditText) findViewById(R.id.awty_phone_number)).getText().toString();
        if (phoneNumber.isEmpty()) {
            complain("Please enter the phone number of someone to annoy");
            return;
        }

        // interval
        String intervalString = ((EditText) findViewById(R.id.awty_interval)).getText().toString();
        int interval;
        try {
            interval = Integer.valueOf(intervalString);
        } catch (NumberFormatException e) {
            complain("Please enter a positive non-zero whole number of minutes between messages");
            return;
        }
        if (interval < 1) {
            complain("Please enter a positive non-zero whole number of minutes between messages");
            return;
        }

        //
        // start
        //
        start(messageText, phoneNumber, interval);
    }

    private void complain(String reason) {
        Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
    }

    private void updateStartButton() {
        String buttonText;

        ExecutionStatus executionStatus = getExecutionStatus();

        switch (executionStatus) {
            case STARTED:
                buttonText = getString(R.string.awty_button_text_stop);
                break;

            case STOPPED:
                buttonText = getString(R.string.awty_button_text_start);
                break;

            default:
                Log.e(TAG, "unrecognized execution status: " + executionStatus);
                return;
        }

        ((Button) findViewById(R.id.awty_start_button)).setText(buttonText);
    }

    private void start(String messageText, String phoneNumber, int interval) {
        startService();

        // update button text
        updateStartButton();
        String message = phoneNumber + ": " + messageText;
        Log.d(TAG, "Starting with " + message);
    }

    private void stop() {
        stopService();

        // update button text
        updateStartButton();
        Log.d(TAG, "Stopping");
    }
}
