package com.example.rtcaudiotest.codec.decode;

import static android.media.AudioTrack.MODE_STREAM;
import static android.media.AudioTrack.WRITE_BLOCKING;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import java.nio.ByteBuffer;

public class AudioDecodeCodec extends BufferDecodeCodec {
    private static final String TAG = "AudioDecodeCodec";
    private AudioTrack audioTrack;

    public AudioDecodeCodec(MediaFormat mediaFormat) {
        super(mediaFormat);
    }

    @Override
    protected void setAvailable(boolean available) {
        this.available = available;
    }

    public final AudioTrack getAudioTrack() {
        return this.audioTrack;
    }

    public final void setAudioTrack(AudioTrack var1) {
        this.audioTrack = var1;
    }

    public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
        Log.d(TAG, "onOutputBufferAvailable " + index);
        try {
            if (codec == null) {
                Log.w(TAG, "onOutputBufferAvailable codec is  nulll----");
                return;
            }
            if (info == null) {
                Log.w(TAG, "onOutputBufferAvailable MediaCodec.BufferInfo is  nulll----");
                return;
            }
            ByteBuffer buffer = codec.getOutputBuffer(index);
            if (buffer == null) {
                Log.w(TAG, "onOutputBufferAvailable getOutputBuffer buffer is  nulll----");
                return;
            }
            buffer.position(info.offset);
            if (audioTrack != null) {
                audioTrack.write(buffer, info.size, WRITE_BLOCKING);
            }
            codec.releaseOutputBuffer(index, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
        if (format == null) {
            Log.w(TAG,"onOutputFormatChanged format is null ---");
            return;
        }
        int sampleRate = format.getInteger("sample-rate");
        int channelCount = format.getInteger("channel-count");
        int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelCount == 1 ? 16 : 12, 2);
        Log.d(TAG, "onOutputFormatChanged sampleRate " + sampleRate);
        Log.d(TAG, "onOutputFormatChanged channelCount " + channelCount);
        Log.d(TAG, "onOutputFormatChanged minBufferSize " + minBufferSize);
        audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, sampleRate, channelCount, AudioFormat.ENCODING_PCM_16BIT, minBufferSize, MODE_STREAM);
        if (audioTrack != null) {
            audioTrack.play();
        }
    }

    protected void releaseInternal() {
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
        }
        super.releaseInternal();
    }
}
