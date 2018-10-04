package xyz.bboylin.universialtoast;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;

import xyz.bboylin.universaltoast.R;
import xyz.bboylin.universialtoast.UniversalToast.Duration;
import xyz.bboylin.universialtoast.UniversalToast.Type;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static xyz.bboylin.universialtoast.UniversalToast.CLICKABLE;
import static xyz.bboylin.universialtoast.UniversalToast.EMPHASIZE;
import static xyz.bboylin.universialtoast.UniversalToast.UNIVERSAL;

/**
 * 自定义window实现的toast，无notification权限或者是点击类型时强制采用这种
 *
 * @author lin
 */

public class CustomToast implements IToast {
    private static final String TAG = "UniversalToast";
    private static final int NO_LEFT_ICON = 0;
    private static final int TIME_LONG = 3500;
    private static final int TIME_SHORT = 2000;

    private WindowManager.LayoutParams mParams;
    private WindowManager mWindowManager;
    private Context mContext;
    private View mView;
    private int mDuration;
    private static Handler sHandler = new Handler(Looper.getMainLooper());
    private Runnable mShowRunnable;
    private Runnable mCancelRunnable;
    private View.OnClickListener mListener = null;
    @UniversalToast.Type
    private final int mType;
    @DrawableRes
    private int mLeftIconRes = NO_LEFT_ICON;
    @Nullable
    private Uri mLeftGifUri;

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
        mContext = context;
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
        mCancelRunnable = new Runnable() {
            @Override
            public void run() {
                // activity正在finish的时候不要remove
                if (!(mContext != null && mContext instanceof Activity && ((Activity) mContext).isFinishing())) {
                    mWindowManager.removeViewImmediate(mView);
                }
                mParams = null;
                mWindowManager = null;
                mView = null;
                mListener = null;
            }
        };
    }

    public static CustomToast makeText(@NonNull Context context, @NonNull String text, @Duration int duration) {
        return makeText(context, text, duration, UNIVERSAL);
    }

    public static CustomToast makeText(@NonNull Context context, @NonNull String text, @Duration int duration, @Type
            int type) {
        return new CustomToast(context, text, duration, type);
    }

    @Override
    public IToast setDuration(@Duration int duration) {
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
    public IToast setLeftIconRes(@DrawableRes int resId) {
        mLeftIconRes = resId;
        return this;
    }

    @Override
    public IToast setLeftGifUri(@NonNull Uri leftGifUri) {
        mLeftGifUri = leftGifUri;
        return this;
    }

    @Override
    public IToast setAnimations(@StyleRes int animations) {
        mParams.windowAnimations = animations;
        return this;
    }

    @Override
    public IToast setColor(@ColorRes int colorRes) {
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
    public IToast setText(@StringRes int resId) {
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
        if (mShowRunnable != null) {
            sHandler.removeCallbacksAndMessages(null);
        }
        mShowRunnable = new Runnable() {
            @Override
            public void run() {
                if (mType == CLICKABLE && mListener == null) {
                    Log.e(TAG, "the listener of clickable toast is null,have you called method:setClickCallback?");
                    return;
                }
                if (mView != null && mView.getParent() instanceof ViewGroup) {
                    ((ViewGroup) mView.getParent()).removeView(mView);
                }
                SimpleDraweeView draweeView = mView.findViewById(R.id.icon);
                if (mLeftGifUri != null) {
                    DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                            .setAutoPlayAnimations(true) // 自动播放动画
                            .setUri(mLeftGifUri)
                            .build();
                    draweeView.setController(draweeController);
                    draweeView.setVisibility(View.VISIBLE);
                } else if (mLeftIconRes != NO_LEFT_ICON) {
                    draweeView.setActualImageResource(mLeftIconRes);
                    draweeView.setVisibility(View.VISIBLE);
                }
                mWindowManager.addView(mView, mParams);
                Log.d(TAG, "addView");
                sHandler.postDelayed(mCancelRunnable, mDuration);
            }
        };
        sHandler.post(mShowRunnable);
    }

    @Override
    public void cancel() {
        if (sHandler != null) {
            sHandler.post(new Runnable() {
                @Override
                public void run() {
                    // activity正在finish的时候不要remove
                    if (!(mContext != null && mContext instanceof Activity && ((Activity) mContext).isFinishing())) {
                        mWindowManager.removeViewImmediate(mView);
                    }
                    mParams = null;
                    mWindowManager = null;
                    mView = null;
                    mListener = null;
                }
            });
            sHandler.removeCallbacks(mCancelRunnable);
        }
    }

    @Override
    public void showSuccess() {
        setLeftIconRes(mType == EMPHASIZE ? R.drawable.ic_check_circle_white_24dp : R.drawable.ic_done_white_24dp);
        show();
    }

    @Override
    public void showError() {
        setLeftIconRes(R.drawable.ic_clear_white_24dp);
        show();
    }

    @Override
    public void showWarning() {
        setLeftIconRes(R.drawable.ic_error_outline_white_24dp);
        show();
    }

    @Override
    public IToast setClickCallback(@NonNull String text, @NonNull View.OnClickListener listener) {
        return setClickCallback(text, R.drawable.ic_play_arrow_white_24dp, listener);
    }

    @Override
    public IToast setClickCallback(@NonNull String text, @DrawableRes int resId, @NonNull View.OnClickListener
            listener) {
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
