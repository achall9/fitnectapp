package achall9.com.fitnectapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class GymSelectActivity extends AppCompatActivity {

    static ArrayList<String> gymList = new ArrayList<>();
    private ArrayList<String> searchedGyms = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    Toolbar toolbar;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gym_select);
        gymList.clear();
        searchedGyms.clear();
        sp = this.getSharedPreferences("achall9.com.fitnectapp", Context.MODE_PRIVATE);

        //set the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //add data from online to SharedPreferences in case app was deleted and reinstalled or somehow info was erased
        retrieveInfo();

        //ADD GYMS HERE
        gymList.add("Gym Not Listed");
        gymList.add("Anytime Fitness");
        gymList.add("Around The Clock Fitness");
        gymList.add("Crunch Fitness");
        gymList.add("FGCU Fitness Center");
        gymList.add("Gold's Gym");
        gymList.add("Gulf Coast Fitness");
        gymList.add("LA Fitness");
        gymList.add("NSU Recplex");
        gymList.add("Planet Fitness");


        ListView gymListView = (ListView) findViewById(R.id.gymListView);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, gymList);
        assert gymListView != null;
        gymListView.setAdapter(arrayAdapter);

        //Make clickable to take to main
        gymListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                String myGym = sp.getString("gym name", null);
                if (myGym != null) {
                    sp.edit().remove("gym name").apply();
                    sp.edit().putString("gym name", gymList.get(position)).apply();
                } else {
                    sp.edit().putString("gym name", gymList.get(position)).apply();
                }
                Firebase ref = new Firebase("https://fitnectapp.firebaseio.com/");
                myGym = sp.getString("gym name", null);
                assert myGym != null;
                UserInfo userInfo = new UserInfo();
                String uName = sp.getString("username", null);
                if(uName != null){
                    userInfo.setUsername(uName);
                    ref.child(myGym).push().setValue(userInfo);
                }
                startActivity(intent);
            }
        });
    }

    //search bar and toolbar actions
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu5, menu);

        SearchView searchView;
        final MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                myActionMenuItem.collapseActionView();
                searchedGyms.clear();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                for (int i = 0; i < gymList.size(); i++) {
                    if (!gymList.get(i).toLowerCase().contains(s)) {
                        searchedGyms.add(gymList.get(i));
                        gymList.remove(i);
                    }
                }
                arrayAdapter.notifyDataSetChanged();

                if (s.equals("") && searchedGyms.size() > 0) {
                    for (int i = 0; i < searchedGyms.size(); i++) {
                        gymList.add(searchedGyms.get(i));
                    }
                    arrayAdapter.notifyDataSetChanged();
                    searchedGyms.clear();
                }
                return true;
            }
        });
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

            case R.id.action_logout:
                sp.edit().putBoolean("logged in", false).apply();
                FirebaseAuth.getInstance().signOut();
                intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    private void retrieveInfo() {
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
        sp = this.getSharedPreferences("achall9.com.fitnectapp", Context.MODE_PRIVATE);
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        if (ds.getKey().equals("user info")) {
            for (DataSnapshot data : ds.getChildren()) {
                //set to userInfo instance and add to arraylist if needed
                if (email != null && data.getValue(UserInfo.class).getEmail().toLowerCase().equals(email.toLowerCase())) {
                    String username = data.getValue(UserInfo.class).getUsername();
                    String firstName = data.getValue(UserInfo.class).getFirstName();
                    String lastName = data.getValue(UserInfo.class).getLastName();
                    sp.edit().putString("username", username).apply();
                    sp.edit().putString("first name", firstName).apply();
                    sp.edit().putString("last name", lastName).apply();
                    sp.edit().putString("email", email).apply();
                }
            }
        }
    }
}
