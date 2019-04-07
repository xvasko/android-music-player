package com.matejvasko.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StopServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("AXCAasdjfn njfn");
        Intent service = new Intent(context, LocationService.class);
        context.stopService(service);
    }
}
