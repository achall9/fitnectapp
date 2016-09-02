package achall9.com.fitnectapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WorkoutsShared extends Fragment {
    static ArrayList<String> sharedWorkouts = new ArrayList<>();
    private ListView sharedWorkoutList;
    static ArrayList<Integer> likesList = new ArrayList<>();
    SharedPreferences sp;
    MyCustomAdapter customAdapter;
    private ArrayList<String> searchedWorkouts = new ArrayList<>();
    private ArrayList<Integer> searchedLikes = new ArrayList<>();
    private ArrayList<String> searchedUsers = new ArrayList<>();
    ProgressDialog dialog;
    private FragmentActivity fragmentActivity;
    static ArrayList<String> myGymUsers = new ArrayList<>();
    static ArrayList<String> allGymUsers = new ArrayList<>();
    Map<String, Integer> sortMap;
    static MenuItem titleItem;
    private ArrayList<String> splitList = new ArrayList<>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentActivity = super.getActivity();
        View rootView = inflater.inflate(R.layout.activity_workouts_shared, container, false);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        dialog = new ProgressDialog(fragmentActivity);
        dialog.setMessage("Loading");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();

        sharedWorkouts.clear();
        searchedWorkouts.clear();
        likesList.clear();
        searchedLikes.clear();
        myGymUsers.clear();
        allGymUsers.clear();
        searchedUsers.clear();

        sp = fragmentActivity.getSharedPreferences("achall9.com.fitnectapp", Context.MODE_PRIVATE);

        sharedWorkoutList = (ListView) fragmentActivity.findViewById(R.id.sharedExerciselistView);

        Firebase.setAndroidContext(fragmentActivity);
        retrieveInfo();

        sharedWorkoutList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(fragmentActivity, WorkoutView.class);
                intent.putExtra("title", position);
                fragmentActivity.startActivity(intent);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu6, menu);
        super.onCreateOptionsMenu(menu, inflater);

        titleItem = menu.findItem(R.id.action_filterDropDown);

        SearchView searchView;
        final MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //reset listview to show all WO when search submitted
                myActionMenuItem.collapseActionView();
                searchedWorkouts.clear();
                searchedLikes.clear();
                searchedUsers.clear();
                return true;
            }

            //change listview to show WO with searched charsequence
            @Override
            public boolean onQueryTextChange(String s) {
                for (int i = 0; i < sharedWorkouts.size(); i++) {
                    if (!sharedWorkouts.get(i).toLowerCase().contains(s.toLowerCase())) {
                        searchedWorkouts.add(sharedWorkouts.get(i));
                        searchedLikes.add(likesList.get(i));
                        searchedUsers.add(allGymUsers.get(i));
                        sharedWorkouts.remove(i);
                        likesList.remove(i);
                        allGymUsers.remove(i);
                        customAdapter.notifyDataSetChanged();
                    }
                    //reset list if want to change charsequence
                    if (s.equals("") && searchedWorkouts.size() > 0) {
                        for (i = 0; i < searchedWorkouts.size(); i++) {
                            sharedWorkouts.add(searchedWorkouts.get(i));
                            likesList.add(searchedLikes.get(i));
                            allGymUsers.add(searchedUsers.get(i));
                        }
                        customAdapter.notifyDataSetChanged();
                        searchedWorkouts.clear();
                        searchedLikes.clear();
                        searchedUsers.clear();
                    }
                }
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection for menu
        switch (item.getItemId()) {
            case R.id.action_all:
                retrieveInfo();
                titleItem.setTitle("All Workouts");
                break;
            case R.id.action_rating:
                sortFitLove();
                titleItem.setTitle("Popular");
                break;

            case R.id.action_my_gym:
                myGym();
                titleItem.setTitle("My Gym");
                break;

            case android.R.id.home:
                Intent intent = new Intent(fragmentActivity, MainActivity.class);
                startActivity(intent);
                break;

            case R.id.action_settings:
                intent = new Intent(fragmentActivity, SettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.action_logout:
                sp.edit().putBoolean("logged in", false).apply();
                FirebaseAuth.getInstance().signOut();
                intent = new Intent(fragmentActivity, LoginActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    public void sortFitLove() {
        sharedWorkouts.clear();
        likesList.clear();
        allGymUsers.clear();
        splitList.clear();

        Set<Map.Entry<String, Integer>> set = sortMap.entrySet();
        List<Map.Entry<String, Integer>> sortList = new ArrayList<>(set);
        Collections.sort(sortList, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        for (Map.Entry<String, Integer> entry : sortList) {
            Collections.addAll(splitList, entry.getKey().split(":"));
            likesList.add(entry.getValue());
        }
        for (int i = 0; i < splitList.size(); i++) {
            if (i % 2 == 0) {
                sharedWorkouts.add(splitList.get(i));
            } else {
                allGymUsers.add(splitList.get(i));
            }
        }
        customAdapter.notifyDataSetChanged();
    }

    //filter workouts only created by users from your own gym needs improvement. Contains may not be equal
    public void myGym() {
        for (int i = 0; i < allGymUsers.size(); i++) {
            String gymUser = allGymUsers.get(i);
            if (!myGymUsers.contains(gymUser)) {
                sharedWorkouts.remove(i);
                likesList.remove(i);
                allGymUsers.remove(i);
            }
        }
        customAdapter.notifyDataSetChanged();
    }

    private void retrieveInfo() {
        sharedWorkouts.clear();
        searchedWorkouts.clear();
        likesList.clear();
        searchedLikes.clear();
        myGymUsers.clear();
        allGymUsers.clear();

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
            sortMap = new HashMap<>();
            for (DataSnapshot data : ds.getChildren()) {
                Workout workout = new Workout();
                workout.setUsername(data.getValue(Workout.class).getUsername());
                workout.setWorkout(data.getValue(Workout.class).getWorkout());
                workout.setLikes(data.getValue(Workout.class).getLikes());
                sharedWorkouts.add(workout.getWorkout());
                likesList.add(workout.getLikes());
                boolean complete = sp.getBoolean(workout.getWorkout() + " complete", false);
                if (complete) {
                    allGymUsers.add(workout.getUsername() + "   |   Completed");
                }
                allGymUsers.add(workout.getUsername());
                //add values to map to keep connected when filtered
                sortMap.put(workout.getWorkout() + ":" + workout.getUsername(), workout.getLikes());
            }
        }
        //for when sorting workouts by users gym only
        String myGym = sp.getString("gym name", null);
        if (myGym != null && ds.getKey().equals(myGym)) {
            for (DataSnapshot data : ds.getChildren()) {
                myGymUsers.add(data.getValue(Workout.class).getUsername());
            }
        }

        if (sharedWorkouts.size() > 0) {
            //hide loading screen, data has loaded
            dialog.dismiss();
            sharedWorkoutList = (ListView) fragmentActivity.findViewById(R.id.sharedExerciselistView);
            customAdapter = new MyCustomAdapter(sharedWorkouts, fragmentActivity);
            sharedWorkoutList.setAdapter(customAdapter);
        }
    }


    class MyCustomAdapter extends BaseAdapter implements ListAdapter {
        private ArrayList<String> sharedWorkouts = new ArrayList<>();
        private Context context;


        public MyCustomAdapter(ArrayList<String> sharedWorkouts, Context context) {
            this.sharedWorkouts = sharedWorkouts;
            this.context = context;
            Firebase.setAndroidContext(this.context);
        }

        @Override
        public int getCount() {
            return sharedWorkouts.size();
        }

        @Override
        public Object getItem(int pos) {
            return sharedWorkouts.get(pos);
        }

        @Override
        public long getItemId(int pos) {
            return 0;
        }

        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.custom_list2, null);
            }

            //Handle TextView and display Workout name
            final TextView sharedText = (TextView) view.findViewById(R.id.sharedText);
            sharedText.setText(sharedWorkouts.get(position));

            //Display username
            TextView userText = (TextView) view.findViewById(R.id.userSharedText);
            userText.setText(WorkoutsShared.allGymUsers.get(position));

            //Display likes
            if (WorkoutsShared.likesList.size() > 0) {
                TextView likeText = (TextView) view.findViewById(R.id.likeText);
                String like = Integer.toString(WorkoutsShared.likesList.get(position));
                likeText.setText(like);
            }
            return view;
        }
    }

}

