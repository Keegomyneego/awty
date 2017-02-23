package edu.washington.ksf7.awty;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class AWTYBroadcastReceiver extends BroadcastReceiver {

    private static String TAG = "AWTYBroadcastReceiver";

    public static final int minutesBeforeFirstFire = 1;

    //----------------------------------------------------------------------------------------------
    // Client Interface
    //----------------------------------------------------------------------------------------------

    public enum ExecutionStatus {
        STARTED,
        STOPPED
    }

    public AWTYBroadcastReceiver() {
    }

    public static ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public static void startService(Context context, String message, int minuteInterval) {
        switch (executionStatus) {
            case STOPPED:

                Intent startAWTYReceiver = new Intent(context, AWTYBroadcastReceiver.class);
                startAWTYReceiver.putExtra("message", message);

                alarmIntent = PendingIntent.getBroadcast(context, 0, startAWTYReceiver, 0);
                alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);

                long triggerInMillis = minutesBeforeFirstFire * 60 * 1000;
                long intervalMillis = minuteInterval * 60 * 1000;

                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + triggerInMillis,
                        intervalMillis,
                        alarmIntent);

                executionStatus = ExecutionStatus.STARTED;
                break;

            default:
                Log.e(TAG, "AWTYBroadcastReceiver must be stopped before it can be started again!");
        }
    }

    public static void stopService() {
        switch (executionStatus) {
            case STARTED:
                if (alarmManager != null) {
                    alarmManager.cancel(alarmIntent);
                }

                executionStatus = ExecutionStatus.STOPPED;
                break;

            default:
                Log.e(TAG, "AWTYBroadcastReceiver must be started before it can be stopped!");
        }
    }

    //----------------------------------------------------------------------------------------------
    // Implementation
    //----------------------------------------------------------------------------------------------

    private static ExecutionStatus executionStatus = ExecutionStatus.STOPPED;
    private static AlarmManager alarmManager;
    private static PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra("message");
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
