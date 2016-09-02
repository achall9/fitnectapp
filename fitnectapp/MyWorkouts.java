package achall9.com.fitnectapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyWorkouts extends Fragment {
    static ArrayList<String> workouts = new ArrayList<>();
    static ArrayAdapter arrayAdapter;
    static Set<String> set;
    static Set<String> savedSet;
    private SharedPreferences sp;
    static ListView workoutList;
    private FragmentActivity fragmentActivity;
    private static SQLDatabase db;
    private ArrayList<String> savedWO = new ArrayList<>();
    private static final int REQUEST_CAMERA = 1888;
    private static final int SELECT_FILE = 1887;
    private String userChoosenTask;
    private String getUserProfile;
    private byte[] profileBytes;
    private ImageView myPic;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentActivity = super.getActivity();
        View rootView = inflater.inflate(R.layout.fragment_my_workouts, container, false);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        sp = fragmentActivity.getSharedPreferences("achall9.com.fitnectapp", Context.MODE_PRIVATE);
        db = new SQLDatabase(fragmentActivity);

        TextView welcome = (TextView) fragmentActivity.findViewById(R.id.welcomeText);
        welcome.setText("Welcome back \n" + sp.getString("first name", "") + "!");

        int completeNum = sp.getInt("complete number", 0);
        int shareNum = sp.getInt("share number", 0);
        TextView woCompleteNum = (TextView) fragmentActivity.findViewById(R.id.completedWoNum);
        woCompleteNum.setText(completeNum + " Workouts completed");
        TextView woShareNum = (TextView) fragmentActivity.findViewById(R.id.sharedWoNum);
        woShareNum.setText(shareNum + " Workouts shared");

        //Viewing your own profile
        workoutList = (ListView) view.findViewById(R.id.listView);
        set = sp.getStringSet("workouts", null);
        if (sp.getStringSet("workouts saved", null) != null) {
            set.addAll(sp.getStringSet("workouts saved", null));
            savedWO.addAll(sp.getStringSet("workouts saved", null));
        }
        workouts.clear();
        if (set != null) {
            workouts.addAll(set);
        } else {
            workouts.add("New");
            set = new HashSet<>();
            set.addAll(workouts);
            sp.edit().putStringSet("workouts", set).apply();
        }
        assert workoutList != null;
        arrayAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, workouts);
        workoutList.setAdapter(arrayAdapter);

        //enter into workout when clicked
        workoutList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean savedWoClick = false;
                //check to see if clicked on saved Workout or user created Workout
                for (String wo : savedWO) {
                    if (workouts.get(position).equals(wo)) {
                        savedWoClick = true;
                        Intent intent = new Intent(fragmentActivity, WorkoutView.class);
                        intent.putExtra("user title", position);
                        startActivity(intent);
                        break;
                    }
                }
                if (!savedWoClick) {
                    Intent intent = new Intent(getActivity(), WorkoutEdit.class);
                    intent.putExtra("workoutID", position);
                    startActivity(intent);
                }
            }
        });
        //Delete option if workout list button held
        longClick();

        //allow profile picture to be set
        myPic = (ImageView) fragmentActivity.findViewById(R.id.myProfileImageView);
        String urlStr = sp.getString("profileUrl", null);
        if (urlStr != null){
            Glide.with(getContext()).load(urlStr)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.IMMEDIATE)
                    .bitmapTransform(new CropCircleTransformation(getContext()))
                    .into(myPic);
        }
        myPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu2, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection for menu
        switch (item.getItemId()) {
            //back button
            case R.id.action_settings:
                Intent intent = new Intent(this.getActivity(), SettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.action_logout:
                sp.edit().putBoolean("logged in", false).apply();
                FirebaseAuth.getInstance().signOut();
                intent = new Intent(fragmentActivity, LoginActivity.class);
                startActivity(intent);
                break;

            case R.id.action_add:
                workouts.add("New");
                if (set == null) {
                    set = new HashSet<>();
                } else {
                    set.clear();
                }
                set.addAll(workouts);
                sp.edit().remove("workouts").apply();
                sp.edit().putStringSet("workouts", set).apply();
                arrayAdapter.notifyDataSetChanged();

                intent = new Intent(this.getActivity(), WorkoutEdit.class);
                intent.putExtra("workoutID", workouts.size() - 1);
                startActivity(intent);
                break;
        }
        return true;
    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(fragmentActivity);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(fragmentActivity);

                if (items[item].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";
                    if (result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask = "Choose from Library";
                    if (result)
                        galleryIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void cameraIntent() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == -1) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA) {
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

        loadProfileAndSaveDB();

        // myPic.setImageBitmap(thumbnail);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data.getData());
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
        loadProfileAndSaveDB();
    }

    public void loadProfileAndSaveDB() {
        Glide.with(getContext()).load(profileBytes)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.IMMEDIATE)
                .bitmapTransform(new CropCircleTransformation(getContext()))
                .into(myPic);

        Firebase.setAndroidContext(getContext());
        final Firebase firebaseRef = new Firebase("https://fitnectapp.firebaseio.com/");
        FirebaseStorage fireBaseStr = FirebaseStorage.getInstance();
        StorageReference jobRef = fireBaseStr.getReferenceFromUrl("gs://fitnectapp.appspot.com");
        String imagePath = "profile/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "_" + System.currentTimeMillis() + ".jpg";
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg").build();
        jobRef.child(imagePath).putBytes(profileBytes, metadata).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //Firebase.setAndroidContext(fragmentActivity);
                UserInfo userInfo = new UserInfo();
                userInfo.setSharedNumber(sp.getInt("share number", 0));
                userInfo.setCompleteNumber(sp.getInt("complete number", 0));
                userInfo.setEmail(sp.getString("email", null));
                userInfo.setUsername(sp.getString("username", null));
                userInfo.setFirstName(sp.getString("first name", null));
                userInfo.setLastName(sp.getString("last name", null));
                sp.edit().putString("profileUrl", taskSnapshot.getDownloadUrl().toString()).apply();
                userInfo.setProfileUrl(taskSnapshot.getDownloadUrl().toString());

                //Save number shared to UserInfo
                Firebase postRef = firebaseRef.child("user info");
                postRef.child(userInfo.getUsername()).setValue(userInfo);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Profile Uploading has failed!", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void longClick() {
        workoutList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(fragmentActivity)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Are you sure?")
                        .setMessage("Delete this workout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (set == null) {
                                    set = new HashSet<>();
                                } else {
                                    set.clear();
                                    if (sp.getStringSet("workouts saved", null) != null) {
                                        savedSet = new HashSet<>();
                                        savedSet.addAll(sp.getStringSet("workouts saved", null));
                                        savedSet.remove(workouts.get(position));
                                    }
                                }
                                //delete from database
                                List<Workout> workoutsDb = db.getAllExercises();
                                for (Workout wo : workoutsDb) {
                                    String workoutTitle = wo.getWorkout();
                                    if (workoutTitle.equals(workouts.get(position))) {
                                        db.deleteExercise(wo);
                                    }
                                }
                                workouts.remove(position);
                                set.addAll(workouts);
                                sp.edit().remove("workouts").apply();
                                sp.edit().remove("workouts saved").apply();
                                sp.edit().putStringSet("workouts", set).apply();
                                sp.edit().putStringSet("workouts saved", savedSet).apply();
                                arrayAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            }
        });
    }

    public class CropCircleTransformation implements Transformation<Bitmap> {

        private BitmapPool mBitmapPool;

        public CropCircleTransformation(Context context) {
            this(Glide.get(context).getBitmapPool());
        }

        public CropCircleTransformation(BitmapPool pool) {
            this.mBitmapPool = pool;
        }

        @Override
        public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
            Bitmap source = resource.get();
            int size = Math.min(source.getWidth(), source.getHeight());

            int width = (source.getWidth() - size) / 2;
            int height = (source.getHeight() - size) / 2;

            Bitmap bitmap = mBitmapPool.get(size, size, Bitmap.Config.ARGB_8888);
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader =
                    new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            if (width != 0 || height != 0) {
                // source isn't square, move viewport to center
                Matrix matrix = new Matrix();
                matrix.setTranslate(-width, -height);
                shader.setLocalMatrix(matrix);
            }
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            return BitmapResource.obtain(bitmap, mBitmapPool);
        }

        @Override
        public String getId() {
            return "CropCircleTransformation()";
        }
    }
}