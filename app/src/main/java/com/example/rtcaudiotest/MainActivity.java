package com.example.rtcaudiotest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.rtcaudiotest.codec.MyAudioPlayer;
import com.example.rtcaudiotest.codec.MyAudioRecorder;
import com.example.rtcaudiotest.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'rtcaudiotest' library on application startup.
//    static {
//        System.loadLibrary("rtcaudiotest");
//    }

    private boolean isGranted = false;
    private ActivityMainBinding binding;
    private Button imgPlay,imgRecoder;
    private EditText ipInput, sendPortInput, receivePortInpurt;
    private MyAudioPlayer audioPlayer;
    private MyAudioRecorder audioRecorder;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        audioPlayer = new MyAudioPlayer();
        audioRecorder = new MyAudioRecorder();
        preferences = getPreferences(Context.MODE_PRIVATE);

        // Example of a call to a native method
//        TextView tv = binding.sampleText;
//        tv.setText(stringFromJNI());
        imgPlay = binding.playingView;
        imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((Boolean) audioPlayer.isPlaying().getValue()) {
                    audioPlayer.stop();
                    imgPlay.setText("播放");
                } else {
                    int receiveport = 0;
                    if (receivePortInpurt.getText() == null) {
                        Toast.makeText(MainActivity.this,"需要输入接收端口",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    receiveport = Integer.parseInt(receivePortInpurt.getText().toString());
                    if (receiveport == 0) {
                        Toast.makeText(MainActivity.this,"输入的接收端口有误",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    audioPlayer.start(false,receiveport);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("rtp_receivePort",receiveport);
                    editor.commit();
                    imgPlay.setText("停止播放");
                }
            }
        });

        imgRecoder = binding.recoderView;
        imgRecoder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((Boolean) audioRecorder.isRecording().getValue()) {
                    audioRecorder.stop();
                    imgRecoder.setText("开始录音");
                } else {
                    if (!isGranted) {
                        requestAudioPermission();
                        return;
                    }
                    String ip = ipInput.getText().toString();
                    if(TextUtils.isEmpty(ip)) {
                        Toast.makeText(MainActivity.this,"需要输入Ip地址",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int sendport = 0;
                    if (sendPortInput.getText() == null) {
                        Toast.makeText(MainActivity.this,"需要输入发送端口",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sendport = Integer.parseInt(sendPortInput.getText().toString());
                    if (sendport == 0) {
                        Toast.makeText(MainActivity.this,"输入的发送端口有误",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    audioRecorder.start(ipInput.getText().toString(),sendport,false);
                    imgRecoder.setText("停止录音");
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("rtp_ip",ip);
                    editor.putInt("rtp_sendport",sendport);
                    editor.commit();
                }
            }
        });

        ipInput = binding.ipInput;
        sendPortInput = binding.sendportInput;
        receivePortInpurt = binding.receiveportInput;
        String ip = preferences.getString("rtp_ip","");
        int sendPort = preferences.getInt("rtp_sendport",0);
        int receivePort = preferences.getInt("rtp_receivePort",0);
        if (!TextUtils.isEmpty(ip)) {
            ipInput.setText(ip);
        }
        if (sendPort != 0) {
            sendPortInput.setText(String.valueOf(sendPort));
        }
        if (receivePort != 0) {
            receivePortInpurt.setText(String.valueOf(receivePort));
        }
        requestAudioPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions == null || grantResults == null) {
            return;
        }
        if (requestCode == 101) {
            String var10000 = permissions[0];
            if (var10000 != null) {
                if (var10000.contains(Manifest.permission.RECORD_AUDIO)) {
                    int grantResult = grantResults[0];
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        this.isGranted = true;
                        Toast.makeText((Context)this, (CharSequence)"audio grant success!!!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText((Context)this, (CharSequence)"audio grant fail!!!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void requestAudioPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 101);
        } else {
            isGranted = true;
        }
    }

    /**
     * A native method that is implemented by the 'rtcaudiotest' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}