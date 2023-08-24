package com.example.widget;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DocumentAdapter extends ArrayAdapter<Document> implements Filterable {
    private int resourceId;
    private List<Document> mFilteredDocuments = new ArrayList<>();
    public List<Document> documentList = new ArrayList<>();

    public DocumentAdapter(@NonNull Context context, int textViewResourceId, List<Document> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
        documentList = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.documentImage = view.findViewById(R.id.document_image_acticity);
            viewHolder.documentName = view.findViewById(R.id.document_name_acticity);
            viewHolder.documentTime = view.findViewById(R.id.document_time_acticity);
            viewHolder.documentSize = view.findViewById(R.id.document_size_acticity);
            viewHolder.documentCheckBox = view.findViewById(R.id.document_checkBox);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        if (documentList.size() > 0) {
            Document document = getItem(position);
            viewHolder.documentImage.setImageResource(document.getImageId());
            viewHolder.documentName.setText(document.getName());
            viewHolder.documentTime.setText(document.getTime());
            viewHolder.documentSize.setText(document.getSize());
        }
        return view;
    }

    class ViewHolder {
        ImageView documentImage;
        TextView documentName;
        TextView documentTime;
        TextView documentSize;
        CheckBox documentCheckBox;
    }

    // 查找文件功能,根据文件名过滤得到结果
    @Override
    public Filter getFilter() {
        return new DocumentFilter();
    }

    private class DocumentFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Document> filteredList = new ArrayList<>();

            if (constraint.length() == 0) {
                filteredList = MainActivity.documents;
            } else {
                for (Document document : MainActivity.documents) {
                    if (document.getName().contains(constraint)) {
                        filteredList.add(document);
                    }
                }
            }

            results.count = filteredList.size();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredDocuments = (List<Document>) results.values;
            clear();
            addAll(mFilteredDocuments);
            notifyDataSetChanged();
        }
    }
}
