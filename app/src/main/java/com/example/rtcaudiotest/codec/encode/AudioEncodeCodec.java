package com.example.rtcaudiotest.codec.encode;

import static android.media.AudioFormat.CHANNEL_IN_MONO;
import static android.media.AudioFormat.CHANNEL_IN_STEREO;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaRecorder;

import java.nio.ByteBuffer;

public class AudioEncodeCodec extends BufferEncodeCodec {
    private AudioRecord audioRecord;
    private int sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
    private int channelCount = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
    private int minBufferSize;

    public AudioEncodeCodec(MediaFormat mediaFormat) {
        super(mediaFormat);
        minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelCount == 1 ? CHANNEL_IN_MONO : CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
    }

    @Override
    public void init() {
        super.init();
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, sampleRate,
                        channelCount == 1 ? CHANNEL_IN_MONO : CHANNEL_IN_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        2 * minBufferSize
                );
                audioRecord.startRecording();
                setAvailable(true);
            }
        });
    }

    @Override
    protected void releaseInternal() {
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
        }
        super.releaseInternal();
    }

    @Override
    protected void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public void onInputBufferAvailable(MediaCodec codec, int index) {
        try {
            ByteBuffer byteBuffer = codec.getInputBuffer(index);
            if (byteBuffer != null) {
                long startTime = System.currentTimeMillis();
                int readSize = audioRecord.read(byteBuffer, byteBuffer.capacity());
                String logStr = String.format("read time %s, read size %s", System.currentTimeMillis() - startTime, readSize);
                android.util.Log.d(AudioEncodeCodec.class.getSimpleName(), logStr);
                if (readSize < 0) {
                    readSize = 0;
                }
                codec.queueInputBuffer(index, 0, readSize, System.nanoTime() / 1000, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
