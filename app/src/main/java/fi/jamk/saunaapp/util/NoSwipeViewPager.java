package fi.jamk.saunaapp.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NoSwipeViewPager extends android.support.v4.view.ViewPager {
    public NoSwipeViewPager(Context context) {
        super(context);
    }

    public NoSwipeViewPager(Context context, AttributeSet attr) {
        super(context, attr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return false;
    }
}
