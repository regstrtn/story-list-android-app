package com.android.StoryList;

import static com.android.StoryList.util.Constants.FETCH_STORY_COUNT;
import static com.android.StoryList.util.Util.logOutUser;
import static com.android.StoryList.util.Util.openAddStoryPage;
import static com.android.StoryList.util.Util.openUserProfilePage;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.widget.Toast;
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

public class MainActivity extends AppCompatActivity {

  RecyclerView r1;
  String titles[], descriptions[];
  int images[] = {R.drawable.aqueductsegovia, R.drawable.pompeii, R.drawable.hagiasofia, R.drawable.pantheon};
  StoryListAdapter storyListAdapter;
  Context ctx;

  private boolean loading = true;
  int pastVisiblesItems, visibleItemCount, totalItemCount;

  // Firebase fields.
  FirebaseFirestore dbRef;
  FirebaseAuth firebaseAuth;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    firebaseAuth = FirebaseAuth.getInstance();
    setContentView(R.layout.activity_main);
    LinearLayoutManager mLayoutManager;
    mLayoutManager = new LinearLayoutManager(this);
    r1 = (RecyclerView) findViewById(R.id.StoriesList);
    titles = getResources().getStringArray(R.array.StoryTitles);
    descriptions = getResources().getStringArray(R.array.StoryContent);
    ctx = this;
    addPaginationToRecyclerView(r1, mLayoutManager);
    r1.setLayoutManager(mLayoutManager);

    // storyListAdapter = new StoryListAdapter(this, titles, descriptions, images);
    // r1.setAdapter(storyListAdapter);
    dbRef = FirebaseFirestore.getInstance();
    fetchStories(FETCH_STORY_COUNT);
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
        break;
      case R.id.AddPostMenuItem:
        openAddStoryPage(MainActivity.this);
        break;
      case R.id.UserProfileMenuItem:
        openUserProfilePage(MainActivity.this);
        break;
      case R.id.LogOutMenuItem:
        logOutUser(firebaseAuth, MainActivity.this);
        invalidateOptionsMenu();
        break;
      default:
        super.onOptionsItemSelected(item);
        break;
    }
    return true;
  }

  private void fetchStories(int numStoriesToFetch) {
    // UNAUTHENTICATED Access will be removed after 30 days.
    dbRef.collection("Stories")
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
                  Util.shufflePosts(titles, descriptions, imageUrls);
                  storyListAdapter = new StoryListAdapter(
                      ctx, titles.toArray(new String[0]), descriptions.toArray(new String[0]), imageUrls.toArray(new String[0]));
                  r1.setAdapter(storyListAdapter);

                } else {
                    Log.w("Firebase", "Error getting documents.", task.getException());
                }
            }
        });
  }

  public void addPaginationToRecyclerView(RecyclerView r1, LinearLayoutManager mLayoutManager) {
    r1.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (dy > 0) { //check for scroll down
          visibleItemCount = mLayoutManager.getChildCount();
          totalItemCount = mLayoutManager.getItemCount();
          pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

          if (loading) {
            if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
              loading = false;
              Log.v("...", "Last Item Wow !");
              // Do pagination.. i.e. fetch new data
              fetchStories(FETCH_STORY_COUNT);
              loading = true;
            }
          }
        }
      }
    });
  }

}