package com.example.widget;

import static android.content.Context.MODE_PRIVATE;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of App Widget functionality.
 */
public class MyAppWidgetProvider extends AppWidgetProvider {
    //定义一个action，这个action要在AndroidMainfest中去定义，不然识别不到，名字是自定义的
    private static final String CLICK_ACTION = "com.example.widget.CLICK";
    private static final String REFRESH_ACTION = "com.example.widget.REFRESH";
    //点击事件
    public static final String TOAST_ACTION = "com.example.widget.TOAST_ACTION";
    public static final String EXTRA_ITEM = "com.example.widget.EXTRA_ITEM";
    private RemoteViews remoteViews;
    private ContentResolver contentResolver;
    private OneTimeWorkRequest workRequest;
    //onReceive不存在widget生命周期中，它是用来接收广播，通知全局的
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d("mytag.MyAppWidgetProvider", "收到广播");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        //当我们点击桌面上的widget按钮（这个按钮我们在onUpdate中已经为它设置了监听），widget就会发送广播
        //打开二级页
        if (intent.getAction().equals(CLICK_ACTION)) {
            Log.d("mytag.MyAppWidgetProvider", "打开二级页");
            Intent activityIntent = new Intent(context, MainActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        }
        //手动刷新
        if (intent.getAction().equals(REFRESH_ACTION)) {
            //TODO 刷新
            Log.d("mytag.MyAppWidgetProvider", "刷新");
//            Intent serviceIntent = new Intent(context, WidgetService.class);
//            context.startForegroundService(serviceIntent);
        }
        //更新widget
        if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            Bundle extras = intent.getExtras();
            // WIDGET_STATUS_CHANGE代表桌面曝光
            if (extras != null && extras.containsKey("WIDGET_STATUS_CHANGE")) {
                // 获取曝光的widgetID
                int[] appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                if (appWidgetIds != null && appWidgetIds.length > 0) {
                    onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds);
                }
            } else {
                super.onReceive(context, intent);
            }
        }
        //打开文件
        if (intent.getAction().equals(MyAppWidgetProvider.TOAST_ACTION)) {
            int viewIndex = intent.getIntExtra(EXTRA_ITEM, -1);
            if (viewIndex != -1) {
                SharedPreferences prefsUri = context.getSharedPreferences("uri", MODE_PRIVATE);
                String uriString = prefsUri.getString("uri_" + viewIndex, null);
                if (uriString != null) {
                    Uri uri = Uri.parse(uriString);
                    Intent intentFile = new Intent("android.intent.action.VIEW");
                    intentFile.addCategory("android.intent.category.DEFAULT");
                    intentFile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intentFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    String type = "text/plain";
                    contentResolver = context.getContentResolver();
                    type = contentResolver.getType(uri);

                    intentFile.setDataAndType(uri, type);
                    context.startActivity(intentFile);
                }
            }
        }
        super.onReceive(context, intent);
    }

    //当widget第一次添加到桌面的时候回调，可添加多次widget，但该方法只回调一次
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        workRequest = new OneTimeWorkRequest.Builder(WidgetWorker.class).build();
    }

    //当widget被初次添加或大小被改变时回调
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    //widget更新时回调
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //因为可能有多个widget，所以要对它们全部更新
        for (int i = 0; i < appWidgetIds.length; i++) {
            Log.d("mytag.MyAppWidgetProvider", "widget update");
            contentResolver = context.getContentResolver();
            context.startService(new Intent(context, WidgetService.class));

            int appWidgetId = appWidgetIds[i];
            //创建一个远程view，绑定我们要操控的widget布局文件
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.my_app_widget_provider);

            Intent intentClick = new Intent(context, MyAppWidgetProvider.class);
            Intent intentRefresh = new Intent(context, MyAppWidgetProvider.class);

            //这个必须要设置，不然点击效果会无效
            intentRefresh.setAction(REFRESH_ACTION);
            intentClick.setAction(CLICK_ACTION);

            //PendingIntent表示的是一种即将发生的意图，区别于Intent它不是立即会发生的
            PendingIntent pendingIntentClick = PendingIntent.getBroadcast(context, 0, intentClick, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent pendingIntentRefresh = PendingIntent.getBroadcast(context, 0, intentRefresh, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            //PendingIntent pendingIntentRefresh = PendingIntent.getService(context ,0, intentRefresh, PendingIntent.FLAG_UPDATE_CURRENT);

            //为布局文件中的按钮设置点击监听
            remoteViews.setOnClickPendingIntent(R.id.refresh_btn, pendingIntentRefresh);
            remoteViews.setOnClickPendingIntent(R.id.refresh_area, pendingIntentRefresh);
            //为整个widget设置点击监听
            remoteViews.setOnClickPendingIntent(R.id.my_app_widget_provider, pendingIntentClick);

            //用于启动GridWidgetService的intent
            Intent intentGrid = new Intent(context, GridWidgetService.class);
            //将widget id作为参数传递
            intentGrid.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intentGrid.setData(Uri.parse(intentGrid.toUri(Intent.URI_INTENT_SCHEME)));
            //设置远程adapter，远程adapter通过GridWidgetService请求RemoteViews
            remoteViews.setRemoteAdapter(R.id.grid_view, intentGrid);

            Intent toastIntent = new Intent(context, MyAppWidgetProvider.class);
            toastIntent.setAction(MyAppWidgetProvider.TOAST_ACTION);
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            remoteViews.setPendingIntentTemplate(R.id.grid_view, toastPendingIntent);

            //告诉AppWidgetManager对当前应用程序小部件执行更新
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.grid_view);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    //当widget被删除时回调
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    //当最后一个widget实例被删除时回调.
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        //停止任务
        WorkManager.getInstance(context).cancelUniqueWork("WidgetUpdate");
    }
}