package com.example.yisen614.photogallery;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Downloader<T> extends HandlerThread {

    private static final String TAG = "Downloader";
    private boolean mHasQuit = false;
    private static final int MESSAGE_DOWNLOAD = 0;
    private static final int GET_FROM_LOCAL = 1;
    private Handler mRequestHandler;
    private Handler mResponseHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private DownloadListener<T> downloadListener;
    private Map<String, Bitmap> drawableMap;

    public Downloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
        drawableMap = new HashMap<>();
    }

    public interface DownloadListener<T> {
        void onDownloaded(T target, Bitmap bitmap);
    }

    public void setDownloadListener(DownloadListener<T> listener) {
        downloadListener = listener;
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                final T target = (T) msg.obj;
                if(msg.what == MESSAGE_DOWNLOAD){
                    handleRequest(target);
                }else if (msg.what == GET_FROM_LOCAL){
                    final String url = mRequestMap.get(target);
                    if (mRequestMap.get(target) != url ||
                            mHasQuit) {
                        return;
                    }
                    final Bitmap bitmap = drawableMap.get(url);
                    mRequestMap.remove(target);
                    mResponseHandler.post(new Runnable() {
                        public void run() {
                            downloadListener.onDownloaded(target, bitmap);
                        }
                    });
                }
            }
        };
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }


    private void handleRequest(final T target) {
        try {
            final String url = mRequestMap.get(target);

            if (url == null) {
                return;
            }

            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);

            final Bitmap bitmap = BitmapFactory
                    .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);

            Log.i(TAG, "Bitmap created");

            mResponseHandler.post(new Runnable() {
                public void run() {
                    if (mRequestMap.get(target) != url ||
                            mHasQuit) {
                        return;
                    }
                    mRequestMap.remove(target);
                    downloadListener.onDownloaded(target, bitmap);
                    drawableMap.put(url, bitmap);
                }
            });
        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }

    public void queueDownload(T target, String url) {
        Log.i(TAG, "Got a url: " + url);
        if (url == null) {
            mRequestMap.remove(target);
        } else if (drawableMap.get(url) != null) {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(GET_FROM_LOCAL, target)
                    .sendToTarget();
        } else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
                    .sendToTarget();
        }
    }
}
