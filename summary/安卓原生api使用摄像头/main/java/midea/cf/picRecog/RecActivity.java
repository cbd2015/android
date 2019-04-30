package midea.cf.picRecog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;

import util.BitmapUtils;
import util.DisplayUtil;
import view.ClipImage;

/**
 * activity_rec.xml第二个活动
 */
public class RecActivity extends Activity implements ClipImage.OnDrawListener, PicRecModel.OnPicRecListener, View.OnClickListener {
    Button btnGoDetail;
    TextView txt;
    ImageView img;
    ImageView smallImg;
    ClipImage clipImage;


    String tag="";
    String sn="";
    String recogResult="";
    EditText editTextTag;
    EditText editTextSN;
    public static WeakReference <Bitmap> bit;
    public static Camera.Size size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rec);

        // sn  and Tag
//        editTextTag = (EditText) findViewById(R.id.et_tag2);
//        editTextSN = (EditText) findViewById(R.id.et_sn2);

        //取得启动该Activity的Intent对象
        /*取出Intent中附加的数据*/
        // 获取共享参数
//      SharedPreferences sh = getSharedPreferences("Host",Context.MODE_PRIVATE);
//      PicRecModel.isself = sh.getInt("CODE",1);
        Intent getintent=getIntent();
        tag=getintent.getStringExtra("tag");
        sn=getintent.getStringExtra("sn");
//      editTextTag.setText(tag);
//      editTextSN.setText(sn);

//      Toast.makeText(this, tag, Toast.LENGTH_SHORT).show();
//      Toast.makeText(this, sn, Toast.LENGTH_SHORT).show();

        btnGoDetail=(Button) findViewById(R.id.goCameraId);// 返回主页摄像头取景的活动窗口


//      btnGoDetail.setText(String.valueOf(PicRecModel.isself));
        clipImage=(ClipImage) findViewById(R.id.clipImage1);
        clipImage.setOnDrawListener(this);

        smallImg=(ImageView) findViewById(R.id.imgSmallId);
        txt=(TextView) findViewById(R.id.txtId);
        img=(ImageView) findViewById(R.id.imgId);

        // bitmap图像数据获取
        img.setImageBitmap(bit.get());
        PicRecModel.getInstnce().setOnPicRecListener(this);
        btnGoDetail.setOnClickListener(this);
    }

    @Override
    public void onDrawFinished(PointF min,PointF max) {
//        Bitmap b=bit.get();
//        if (bit != null && b != null) {
//            b = rotateBitmap(b,90);
//             Bitmap s = Bitmap.createBitmap(b,0,0,1280,720);
//            try {
//                recogResult = PicRecModel.getInstnce().picRec(BitmapUtils.bitmapToBase64(s),sn,tag);
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            }
//            txt.setText(recogResult);
////                        Toast.makeText(this,(String)recogResult,Toast.LENGTH_SHORT).show();
//            smallImg.setImageBitmap(s);
//
//        }
        System.out.println("onDrawFinished");// 无手动画圈圈
    }

    public void myDrawFinished(PointF min,PointF max) {
        txt.setText("正在识别...");
        float raX;
        float raY;
        PointF minP;
        PointF maxP;
        minP=min;
        maxP=max;
        raX=(float) size.height / (float) (DisplayUtil.getScreenMetrics(AWEApplication.newInstance()).x);
        raY=(float) size.width / (float) (DisplayUtil.getScreenMetrics(AWEApplication.newInstance()).y);
        int width=(int) (raX * (maxP.x - minP.x));
        int height=(int) (raY * (maxP.y - minP.y));
        Bitmap b=bit.get();
        if (bit != null && b != null) {
            Bitmap s=Bitmap.createBitmap(b,(int) (minP.x * raX),(int) (minP.y * raY),width,height);
            /**
             * data、tag、sn
              */
            try {
                PicRecModel.getInstnce().picRec(BitmapUtils.bitmapToBase64(s),sn,tag);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            smallImg.setImageBitmap(s);
        }
    }

    @Override
    public void onPicRec(int resultCode,final String result) {

//        if (result != null) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    txt.setText(result);
//                }
//            });
//        }
        // 显示图像识别结果
        if (result != null) {
            // Jdk8.0 Lambda表达式，匿名函数
            runOnUiThread(() -> {
                txt.setText(recogResult);
                System.out.print("===onPicRec===");
            });
            // 静态内部类
//              new Runnable() {
//                @Override
//                public void run() {
//                    txt.setText(result);
//                }
//            });
        }
    }

    /**
     * 选择变换, 图像旋转
     * @param origin 原图
     * @param alpha  旋转角度，可正可负
     * @return 旋转后的图片
     */
    private Bitmap rotateBitmap(Bitmap origin,float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        // 围绕原地进行旋转
        Bitmap newBM = Bitmap.createBitmap(origin,0,0, width, height, matrix,false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        try {
            origin.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newBM; // 返回旋转后的Bitmap位图
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goCameraId:
                // 第二个活动释放，返回第一个活动
                Bitmap b=bit.get();
                if (bit != null && b != null) {
                    b = rotateBitmap(b,0);
//                    final Bitmap s=Bitmap.createBitmap(b,0,0,1280,720);
                    final Bitmap s = Bitmap.createBitmap(b,50,350,600,600);
                    System.out.print("before Thread=========");

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                recogResult=PicRecModel.getInstnce().picRec(BitmapUtils.bitmapToBase64(s),sn,tag);
                                System.out.print("run upload onclick=========");
//                                txt.setText( recogResult );
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
                break;
            case R.id.et_exit:
//              Toast.makeText(RecActivity.this,(String)recogResult,Toast.LENGTH_SHORT).show();
//              break;
                finish();
                break;
            default:
                break;
        }
    }

    /**
     * GC 回收
     */
    @Override
    public void onDestroy(){
        super.onDestroy();
    }

}
