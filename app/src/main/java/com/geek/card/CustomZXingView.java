package com.geek.card;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

import cn.bingoogolapple.qrcode.zxing.ZXingView;

public class CustomZXingView extends ZXingView {

    public CustomZXingView(Context context, AttributeSet attributeSet){
        super(context,attributeSet);
    }

    public Camera getCamera(){
        return mCamera;
    }
}
