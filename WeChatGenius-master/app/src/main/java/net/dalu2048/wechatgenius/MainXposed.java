/*
 * ************************************************************
 * 文件：MainXposed.java  模块：app  项目：WeChatGenius
 * 当前修改时间：2018年08月19日 17:06:09
 * 上次修改时间：2018年08月19日 17:06:09
 * 作者：大路
 * Copyright (c) 2018
 * ************************************************************
 */

package net.dalu2048.wechatgenius;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import net.dalu2048.wechatgenius.net.ConstensValues;
import net.dalu2048.wechatgenius.net.HttpRequest;
import net.dalu2048.wechatgenius.util.RequestInterceptor;
import net.dalu2048.wechatgenius.util.StringFormatter;
import net.dalu2048.wechatgenius.util.TimeUtils;
import net.dalu2048.wechatgenius.util.countmd5;
import net.dalu2048.wechatgenius.xposed.WechatUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public final class MainXposed implements IXposedHookLoadPackage {
    //微信数据库包名称
    private static final String WECHAT_DATABASE_PACKAGE_NAME = "com.tencent.wcdb.database.SQLiteDatabase";
    //聊天精灵客户端包名称
    private static final String WECHATGENIUS_PACKAGE_NAME = "net.dalu2048.wechatgenius";
    //微信主进程名
    private static final String WECHAT_PROCESS_NAME = "com.tencent.mm";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        //region hook模块是否激活
        if (lpparam.packageName.equals(WECHATGENIUS_PACKAGE_NAME)) {
            //hook客户端APP的是否激活返回值。替换为true。
            Class<?> classAppUtils = XposedHelpers.findClassIfExists(WECHATGENIUS_PACKAGE_NAME + ".util.AppUtils", lpparam.classLoader);
            if (classAppUtils != null) {
                XposedHelpers.findAndHookMethod(classAppUtils,
                        "isModuleActive",
                        XC_MethodReplacement.returnConstant(true));
                XposedBridge.log("成功hook住net.xxfeng.cc.util.AppUtils的isModuleActive方法。");
            }
            return;
        }
        //endregion

        if (!lpparam.processName.equals(WECHAT_PROCESS_NAME)) {
            return;
        }
        XposedBridge.log("进入微信进程：" + lpparam.processName);
        //调用 hook数据库插入。
        hookDatabaseInsert(lpparam);
        //sendWxCircle(lpparam);
    }
    boolean isUploading=true;
    public void sendWxCircle(XC_LoadPackage.LoadPackageParam lpparam)
    {
        if (!lpparam.packageName.equals("com.tencent.mm"))
            return;
        XposedBridge.log("Loaded app wx: " + lpparam.packageName);
        try
        {
            XposedHelpers.findAndHookMethod("com.tencent.mm.ui.LauncherUI", lpparam.classLoader, "onResume", new XC_MethodHook()
            {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    //这里获取到的就是LauncherUI对象实例了
                    if (isUploading){
                        Object obj=param.thisObject;
                        Activity UploadActivity=(Activity)obj;
//                      String vidioCoverImage="/storage/emulated/0/01.jpg";
//                      String videoAbsolutePath="/storage/emulated/0/01.mp4";
                        String vidioCoverImage= Environment.getExternalStorageDirectory().getAbsolutePath()+"/Pictures/01.jpg";
                        String videoAbsolutePath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/01.mp4";
                        XposedBridge.log(vidioCoverImage);
                        XposedBridge.log(videoAbsolutePath);

                        File imgFile=new File(vidioCoverImage);
                        File videoFile=new File(videoAbsolutePath);
                        if(videoFile.exists()&&imgFile.exists())
                        {
                            XposedBridge.log("找到文件，准备上传！");
                            try
                            {
                                XposedBridge.log("videoFile md5 验证");
                                String md5= countmd5.g(videoFile);

                                String activityName="com.tencent.mm.plugin.sns.ui.SightUploadUI";

                                Intent intent=new Intent();
                                intent.setClassName(UploadActivity.getApplicationContext(),activityName);
                                intent.putExtra("sight_md5",md5);
                                intent.putExtra("KSightDraftEntrance",false);
                                intent.putExtra("KSightPath",videoAbsolutePath);
                                intent.putExtra("KSightThumbPath",vidioCoverImage);
                                intent.putExtra("Kdescription","TestTestTest...");
                                UploadActivity.startActivity(intent);
                                XposedBridge.log("Post，successful!");
                            }
                            catch(Throwable e)
                            {
                                XposedBridge.log(Log.getStackTraceString(e));
                            }
                        }
                        else
                        {
                            XposedBridge.log("file not exist...");
                        }
                        isUploading=false;
                    }
                }
            });
        }
        catch (Throwable t)
        {
            XposedBridge.log(t.toString());
        }
    }

    //hook数据库插入操作->com.tencent.wcdb.database.SQLiteDatabase->insertWithOnConflict
    private void hookDatabaseInsert(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Class<?> classDb = XposedHelpers.findClassIfExists(WECHAT_DATABASE_PACKAGE_NAME, loadPackageParam.classLoader);
        if (classDb == null) {
            XposedBridge.log("hook数据库insert操作：未找到类" + WECHAT_DATABASE_PACKAGE_NAME);
            return;
        }
        XposedHelpers.findAndHookMethod(classDb,
                "insertWithOnConflict",
                String.class, String.class, ContentValues.class, int.class,//反编译知道微信插入数据库有4个参数
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String tableName = (String) param.args[0];
                        ContentValues contentValues = (ContentValues) param.args[2];
                        if (tableName == null || tableName.length() == 0 || contentValues == null) {
                            return;
                        }
                        //过滤掉非聊天消息
                        if (!tableName.equals("message")) {
                            return;
                        }
                        //打印出日志
                        printInsertLog(tableName, (String) param.args[1], contentValues, (Integer) param.args[3]);

                        //提取消息内容
                        //1：表示是自己发送的消息
                        int isSend = contentValues.getAsInteger("isSend");
                        //消息内容
                        String strContent = contentValues.getAsString("content");
                        String wxId="";
                        String message="";
                        strContent=strContent.replaceAll("[\\t\\n\\r]", "");
                        if (strContent.contains(":")){
                            wxId=strContent.split(":")[0];
                            message=strContent.split(":")[1];
                        }
                        //时间
                        final String createTime=contentValues.getAsString("createTime");
                        //说话人ID/群聊id
                        final String strTalker = contentValues.getAsString("talker");
                        XposedBridge.log("Xposed框架hook"+strTalker+"的聊天内容:"+strContent);
                        if (!strContent.contains("<img")){
                            if (message.contains("<appmsg")&&message.contains("<title>")){//引用消息
                                String[] msgArray=message.split("<title>");
                                if (msgArray.length>1){
                                    String[] msgArray2= msgArray[1].split("</title>");
                                    requestReportMsg(wxId,msgArray2[0],createTime,strTalker);
                                }
                            }else {
                                requestReportMsg(wxId,message,createTime,strTalker);
                            }
                        }
                        //收到消息，进行回复（要判断不是自己发送的、不是群消息、不是公众号消息，才回复）
//                        if (isSend != 1 && !strTalker.endsWith("@chatroom") && !strTalker.startsWith("gh_")) {
//                            String wxid="wj1990sf";
//                            String replyContent="测试回复内容";
//                            try {
//                                Class clz_h=XposedHelpers.findClass("com.tencent.mm.modelmulti.h",loadPackageParam.classLoader);
//                                Object msg=XposedHelpers.newInstance(clz_h,wxid,replyContent,1);
//                                Class clz_aw=XposedHelpers.findClass("com.tencent.mm.model.av",loadPackageParam.classLoader);
//                                Object clz_p=XposedHelpers.callStaticMethod(clz_aw,"Pw");
//                                XposedHelpers.callMethod(clz_p,"a",msg,0);
//                                //WechatUtils.replyTextMessage(loadPackageParam, "回复：" + strContent, strTalker);
//                            }catch (Exception e){
//                                XposedBridge.log("调用微信消息回复方法异常");
//                                XposedBridge.log(e);
//                            }
//                        }
                    }
                });
    }

    private void requestReportMsg(final String wxId, final String message, final String createTime, final String strTalker) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //请求网络
                //1.创建请求头-post
                final RequestBody requestBody = new FormBody.Builder()//FormBody.Builder()静态内部类--默认为FormUrlEncoded（普通表单）方式提交
                        .add("groupId", strTalker)
                        .add("message", message)
                        .add("wxId", wxId)
                        .add("time", TimeUtils.getTime(Long.parseLong(createTime)))
                        .build();
                XposedBridge.log("请求参数："+"groupId："+strTalker+"message:"+message+"wxId:"+wxId+"time:"+TimeUtils.getTime(Long.parseLong(createTime)));
                final String url = ConstensValues.BASE_URL4 + "app/message/report";
                final Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();
                //2.创建okhttp对象
                //OkHttpClient okHttpClient = new OkHttpClient();
                OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor
                        (new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(20, TimeUnit.SECONDS)
                        //.addInterceptor(new RequestInterceptor())//基础参数生效
                        .connectTimeout(10, TimeUnit.SECONDS).build();
                //3.并创建回调
                Call call=okHttpClient.newCall(request);
                //请求加入异步回调
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response != null && response.body() != null) {
                            if (response.isSuccessful()) {

                            } else {
                            }
                        }else {

                        }
//                        String request= StringFormatter.jsonFormatter(response.toString());
//                        XposedBridge.log("请求参数："+request);
                        //io线程中
                        String content=response.body().string();
                        XposedBridge.log("返回参数："+content);
                    }
                });
            }
        }).start();
    }

    //输出插入操作日志
    private void printInsertLog(String tableName, String nullColumnHack, ContentValues contentValues, int conflictValue) {
        String[] arrayConflicValues =
                {"", " OR ROLLBACK ", " OR ABORT ", " OR FAIL ", " OR IGNORE ", " OR REPLACE "};
        if (conflictValue < 0 || conflictValue > 5) {
            return;
        }
        XposedBridge.log("Hook数据库insert。table：" + tableName
                + "；nullColumnHack：" + nullColumnHack
                + "；CONFLICT_VALUES：" + arrayConflicValues[conflictValue]
                + "；contentValues:" + contentValues);
    }

}
