package achall9.com.fitnectapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ProfileViewActivity extends AppCompatActivity {

    static ArrayList<String> userWorkouts = new ArrayList<>();
    static String user;
    static ListView workoutList;
    static ArrayAdapter arrayAdapter;
    private SharedPreferences sp;
    ProgressDialog dialog;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);
        sp = this.getSharedPreferences("achall9.com.fitnectapp", Context.MODE_PRIVATE);
        userWorkouts.clear();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();

        //If viewing other users profile:
        Intent intent = this.getIntent();
        user = intent.getStringExtra("user");
        if (user != null) {
            this.retrieveInfo();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection for menu
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(getApplicationContext(), WorkoutView.class);
                startActivity(intent);
                break;

            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.action_logout:
                sp.edit().putBoolean("logged in", false).apply();
                FirebaseAuth.getInstance().signOut();
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;
        }
        return true;
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
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private void getUpdates(DataSnapshot ds) {
        if (ds.getKey().equals("Workouts")) {
            for (DataSnapshot data : ds.getChildren()) {
                Workout workout = new Workout();
                workout.setUsername(data.getValue(Workout.class).getUsername());
                if(user.equals(workout.getUsername())) {
                    TextView titleText = (TextView) this.findViewById(R.id.profileName);
                    titleText.setText(user);
                    workout.setWorkout(data.getValue(Workout.class).getWorkout());
                    userWorkouts.add(workout.getWorkout());
                }
            }
        }
        if(ds.getKey().equals("user info")){
           for(DataSnapshot data : ds.getChildren()){
               UserInfo userInfo = new UserInfo();
               userInfo.setUsername(data.getValue(UserInfo.class).getUsername());
               if(user.equals(userInfo.getUsername())) {
                   TextView completeNum = (TextView) this.findViewById(R.id.completedWoNumView);
                   userInfo.setCompleteNumber(data.getValue(UserInfo.class).getCompleteNumber());
                   completeNum.setText(String.valueOf(userInfo.getCompleteNumber()) + " Workouts completed");
                   TextView sharedNum = (TextView) this.findViewById(R.id.sharedWoNumView);
                   userInfo.setSharedNumber(data.getValue(UserInfo.class).getsharedNumber());
                   sharedNum.setText(String.valueOf(userInfo.getsharedNumber()) + " Workouts shared");
                   ImageView myPic = (ImageView) findViewById(R.id.userProfileImageView);
                   userInfo.setProfileUrl(data.getValue(UserInfo.class).getProfileUrl());
                   String urlStr = String.valueOf(userInfo.getProfileUrl());
                   Log.d("HEYY", urlStr);
                   dialog.dismiss();
                   if (!urlStr.equals("") && !urlStr.equals("null")){
                       Glide.with(ProfileViewActivity.this).load(urlStr)
                               .diskCacheStrategy(DiskCacheStrategy.ALL)
                               .priority(Priority.IMMEDIATE)
                               .bitmapTransform(new CropCircleTransformation(ProfileViewActivity.this))
                               .into(myPic);
                   }
               }
            }
        }
        if (userWorkouts.size() > 0) {
            workoutList = (ListView) this.findViewById(R.id.profileListview);
            arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userWorkouts);
            workoutList.setAdapter(arrayAdapter);

            //enter into workout when clicked
            workoutList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getApplicationContext(), WorkoutView.class);
                    intent.putExtra("my title", position);
                    startActivity(intent);
                }
            });
        }
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

        @Override public String getId() {
            return "CropCircleTransformation()";
        }
    }
}
