package com.example.androidride.webviewdownloadfileex;

import android.Manifest;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Chronometer;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG="AndroidRide";
    private WebView webview;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar= findViewById(R.id.progressBar);
        webview=(WebView)findViewById(R.id.webview);


       webview.loadUrl("https://www.foumovies.me/");
        webview.setWebChromeClient(new WebChromeClient(){

            @Override
            public void onProgressChanged(WebView view, int newProgress) {

                if(newProgress==100)
                {
                    progressBar.setVisibility(View.GONE);
                }
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
            }
        });
        webview.setWebViewClient(new WebViewClient(){
                                     @Override
                                     public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                         progressBar.setVisibility(View.VISIBLE);
                                         super.onPageStarted(view, url, favicon);
                                     }

                                     @Override
                                     public void onPageFinished(WebView view, String url) {

                                         super.onPageFinished(view, url);
                                     }
                                 }
                                 );


        webview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, final String userAgent, String contentDisposition, String mimetype, long contentLength)
            {
				//checking runtime permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        downloadDialog(url,userAgent,contentDisposition,mimetype);

                    } else {
						//requesting permissions
						ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                    }
                }
                else {
                    //Code for devices below API 23 or Marshmallow
                    Log.v(TAG,"Permission is granted");
                    downloadDialog(url,userAgent,contentDisposition,mimetype);

                }
            }
        });
    }
    public void downloadDialog(final String url,final String userAgent,String contentDisposition,String mimetype)
    {
		//filename using url.
        final String filename = URLUtil.guessFileName(url,contentDisposition,mimetype);
		//creates alertdialog
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
		//alertdialog title
        builder.setTitle("Download");
		//alertdialog message
        builder.setMessage("Do you want to save " +filename);
		
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
				//DownloadManager.Request created with url.
				DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
				//cookie
                String cookie=CookieManager.getInstance().getCookie(url);
				 //Add cookie and User-Agent to request
                request.addRequestHeader("Cookie",cookie);
                request.addRequestHeader("User-Agent",userAgent);
				 //file scanned by MediaScannar	
                request.allowScanningByMediaScanner();
				//Download is visible and its progress, after completion too.
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
				//DownloadManager created
                DownloadManager downloadManager=(DownloadManager)getSystemService(DOWNLOAD_SERVICE);
				//saves file in Download folder
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
				//download enqued
                downloadManager.enqueue(request);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) 
			{
			 //cancel the dialog if Cancel clicks
			dialog.cancel();
            }	

        });
		//alertdialog shows
        builder.create().show();

    }

    @Override
    public void onBackPressed() {
        if(webview.canGoBack()){
            webview.goBack();
        }
        super.onBackPressed();
    }
}
