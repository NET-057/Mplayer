package com.android.lnx.androsound1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Time;
import java.util.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    MediaPlayer media;
    AudioManager audio;
    SeekBar forward;
    TextView volume;
    TextView totaltime;
    static int current_time = 0;
    int maxivol;
    Button playandpause;
    SeekBar bar;
    Button voloff;
    Button volfull;
    Handler handler;
    Runnable runnable;
    int inc = 0;
    boolean playstatus = false;


    public void pause(View view)
    {
        media.pause();
    }

    public String calculatetime(Integer minisec)
    {
        int minute = minisec / 60 / 1000;
        int sec = (minisec / 1000) - (minisec / 60 / 1000) * 60;
        String minstr = Integer.toString(minute);
        String secstr = Integer.toString(sec);

        if ((minstr.length() == 1)) {
            minstr = "0" + minstr;
        }
        if (secstr.length() == 1) {
            secstr = "0" + secstr;
        }

        return minstr + ":" + secstr;



    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audio = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        maxivol = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        handler = new Handler();

        media = MediaPlayer.create(this,R.raw.yaara);


        playandpause = findViewById(R.id.playandpause);
        playandpause.setOnClickListener(this);

        voloff = findViewById(R.id.voloff);
        voloff.setOnClickListener(this);

        volfull = findViewById(R.id.volon);
        volfull.setOnClickListener(this);

        bar = findViewById(R.id.volumeseek);
        bar.setMax(maxivol);
        bar.setProgress(current);
        volume = findViewById(R.id.currentvol);
        volume.setText(Integer.toString(current)+"/"+Integer.toString(maxivol));
        TextView pxt = findViewById(R.id.timestart);
        pxt.setText("00:00");
        totaltime = findViewById(R.id.totaltime);
        totaltime.setText(calculatetime(media.getDuration()));

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {


                audio.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
                volume.setText(Integer.toString(i)+"/"+Integer.toString(maxivol));


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        forward = findViewById(R.id.musicseekbar);
        forward.setMax(media.getDuration());

      /*  new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                forward.setProgress(media.getCurrentPosition());
            }
        },0,1000); */


        forward.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                if(b) {
                    media.seekTo(i);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                media.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                media.start();
                playcycle();
            }
        });


    }
    public void playcycle()
    {
        String time = calculatetime(media.getCurrentPosition());
        TextView pxt = findViewById(R.id.timestart);
        pxt.setText(time);
        forward.setProgress(media.getCurrentPosition());

        if(media.isPlaying()) {
            runnable = new Runnable() {
                @Override
                public void run() {

                    playcycle();

                }
            };
            handler.postDelayed(runnable, 1000);

        }

    }

    public void selectsong(View view)
    {
        media.stop();
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("audio/*");
        startActivityForResult(i,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data != null)
            {

                FileDescriptor fd = null;
                Uri audioFileUri = data.getData();
                Log.d("mediaall", "onActivityResult: " + audioFileUri);
                try {
                    Log.d("mediapuri", audioFileUri.getPath());
                    File baseDir = Environment.getExternalStorageDirectory();

                    String ar[] = audioFileUri.getPath().split(":");
                    Log.d("mediapur", ar[1]);
                    String final_path = baseDir.getAbsolutePath()+"/"+ar[1];

                    FileInputStream fis = new FileInputStream(final_path);
                    fd = fis.getFD();
                    if (fd != null) {
                        media = new MediaPlayer();
                        media.setDataSource(fd);
                        media.prepare();
                        totaltime.setText(calculatetime(media.getDuration()));
                        forward.setProgress(0);
                        forward.setMax(media.getDuration());
                        playstatus = false;
                        playandpause.setBackgroundResource(R.drawable.play);
                    }

                    Toast.makeText(getApplicationContext(), audioFileUri.getPath(), Toast.LENGTH_LONG).show();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Data is NUll", Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.playandpause)
        {
            if(playstatus==true)
            {
                media.pause();
                playandpause.setBackgroundResource(R.drawable.play);
                playstatus = false;
            }
            else
            {
                media.setLooping(true);
                media.start();
                playcycle();
                playandpause.setBackgroundResource(R.drawable.stop);
                playstatus = true;
            }

        }
        else if(v.getId()==R.id.voloff)
        {
            bar.setProgress(0);
        }
        else if(v.getId()==R.id.volon)
        {
            bar.setProgress(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        media.start();
    }
}
