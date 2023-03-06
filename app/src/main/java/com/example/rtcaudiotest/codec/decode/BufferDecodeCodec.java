package com.example.rtcaudiotest.codec.decode;

import android.media.MediaCodec;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.text.TextUtils;

import com.example.rtcaudiotest.codec.BaseCodec;

import java.io.IOException;

abstract public class BufferDecodeCodec extends BaseCodec {
    public BufferDecodeCodec(MediaFormat mediaFormat) {
        super(mediaFormat);
    }

    @Override
    protected MediaCodec onCreateMediaCodec(MediaFormat mediaFormat) throws IOException {
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
        String mediaCodecName = mediaCodecList.findDecoderForFormat(mediaFormat);
        if (TextUtils.isEmpty(mediaCodecName)) {
            throw new RuntimeException("not find the matched codec!!!!!!!");
        } else {
            android.util.Log.d("BufferDecodeCodec","onCreateMediaCodec mediaCodecName: " + mediaCodecName);
        }
        MediaCodec mediaCodec = MediaCodec.createByCodecName(mediaCodecName);
        return mediaCodec;
    }

    @Override
    protected void onConfigMediaCodec(MediaCodec mediaCodec) {
        mediaCodec.configure(mediaFormat, null, null, 0);
    }

}
