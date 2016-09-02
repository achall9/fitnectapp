package achall9.com.fitnectapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class WorkoutEdit extends AppCompatActivity {

    private static int workoutID;
    private static SQLDatabase db;
    private List<String> exerciseText = new ArrayList<>();
    private List<String> repsText = new ArrayList<>();
    private List<String> setsText = new ArrayList<>();
    private List<Workout> workoutList = new ArrayList<>();
    private ListView workoutListView;
    private CustomAdapter myAdapter;
    SharedPreferences sp;
    private Toolbar toolbar;
    private Set<String> woComment = new LinkedHashSet<>();
    private List<String> commentList = new ArrayList<>();
    int likes;
    int clicked;
    static String[] muscles  = {"View Exercises","Abs","Back","Biceps","Cardio","Chest","Legs","Shoulders","Triceps"};
    static String title;
    boolean addOn;
    Boolean cardio;
    static String comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_edit);
        sp = this.getSharedPreferences("achall9.com.fitnectapp", Context.MODE_PRIVATE);
        cardio = false;

        final AutoCompleteTextView exercise = (AutoCompleteTextView) findViewById(R.id.exerciseText);
        exercise.requestFocus();
        final EditText reps = (EditText) findViewById(R.id.repsText);
        final EditText sets = (EditText) findViewById(R.id.setsText);
        assert reps != null;
        assert sets != null;

        Intent intent = getIntent();

        int exChose;
        exChose = intent.getIntExtra("Exercise", -1);


        //if not coming from ExerciseList activity
        if(exChose == -1) {
            clearAll();
        }
        //coming from ExerciseList activity it is an add On to a workout; addOn is true
        else{
            if(ExerciseListActivity.cardio){
                reps.setHint("Min.");
                sets.setHint("Sec.");
            }
            addOn = true;
            workoutID = sp.getInt("workoutID",-1);
            retrieve();
            myAdapter = new CustomAdapter(this, R.layout.custom_list, workoutList);
            workoutListView = (ListView) findViewById(R.id.exerciseListView);
            assert workoutListView != null;
            workoutListView.setAdapter(myAdapter);

            if(ExerciseListActivity.machineOn){
                exercise.setText(ExerciseListActivity.machineList.get(exChose));
            }
            else{
                exercise.setText(ExerciseListActivity.freeWeightList.get(exChose));
            }
        }
        //set clicked as 0 so adapter knows to be updated or set
        clicked = 0;

        //set up toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set Title
        final EditText titleEntry = (EditText) findViewById(R.id.workoutTitle);
        //not coming from ExerciseList, get intentExtra with titleName index
        if(!addOn){
            workoutID = intent.getIntExtra("workoutID", -1);
        }
        //set the tile text if given one previously else set to blank text and not "new"
        assert titleEntry != null;
        if(workoutID != -1) {
            titleEntry.setText(MyWorkouts.workouts.get(workoutID));
            sp.edit().putInt("workoutID", workoutID).apply();
            //dont set title as NEW so user doesnt need to erase it
            if(titleEntry.getText().toString().equals("New")){
                titleEntry.setText("");
            }
        }
        else{
            titleEntry.setText(title);
        }
        //allow title to be edited
        titleEntry.addTextChangedListener(new MyTextWatcher(titleEntry));

        myAdapter = new CustomAdapter(this, R.layout.custom_list, workoutList);
        db = new SQLDatabase(this);

        //Set workouts to adapter if entering previously saved WO
        if(exChose == -1){
            List<Workout> workouts = db.getAllExercises();
            for (Workout wo : workouts) {
                String workoutTitle = wo.getWorkout();
                if (workoutTitle.equals(MyWorkouts.workouts.get(workoutID))) {
                    //tell adapter that it's being updated and not recreated
                    clicked++;
                    //add to arrayLists
                    exerciseText.add(wo.getExercise());
                    repsText.add(wo.getReps());
                    setsText.add(wo.getSets());
                    woComment.add(wo.getComment());
                    Workout workout = new Workout(wo.getExercise(), wo.getReps(), wo.getSets(), wo.getComment());
                    workoutList.add(workout);
                    workoutListView = (ListView) findViewById(R.id.exerciseListView);
                    workoutListView.setAdapter(myAdapter);
                    addOn = true;
                }
            }
        }

        //Help give user exercises to pick from with spinner. Let them pick muscle group to take them to activty with exercises for that muscle
        final Spinner musclesDropDown = (Spinner) findViewById(R.id.exerciseDropDown);
        final ArrayAdapter<String> exAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, muscles);
        assert musclesDropDown != null;
        musclesDropDown.setAdapter(exAdapter);
        musclesDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 1: //abs
                        Intent intent = new Intent(getApplicationContext(), ExerciseListActivity.class);
                        intent.putExtra("Exercise", 1);
                        startActivity(intent);
                        quickSave();
                        break;
                    case 2: //back
                        intent = new Intent(getApplicationContext(), ExerciseListActivity.class);
                        intent.putExtra("Exercise", 2);
                        startActivity(intent);
                        quickSave();
                        break;
                    case 3: //biceps
                        intent = new Intent(getApplicationContext(), ExerciseListActivity.class);
                        intent.putExtra("Exercise", 3);
                        startActivity(intent);
                        quickSave();
                        break;
                    case 4: //cardio
                        intent = new Intent(getApplicationContext(), ExerciseListActivity.class);
                        intent.putExtra("Exercise", 4);
                        startActivity(intent);
                        quickSave();
                        break;
                    case 5: //chest
                        intent = new Intent(getApplicationContext(), ExerciseListActivity.class);
                        intent.putExtra("Exercise", 5);
                        startActivity(intent);
                        quickSave();
                        break;
                    case 6: //legs
                        intent = new Intent(getApplicationContext(), ExerciseListActivity.class);
                        intent.putExtra("Exercise", 6);
                        startActivity(intent);
                        quickSave();
                        break;
                    case 7: //shoulders
                        intent = new Intent(getApplicationContext(), ExerciseListActivity.class);
                        intent.putExtra("Exercise", 7);
                        startActivity(intent);
                        quickSave();
                        break;
                    case 8: //triceps
                        intent = new Intent(getApplicationContext(), ExerciseListActivity.class);
                        intent.putExtra("Exercise", 8);
                        startActivity(intent);
                        quickSave();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //When the plus button is pressed, exercise with reps and sets added to Listview
        ImageButton addNew = (ImageButton) findViewById(R.id.addButton);
        assert addNew != null;
        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workoutListView = (ListView) findViewById(R.id.exerciseListView);

                //get text from edit text
                String exText = exercise.getText().toString();
                String rpText;
                String stText;
                if(ExerciseListActivity.cardio != null && ExerciseListActivity.cardio){
                    rpText = reps.getText().toString() + "  Minutes";
                    stText = sets.getText().toString() + "  Seconds";
                }else{
                    rpText = reps.getText().toString() + "  Reps";
                    stText = sets.getText().toString() + "  Sets";
                }

                if(exText.equals("") || rpText.equals("") || stText.equals("")){
                    Toast.makeText(getApplicationContext(), "Fill in all spaces before adding to list!", Toast.LENGTH_LONG).show();
                }
                //Ensure that all Entries were filled in
                else{
                    //add to array
                    exerciseText.add(exText);
                    repsText.add(rpText);
                    setsText.add(stText);

                    Workout workoutAdd = new Workout();
                    workoutAdd.setExercise(exText);
                    workoutAdd.setReps(rpText);
                    workoutAdd.setSets(stText);
                    Workout workoutSet = new Workout(workoutAdd.getExercise(), workoutAdd.getReps(), workoutAdd.getSets());

                    //Make it of type workout to add to the WorkoutList and set/notify adapter
                    workoutList.add(workoutSet);

                    popUpEdit();
                    if (clicked == 0) {
                        workoutListView.setAdapter(myAdapter);
                    } else {
                        myAdapter.notifyDataSetChanged();
                    }
                    //reset editTexts
                    exercise.setText("");
                    reps.setText("");
                    sets.setText("");
                    exercise.requestFocus();
                    reps.setHint("Reps");
                    sets.setHint("Sets");
                }
            }
        });
    }
    //Method to save Exercises
    public void quickSave(){
        workoutID = sp.getInt("workoutID", -1);
        String title = MyWorkouts.workouts.get(workoutID);
        if(!title.equals("") && !title.equals("New")){
            List<Workout> workouts = db.getAllExercises();
            for (Workout wo : workouts) {
                String workoutTitle = wo.getWorkout();
                if (workoutTitle.equals(title)) {
                    db.deleteExercise(wo);
                }
            }
            for (int i = 0; i < exerciseText.size(); i++) {
                String exText = exerciseText.get(i);
                String rpText = repsText.get(i);
                String stText = setsText.get(i);
                commentList.addAll(woComment);
                String comText = commentList.get(i);
                db.addExercise(new Workout(title, exText, rpText, stText, comText));
            }
        }
    }

    //Method to retrive workout saved to database and update adapter
    public void retrieve(){
        clearAll();
        List<Workout> workouts = db.getAllExercises();
        for (Workout wo : workouts) {
            String workoutTitle = wo.getWorkout();
            if (workoutTitle.equals(MyWorkouts.workouts.get(workoutID))) {
                //tell adapter that it's being updated and not recreated after if/when new exercises added
                clicked++;
                //add to arrayLists because we delete then resave exercise if changed
                exerciseText.add(wo.getExercise());
                repsText.add(wo.getReps());
                setsText.add(wo.getSets());
                woComment.add(wo.getComment());
                Workout workout = new Workout(wo.getExercise(), wo.getReps(), wo.getSets(), wo.getComment());
                workoutList.add(workout);
            }
        }
    }

    //Clear all arrayLists
    public void clearAll(){
        exerciseText.clear();
        repsText.clear();
        setsText.clear();
        woComment.clear();
        commentList.clear();
        workoutList.clear();
    }

    public void popUpEdit(){
        LayoutInflater layoutInflater
                = (LayoutInflater)getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.comment_layout, null);
        final PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.showAsDropDown(toolbar, 0, 0);
        popupWindow.setFocusable(true);
        popupWindow.update();

        Button closeBtn = (Button) popupView.findViewById(R.id.doneBtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText commentText = (EditText) popupView.findViewById(R.id.commentText);
                comment = commentText.getText().toString();
                if(comment.equals("")) {
                    comment = "No Comments Made";
                }
                if(comment.contains(",")){
                    Toast.makeText(getApplicationContext(),"Please remove all commas from your workout comment", Toast.LENGTH_LONG).show();
                }
                else{
                    woComment.add(comment);
                    popupWindow.dismiss();
                }
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
        inflater.inflate(R.menu.menu3, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection for menu
        switch (item.getItemId()) {
            case R.id.action_logout:
                sp.edit().putBoolean("logged in", false).apply();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                break;

            case R.id.action_settings:
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                break;

            case android.R.id.home:
                //back button
                intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                break;

            case R.id.action_save:
                db = new SQLDatabase(this);
                List<Workout> workoutsDB = db.getAllExercises();
                String woTitle = MyWorkouts.workouts.get(workoutID);

                //make sure user entered a title and delete previous version of WO if already saved before to replace
                String title = MyWorkouts.workouts.get(workoutID);
                if(!title.equals("") && !title.equals("New")){
                    List<Workout> workouts = db.getAllExercises();
                    for (Workout wo : workouts) {
                        String workoutTitle = wo.getWorkout();
                        if (workoutTitle.equals(title)) {
                            db.deleteExercise(wo);
                        }
                    }
                    workoutListView = (ListView) findViewById(R.id.exerciseListView);
                    db = new SQLDatabase(this);
                    try {
                        for (int i = 0; i < exerciseText.size(); i++) {
                            String exText = exerciseText.get(i);
                            String rpText = repsText.get(i);
                            String stText = setsText.get(i);
                            commentList.addAll(woComment);
                            String comment = commentList.get(i);
                            db.addExercise(new Workout(title, exText, rpText, stText, comment));
                        }
                    } catch (IndexOutOfBoundsException ex) {
                        Toast.makeText(getApplicationContext(), "Error while saving", Toast.LENGTH_SHORT).show();
                    }

                    intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), "Workout saved!", Toast.LENGTH_SHORT).show();
                    clearAll();

                }
                else{
                    Toast.makeText(getApplicationContext(), "Don't forget to give it a title!", Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.action_share:
                new AlertDialog.Builder(this)
                        .setTitle("Share now")
                        .setMessage("Would you like to share this workout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                List<Workout> workoutsDB = db.getAllExercises();
                                String woTitle = MyWorkouts.workouts.get(workoutID);
                                int completeNum = sp.getInt("share number", 0);
                                completeNum++;
                                sp.edit().putInt("share number", completeNum).apply();
                                likes = 0;
                                sp = getApplicationContext().getSharedPreferences("achall9.com.fitnectapp", Context.MODE_PRIVATE);
                                workoutListView = (ListView) findViewById(R.id.exerciseListView);
                                exerciseText.clear();
                                repsText.clear();
                                setsText.clear();
                                woComment.clear();
                                commentList.clear();
                                db = new SQLDatabase(getApplicationContext());
                                workoutsDB = db.getAllExercises();

                                //set workout title in firebase
                                woTitle = MyWorkouts.workouts.get(workoutID);

                                //set each exercise, rep and text in firebase
                                for(Workout wo : workoutsDB){
                                    String workoutTitle = wo.getWorkout();
                                    if (workoutTitle.equals(woTitle)) {
                                        exerciseText.add(wo.getExercise());
                                        repsText.add(wo.getReps());
                                        setsText.add(wo.getSets());
                                        commentList.add(wo.getComment());
                                    }
                                }
                                Workout workout = new Workout();

                                workout.setExercise(exerciseText.toString());
                                workout.setReps(repsText.toString());
                                workout.setSets(setsText.toString());
                                commentList.addAll(woComment);
                                workout.setComment(commentList.toString());
                                workout.setWorkout(woTitle);
                                workout.setUsername(sp.getString("username", null));
                                workout.setLikes(likes);

                                UserInfo userInfo = new UserInfo();
                                userInfo.setSharedNumber(completeNum);
                                userInfo.setEmail(sp.getString("email", null));
                                userInfo.setUsername(sp.getString("username", null));
                                userInfo.setFirstName(sp.getString("first name", null));
                                userInfo.setLastName(sp.getString("last name", null));

                                Firebase.setAndroidContext(getApplicationContext());
                                final Firebase firebaseRef = new Firebase("https://fitnectapp.firebaseio.com/");
                                Firebase postRef = firebaseRef.child("Workouts");
                                //Share workout
                                String postName = workout.getWorkout() + ": " + workout.getUsername();
                                postRef.child(postName).setValue(workout);

                                //Save number shared to UserInfo
                                postRef = firebaseRef.child("user info");
                                postRef.child(userInfo.getUsername()).setValue(userInfo);
                                Toast.makeText(getApplicationContext(), "Your workout has been shared!", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public class CustomAdapter extends ArrayAdapter<Workout> {

        public CustomAdapter(Context context, int layoutResource, List<Workout> workoutList) {
            super(context, layoutResource, workoutList);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final Workout workout = getItem(position);

            if (convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                convertView = layoutInflater.inflate(R.layout.custom_list, null);
            }

            if (position != -1) {
                //Get each edit text
                final TextView exercise = (TextView) convertView.findViewById(R.id.exercise);
                final TextView reps = (TextView) convertView.findViewById(R.id.reps);
                final TextView sets = (TextView) convertView.findViewById(R.id.sets);
                final ImageButton comment = (ImageButton) convertView.findViewById(R.id.commentButton);
                assert comment != null;

                //populate data if already in database
                exercise.setText(workout.getExercise());
                reps.setText(workout.getReps());
                sets.setText(workout.getSets());
                comment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popUpView(workout.getComment());
                    }
                });
            }
            return convertView;
        }
    }

    //Text watcher class for TitleEntry
    private class MyTextWatcher implements TextWatcher{

        private View view;
        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String text = String.valueOf(charSequence);
            switch(view.getId()){
                case R.id.workoutTitle:
                    workoutID = sp.getInt("workoutID", -1);
                    MyWorkouts.workouts.set(workoutID, text);
                    title = text;
                    MyWorkouts.arrayAdapter.notifyDataSetChanged();
                    SharedPreferences sp = getApplicationContext().getSharedPreferences("achall9.com.fitnectapp", Context.MODE_PRIVATE);
                    if (MyWorkouts.set == null) {
                        MyWorkouts.set = new HashSet<>();
                    } else {
                        MyWorkouts.set.clear();
                    }
                    MyWorkouts.set.addAll(MyWorkouts.workouts);
                    sp.edit().remove("workouts").apply();
                    sp.edit().putStringSet("workouts", MyWorkouts.set).apply();
                    break;
            }
        }
        public void afterTextChanged(Editable editable) {
        }
    }
}
