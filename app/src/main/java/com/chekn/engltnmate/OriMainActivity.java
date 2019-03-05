package com.chekn.engltnmate;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

/**
 * Created by CHEKN on 2018-12-24.
 */

public class OriMainActivity extends AppCompatActivity {

    WebView mWebview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 创建WebView对象
        mWebview = (WebView) findViewById(R.id.webview);

        // 支持与JS交互
        mWebview.getSettings().setJavaScriptEnabled(true);

        // 加载需要显示的网页
        mWebview.loadUrl("http://ip.cn/");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //释放资源
        mWebview.destroy();
        mWebview=null;
    }
}
