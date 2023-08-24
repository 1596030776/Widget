package com.example.widget;

import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Date;

public class Document implements Parcelable {
    private String name;
    private int imageId;
    private String time;
    private Date date;
    private String type;
    private String size;
    private Uri uri;
    public boolean isSelected = false;

    public Document(String name, String size, Date date, Uri uri) {
        this.name = name;
        this.uri = uri;
        this.size = size;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String fileTime = format.format(date);
        this.time = fileTime;
        this.date = date;

        // 获取文件类型
        int index = name.lastIndexOf(".");
        this.type = name.substring(index + 1);

        // 获取文件图片
        if (name.endsWith(".doc") || name.endsWith("docx")) {
            this.imageId = R.drawable.doc;
        } else if (name.endsWith(".pdf") || name.endsWith(".ppt") || name.endsWith(".pptx")) {
            this.imageId = R.drawable.pdf;
        } else if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
            this.imageId = R.drawable.xls;
        } else if (name.endsWith(".txt") || name.endsWith(".vcf")) {
            this.imageId = R.drawable.txt;
        }
    }

    public Document(String name,String size,String time) {
        this.name = name;
        this.size = size;
        this.time = time;

        // 获取文件类型
        int index = name.lastIndexOf(".");
        this.type = name.substring(index + 1);

        // 获取文件图片
        if (name.endsWith(".doc") || name.endsWith("docx")) {
            this.imageId = R.drawable.doc;
        } else if (name.endsWith(".pdf") || name.endsWith(".ppt") || name.endsWith(".pptx")) {
            this.imageId = R.drawable.pdf;
        } else if (name.endsWith(".xls")) {
            this.imageId = R.drawable.xls;
        } else if (name.endsWith(".txt") || name.endsWith(".vcf")) {
            this.imageId = R.drawable.txt;
        }
    }

    protected Document(Parcel in) {
        name = in.readString();
        size = in.readString();
        time = in.readString();
        type = in.readString();
        imageId = in.readInt();
        uri = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Creator<Document> CREATOR = new Creator<Document>() {
        @Override
        public Document createFromParcel(Parcel in) {
            return new Document(in);
        }

        @Override
        public Document[] newArray(int size) {
            return new Document[size];
        }
    };

    public int getImageId() {
        return imageId;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public String getType() {
        return type;
    }

    public String getSize() {
        return size;
    }

    public Uri getUri() {
        return uri;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(size);
        dest.writeString(time);
        dest.writeString(type);
        dest.writeInt(imageId);
        dest.writeParcelable(uri, flags);
    }
}
