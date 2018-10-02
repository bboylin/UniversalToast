package xyz.bboylin.universialtoast;

import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * 做了一些修改，避免7.1.1上BadTokenException的context
 *
 * @author drakeet
 * @link https://github.com/drakeet/ToastCompat
 */
final class SafeToastContext extends ContextWrapper {

    SafeToastContext(@NonNull Context base) {
        super(base);
    }

    @Override
    public Context getApplicationContext() {
        return new ApplicationContextWrapper(getBaseContext().getApplicationContext());
    }

    private static final class ApplicationContextWrapper extends ContextWrapper {

        private ApplicationContextWrapper(@NonNull Context base) {
            super(base);
        }


        @Override
        public Object getSystemService(@NonNull String name) {
            if (Context.WINDOW_SERVICE.equals(name)) {
                // noinspection ConstantConditions
                return new WindowManagerWrapper((WindowManager) getBaseContext().getSystemService(name));
            }
            return super.getSystemService(name);
        }
    }


    private static final class WindowManagerWrapper implements WindowManager {

        private static final String TAG = "WindowManagerWrapper";
        @NonNull
        private WindowManager mWindowManager;

        private WindowManagerWrapper(@NonNull WindowManager windowManager) {
            this.mWindowManager = windowManager;
        }


        @Override
        public Display getDefaultDisplay() {
            return mWindowManager.getDefaultDisplay();
        }


        @Override
        public void removeViewImmediate(View view) {
            mWindowManager.removeViewImmediate(view);
        }


        @Override
        public void addView(View view, ViewGroup.LayoutParams params) {
            try {
                Log.d(TAG, "WindowManager's addView(view, params) has been hooked.");
                mWindowManager.addView(view, params);
            } catch (BadTokenException e) {
                // ignore
                Log.d(TAG, "BadTokenException ignored");
            } catch (Throwable throwable) {
                Log.e(TAG, "[addView]", throwable);
            }
        }


        @Override
        public void updateViewLayout(View view, ViewGroup.LayoutParams params) {
            mWindowManager.updateViewLayout(view, params);
        }


        @Override
        public void removeView(View view) {
            mWindowManager.removeView(view);
        }
    }
}
