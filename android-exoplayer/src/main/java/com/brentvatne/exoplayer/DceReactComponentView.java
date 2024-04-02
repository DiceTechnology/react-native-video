package com.brentvatne.exoplayer;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactRootView;
import com.facebook.react.views.view.ReactViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DceReactComponentView extends LinearLayout {
    public interface Callback {
        void onSizeChanged(int width, int height);

        void onFocusChanged(boolean gainFocus);
    }

    private final List<View> focusableChildList = new ArrayList<>();
    private final List<View> nextFocusUpViewList = new ArrayList<>();
    private Callback callback;
    private boolean hasFocused = false;
    private final ViewTreeObserver.OnGlobalFocusChangeListener onGlobalFocusChangeListener = (oldFocus, newFocus) -> {
        if (callback == null) return;
        if (!hasFocused && newFocus instanceof ReactViewGroup) {
            hasFocused = true;
            callback.onFocusChanged(true);
        } else if (hasFocused && !(newFocus instanceof ReactViewGroup)) {
            hasFocused = false;
            callback.onFocusChanged(false);
        }
    };

    public DceReactComponentView(Context context) {
        super(context);
    }

    public DceReactComponentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCallbackListener(Callback callback) {
        this.callback = callback;
    }

    public void setNextFocusUpViews(View... views) {
        nextFocusUpViewList.clear();
        nextFocusUpViewList.addAll(Arrays.asList(views));
    }

    public void addComponent(String component) {
        removeAllViews();
        focusableChildList.clear();
        setVisibility(View.VISIBLE);
        ReactRootView reactRootView = new ReactRootView(getContext());
        reactRootView.startReactApplication(((ReactApplication) getContext().getApplicationContext()).getReactNativeHost().getReactInstanceManager(), component, null);
        addView(reactRootView, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalFocusChangeListener(onGlobalFocusChangeListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        focusableChildList.clear();
        setCallbackListener(null);
        getViewTreeObserver().removeOnGlobalFocusChangeListener(onGlobalFocusChangeListener);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (callback != null && w > 0 && h > 0) {
            callback.onSizeChanged(w, h);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        findFocusableChild(this);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus && !focusableChildList.isEmpty()) {
            focusableChildList.get(0).requestFocus();
        }
    }

    @Override
    public View focusSearch(View focused, int direction) {
        int index = focusableChildList.indexOf(focused);
        if (index != -1) {
            if (getOrientation() == LinearLayout.HORIZONTAL) {
                if (direction == View.FOCUS_RIGHT && index == focusableChildList.size() - 1) {
                    return focused;
                } else if (direction == View.FOCUS_LEFT && index == 0) {
                    return focused;
                } else if (direction == View.FOCUS_UP) {
                    for (View nextView : nextFocusUpViewList) {
                        if (nextView.getVisibility() == View.VISIBLE) {
                            return nextView;
                        }
                    }
                }
            } else if (getOrientation() == LinearLayout.VERTICAL) {
                if (direction == View.FOCUS_UP && index == 0) {
                    return focused;
                } else if (direction == View.FOCUS_DOWN && index == focusableChildList.size() - 1) {
                    return focused;
                }
            }
        }
        return super.focusSearch(focused, direction);
    }

    private void findFocusableChild(ViewGroup parentView) {
        if (!focusableChildList.isEmpty()) return;
        if (parentView == null || parentView.getChildCount() == 0) return;
        for (int i = 0; i < parentView.getChildCount(); i++) {
            View childView = parentView.getChildAt(i);
            if (childView.isFocusable()) focusableChildList.add(childView);
            if (childView instanceof ViewGroup) {
                findFocusableChild((ViewGroup) childView);
            }
        }
    }
}
