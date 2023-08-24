package com.example.widget;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class FragmentPage extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private ArrayList<Document> documents;
    private View rootView;
    public RecyclerView recyclerView;
    public CheckboxAdapter adapter;
    private ImageButton deleteBtn;
    public SwipeRefreshLayout mSwipeRefreshLayout;
    private selectBtnListener mCallback;
    public TextView selectAllBtn;
    public LinearLayout documentActionBar;
    public static boolean selectAll = false;
    private LinearLayoutManager layoutManager;

    public FragmentPage() {
    }

    public void setOnFragmentInteractionListener(selectBtnListener listener) {
        mCallback = listener;
    }

    public interface selectBtnListener {
        public void switchSelectBtn();
    }

    public void switchSelectBtn() {
        mCallback.switchSelectBtn();
        recyclerView.getAdapter().notifyDataSetChanged();

        int lastItemPosition = adapter.documentList.size() - 1;
        Log.d("mytag.FragmentPage", lastItemPosition+"");

    }

    public static FragmentPage newInstance(ArrayList<Document> param1) {
        FragmentPage fragment = new FragmentPage();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PARAM1, (ArrayList) param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            documents = (ArrayList) getArguments().getParcelableArrayList(ARG_PARAM1);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //rootView为page_item页面
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.page_item, container, false);
        }
        recyclerView = rootView.findViewById(R.id.document_list);
        selectAllBtn = rootView.findViewById(R.id.selectAll_btn);
        documentActionBar = rootView.findViewById(R.id.document_action_bar);
        deleteBtn = rootView.findViewById(R.id.delete_button);
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);

        layoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new CheckboxAdapter(getContext(), documents, layoutManager);
        recyclerView.setAdapter(adapter);

        HandlerThread mHandlerThread = new HandlerThread("refreshHandlerThread");
        Handler workHandler = new MyHandler(this);
        mHandlerThread.start();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Message msg = Message.obtain();
                msg.what = 1;
                // 通过Handler发送消息到其绑定的消息队列
                workHandler.sendMessage(msg);
            }
        });

        //删除文件按钮点击事件
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i=0;i<adapter.documentList.size();i++) {
                    //根据文件uri从手机存储中删除文件
                    Document document = adapter.documentList.get(i);
                    if (document.isSelected) {
                        adapter.documentList.remove(i);
                        ContentResolver contentResolver = getContext().getContentResolver();
                        contentResolver.delete(document.getUri(), null, null);
                        i--;
                        //删除后刷新页面
                        adapter.notifyItemRemoved(i);
                    }
                }

                //调用主页面的选择按钮退出选择状态
                MainActivity.selectBtn.performClick();
            }
        });

        //全选文件按钮点击事件
        selectAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAll = !selectAll;
                if (!selectAll) {
                    selectAllBtn.setText("全选");
                    for (int i=0;i<adapter.documentList.size();i++) {
                        if(recyclerView.getChildAt(i) != null) {
                            CheckBox checkBox = recyclerView.getChildAt(i).findViewById(R.id.document_checkBox);
                            checkBox.setChecked(false);
                            adapter.documentList.get(i).isSelected = false;
                        }
                    }
                } else {
                    selectAllBtn.setText("取消");
                    for (int i=0;i<adapter.documentList.size();i++) {
                        if (recyclerView.getChildAt(i) != null) {
                            CheckBox checkBox = recyclerView.getChildAt(i).findViewById(R.id.document_checkBox);
                            checkBox.setChecked(true);
                            adapter.documentList.get(i).isSelected = true;
                        }
                    }
                }
            }
        });

        return rootView;
    }

    private class MyHandler extends Handler {
        //弱引用，在垃圾回收时，被回收
        WeakReference<Fragment> fragment;

        MyHandler(Fragment f){
            this.fragment=new WeakReference<Fragment>(f);
        }

        public void handleMessage(Message message){
            switch (message.what){
                case 1:
                    //处理下拉刷新
                    MainActivity.refreshPage(getContext());
                    SharedPreferences prefsUri = getActivity().getSharedPreferences("uri", MODE_PRIVATE);
                    SharedPreferences.Editor editorUri = prefsUri.edit();
                    SharedPreferences prefsType = getActivity().getSharedPreferences("type", MODE_PRIVATE);
                    SharedPreferences.Editor editorType = prefsType.edit();
                    for (int i=0;i<MainActivity.documents.size();i++) {
                        editorUri.putString("uri_" + i, MainActivity.documents.get(i).getUri().toString());
                        editorType.putString("type_" + i, "." + MainActivity.documents.get(i).getType());
                        editorUri.apply();
                        editorType.apply();
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //通知UI变化
                            adapter.notifyDataSetChanged();
                            //结束转动条加载
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    });
            }
        }
    }
}