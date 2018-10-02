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
    private static final int NOTIFICATION_UNKNOWN = -1;
    private static final int NOTIFICATION_DISABLED = 0;
    private static final int NOTIFICATION_ENABLED = 1;
    private static int sNotificationStatus = NOTIFICATION_UNKNOWN;

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

    public static IToast makeText(@NonNull Context context, @NonNull String text, @Duration int duration, @Type int
            type) {
        // 允许通知权限则尽量用系统toast
        // 没有通知权限或者是可点击的toast则使用自定义toast
        if (notificationEnabled(context) && type != CLICKABLE) {
            Log.d("TAG", sNotificationStatus + ":SystemToast");
            return SystemToast.makeText(context, text, duration, type);
        } else {
            Log.d("TAG", sNotificationStatus + ":CustomToast");
            return CustomToast.makeText(context, text, duration, type);
        }
    }

    private static boolean notificationEnabled(Context context) {
        // 5.0以下采用自定义toast
        if (sNotificationStatus == NOTIFICATION_UNKNOWN) {
            if (Build.VERSION.SDK_INT >= KITKAT) {
                sNotificationStatus = NotificationManagerCompat.from(context).areNotificationsEnabled() ?
                        NOTIFICATION_ENABLED : NOTIFICATION_DISABLED;
            } else {
                sNotificationStatus = NOTIFICATION_DISABLED;
            }
        }
        return sNotificationStatus == NOTIFICATION_ENABLED;
    }
}

