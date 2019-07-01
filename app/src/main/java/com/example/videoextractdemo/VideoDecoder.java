package com.example.videoextractdemo;

import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoDecoder implements Handler.Callback {
    private final int START_DECODE = 0;

    private File videoFile;

    private Surface mSurface;
    private MediaCodec mediaCodec;
    private MediaExtractor mediaExtractor;

    private HandlerThread handlerThread; // 子线程
    private Handler handler;

    public VideoDecoder(Surface surface) {
//        mediaCodec = MediaCodec.createDecoderByType("");
        this.mSurface = surface;
        handlerThread = new HandlerThread("video decoder");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper(), this);
    }

    public void setPath(File videoFile) {
        this.videoFile = videoFile;
    }

    public boolean prepare() throws IOException {
        MediaFormat mediaFormat = extractFormat();
        if (mediaFormat == null) return false; // 没找到视频轨道
        mediaCodec = MediaCodec.createDecoderByType(mediaFormat.getString(MediaFormat.KEY_MIME));
        mediaCodec.configure(mediaFormat, mSurface, null, 0);
        mediaCodec.start();
        return true;
    }

    public void start() {
        handler.sendEmptyMessage(START_DECODE);
    }

    public void release() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }
        if (mediaExtractor != null) {
            mediaExtractor.release();
            mediaExtractor = null;
        }
        sawInputEOS = true;
        sawOutputEOS = true;
    }

    public void destroy() throws Exception {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            release();
        }
        if (handlerThread != null) {
            handlerThread.quitSafely();
            handlerThread.join();
            handlerThread = null;
            handler = null;
        }
    }

    @Nullable
    private MediaFormat extractFormat() throws IOException {
        mediaExtractor = new MediaExtractor();
        mediaExtractor.setDataSource(videoFile.getAbsolutePath());
        int trackCount = mediaExtractor.getTrackCount();
        if (trackCount == 0) {
            return null;
        }
        for (int i = 0; i < trackCount; i++) {
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                mediaExtractor.selectTrack(i);
                return mediaFormat;
            }
        }
        return null;
    }

    private boolean sawInputEOS = false;
    private boolean sawOutputEOS = false;

    @WorkerThread
    private void _startDecode() {
        sawInputEOS = false;
        sawOutputEOS = false;
        doDecodeWork();
    }

    @WorkerThread
    private void doDecodeWork() {
        if (!sawInputEOS) {
            int inputBufferIndex = mediaCodec.dequeueInputBuffer(2000);
            if (inputBufferIndex >= 0) {
                ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                int chuckSize = mediaExtractor.readSampleData(inputBuffer, 0);
                if (chuckSize < 0) {
                    chuckSize = 0;
                    sawInputEOS = true;
                }
                long presentTime = mediaExtractor.getSampleTime();
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, chuckSize, presentTime,
                        sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : mediaExtractor.getSampleFlags());
                mediaExtractor.advance();
            } else {
                // input error
            }
        }
        if (!sawOutputEOS) {
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            int status = mediaCodec.dequeueOutputBuffer(info, 0);
            if (status >= 0) {
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) > 0) {
                    sawOutputEOS = true;
                }
                boolean render = info.size != 0;
                mediaCodec.releaseOutputBuffer(status, render);
                // 回调获取 bitmap
                // TODO: 2019-07-01 ghy
            } else {
                if (status == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // log
                } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

                } else if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // log
                } else {
                    // log
                }
            }
        }
        if (!sawInputEOS || !sawOutputEOS) {
            doDecodeWork();
        } else {
            // end
            // TODO: 2019-07-01 ghy
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case START_DECODE: {
                _startDecode();
                break;
            }
        }
        return false;
    }


    private interface OnBitmapGetListener {
        void onBitmapGet(Bitmap singleBitmap, long pts);
    }

    private OnBitmapGetListener mOnBitmapGetListener;

    public void setOnBitmapGetListener(OnBitmapGetListener onBitmapGetListener) {
        this.mOnBitmapGetListener = onBitmapGetListener;
    }
}
