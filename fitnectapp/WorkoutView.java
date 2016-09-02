package achall9.com.fitnectapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.facebook.FacebookSdk;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorkoutView extends AppCompatActivity {
    static int title;
    static int myTitle;
    static int userTitle;
    static Set<String> set;
    static List<Workout> workoutList = new ArrayList<>();
    static List<String> exerciseText = new ArrayList<>();
    static List<String> repsText = new ArrayList<>();
    static List<String> setsText = new ArrayList<>();
    static List<String> commentText = new ArrayList<>();
    ListView listView;
    static String username;
    static String titleName;
    static String exercise;
    static String reps;
    static String sets;
    static String comment;
    static String workoutName;
    static String titleTextOfficial;
    SharedPreferences sp;
    private Toolbar toolbar;
    private ProgressDialog dialog;
    static int likes;
    static boolean likedWO;
    ArrayList<Integer> lList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_view);
        Firebase.setAndroidContext(this);
        sp = this.getSharedPreferences("achall9.com.fitnectapp", Context.MODE_PRIVATE);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();

        //set up the title of the workout clicked
        Intent intent = getIntent();
        myTitle = intent.getIntExtra("my title", -1);
        title = intent.getIntExtra("title", -1);
        userTitle = intent.getIntExtra("user title", -1);
        boolean complete;
        complete = intent.getBooleanExtra("completed", false);

        retrieveInfo();
        int completeNum = sp.getInt("complete number", 0);
        if(complete){
            completeNum++;
            sp.edit().putInt("complete number", completeNum).apply();

            //save user info database
            UserInfo userInfo = new UserInfo();
            userInfo.setCompleteNumber(completeNum);
            userInfo.setEmail(sp.getString("email", null));
            userInfo.setUsername(sp.getString("username", null));
            userInfo.setFirstName(sp.getString("first name", null));
            userInfo.setLastName(sp.getString("last name", null));
            Firebase firebaseRef = new Firebase("https://fitnectapp.firebaseio.com/");
            //Save WO complete number online
            Firebase postRef = firebaseRef.child("user info");
            postRef.child(userInfo.getUsername()).setValue(userInfo);

            //let SP know WO is complete to set color of icon
            sp.edit().putBoolean(titleName + " complete", true).apply();
            new AlertDialog.Builder(this)
                    .setTitle("Great job!")
                    .setMessage("You have completed the workout!")
                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }

        TextView userTV = (TextView) findViewById(R.id.workoutUser);
        userTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ProfileViewActivity.class);
                intent.putExtra("user", username);
                startActivity(intent);
            }
        });
    }

    public void popUpView(String comment){
        LayoutInflater layoutInflater
                = (LayoutInflater)getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.comment_layout, null);
        final PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.showAsDropDown(toolbar, 0, 0);
        popupWindow.update();

        final EditText commentText = (EditText) popupView.findViewById(R.id.commentText);
        commentText.setFocusable(false);
        commentText.setText(comment);

        Button closeBtn = (Button) popupView.findViewById(R.id.doneBtn);
        closeBtn.setText("Close");
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu4, menu);

        Drawable myIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.fitlove);
        ColorFilter filter = new LightingColorFilter(Color.parseColor("#ffffff"), Color.TRANSPARENT);
        assert myIcon != null;
        //set like button to white
        myIcon.setColorFilter(filter);
        //set beginWo button to white
        myIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.begin_wo);
        myIcon.setColorFilter(filter);

        //set color of fitlove icon to orange if liked
        likedWO = sp.getBoolean(titleName, false);
        if(likedWO){
            myIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.fitlove);
            filter = new LightingColorFilter(Color.parseColor("#ff9100"), Color.TRANSPARENT);
            assert myIcon != null;
            myIcon.setColorFilter(filter);
        }
        boolean completeWO = sp.getBoolean(titleName + " complete", false);
        if(completeWO){
            myIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.begin_wo);
            filter = new LightingColorFilter(Color.parseColor("#ff9100"), Color.TRANSPARENT);
            assert myIcon != null;
            myIcon.setColorFilter(filter);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection for menu
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;

            case R.id.action_begin:
                new AlertDialog.Builder(this)
                        .setTitle("Begin workout")
                        .setMessage("Are you ready to begin this workout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getApplicationContext(), WorkoutComplete.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                break;

            case R.id.action_save:
                titleTextOfficial = WorkoutsShared.sharedWorkouts.get(title);
                set = new HashSet<>();
                set.add(titleTextOfficial);
                sp.edit().putStringSet("workouts saved", set).apply();
                Toast.makeText(getApplicationContext(), "Workout saved!", Toast.LENGTH_LONG).show();
                break;

            case R.id.action_like:
                likedWO = sp.getBoolean(titleName, false);
                if(!likedWO) {
                    likes++;
                    Drawable myIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.fitlove);
                    ColorFilter filter = new LightingColorFilter(Color.parseColor("#ff9100"), Color.TRANSPARENT);
                    myIcon.setColorFilter(filter);
                    final Firebase firebaseRef = new Firebase("https://fitnectapp.firebaseio.com/");
                    Firebase postRef = firebaseRef.child("Workouts");

                    Workout workout = new Workout();
                    workout.setWorkout(workoutName);
                    workout.setUsername(username);
                    workout.setLikes(likes);
                    workout.setComment(comment);
                    workout.setExercise(exercise);
                    workout.setReps(reps);
                    workout.setSets(sets);

                    postRef.child(titleName).setValue(workout);
                    sp.edit().putBoolean(titleName, true).apply();
                }
                break;

            case R.id.action_logout:
                sp.edit().putBoolean("logged in", false).apply();
                FirebaseAuth.getInstance().signOut();
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;

            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
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
                getUpdates(snapshot);
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

    private void getUpdates(DataSnapshot ds){
        if (ds.getKey().equals("Workouts")) {
            exerciseText.clear();
            repsText.clear();
            setsText.clear();
            workoutList.clear();
            String otherTitleText;
            String titleText;
            Workout workout = new Workout();
            for(DataSnapshot data : ds.getChildren()) {
                workout.setExercise(data.getValue(Workout.class).getExercise());
                workout.setReps(data.getValue(Workout.class).getReps());
                workout.setSets(data.getValue(Workout.class).getSets());
                titleText = data.getValue(Workout.class).getWorkout() + ": " + data.getValue(Workout.class).getUsername();
                otherTitleText = data.getValue(Workout.class).getWorkout();
                workout.setLikes(data.getValue(Workout.class).getLikes());
                workout.setWorkout(data.getValue(Workout.class).getWorkout());
                workout.setUsername(data.getValue(Workout.class).getUsername());
                workout.setComment(data.getValue(Workout.class).getComment());
                if (title != -1) {
                    titleTextOfficial = WorkoutsShared.sharedWorkouts.get(title);
                }
                if(myTitle != -1){
                    titleTextOfficial = ProfileViewActivity.userWorkouts.get(myTitle);
                }
                if(userTitle != -1){
                    titleTextOfficial = MyWorkouts.workouts.get(userTitle);
                }

                if (titleTextOfficial.equals(titleText) || titleTextOfficial.equals(otherTitleText)) {
                    //set workout info
                    exercise = workout.getExercise();
                    reps = workout.getReps();
                    sets = workout.getSets();
                    comment = workout.getComment();
                    workoutName = workout.getWorkout();
                    username = workout.getUsername();
                    likes = workout.getLikes();
                    titleName = workoutName + ": " + username;
                    lList.add(workout.getLikes());

                    //set workout title
                    TextView woTV = (TextView) findViewById(R.id.workoutTitle);
                    woTV.setText(workoutName);

                    //set userName title
                    TextView userTV = (TextView) findViewById(R.id.workoutUser);
                    userTV.setText(username);

                    exerciseText = new ArrayList<>(Arrays.asList(exercise.split(",")));
                    repsText = new ArrayList<>(Arrays.asList(reps.split(",")));
                    setsText = new ArrayList<>(Arrays.asList(sets.split(",")));
                    commentText = new ArrayList<>(Arrays.asList(comment.split(",")));


                    String myRegex = "[^a-zA-Z0-9_ ]";
                    int index = 0;
                    for (String s : exerciseText) {
                        exerciseText.set(index++, s.replaceAll(myRegex, ""));
                    }
                    index = 0;
                    for (String s : repsText) {
                        repsText.set(index++, s.replaceAll(myRegex, ""));
                    }
                    index = 0;
                    for (String s : setsText) {
                        setsText.set(index++, s.replaceAll(myRegex, ""));
                    }
                    index = 0;
                    for (String s : commentText) {
                        commentText.set(index++, s.replaceAll(myRegex, ""));
                    }

                    for (int i = 0; i < exerciseText.size(); i++) {
                        Workout workoutAdd = new Workout(exerciseText.get(i), repsText.get(i), setsText.get(i), commentText.get(i));
                        workoutList.add(workoutAdd);
                    }
                    listView = (ListView) findViewById(R.id.workoutListview);
                    CustomAdapter myAdapter = new CustomAdapter(this, R.layout.custom_list, workoutList);
                    listView.setAdapter(myAdapter);
                    dialog.dismiss();
                }
            }
        }
    }
    public class CustomAdapter extends ArrayAdapter<Workout> {

        private int layoutResource;

        public CustomAdapter(Context context, int layoutResource, List<Workout> workoutList) {
            super(context, layoutResource, workoutList);
            this.layoutResource = layoutResource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                convertView = layoutInflater.inflate(layoutResource, null);
            }

            final Workout workout = getItem(position);

            if (position != -1) {
                //Get each edit text
                final TextView exercise = (TextView) convertView.findViewById(R.id.exercise);
                final TextView reps = (TextView) convertView.findViewById(R.id.reps);
                final TextView sets = (TextView) convertView.findViewById(R.id.sets);
                final ImageButton comment = (ImageButton) convertView.findViewById(R.id.commentButton);
                assert comment != null;

                //set text
                exercise.setText(workout.getExercise());
                reps.setText(workout.getReps());
                sets.setText(workout.getSets());
                comment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popUpView(workout.getComment());
                    }
                });

                //allow user to search a tutorial for exercise if not sure what it is
                exercise.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String exSelect = workout.getExercise();
                        new AlertDialog.Builder(getContext())
                                .setTitle("Tutorial")
                                .setMessage("Would you like to search a tutorial for this exercise?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                                        intent.putExtra(SearchManager.QUERY, exSelect + " Tutorial");
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                });
            }
            return convertView;
        }
    }

}
