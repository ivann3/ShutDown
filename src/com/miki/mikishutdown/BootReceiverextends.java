package com.miki.mikishutdown;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiverextends extends BroadcastReceiver {

	static final String action_boot ="android.intent.action.BOOT_COMPLETED";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Intent bootStartIntent = new Intent(context, MainActivity.class);
		bootStartIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(bootStartIntent);
	}

}