package achall9.com.fitnectapp;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExerciseListActivity extends AppCompatActivity {

    static List<String> freeWeightList = new ArrayList<>();
    static List<String> machineList = new ArrayList<>();
    static boolean machineOn;
    private Resources resources;
    static List<String> allExerciseList = new ArrayList<>();
    Toolbar toolbar;
    static Boolean cardio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_list);
        final ListView exerciseView = (ListView) findViewById(R.id.exSearchListView);
        assert exerciseView != null;
        freeWeightList.clear();
        machineList.clear();
        allExerciseList.clear();
        resources = getResources();
        cardio = false;

        //set up toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final CustomAdapter fwAdapter = new CustomAdapter(this, R.layout.custom_exercise_list, freeWeightList);
        final CustomAdapter machineAdapter = new CustomAdapter(this, R.layout.custom_exercise_list, machineList);

//        final ArrayAdapter<String> fwAdapter = new ArrayAdapter<String>(this, CustomAdapter, freeWeightList);
//        final ArrayAdapter<String> machineAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, machineList);
        Intent intent = getIntent();
        int exercises = intent.getIntExtra("Exercise", -1);
        switch (exercises) {
            ///add to each list through object or something to store all data at once
            case 1: //abs
                try {
                    String output = LoadFile("abs_fw", true);
                    Collections.addAll(freeWeightList, output.split(","));
                    output = LoadFile("abs_mcn", true);
                    Collections.addAll(machineList, output.split(","));
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Error loading exercises", Toast.LENGTH_LONG).show();
                }
                exerciseView.setAdapter(fwAdapter);
                break;
            case 2: //back
                try {
                    String output = LoadFile("back_fw", true);
                    Collections.addAll(freeWeightList, output.split(","));
                    output = LoadFile("back_mcn", true);
                    Collections.addAll(machineList, output.split(","));
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Error loading exercises", Toast.LENGTH_LONG).show();
                }
                exerciseView.setAdapter(fwAdapter);
                break;
            case 3: //biceps
                try {
                    String output = LoadFile("biceps_fw", true);
                    Collections.addAll(freeWeightList, output.split(","));
                    output = LoadFile("biceps_mcn", true);
                    Collections.addAll(machineList, output.split(","));
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Error loading exercises", Toast.LENGTH_LONG).show();
                }
                exerciseView.setAdapter(fwAdapter);
                break;
            case 4: //cardio
                try {
                    String output = LoadFile("cardio_fw", true);
                    Collections.addAll(freeWeightList, output.split(","));
                    output = LoadFile("cardio_mcn", true);
                    Collections.addAll(machineList, output.split(","));
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Error loading exercises", Toast.LENGTH_LONG).show();
                }
                exerciseView.setAdapter(fwAdapter);
                cardio = true;
                break;
            case 5: //chest
                try {
                    String output = LoadFile("chest_fw", true);
                    Collections.addAll(freeWeightList, output.split(","));
                    output = LoadFile("chest_mcn", true);
                    Collections.addAll(machineList, output.split(","));
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Error loading exercises", Toast.LENGTH_LONG).show();
                }
                exerciseView.setAdapter(fwAdapter);
                break;
            case 6: //legs
                try {
                    String output = LoadFile("legs_fw", true);
                    Collections.addAll(freeWeightList, output.split(","));
                    output = LoadFile("legs_mcn", true);
                    Collections.addAll(machineList, output.split(","));
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Error loading exercises", Toast.LENGTH_LONG).show();
                }
                exerciseView.setAdapter(fwAdapter);
                break;
            case 7: //shoulders
                try {
                    String output = LoadFile("shoulders_fw", true);
                    Collections.addAll(freeWeightList, output.split(","));
                    output = LoadFile("shoulders_mcn", true);
                    Collections.addAll(machineList, output.split(","));
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Error loading exercises", Toast.LENGTH_LONG).show();
                }
                exerciseView.setAdapter(fwAdapter);
                break;
            case 8: //triceps
                try {
                    String output = LoadFile("triceps_fw", true);
                    Collections.addAll(freeWeightList, output.split(","));
                    output = LoadFile("triceps_mcn", true);
                    Collections.addAll(machineList, output.split(","));
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Error loading exercises", Toast.LENGTH_LONG).show();
                }
                exerciseView.setAdapter(fwAdapter);
                break;
        }

        final Button freeWeight = (Button) findViewById(R.id.freeWeight);
        final Button machines = (Button) findViewById(R.id.machines);
        assert freeWeight != null;
        assert machines != null;

        freeWeight.setBackgroundColor(Color.parseColor("#7986CB"));
        machineOn = false;

        freeWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (machineOn) {
                    freeWeight.setBackgroundColor(Color.parseColor("#7986CB"));
                    machines.setBackgroundColor(Color.parseColor("#ffffff"));
                    machineOn = false;
                    exerciseView.setAdapter(fwAdapter);
                }
            }
        });

        machines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!machineOn) {
                    freeWeight.setBackgroundColor(Color.parseColor("#ffffff"));
                    machines.setBackgroundColor(Color.parseColor("#7986CB"));
                    machineOn = true;
                    exerciseView.setAdapter(machineAdapter);
                }
            }
        });

        exerciseView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), WorkoutEdit.class);
                intent.putExtra("Exercise",position);
                startActivity(intent);
            }
        });
    }

    public String LoadFile(String fileName, boolean loadFromRawFolder) throws IOException {
        //Create a InputStream to read the file into
        InputStream input;
        if (loadFromRawFolder) {
            //get the resource id from the file name
            int rID = resources.getIdentifier("achall9.com.fitnectapp:raw/" + fileName, null, null);
            //get the file as a stream
            input = resources.openRawResource(rID);
        } else {
            //get the file as a stream
            input = resources.getAssets().open(fileName);
        }
        //create a buffer that has the same size as the InputStream
        byte[] buffer = new byte[input.available()];
        //read the text file as a stream, into the buffer
        input.read(buffer);
        //create a output stream to write the buffer into
        ByteArrayOutputStream oS = new ByteArrayOutputStream();
        //write this buffer to the output stream
        oS.write(buffer);
        //Close the Input and Output streams
        oS.close();
        input.close();
        //return the output stream as a String
        return oS.toString();
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
            case R.id.action_settings:
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                break;
            case android.R.id.home:
                //back button
                intent = new Intent(getApplicationContext(),WorkoutEdit.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    public class CustomAdapter extends ArrayAdapter<String> {

        private int layoutResource;

        public CustomAdapter(Context context, int layoutResource, List<String> exerciseList) {
            super(context, layoutResource, exerciseList);
            this.layoutResource = layoutResource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                convertView = layoutInflater.inflate(layoutResource, null);
            }

            final String exerciseText = getItem(position);

            if (position != -1) {
                //Get each edit text
                final TextView exercise = (TextView) convertView.findViewById(R.id.exerciseListItem);
                exercise.setText(exerciseText);

                TextView tutorial = (TextView) convertView.findViewById(R.id.tutorialSearchExList);
                tutorial.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(getContext())
                                .setTitle("Tutorial")
                                .setMessage("Would you like to search a tutorial for this exercise?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                                        intent.putExtra(SearchManager.QUERY, exerciseText + " Tutorial");
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
