package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.ui.MainActivity;
import com.udacity.stockhawk.ui.StockDetailActivity;

public class StockWidgetProvider extends AppWidgetProvider {

    public static final String EXTRA_SYMBOL = "extra:symbol";
    public static final String ACTION_UPDATE_WIDGETS = "com.udacity.stockhawk.APPWIDGET_UPDATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent != null) {
            if (intent.getAction().equals(ACTION_UPDATE_WIDGETS)) {
                ComponentName thisWidget = new ComponentName(context.getApplicationContext(), StockWidgetProvider.class);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list_in_widget);
                if (appWidgetIds != null && appWidgetIds.length > 0) {
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);


        for (int appWidgetId : appWidgetIds) {

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener to the button
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_inital);
            // what is happened to widget when i click on it:
            remoteViews.setOnClickPendingIntent(R.id.container, pIntent);

            // set adapter to list_in_widget listview
            // to do that i start e service so as to use a background thread
            // onUpdate() method runs on UI thread !!!
            Intent widgetIntent = new Intent(context, StockWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            remoteViews.setRemoteAdapter(R.id.list_in_widget, widgetIntent);

            // Create an Intent to launch StockDetailActivity.class from stocks list_in_widget
            Intent launchIntent = new Intent(context, StockDetailActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.list_in_widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list_in_widget);
        }
    }
}



