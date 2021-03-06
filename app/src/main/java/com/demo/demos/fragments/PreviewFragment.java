package com.demo.demos.fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.demo.demos.R;
import com.demo.demos.base.BaseActivity;
import com.demo.demos.base.BaseFragment;
import com.demo.demos.model.ScanResult;
import com.demo.demos.utils.CameraUtils;
import com.demo.demos.views.AutoFitTextureView;
import com.demo.demos.views.ViewfinderView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreviewFragment extends BaseFragment {

    public final static int UPDATE_TIP=1;

    public final static int UPDATE_IMAGE=2;
    protected  ScanResult result = new ScanResult();
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
             switch (msg.what){
                 case UPDATE_TIP:
                      ScanResult result = (ScanResult) msg.obj;
                      if(result.bitmap!=null){
                          iv_show.setImageBitmap(result.bitmap);
                      }
                     break;
                 case UPDATE_IMAGE:
                     break;
             }


        }
    };



    private static final long PREVIEW_SIZE_MIN = 720 * 480;
    Button btnChangePreviewSize;
    Button btnImageMode;
    Button btnVideoMode;
    ImageView iv_show;
    //    TextureView previewView;//????????????view
    AutoFitTextureView previewView;//?????????????????????view
    CameraManager cameraManager;//???????????????
    CameraDevice cameraDevice;//???????????????
    CameraCaptureSession cameraCaptureSession;//???????????????
    String cameraId;//??????id
    List<Size> outputSizes;//??????????????????
    int sizeIndex = 0;
    Size previewSize;//????????????
    ImageReader previewReader;
    ViewfinderView viewfinderView;




    //?????????????????????
 /*   final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3,5,1, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(100));*/



    private ExecutorService threadPoolExecutor  = Executors.newSingleThreadExecutor();

    public PreviewFragment() {
        // Required empty public constructor
    }

    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {

        //TextureView ??????????????????????????????
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //TextureView ?????????????????????
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preview, container, false);
    }


    Bitmap getTestBitmap(){
       // getActivity().getResources().getDrawable(R.drawable.ic_idcard);
        return BitmapFactory.decodeResource(getResources(),R.drawable.ic_idcard);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //???????????????
        initCamera();
        //???????????????
        initViews(view);
    }

    private void initCamera() {
        cameraManager = CameraUtils.getInstance().getCameraManager();
        cameraId = CameraUtils.getInstance().getCameraId(false);//????????????????????????
        //??????????????????????????????????????????????????????
        outputSizes = CameraUtils.getInstance().getCameraOutputSizes(cameraId, SurfaceTexture.class);

        //?????????????????????
        previewSize = outputSizes.get(0);
    }

    private void initViews(View view) {
        iv_show = view.findViewById(R.id.iv_show);
        viewfinderView = view.findViewById(R.id.viewFinder);
        btnChangePreviewSize = view.findViewById(R.id.btn_change_preview_size);
        btnChangePreviewSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //?????????????????????
                updateCameraPreview();
                setButtonText();
                Log.e(TAG, "onClick: " + previewView.getWidth()+ ';' + previewView.getHeight() );

            }
        });
        setButtonText();

        btnImageMode = view.findViewById(R.id.btn_image_mode);
        btnImageMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //???????????????????????????????????????
                updateCameraPreviewWithImageMode();
            }
        });

        btnVideoMode = view.findViewById(R.id.btn_video_mode);
        btnVideoMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //??????????????????????????????????????????????????????????????????????????????????????????
                //????????????????????????????????????????????????????????????????????????????????????
                updateCameraPreviewWithVideoMode();
            }
        });

        previewView = view.findViewById(R.id.afttv_camera);
        previewView.setAspectRation(previewSize.getHeight(), previewSize.getWidth());

        //?????? TextureView ???????????????
        previewView.setSurfaceTextureListener(surfaceTextureListener);

        setLabelText("?????????????????????????????????");

    }

    public void setFrameSize(int width,int height){
        viewfinderView.setFrameSize(width,height);
    }


    public void setLabelText(String text){
        viewfinderView.setLabelText(text);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((BaseActivity) getActivity()).requestPermission("???????????????????????????????????????app????????????",
                new BaseActivity.Callback() {
                    @Override
                    public void success() {
                        if (previewView.isAvailable()) {
                            openCamera();
                        } else {
                            previewView.setSurfaceTextureListener(surfaceTextureListener);
                        }
                    }

                    @Override
                    public void failed() {
                        Toast.makeText(getContext(), "?????????????????????????????????", Toast.LENGTH_SHORT).show();
                    }
                },
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCamera();
    }

    @SuppressLint("MissingPermission")
    private void openCamera() {
        try {
            //????????????
            cameraManager.openCamera(cameraId,
                    new CameraDevice.StateCallback() {
                        @Override
                        public void onOpened(CameraDevice camera) {
                            if (camera == null) {
                                return;
                            }
                            cameraDevice = camera;
                            //?????????????????? session
                            createPreviewSession();
                        }

                        @Override
                        public void onDisconnected(CameraDevice camera) {
                            //??????????????????
                            releaseCamera();
                        }

                        @Override
                        public void onError(CameraDevice camera, int error) {
                            //??????????????????
                            releaseCamera();
                        }
                    },
                    null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



    protected ScanResult ocrRecognize(byte[] bytes){
        Bitmap temp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
/*        Log.d("xiong","image->" + image.getWidth() + "|" + image.getHeight() + " format->" + image.getFormat() +
                " planes.length->" + image.getPlanes().length + " bytes->" + bytes.length + " temp->" + temp.getByteCount());*/
        //   Bitmap newBitmap = BitmapUtil.rotateBitmap(temp, 90);
        //  iv_show.setImageBitmap(temp);

        result.status = UPDATE_TIP;
        result.bitmap = temp;


        return result;
    }


    private void asyncOcrRecognize(final byte[] image){
        Runnable runnable  = new Runnable() {
            @Override
            public void run() {
              ScanResult  result =   ocrRecognize(image);
              Message  message =  handler.obtainMessage();
              message.what = UPDATE_TIP;
              message.obj = result;
              handler.sendMessage(message);
            }
        };
        threadPoolExecutor.execute(runnable);
    }



    private void createPreviewSession() {
        //?????????????????????
        CameraUtils.getInstance().releaseImageReader(previewReader);
        CameraUtils.getInstance().releaseCameraSession(cameraCaptureSession);
        //??????TextureView ??? ????????? previewSize ?????????????????????????????????Surface
        SurfaceTexture surfaceTexture = previewView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());//??????SurfaceTexture???????????????
        final Surface previewSurface = new Surface(surfaceTexture);
        //?????? ImageReader ??? surface
        previewReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.JPEG, 2);
        previewReader.setOnImageAvailableListener(
                new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader reader) {
                        Image image = reader.acquireLatestImage();
                        if (image != null) {
                            ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
                            byte[] bytes = new byte[byteBuffer.remaining()];
                            byteBuffer.get(bytes);
                            asyncOcrRecognize(bytes);
                            image.close();
                        }
                    }
                },
                null);
        final Surface readerSurface = previewReader.getSurface();

        try {
            //????????????session
            cameraDevice.createCaptureSession(Arrays.asList(previewSurface, readerSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {

                            cameraCaptureSession = session;

                            try {
                                //????????????????????????
                                CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                builder.addTarget(previewSurface);//?????? previewSurface ?????????????????????????????????
                                builder.addTarget(readerSurface);
                                CaptureRequest captureRequest = builder.build();
                                //????????????????????????????????????????????????
                                session.setRepeatingRequest(captureRequest, new CameraCaptureSession.CaptureCallback() {
                                            @Override
                                            public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
                                                super.onCaptureProgressed(session, request, partialResult);
                                            }

                                            @Override
                                            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                                                super.onCaptureCompleted(session, request, result);
                                            }
                                        },
                                        null);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {

                        }
                    },
                    null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void updateCameraPreview() {
        if (sizeIndex + 1 < outputSizes.size()) {
            sizeIndex++;
        } else {
            sizeIndex = 0;
        }
        previewSize = outputSizes.get(sizeIndex);
        previewView.setAspectRation(previewSize.getHeight(), previewSize.getWidth());
        //??????????????????
        createPreviewSession();
    }

    private void updateCameraPreviewWithImageMode() {
        previewSize = outputSizes.get(0);
        previewView.setAspectRation(previewSize.getHeight(), previewSize.getWidth());
        createPreviewSession();
    }

    private void updateCameraPreviewWithVideoMode() {
        List<Size> sizes = new ArrayList<>();
        //???????????????????????????????????????????????????
        float ratio = ((float) previewView.getHeight() / previewView.getWidth());
        //???????????????????????????????????????????????????????????????????????????
        for (int i = 0; i < outputSizes.size(); i++) {
            if (((float) outputSizes.get(i).getWidth()) / outputSizes.get(i).getHeight() == ratio) {
                sizes.add(outputSizes.get(i));
            }
        }
        if (sizes.size() > 0) {
            previewSize = Collections.max(sizes, new CameraUtils.CompareSizesByArea());
            previewView.setAspectRation(previewSize.getHeight(), previewSize.getWidth());
            createPreviewSession();
            return;
        }
        //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        sizes.clear();
        float detRatioMin = Float.MAX_VALUE;
        for (int i = 0; i < outputSizes.size(); i++) {
            Size size = outputSizes.get(i);
            float curRatio = ((float) size.getWidth()) / size.getHeight();
            if (Math.abs(curRatio - ratio) < detRatioMin) {
                detRatioMin = curRatio;
                previewSize = size;
            }
        }
        if (previewSize.getWidth() * previewSize.getHeight() > PREVIEW_SIZE_MIN) {
            previewView.setAspectRation(previewSize.getHeight(), previewSize.getWidth());
            createPreviewSession();
        }
        //??????????????????????????????????????????????????????????????????????????????????????????????????????
        long area = previewView.getWidth() * previewView.getHeight();
        long detAreaMin = Long.MAX_VALUE;
        for (int i = 0; i < outputSizes.size(); i++) {
            Size size = outputSizes.get(i);
            long curArea = size.getWidth() * size.getHeight();
            if (Math.abs(curArea - area) < detAreaMin) {
                detAreaMin = curArea;
                previewSize = size;
            }
        }
        previewView.setAspectRation(previewSize.getHeight(), previewSize.getWidth());
        createPreviewSession();
    }

    private void releaseCamera() {
        CameraUtils.getInstance().releaseImageReader(previewReader);
        CameraUtils.getInstance().releaseCameraSession(cameraCaptureSession);
        CameraUtils.getInstance().releaseCameraDevice(cameraDevice);
    }

    private void setButtonText() {
        btnChangePreviewSize.setText(previewSize.getWidth() + "-" + previewSize.getHeight());
    }
}
