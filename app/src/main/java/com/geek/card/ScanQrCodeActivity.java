package com.geek.card;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;


import java.lang.ref.WeakReference;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.QRCodeDecoder;


public class ScanQrCodeActivity extends AppCompatActivity implements QRCodeView.Delegate{

    private static final String TAG = ScanQrCodeActivity.class.getSimpleName();
    private static final int REQUEST_CODE_CHOOSE_PHOTO_FROM_GALLERY = 666;
    private static final int FLASH_LIGHT_CLOSE_TAG = 0;
    private static final int ZOOM_MODE_MINUS = 1;
    private static final int ZOOM_MODE_PLUS = 2;

    public static final String EXTRA_KEY = "scan_type";
    public static final int SCAN_CONTRACT = 100;
    public static final int SCAN_SECURITY_CODE = 101;

    CustomZXingView mQRCodeView;

    private Camera mCamera;
    private Camera.Parameters mCameraParameters;
    private int mMaxZoom;
    private int mChangeRate;
    private static String mPhotoPath;
    private Dialog mDialog;
    private Handler mHandler;
    private int mZoomMode = ZOOM_MODE_MINUS;
    private int mScanType;
    private Runnable mSetZoomRunnable;

    private static class DecodeAsyncTask extends AsyncTask<Void,Void,String> {
        private WeakReference<ScanQrCodeActivity> mContext;
        DecodeAsyncTask(ScanQrCodeActivity context){
            mContext = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(Void... voids) {
            return QRCodeDecoder.syncDecodeQRCode(mPhotoPath);
        }

        @Override
        protected void onPostExecute(String s) {
            if(mContext.get() != null){
                mContext.get().scanResult(s);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr_code);

        mQRCodeView = findViewById(R.id.zx_view);

        initData(savedInstanceState);
    }



    public void initData(Bundle savedInstanceState) {
        mQRCodeView.setDelegate(this);
        mHandler = new Handler();
        mSetZoomRunnable = new Runnable() {
            @Override
            public void run() {
                int currentZoom = mCameraParameters.getZoom();
                if(mZoomMode == ZOOM_MODE_MINUS){
                    currentZoom = currentZoom - mChangeRate;
                    currentZoom = currentZoom < 0 ? 0 : currentZoom;
                }else if(mZoomMode == ZOOM_MODE_PLUS){
                    currentZoom = currentZoom + mChangeRate;
                    currentZoom = currentZoom > mMaxZoom ? mMaxZoom : currentZoom;
                }
                setZoom(currentZoom);
                mHandler.postDelayed(this,200);
            }
        };
        mScanType = getIntent().getIntExtra(EXTRA_KEY,SCAN_CONTRACT);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mQRCodeView.startCamera();
        mCamera = mQRCodeView.getCamera();
        if(mCamera != null){
            mCameraParameters = mCamera.getParameters();
            mMaxZoom = mCameraParameters.getMaxZoom();
            mChangeRate = mMaxZoom / 100;
            mChangeRate = mChangeRate == 0 ? 1 : mChangeRate;
            mChangeRate = mChangeRate * 2;
            mQRCodeView.startSpotAndShowRect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mQRCodeView.stopCamera();
    }

    @Override
    protected void onDestroy() {
        mQRCodeView.onDestroy();
        if(mDialog != null){
            if(mDialog.isShowing()){
                mDialog.dismiss();
            }
            mDialog = null;
        }
        super.onDestroy();
    }

    @Override
    public void onScanQRCodeSuccess(String s) {
        //mQRCodeView.startSpot();
        mQRCodeView.stopSpot();
        scanResult(s);
    }

    @Override
    public void onScanQRCodeOpenCameraError() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mQRCodeView.showScanRect();
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_CHOOSE_PHOTO_FROM_GALLERY){
            if(data != null){
                Uri uri = data.getData();
                if(uri != null){
                    if(!TextUtils.isEmpty(uri.getAuthority())){
                        Cursor cursor = getContentResolver().query(uri,
                                new String[]{MediaStore.Images.Media.DATA},null,null,null);
                        if(cursor != null){
                            cursor.moveToFirst();
                            mPhotoPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                            cursor.close();
                        }
                    }else{
                        mPhotoPath = uri.getPath();
                    }
                }
            }
            if(!"".equals(mPhotoPath)){
                //String result = QRCodeDecoder.syncDecodeQRCode(photoPath);
                //this method should execute in sub thread
                DecodeAsyncTask decodeAsyncTask = new DecodeAsyncTask(this);
                decodeAsyncTask.execute();
            }
        }
    }

    private void setZoom(int value){
        if(mCamera != null && mCameraParameters != null){
            mCameraParameters.setZoom(value);
            mCamera.setParameters(mCameraParameters);
        }
    }

    private void scanResult(String s){
        if(TextUtils.isEmpty(s)){
            finish();
            return;
        }
        switch (mScanType){
            case SCAN_CONTRACT:
                scanResultOfUser(s);
                break;
        }
    }

    private void scanResultOfUser(String s){
        Intent result = new Intent();
        result.putExtra("data", s);
        setResult(RESULT_OK,result);
        finish();
    }

}
