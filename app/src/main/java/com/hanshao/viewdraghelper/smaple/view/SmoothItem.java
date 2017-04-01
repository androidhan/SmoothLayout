package com.hanshao.viewdraghelper.smaple.view;


import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


public class SmoothItem extends FrameLayout {

	private ViewDragHelper mDrag;
	private ViewGroup mainView;
	private ViewGroup rightView;
	private int mainWidth;
	private int rightWidth;
	private int mainHeight;
	private int rightHeight;


	public static int OPEN = 0;    //此状态为打开状态
	public static int CLOSE = 1;    // 此状态为关闭状态
	public static int DRAG = 2;        //此状态为拖拽状态
	public static int STARTOPEN = 3;    //此状态为要开始打开状态
	public static int STARTCLOSE = 4; //此状态为开始关闭状态

	private int currentState = CLOSE; //默认关闭状态


	public interface OnStateListener {
		void openState(SmoothItem item);

		void closeState(SmoothItem item);

		void dragState(SmoothItem item);

		void startOpen(SmoothItem item);

		void startClose(SmoothItem item);
	}

	//状态监听器
	private OnStateListener listenr;

	public void setOnStateListener(OnStateListener listenr) {
		this.listenr = listenr;
	}

	public SmoothItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}


	public SmoothItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SmoothItem(Context context) {
		super(context);
		init();
	}


	private MyCallBack mCallBack = new MyCallBack();
	private float startX;
	private float startY;
	private int perState;

	private void init() {
		mDrag = ViewDragHelper.create(this, mCallBack);
	}


	class MyCallBack extends Callback {

		public boolean tryCaptureView(View arg0, int arg1) {
			return true;
		}

		@Override
		public int getViewHorizontalDragRange(View child) {
			return 1000;
		}

		@Override
		public int clampViewPositionHorizontal(View child, int left, int dx) {

			if (child == rightView) {

				//右view拖拽
				if (mainView.getLeft() + dx > 0) {
					left = mainWidth;
				} else if (mainView.getLeft() + dx < -rightWidth) {
					left = mainWidth - rightWidth;
				}
			} else {
				//左view拖拽
				if (rightView.getLeft() + dx > mainWidth) {
					left = 0;
				} else if (rightView.getLeft() + dx < mainWidth - rightWidth) {
					left = -rightWidth;
				}
			}

			return left;
		}


		@Override
		public void onViewPositionChanged(View changedView, int left, int top,
										  int dx, int dy) {

			if (changedView == rightView) {
				mainView.offsetLeftAndRight(dx);
			} else {
				rightView.offsetLeftAndRight(dx);
			}

			invalidate();

			//进行将当前状态更新并且执行相关的监听回调
			dispatchHandleListener();


			super.onViewPositionChanged(changedView, left, top, dx, dy);
		}


		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {

			if (mainView.getLeft() == 0 || mainView.getLeft() == -rightWidth) {
				return;
			}

			if (mainView.getLeft() > -rightWidth / 2 && xvel == 0) {
				closeAnimation();
			} else if (xvel > 0) {
				closeAnimation();
			} else {
				openAnimation();
			}

			super.onViewReleased(releasedChild, xvel, yvel);
		}

	}


	@Override
	public void computeScroll() {
		super.computeScroll();

		if (mDrag.continueSettling(true)) {
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}


	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mainView = (ViewGroup) getChildAt(0);
		rightView = (ViewGroup) getChildAt(1);
	}


	public void closeAnimation() {

		if (mDrag.smoothSlideViewTo(mainView, 0, 0)) {
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	public void openAnimation() {
		if (mDrag.smoothSlideViewTo(mainView, -rightWidth, 0)) {
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}


	public void dispatchHandleListener() {
		perState = currentState;
		currentState = upCurrentState();

		if (currentState == OPEN) {
			if (listenr != null) {
				listenr.openState(this);
			}
		} else if (currentState == CLOSE) {
			if (listenr != null) {
				listenr.closeState(this);
			}
		} else if (currentState == STARTOPEN) {
			if (listenr != null) {
				listenr.startOpen(this);
			}
		} else if (currentState == STARTCLOSE) {
			if (listenr != null) {
				listenr.startClose(this);
			}
		} else {
			if (listenr != null) {
				listenr.dragState(this);
			}
		}


	}


	private int upCurrentState() {

		if (mainView.getLeft() < 0 && mainView.getLeft() > -rightWidth) {
			if (perState == OPEN) {
				return STARTCLOSE;
			} else if (perState == CLOSE) {
				return STARTOPEN;
			}
		} else if (mainView.getLeft() == 0) {
			return CLOSE;
		} else if (mainView.getLeft() == -rightWidth) {
			return OPEN;
		}
		return DRAG;
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		mainWidth = mainView.getMeasuredWidth();
		mainHeight = mainView.getMeasuredHeight();
		rightWidth = rightView.getMeasuredWidth();
		rightHeight = rightView.getMeasuredHeight();
	}


	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
							int bottom) {

		mainView.layout(0, 0, mainWidth, mainHeight);
		rightView.layout(mainWidth, 0, mainWidth + rightWidth, rightHeight);
	}

	//处理当前状态的回调
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				startX = (float) event.getRawX();
				startY = (float) event.getRawY();

				getParent().requestDisallowInterceptTouchEvent(true);

				break;
			case MotionEvent.ACTION_MOVE:
				float endY = (float) event.getRawY();
				float endX = (float) event.getRawX();

				if (mainView.getLeft() != 0) {
					//当前item已被滑动过，请求不拦截
					getParent().requestDisallowInterceptTouchEvent(true);
					break;
				}

				if (Math.abs(endY - startY) < Math.abs(endX - startX)) {
					//趋势x轴滑动
					if (endX - startX > 0) {
						//右滑
						getParent().requestDisallowInterceptTouchEvent(false);
					} else {
						getParent().requestDisallowInterceptTouchEvent(true);
					}
				} else {

					//趋势y轴滑动
					getParent().requestDisallowInterceptTouchEvent(false);
				}

				//保留上一次的x,y，用于判断滑动趋势
				startX = endX;
				startY = endY;
				break;
			case MotionEvent.ACTION_UP:

				if (mainView.getLeft() == 0) {
					getParent().requestDisallowInterceptTouchEvent(false);
				} else {
					getParent().requestDisallowInterceptTouchEvent(true);
				}
			default:
		}
		return super.dispatchTouchEvent(event);
	}


	public void closeRight() {
		synchronized (this) {
			closeAnimation();
		}
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {

		try {
			mDrag.processTouchEvent(event);
		} catch (Exception e) {
		}
		return true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return mDrag.shouldInterceptTouchEvent(ev);
	}
}
