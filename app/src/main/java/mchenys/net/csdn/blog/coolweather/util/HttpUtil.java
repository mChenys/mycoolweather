package mchenys.net.csdn.blog.coolweather.util;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by mChenys on 2016/12/16.
 */
public class HttpUtil {

    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

    public static String sendOkHttpRequest(String address) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        try {
            okhttp3.Response response = client.newCall(request).execute();
            return null == response ? null : response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
