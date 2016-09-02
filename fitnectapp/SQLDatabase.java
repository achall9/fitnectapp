package achall9.com.fitnectapp;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
public class SQLDatabase extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "workoutData";

    // Contacts table name
    private static final String TABLE_WORKOUTS = "workouts";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_WORKOUT = "workout";
    private static final String KEY_EXERCISE = "exercise";
    private static final String KEY_REPS = "reps";
    private static final String KEY_SETS = "sets";
    private static final String KEY_COMMENT = "comment";
    private static final String KEY_LIKES = "likes";

    public SQLDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_WORKOUTS_TABLE = "CREATE TABLE " + TABLE_WORKOUTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"+ KEY_WORKOUT + " TEXT," + KEY_EXERCISE + " TEXT,"
                + KEY_REPS + " TEXT," + KEY_SETS + " TEXT," + KEY_COMMENT + " TEXT," + KEY_LIKES + " INT" + ")";
        db.execSQL(CREATE_WORKOUTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUTS);

        // Create tables again
        onCreate(db);
    }
    // Adding new workout
    public void addExercise(Workout workout) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_WORKOUT, workout.getWorkout()); // Workout exercise
        values.put(KEY_EXERCISE, workout.getExercise()); // Workout exercise
        values.put(KEY_REPS, workout.getReps()); // Workout number of reps
        values.put(KEY_SETS, workout.getSets());// Workout number of sets
        values.put(KEY_COMMENT, workout.getComment()); //the comment
        values.put(KEY_LIKES, workout.getLikes()); //add likes

        // Inserting Row
        db.insert(TABLE_WORKOUTS, null, values);
        db.close(); // Closing database connection
    }

    // Getting single exercise with reps and sets
//    public Workout getExercise(int id) {
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        Cursor cursor = db.query(TABLE_WORKOUTS, new String[] { KEY_ID, KEY_WORKOUT,
//                        KEY_EXERCISE, KEY_REPS, KEY_SETS}, KEY_ID + "=?",
//                new String[] { String.valueOf(id) }, null, null, null, null);
//
//        if (cursor != null)
//            cursor.moveToFirst();
//
//        Workout workout = new Workout(Integer.parseInt(cursor.getString(0)),
//                cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
//        cursor.close();
//        // return workout
//        return workout;
//    }

    // Getting All Exercises
    public List<Workout> getAllExercises() {
        List<Workout> workoutList = new ArrayList<Workout>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_WORKOUTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Workout workout = new Workout();
                workout.setWorkout(cursor.getString(1));
                workout.setExercise(cursor.getString(2));
                workout.setReps(cursor.getString(3));
                workout.setSets(cursor.getString(4));
                workout.setComment(cursor.getString(5));
                workout.setLikes(cursor.getInt(6));
                // Adding workout to list
                workoutList.add(workout);
            } while (cursor.moveToNext());
        }
        cursor.close();
        // return contact list
        return workoutList;
    }

    // Getting exercise Count
    public int getExerciseCount() {
        String countQuery = "SELECT * FROM " + TABLE_WORKOUTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        // return count
        return cursor.getCount();
    }
    // Updating single exercise
    public int updateExercise(Workout workout) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_EXERCISE, workout.getExercise());
        values.put(KEY_REPS, workout.getReps());
        values.put(KEY_SETS, workout.getSets());
        values.put(KEY_COMMENT, workout.getComment());

        // updating row
        return db.update(TABLE_WORKOUTS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(workout.getId()) });
    }

    // Deleting single exercise
    public void deleteExercise(Workout workout) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WORKOUTS,KEY_WORKOUT + " = ?", new String[] {String.valueOf(workout.getWorkout())} );
        db.close();
    }

    public void deleteExercise(String exercise, String reps, String sets){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WORKOUTS,KEY_EXERCISE + " = ?", new String[] {String.valueOf(exercise)} );
        db.delete(TABLE_WORKOUTS,KEY_REPS + " = ?", new String[] {String.valueOf(reps)} );
        db.delete(TABLE_WORKOUTS,KEY_SETS + " = ?", new String[] {String.valueOf(sets)} );
        db.close();
    }


}


