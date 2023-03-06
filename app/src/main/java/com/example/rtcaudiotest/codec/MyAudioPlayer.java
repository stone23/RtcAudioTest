package com.example.rtcaudiotest.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dragon.rtplib.RtpWrapper;
import com.example.rtcaudiotest.codec.decode.AudioDecodeCodec;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

public class MyAudioPlayer {
    private static final String TAG = "MyAudioPlayer";
    private final MutableLiveData _isPlaying = new MutableLiveData();
    private final LiveData isPlaying;
    private final RtpWrapper audioRtpWrapper;
    private AudioDecodeCodec audioDecodeCodec;
    private final ArrayBlockingQueue<Integer> indexArray;
    private final int audioChannelCount;
    private final int audioProfile;
    private final int audioIndex;
    private final ByteBuffer audioSpecificConfig;
    private long currentTime;

    public final LiveData isPlaying() {
        return this.isPlaying;
    }

    public MyAudioPlayer() {
        this.isPlaying = (LiveData)_isPlaying;
        this.audioRtpWrapper = new RtpWrapper();
        this.indexArray = new ArrayBlockingQueue(10);
        this.audioChannelCount = 1;
        this.audioProfile = 1;
        this.audioIndex = 4;
        byte[] var1 = new byte[2];
        var1[0] = (byte)(this.audioProfile + 1 << 3 & 255 | this.audioIndex >>> 1 & 255);
        var1[1] = (byte)(this.audioIndex << 7 & 255 | this.audioChannelCount << 3 & 255);
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put(var1);
        buffer.position(0);
        this.audioSpecificConfig = buffer;
        this._isPlaying.setValue(false);
    }

    public final void start(final boolean hasAuHeader,final int receivePort) {
        if (!(Boolean)_isPlaying.getValue()) {
            _isPlaying.setValue(true);
            int sampleRate = 44100;
            MediaFormat audioFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate,audioChannelCount);
            audioFormat.setByteBuffer("csd-0", this.audioSpecificConfig);
            this.indexArray.clear();
            audioDecodeCodec = (AudioDecodeCodec)(new AudioDecodeCodec(audioFormat) {
                public void onInputBufferAvailable(MediaCodec codec, int index) {
                    try {
                        indexArray.put(index);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            audioDecodeCodec.init();
            //40020
            audioRtpWrapper.open(receivePort, 97, 1000);
            audioRtpWrapper.setCallback((RtpWrapper.IDataCallback)(new RtpWrapper.IDataCallback() {
                public final void onReceivedData(byte[] data, int len) {
                    if (len >= 4) {
                        Integer index = null;
                        try {
                            index = (Integer)indexArray.take();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (currentTime == 0L) {
                            currentTime = System.currentTimeMillis();
                        }
                        ByteBuffer buffer = audioDecodeCodec.mediaCodec.getInputBuffer(index);
                        long time = (System.currentTimeMillis() - currentTime) * (long)1000;
                        if (hasAuHeader) {
                            if (buffer != null) {
                                buffer.position(0);
                            }
                            if (buffer != null) {
                                buffer.put(data, 4, len - 4);
                            }
                            if (buffer != null) {
                                buffer.position(0);
                            }
                            audioDecodeCodec.mediaCodec.queueInputBuffer(index, 0, len - 4, time, 1);
                        } else {
                            if (buffer != null) {
                                buffer.position(0);
                            }

                            if (buffer != null) {
                                buffer.put(data, 0, len);
                            }

                            if (buffer != null) {
                                buffer.position(0);
                            }
                            audioDecodeCodec.mediaCodec.queueInputBuffer(index, 0, len, time, 1);
                        }
                    } else {
                        Log.w(TAG,"onReceivedData len is: " + len);
                    }
                }
            }));
        }

    }

    public final void stop() {
        if ((Boolean)_isPlaying.getValue()) {
            _isPlaying.setValue(false);
            audioRtpWrapper.close();
            if (audioDecodeCodec != null) {
                audioDecodeCodec.release();
            }
        }
    }

//    public AudioDecodeCodec getAudioDecodeCodec() {
//        if (audioDecodeCodec != null) {
//            return audioDecodeCodec;
//        }
//        return null;
//    }
//
//    public void setAudioDecodeCode(AudioDecodeCodec var1) {
//        this.audioDecodeCodec = audioDecodeCodec;
//    }
}
