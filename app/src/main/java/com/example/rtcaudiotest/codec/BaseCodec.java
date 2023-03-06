package com.example.rtcaudiotest.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import androidx.annotation.NonNull;

import java.io.IOException;

abstract public class BaseCodec extends MediaCodec.Callback {
    protected MediaFormat mediaFormat;
    private HandlerThread handlerThread = new HandlerThread("HandlerThreadScope");
    protected Handler backgroundHandler;
    public static final int CALL_EVENT = 1001;
    public static final int QUIT_EVENT = CALL_EVENT + 1;
    protected boolean available = true;
    protected MediaCodec mediaCodec;


    public BaseCodec(MediaFormat mediaFormat) {
        this.mediaFormat = mediaFormat;
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == QUIT_EVENT) {
                    handlerThread.quitSafely();
                }
            }
        };

    }

    public void init() {
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mediaCodec = onCreateMediaCodec(mediaFormat);
                    onConfigMediaCodec(mediaCodec);
                    mediaCodec.setCallback(BaseCodec.this);
                    mediaCodec.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
        if(available) {
            backgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    BaseCodec.this.onInputBufferAvailable(codec,index);
                }
            });
        }
    }

    @Override
    public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
        if(available) {
            backgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    BaseCodec.this.onOutputBufferAvailable(codec,index,info);
                }
            });
        }
    }

    @Override
    public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
        android.util.Log.d(this.getClass().getSimpleName(), e.getMessage() == null ? "" : e.getMessage());
    }

    @Override
    public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
        if(available) {
            backgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    BaseCodec.this.onOutputFormatChanged(codec,format);
                }
            });
        }
    }

    public void release() {
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                releaseInternal();
                backgroundHandler.sendEmptyMessage(QUIT_EVENT);
                setAvailable(false);
            }
        });
    }

    protected void releaseInternal() {
        mediaCodec.flush();
        mediaCodec.stop();
        mediaCodec.release();
    }

    protected abstract MediaCodec onCreateMediaCodec(MediaFormat mediaFormat) throws IOException;

    protected abstract void onConfigMediaCodec(MediaCodec mediaCodec);
    protected abstract void setAvailable(boolean available);
}
