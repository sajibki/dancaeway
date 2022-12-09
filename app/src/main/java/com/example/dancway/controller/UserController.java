package com.example.dancway.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.dancway.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class UserController {
    private User user;
    private FirebaseAuth auth;
    private Activity context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    //Instance Reference
    private static UserController single_instance = null;

    private UserController(Activity context){

        auth = FirebaseAuth.getInstance();
        this.context = context;
    // Setup storage
        sharedPreferences = context.getSharedPreferences("Session", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Restrict to singular Object
    public static UserController getInstance(Activity context){
        if (single_instance == null)
            single_instance = new UserController(context);
        return single_instance;
    }

    public void setUser(User newUser){user = newUser;}

    public User getUser(){return user;}

    public void setActivity(Activity newContext){context = newContext;}

    public Activity getActivityContext(){return context;}

    public void register(String email, String password, String repeat_password, String username){
        registerUser(email, password, repeat_password, username);
    }

    private void registerUser(String email, String password, String repeat_password ,String username) {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            user = User.getCurrentUser(auth.getCurrentUser(), username);
                            Toast.makeText(context, "User registered successfully", Toast.LENGTH_SHORT).show();
                            
							//On success store in permanent Storage
                            editor.putString("email",email);
                            editor.putString("pass",password);
                            editor.putBoolean("loggedin",true);
                            editor.apply();
                        }else{
                            Toast.makeText(context, "Registration Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // returns true on success
    public boolean autologin(){

        if ( sharedPreferences.getBoolean("loggedin" , false))
        {
            String email = sharedPreferences.getString("email", "");
            String password = sharedPreferences.getString("pass" , "");
            loginUser(email,password);
            return true;
        }
            return false;
    }

    public void login(String email, String password){loginUser(email,password);}

    private void loginUser(String email, String password){

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    user = User.getCurrentUser(auth.getCurrentUser(), email); //Stores FirebaseUser in user if its not null
                    Toast.makeText(context, "User logged in", Toast.LENGTH_SHORT).show();
                    
					//Permanent Storage
                    editor.putString("email",email);
                    editor.putString("pass",password);
                    editor.putBoolean("loggedin",true);
                    editor.apply();
                } else {
                    Toast.makeText(context, "Login Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Remove permanent Login Details
    public void logout(){
        editor.putString("email","");
        editor.putString("pass","");
        editor.putBoolean("loggedin",false);
        editor.apply();
    }

    // Check if logged in
    public boolean logstate(){
        boolean loggedin = sharedPreferences.getBoolean("loggedin", false);
        return loggedin;
    }


}
