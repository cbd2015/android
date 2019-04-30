package util;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import midea.cf.picRecog.AWEApplication;
/**
 * Created by ricecooker on 16/6/24.
 */
public class VolleyRequestQueueManager {
    private static final String TAG="volley";
    private static RequestQueue requestQueue=null;
    public static RequestQueue newInstance()
    {
        if(requestQueue==null)
            requestQueue= Volley.newRequestQueue(AWEApplication.newInstance());
        return requestQueue;
    }
    public static void addRequest(Request<?> request,Object tag)
    {
        request.setTag(tag==null?TAG:tag);
        newInstance().add(request);
    }
    public static void cancelRequest(Object tag)
    {
        newInstance().cancelAll(tag==null?TAG:tag);
    }
    public static void release(Object tag)
    {
        cancelRequest(tag);
        newInstance().stop();
        requestQueue=null;
    }
}
