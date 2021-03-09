package com.demo.demos.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * @author xiongbin
 * @description:
 * @date : 2021/3/9 8:50
 */

public class ScanResult implements Parcelable {
    public static final  int SUCCESS=1;
    public int code;
    public String message;
    public Bitmap bitmap;

    protected ScanResult(Parcel in) {
        code = in.readInt();
        message = in.readString();
        bitmap = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public ScanResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ScanResult() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(code);
        dest.writeString(message);
        dest.writeParcelable(bitmap, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ScanResult> CREATOR = new Creator<ScanResult>() {
        @Override
        public ScanResult createFromParcel(Parcel in) {
            return new ScanResult(in);
        }

        @Override
        public ScanResult[] newArray(int size) {
            return new ScanResult[size];
        }
    };
}
