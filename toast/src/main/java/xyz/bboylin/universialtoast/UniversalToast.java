package xyz.bboylin.universialtoast;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

import xyz.bboylin.universaltoast.R;

/**
 * Created by lin on 2018/1/9.
 */

public class UniversalToast {
    @NonNull
    private final Toast toast;
    @NonNull
    private final Context context;
    @NonNull
    private final String text;
    @Type
    private final int type;
    private static final String TAG = UniversalToast.class.getSimpleName();

    //通用和强调toast，用原生toast实现
    public static final int UNIVERSAL = 0;
    public static final int EMPHASIZE = 1;
    //可点击toast，用自定义window实现
    public static final int CLICKABLE = 2;

    public static final int LENGTH_LONG = Toast.LENGTH_LONG;
    public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
    private static final int TIME_LONG = 3500;
    private static final int TIME_SHORT = 2000;

    @IntDef({UNIVERSAL, EMPHASIZE, CLICKABLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    @IntDef({LENGTH_LONG, LENGTH_SHORT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {
    }

    private WindowManager.LayoutParams params;
    private WindowManager windowManager;
    private View view;
    private int duration;
    private Handler handler;
    private View.OnClickListener listener = null;

    private UniversalToast(@NonNull Context context, @NonNull Toast toast, @NonNull String text, @Type int type) {
        this.context = context;
        this.toast = toast;
        this.text = text;
        this.type = type;
        if (type == CLICKABLE) {
            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            view = toast.getView();
            duration = toast.getDuration() == UniversalToast.LENGTH_LONG ? TIME_LONG : TIME_SHORT;
            params = new WindowManager.LayoutParams();
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.format = PixelFormat.TRANSLUCENT;
            params.windowAnimations = android.R.style.Animation_Toast;
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
            params.setTitle("Toast");
            params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                    | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING;
            handler = new Handler();
        }
    }

    public static UniversalToast makeText(@NonNull Context context, @NonNull String text, @Duration int duration) {
        return makeText(context, text, duration, UNIVERSAL);
    }

    public static UniversalToast makeText(@NonNull Context context, @NonNull String text, @Duration int duration, @Type int type) {
        Toast toast = Toast.makeText(context, text, duration);
        int layoutId = R.layout.toast_universal;
        switch (type) {
            case UNIVERSAL:
                break;
            case EMPHASIZE:
                layoutId = R.layout.toast_emphasize;
                toast.setGravity(Gravity.CENTER, 0, 0);
                break;
            case CLICKABLE:
                layoutId = R.layout.toast_clickable;
                break;
            default:
                break;
        }
        View view = LayoutInflater.from(context).inflate(layoutId, null);
        ((TextView) view.findViewById(R.id.text)).setText(text);
        toast.setView(view);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && type != CLICKABLE) {
            setContext(view, new SafeToastContext(context));
        }
        return new UniversalToast(context, toast, text, type);
    }

    private static void setContext(@NonNull View view, @NonNull Context context) {
        try {
            Field field = View.class.getDeclaredField("mContext");
            field.setAccessible(true);
            field.set(view, context);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param duration it's not recommanded for you to
     *                 set duration as values except for 2000L,3500L.
     */
    @Deprecated
    public UniversalToast setDuration(int duration) {
        if (type != CLICKABLE) {
            toast.setDuration(duration);
        } else {
            this.duration = duration;
        }
        return this;
    }

    public UniversalToast setLeftIcon(@DrawableRes int resId) {
        ImageView imageView;
        if (type == CLICKABLE) {
            imageView = (ImageView) view.findViewById(R.id.icon);
        } else {
            imageView = (ImageView) toast.getView().findViewById(R.id.icon);
        }
        imageView.setBackgroundResource(resId);
        imageView.setVisibility(View.VISIBLE);
        return this;
    }

    /**
     * @param animations A style resource defining the animations to use for this window.
     *                   This must be a system resource; it can not be an application resource
     *                   because the window manager does not have access to applications.
     */
    @Deprecated
    public UniversalToast setAnimations(@StyleRes int animations) {
        Log.d(TAG, "method:setAnimations is Deprecated , animations must be a system resource " +
                ", considering the window manager does not have access to applications.");
        if (type == CLICKABLE) {
            params.windowAnimations = animations;
        } else {
            try {
                Field tnField = toast.getClass().getDeclaredField("mTN");
                tnField.setAccessible(true);
                Object mTN = tnField.get(toast);
                Field tnParamsField = mTN.getClass().getDeclaredField("mParams");
                tnParamsField.setAccessible(true);
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) tnParamsField.get(mTN);
                params.windowAnimations = animations;
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    //设置toast背景颜色
    public UniversalToast setColor(@ColorRes int colorRes) {
        GradientDrawable drawable;
        if (type == CLICKABLE) {
            drawable = (GradientDrawable) view.getBackground();
        } else {
            drawable = (GradientDrawable) toast.getView().getBackground();
        }
        drawable.setColor(context.getResources().getColor(colorRes));
        return this;
    }

    public UniversalToast setBackground(Drawable drawable) {
        if (type == CLICKABLE) {
            view.setBackground(drawable);
        } else {
            toast.getView().setBackground(drawable);
        }
        return this;
    }

    public UniversalToast setGravity(int gravity, int xOffset, int yOffset) {
        if (type == CLICKABLE) {
            final Configuration config = view.getContext().getResources().getConfiguration();
            final int g = Gravity.getAbsoluteGravity(gravity, config.getLayoutDirection());
            params.gravity = g;
            if ((g & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.FILL_HORIZONTAL) {
                params.horizontalWeight = 1.0f;
            }
            if ((g & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.FILL_VERTICAL) {
                params.verticalWeight = 1.0f;
            }
            params.x = xOffset;
            params.y = yOffset;
        } else {
            toast.setGravity(gravity, xOffset, yOffset);
        }
        return this;
    }

    public UniversalToast setMargin(float horizontalMargin, float verticalMargin) {
        if (type == CLICKABLE) {
            params.verticalMargin = verticalMargin;
            params.horizontalMargin = horizontalMargin;
        } else {
            toast.setMargin(horizontalMargin, verticalMargin);
        }
        return this;
    }

    public UniversalToast setText(@StringRes int resId) {
        if (type == CLICKABLE) {
            ((TextView) view.findViewById(R.id.text)).setText(resId);
        } else {
            toast.setText(resId);
        }
        return this;
    }

    public UniversalToast setText(@NonNull CharSequence s) {
        if (type == CLICKABLE) {
            ((TextView) view.findViewById(R.id.text)).setText(s);
        } else {
            toast.setText(s);
        }
        return this;
    }

    public void show() {
        if (type != CLICKABLE) {
            toast.show();
        } else {
            if (listener == null) {
                Log.e(TAG, "the listener of clickable toast is null,have you called method:setClickCallBack?");
                return;
            }
            if (view.getParent() != null) {
                windowManager.removeView(view);
            }
            windowManager.addView(view, params);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    cancel();
                }
            }, duration);
        }
    }

    private void cancel() {
        if (type != CLICKABLE) {
            return;
        }
        windowManager.removeView(view);
        params = null;
        windowManager = null;
        view = null;
        handler = null;
        listener = null;
    }

    public void showSuccess() {
        setLeftIcon(type == EMPHASIZE ? R.drawable.ic_check_circle_white_24dp : R.drawable.ic_done_white_24dp);
        show();
    }

    public void showError() {
        setLeftIcon(R.drawable.ic_clear_white_24dp);
        show();
    }

    public void showWarning() {
        setLeftIcon(R.drawable.ic_error_outline_white_24dp);
        show();
    }

    public UniversalToast setClickCallBack(@NonNull String text, @NonNull View.OnClickListener listener) {
        return setClickCallBack(text, R.drawable.ic_play_arrow_white_24dp, listener);
    }

    public UniversalToast setClickCallBack(@NonNull String text, @DrawableRes int resId, @NonNull View.OnClickListener listener) {
        if (type != CLICKABLE) {
            Log.d(TAG, "only clickable toast has click callback!!!");
            return this;
        }
        this.listener = listener;
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.btn);
        layout.setVisibility(View.VISIBLE);
        layout.setOnClickListener(listener);
        TextView textView = (TextView) layout.findViewById(R.id.btn_text);
        textView.setText(text);
        ImageView imageView = (ImageView) layout.findViewById(R.id.btn_icon);
        imageView.setBackgroundResource(resId);
        return this;
    }
}

