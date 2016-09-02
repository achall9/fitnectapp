package achall9.com.fitnectapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Workout {

    //private variables
    String id;
    String workout;
    String exercise;
    String reps;
    String sets;
    String username;
    int likes;
    String comment;

    // Empty constructor  NECESSARY
    public Workout() {}


    public Workout(String exercise, String reps, String sets, String comment) {
        this.exercise = exercise;
        this.reps = reps;
        this.sets = sets;
        this.comment = comment;
    }



    // constructor without ID
    public Workout(String _workout, String _exercise, String _reps, String _sets, String comment) {
        this.workout = _workout;
        this.exercise = _exercise;
        this.reps = _reps;
        this.sets = _sets;
        this.comment = comment;
    }
    public Workout(String exercise, String reps, String sets){
        this.exercise = exercise;
        this.reps = reps;
        this.sets = sets;
    }

    public String getId(){
        return  this.id;
    }
    public void setId(String id){
        this.id = id;
    }

    //gets workout
    public String getWorkout() {
        return this.workout;
    }

    // setting exercise
    public void setWorkout(String workout) {
        this.workout = workout;
    }

    // getting name
    public String getExercise() {
        return this.exercise;
    }

    // setting exercise
    public void setExercise(String exercise) {
        this.exercise = exercise;
    }

    // getting reps
    public String getReps() {
        return this.reps;
    }

    // setting reps
    public void setReps(String reps) {
        this.reps = reps;
    }

    // getting sets
    public String getSets() {
        return this.sets;
    }

    // setting sets
    public void setSets(String sets) {
        this.sets = sets;
    }

    public String getUsername() {
        return this.username;
    }
    // setting sets
    public void setUsername(String username) {
        this.username = username;
    }

    public int getLikes(){
        return  this.likes;
    }
    public void setLikes(int likes){
        this.likes = likes;
    }

    public String getComment(){
        return  this.comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
}

