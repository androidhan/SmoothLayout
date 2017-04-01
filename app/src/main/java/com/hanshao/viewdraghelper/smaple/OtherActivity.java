package com.hanshao.viewdraghelper.smaple;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.hanshao.viewdraghelper.smaple.view.SmoothItem;

import java.util.ArrayList;
import java.util.List;


public class OtherActivity extends AppCompatActivity {

    private ListView mListView;
    private List<Integer> mList;
    private List<SmoothItem> mSmoothItems= new ArrayList<>();


    public static void startActivity(Activity activity){
        Intent intent = new Intent(activity,OtherActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        initView();
        initData();
    }

    private void initData() {
        mList = new ArrayList<>();
        for (int i = 0; i < 10 ; i++) {
            mList.add(i);
        }

        mListView.setAdapter(new MyAdapter());
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.list_view);

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                for (SmoothItem sim:
                        mSmoothItems) {
                    sim.closeRight();
                }
                mSmoothItems.clear();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }






    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView != null){
                return convertView;
            }else{
                View v= View.inflate(OtherActivity.this,R.layout.holder_item,null);
                SmoothItem aimItem =  (SmoothItem) v.findViewById(R.id.item);
                aimItem.setOnStateListener(new SmoothItem.OnStateListener() {
                    @Override
                    public void openState(SmoothItem item) {
                    }

                    @Override
                    public void closeState(SmoothItem item) {

                    }

                    @Override
                    public void dragState(SmoothItem item) {
                    }

                    @Override
                    public void startOpen(SmoothItem item) {
                        //进行关闭当前

                        if(mSmoothItems.size() != 0){

                            for (SmoothItem sim:
                                    mSmoothItems) {
                                sim.closeRight();
                            }
                            mSmoothItems.clear();
                            mSmoothItems.add(item);
                        }else{
                            mSmoothItems.add(item) ;
                        }
                    }

                    @Override
                    public void startClose(SmoothItem item) {

                    }
                });
                return v;
            }

        }
    }


}
