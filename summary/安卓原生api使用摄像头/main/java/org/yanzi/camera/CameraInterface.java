package org.yanzi.camera;
import java.io.IOException;
import java.util.List;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;

import util.CamParaUtil;
public class CameraInterface {
    private static final String TAG = "yanzi";
    private Camera mCamera;
    private Camera.Parameters mParams;
    private boolean isPreviewing = false;
    private float mPreviwRate = -1f;
    private static CameraInterface mCameraInterface;

    // 懒汉模式，实例化
    private CameraInterface(){
    }

    public static synchronized CameraInterface getInstance(){
        if( mCameraInterface == null ){
            mCameraInterface = new CameraInterface();
        }
        return mCameraInterface;
    }

    /**
     *  1. 摄像头默认是拍照之后停止在预览界面
     *  2. 关闭预览要手动配置回调函数：doResumePreview
      */
    public void doResumePreview() {
        mCamera.startPreview();
        isPreviewing = true;
    }

    public interface CamOpenOverCallback{
        public void cameraHasOpened();
    }

    public Size getPicSize( )
    {
        if( mCamera!=null )
            return mCamera.getParameters().getPictureSize();
        return null;
    }

    /**打开Camera，回调函数
     * @param callback
     */
    public void doOpenCamera(CamOpenOverCallback callback){
        Log.i(TAG, "Camera open....");
        if(mCamera == null){
            try{
//                Log.i(TAG, "Camera open over...." + Camera.getNumberOfCameras());
                mCamera = Camera.open();
//                Log.i(TAG, "Camera open over....");
            }catch(RuntimeException e){
//                Log.e(TAG, "Camera open failed...." + e.toString());
                return;
            }
            if(mCamera == null)
            {
//                Log.e(TAG, "未找到后置摄像头");
                return;
            }
            if(callback != null){
                callback.cameraHasOpened();
            }
        }else{
//            Log.i(TAG, "Camera open 异常!!!");
            doStopCamera();
        }
    }
    /**
     * 使用Surfaceview开启预览
     * @param SurfaceHolder holder
     * @param float previewRate
     * @param mCamera.setPreviewDisplay(holder)
     */
//    public void doStartPreview(SurfaceHolder holder, float previewRate){
////        Log.i(TAG, "doStartPreview...");
//        if(isPreviewing){
//            mCamera.stopPreview();
//            return;
//        }
//        if(mCamera != null){
//            try {
//                mCamera.setPreviewDisplay(holder);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            initCamera(previewRate);
//        }
//    }
    /**
     * 使用TextureView预览Camera
     * @param surface
     * @param previewRate
     */
    public void doStartPreview( SurfaceTexture surface, float previewRate ){
//        Log.i( TAG, "doStartPreview..." );
        if( isPreviewing ){
            mCamera.stopPreview();
            return;
        }
        if( mCamera != null ){
            try {
                mCamera.setPreviewTexture(surface);
            } catch ( IOException e ) {
                e.printStackTrace();
            }
            initCamera( previewRate );
        }
    }

    /**
     * 停止预览，释放Camera
     */
    public void doStopCamera( ){
        if( null != mCamera )
        {
            mCamera.setPreviewCallback( null );
            mCamera.stopPreview();
            isPreviewing = false;
            mPreviwRate = -1f;
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     *  拍照
     */
    public void doTakePicture(ShutterCallback shutter, PictureCallback raw, PictureCallback jpeg){
        if(isPreviewing && ( mCamera != null )){
            mCamera.takePicture(shutter, raw, jpeg);
        }
    }

    /**
     *  正在预览
     */

    public boolean isPreviewing( ){
        return isPreviewing;
    }

    /**
     *  暂停预览
     */
    public void doPausePreview(){
        mCamera.stopPreview();
        isPreviewing = false;
    }

    private void initCamera(float previewRate){
        if( mCamera != null ){

            mParams = mCamera.getParameters();
            mParams.setPictureFormat(PixelFormat.JPEG);//设置拍照后存储的图片格式
            CamParaUtil.getInstance().printSupportPictureSize(mParams);
            CamParaUtil.getInstance().printSupportPreviewSize(mParams);

            //设置PreviewSize和PictureSize
            Size pictureSize = CamParaUtil.getInstance().getPropPictureSize(
                    mParams.getSupportedPictureSizes(),previewRate, 1280);
            mParams.setPictureSize(pictureSize.width, pictureSize.height);
            Size previewSize = CamParaUtil.getInstance().getPropPreviewSize(
                    mParams.getSupportedPreviewSizes(), previewRate, 1280);
            mParams.setPreviewSize(previewSize.width, previewSize.height);

//			mParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//            mCamera.setDisplayOrientation(90);

//			CamParaUtil.getInstance().printSupportFocusMode(mParams);
            List<String> focusModes = mParams.getSupportedFocusModes();
            if(focusModes.contains("continuous-video")){
                mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }

            // 开启预览
            mCamera.setParameters(mParams);
            mCamera.startPreview();
            isPreviewing = true;
            mPreviwRate = previewRate;

            mParams = mCamera.getParameters(); // 重新get一次

//            Log.i(TAG, "最终设置:PreviewSize--With = " + mParams.getPreviewSize().width
//                    + "Height = " + mParams.getPreviewSize().height);
//            Log.i(TAG, "最终设置:PictureSize--With = " + mParams.getPictureSize().width
//                    + "Height = " + mParams.getPictureSize().height);
        }
    }


    /**
     *   为了实现拍照的快门声音及拍照保存照片需要下面三个回调变量
     */
//	ShutterCallback mShutterCallback = new ShutterCallback()
//	//快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。
//	{
//		public void onShutter() {
//			// TODO Auto-generated method stub
//			Log.i(TAG, "myShutterCallback:onShutter...");
//		}
//	}

//	PictureCallback mRawCallback = new PictureCallback()
//	// 拍摄的未压缩原数据的回调,可以为null
//	{
//		public void onPictureTaken(byte[] data, Camera camera) {
//			// TODO Auto-generated method stub
//			Log.i(TAG, "myRawCallback:onPictureTaken...");
//		}
//	}

//	PictureCallback mJpegPictureCallback = new PictureCallback()
//	{
//	//  对jpeg图像数据的回调,最重要的一个回调
//		public void onPictureTaken(byte[] data, Camera camera) {
//			// TODO Auto-generated method stub
//			Log.i(TAG, "myJpegCallback:onPictureTaken...");
//			Bitmap b = null;
//			if(null != data){
//				b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
//				mCamera.stopPreview();
//				isPreviewing = false;
//			}
//			// 保存图片到sdcard
//			if(null != b)
//			{
//				// 设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。
//				// 图片竟然不能旋转了，故这里要旋转下
////				Bitmap rotaBitmap = ImageUtil.getRotateBitmap(b, 90.0f);
////				FileUtil2.saveBitmap(b);
//				PicRecModel.getInstnce().picRec(BitmapUtils.bitmapToBase64(b));
//			}
//			// 再次进入预览
//			mCamera.startPreview();
//			isPreviewing = true;
//		}
//	}

}
