package com.example.widget;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.List;

public class CheckboxAdapter extends RecyclerView.Adapter<CheckboxAdapter.ViewHolder>{
    public List<Document> documentList;
    private Context mContext;
    private LinearLayoutManager mLayoutManager;
    private int position = 0;

    public CheckboxAdapter(Context context, List<Document> list, LinearLayoutManager layoutManager) {
        documentList = list;
        mContext = context;
        mLayoutManager = layoutManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.documents_item_activity, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.documentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = holder.getLayoutPosition();
                //当不处于选择状态时才能打开文件
                if (!MainActivity.clickFlag && documentList.size()>0) {
                    Document document = documentList.get(position);

                    Intent intent = new Intent("android.intent.action.VIEW");
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    String type = "text/plain";
                    ContentResolver contentResolver = mContext.getContentResolver();
                    type = contentResolver.getType(document.getUri());

                    intent.setDataAndType(document.getUri(), type);
                    mContext.startActivity(intent);
                } else {
                    //处于选择状态时,点击一个listview项目则选中
                    CheckBox checkBox = mLayoutManager.findViewByPosition(position).findViewById(R.id.document_checkBox);
                    if (documentList.get(position).isSelected) {
                        //取消选择一个项目
                        checkBox.setChecked(false);
                        documentList.get(position).isSelected = false;
                    } else {
                        //选择一个项目
                        checkBox.setChecked(true);
                        documentList.get(position).isSelected = true;
                    }
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Document document = documentList.get(position);
        holder.documentImage.setImageResource(document.getImageId());
        holder.documentName.setText(document.getName());
        holder.documentSize.setText(document.getSize());
        holder.documentTime.setText(document.getTime());

        //遍历fragment中的listview，根据clickFlag将多选框设为可见或不可见
        if (MainActivity.clickFlag) {
            holder.documentCheckBox.setVisibility(View.VISIBLE);
        } else {
            holder.documentCheckBox.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView documentImage;
        TextView documentName;
        TextView documentTime;
        TextView documentSize;
        CheckBox documentCheckBox;
        View documentView;

        public ViewHolder(@NonNull View view) {
            super(view);
            documentImage = view.findViewById(R.id.document_image_acticity);
            documentName = view.findViewById(R.id.document_name_acticity);
            documentTime = view.findViewById(R.id.document_time_acticity);
            documentSize = view.findViewById(R.id.document_size_acticity);
            documentCheckBox = view.findViewById(R.id.document_checkBox);
            documentView = view;
        }
    }
}