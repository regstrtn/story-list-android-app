package com.android.StoryList;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.StoryList.util.Constants;
import com.example.StoryList.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserProfile extends AppCompatActivity {

  private FirebaseAuth firebaseAuth;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.user_profile_activity);
    firebaseAuth = FirebaseAuth.getInstance();
    redirectIfUserNotLoggedIn();
    findViewById(R.id.textView3).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(UserProfile.this, MainActivity.class));
      }
    });

  }

  private void redirectIfUserNotLoggedIn() {
    FirebaseUser user = firebaseAuth.getCurrentUser();
    if (user == null) {
      Intent openLoginPageIntent = new Intent(UserProfile.this, UserLogin.class);
      startActivity(openLoginPageIntent);
      finish();
    }
  }
}