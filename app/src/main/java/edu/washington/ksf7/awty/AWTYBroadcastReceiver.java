package edu.washington.ksf7.awty;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AWTYBroadcastReceiver extends BroadcastReceiver {

    //----------------------------------------------------------------------------------------------
    // Client Interface
    //----------------------------------------------------------------------------------------------

    public AWTYBroadcastReceiver() {
    }

    //----------------------------------------------------------------------------------------------
    // Implementation
    //----------------------------------------------------------------------------------------------

    @Override
    public void onReceive(Context context, Intent intent) {

        Toast.makeText(context, "received!", Toast.LENGTH_SHORT).show();
    }
}
