package midea.cf.picRecog;
import android.app.Activity;
import android.app.Application;

/**
 *  APP 开机首页
 *  拍照识别首页
 */
public class AWEApplication extends Application {
    private static AWEApplication myApplication;//懒汉单例模式

    private Activity mainActivity;

    public static AWEApplication newInstance()
    {
        return myApplication;//单例模式
    }

    public static boolean isDebug = true; // 工作模式切换标致

    @Override
    public void onCreate() {
        /*创建活动*/
        super.onCreate();
        myApplication = this;
    }

    public Activity getMainActivity() {
        /* 获取到主页的活动 */
        return mainActivity;
    }

    public void setMainActivity(Activity mainActivity) {
        /* 将单例模式，初始化的活动赋给实例 */
        this.mainActivity = mainActivity;
    }
}
