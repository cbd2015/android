package midea.cf.picRecog;


import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import midea.cf.picRecog.PicRecModel2.OnPicRecListener;
import util.BitmapUtils;
import util.FileUtil2;


class RiceRecService extends Service implements OnPicRecListener{
    public static final String AUTHORIZATION = "Basic bWlkZWE6VzBKZzBPUHlGSTYxMVQ2Q3NkZDAzSjhXWllzRnEwQ0krMmltS2pXNU5vST0=";
    private static final String TAG = RiceRecService.class.getSimpleName();
    private Camera mCamera;
    private Executor mExecutor;
    private Handler mHandler;
    private volatile AtomicInteger mTimeInterval = new AtomicInteger(10000);
    public static String SN = "";
    //    public static String UserTag = "";
    public static String DEBUG_URL = null;
    private int reset_count = 0;
    private int msg_failed_count = 0;
    ThreadLocal<String> threadLocal = new ThreadLocal<>();
    ThreadLocal<Boolean> threadLocal1 = new ThreadLocal<>();

    @Override
    public void onCreate() { }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        Log.i("service", "des");
        mHandler.removeCallbacksAndMessages("task");
        mHandler = null;
        mExecutor = null;
        PicRecModel2.getInstnce().release();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        if (intent != null) {
            mTimeInterval.getAndSet(intent.getIntExtra("time", mTimeInterval.get()));
            SN = intent.getStringExtra("SN");
            if (SN == null)
                SN = "";
            String UserTag = intent.getStringExtra("UserTag");
            if (UserTag == null)
                UserTag = "";
            DEBUG_URL = intent.getStringExtra("DebugUrl");
            boolean needAnswer = intent.getBooleanExtra("NeedAnswer", true); // 默认需要返回
//            if (tmp == Integer.MAX_VALUE && mTimeInterval.get() != Integer.MAX_VALUE) {
            if (mTimeInterval.get() == 0) {
                openAndCatch(UserTag, needAnswer);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onPicRec(final int resultCode, final String result) {
        // TODO Auto-generated method stub
        sendBroadcast(resultCode, result);

    }

    public void sendBroadcast(final int resultCode, final String result) {
        // 有可能在主线程
        Log.d("cf", Thread.currentThread().getName());
        if (!threadLocal1.get()) {
            Log.d("cf", "no need to answer");
            return; // 不需要应答
        }
        Intent intent = new Intent();
        intent.setAction("com.midea.bb8.broadcast.factory.rice");
        intent.putExtra("result", result);
        intent.putExtra("result_code", resultCode);
        AWEApplication.newInstance().sendBroadcast(intent);
    }

    private void openAndCatch(final String tag, final boolean needAnswer) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                threadLocal.set(tag); // 当前线程用的都是这个tag
                threadLocal1.set(needAnswer); // 当前线程用的都是这个needAnswer
            }
        });
    }

    private void handlePic(final int len) {
        Log.d(TAG, Thread.currentThread().getName() + " handlePic " + len);
        if (len != -1) {
            msg_failed_count = 0;
            Bitmap mBitmap = BitmapFactory.decodeFile(String.format("%s/pic.jpg", FileUtil2.initPath));
            // 这个tag适用于一次工作流程
            PicRecModel2.getInstnce().picRec(BitmapUtils.bitmapToBase64(mBitmap), threadLocal.get());

            if (mBitmap != null && !mBitmap.isRecycled())
                    mBitmap.recycle();
        } else {
            msg_failed_count++;
            if (true) {
                msg_failed_count = 0;
                sendBroadcast(-2, "Camera recv data failed");
                stopSelf();
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                openAndCatch(threadLocal.get(), threadLocal1.get());
            }
        }
    }
}

