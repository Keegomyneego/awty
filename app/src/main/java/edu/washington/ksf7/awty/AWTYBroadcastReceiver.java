package edu.washington.ksf7.awty;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
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

    public static void startService(Activity activityContext, String phoneNumber, String message, int minuteInterval) {

        Log.i(TAG, "Starting AWTY Service.");

        switch (executionStatus) {
            case STOPPED:

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (activityContext.checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        Log.i(TAG, "Permission to send SMS not yet granted, requesting...");

                        activityContext.requestPermissions(new String[] {
                                Manifest.permission.SEND_SMS
                        }, 1);
                    }
                }

                Intent startAWTYReceiver = new Intent(activityContext, AWTYBroadcastReceiver.class);
                startAWTYReceiver.putExtra("phoneNumber", phoneNumber);
                startAWTYReceiver.putExtra("message", message);

                alarmIntent = PendingIntent.getBroadcast(activityContext, 0, startAWTYReceiver, 0);
                alarmManager = (AlarmManager) activityContext.getSystemService(activityContext.ALARM_SERVICE);

                long triggerInMillis = minutesBeforeFirstFire * 60 * 1000;
                long intervalMillis = minuteInterval * 60 * 1000;

                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + triggerInMillis,
                        intervalMillis,
                        alarmIntent);

                Log.i(TAG, "Scheduled message \"" + message + "\" to be sent to " + phoneNumber + " every " + minuteInterval + " minutes.");

                executionStatus = ExecutionStatus.STARTED;
                break;

            default:
                Log.e(TAG, "AWTYBroadcastReceiver must be stopped before it can be started again!");
        }
    }

    public static void stopService() {

        Log.i(TAG, "Stopping AWTY Service");

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
        String phoneNumber = intent.getStringExtra("phoneNumber");
        String message = intent.getStringExtra("message");

        // Send sms message
        sendSMS(context.getApplicationContext(), message, phoneNumber);

        // Notify sender that it's been sent
        Toast.makeText(context, "Texting " + phoneNumber + ": " + message, Toast.LENGTH_SHORT).show();

        // Log that we sent it
        Log.i(TAG, "Sending \"" + message + "\" to " + phoneNumber);
    }

    private void sendSMS(Context context, String message, String phoneNumber) {

        // Listen to status broadcasts
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
        context.registerReceiver(getSMSSentStatusReceiver(), new IntentFilter(SENT));
        context.registerReceiver(getSMSDeliveredStatusReceiver(), new IntentFilter(DELIVERED));

        // Send the message
        SmsManager smsManager = SmsManager.getDefault();
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED), 0);
        smsManager.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }

    // BroadcastReceivers

    private BroadcastReceiver getSMSSentStatusReceiver() {
        return new BroadcastReceiver() {

            @Override
            public void onReceive(Context msgContext, Intent msgIntent) {
                String sentStatus = "";

                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        sentStatus = "SMS sent";
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        sentStatus = "Generic failure";
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        sentStatus = "No service";
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        sentStatus = "Null PDU";
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        sentStatus = "Radio off";
                        break;
                }

                Toast.makeText(msgContext, sentStatus, Toast.LENGTH_SHORT).show();

                // One time receiver, stop listening after first receive
                msgContext.unregisterReceiver(this);
            }
        };
    }

    private BroadcastReceiver getSMSDeliveredStatusReceiver() {
        return new BroadcastReceiver() {

            @Override
            public void onReceive(Context msgContext, Intent msgIntent) {
                String deliveredStatus = "";

                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        deliveredStatus = "SMS delivered";
                        break;
                    case Activity.RESULT_CANCELED:
                        deliveredStatus = "SMS not delivered";
                        break;
                }

                Toast.makeText(msgContext, deliveredStatus, Toast.LENGTH_SHORT).show();

                // One time receiver, stop listening after first receive
                msgContext.unregisterReceiver(this);
            }
        };
    }
}
