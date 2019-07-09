package com.example.videoextractdemo;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class VideoRenderer implements Handler.Callback{
    public static final int INITIALIZE = 0;
    private static final int DRAW = 1;
    private static final int DESTROY = 2;

    private SurfaceTexture surfaceTexture;

    private HandlerThread handlerThread;
    private Handler handler;
    private OffscreenEGLConnection offscreenEGLConnection;
    private GifTexImage2DProgram program;

    public VideoRenderer(SurfaceTexture surfaceTexture) {
        this.surfaceTexture = surfaceTexture;
        handlerThread = new HandlerThread("video-renderer");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper(), this);
    }

    public void initialize() {
        handler.sendEmptyMessage(INITIALIZE);
    }

    private void _initialize() {
        offscreenEGLConnection = new OffscreenEGLConnection();
        offscreenEGLConnection.initialize(surfaceTexture);

        program = new GifTexImage2DProgram();
        program.initialize();
        program.onRecordChanged();
    }

    public void draw() {
        handler.sendEmptyMessage(DRAW);
    }

    private void _draw() {
        program.draw();
//        offscreenEGLConnection.draw();
    }

    public void destroy() {
        handler.sendEmptyMessage(DESTROY);
    }

    private void _destroy() {
        program.destroy();
        offscreenEGLConnection.destroy();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case INITIALIZE: {
                _initialize();
                break;
            }
            case DRAW: {
                _draw();
                break;
            }
            case DESTROY: {
                _destroy();
                break;
            }
        }
        return false;
    }
}
