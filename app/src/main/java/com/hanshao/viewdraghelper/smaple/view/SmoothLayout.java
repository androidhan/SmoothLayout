package com.hanshao.viewdraghelper.smaple.view;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


public class SmoothLayout extends FrameLayout {



    private static final int OPEN=0;  //打开状态
    private static final int CLOSE=1; //关闭状态
    private static final int STARTOPEN=2; //开始打开
    private static final int STARTCCLOSE=3; //开始关闭
    private static final int DRAG=4;  //进行拖拽状态

    private ViewDragHelper mViewDragHelper;
    private ViewGroup mMainViewGroup;
    private ViewGroup mBackgroundViewGroup;
    private View mShadowView;

    private int mMainMeasuredHeight;
    private int mMainMeasuredWidth;
    private int mBackgroundMeasuredHeight;
    private int mBackgroundMeasuredWidth;
    private int mShadowMeasuredHeight;
    private int mShadowMeasuredWidth;

    private boolean mDragEnable = false; //拖拽是否可用
    private boolean mAnimationComplete=true ;

    private int mPreState ; //保留历史状态
    private int mCurrentState = CLOSE; //当前的状态


    public  interface  OnDragStateListener{
        void onOpenState();
        void onCloseState();
        void onDragState();
        void onStartOpenState();
        void onStartCloseState();
    }



    public SmoothLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SmoothLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SmoothLayout(Context context) {
        super(context,null);
    }



    public boolean getDragEnable(){
        return mDragEnable;
    }

    private OnDragStateListener mOnDragStateListener;


    public void setDragEnable(boolean enable) {

    }

    private void init() {

        mViewDragHelper = ViewDragHelper.create(this,mViewDragHelperCallBack);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //绑定拦截事件
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    private ViewDragHelper.Callback mViewDragHelperCallBack = new ViewDragHelper.Callback() {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {

            if(child == mBackgroundViewGroup || child == mShadowView){
                return false;
            }
            return mDragEnable;
        }


        @Override
        public int getViewHorizontalDragRange(View child) {
            return 1000;
        }


        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {

            if (left > mBackgroundMeasuredWidth) {
                left = mBackgroundMeasuredWidth;
            } else if (left <= 0) {
                left = 0;
            }
            return left;
        }


        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return super.clampViewPositionVertical(child, top, dy);
        }


        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {

            mShadowView.offsetLeftAndRight(dx);
            //兼容低版本
            invalidate();
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            //进行将当前状态更新并且执行相关的监听回调
            dispatchHandlerListener();

        }


        //松手拖拽的回调
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {

            if(releasedChild == mMainViewGroup){

                if(mMainViewGroup.getLeft() == 0 || mMainViewGroup.getLeft() == mBackgroundMeasuredWidth){
                    return ;
                }else if(mMainViewGroup.getLeft()> 0 && mMainViewGroup.getLeft()< mBackgroundMeasuredWidth){

                    if(xvel < 0){
                        //进行左滑趋势，进行关闭平滑移动
                        close();
                    }else if(xvel > 0){
                        //进行右滑趋势，进行打开平滑移动
                        open();
                    }else{
                        if(mMainViewGroup.getLeft() > mBackgroundMeasuredWidth/2){
                            open();
                        }else{
                            close();
                        }
                    }
                }
            }
            super.onViewReleased(releasedChild, xvel, yvel);
        }

    };


    public void open() {
        //同步处理
        synchronized (this){
            animationForOpen();
        }
    }

    public void close() {
        //同步处理
        synchronized (this){
            animationForClose();
        }
    }


    //打开动画
    private void animationForOpen() {

            if( mAnimationComplete && mViewDragHelper.smoothSlideViewTo(mMainViewGroup,mBackgroundMeasuredWidth,0) ){
                //请求绘画，只能绘制一帧
                ViewCompat.postInvalidateOnAnimation(this);
                mDragEnable = true;
                mAnimationComplete= false;
            }
    }

    //关闭动画
    private void animationForClose() {

        if( mAnimationComplete && mViewDragHelper.smoothSlideViewTo(mMainViewGroup,0,0) )
        {
            ViewCompat.postInvalidateOnAnimation(this);
            mDragEnable = false;
            mAnimationComplete= false;
        }
    }



     //这个方法在重新绘画的时候会高频调用
    @Override
    public void computeScroll() {
        super.computeScroll();
        if(mViewDragHelper.continueSettling(true)){
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }





    public void setOnDragStateListener(OnDragStateListener onDragStateListener){
        this.mOnDragStateListener = onDragStateListener;
    }


    //执行监听器的回调
    private  void dispatchHandlerListener(){
        mPreState = mCurrentState;
        mCurrentState = updateCurrentState();

        switch (mCurrentState){
            case OPEN :
                if(mOnDragStateListener != null){
                    mOnDragStateListener.onOpenState();
                }
                break;
            case CLOSE:
                if(mOnDragStateListener != null){
                    mOnDragStateListener.onCloseState();
                }
                break;
            case STARTOPEN:
                if(mOnDragStateListener != null){
                    mOnDragStateListener.onStartOpenState();
                }
                break;
            case STARTCCLOSE:
                if(mOnDragStateListener != null){
                    mOnDragStateListener.onStartCloseState();
                }
                break;
            case DRAG:
                if(mOnDragStateListener != null){
                    mOnDragStateListener.onDragState();
                }
        }
    }


    private int updateCurrentState() {

        if(mMainViewGroup.getLeft() == 0){
            mDragEnable= false;
            mAnimationComplete = true;
            return CLOSE;
        }else if(mMainViewGroup.getLeft() == mBackgroundMeasuredWidth){
            mDragEnable= true;
            mAnimationComplete = true;
            return OPEN;
        }else if(mPreState == OPEN && mMainViewGroup.getLeft()>0 && mMainViewGroup.getLeft()<mBackgroundMeasuredWidth){
            return STARTCCLOSE;
        }else if(mPreState == CLOSE && mMainViewGroup.getLeft()>0 && mMainViewGroup.getLeft()<mBackgroundMeasuredWidth){
            return STARTOPEN;
        }else{
            return DRAG;
        }

    }




    @Override
    protected void onFinishInflate() {

        if(getChildCount() != 3){
             throw new RuntimeException("child must three ");
        }
        mBackgroundViewGroup = (ViewGroup) getChildAt(0);
        mMainViewGroup = (ViewGroup) getChildAt(1);
        mShadowView = (View) getChildAt(2);

        super.onFinishInflate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mMainMeasuredHeight = mMainViewGroup.getMeasuredHeight();
        mMainMeasuredWidth = mMainViewGroup.getMeasuredWidth();
        mBackgroundMeasuredHeight = mBackgroundViewGroup.getMeasuredHeight();
        mBackgroundMeasuredWidth = mBackgroundViewGroup.getMeasuredWidth();
        mShadowMeasuredHeight = mShadowView.getMeasuredHeight();
        mShadowMeasuredWidth = mShadowView.getMeasuredWidth();
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mMainViewGroup.layout(0,0,mMainMeasuredWidth,mMainMeasuredHeight);
        mBackgroundViewGroup.layout(0, 0,mBackgroundMeasuredWidth,mBackgroundMeasuredHeight);
        mShadowView.layout(-mShadowMeasuredWidth,0,0,mShadowMeasuredHeight);
    }

}
