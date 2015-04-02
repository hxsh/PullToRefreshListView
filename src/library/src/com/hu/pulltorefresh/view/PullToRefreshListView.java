package com.hu.pulltorefresh.view;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;

import com.hu.pulltorefresh.R;

public class PullToRefreshListView extends ListView {

	public static final int MODE_NORMAL = 0;
	public static final int MODE_REFRESH = 1;
	private static final int MOTION_NORMAL = 0;
	private static final int MOTION_DOWN = 1;
	private static final int MOTION_MOVE_REFRESH = 2;
	private static final int MOTION_MOVE_SCROLL_ITEM = 3;
	
	private ImageView mRefreshIV;
	
	private int mActionDownY; // 按下屏幕的位置
	private int mPreMoveY; // 上一次移动屏幕时的位置
	private boolean mCanRefresh;
	private static int MAX_VALUE = 0;
	private boolean mIsToUp;
	private boolean mIsToUped;
	private boolean mIsRefreshReady;
	private boolean mIsAddedRefreshIV;
	private boolean mIsRefreshing;
	private int mRefreshIVWidth;
	private int mRefreshIVHeight;
	private int mRefreshIVLeft;
	private int mRefreshIVTop;
	private int mRefreshIVRight;
	private int mRefreshIVBottom;
	private int mMode = MODE_NORMAL;
	private int mPreMotion;

	public PullToRefreshListView(Context context) {
		this(context, null);
	}
	
