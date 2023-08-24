package com.example.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class WidgetWorker extends Worker {

    public WidgetWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        //模拟耗时/网络请求操作
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        Log.d("mytag.MyAppWidgetProvider", "doWork: 刷新");

        //刷新widget
        //updateWidget(getApplicationContext());

        return Result.success();
    }

    /**
     * 刷新widget
     */
    private void updateWidget(Context context) {
        //只能通过远程对象来设置appwidget中的控件状态
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.my_app_widget_provider);
        //通过远程对象修改textview
        //remoteViews.setTextViewText(R.id.tv_text, data);

        //获得appwidget管理实例，用于管理appwidget以便进行更新操作
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        //获得所有本程序创建的appwidget
        ComponentName componentName = new ComponentName(context, MyAppWidgetProvider.class);
        //更新appwidget
        appWidgetManager.updateAppWidget(appWidgetManager.getAppWidgetIds(componentName), remoteViews);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(componentName), R.id.grid_view);
    }
}
