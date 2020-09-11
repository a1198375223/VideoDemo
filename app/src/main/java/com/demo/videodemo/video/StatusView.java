package com.demo.videodemo.video;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.demo.videodemo.R;

public class StatusView extends LinearLayout {
    private TextView tvMessage;
    private TextView btnAction;
    private float downX;
    private float downY;

    public StatusView(Context context) {
        this(context, null);
    }

    public StatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_status_view, this);
        tvMessage = findViewById(R.id.message);
        btnAction = findViewById(R.id.status_btn);

        setBackgroundResource(android.R.color.black);
        setClickable(true);
    }

    public void setMessage(String msg) {
        if (tvMessage != null) {
            tvMessage.setText(msg);
        }
    }

    public void setButtonTextAndAction(String text, OnClickListener listener) {
        if (btnAction != null) {
            btnAction.setText(text);
            btnAction.setOnClickListener(listener);
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
                // 请求上级不拦截该事件
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                float absDeltaX = Math.abs(ev.getX() - downX);
                float absDeltaY = Math.abs(ev.getY() - downY);

                /*
                  ViewConfiguration
                  getScaledTouchSlop()                 : 获取被视为滑动的最小距离
                  getScaledMinimumFlingVelocity()      : 获得允许执行fling的最小速度值
                  getScaledMaximumFlingVelocity()      : 获取允许执行fling的最大速度值
                  hasPermanentMenuKey()                : 即判断设备是否有返回、主页、菜单键等实体按键（非虚拟按键）
                  getTapTimeout()                      : 获得敲击超时时间，如果在此时间内没有移动，则认为是一次点击
                  getDoubleTapTimeout()                : 双击间隔时间，在该时间内被认为是双击
                  getLongPressTimeout()                : 长按时间，超过此时间就认为是长按
                  getKeyRepeatTimeout()                : 重复按键间隔时间
                 */
                if (absDeltaX > ViewConfiguration.get(getContext()).getScaledTouchSlop() ||
                        absDeltaY > ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
}