	public PullToRefreshListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public PullToRefreshListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}
	
	private void initView(Context context){
		MAX_VALUE = dp2px(context, 200);
		mRefreshIVWidth = mRefreshIVHeight = dp2px(context, 24);
		int screenWidth = getScreenWidth();
		mRefreshIVLeft = screenWidth/2 - mRefreshIVWidth/2;
		mRefreshIVRight = mRefreshIVLeft + mRefreshIVWidth;
		
		mRefreshIV = new ImageView(context);
		LayoutParams params = new LayoutParams(mRefreshIVWidth, mRefreshIVHeight);
		mRefreshIV.setLayoutParams(params);
//		mIV.setPadding(width/4, width/4, width/4, width/4);
		mRefreshIV.setScaleType(ScaleType.FIT_CENTER);
		mRefreshIV.setImageResource(R.drawable.ic_refresh);
//		mIV.setImageResource(R.drawable.ic_refresh);
	}
	
	public static int dp2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
	
	private int getScreenWidth(){
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		Point point = new Point();
		wm.getDefaultDisplay().getSize(point);
		return point.x;
	}
	
	public void setMode(int mode){
		if(mode > -1 && mode < 2){
			mMode = mode;
		}
	}
	
	public void onRefreshComplete(){
		mIsRefreshing = false;
		resetData();
	}
	
	public boolean isRefreshing(){
		return mIsRefreshing;
	}
	
	private void resetData(){
		mRefreshIV.clearAnimation();
		mPreMoveY = 0;
		mRefreshIVTop = - mRefreshIVHeight;
		mRefreshIVBottom = 0;
		mIsToUp = false;
		mIsToUped = false;
		mIsRefreshReady = false;
		mRefreshIV.layout(mRefreshIVLeft, mRefreshIVTop, mRefreshIVRight, mRefreshIVBottom);
		if(!mIsAddedRefreshIV){
			mIsAddedRefreshIV = true;
			((ViewGroup)getParent()).addView(mRefreshIV);
		}
	}
	
	/*private AlphaAnimation getIVAlphaAnim(){
		mRefreshIV.clearAnimation();
		AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(200);
		return animation;
	}*/
	
	private boolean isViewAtTop(){
		View view = getChildAt(0);
		int pos = getPositionForView(view);
		return pos == 0 && view.getTop() == 0;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(MODE_REFRESH == mMode && !isRefreshing()){
				prePullToRefreshView(ev.getX(), ev.getY());
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if(mMode == MODE_REFRESH && mCanRefresh && !isRefreshing()){
				if(mPreMotion == MOTION_DOWN){
					mPreMotion = ((int) ev.getY() > mActionDownY) ? MOTION_MOVE_REFRESH : MOTION_MOVE_SCROLL_ITEM;
					if(mPreMotion == MOTION_MOVE_REFRESH){
						resetData();
					}
				}
				if(mPreMotion == MOTION_MOVE_REFRESH){
					pullToRefreshView(ev.getY());
					if(mIsToUped){
						return true;
					}
				}
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if(mPreMotion == MOTION_MOVE_REFRESH){
				releaseRefreshView();
				return true;
			}
			break;
			
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}
	
	private void prePullToRefreshView(float x, float y){
		mCanRefresh = false;
		mPreMotion = MOTION_DOWN;
		mActionDownY = (int) y;
		mCanRefresh = isViewAtTop();
	}
	
	private void pullToRefreshView(float positionY){
		int currY = (int) positionY;
		boolean toDown = currY > mPreMoveY;
		int distance = currY - mActionDownY - (mRefreshIVHeight*2);
		if(distance > MAX_VALUE){
			mActionDownY = currY - MAX_VALUE - (mRefreshIVHeight*2);
		}else if(distance + (mRefreshIVHeight*2) >= 0){
			if(distance > MAX_VALUE/2){
				if(!mIsRefreshReady){
					mIsRefreshReady = true;
//					mIV.setImageResource(R.drawable.ic_refresh);
//					mIV.startAnimation(getIVAlphaAnim());
				}
				float rotation = 1.0f * 360 * 2 * distance / MAX_VALUE;
				mRefreshIV.setRotation(rotation);
			}else{
				if(mIsRefreshReady){
					mIsToUp = true;
					mIsRefreshReady = false;
//					mIV.setImageResource(R.drawable.ic_refresh);
//					mIV.setRotation(180f);
//					mIV.startAnimation(getIVAlphaAnim());
				}else{
					if(toDown){
						if(mIsToUp){
//							mIV.setRotation(180f);
						}
					}else{
						if(!mIsToUp){
//							mIV.setRotation(180f);
						}
					}
				}
			}
			mRefreshIVTop = distance / 2 - 1;
			mRefreshIVBottom = mRefreshIVTop + mRefreshIVHeight;
			mRefreshIV.layout(mRefreshIVLeft, mRefreshIVTop, mRefreshIVRight, mRefreshIVBottom);
		}else{
			mActionDownY = currY;
		}
		mIsToUp = !toDown;
		if(mIsToUp){
			mIsToUped = true;
		}
		mPreMoveY = currY;
	}
	
	private void releaseRefreshView(){
		mPreMotion = MOTION_NORMAL;
		if(mIsRefreshReady){
			mRefreshIV.layout(mRefreshIVLeft, mRefreshIVHeight, mRefreshIVRight, mRefreshIVHeight * 2);
			mRefreshIV.setRotation(0);
			RotateAnimation rotateAni = new RotateAnimation(0, 180, 
					Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			rotateAni.setInterpolator(new LinearInterpolator());
			rotateAni.setDuration(400);
			rotateAni.setRepeatCount(-1);
			mRefreshIV.startAnimation(rotateAni);
			if(mRefreshListener != null){
				mIsRefreshing = true;
				mRefreshListener.onRefresh();
			}
		}else{
			mRefreshIV.layout(mRefreshIVLeft, mRefreshIVTop, mRefreshIVRight, mRefreshIVBottom);
			TranslateAnimation animation = new TranslateAnimation(0, 0, 0, - mRefreshIVTop - mRefreshIVHeight);
			animation.setDuration(200);
			animation.setFillAfter(true);
			mRefreshIV.startAnimation(animation);
		}
	}
	
	private OnRefreshListener mRefreshListener;
	
	public void setOnRefreshListener(OnRefreshListener refreshListener){
		mRefreshListener = refreshListener;
	}
	
	public interface OnRefreshListener{
		void onRefresh();
	}

}
