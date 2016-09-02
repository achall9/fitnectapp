package achall9.com.fitnectapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;
import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    private EditText emailText;
    private EditText passwordText;
    private TextView signUpTextView;
    private boolean userAccept = false;
    SharedPreferences sp;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sp = this.getSharedPreferences("achall9.com.fitnectapp", Context.MODE_PRIVATE);
        //if logged in previously, skip login screen
        boolean loggedIn = sp.getBoolean("logged in", false);
        mAuth = FirebaseAuth.getInstance();
        Firebase.setAndroidContext(this);

        if(loggedIn) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        LoginActivity.this.finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signUpTextView = (TextView) findViewById(R.id.signUpTextView);
        signUpTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // this is where you start the signup Activity if not signed up already
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    //This is where you start the main activity if login successful
    public void login(View view) {
        emailText= (EditText) findViewById(R.id.emailEditText);
        passwordText = (EditText) findViewById(R.id.passwordEditText);
        final String emailT = emailText.getText().toString();
        String pwT = passwordText.getText().toString();

        if(!userAccept){
            mAuth.signInWithEmailAndPassword(emailT, pwT)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Intent intent = new Intent(getApplicationContext(),GymSelectActivity.class);
                            intent.putExtra("email", emailT);
                            sp.edit().putBoolean("logged in", true).apply();
                            startActivity(intent);
                            Toast.makeText(getApplicationContext(), "Log in successful", Toast.LENGTH_LONG).show();
                            if (!task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Log in failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }
}