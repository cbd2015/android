package util;
import android.content.Context;

import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by cf on 16/7/8.
 */
public class ToastUtil {
    public static boolean isShow = true;
    private static boolean isShowing = false;
    private static Toast toast;

    public static void show(Context context, CharSequence text, int duration) {
        if (toast != null) {
            toast.setText(text);
            toast.setDuration(duration);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
        } else {
            toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
        }
    }
    
    public static void showD(Context context, CharSequence text, int duration) {
        if (isShow) {
            if (toast != null) {
                toast.setText(text);
                toast.setDuration(duration);
                toast.show();
            } else {
                toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }
    }
}
