package xyz.bboylin.universialtoast;

import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.view.View;

/**
 * Toast公共接口
 *
 * @author lin
 */

public interface IToast {
    /**
     * 设置duration
     *
     * @param duration 显示时长
     * @return IToast
     */
    IToast setDuration(int duration);

    /**
     * 设置icon
     *
     * @param resId drawableId
     * @return IToast
     */
    IToast setIcon(@DrawableRes int resId);

    /**
     * 设置动画
     *
     * @param animations style id
     * @return IToast
     */
    IToast setAnimations(@StyleRes int animations);

    /**
     * 设置背景颜色
     *
     * @param colorRes color res id
     * @return IToast
     */
    IToast setColor(@ColorRes int colorRes);

    /**
     * 设置背景drawable
     *
     * @param drawable drawable
     * @return IToast
     */
    IToast setBackground(Drawable drawable);

    /**
     * 设置gravity
     *
     * @param gravity gravity
     * @param xOffset x偏移
     * @param yOffset y 偏移
     */
    IToast setGravity(int gravity, int xOffset, int yOffset);

    /**
     * 设置margin
     *
     * @param horizontalMargin 水平margin
     * @param verticalMargin   垂直margin
     * @return IToast
     */
    IToast setMargin(float horizontalMargin, float verticalMargin);

    /**
     * 设置文字
     *
     * @param resId string id
     * @return IToast
     */
    IToast setText(@StringRes int resId);

    /**
     * 设置文字
     *
     * @param charSequence 字符串
     * @return IToast
     */
    IToast setText(@NonNull CharSequence charSequence);

    /**
     * 显示方法
     */
    void show();

    /**
     * 取消显示
     */
    void cancel();

    /**
     * 显示成功类型的toast
     */
    void showSuccess();

    /**
     * 显示失败类型的toast
     */
    void showError();

    /**
     * 显示warning类型的toast
     */
    void showWarning();

    /**
     * 设置点击事件
     *
     * @param text     按钮文字，建议俩字
     * @param listener 点击listener
     * @return IToast
     */
    IToast setClickCallBack(@NonNull String text, @NonNull View.OnClickListener listener);

    /**
     * 设置点击事件
     *
     * @param text     按钮文字，建议俩字
     * @param resId    按钮图标
     * @param listener 点击listener
     * @return IToast
     */
    IToast setClickCallBack(@NonNull String text, @DrawableRes int resId, @NonNull View.OnClickListener listener);
}
