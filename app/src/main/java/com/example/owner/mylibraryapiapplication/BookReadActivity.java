package com.example.owner.mylibraryapiapplication;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Mark on 14/10/2016.
 */
public class BookReadActivity extends AppCompatActivity {

        private WebView webview;
        private static final String TAG = "Main";
        private ProgressDialog progressBar;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.read_book);
            String book_url = (String) getIntent().getSerializableExtra(BookDisplayActivity.BOOK_DETAIL_URL);
            this.webview = (WebView)findViewById(R.id.webview);
            // Needs to have JavaScript enabled
            if (webview != null) {
                WebSettings settings = webview.getSettings();
                settings.setJavaScriptEnabled(true);
                webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            }
            // Let the user know what is going on
            progressBar = ProgressDialog.show(BookReadActivity.this, "WebView Book from Open Library", "Loading...");

            webview.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Log.i(TAG, "Processing webview url click...");
                    view.loadUrl(url);
                    return true;
                }

                public void onPageFinished(WebView view, String url) {
                    Log.i(TAG, "Finished loading URL: " +url);
                    if (progressBar.isShowing()) {
                        progressBar.dismiss();
                    }
                }
            });

            webview.loadUrl(book_url);
        }

}
