package com.hotwire.hotels.hwcclib.animation.drawable;

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

/**
 * Modified implementation of AnimatedScaleDrawable from https://github.com/slightfoot/android-animatedscaledrawable
 */
public class AnimatedScaleDrawable extends Drawable implements Drawable.Callback, Animatable {
    public static final String TAG = AnimatedScaleDrawable.class.getSimpleName();

    private static final int DEFAULT_DURATION_MS = 200;
    private static final float DEFAULT_FROM_SCALE_X = 1.0f;
    private static final float DEFAULT_TO_SCALE_X = 0.0f;

    private static final float DEFAULT_FROM_SCALE_Y = 1.0f;
    private static final float DEFAULT_TO_SCALE_Y = 1.0f;

    private AnimationScaleState mState;
    private Drawable mTransitionDrawable;
    private boolean mMutated;
    private boolean mIsCardSwapped;

    private final Rect mTmpRect = new Rect();

    public AnimatedScaleDrawable() {
        this(null, null);
    }

    public AnimatedScaleDrawable(Drawable drawable) {
        this(null, null);
        setDrawable(drawable);
    }

    private AnimatedScaleDrawable(AnimationScaleState pulsingState, Resources res) {
        mState = new AnimationScaleState(pulsingState, this, res);
    }

    public void setDrawable(Drawable drawable) {

        if (mState.mDrawable != drawable) {
            if (mState.mDrawable != null) {

                mState.mDrawable.setCallback(null);
            }
            mState.mDrawable = drawable;
            if (mState.mDrawable != null) {
                mState.mDrawable.setVisible(isVisible(), true);
                mState.mDrawable.setState(getState());
                mState.mDrawable.setLevel(getLevel());
                mState.mDrawable.setBounds(getBounds());
                mState.mDrawable.setCallback(this);
            }
        }
    }

    public void startDrawableTransition(Drawable transitionDrawable) {

        if (null != transitionDrawable) {
            mTransitionDrawable = transitionDrawable;
            mTransitionDrawable.setCallback(this);
            start();
        }
    }

    public void setInterpolator(Context context, int resId) {
        setInterpolator(AnimationUtils.loadInterpolator(context, resId));
    }

    public void setInterpolator(Interpolator interpolator) {
        mState.mInterpolator = interpolator;
    }

    public void setDuration(int duration) {
        mState.mDuration = duration;
    }

    public void setFromScaleX(float fromScaleX) {
        mState.mFromScaleX = fromScaleX;
    }

    public void setToScaleX(float toScaleX) {
        mState.mToScaleX = toScaleX;
    }

    public void setFromScaleY(float fromScaleY) {
        mState.mFromScaleY = fromScaleY;
    }

    public void setToScaleY(float toScaleY) {
        mState.mToScaleY = toScaleY;
    }

    public void setUseBounds(boolean useBounds) {
        mState.mUseBounds = useBounds;
        onBoundsChange(getBounds());
    }

    public void setInvertTransformation(boolean invert) {
        mState.mInvert = invert;
    }

    public Interpolator getInterpolator() {
        return mState.mInterpolator;
    }

    public int getDuration() {
        return mState.mDuration;
    }

    public float getFromScaleX() {
        return mState.mFromScaleX;
    }

    public float getToScaleX() {
        return mState.mToScaleX;
    }

    public float getFromScaleY() {
        return mState.mFromScaleY;
    }

    public float getToScaleY() {
        return mState.mToScaleY;
    }

    public boolean isUsingBounds() {
        return mState.mUseBounds;
    }

    public boolean isInvertingTransformation() {
        return mState.mInvert;
    }

    @Override
    public void start() {
        if (mState.mAnimating) {
            return;
        }

        if (mState.mInterpolator == null) {
            mState.mInterpolator = new LinearInterpolator();
        }

        if (mState.mTransformation == null) {
            mState.mTransformation = new Transformation();
        }
        else {
            mState.mTransformation.clear();
        }

        if (mState.mAnimation == null) {
            mState.mAnimation = new AlphaAnimation(0.0f, 1.0f);
        }
        else {
            mState.mAnimation.reset();
        }

        mState.mAnimation.setDuration(mState.mDuration);
        mState.mAnimation.setInterpolator(mState.mInterpolator);
        mState.mAnimation.setStartTime(Animation.START_ON_FIRST_FRAME);
        mState.mAnimating = true;

        invalidateSelf();
    }

