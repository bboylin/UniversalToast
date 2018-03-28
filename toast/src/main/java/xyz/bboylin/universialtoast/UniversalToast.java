package xyz.bboylin.universialtoast;

import android.content.Context;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.os.Build.VERSION_CODES.KITKAT;

/**
 * 核心toast类
 *
 * @author lin
 */

public class UniversalToast {
    private static int notificationEnabledValue = -1;

    public static final int UNIVERSAL = 0;
    public static final int EMPHASIZE = 1;
    public static final int CLICKABLE = 2;

    public static final int LENGTH_LONG = Toast.LENGTH_LONG;
    public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;

    @IntDef({UNIVERSAL, EMPHASIZE, CLICKABLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    @IntDef({LENGTH_LONG, LENGTH_SHORT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {
    }

    public static IToast makeText(@NonNull Context context, @NonNull String text, @Duration int duration) {
        return makeText(context, text, duration, UNIVERSAL);
    }

    public static IToast makeText(@NonNull Context context, @NonNull String text, @Duration int duration, @Type int type) {
        // 5.0以下采用自定义toast
        if (notificationEnabledValue < 0) {
            if (Build.VERSION.SDK_INT > KITKAT) {
                notificationEnabledValue = NotificationManagerCompat.from(context).areNotificationsEnabled() ? 1 : 0;
            } else {
                notificationEnabledValue = 0;
            }
        }
        // 允许通知权限则尽量用系统toast
        // 没有通知权限或者是可点击的toast则使用自定义toast
        if (notificationEnabledValue > 0 && type != CLICKABLE) {
            Log.e("TAG", notificationEnabledValue + "SystemToast");
            return SystemToast.makeText(context, text, duration, type);
        } else {
            Log.e("TAG", notificationEnabledValue + "CustomToast");
            return CustomToast.makeText(context, text, duration, type);
        }
    }
}

