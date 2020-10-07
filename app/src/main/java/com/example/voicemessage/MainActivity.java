package com.example.voicemessage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.anstrontechnologies.corehelper.AnstronCoreHelper;
import com.devlomi.record_view.OnRecordClickListener;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.FileUtils;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import id.zelory.compressor.Compressor;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {


    private MediaRecorder mediaRecorder = null;
    private int Counter, REQUEST_AUDIO_PERMISSION_CODE = 1;
    private String recodfile;
    private FirebaseAuth Mauth;
    private StorageReference audiofile;


    private String permission[] = {RECORD_AUDIO, WRITE_EXTERNAL_STORAGE};
    private String mFileName;

    private ProgressDialog Mpogress;


    private Button buttonchange_onclick;
    private RecordView recordView;
    private RecordButton recordButton;
    private MaterialCardView messgecard;
    private Handler handler = new Handler();
    private EditText editText;
    private MaterialCardView floatingActionButton;

    private Button uploadfile;
    private StorageReference ImageRoot;
    private StorageReference thamimage_ref;
    private ProgressDialog Mprogress;
    byte[] thumb_byte_data;
    File tham_filepath;
    private Bitmap thumb_bitmap;
    private AnstronCoreHelper anstronCoreHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Mpogress = new ProgressDialog(MainActivity.this);
        ImageRoot = FirebaseStorage.getInstance().getReference();
        anstronCoreHelper = new AnstronCoreHelper(getApplicationContext());

        thamimage_ref = FirebaseStorage.getInstance().getReference().child("Convert");
        Mpogress = new ProgressDialog(MainActivity.this);

        uploadfile = findViewById(R.id.UploadFileID);

        uploadfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(cheack_image_permission()){
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, 511);
                }
                else {

                }


            }
        });

        /// todo all function
        floatingActionButton = findViewById(R.id.SendButtonID);
        messgecard = findViewById(R.id.MessageCard);
        buttonchange_onclick = findViewById(R.id.btn_change_onclick);
        recordView = findViewById(R.id.RecordView);
        recordButton = findViewById(R.id.RecodButton);

        audiofile = FirebaseStorage.getInstance().getReference().child("MyAudio");

        editText = findViewById(R.id.InputEditextID);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();

                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                if (text.isEmpty()) {
                    floatingActionButton.setVisibility(View.GONE);
                    recordButton.setVisibility(View.VISIBLE);
                } else {
                    recordButton.setVisibility(View.GONE);
                    floatingActionButton.setVisibility(View.VISIBLE);
                }
            }
        });


        recordButton.setRecordView(recordView);


        buttonchange_onclick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startService(new Intent(MainActivity.this, MyAndridSevice.class));

            }
        });

        recordButton.setOnRecordClickListener(new OnRecordClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                Log.d("RecordView", "onStart");
                Toast.makeText(MainActivity.this, "OnStartRecord", Toast.LENGTH_SHORT).show();

                if (checkpermission()) {
                    startrecoding();

                    startService(new Intent(MainActivity.this, MyAndridSevice.class));
                } else {

                }


                messgecard.setVisibility(View.GONE);


            }

            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this, "onCancel", Toast.LENGTH_SHORT).show();


                ExampleRunnable runnable = new ExampleRunnable(5);
                new Thread(runnable).start();


            }

            @Override
            public void onFinish(long recordTime) {

                   stoprecoding();

                startService(new Intent(MainActivity.this, MyAndridSevice.class));

                Toast.makeText(getApplicationContext(), "Finish", Toast.LENGTH_SHORT).show();
                messgecard.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLessThanSecond() {
                Toast.makeText(MainActivity.this, "OnLessThanSecond", Toast.LENGTH_SHORT).show();
                Log.d("RecordView", "onLessThanSecond");

                messgecard.setVisibility(View.VISIBLE);
            }
        });


        /// end
    }

    class ExampleRunnable implements Runnable {

        int second;

        public ExampleRunnable(int second) {
            this.second = second;
        }

        @Override
        public void run() {
            for (int i = 0; i <= 5; i++) {

                if (i == 5) {

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messgecard.setVisibility(View.VISIBLE);
                        }
                    });

                }

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /// todo all function


    private void startrecoding() {

        Calendar calendar_time = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat_time = new SimpleDateFormat("hh:mm:ss");
        String CurrentTime = simpleDateFormat_time.format(calendar_time.getTime());

        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/AudioRecording" + CurrentTime + ".3gp";

        mediaRecorder = new MediaRecorder();

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(mFileName);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {

            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void stoprecoding() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                Toast.makeText(getApplicationContext(), mFileName, Toast.LENGTH_SHORT).show();

                mediaRecorder = null;
            } catch (IllegalStateException e) {
                e.printStackTrace();

                mediaRecorder = null;
            }
        }

    }

    /*private void saveing_data_firebase(String recodfile) {

        Uri uri = Uri.fromFile(new File(recodfile));
        Mpogress.setMessage("uploading ...");
        Mpogress.setCanceledOnTouchOutside(false);
        Mpogress.show();

        StorageReference filepath = audiofile.child(uri.getLastPathSegment());
        filepath.putFile(uri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "upload success", Toast.LENGTH_SHORT).show();

                            Mpogress.dismiss();
                        } else {
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();


                            Mpogress.dismiss();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                        Mpogress.dismiss();
                    }
                });
    }*/


    private boolean checkpermission() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

            return true;
        } else {

            ActivityCompat.requestPermissions(MainActivity.this, permission, REQUEST_AUDIO_PERMISSION_CODE);
            return false;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 511 && resultCode == RESULT_OK) {


            Uri imageuri = data.getData();

            if(imageuri != null){
               final File file = new File(SiliCompressor.with(this)
                       .compress(FileUtils.getPath(this, imageuri), new File(this.getCacheDir(), "temp")));
                Uri fromfile = Uri.fromFile(file);




                ImageRoot.child("Image/").child(anstronCoreHelper.getFileNameFromUri(fromfile)).putFile(fromfile)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getApplicationContext(), task.getResult().getDownloadUrl().toString(), Toast.LENGTH_SHORT).show();
                                }
                                else {

                                }
                            }
                        });

            }



        }
    }


    private boolean cheack_image_permission(){
        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 511);

            return false;
        }
    }
}
