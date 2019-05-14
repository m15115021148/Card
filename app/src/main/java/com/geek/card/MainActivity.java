package com.geek.card;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private String[] strs = {Manifest.permission.CAMERA};
    private TextView mContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContent = findViewById(R.id.content);

        requestPermission();
    }

    public void onScan(View view) {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permission != 0){
            requestPermission();
            return;
        }
        Intent intent = new Intent(this,ScanQrCodeActivity.class);
        startActivityForResult(intent,1001);
    }

    private void requestPermission(){
        int checkCallPhonePermission = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA);
        if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,strs,1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK){
            String securityCode = data.getStringExtra("data");
            showScan(securityCode);
        }
    }

    private void showScan(String result){
        StringBuffer sb = new StringBuffer();
        sb.append("扫描到的内容：\n\n");
        sb.append(result);
        sb.append("\n");
        mContent.setText(sb.toString());
    }
}
