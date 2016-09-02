package achall9.com.fitnectapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    static ArrayList<String> userList = new ArrayList<>();
    private boolean accept = false;
    private SharedPreferences sp;
    private FirebaseAuth mAuth;
    private ImageButton profileImageBut;
    private static final int REQUEST_CAMERA = 1888;
    private static final int SELECT_FILE = 1887;
    private String userChoosenTask;
    private String getUserProfile;
    private byte[] profileBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Firebase.setAndroidContext(this);
        mAuth = FirebaseAuth.getInstance();

        userList.clear();
        sp = this.getSharedPreferences("achall9.com.fitnectapp", Context.MODE_PRIVATE);
        profileImageBut = (ImageButton) findViewById(R.id.addProfileImageButton);

        profileImageBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        retrieveInfo();
    }

    public void signUp(View view) {
        boolean userOpen = true;
        final UserInfo userInfo = new UserInfo();
        EditText firstName = (EditText) findViewById(R.id.firstNameEditText);
        EditText lastName = (EditText) findViewById(R.id.lastNameEditText);
        EditText email = (EditText) findViewById(R.id.emailEditText);
        EditText userName = (EditText) findViewById(R.id.usernameEditText);
        EditText password = (EditText) findViewById(R.id.passwordEditText);
        EditText passwordCheck = (EditText) findViewById(R.id.passwordCheckEditText);

        String fName = firstName.getText().toString();
        String lName = lastName.getText().toString();
        final String em = email.getText().toString();
        String uName = userName.getText().toString();
        final String pw = password.getText().toString();
        String pwc = passwordCheck.getText().toString();
        if (!fName.equals("") && !lName.equals("") && !em.equals("") && !uName.equals("") && !pw.equals("") && !pwc.equals("")) {
            sp.edit().putString("first name", fName).apply();
            userInfo.setFirstName(fName);

            sp.edit().putString("last name", lName).apply();
            userInfo.setLastName(lName);

            sp.edit().putString("email", em).apply();
            userInfo.setEmail(em);

            sp.edit().putString("username", uName).apply();
            userInfo.setUsername(uName);

            if(userList.size() > 0) {
                for (int i = 0; i < userList.size(); i++) {
                    if (uName.toLowerCase().equals(userList.get(i).toLowerCase())) {
                        userOpen = false;
                        break;
                    }
                }
            }
            if(uName.contains(".")){
                userOpen = false;
            }else if(uName.contains("#")){
                userOpen = false;
            }else if(uName.contains("$")){
                userOpen = false;
            }else if(uName.contains("[")){
                userOpen = false;
            }else if(uName.contains("]")){
                userOpen = false;
            }

            //both passwords are verfiied to be the same and username open
            if(userOpen){
                if (pw.equals(pwc)){
                    if(pw.length() > 4) {
                        accept = true;
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Password must be at least 5 characters long", Toast.LENGTH_LONG).show();
                    }
                } else {
                    TextView noMatch = (TextView) findViewById(R.id.wrong);
                    noMatch.setVisibility(View.VISIBLE);
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "Username already taken or invalid characters", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Please fill in all fields correctly!", Toast.LENGTH_LONG).show();
        }

        //all field were filled in correctly and passwords match  ACCEPT INPUT
        if (accept) {
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("Loading...");
            dialog.setCancelable(false);
            dialog.setInverseBackgroundForced(false);
            dialog.show();
            final Firebase firebaseRef = new Firebase("https://fitnectapp.firebaseio.com/");
            mAuth.createUserWithEmailAndPassword(userInfo.getEmail(), pw)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            FirebaseStorage fireBaseStr = FirebaseStorage.getInstance();
                            StorageReference jobRef = fireBaseStr.getReferenceFromUrl("gs://fitnectapp.appspot.com");
                            String imagePath = "profile/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "_" + System.currentTimeMillis() + ".jpg";
                            StorageMetadata metadata = new StorageMetadata.Builder()
                                    .setContentType("image/jpeg").build();
                            jobRef.child(imagePath).putBytes(profileBytes, metadata).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Firebase postRef  = firebaseRef.child("user info");
                                    sp.edit().putString("profileUrl", taskSnapshot.getDownloadUrl().toString()).apply();
                                    userInfo.setProfileUrl(taskSnapshot.getDownloadUrl().toString());
                                    postRef.child(userInfo.getUsername()).setValue(userInfo);
                                    sp.edit().putBoolean("logged in", true).apply();
                                    Intent intent = new Intent(getApplicationContext(), GymSelectActivity.class);
                                    dialog.dismiss();
                                    startActivity(intent);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Profile Uploading has failed!", Toast.LENGTH_LONG).show();
                                    Firebase postRef  = firebaseRef.child("user info");
                                    userInfo.setProfileUrl("");
                                    sp.edit().putString("profileUrl","").apply();
                                    postRef.child(userInfo.getUsername()).setValue(userInfo);
                                    sp.edit().putBoolean("logged in", true).apply();
                                    Intent intent = new Intent(getApplicationContext(), GymSelectActivity.class);
                                    dialog.dismiss();
                                    startActivity(intent);
                                    finish();
                                }
                            });
                            if (!task.isSuccessful()) {
                                dialog.hide();
                                Toast.makeText(getApplicationContext(), "Sign up failed. Error from server", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //code for deny
                    Toast.makeText(getApplicationContext(), "Cannot access camera or gallery", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result=Utility.checkPermission(SignUpActivity.this);

                if (items[item].equals("Take Photo")) {
                    userChoosenTask ="Take Photo";
                    if(result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask ="Choose from Library";
                    if(result)
                        galleryIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    private void cameraIntent()
    {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA){
                onCaptureImageResult(data);
            }

        }
    }
    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //thumbnail.recycle();
        byte[] byteArray = bytes.toByteArray();
        profileBytes = byteArray;
        getUserProfile = Base64.encodeToString(byteArray, Base64.DEFAULT);

        profileImageBut.setImageBitmap(thumbnail);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, bYtE);
        //bm.recycle();
        byte[] byteArray = bYtE.toByteArray();
        getUserProfile = Base64.encodeToString(byteArray, Base64.DEFAULT);
        profileBytes = byteArray;
        profileImageBut.setImageBitmap(bm);
    }
    private void retrieveInfo(){
        Firebase ref = new Firebase("https://fitnectapp.firebaseio.com/");

        // Attach an listener to read the data at our posts reference
        ref.addChildEventListener(new ChildEventListener() {
            // Retrieve new posts as they are added to the database
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                getUpdates(snapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildKey) {
                getUpdates(snapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildKey) {
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private void getUpdates(DataSnapshot ds) {
        if (ds.getKey().equals("user info")) {
            for (DataSnapshot data : ds.getChildren()) {
                UserInfo userInfo = new UserInfo();
                userInfo.setUsername(data.getValue(UserInfo.class).getUsername());
                userList.add(userInfo.getUsername());
            }
        }
    }
}