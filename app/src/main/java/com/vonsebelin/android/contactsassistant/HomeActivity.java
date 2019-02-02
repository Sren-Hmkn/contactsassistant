package com.vonsebelin.android.contactsassistant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.icu.text.SimpleDateFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.taishi.library.Indicator;
import com.vonsebelin.android.contactsassistant.data.ContactContract;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static com.vonsebelin.android.contactsassistant.data.ContactProvider.LOG_TAG;


public class HomeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CONTACT_LOADER = 0;

    private ListView contactListView;
    private ContactCursorAdapter mCursorAdapter;
    private RelativeLayout recHint;
    private ImageView watchAndTakePic;

    // PICTURE STUFF
    private ImageView makePic, thumbnail;
    private Bitmap bCard;
    byte[] picBlob;
    private File fileUri = null;//Uri to capture image

    // ALL THAT MEDIA STUFF
    private static final int MICROPHONE = 0x1;
    private static final int CAMERA = 0x2;
    private String filePath;


    FloatingActionButton fab;
    private MediaRecorder mMediaRecorder;
    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;
    private File mFile, mBusinessCard, mPicTwo;
    private ProgressBar progress;
    private int maxMediaDuration;
    private MediaObserver observer = null;
    private Indicator indicator;
    private ImageView playAndRepeatButton;
    private View toBedeletedView;
    private Cursor mCurrentCursor;



    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        contactListView = findViewById(R.id.list);
        View emptyView = findViewById(R.id.empty_view);
        contactListView.setEmptyView(emptyView);

        recHint = (RelativeLayout) findViewById(R.id.rec_hint);
        mCursorAdapter = new ContactCursorAdapter(this,null);
        contactListView.setAdapter(mCursorAdapter);
        getLoaderManager().initLoader(CONTACT_LOADER, null, this);

        indicator = (Indicator) findViewById(R.id.music_indicator);
        indicator.setVisibility(View.GONE);

        mAudioManager =  (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        mMediaPlayer = new MediaPlayer();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        mFile = new File(this.getCacheDir(), "tmp_contact_note");
        mBusinessCard = new File(this.getCacheDir(), "tmp_contact_bcard");

        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // start edit-activity
                Intent intent = new Intent(HomeActivity.this, EditActivity.class);
                Uri currentContactUri = ContentUris.withAppendedId(ContactContract.ContactEntry.CONTENT_URI,id);
                intent.setData(currentContactUri);
                startActivity(intent);
            }
        });

        fab.setOnTouchListener(new View.OnTouchListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                // it throws a problem when recording is too short and MediaRecorder hasn't fully entered the recording-state.
                try {
                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                        if(askForPermission(Manifest.permission.RECORD_AUDIO,MICROPHONE)) {
                            startRecording();
                        } else {
                            Toast.makeText(HomeActivity.this, "permission needed to proceed", Toast.LENGTH_LONG).show();
                        }
                    }
                    else if(event.getAction() == MotionEvent.ACTION_UP){
                        stopRecording();
                        try {
                            putContactNoteToDb(saveFile(mFile));
                        } catch (IOException e) {
                            Log.e("Mistake saving file",e.getMessage());
                        }
                    }
                }
                catch (Exception e) {
                    Log.e("SPACE TAXI", e.getMessage());
                }
                return true;
            }
        });
    }


    // Item Interactions
    // Play Note
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void playAudioFile(View v){
        LinearLayout parentLayout = (LinearLayout) v.getParent();
        progress = parentLayout.findViewById(R.id.progress_bar);
        final int position = contactListView.getPositionForView(parentLayout);
        ((ImageView) v).setImageResource(R.drawable.ic_replay_black_24dp);
        playAndRepeatButton = (ImageView) v;
        if(position >= 0){
            startMediaPlayer((Cursor) mCursorAdapter.getItem(position));
        }
    }

    // Delete Stuff via UI
    public void showDeleteConfirmationDialog(View view) {
        toBedeletedView = view;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to delete the contact?");

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }

            }
        });

        builder.setPositiveButton("delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteEntryAndFile(toBedeletedView);
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteEntryAndFile(View v){
        final int position = contactListView.getPositionForView((LinearLayout) v.getParent());
        if(position >= 0){
            Cursor currentCursor = (Cursor) mCursorAdapter.getItem(position);
            int currentEntryIdColumnIndex = currentCursor.getColumnIndex(ContactContract.ContactEntry._ID);
            int currentEntryId = currentCursor.getInt(currentEntryIdColumnIndex);
            String currentEntryIdString = ""+currentEntryId;

            int currentEntryNoteColumnIndex = currentCursor.getColumnIndex(ContactContract.ContactEntry.COLUMN_CONTACT_NOTE);
            String currentEntryFilePath = currentCursor.getString(currentEntryNoteColumnIndex);
            File oldFile = new File(currentEntryFilePath);

            // delete file
            HomeActivity.this.deleteFile(oldFile.getName());

            // delete entry
            Uri contactUri = Uri.withAppendedPath(ContactContract.ContactEntry.CONTENT_URI, currentEntryIdString);
            try {
                getContentResolver().delete(contactUri, null,null);
            } catch (IllegalArgumentException e) {
                Log.e("Error Deleting Entry", e.getMessage());
            }
        }
    }

    // RECORDER STUFF
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startRecording(){

        mMediaRecorder = new MediaRecorder();
        // start recording thingy
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // CAN CHECK DIFFERENT TYPES FOR BETTER QUALITY?!
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setOutputFile(mFile);
        try {
            Log.i("SPACE TAXI","and...");
            mMediaRecorder.prepare();
            Log.i("SPACE TAXI","PREPARED");
            mMediaRecorder.start();
            //recording hint
            recHint.setVisibility(View.VISIBLE);
            //music indicator
            indicator.setVisibility(View.VISIBLE);
            indicator.setBarNum(15);
            indicator.setStepNum(15);


            Log.i("SPACE TAXI","STARTED");
        } catch (Exception e) {
            Log.e("MICROPHONE", "MIC SAYS NO: " + e);
        }
    }

    private void stopRecording(){
        recHint.setVisibility(View.GONE);
        indicator.setVisibility(View.GONE);

        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
    }

    // AUDIOMANAGER STUFF
    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange){
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    releaseMediaPlayer(mMediaPlayer);
                    observer.stop();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    mMediaPlayer.start();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    releaseMediaPlayer(mMediaPlayer);
                    observer.stop();
                    break;
            }
        }
    };

    // MEDIAPLAYER STUFF
    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            progress.setProgress(mp.getDuration());
            releaseMediaPlayer(mp);
            observer.stop();
            playAndRepeatButton.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
        }

    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void releaseMediaPlayer(MediaPlayer mp) {
        if (mp != null) {
            mp.release();
            mp = null;
            mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
        }
    }

    private void startMediaPlayer(Cursor cursor){
        if(observer != null) observer.stop();
        Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(ContactContract.ContactEntry.COLUMN_CONTACT_NOTE)));
        releaseMediaPlayer(mMediaPlayer);

        if (mAudioManager.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) ==
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mMediaPlayer = MediaPlayer.create(HomeActivity.this, uri);
            maxMediaDuration = mMediaPlayer.getDuration();
            progress.setMax(maxMediaDuration);
            observer = new MediaObserver();
            mMediaPlayer.start();
            new Thread(observer).start();
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
        }
    }

    private class MediaObserver implements Runnable {
        private AtomicBoolean stop = new AtomicBoolean(false);
        private int progressTimer=0;
        Thread t = Thread.currentThread();

        public void stop() {
            stop.set(true);
        }

        @Override
        public void run() {
            while (!stop.get()) {
                try {
                    progressTimer = mMediaPlayer.getCurrentPosition();
                } catch (Exception e) {
                    progressTimer = maxMediaDuration;
                    Log.e("SPACE", "progressbar is over!");
                }
                progress.setProgress(progressTimer);

                try {
                    Log.i("SPACE","sleep");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        releaseMediaPlayer(mMediaPlayer);
        if(observer != null) observer.stop();
    }

    // Ask for microphone permissions
    private boolean askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(HomeActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, permission)) {
                ActivityCompat.requestPermissions(HomeActivity.this, new String[]{permission}, requestCode);
            } else {
                ActivityCompat.requestPermissions(HomeActivity.this, new String[]{permission}, requestCode);
            }
            return false;
        }
        return true;
    }


    // CURSOR CLOADER OVERRIDE METHODS
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ContactContract.ContactEntry._ID,
                ContactContract.ContactEntry.COLUMN_CONTACT_NAME,
                ContactContract.ContactEntry.COLUMN_CONTACT_NOTE,
                ContactContract.ContactEntry.COLUMN_CONTACT_BCARD
        };

        return new CursorLoader(this,
                ContactContract.ContactEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    // SAVE CONTACT ACTION
    private File saveFile(File src) throws IOException {
        File dst = new File(this.getFilesDir().getAbsolutePath(), stringGenerator(15));
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
        return dst;
    }

    // random String generator by https://www.baeldung.com/java-random-string
    public static String stringGenerator(int targetStringLength) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private boolean putContactNoteToDb(File noteFile) {

        if (!noteFile.exists()) {
            Toast.makeText(this, "No audio given", Toast.LENGTH_SHORT);
            return false;
        }

        SimpleDateFormat s = new SimpleDateFormat("dd.MM.YYYY");
        SimpleDateFormat s2 = new SimpleDateFormat("hh:mm");
        String date = s.format(new Date());
        String time = s2.format(new Date());

        Uri recordingUri = Uri.fromFile(noteFile);
        ContentValues values = new ContentValues();
        values.put(ContactContract.ContactEntry.COLUMN_CONTACT_NOTE,""+recordingUri);
        values.put(ContactContract.ContactEntry.COLUMN_CONTACT_NAME,"new contact " + time);
        values.put(ContactContract.ContactEntry.COLUMN_CONTACT_DATE,""+date);

        Uri newUri = null;
        try {
            newUri = getContentResolver().insert(ContactContract.ContactEntry.CONTENT_URI, values);
        } catch (IllegalArgumentException e) {
            Log.e("Error", e.getMessage());
            if (newUri == null) {
                Toast.makeText(
                        this,
                        "Something went wrong saving the note!",
                        Toast.LENGTH_LONG
                ).show();
                return false;
            } else {
                Toast.makeText(
                        this,
                        "Insertion successful",
                        Toast.LENGTH_SHORT
                ).show();
                return true;
            }
        }
        return true;
    }

    private boolean putBcardToDb(){

        if (picBlob == null) {
            Toast.makeText(this, "No pic taken", Toast.LENGTH_SHORT);
            return false;
        }

        Bitmap bitmap = BitmapFactory.decodeByteArray(picBlob, 0, picBlob.length);
        watchAndTakePic.setImageBitmap(bitmap);

        ContentValues values = new ContentValues();
        values.put(ContactContract.ContactEntry.COLUMN_CONTACT_BCARD, picBlob);

        Uri newUri = null;
        try {
            newUri = getContentResolver().insert(ContactContract.ContactEntry.CONTENT_URI, values);
        } catch (IllegalArgumentException e) {
            Log.e("Error", e.getMessage());
            if (newUri == null) {
                Toast.makeText(
                        this,
                        "Something went wrong saving the pic!",
                        Toast.LENGTH_LONG
                ).show();
                return false;
            } else {
                Toast.makeText(
                        this,
                        "Insertion successful",
                        Toast.LENGTH_SHORT
                ).show();
                return true;
            }
        }
        return true;
    }

    // Taking Picture Stuff
    public void pictureClick(View v) {
        if(askForPermission(Manifest.permission.CAMERA, CAMERA)){

            final int position = contactListView.getPositionForView((LinearLayout) v.getParent());
            mCurrentCursor = (Cursor) mCursorAdapter.getItem(position);
            watchAndTakePic = (ImageView) v;

            int currentItemBcardColumnIndex = mCurrentCursor.getColumnIndex(ContactContract.ContactEntry.COLUMN_CONTACT_BCARD);
            byte[] currentBcard = mCurrentCursor.getBlob(currentItemBcardColumnIndex);

            if(position >= 0) {
                if(currentBcard == null) {
                    // TAKE PICTURE!!
                }
            }
        }
    }
}
