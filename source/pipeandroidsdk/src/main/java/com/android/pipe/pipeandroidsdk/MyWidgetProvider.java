package com.android.pipe.pipeandroidsdk;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;


public class MyWidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        final int N = appWidgetIds.length;


        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            PendingIntent pendingIntent = null;
            {

	            Intent intent = new Intent(context, RecordVideoActivity.class);
	            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			}


            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setOnClickPendingIntent(R.id.widget_launch_open_camera, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

}
