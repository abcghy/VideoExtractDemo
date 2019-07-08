package com.example.videoextractdemo;

import android.graphics.SurfaceTexture;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener,
        SurfaceTexture.OnFrameAvailableListener {

    private File cacheFile;
    private VideoDecoder videoDecoder;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextureView textureView = findViewById(R.id.textureView);

        textureView.setSurfaceTextureListener(this);

        cacheFile = new File(getCacheDir(), "ring-small.mp4");
        if (!cacheFile.exists()) {
            // 复制
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] data = new byte[1024];
                        InputStream is = getAssets().open("ring-small.mp4");
                        FileOutputStream fos = new FileOutputStream(cacheFile);
                        while (is.read(data) != -1) {
                            fos.write(data);
                        }
                        fos.flush();
                        is.close();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            start();
                        }
                    });
                }
            }).start();
        } else {
            start();
        }
    }

    private void start() {
        if (videoDecoder != null) {
            Log.d(TAG, "run: setpath");
            videoDecoder.setPath(cacheFile);
            try {
                videoDecoder.prepare(width, height);
                videoDecoder.start();
                Log.d(TAG, "run: start");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int width, height;

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//        VideoDecoder videoDecoder = new VideoDecoder(new Surface(surface));
        this.width = width;
        this.height = height;
        videoDecoder = new VideoDecoder(surface);
//        surface.setOnFrameAvailableListener(this);
        if (cacheFile.exists()) {
            start();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Log.e(TAG, "onFrameAvailable: , currenThread: " + Thread.currentThread().getName());
//        surfaceTexture.updateTexImage();
    }
}
