package net.dalu2048.wechatgenius.net;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by wangj on 2017/8/23 0023.
 */

public class OkhttpUtils {
    public void execute(final RequestListerner requestListerner) {
        //创建请求头-get
      /*  final RequestListerner request = new RequestListerner.Builder()
                .url(url)
                .get()
                .build();*/
        //1.创建请求头-post
        final RequestBody requestBody = new FormBody.Builder()//FormBody.Builder()静态内部类--默认为FormUrlEncoded（普通表单）方式提交
                .add("user_name", "demo")
                .add("version", "100")
                .build();
        final String url = ConstensValues.BASE_URL + "app/message/report";
        final Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        //2.创建okhttp对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //3.并创建回调
        Call call=okHttpClient.newCall(request);
        requestListerner.onHttpRequestBegin(url);
        //请求加入异步回调
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requestListerner.onHttpRequestFailed(url, null);
                requestListerner.onHttpRequestComplete(url, null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.body() != null) {
                    if (response.isSuccessful()) {
                        requestListerner.onHttpRequestSuccess(url, response);
                    } else {
                        requestListerner.onHttpRequestFailed(url, response);
                    }
                }else {
                    requestListerner.onHttpRequestFailed(url, response);
                }
                requestListerner.onHttpRequestComplete(url, response);
                //io线程中
                String content=response.body().string();
                System.out.println(content);
            }
        });
    }
}
