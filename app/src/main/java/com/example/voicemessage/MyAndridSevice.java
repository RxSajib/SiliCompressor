package com.example.voicemessage;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MyAndridSevice extends Service {

    private String mFileName;
    private MediaRecorder mediaRecorder;
    private ProgressDialog Mpogress;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {


        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
