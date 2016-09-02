package achall9.com.fitnectapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {
    SharedPreferences sp;
    ArrayList<String> settingsInfo = new ArrayList<>();
    static ListView listView;
    Toolbar toolbar;
    static String username;
    static String email;
    static String userCompare;
    static String myGym;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sp = this.getSharedPreferences("achall9.com.fitnectapp", Context.MODE_PRIVATE);
        settingsInfo.clear();

        //set up toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        email = sp.getString("email", null);
        assert email != null;
        username = sp.getString("username", null);
        myGym = sp.getString("gym name", null);

        settingsInfo.add("Email: " + email);
        settingsInfo.add("Username: " + username);
        settingsInfo.add("Password: Reset Password?");
        settingsInfo.add("My Gym: " + myGym);
        settingsInfo.add("Logout");

        listView = (ListView) findViewById(R.id.settingListview);
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, settingsInfo);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        newEmail();
                        break;
                    case 1:
                        //change username feature possible for future
                        break;
                    case 2:
                        newPassword();
                        break;
                    case 3:
                        Intent intent = new Intent(getApplicationContext(), GymSelectActivity.class);
                        startActivity(intent);
                        break;
                    case 4:
                        FirebaseAuth.getInstance().signOut();
                        sp.edit().putBoolean("logged in", false).apply();
                        intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });
    }

    public void newPassword() {
        //set up popup window and hide settings listview
        LayoutInflater layoutInflater
                = (LayoutInflater) getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.password_reset, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.showAsDropDown(toolbar, 0, 0);
        popupWindow.setFocusable(true);
        popupWindow.update();
        listView.setVisibility(View.INVISIBLE);

        final EditText oldPW = (EditText) popupView.findViewById(R.id.oldPassword);
        final EditText newPW = (EditText) popupView.findViewById(R.id.newPassword);
        final EditText newVerify = (EditText) popupView.findViewById(R.id.verifyNew);
        Button reset = (Button) popupView.findViewById(R.id.resetPW);
        Button cancel = (Button) popupView.findViewById(R.id.cancel);
        //when reset button clicked
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = sp.getString("email", null);
                assert email != null;
                String oldPass = oldPW.getText().toString();
                String newPass = newPW.getText().toString();
                String verifyPass = newVerify.getText().toString();
                if (!oldPass.equals("") && !newPass.equals("") && !verifyPass.equals("")) {
                    if (newPass.equals(verifyPass)) {
                        Firebase ref = new Firebase("https://fitnectapp.firebaseio.com/");
                        ref.changePassword(email, oldPass, newPass, new Firebase.ResultHandler() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(getApplicationContext(), "Password has been changed successfully!", Toast.LENGTH_LONG).show();
                                listView.setVisibility(View.VISIBLE);
                                popupWindow.dismiss();
                            }

                            @Override
                            public void onError(FirebaseError firebaseError) {

                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "New passwords do not match", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please fill in all fieds to reset password", Toast.LENGTH_LONG).show();
                }
            }
        });
        //cancel button
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                listView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void newEmail() {
        //set up popup window and hide settings listview
        LayoutInflater layoutInflater
                = (LayoutInflater) getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.password_reset, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.showAsDropDown(toolbar, 0, 0);
        popupWindow.setFocusable(true);
        popupWindow.update();
        listView.setVisibility(View.INVISIBLE);

        //use password Layout, But customize for changing email
        final EditText oldPW = (EditText) popupView.findViewById(R.id.oldPassword);
        final EditText newPW = (EditText) popupView.findViewById(R.id.newPassword);
        final EditText newVerify = (EditText) popupView.findViewById(R.id.verifyNew);
        oldPW.setInputType(InputType.TYPE_CLASS_TEXT);
        newVerify.setInputType(InputType.TYPE_CLASS_TEXT);
        Button reset = (Button) popupView.findViewById(R.id.resetPW);
        Button cancel = (Button) popupView.findViewById(R.id.cancel);
        TextView tv1 = (TextView) popupView.findViewById(R.id.oldPwTextView);
        TextView tv2 = (TextView) popupView.findViewById(R.id.newPwTextView);
        TextView tv3 = (TextView) popupView.findViewById(R.id.verifyPwTextView);
        //customize for email change
        tv1.setText("Email:");
        tv2.setText("Password:");
        tv3.setText("New Email:");
        reset.setText("Change Email");

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPass = oldPW.getText().toString();
                String newPass = newPW.getText().toString();
                final String verifyPass = newVerify.getText().toString();
                if (!oldPass.equals("") && !newPass.equals("") && !verifyPass.equals("")) {
                    Firebase ref = new Firebase("https://fitnectapp.firebaseio.com/");
                    ref.changeEmail(oldPass, newPass, verifyPass, new Firebase.ResultHandler() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getApplicationContext(), "Email has been changed successfully!", Toast.LENGTH_LONG).show();
                            sp.edit().putString("email", verifyPass).apply();
                            popupWindow.dismiss();
                            listView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(FirebaseError firebaseError) {
                            Toast.makeText(getApplicationContext(), "Incorrect email or password", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Please fill in all fieds to reset password", Toast.LENGTH_LONG).show();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                listView.setVisibility(View.VISIBLE);
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
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case android.R.id.home: //back button
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }
}