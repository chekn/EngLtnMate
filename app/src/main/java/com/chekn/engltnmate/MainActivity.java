package com.chekn.engltnmate;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.chekn.engltnmate.download.AppUpdateMode;
import com.chekn.engltnmate.logic.TtlLogic;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private ProgressBar progressBar;
    private Context mContext;
    private String termTtl, res, webAudioDir;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //frame setter
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_main);

        mContext = this.getApplicationContext();
        progressBar= (ProgressBar)findViewById(R.id.progressbar);//进度条

        //动态设置进度条颜色
        LayerDrawable drawable = (LayerDrawable) progressBar.getProgressDrawable();
        ClipDrawable clipDrawable = new ClipDrawable(new ColorDrawable(Color.parseColor("#e2e1dc")), Gravity.START, ClipDrawable.HORIZONTAL);
        drawable.setDrawableByLayerId(android.R.id.progress, clipDrawable);


        res = getString(R.string.res);
        webAudioDir  = getString(R.string.web_audio_dir);
        termTtl = "仁爱 / 初中 / 八年级 / 上";

        webView = (WebView) findViewById(R.id.webview);
        dynLoadAssHtml();
        //webView.loadUrl("file:///android_asset/web/lrc_js_检测无效资源 - 真机适配 - 移动化 基于常识的处理方式.html");//加载asset文件夹下html
        //webView.loadUrl("http://139.196.35.30:8080/OkHttpTest/apppackage/test.html");//加载url

        //使用webview显示html代码 /  方法可用 作 合成内容, 验证通过
        //        webView.loadDataWithBaseURL(null,"<html><head><title> 欢迎您 </title></head>" +
        //                "<body><h2>使用webview显示 html代码</h2></body></html>", "text/html" , "utf-8", null);
        //loadDataWithBaseURL (String baseUrl, String data, String mimeType, String encoding, String historyUrl)
        //webView.loadDataWithBaseURL("file:///android_asset/web/","<html><head> <title> 欢迎您 </title> <script src=\"./dg_data.js\"></script> </head><body> <script>for(var i in ms){document.write(ms[i].sound);}</script> </body></html>", "text/html", "UTF-8", null);
        //webView.loadDataWithBaseURL("file:///android_asset/web/","<html><head> <title> 欢迎您 </title> </head><body> <a href=\"javascript:android.getClient('this probe native..')\">click me</a> </body></html>", "text/html", "UTF-8", null);

        webView.addJavascriptInterface(this,"android");//添加js监听 这样html就能调用客户端
        webView.setWebChromeClient(webChromeClient);
        webView.setWebViewClient(webViewClient);

        WebSettings webSettings=webView.getSettings();
        webSettings.setJavaScriptEnabled(true);//允许使用js

        /**
         * LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
         * LOAD_DEFAULT: （默认）根据cache-control决定是否从网络上取数据。
         * LOAD_NO_CACHE: 不使用缓存，只从网络获取数据.
         * LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据。
         */
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);//不使用缓存，只从网络获取数据.

        //支持屏幕缩放
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);

        //不显示webview缩放按钮
        //        webSettings.setDisplayZoomControls(false);

        //initPermisionAround

        //设置是够去下载文件
        goDownData(0);
    }

    //WebViewClient主要帮助WebView处理各种通知、请求事件
    private WebViewClient webViewClient=new WebViewClient(){
        @Override
        public void onPageFinished(WebView view, String url) {//页面加载完成
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {//页面开始加载
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i("ansen","拦截url:"+url);
            if(url.equals("http://www.google.com/")){
                Toast.makeText(MainActivity.this,"国内不能访问google,拦截该url",Toast.LENGTH_LONG).show();
                return true;//表示我已经处理过了
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        private Map<String, InputStream> streamMap = new HashMap<>();

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

            Map<String, String> requestHeaders = request.getRequestHeaders();
            String url = request.getUrl().toString().replaceAll("^.*?//","");

            try{ url = UrlUtil.decode(url, "utf-8");
            } catch (UnsupportedEncodingException e){}

            if( url.startsWith(webAudioDir) ) {
                if( url.endsWith(".js") ) {
                    try {
                        String cache = getCacheFilePath( url, webAudioDir );
                        File file = new File(cache);
                        byte[] resBates = IOUtils.readBytes(file);
                        ByteArrayInputStream inStream = new ByteArrayInputStream(resBates);
                        return new WebResourceResponse("application/x-javascript", "utf-8", inStream);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else if( url.endsWith(".mp3")) {

                    String cache = getCacheFilePath( url, webAudioDir );
                    File file = new File(cache);
                    long fSize = file.length();
                    if( file.exists() ) {
                        Log.i("ansen","找到缓存:"+url);
                        Log.i("ansen","找到缓存路径:"+cache);
                        try {

                            /*//解决拦截流的未加载点击 【发生 当下载时发生 错误】 问题 的尝试, 不能解决问题, 无效代码
                            InputStream oldStream = streamMap.get(url);
                            //LogUtils.i("...." + oldStream.available());
                            if( oldStream != null && oldStream.available() != 1  )
                                oldStream.close();
                            streamMap.put(url, inStream);*/

                            String[] rangeNums = requestHeaders.get("Range").replace("bytes=","").split("-");
                            long rgStart = Long.parseLong( rangeNums[0] );
                            long rgEnd = rangeNums.length == 1 ? fSize-1: Long.parseLong( rangeNums[1] );
                            LogUtils.i("...请求起始位置: " + rgStart + ", 请求结束位置: " + rgEnd);

                            Map<String,String> responseHeaders = new HashMap<String, String>();
                            responseHeaders.put("Accept-Ranges","bytes");
                            String mimeType = "audio/mp3";

                            if (rgStart >= fSize || rgEnd >= fSize || rgStart > rgEnd) {
                                LogUtils.i("request out of range: " + fSize);
                                responseHeaders.put("Content-Range", String.format("bytes -/%d", fSize));
                                return new WebResourceResponse(mimeType, null, 416, "Requested range not satisfiable", responseHeaders, null);
                            }

                            int outBateLen = (int) (rgEnd - rgStart + 1); //断点续传这个 起始位置和结束位置 都包含
                            byte[] resBates = IOUtils.readBytes(file);
                            /*byte[] outBates = new byte[outBateLen];
                            System.arraycopy(resBates, (int)(rgStart), outBates, 0, outBateLen);
                            responseHeaders.put("Content-Range", String.format("bytes %d-%d/%d", rgStart, rgEnd, fSize));
                            ByteArrayInputStream inStream = new ByteArrayInputStream(outBates);*/
                            responseHeaders.put("Content-Range", String.format("bytes %d-%d/%d", rgStart, rgEnd, fSize));
                            ByteArrayInputStream inStream = new ByteArrayInputStream(resBates); inStream.skip(rgStart);
                            BrokenInputStream binStream = new BrokenInputStream(inStream, rgStart, outBateLen);

                            return new WebResourceResponse(mimeType, null, 206, "Partial content",responseHeaders, binStream);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return super.shouldInterceptRequest(view, url);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            LogUtils.i("loaded:" + url);
            super.onLoadResource(view, url);
        }


    };

    //WebChromeClient主要辅助WebView处理Javascript的对话框、网站图标、网站title、加载进度等
    private WebChromeClient webChromeClient=new WebChromeClient(){
        //不支持js的alert弹窗，需要自己监听然后通过dialog弹窗
        @Override
        public boolean onJsAlert(WebView webView, String url, String message, JsResult result) {
            AlertDialog.Builder localBuilder = new AlertDialog.Builder(webView.getContext());
            localBuilder.setMessage(message).setPositiveButton("确定",null);
            localBuilder.setCancelable(false);
            localBuilder.create().show();

            //注意:
            //必须要这一句代码:result.confirm()表示:
            //处理结果为确定状态同时唤醒WebCore线程
            //否则不能继续点击按钮
            result.confirm();
            return true;
        }

        //获取网页标题
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            Log.i("ansen","网页标题:"+title);
        }

        //加载进度回调
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setProgress(newProgress);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (webView.canGoBack() && keyCode == KeyEvent.KEYCODE_BACK){//点击返回按钮的时候判断有没有上一页
            Log.i("ansen","是否有上一个页面:"+webView.canGoBack());
            webView.goBack(); // goBack()表示返回webView的上一页面
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    /**
     * JS调用android的方法
     * @param str
     * @return
     */
    @JavascriptInterface //仍然必不可少
    public void  getClient(String str){
        String addr = TtlLogic.parse(str);
        Log.i("ansen","html调用客户端:"+ addr );
        //webView.loadDataWithBaseURL("file:///android_asset/web/","<html><head> <title> 欢迎您 </title> <script src=\"./dg_data.js\"></script> </head><body> <script>for(var i in ms){document.write(ms[i].sound);}</script> </body></html>", "text/html", "UTF-8", null);
        //wxRunJs(addr);
        if( checkJsExist(str) ) {
            dynLoadAssHtml();
        } else
            webView.loadUrl("javascript:xp.rstSetter(\""+ termTtl +"\")");
    }

    @JavascriptInterface //仍然必不可少
    public void  goLtnHtml(){
        dynLoadAssLtnHtml();
    }
    @JavascriptInterface //仍然必不可少
    public void  goLsnHtml(){
        dynLoadAssHtml();
    }

    public void wxRunJs(String toastStr) {
        webView.loadUrl("javascript:toast(\""+ toastStr +"\")");

    }

    public boolean checkJsExist(String ttl) {

        //
        String addr = TtlLogic.parse(ttl);
        String dynDataJs =  webAudioDir + "/grade/" + addr + "/dg_data.js";
        File cacheJs = new File( getCacheFilePath(dynDataJs) );
        if( !cacheJs.exists() ) {
            wxRunJs("当前选择数据集 " +ttl+ " 没有下载....");
            return false;
        } else {
            termTtl = ttl;
            return true;
        }
    }

    public void dynLoadAssLtnHtml(){
        String tmpl = AndPkgUtils.loadAssetAsString("web/lrc_js_listen - 移动化.html");

        String addr = TtlLogic.parse(termTtl);
        String dynDataJs =  webAudioDir + "/grade/" + addr + "/word_ln_data.js";
        tmpl = tmpl.replace("./word_ln_data.js","http://"+ dynDataJs);
        webView.loadDataWithBaseURL("file:///android_asset/web/", tmpl, "text/html", "UTF-8", null);
    }


    public void dynLoadAssHtml(){
        String tmpl = AndPkgUtils.loadAssetAsString("web/lrc_js_检测无效资源 - 真机适配 - 移动化 基于常识的处理方式.html");

        //分析 当前对应 的文件是否存在
        if(tmpl.equals("")) {
            wxRunJs("没有读取到模板文件！！！！");
            return ;
        }

        String addr = TtlLogic.parse(termTtl);
        String dynDataJs =  webAudioDir + "/grade/" + addr + "/dg_data.js";
        tmpl = tmpl.replace("./dg_data.js","http://"+ dynDataJs);
        tmpl = tmpl.replace("仁爱 / 初中 / 八年级 / 上",termTtl);
        webView.loadDataWithBaseURL("file:///android_asset/web/", tmpl, "text/html", "UTF-8", null);
        LogUtils.i(tmpl);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //释放资源
        webView.destroy();
        webView=null;
    }


    public String getCacheFilePath(String fileUrl){
        return this.getCacheFilePath(fileUrl, webAudioDir);
    }

    public String getCacheFilePath(String fileUrl, String webBigDir){
        //String fileName=fileUrl.substring(fileUrl.lastIndexOf("/")+1,fileUrl.length());//获取文件名称
        String newFilePath= fileUrl.replace(webBigDir, Environment.getExternalStorageDirectory() + File.separator + res);
        return newFilePath;
    }


    //数据包信息
    Handler handler = new Handler();
    private Runnable task = new Runnable() {
        public void run() {
            String url = "http://3fcd3b5572-pc:8080/grade.zip";
            String filePath =  Environment.getExternalStorageDirectory() + File.separator + res + File.separator +"grade.zip";
            LogUtils.i("..." + filePath);
            File file = new File(filePath);
            if( !file.exists() )
                AppUpdateMode.getInstance().download(url ,filePath);
        }
    };



    private final int REQUESTCODE = 101;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUESTCODE) {
            //询问用户权限
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
                //用户同意
            } else {
                //用户不同意
                this.finish();
            }
        }
    }
    public void goDownData(int waitMs){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            int checkSelfPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(checkSelfPermission == PackageManager.PERMISSION_DENIED){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUESTCODE);
            }
        }


        //初始化sd卡资源文件夹
        String res = this.getString(R.string.res);
        File flie = new File( Environment.getExternalStorageDirectory() + File.separator + res );
        flie.mkdirs();

        //去下数据
        handler.postDelayed(task,25000);
    }

}


/**
private WebResourceResponse loadBookUri(XWalkView view, Uri uri, String range) {
    HashMap<String, String> headers = new HashMap<String, String>();
    String mimeType = getMimeType(uri);

    try {
        String path = uri.getPath().substring(BOOK_URI.getPath().length());
        File file = new File(getRootDir(mScene), path);

        if (!file.canRead()) {
            throw new IOException(file + " can not be read");
        }

        final long total_length = file.length();
        long range_start = 0;
        long range_end = total_length - 1;
        boolean partial = false;

        if (range != null) {
            range = range.replace("bytes=", "");
            Pattern p = Pattern.compile("(\\d+)\\-(\\d*)");
            Matcher m = p.matcher(range);
            if (m.find()) {
                partial = true;
                range_start = Long.parseLong(m.group(1));
                String str = m.group(2);
                if (!str.isEmpty()) {
                    range_end = Long.parseLong(str);
                }
                if (range_start >= total_length || range_end >= total_length || range_start > range_end) {
                    Log.w(TAG, "request out of range: " + range);
                    headers.put("Content-Range", String.format("bytes /%d", total_length));
                    return createResponse(mimeType, 416, "Requested range not satisfiable", headers, null);
                }
            }
        }

        InputStream inputStream = new FileInputStream(file);
        int status = 200;
        String reason = "OK";
        headers.put("Cache-Control", "no-cache");
        headers.put("Accept-Ranges", "bytes");
        headers.put("Content-Encoding", "identity");

        if (partial) {
            if (range_start > 0) {
                inputStream.skip(range_start);
            }
            inputStream = new BrokenInputStream(inputStream, IS_LEGACY ? 0 : range_start, range_end - range_start + 1);
            status = 206;
            reason = "Partial content";
            headers.put("Content-Range", String.format("bytes %d-%d/%d", range_start, range_end, total_length));
        }

        return createResponse(mimeType, status, reason, headers, inputStream);
    } catch (Exception e) {
        Log.w(TAG, "fail to read file", e);
        return createResponse(mimeType, 404, "Not found", headers, null);
    }
}

public boolean onKeyDown(int keyCode, KeyEvent event) {
    // TODO Auto-generated method stub
    switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            this.tbsSuiteExit();
    }
    return super.onKeyDown(keyCode, event);
}

private void tbsSuiteExit() {
    // exit TbsSuite?
    AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
    dialog.setTitle("X5功能演示");
    dialog.setPositiveButton("OK", new AlertDialog.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            // TODO Auto-generated method stub
            Process.killProcess(Process.myPid());
        }
    });
    dialog.setMessage("quit now?");
    dialog.create().show();
}


// 获取文件保存路径 sdcard根目录/download/文件名称
public static String getSaveFilePath(String fileUrl){
    String fileName=fileUrl.substring(fileUrl.lastIndexOf("/")+1,fileUrl.length());//获取文件名称
    String newFilePath= Environment.getExternalStorageDirectory() + "/Download/"+fileName;
    return newFilePath;
}
 */

/**
 ref1: https://blog.csdn.net/lowprofile_coding/article/details/77928614 (参考 作webview项目创建)
 ref2: https://www.jianshu.com/p/6c796944a19f , https://www.jianshu.com/p/3a9549fc8504 (参考 加载本地html设置)
 ref3: https://blog.csdn.net/qq_36252044/article/details/77993297 (资料 webviewclient方法和浏览器设置默认编码)
        https://www.jb51.net/article/139424.htm (资料 下载使用)
        https://blog.csdn.net/weixin_41885053/article/details/81011853 (资料 内存泄露问题)
        shouldOverrideUrlLoading (已过时, 意义不大, 基本不用)
        https://blog.csdn.net/qiyei2009/article/details/52786845 (资料 其绕载逻辑 值的赞赏,但由于 shouldOverrideUrlLoading已过时,代码已无效)

 转用x5内核
 ref3: https://x5.tencent.com/tbs/guide/sdkInit.html (资料 tbs使用步骤)
 ref4: https://blog.csdn.net/u012982629/article/details/81357154 (资料 关于JavascriptInterface没有匹配的类的处理)
 ref5: https://www.jianshu.com/p/4cf6dff6657b (资料 去掉QQ浏览器推广)

 速度优化
 ref6: https://www.jb51.net/article/139424.htm (资料 缓存文件夹设置)
 ref7: https://blog.csdn.net/lowprofile_coding/article/details/77852131 (资料 写sdcard)
       https://github.com/ansen666/checkupdate
 ref8: https://blog.csdn.net/lyhhj/article/details/49517537 (资料 缓存文件夹位置)
 ref9: https://blog.csdn.net/lcwoooo/article/details/74474640 (参考 缓存实现, 拦截加载本地内容)
 ref10: https://blog.csdn.net/u013700502/article/details/69388727 (资料 加载html字符串内容,其它内容无视)
        https://www.jianshu.com/p/5e7075f4875f (资料 wx相关缓存作用)
 ref11: https://blog.csdn.net/csdn_loveqingqing/article/details/79002978 (参考 资源包下载解压实现)
        https://blog.csdn.net/qq_17853651/article/details/79180792 (参考 okgo3.0使用,这个的下载用处不大)
        https://github.com/hytcyjb/yjboAndroidProject/blob/f3cafeae7288c2cc54896b07c537ddd8329f5db1/okhttp-OkGo-master/app/src/main/java/com/lzy/demo/GApp.java (资料 初始化okGo的类说明)
        https://blog.csdn.net/qq_37982823/article/details/81171743 (参考 okgo下载功能的使用)

 android开发规范
 ref12: https://blog.csdn.net/y874961524/article/details/57413814 (资料 资源string使用)


 ref13: https://github.com/KingJA/WisdomE/blob/master/app/src/main/java/com/kingja/cardpackage/util/ToastUtil.java (参考 实现ToastUtils)
 ref14: https://blog.csdn.net/weiye__lee/article/details/82749079 (参考 下载更新UI)
        https://github.com/itlwy/AppSmartUpdate/tree/70400297374923ec45b972790f5c35c220dd3ea6/smartupdate/src/main
        https://blog.csdn.net/qq_35434831/article/details/80233126 (参考 下载更新通知栏实现)
        https://github.com/sai251716795/HY_Loan/blob/2af4d7bc1b0aedc1f232f19204fabe46973722d7/app/src/main/java/com/yhx/loan/server/update/AppUpdateService.java

 ref15: https://zhidao.baidu.com/question/1801184194895597147.html (参考 正则表达式匹配最后某个字符。使用耗力高,本末倒置,改用了lastIndexOf)
        正则匹配乃是从前往后循规而找，并不是拿整体来找（后面的部分并不会影响前面的）
        如：
        "/xxt/dd/dd.xp".replaceAll( "/.*?)$", "" ) => ""

 ref16: https://www.cnblogs.com/caobotao/p/5020857.html (资料 复习AsyncTask)
 ref15: https://blog.csdn.net/u013040434/article/details/60581009 (资料 解压确有中文乱码问题)
        https://www.jianshu.com/p/356f0bb537d8 (参考 修正解压中文问题,其代码存在严重问题,且编码设定也存在问题)
        https://blog.csdn.net/weixin_40855673/article/details/79301122 (参考 解压zip编码设定说明)

 ref16: https://github.com/wafs702/rpg_maker_mv_android_sample/blob/5cce7583879d94c83f925375c1b41af8489f2a73/app/src/main/java/com/example/user/rpgmakermvandroidsample/MainActivity.java (资料 缓存端开启跨域)
 ref17: https://www.51-n.com/t-4306-1-1.html (资料 使用动态写数据不能设定audio.currentTime的问题)
        https://github.com/BenQdigiPages/books_sandbox_android/blob/f9f9d947009eee6dbaae98d2726ef6f66fc7c9cb/app/src/main/java/com/books/viewer/ViewerBridge.java
 https://github.com/BenQdigiPages/books_sandbox_android/blob/f9f9d947009eee6dbaae98d2726ef6f66fc7c9cb/app/src/main/java/com/books/viewer/ViewerBridge.java (参考 解决webResurceResponce未加载时点击下载出错问题,这个应该是对本地拦截流的处理,下载的流确是正常的截断数据)

 ref18: https://blog.csdn.net/educast/article/details/52332496 (参考 截取数组)

 无线调试
 ref19: https://blog.csdn.net/zhenwenxian/article/details/5901289 (资料 adb连接方法)
        https://www.cnblogs.com/jiangzhaowei/p/8205490.html (资料 连接失败原因)

 兼容 6.0
 ref20: https://blog.csdn.net/d13771862759/article/details/79204313 (参考 兼容6.0 写sd卡)

 修改 progressbar 颜色
 ref21:https://blog.csdn.net/samguoyi/article/details/7677753 (资料 配置文件方式修改,较麻烦 故放弃)
 ref22: https://blog.csdn.net/lixiaoshuai_91/article/details/52550236 (资料 当前style的drawable属性设定)
 ref23: https://blog.csdn.net/buptlzx/article/details/8294979 (资料 构建ColorDrawable对象,其他无视)
 ref24: https://blog.csdn.net/qq_19269585/article/details/82707549 (资料 代码中动态的构建drawable, 设定到 指定的id 的资源里)
 ref25: https://blog.csdn.net/yitianhao000/article/details/54315870 (资料 代码构建layer-list图层实现)
 ref26: https://blog.csdn.net/chen930724/article/details/49807821 (资料 关于progressbar style的说明)

 ref27: https://blog.csdn.net/zhaokx3/article/details/52879691 (参考 rgb值转color对象)
 ref28: https://www.2cto.com/kf/201402/278145.html (资料 color的设置方法)

 混淆资料
 ref29: https://blog.csdn.net/doris_d/article/details/52610074 (资料 proguard-android.txt)
 ref30: https://blog.csdn.net/doris_d/article/details/52609703 (资料 基本混淆配置)
 ref31: https://www.jianshu.com/p/f8735c3cea1e (资料 混淆配置实例,部分内容存在错误)
 ref32: https://blog.csdn.net/guolipeng_network/article/details/74551968 (资料 比较不错的混淆模板)

 ref33: http://wiki.open.qq.com/wiki/%E4%BB%A3%E7%A0%81%E6%B7%B7%E6%B7%86 (资料 x5内核的混淆)
 ref34: https://www.jianshu.com/p/6aa5cb272514 (资料 okgo的混淆)

 ref36: https://blog.csdn.net/w690333243/article/details/63679734 (资料 libraryjars参数)
        https://zhidao.baidu.com/question/1947706140000595908.html?qbl=relate_question_4&word=android%20%D6%BB%BB%EC%CF%FD%CA%E4%C8%EB%B5%C4%B4%FA%C2%EB%2C%B2%BB%BB%EC%CF%FD%D2%C0%C0%B5
 ref37: https://blog.csdn.net/weimingjue/article/details/84976058 (参考 只混淆自己的实例)

 ref38: https://blog.csdn.net/katrinawj/article/details/80016315 (资料 dex2jar 2.0 反编译测试)
        旧版本的jdk、apktool也能反编译dex文件
 ref39: https://blog.csdn.net/hanchao5272/article/details/79379803 （资料 jdk版本和major版本）

 ref40: https://blog.csdn.net/sunzhenglin2016/article/details/78368676 (资料 proguard-rules\release\aapt_rules.txt FileNotFoundException)
        https://www.cnblogs.com/tuike/p/7018280.html (资料 Job failed, see logs for details)

 ref41: https://blog.csdn.net/lowprofile_coding/article/details/77928614 （参考 Java和JavaScript互调实现）
        https://www.jb51.net/article/139424.htm （参考 java调js实现）

 ref42: https://zhidao.baidu.com/question/374231948.html (参考 assets子文件夹如何读,看 k67395333 回答)

 ref43: https://blog.csdn.net/ainuser/article/details/79166463 (参考 修改git配置,使用见 app/git-history.txt)
 */