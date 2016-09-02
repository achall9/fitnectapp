package achall9.com.fitnectapp;

import android.app.SearchManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class WorkoutComplete extends AppCompatActivity {
    int index;
    TextView title;
    TextView user;
    TextView exercise;
    TextView tutorial;
    TextView reps;
    TextView sets;
    TextView comment;
    ImageButton previous;
    ImageButton next;
    Toolbar toolbar;
    String searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_complete);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //call layout texts and buttons
        title = (TextView) findViewById(R.id.beginTitle);
        user = (TextView) findViewById(R.id.beginUser);
        exercise = (TextView) findViewById(R.id.beginExercise);
        tutorial = (TextView) findViewById(R.id.tutorialSearch);
        reps = (TextView) findViewById(R.id.beginReps);
        sets = (TextView) findViewById(R.id.beginSets);
        comment = (TextView) findViewById(R.id.beginComment);
        previous = (ImageButton) findViewById(R.id.previousEx);
        next = (ImageButton) findViewById(R.id.nextEx);
        assert title != null;
        assert exercise != null;
        assert reps != null;
        assert sets != null;
        assert comment != null;
        assert next != null;
        assert previous != null;

        //set index of exercise to show in arraylist from Wo View activity
        index = 0;

        //set text to show individual exercise
        title.setText(WorkoutView.titleTextOfficial);
        user.setText(WorkoutView.username);
        exercise.setText(WorkoutView.exerciseText.get(index));
        reps.setText(WorkoutView.repsText.get(index));
        sets.setText(WorkoutView.setsText.get(index));
        comment.setText(WorkoutView.commentText.get(index));
        searchText = WorkoutView.exerciseText.get(index) + " tutorial";

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index++;
                previous.setVisibility(View.VISIBLE);
                if (index != WorkoutView.exerciseText.size()) {
                    exercise.setText(WorkoutView.exerciseText.get(index));
                    reps.setText(WorkoutView.repsText.get(index));
                    sets.setText(WorkoutView.setsText.get(index));
                    comment.setText(WorkoutView.commentText.get(index));
                    searchText = WorkoutView.exerciseText.get(index) + " tutorial";
//                    if (index == WorkoutView.exerciseText.size() - 1) {
//                        next.setText("Finish");
//                    }
                } else {
                    Intent intent = new Intent(getApplicationContext(), WorkoutView.class);
                    intent.putExtra("completed", true);
                    startActivity(intent);
                }
            }
        });

        if (index == 0) {
            previous.setVisibility(View.INVISIBLE);
        }
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                next.setText("Next");
                if (index > 0) {
                    index--;
                    exercise.setText(WorkoutView.exerciseText.get(index));
                    reps.setText(WorkoutView.repsText.get(index));
                    sets.setText(WorkoutView.setsText.get(index));
                    comment.setText(WorkoutView.commentText.get(index));
                    searchText = WorkoutView.exerciseText.get(index) + " tutorial";
                }
                if (index == 0) {
                    previous.setVisibility(View.INVISIBLE);
                }
            }
        });

        tutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, searchText);
                startActivity(intent);
            }
        });

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
            //back button
            case android.R.id.home:
                Intent intent = new Intent(this, WorkoutView.class);
                startActivity(intent);
                break;

            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;

        }
        return true;
    }
}
