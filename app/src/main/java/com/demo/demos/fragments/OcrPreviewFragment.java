package com.demo.demos.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.demo.demos.detection.MTCNN;
import com.demo.demos.model.ScanResult;


/**
 * @author xiongbin
 * @description:
 * @date : 2021/3/9 8:48
 */

public class OcrPreviewFragment extends PreviewFragment {


    MTCNN mtcnn ;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
       mtcnn =  MTCNN.create(getActivity().getAssets());
    }

    @Override
    protected ScanResult ocrRecognize(byte[] image) {
        ScanResult result = super.ocrRecognize(image);
        return   mtcnn.detect(result);
    }
}
