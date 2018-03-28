package xyz.bboylin.universialtoast;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import xyz.bboylin.universaltoast.R;
import xyz.bboylin.universialtoast.UniversalToast.Duration;
import xyz.bboylin.universialtoast.UniversalToast.Type;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.O;
import static xyz.bboylin.universialtoast.UniversalToast.CLICKABLE;
import static xyz.bboylin.universialtoast.UniversalToast.EMPHASIZE;
import static xyz.bboylin.universialtoast.UniversalToast.UNIVERSAL;

/**
 * 自定义window实现的toast，无notification权限或者是点击类型时强制采用这种
 *
 * @author lin
 */

public class CustomToast implements IToast {
    private WindowManager.LayoutParams mParams;
    private WindowManager mWindowManager;
    private View mView;
    private int mDuration;
    private Handler mHandler;
    private View.OnClickListener mListener = null;
    @UniversalToast.Type
    private final int mType;
    private static final int TIME_LONG = 3500;
    private static final int TIME_SHORT = 2000;
    private static final String TAG = UniversalToast.class.getSimpleName();

    private CustomToast(@NonNull Context context, @NonNull String text, @Duration int duration, @Type int type) {
        mType = type;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int layoutId = R.layout.toast_universal;
        switch (type) {
            case UNIVERSAL:
                break;
            case EMPHASIZE:
                layoutId = R.layout.toast_emphasize;
                break;
            case CLICKABLE:
                layoutId = R.layout.toast_clickable;
                break;
            default:
                break;
        }
        mView = LayoutInflater.from(context).inflate(layoutId, null);
        ((TextView) mView.findViewById(R.id.text)).setText(text);
        mDuration = (duration == UniversalToast.LENGTH_LONG ? TIME_LONG : TIME_SHORT);
        mParams = new WindowManager.LayoutParams();
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.format = PixelFormat.TRANSLUCENT;
        mParams.windowAnimations = android.R.style.Animation_Toast;
        mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        mParams.setTitle("Toast");
        mParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING;
        mHandler = new Handler();
    }

    public static CustomToast makeText(@NonNull Context context, @NonNull String text, @Duration int duration) {
        return makeText(context, text, duration, UNIVERSAL);
    }

    public static CustomToast makeText(@NonNull Context context, @NonNull String text, @Duration int duration, @Type int type) {
        return new CustomToast(context, text, duration, type);
    }

    @Override
    public IToast setDuration(int duration) {
        if (duration == UniversalToast.LENGTH_SHORT) {
            mDuration = TIME_SHORT;
        } else if (duration == UniversalToast.LENGTH_LONG) {
            mDuration = TIME_LONG;
        } else {
            mDuration = duration;
        }
        return this;
    }

    @Override
    public IToast setIcon(int resId) {
        ImageView imageView = mView.findViewById(R.id.icon);
        imageView.setBackgroundResource(resId);
        imageView.setVisibility(View.VISIBLE);
        return this;
    }

    @Override
    public IToast setAnimations(int animations) {
        mParams.windowAnimations = animations;
        return this;
    }

    @Override
    public IToast setColor(int colorRes) {
        GradientDrawable drawable = (GradientDrawable) mView.getBackground();
        drawable.setColor(mView.getContext().getResources().getColor(colorRes));
        return this;
    }

    @TargetApi(JELLY_BEAN)
    @Override
    public IToast setBackground(Drawable drawable) {
        mView.setBackground(drawable);
        return this;
    }

    @TargetApi(JELLY_BEAN_MR1)
    @Override
    public IToast setGravity(int gravity, int xOffset, int yOffset) {
        final Configuration config = mView.getContext().getResources().getConfiguration();
        final int g = Gravity.getAbsoluteGravity(gravity, config.getLayoutDirection());
        mParams.gravity = g;
        if ((g & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.FILL_HORIZONTAL) {
            mParams.horizontalWeight = 1.0f;
        }
        if ((g & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.FILL_VERTICAL) {
            mParams.verticalWeight = 1.0f;
        }
        mParams.x = xOffset;
        mParams.y = yOffset;
        return this;
    }

    @Override
    public IToast setMargin(float horizontalMargin, float verticalMargin) {
        mParams.verticalMargin = verticalMargin;
        mParams.horizontalMargin = horizontalMargin;
        return this;
    }

    @Override
    public IToast setText(int resId) {
        ((TextView) mView.findViewById(R.id.text)).setText(resId);
        return this;
    }

    @Override
    public IToast setText(@NonNull CharSequence charSequence) {
        ((TextView) mView.findViewById(R.id.text)).setText(charSequence);
        return this;
    }

    @Override
    public void show() {
        if (mType == CLICKABLE && mListener == null) {
            Log.e(TAG, "the listener of clickable toast is null,have you called method:setClickCallBack?");
            return;
        }
        if (mView.getParent() != null) {
            mWindowManager.removeView(mView);
        }
        Log.e(TAG, "addview");
        mWindowManager.addView(mView, mParams);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cancel();
            }
        }, mDuration);
    }

    @Override
    public void cancel() {
        mWindowManager.removeView(mView);
        mParams = null;
        mWindowManager = null;
        mView = null;
        mHandler = null;
        mListener = null;
    }

    @Override
    public void showSuccess() {
        setIcon(mType == EMPHASIZE ? R.drawable.ic_check_circle_white_24dp : R.drawable.ic_done_white_24dp);
        show();
    }

    @Override
    public void showError() {
        setIcon(R.drawable.ic_clear_white_24dp);
        show();
    }

    @Override
    public void showWarning() {
        setIcon(R.drawable.ic_error_outline_white_24dp);
        show();
    }

    @Override
    public IToast setClickCallBack(@NonNull String text, @NonNull View.OnClickListener listener) {
        return setClickCallBack(text, R.drawable.ic_play_arrow_white_24dp, listener);
    }

    @Override
    public IToast setClickCallBack(@NonNull String text, @DrawableRes int resId, @NonNull View.OnClickListener listener) {
        if (mType != CLICKABLE) {
            Log.d(TAG, "only clickable toast has click callback!!!");
            return this;
        }
        mListener = listener;
        LinearLayout layout = mView.findViewById(R.id.btn);
        layout.setVisibility(View.VISIBLE);
        layout.setOnClickListener(listener);
        TextView textView = layout.findViewById(R.id.btn_text);
        textView.setText(text);
        ImageView imageView = layout.findViewById(R.id.btn_icon);
        imageView.setBackgroundResource(resId);
        return this;
    }
}
