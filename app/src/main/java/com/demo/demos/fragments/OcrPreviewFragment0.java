package com.demo.demos.fragments;

import android.graphics.Bitmap;

import com.demo.demos.detection.MTCNN;
import com.demo.demos.model.ScanResult;


/**
 * @author xiongbin
 * @description:
 * @date : 2021/3/9 8:48
 */

public class OcrPreviewFragment0 extends PreviewFragment {
    MTCNN mtcnn = new MTCNN();

    @Override
    protected ScanResult ocrRecognize(byte[] image) {
        ScanResult result = super.ocrRecognize(image);
        return  mtcnn.detect(result);
    }
}

