package com.kezy.tset;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.AppBarLayout;

public class MainActivity2 extends AppCompatActivity {

//    private SwipeRefreshLayout refreshLayout;

    private WebView webView;

    private AppBarLayout appBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Intent intent = getIntent();
        if (intent != null) {
            //获取整个uri的链接
            String dataString = intent.getDataString();
            //获取相应Uri中的一些内容！

            Uri data = intent.getData();
            if (data != null) {
                String scheme = data.getScheme();
                String authority = data.getAuthority();
                String host = data.getHost();
                String port = String.valueOf(data.getPort());
                String path = data.getPath();
                String query = data.getQuery();
            }
            Log.e("---------msg", " -------- dataString " + dataString);
        }


//        refreshLayout = findViewById(R.id.refresh_layout);
        webView = findViewById(R.id.webview);
        appBarLayout = findViewById(R.id.appbar_layout);


//        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                refreshLayout.setRefreshing(false);
//            }
//        });

        webView.loadUrl("https://www.kuaishou.com/");
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());

//        // 解决 SwipeRefreshLayout， AppBarLayout， recycleview 滑动冲突
//        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
//            @Override
//            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
//
//                if (verticalOffset >= 0) {
//                    refreshLayout.setEnabled(true);
//                } else {
//                    refreshLayout.setEnabled(false);
//                }
//            }
//        });


    }

    private class MyChrome extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            webView.loadUrl(String.valueOf(request.getUrl()));
            return true;
        }


    }

}