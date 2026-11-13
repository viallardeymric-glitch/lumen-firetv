package fr.lumen.tv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Lance Lumen dès que le Fire Stick a fini de démarrer. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent go = new Intent(context, MainActivity.class);
        go.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(go);
    }
}
