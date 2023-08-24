package com.example.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetService extends Service {
    private AppWidgetManager appWidgetManager; // Widget 管理器对象
    private int[] appWidgetIds; // Widget 的 ID 数组
    private RemoteViews remoteViews; // Widget 视图对象

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("mytag.WidgetService", "启动服务");
        updateWidget();

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("mytag.WidgetService", "启动服务onCreat");

        // 获取 Widget 管理器对象
        appWidgetManager = AppWidgetManager.getInstance(this);
        // 获取 Widget 的 ID 数组
        appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, MyAppWidgetProvider.class));
        appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.my_app_widget_provider);
    }

    private void updateWidget() {
        // 更新 Widget
        //用于启动GridWidgetService的intent
        Intent intentGrid = new Intent(getApplicationContext(), GridWidgetService.class);
        //将widget id作为参数传递
        intentGrid.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds);
        intentGrid.setData(Uri.parse(intentGrid.toUri(Intent.URI_INTENT_SCHEME)));
        //设置远程adapter，远程adapter通过GridWidgetService请求RemoteViews
        remoteViews.setRemoteAdapter(R.id.grid_view, intentGrid);// 通知 Widget 更新数据源
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.grid_view);
        // 更新 Widget 视图
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
