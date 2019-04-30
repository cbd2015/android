package midea.cf.picRecog;

import android.util.Log;

import util.HttpsUtil;
import util.HttpsUtil.OnHttpListener;

public class PicRecModel2 implements OnHttpListener{
    public interface OnPicRecListener{
        void onPicRec(int resultCode,String result);
    }

    private OnPicRecListener onPicRecListener;

    public void setOnPicRecListener(OnPicRecListener onPicRecListener) {
        this.onPicRecListener = onPicRecListener;
    }
    private static PicRecModel2 mPicRecModel;

    public static PicRecModel2 getInstnce()
    {
        if (mPicRecModel==null)
        {
            mPicRecModel=new PicRecModel2();
            HttpsUtil.getInstance().setmOnHttpListener(mPicRecModel);
        }
        return mPicRecModel;
    }

    public void release(){
        HttpsUtil.getInstance().release();
        onPicRecListener=null;
        mPicRecModel=null;
    }
    private PicRecModel2(){
    }

    //	private String mUrl="http://120.27.157.103:8080/imgrec/b64data";
    //	http://120.27.157.103:5001/
    private String mUrl = "https://ce5.midea.com/img-collecting-service/uploadImg";
    private int recCount;
    public void picRec(final String img, String tag) {
        if(recCount>0)
            return;
        recCount++;
        if (RiceRecService.DEBUG_URL != null) mUrl = RiceRecService.DEBUG_URL;
        HttpsUtil.getInstance().doRequest(mUrl, img, tag);
    }

    @Override
    public void onHttp(int code, String res) {
        recCount--;
        if (AWEApplication.isDebug)
        {
            Log.w(this.getClass().getName(), "response=" + res);
        }
        if(onPicRecListener==null)
            return;
        onPicRecListener.onPicRec(code==200?0:1, res);
    }
}
