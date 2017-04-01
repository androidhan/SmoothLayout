package com.hanshao.viewdraghelper.smaple;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hanshao.viewdraghelper.smaple.view.SmoothLayout;

public class MainActivity extends AppCompatActivity {

    private SmoothLayout mSmoothLayout;
    private ImageView mOpen;
    private TextView mLook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        

    }

    private void initData() {
    }

    private void initView() {

        mSmoothLayout = (SmoothLayout) findViewById(R.id.smooth_layout);
        mOpen = (ImageView) findViewById(R.id.open);
        mLook = (TextView) findViewById(R.id.look);


        mSmoothLayout.setOnDragStateListener(new SmoothLayout.OnDragStateListener() {
            @Override
            public void onOpenState() {
//                Log.e("TAG","当前正是打开状态");
            }

            @Override
            public void onCloseState() {
//                Log.e("TAG","当前正是关闭状态");
            }

            @Override
            public void onDragState() {
            }
//                 Log.e("TAG","当前正是拖拽状态");
            @Override
            public void onStartOpenState() {
//                Log.e("TAG","当前正是开始打开状态");
            }

            @Override
            public void onStartCloseState() {
//                Log.e("TAG","当前正是开始关闭状态");
            }
        });

        mOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mSmoothLayout.getDragEnable()){
                    mSmoothLayout.close();
                }else{
                    mSmoothLayout.open();
                }

            }
        });

        mLook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                OtherActivity.startActivity(MainActivity.this);
            }
        });
    }
}
