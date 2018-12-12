package com.example.laofeng.opencvfacedetect;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.opencv.core.MatOfRect;
import org.opencv.objdetect.CascadeClassifier;
import android.content.Context;
import android.os.Handler;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import pub.devrel.easypermissions.EasyPermissions;
import android.Manifest;
import android.widget.Toast;
import android.view.WindowManager;
import android.content.pm.ActivityInfo;

public class OpencvActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    private CameraBridgeViewBase openCvCameraView;

    private static final String TAG = "OpencvActivity";
    private CascadeClassifier cascadeClassifier = null; //级联分类器
    private Mat mRgba; //图像容器
    private Mat mGray;
    private int absoluteFaceSize = 0;
    private Handler handler;
    public Scalar colorArry[] = new Scalar[5];



    private static final int CAMERA_REQUESTCODE = 101;
    private static final int LOCATION_CONTACTS_REQUESTCODE = 102;


    private void initializeOpenCVDependencies() {
        try {
            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface_improved); //OpenCV的人脸模型文件： lbpcascade_frontalface_improved
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface_improved.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
            // 加载cascadeClassifier
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Error loading cascade", e);
        }
        // 显示
        openCvCameraView.enableView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opencv);

        handler = new Handler();

        openCvCameraView = (CameraBridgeViewBase) findViewById(R.id.javaCameraView);
        openCvCameraView.setCameraIndex(0); //摄像头索引        -1/0：后置双摄     1：前置
        openCvCameraView.enableFpsMeter(); //显示FPS
        openCvCameraView.setCvCameraViewListener(this);

        initWindowSettings();//设置窗口为全屏

        requestCamera();

    }


    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV init error");
        }
        initializeOpenCVDependencies();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat();
        mGray = new Mat();

        //颜色数组在摄像机启动时定义
        colorArry[0] = new Scalar(255,0,0);
        colorArry[1] = new Scalar(0,255,0);
        colorArry[2] = new Scalar(0,0,255);
        colorArry[3] = new Scalar(0,0,0);
        colorArry[4] = new Scalar(100,50,66);

    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
    }

    //在此处进行人脸识别
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba(); //RGBA
        mGray = inputFrame.gray(); //单通道灰度图


        if (absoluteFaceSize == 0) {
            int height = mGray.rows();
            if (height  > 0) {
                absoluteFaceSize =height;
            }
        }

        //检测并显示
        MatOfRect faces = new MatOfRect();
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(mGray, faces, 1.1, 2, 2,
                    new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }
        Rect[] facesArray = faces.toArray();


        if (facesArray.length > 0){
            for (int i = 0; i < facesArray.length; i++) {    //用框标记
                Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), colorArry[i], 3);
            }
        }
        return mRgba;
    }

    //用于申请相机权限此处使用的时第三方包Easypermission
    public void requestCamera() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
            Toast.makeText(this, "已授权 Camera", Toast.LENGTH_LONG).show();
        } else {
            // request for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_ask),
                    CAMERA_REQUESTCODE, Manifest.permission.CAMERA);
        }
    }

    // 初始化窗口设置, 包括全屏、横屏、常亮
    private void initWindowSettings() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

}
