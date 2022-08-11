package net.dalu2048.wechatgenius.util;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import de.robv.android.xposed.XposedBridge;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static android.text.TextUtils.isEmpty;

/**
 * @description 主要解决
 * 1、统一附加vega网关必要参数问题
 * @link http://wiki.chinawayltd.com/pages/viewpage.action?pageId=29657342
 */
public class RequestInterceptor implements Interceptor {

    private String TAG = getClass().getSimpleName();
    public final static int INTERCEPT_CODE= 10086; //自定义的拦截code

    @Override
    public Response intercept(Chain chain) throws IOException {
        //拿到请求url,添加token sign accessId g7timestamp
        Request request = chain.request();
        Request newRequest;
        HttpUrl url = request.url();
        String actionPath = url.encodedPath();

        if (actionPath == null || actionPath.contains("cashdesk-sdk")) {
            return chain.proceed(request);
        }


        if (isEmpty(url.queryParameter("accessid"))
                && isEmpty(url.queryParameter("token"))
                && isEmpty(url.queryParameter("sign"))
                && !isEmpty(actionPath)) {

            String[] split = actionPath.split("rest");
            if (split.length > 1) {
                actionPath = split[1];
            }

            String urlStr = url.toString();

            if (isEmpty(url.query())) {
                urlStr += "?";
            } else {
                urlStr += "&";
            }
            actionPath = actionPath.substring(1);
            String vegaParam = generateVegaParams(actionPath, chain.request().method(), url);
            if (vegaParam == null) {
                Log.e(TAG, "conflict");
//                LoginActivity.startWithConflict(CmtApplication.getInstance());
                //PlatformUtils.logout();
                return new Response.Builder()
                        .code(INTERCEPT_CODE) //Simply put whatever value you want to designate to aborted request.
                        .protocol(Protocol.HTTP_2)
                        .message("Dummy response")
                        .body(ResponseBody.create(MediaType.get("text/html; charset=utf-8"), "")) // 返回空页面
                        .request(chain.request())
                        .build();
            } else {
                urlStr += generateVegaParams(actionPath, chain.request().method(), url);
            }
            //if (BuildConfig.DEBUG_LOG) LogUtils.d(TAG, urlStr);
            request = request.newBuilder().url(urlStr).build();
        }
        String traceId = UUID.randomUUID().toString().replace("-", "");

        Request.Builder builder = request.newBuilder();
        if (actionPath.contains("ntocc-acms-app-service")) {
            builder.addHeader("Accept", "application/json; charset=utf-8");
        }
        newRequest = builder
                .addHeader("X-B3-TraceId", traceId)
                .method(request.method(), request.body())
                .build();


        //Utility.startTraceNet(newRequest);
        return chain.proceed(newRequest);
    }

    private String generateVegaParams(String actionPath, String method, HttpUrl url) {
        StringBuilder str = new StringBuilder();
        String timestamp = String.valueOf(System.currentTimeMillis());

        str.append("accessid=").append("w0uxlza");
        if (TextUtils.isEmpty(url.queryParameter("g7timestamp"))) {
            str.append("&g7timestamp=").append(timestamp);
        }
        str.append("&app=1");
        str.append("&from=jiedanbao");
        str.append("&secretKey=DukCK4kvN5qCvpSU3uWKYFaHDS0SUtFx");
        str.append("&uid=653");
        str.append("&ua=android&appclientversion=").append("1.1.1");

        // 没有refer场景，使用缓存中refer
        if (TextUtils.isEmpty(url.queryParameter("referer"))) {
            str.append("&referer=").append("d507c00281c733bd693e5049ea33ad7e");
        }
        return str.append("&sign=").append("OHHbIMHM%2FwpdKzFOZaB8Ok2ck%2FA%3D").toString();

    }
}