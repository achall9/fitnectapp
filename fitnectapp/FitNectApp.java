package achall9.com.fitnectapp;

import android.app.Application;
import com.firebase.client.Firebase;

public class FitNectApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
