package com.example.rtcaudiotest.codec;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dragon.rtplib.RtpWrapper;
import com.example.rtcaudiotest.codec.encode.AudioEncodeCodec;

import java.nio.ByteBuffer;

public class MyAudioRecorder {
    private static final String TAG = "MyAudioRecorder";
    private final MutableLiveData _isRecording = new MutableLiveData();
    private final LiveData isRecording;
    private final int audioPayloadType;
//    private final int audioRtpPort;
    private final int audioBitRate;
    private final int audioChannelCount;
    private final int audioSampleRate;
    private final int audioMinBufferSize;
    private final int audioMaxBufferSize;
    private final byte[] audioBuffer;
    private long lastSendAudioTime;
    private final int audioProfile;
    private final int audioIndex;
    private final byte[] audioSpecificConfig;
    private final byte[] auHeaderLength;
    private RtpWrapper audioRtpWrapper;
    private BaseCodec audioCodec;
    private byte[] bufferArray;

    public final LiveData isRecording() {
        return this.isRecording;
    }

    private final byte[] auHeader(int len) {
        byte[] var2 = new byte[2];
        var2[0] = (byte)((len & 8160) >> 5);
        var2[1] = (byte)((len & 31) << 3);
        return var2;
    }

    public MyAudioRecorder() {
        this.isRecording = (LiveData)_isRecording;
        this.audioPayloadType = 97;
//        this.audioRtpPort = sendPort; // 40020
        this.audioBitRate = 131072;
        this.audioChannelCount = 1;
        this.audioSampleRate = 44100;
        this.audioMinBufferSize = AudioRecord.getMinBufferSize(this.audioSampleRate, this.audioChannelCount == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        this.audioMaxBufferSize = this.audioMinBufferSize * 3;
        this.audioBuffer = new byte[this.audioMaxBufferSize];
        this.audioProfile = 1;
        this.audioIndex = 4;
        audioSpecificConfig = new byte[2];
        audioSpecificConfig[0] = (byte)(this.audioProfile + 1 << 3 & 255 | this.audioIndex >>> 1 & 255);
        audioSpecificConfig[1] = (byte)(this.audioIndex << 7 & 255 | this.audioChannelCount << 3 & 255);
        auHeaderLength = new byte[2];
        auHeaderLength[0] = 0;
        auHeaderLength[1] = 16;
        this.bufferArray = new byte[this.audioMinBufferSize * 2];
        this._isRecording.setValue(false);
    }


    public void start(final String ip, final int sendPort,final boolean hasAuHeader) {
        if(TextUtils.isEmpty(ip)) {
            android.util.Log.w(TAG,"ip is empty-----");
            return;
        }
        if ((Boolean)_isRecording.getValue() != null) {
            if (!(Boolean)_isRecording.getValue()) {
                this._isRecording.setValue(true);
                MediaFormat mediaFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, this.audioSampleRate, this.audioChannelCount);
                if (mediaFormat == null) {
                    android.util.Log.w(TAG,"createAudioFormat error,mediaFormat is null-----");
                    return;
                }
                mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, this.audioBitRate);
                mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
                mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, this.audioMaxBufferSize);
                audioCodec = new AudioEncodeCodec(mediaFormat) {
                    public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
                        try {
                            ByteBuffer buffer = codec.getOutputBuffer(index);
                            if (buffer == null) {
                                android.util.Log.w(TAG,"onOutputBufferAvailable getOutputBuffer byteBuffer is null-----");
                                return;
                            }

                            if (lastSendAudioTime == 0L) {
                                lastSendAudioTime = info.presentationTimeUs;
                            }

                            long increase = (info.presentationTimeUs - lastSendAudioTime) * (long)audioSampleRate / (long)1000 / (long)1000;
                            if (hasAuHeader) {
                                buffer.position(info.offset);
                                buffer.get(bufferArray, 4, info.size);
                                byte[] var7 = auHeaderLength;
                                bufferArray[0] = var7[0];
                                bufferArray[1] = var7[1];
                                var7 = auHeader(info.size);
                                bufferArray[2] = var7[0];
                                bufferArray[3] = var7[1];
                                if (audioRtpWrapper != null) {
                                    audioRtpWrapper.sendData(bufferArray, info.size + 4, 97, true, (int)increase);
                                }
                            } else {
                                buffer.position(info.offset);
                                buffer.get(bufferArray, 0, info.size);
                                if (audioRtpWrapper != null) {
                                    audioRtpWrapper.sendData(bufferArray, info.size, 97, true, (int)increase);
                                }
                            }
                            lastSendAudioTime = info.presentationTimeUs;
                            codec.releaseOutputBuffer(index, false);
                        } catch (Exception var10) {
                            var10.printStackTrace();
                        }

                    }

                    public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
                        audioRtpWrapper = new RtpWrapper();
                        if (audioRtpWrapper != null) {
                            audioRtpWrapper.open(sendPort, audioPayloadType, audioSampleRate);
                            audioRtpWrapper.addDestinationIp(ip);
                        }
                    }
                };
                audioCodec.init();
            }
        }
    }

    public void stop() {
        if ((Boolean)_isRecording.getValue() != null) {
            if ((Boolean)_isRecording.getValue()) {
                _isRecording.setValue(false);
                if (audioCodec != null) {
                    audioCodec.release();
                    if (audioRtpWrapper != null) {
                        audioRtpWrapper.close();
                    }
                }

            }
        }
    }

}
