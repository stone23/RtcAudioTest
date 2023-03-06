package com.example.rtcaudiotest.codec.encode;

import android.media.MediaCodec;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.text.TextUtils;

import com.example.rtcaudiotest.codec.BaseCodec;

import java.io.IOException;

abstract public class BufferEncodeCodec extends BaseCodec {
    public BufferEncodeCodec(MediaFormat mediaFormat) {
        super(mediaFormat);
    }

    @Override
    protected MediaCodec onCreateMediaCodec(MediaFormat mediaFormat) throws IOException {
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
        String codecName = mediaCodecList.findEncoderForFormat(mediaFormat);
        if (TextUtils.isEmpty(codecName)) {
            throw new RuntimeException("not find the matched codec!!!!!!!");
        } else {
            android.util.Log.d("BufferEncodeCodec","onCreateMediaCodec mediaCodecName: " + codecName);
        }
        return MediaCodec.createByCodecName(codecName);
    }

    @Override
    protected void onConfigMediaCodec(MediaCodec mediaCodec) {
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }
}
