package volleyRequest;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.rdcx.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2015/10/28 0028.
 * <p/>
 * 用Post请求一条JsonArray数组数据
 *
 * @author mengchuiliu
 */
public class ArrayRequest extends Request<JSONArray> {
    private Map<String, String> mMap;
    private final Listener<JSONArray> mListener;

    //post方式访问
    public ArrayRequest(String url, Listener<JSONArray> listener, ErrorListener errorListener, Map<String, String>
            map) {
        super(Method.POST, url, errorListener);
        mListener = listener;
        mMap = map;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mMap;
    }

    //get方式
//    public ArrayRequest(String url, Listener<JSONArray> listener, ErrorListener errorListener) {
//        super(Method.GET, url, errorListener);
//        mListener = listener;
//    }

    @Override
    protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonArray = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONArray(jsonArray), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(JSONArray response) {
        mListener.onResponse(response);
    }
}
