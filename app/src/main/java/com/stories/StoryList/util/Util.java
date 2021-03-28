package com.stories.StoryList.util;


import static androidx.core.content.ContextCompat.startActivity;
import static com.stories.StoryList.util.Constants.MAX_IMAGE_SIZE;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Toast;
import com.stories.StoryList.AddNewStory;
import com.stories.StoryList.MainActivity;
import com.stories.StoryList.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Util {
  public static void shufflePosts(ArrayList<String> titles, ArrayList<String> descriptions,
      ArrayList<String> images) {
    Random rnd = new Random();
    int seed = rnd.nextInt();
    Collections.shuffle(titles, new Random(seed));
    Collections.shuffle(descriptions, new Random(seed));
    Collections.shuffle(images, new Random(seed));
  }

  public static void openAddStoryPage(Context ctx) {
    Intent openAddStoryPageIntent = new Intent(ctx, AddNewStory.class);
    startActivity(ctx, openAddStoryPageIntent, new Bundle());
  }

  public static void openUserProfilePage(Context ctx) {
    Intent openUserProfilePageIntent = new Intent(ctx, UserProfile.class);
    startActivity(ctx, openUserProfilePageIntent, new Bundle());
  }

  public static void openFeedsPage(Context ctx) {
    Intent openFeedsPageIntent = new Intent(ctx, MainActivity.class);
    startActivity(ctx, openFeedsPageIntent, new Bundle());
  }

  public static void logOutUser(FirebaseAuth firebaseAuth, Context ctx) {
    FirebaseUser user = firebaseAuth.getCurrentUser();
    if(user!=null) {
      FirebaseAuth.getInstance().signOut();
    } else {
      Toast.makeText(ctx, "No user logged in.", Toast.LENGTH_SHORT).show();
    }
  }


  public static Bitmap getResizedBitmap(Bitmap image) {
    int width = image.getWidth();
    int height = image.getHeight();

    float bitmapRatio = (float)width / (float) height;
    if (bitmapRatio > 1) {
      width = MAX_IMAGE_SIZE;
      height = (int) (width / bitmapRatio);
    } else {
      height = MAX_IMAGE_SIZE;
      width = (int) (height * bitmapRatio);
    }
    return Bitmap.createScaledBitmap(image, width, height, true);
  }


  private Util () {

  }
}
