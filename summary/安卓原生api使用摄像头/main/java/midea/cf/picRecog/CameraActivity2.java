package midea.cf.picRecog;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;

import org.yanzi.camera.CameraInterface;
import org.yanzi.camera.preview.CameraGLSurfaceView;

import java.lang.ref.WeakReference;

import util.DisplayUtil;
import util.ImageUtil;
import util.VolleyRequestQueueManager;

/**
 * @implNote 拍照、照片预览、点击识别、点击保存
 *
 *
 *
 */
public class CameraActivity2 extends Activity implements OnClickListener, ShutterCallback, PictureCallback{
    private static final String TAG = "yanzi";
    private static Camera.Size size;
    CameraGLSurfaceView glSurfaceView = null;
    Button shutterBtn;
    Intent getintent = new Intent();
    Intent outintent = new Intent();
    String tag = "";
    String sn  = "";
    EditText editTextTag;
    EditText editTextSN;
    // 横竖屏要改该值，还有纹理映射坐标
    public static float previewRate = -1f;

    // 获取安卓手机摄像头使用权限
    private void checkPermission() {
        if (Build.VERSION.SDK_INT < 23)
            return;
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.CAPTURE_VIDEO_OUTPUT}, 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Log.i("", "p="+ Environment.getExternalStorageDirectory().getAbsolutePath());
        // 权限验证
        checkPermission();
        // 摄像头图像预览区
        glSurfaceView = (CameraGLSurfaceView) findViewById(R.id.camera_textureview);
        // 取景拍照按钮
        shutterBtn = (Button) findViewById(R.id.btn_shutter);
        editTextTag = (EditText) findViewById(R.id.et_tag);
        editTextSN = (EditText) findViewById(R.id.et_sn);
        // 初始化设置图像预览区
        initViewParams();

        // 点击拍照按钮。按钮时间监听
        shutterBtn.setOnClickListener(this);// 回调onClick()函数
        // Log.e("22222","444"+getSecondaryStoragePath());
    }

    /**
     * 初始化图像预览区的尺寸大小
      */
    private void initViewParams() {
        LayoutParams params = glSurfaceView.getLayoutParams();
        Point p = DisplayUtil.getScreenMetrics(this);
        params.width = p.x ;
        params.height = p.y ;
        previewRate = DisplayUtil.getScreenRate(this); // 默认全屏的比例预览
        glSurfaceView.setLayoutParams(params);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_shutter:
                CameraInterface.getInstance().doTakePicture(this, null, this);

                tag = editTextTag.getText().toString();
                sn = editTextSN.getText().toString();

                 // 方法1：Intent传递参数
//                Intent intent = new Intent(CameraActivity.this, RecActivity.class);
//                Toast.makeText(this, "outPreiview", Toast.LENGTH_SHORT).show();
                 // 方法2：setAction 设置相关的页面并启动活动
//                outintent.setAction(CameraActivity.this,"midea.cf.picRecog.RecActivity");
//                outintent.putExtra("tag", tag);
//                outintent.putExtra("sn", sn);
//                sendBroadcast(outintent);
                break;
            default:
                break;
        }
    }

    /**
     * 继续拍照
     */
    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.bringToFront();
//        Intent getintent = getIntent();
//        String RecogResult = getintent.getStringExtra("RecogResult");
//        Toast.makeText(this, RecogResult, Toast.LENGTH_LONG).show();
        // glSurfaceView.onResume();
    }

    /**
     *  停止拍照
     */
    @Override
    protected void onPause() {
        super.onPause();
     // glSurfaceView.onPause();
    }

    /**
     * 相机快门
     */
    @Override
    public void onShutter() {

    }

    /**
     * 选择变换、图像旋转
     * @param  origin 原图
     * @param  alpha  旋转角度，可正可负
     * @return 旋转后的图片
     */
    private Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);

        // 围绕原地进行旋转
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        try {
            origin.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 返回旋转后的Bitmap位图
        return newBM;
    }

    /**
     * 捕捉界面上的图像
     * 传送识别携带参数信息
     * 页面跳转
     * @param data
     * @param camera
     */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        CameraInterface.getInstance().doPausePreview();
        Bitmap b = ImageUtil.getRotateBitmap(BitmapFactory.decodeByteArray(data, 0, data.length), 90.0f);
        // 将预览区的Bitmap图像传送到第二个活动中显示
        RecActivity.bit = new WeakReference<Bitmap>(b);
        RecActivity.size = CameraInterface.getInstance().getPicSize();
        // CameraInterface.getInstance().doResumePreview();
        Intent intent = new Intent(this,RecActivity.class);
        // 将要传递的值附加到Intent对象
        intent.putExtra("tag", tag);
        intent.putExtra("sn", sn);
        startActivity(intent);
    }

    /**
     * 资源释放
     * 进入图像预览，停止相机
     */
    @Override
    protected void onDestroy() {
        VolleyRequestQueueManager.release(null);
        CameraInterface.getInstance().doStopCamera();
        super.onDestroy();
    }

}

