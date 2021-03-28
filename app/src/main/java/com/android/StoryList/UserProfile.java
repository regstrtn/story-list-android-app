package com.android.StoryList;

import static com.android.StoryList.util.Util.logOutUser;
import static com.android.StoryList.util.Util.openAddStoryPage;
import static com.android.StoryList.util.Util.openFeedsPage;
import static com.android.StoryList.util.Util.openUserProfilePage;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.StoryList.util.Constants;
import com.android.StoryList.util.Util;
import com.example.StoryList.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;

public class UserProfile extends AppCompatActivity {

  private FirebaseAuth firebaseAuth;
  private FirebaseUser firebaseUser;
  private FirebaseFirestore fireStoreRef;


  private StoryListAdapter storyListAdapter;
  private RecyclerView r1;
  private Context ctx;

  private TextView displayNameTextView;
  

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.user_profile_activity);
    firebaseAuth = FirebaseAuth.getInstance();
    fireStoreRef = FirebaseFirestore.getInstance();

    redirectIfUserNotLoggedIn();
    setDisplayName();

    LinearLayoutManager mLayoutManager;
    mLayoutManager = new LinearLayoutManager(this);
    r1 = (RecyclerView) findViewById(R.id.StoriesList);
    r1.setLayoutManager(mLayoutManager);
    ctx = this;

    fetchStories(1000);

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_activity_option_menu, menu);
    MenuItem logOutMenuItem = menu.findItem(R.id.LogOutMenuItem);
    if (firebaseAuth.getCurrentUser()==null) {
      logOutMenuItem.setVisible(false);
    }
    return true;
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
      case R.id.HomeButtonMenuItem:
        openFeedsPage(UserProfile.this);
        break;
      case R.id.AddPostMenuItem:
        openAddStoryPage(UserProfile.this);
        break;
      case R.id.UserProfileMenuItem:
        openUserProfilePage(UserProfile.this);
        break;
      case R.id.LogOutMenuItem:
        logOutUser(firebaseAuth, UserProfile.this);
        invalidateOptionsMenu();
        break;
      default:
        super.onOptionsItemSelected(item);
        break;
    }
    return true;
  }

  private void redirectIfUserNotLoggedIn() {
    firebaseUser = firebaseAuth.getCurrentUser();
    if (firebaseUser == null) {
      Intent openLoginPageIntent = new Intent(UserProfile.this, UserLogin.class);
      startActivity(openLoginPageIntent);
      finish();
    }
  }

  private void setDisplayName() {
    displayNameTextView = (TextView) findViewById(R.id.ProfilePageDisplayName);
    displayNameTextView.setText(firebaseUser.getDisplayName());
  }

  private void fetchStories(int numStoriesToFetch) {
    // UNAUTHENTICATED Access will be removed after 30 days.
    fireStoreRef.collection("Stories")
        .whereEqualTo(Constants.STORY_AUTHOR_UID_FIELD, firebaseUser.getUid())
        .get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
          @Override
          public void onComplete(Task<QuerySnapshot> task) {
            if (task.isSuccessful()) {
              ArrayList<String> titles = new ArrayList<String>();
              ArrayList<String> descriptions = new ArrayList<String>();
              ArrayList<String> imageUrls = new ArrayList<String>();

              for (QueryDocumentSnapshot document : task.getResult()) {
                Log.d("Firebase", document.getId() + " => " + document.getData());
                titles.add(document.getString("Title"));
                descriptions.add(document.getString("MainBody"));
                imageUrls.add(document.getString("ImageUrl"));
              }
              storyListAdapter = new StoryListAdapter(
                  ctx, titles.toArray(new String[0]), descriptions.toArray(new String[0]), imageUrls.toArray(new String[0]));
              r1.setAdapter(storyListAdapter);

            } else {
              Log.w("Firebase", "Error getting documents.", task.getException());
            }
          }
        });
  }
}