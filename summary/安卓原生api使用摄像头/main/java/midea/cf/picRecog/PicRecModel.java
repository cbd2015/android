package midea.cf.picRecog;

import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


public class PicRecModel {

    //  识别服务事件监听
    private static PicRecModel mPicRecModel;
    private OnPicRecListener onPicRecListener;

    private PicRecModel() {
    }

    public interface OnPicRecListener {
        void onPicRec(int resultCode,String result);
    }

    public OnPicRecListener getOnPicRecListener() {
        return onPicRecListener;
    }

    public void setOnPicRecListener(OnPicRecListener onPicRecListener) {
        this.onPicRecListener=onPicRecListener;
    }

    //  单例模式
    public static PicRecModel getInstnce() {
        if (mPicRecModel == null)
            mPicRecModel=new PicRecModel();
        return mPicRecModel;
    }

    // 图像数据采集URL接口
    private String mUrl="https://ce5.midea.com/img-collecting-service/uploadImg";
    // 图像识别服务接口：调用正式的生产环境
//    private String mUrl = "https://ce2.midea.com/rice-imgreg/as/";
    public static int isself=1;
    private int recCount=0;

    //	数据传递参数
    public String picRec(final String img,final String sn,final String tag) throws MalformedURLException {
        String RecogResult="";
        recCount++;
        Request r=null;
        Log.d("picRec, ",isself + "");
        //调用百度图像识别api接口
        if (isself == 0) {
            mUrl="https://aip.baidubce.com/rpc/2.0/ai_custom/v1"
                    + "/classification/model_582?access_token="
                    + "24.eb74f3fedc7d59d9ad51b91a81adbc60.2592000.1516766006.282335-10579649";
            Map <String, String> map=new HashMap <String, String>();
            map.put("image",img);
            map.put("top_num","5");
            r=new JsonObjectRequest(Request.Method.POST,mUrl,new JSONObject(map),
                    new Response.Listener <JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            recCount--;
                            if (AWEApplication.isDebug) {
                                Log.w(this.getClass().getName(),"response=" + response.toString());
                            }
                            if (onPicRecListener != null)
                                onPicRecListener.onPicRec(0,response.toString());
                        }
                    },new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError e) {
                    recCount--;
                    if (AWEApplication.isDebug) {
                        util.ToastUtil.show(
                                AWEApplication.newInstance(),"err=" + e.toString(),Toast.LENGTH_SHORT);
                        Log.e(this.getClass().getSimpleName(),"VolleyError=" + e.toString());
                    }
                    if (onPicRecListener != null)
                        onPicRecListener.onPicRec(1,e.toString());
                }
            });
        } else {
            //  图像数据采集
            mUrl="https://ce5.midea.com/img-collecting-service/uploadImg";
            //  SDK图像识别
            //  mUrl = "https://ce2.midea.com/rice-imgreg/as/";
            Log.w("mUrl=======",mUrl);
            URL url = null;
            URLConnection rulConnection;
            try {
                url=new URL(mUrl);
                rulConnection=url.openConnection();
                HttpURLConnection httpUrlConnection=(HttpURLConnection) rulConnection;
                httpUrlConnection.setConnectTimeout(20000);
                httpUrlConnection.setReadTimeout(20000);
                httpUrlConnection.setDoOutput(true);
                httpUrlConnection.setUseCaches(false);
                httpUrlConnection.setRequestProperty("Content-type","application/x-www-form-urlencoded; charset=utf-8");
                // Https安全传输协议
//                 httpUrlConnection.setRequestProperty("Authorization","Basic bWlkZWE6VzBKZzBPUHlGSTYxMVQ2Q3NkZDAzSjhXWllzRnEwQ0krMmltS2pXNU5vST0=");// GPU正式环境识别
                httpUrlConnection.setRequestProperty("Authorization","Basic cm9vdDo3aSttUytBY0dISkpqMmZjcndRcHhJazY2SHNQdFJYbg==");
                // httpUrlConnection.setRequestProperty("connection", "close");
                httpUrlConnection.setRequestMethod("POST");

                httpUrlConnection.connect();

                // OutputStream outStrm = httpUrlConnection.getOutputStream();
                // BufferedOutputStream bos = new BufferedOutputStream(outStrm);
                DataOutputStream out=new DataOutputStream(httpUrlConnection.getOutputStream());

                StringBuilder strb=new StringBuilder(URLEncoder.encode("data","UTF-8"));
                strb.append('=');
                strb.append(URLEncoder.encode(img,"UTF-8"));
                strb.append('&');
//			RiceRecService.SN="0000EA11100000BB8171191100380000"
//          数据采集和云端识别接口传输的参数
                /** 搅拌采集  和  SDK图像识别接口
                 *  data 和  data
                 *  sn  和 SN
                 *  tag 和 UserTag
                 */
                strb.append(URLEncoder.encode("sn","UTF-8"));
                strb.append('=');
                if (sn.isEmpty() || sn == "" || sn == null) {
                    strb.append(URLEncoder.encode("00000000","UTF-8"));
                } else {
                    strb.append(URLEncoder.encode(sn,"UTF-8"));
                }
                strb.append('&');

                //  strb.append(URLEncoder.encode("UserTag","UTF-8"));
                strb.append(URLEncoder.encode("tag","UTF-8"));
                strb.append('=');
                if (tag.isEmpty() || tag == "" || tag == null) {
                    strb.append(URLEncoder.encode("xxxxxx","UTF-8"));
                } else {
                    strb.append(URLEncoder.encode(tag,"UTF-8"));
                }
                strb.append('&');
                out.write(strb.toString().getBytes("UTF-8"));
                out.close();

                StringBuilder sbResult = new StringBuilder();
                int code = httpUrlConnection.getResponseCode();
                Log.i("cf","code=" + code);
                if (code == 200) {
                    String line;
                    InputStream inStrm = httpUrlConnection.getInputStream();
                    BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inStrm));
                    while ((line = bufferedReader.readLine()) != null) {
                        sbResult.append(line).append("\n");
                    }
                    bufferedReader.close();
                    inStrm.close();
                }
                String res = sbResult.toString();
//                Log.d("picRec", res);
                if (onPicRecListener != null)
                    onPicRecListener.onPicRec(0,res);
                // 返回识别结果
                RecogResult=res;

            } catch (IOException e) {
                e.printStackTrace();
//                Log.e("picRec", e.toString());
                if (onPicRecListener != null)
                    onPicRecListener.onPicRec(1,e.toString());
            }
        }
        // 云端图像识别结果
//        Log.e("RecogResult",RecogResult);
        return RecogResult;
    }
}