    @Override
    public boolean isRunning() {
        return mState.mAnimating;
    }

    @Override
    public void stop() {
        mState.mAnimating = false;
    }

    public void draw(Canvas canvas) {
        final AnimationScaleState st = mState;

        if (st.mDrawable == null) {
            return;
        }

        final Rect bounds = st.mUseBounds ? getBounds() : mTmpRect;

        int saveCount = canvas.save();

        canvas.scale(st.mScaleX, st.mScaleY,
                bounds.left + bounds.width() / 2,
                bounds.top + bounds.height() / 2);

        st.mDrawable.draw(canvas);
        canvas.restoreToCount(saveCount);

        if (st.mAnimating) {

            long animTime = AnimationUtils.currentAnimationTimeMillis();
            st.mAnimation.getTransformation(animTime, st.mTransformation);
            float transformation = st.mTransformation.getAlpha();

            if (st.mFromScaleX != 1.0f || st.mToScaleX != 1.0f) {
                st.mScaleX = st.mFromScaleX + ((st.mToScaleX - st.mFromScaleX) * transformation);
            }

            if (st.mFromScaleY != 1.0f || st.mToScaleY != 1.0f) {
                st.mScaleY = st.mFromScaleY + ((st.mToScaleY - st.mFromScaleY) * transformation);
            }
            invalidateSelf();

            if (transformation == 1.0f) {

                stop();

                if (!mIsCardSwapped) {
                    // mFromScaleX and mToScaleX need to be swapped since we have to reverse the scaling animation
                    swapScaleXvalues();
                    setDrawable(mTransitionDrawable);
                    start();
                    mIsCardSwapped = true;
                }
                else {
                    mIsCardSwapped = false;
                    //Reset the mFromScaleX and mToScaleX values
                    swapScaleXvalues();
                }

            }
        }
    }

    private void swapScaleXvalues() {
        float tmp;
        tmp = mState.mFromScaleX;
        mState.mFromScaleX = mState.mToScaleX;
        mState.mToScaleX = tmp;
    }

    @Override
    protected boolean onStateChange(int[] state) {
        boolean changed = false;
        if (mState.mDrawable != null) {
            changed |= mState.mDrawable.setState(state);
        }
        onBoundsChange(getBounds());
        return changed;
    }

    @Override
    protected boolean onLevelChange(int level) {
        if (mState.mDrawable != null) {
            mState.mDrawable.setLevel(level);
        }
        onBoundsChange(getBounds());
        return true;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        if (mState.mDrawable != null) {
            if (mState.mUseBounds) {
                mState.mDrawable.setBounds(bounds);
            }
            else {
                Gravity.apply(Gravity.CENTER, getIntrinsicWidth(),
                        getIntrinsicHeight(), bounds, mTmpRect);
                mState.mDrawable.setBounds(mTmpRect);
            }
        }
    }

    @Override
    public int getIntrinsicWidth() {
        if (mState.mDrawable != null) {
            return mState.mDrawable.getIntrinsicWidth();
        }
        else {
            return -1;
        }
    }

    @Override
    public int getIntrinsicHeight() {
        if (mState.mDrawable != null) {
            return mState.mDrawable.getIntrinsicHeight();
        }
        else {
            return -1;
        }
    }

    public Drawable getDrawable() {
        return mState.mDrawable;
    }

    @Override
    public int getChangingConfigurations() {
        int changing = super.getChangingConfigurations() | mState.mChangingConfigurations;
        if (mState.mDrawable != null) {
            changing |= mState.mDrawable.getChangingConfigurations();
        }
        return changing;
    }

    public void setAlpha(int alpha) {
        if (mState.mDrawable != null) {
            mState.mDrawable.setAlpha(alpha);
        }
    }

    public void setColorFilter(ColorFilter cf) {
        if (mState.mDrawable != null) {
            mState.mDrawable.setColorFilter(cf);
        }
    }

