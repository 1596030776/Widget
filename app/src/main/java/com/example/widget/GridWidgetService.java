package com.example.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

public class GridWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GridRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class GridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private List<Document> mDataList = new ArrayList<Document>();
    private Context context;
    private int appWidgetId;

    //实现 RemoteViewsFactory 接口的自定义类可以为应用微件包含的集合中的项目提供数据
    public GridRemoteViewsFactory(Context context, Intent intent) {
        Log.d("tag.GridWidgetService", "构造: GridWidgetService");
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public void onCreate() {
        Log.d("tag.GridWidgetService", "onCreate: GridWidgetService");
        for (Document document : MainActivity.documents) {
            mDataList.add(document);
        }
    }

    @Override
    public void onDataSetChanged() {
        mDataList.clear();
        Log.d("tag.GridWidgetService", "onCreate: GridWidgetService");
        for (Document document : MainActivity.documents) {
            mDataList.add(document);
        }
    }

    @Override
    public void onDestroy() {
        mDataList.clear();
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    //返回与位于数据集中指定 position 的数据对应的 RemoteViews 对象
    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.documents_item);
        rv.setTextViewText(R.id.document_name, mDataList.get(position).getName());
        rv.setTextViewText(R.id.document_time, mDataList.get(position).getTime());
        rv.setImageViewResource(R.id.document_image, mDataList.get(position).getImageId());

        Bundle extras = new Bundle();
        extras.putInt(MyAppWidgetProvider.EXTRA_ITEM, position);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.document_item, fillInIntent);

        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

}