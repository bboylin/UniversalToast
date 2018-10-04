package xyz.bboylin.universialtoast;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
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
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;

import java.lang.reflect.Field;

import xyz.bboylin.universaltoast.R;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.N;
import static xyz.bboylin.universialtoast.UniversalToast.EMPHASIZE;
import static xyz.bboylin.universialtoast.UniversalToast.UNIVERSAL;

/**
 * 系统toast
 *
 * @author lin
 */

public class SystemToast implements IToast {
    private static final String TAG = "UniversalToast";
    private static final int NO_LEFT_ICON = 0;
    @NonNull
    private final Toast mToast;
    @NonNull
    private final Context mContext;
    @UniversalToast.Type
    private final int mType;
    @DrawableRes
    private int mLeftIconRes = NO_LEFT_ICON;
    @Nullable
    private Uri mLeftGifUri;

    private SystemToast(@NonNull Context context, @NonNull Toast toast, @UniversalToast.Type int type) {
        mContext = context;
        mToast = toast;
        mType = type;
    }

    public static SystemToast makeText(@NonNull Context context, @NonNull String text, @UniversalToast.Duration int
            duration) {
        return makeText(context, text, duration, UNIVERSAL);
    }

    public static SystemToast makeText(@NonNull Context context, @NonNull String text, @UniversalToast.Duration int
            duration, @UniversalToast.Type int type) {
        Toast toast = Toast.makeText(context, text, duration);
        int layoutId = R.layout.toast_universal;
        if (type == EMPHASIZE) {
            layoutId = R.layout.toast_emphasize;
            toast.setGravity(Gravity.CENTER, 0, 0);
        }
        View view = LayoutInflater.from(context).inflate(layoutId, null);
        ((TextView) view.findViewById(R.id.text)).setText(text);
        toast.setView(view);
        if (Build.VERSION.SDK_INT == N) {
            setContext(view, new SafeToastContext(context));
        }
        return new SystemToast(context, toast, type);
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

    @Deprecated
    @Override
    public IToast setDuration(int duration) {
        mToast.setDuration(duration);
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

    /**
     * @param animations A style resource defining the animations to use for this window.
     *                   This must be a system resource; it can not be an application resource
     *                   because the window manager does not have access to applications.
     */
    @Deprecated
    @Override
    public IToast setAnimations(@StyleRes int animations) {
        Log.d(TAG, "method:setAnimations is Deprecated , animations must be a system resource " +
                ", considering the window manager does not have access to applications.");
        try {
            Field tnField = mToast.getClass().getDeclaredField("mTN");
            tnField.setAccessible(true);
            Object mTN = tnField.get(mToast);
            Field tnParamsField = mTN.getClass().getDeclaredField("mParams");
            tnParamsField.setAccessible(true);
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) tnParamsField.get(mTN);
            params.windowAnimations = animations;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public IToast setColor(@ColorRes int colorRes) {
        GradientDrawable drawable = (GradientDrawable) mToast.getView().getBackground();
        drawable.setColor(mContext.getResources().getColor(colorRes));
        return this;
    }

    @TargetApi(JELLY_BEAN)
    @Override
    public IToast setBackground(Drawable drawable) {
        mToast.getView().setBackground(drawable);
        return this;
    }

    @Override
    public IToast setGravity(int gravity, int xOffset, int yOffset) {
        mToast.setGravity(gravity, xOffset, yOffset);
        return this;
    }

    @Override
    public IToast setMargin(float horizontalMargin, float verticalMargin) {
        mToast.setMargin(horizontalMargin, verticalMargin);
        return this;
    }

    @Override
    public IToast setText(@StringRes int resId) {
        mToast.setText(resId);
        return this;
    }

    @Override
    public IToast setText(@NonNull CharSequence charSequence) {
        mToast.setText(charSequence);
        return this;
    }

    @Override
    public void show() {
        SimpleDraweeView draweeView = mToast.getView().findViewById(R.id.icon);
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
        mToast.show();
    }

    @Deprecated
    @Override
    public void cancel() {
        Log.e(TAG, "only CustomToast can be canceled by user");
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

    @Deprecated
    @Override
    public IToast setClickCallback(@NonNull String text, @NonNull View.OnClickListener listener) {
        Log.e(TAG, "only CustomToast has click callback");
        return this;
    }

    @Deprecated
    @Override
    public IToast setClickCallback(@NonNull String text, int resId, @NonNull View.OnClickListener listener) {
        Log.e(TAG, "only CustomToast has click callback");
        return this;
    }
}