    public int getOpacity() {
        if (mState.mDrawable != null) {
            return mState.mDrawable.getOpacity();
        }
        else {
            return PixelFormat.TRANSLUCENT;
        }
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        final Callback callback = getCallbackCompat();

        if (callback != null) {
            callback.invalidateDrawable(this);
        }
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        final Callback callback = getCallbackCompat();

        if (callback != null) {
            callback.scheduleDrawable(this, what, when);
        }
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {

        final Callback callback = getCallbackCompat();

        if (callback != null) {
            callback.unscheduleDrawable(this, what);
        }
    }

    @SuppressLint("NewApi")
    private Callback getCallbackCompat() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            try {
                Field mCallback = getClass().getField("mCallback");
                mCallback.setAccessible(true);
                return (Callback) mCallback.get(this);
            }
            catch (IllegalArgumentException e) {
                return null;
            }
            catch (IllegalAccessException e) {
                return null;
            }
            catch (NoSuchFieldException e) {
                return null;
            }
        }
        else {
            return getCallback();
        }
    }

    @Override
    public boolean getPadding(Rect padding) {
        if (mState.mDrawable != null) {
            return mState.mDrawable.getPadding(padding);
        }
        else {
            padding.set(0, 0, 0, 0);
            return false;
        }
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        if (mState.mDrawable != null) {
            mState.mDrawable.setVisible(visible, restart);
        }
        return super.setVisible(visible, restart);
    }

    @Override
    public boolean isStateful() {
        return mState.mDrawable != null && mState.mDrawable.isStateful();
    }

    @Override
    public ConstantState getConstantState() {
        if (mState.canConstantState()) {
            mState.mChangingConfigurations = super.getChangingConfigurations();
            return mState;
        }
        return null;
    }

    @Override
    public AnimatedScaleDrawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mState = new AnimationScaleState(mState, this, null);
            mMutated = true;
        }
        return this;
    }

    static final class AnimationScaleState extends Drawable.ConstantState {
        Drawable mDrawable;

        int mChangingConfigurations;

        float mFromScaleX = DEFAULT_FROM_SCALE_X; // 1.0f
        float mToScaleX = DEFAULT_TO_SCALE_X;     // 0.0f
        float mFromScaleY = DEFAULT_FROM_SCALE_Y; // 1.0f
        float mToScaleY = DEFAULT_TO_SCALE_Y;     // 1.0f
        float mScaleX = 1.0f;
        float mScaleY = 1.0f;
        int mDuration = DEFAULT_DURATION_MS;      // 200ms
        boolean mUseBounds = true;
        boolean mInvert = false;
        boolean mAnimating = false;

        Interpolator mInterpolator;
        Transformation mTransformation;
        AlphaAnimation mAnimation;

        private boolean mCanConstantState;
        private boolean mCheckedConstantState;

        public AnimationScaleState(AnimationScaleState source, AnimatedScaleDrawable owner, Resources res) {
            if (source != null) {
                if (res != null) {
                    mDrawable = source.mDrawable.getConstantState().newDrawable(res);
                }
                else {
                    mDrawable = source.mDrawable.getConstantState().newDrawable();
                }
                mDrawable.setCallback(owner);
                mFromScaleX = source.mFromScaleX;
                mScaleX = source.mFromScaleX;

                mToScaleX = source.mToScaleX;
                mFromScaleY = source.mFromScaleY;

                mScaleY = source.mFromScaleY;
                mToScaleY = source.mToScaleY;

                mDuration = source.mDuration;
                mUseBounds = source.mUseBounds;
                mInvert = source.mInvert;
                mAnimating = false;
                mCanConstantState = true;
                mCheckedConstantState = true;
            }
        }

        @Override
        public Drawable newDrawable() {
            return new AnimatedScaleDrawable(this, null);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new AnimatedScaleDrawable(this, res);
        }

        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }

        public boolean canConstantState() {
            if (!mCheckedConstantState) {
                mCanConstantState = mDrawable.getConstantState() != null;
                mCheckedConstantState = true;
            }

            return mCanConstantState;
        }
    }
}
