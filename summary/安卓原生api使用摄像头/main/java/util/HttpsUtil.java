package util;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import midea.cf.picRecog.AWEApplication;
//import midea.cf.panelrec.PicRecServices;

/*
*  基于Https的数据通信
*
* */
public class HttpsUtil{
    public interface OnHttpListener {
        void onHttp(int code, String res);
    }

    private static HttpsUtil mHttpUtils;

    public static HttpsUtil getInstance() {
        if (mHttpUtils == null) {
            mHttpUtils = new HttpsUtil();
        }
        return mHttpUtils;
    }

    public void release() {
        mOnHttpListener = null;
        mHttpUtils = null;
    }

    private OnHttpListener mOnHttpListener;

    public void setmOnHttpListener(OnHttpListener mOnHttpListener) {
        this.mOnHttpListener = mOnHttpListener;
    }

    public void doRequest(String _url, String bs64, String tag) {
        if (mOnHttpListener == null)
            return;

        if (AWEApplication.isDebug) {
            Log.w(this.getClass().getName(), "url=" + _url);
        }

        URL url;
        int code = -1;
        try {
            url = new URL(_url);
            URLConnection rulConnection;

            rulConnection = url.openConnection();

            HttpURLConnection httpUrlConnection = (HttpURLConnection) rulConnection;
            httpUrlConnection.setConnectTimeout(20000);
            httpUrlConnection.setReadTimeout(20000);
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded; charset=utf-8");
            //Https安全传输协议的
            httpUrlConnection.setRequestProperty("Authorization", "Basic bWlkZWE6VzBKZzBPUHlGSTYxMVQ2Q3NkZDAzSjhXWllzRnEwQ0krMmltS2pXNU5vST0=");
            // httpUrlConnection.setRequestProperty("connection", "close");
            httpUrlConnection.setRequestMethod("POST");

            httpUrlConnection.connect();

            // OutputStream outStrm = httpUrlConnection.getOutputStream();
            // BufferedOutputStream bos = new BufferedOutputStream(outStrm);
            DataOutputStream out = new DataOutputStream(
                    httpUrlConnection.getOutputStream());

            StringBuilder sb = new StringBuilder(URLEncoder.encode("data",
                    "UTF-8"));
            sb.append('=');
            sb.append(URLEncoder.encode(bs64, "UTF-8"));
            sb.append('&');
//			RiceRecService.SN="0000EA11100000BB8171191100380000";
//			if(!TextUtils.isEmpty(RiceRecService.SN))
//			{
            sb.append(URLEncoder.encode("sn", "UTF-8"));
            sb.append('=');
            sb.append(URLEncoder.encode("000111122223333", "UTF-8"));
            sb.append('&');
//			}
            sb.append(URLEncoder.encode("tag", "UTF-8"));
            sb.append('=');
            sb.append(URLEncoder.encode(tag, "UTF-8"));
            sb.append('&');

            out.write(sb.toString().getBytes("UTF-8"));
            out.close();
            // bos.write(sb.toString().getBytes("UTF-8"));
            // bos.flush();
            // bos.close();
            // outStrm.close();
            code = httpUrlConnection.getResponseCode();
            StringBuilder sbResult = new StringBuilder();
            Log.i("cf", "code=" + code);
            if (code == 200) {
                InputStream inStrm = httpUrlConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(inStrm));
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    sbResult.append(line).append("\n");
                }

                bufferedReader.close();
                inStrm.close();
            }
            if (mOnHttpListener != null)
                mOnHttpListener.onHttp(code, sbResult.toString());

        } catch (Exception e) {
            // TODO Auto-generated catch block
            if (mOnHttpListener != null)
                mOnHttpListener.onHttp(code, getStackTraceInfo(e));
        }
    }

    /**
     * 获取e.printStackTrace() 的具体信息，赋值给String 变量，并返回
     *
     * @param e Exception
     * @return e.printStackTrace() 中 的信息
     */
    private String getStackTraceInfo(Exception e) {
        StringWriter sw = null;
        PrintWriter pw = null;

        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            e.printStackTrace(pw);//将出错的栈信息输出到printWriter中
            pw.flush();
            sw.flush();

            return sw.toString();
        } catch (Exception ex) {
            return "发生错误";
        } finally {
            if (sw != null) {
                try {
                    sw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (pw != null) {
                pw.close();
            }
        }

    }

}
