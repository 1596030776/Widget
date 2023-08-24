package com.example.widget;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener{
    private SearchView mSearchView = null;
    private ListView mListView = null;

    private TextView canelSearch = null;

    private ArrayList<Document> documentShow = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSearchView = (SearchView) findViewById(R.id.search_view);
        //设置SearchView为搜索栏
        mSearchView.setIconifiedByDefault(false);
        //设置默认提示文字
        mSearchView.setQueryHint("搜索文件");

        mListView = (ListView) findViewById(R.id.search_result_list);
        DocumentAdapter adapter = new DocumentAdapter(this,R.layout.documents_item_activity,documentShow);
        mListView.setAdapter(adapter);
        mListView.setTextFilterEnabled(true);

        // 设置搜索文本监听
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)){
                    adapter.getFilter().filter(newText);
                }else{
                    adapter.getFilter().filter("");
                }
                return false;
            }
        });

        //取消搜索按钮
        canelSearch = findViewById(R.id.cancel_search_btn);
        canelSearch.setOnClickListener(this);

        //点击打开文件
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Document document = documentShow.get(position);

                Intent intent =  new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(document.getUri(),"text/plain");
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cancel_search_btn){
            this.finish();
        }
    }
}
