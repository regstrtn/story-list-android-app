package com.android.StoryList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.StoryList.util.Constants;
import com.android.StoryList.util.Util;
import com.example.StoryList.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.UploadTask.TaskSnapshot;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AddNewStory extends AppCompatActivity {
  Context ctx;
  private static final int PICK_IMAGE = 100;

  private TextView loggedInUserTextView;
  private Uri imageUri;
  private ImageView inputStoryImageView, addStoryButton;
  private TextView inputStoryTitleTextView, inputStoryMainBodyTextView;
  private ProgressBar progressBar;
  private FirebaseFirestore dbRef;
  private FirebaseStorage storage;
  private StorageReference storageRef;

  // Authentication fields.
  private FirebaseAuth firebaseAuth;
  private FirebaseUser loggedInUser;


  Bitmap bmp;
  ByteArrayOutputStream bos;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_new_story);
    dbRef = FirebaseFirestore.getInstance();
    storage = FirebaseStorage.getInstance();
    firebaseAuth = FirebaseAuth.getInstance();
    redirectIfUserNotLoggedIn();
    ctx = this;

    loggedInUserTextView = (TextView) findViewById(R.id.LoggedInUserName);
    inputStoryTitleTextView = (TextView) findViewById(R.id.InputStoryTitle);
    inputStoryMainBodyTextView = (TextView) findViewById(R.id.InputStoryMainBody);
    addStoryButton = (ImageView) findViewById(R.id.AddImageButton);
    Button postNewStoryButton = (Button) findViewById(R.id.PostNewStoryButton);
    progressBar = (ProgressBar) findViewById(R.id.progressBar);

    fetchLoggedInUserName();

    inputStoryImageView = (ImageView) findViewById(R.id.InputStoryImage);
    addStoryButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        openImageGallery(v);
      }
    });

    postNewStoryButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        postNewStory(v);
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.discard_current_action_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
      case(R.id.AbandonAction):
        goToFeedsActivity();
        break;
      default:
        super.onOptionsItemSelected(item);
    }
    return true;
  }

  public void openImageGallery(View v) {
    Intent gallery = new Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI);
    startActivityForResult(gallery, PICK_IMAGE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data){
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK && requestCode == PICK_IMAGE){
      imageUri = data.getData();
      try {
        InputStream input = this.getContentResolver().openInputStream(imageUri);

        bmp = BitmapFactory.decodeStream(input, null, new Options());
        bmp = Util.getResizedBitmap(bmp);
        bos = new ByteArrayOutputStream();
        bmp.compress(CompressFormat.JPEG, 70, bos);
        Log.i("ImageURI", String.valueOf(imageUri));
        inputStoryImageView.setImageBitmap(bmp);
        inputStoryImageView.setBackgroundColor(Color.GRAY);

      }
      catch (Exception e) {
        return;
      }
    }
  }


  public void postNewStory(View v) {
    if(!areInputsValid(inputStoryTitleTextView, inputStoryMainBodyTextView, inputStoryImageView)) {
      return;
    }

    progressBar.setVisibility(View.VISIBLE);
    String storyTitle = inputStoryTitleTextView.getText().toString();
    String storyMainBody = inputStoryMainBodyTextView.getText().toString();

    // String uploadedImageUrl = uploadImageAndGetUrl(imageUri);
    Map<String, Object> newStory = new HashMap<>();
    newStory.put(Constants.TITLE_FIELD, storyTitle);
    newStory.put(Constants.MAIN_BODY_FIELD, storyMainBody);
    newStory.put(Constants.IMAGE_URL_FIELD, "");
    newStory.put(Constants.STORY_AUTHOR_UID_FIELD, loggedInUser.getUid());
    newStory.put(Constants.STORY_AUTHOR_DISPLAY_NAME_FIELD, loggedInUser.getDisplayName());
    uploadImageAndText(newStory, imageUri);

    Log.i("DataToSend", newStory.toString());
  }

  private void uploadImageAndText(Map<String, Object> newStory, Uri imageUri) {
    final Uri[] uploadedImageUrl = new Uri[1];
    storageRef = storage.getReference();
    StorageReference imagesRef = storageRef.child("images/"+imageUri.getLastPathSegment());
    // UploadTask uploadTask = imagesRef.putFile(imageUri);
    UploadTask uploadTask = imagesRef.putBytes(bos.toByteArray());

    uploadTask.addOnSuccessListener(
        new OnSuccessListener<TaskSnapshot>() {
          @Override
          public void onSuccess(TaskSnapshot taskSnapshot) {
            imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
              @Override
              public void onSuccess(Uri uri) {
                uploadedImageUrl[0] = uri;
                newStory.put(Constants.IMAGE_URL_FIELD, uri.toString());
                uploadNewStoryData(newStory);
              }
            });
          }
        });
  }

  private void uploadNewStoryData(Map<String, Object> newStory) {
    dbRef.collection(Constants.COLLECTION_STORIES).document()
        .set(newStory)
        .addOnSuccessListener(new OnSuccessListener<Void>() {
      @Override
      public void onSuccess(Void unused) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(ctx, "Your story added successfully!", Toast.LENGTH_SHORT).show();
        Intent goToUserProfile = new Intent(ctx, UserProfile.class);
        startActivity(goToUserProfile);
      }
    }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(Exception e) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(ctx, "Story could not be added.", Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void goToFeedsActivity() {
    Intent goToFeedsPage = new Intent(ctx, MainActivity.class);
    startActivity(goToFeedsPage);
  }

  public void fetchLoggedInUserName() {
    loggedInUser = firebaseAuth.getCurrentUser();
    if(loggedInUser != null) {
      loggedInUserTextView.setText(loggedInUser.getDisplayName());
    }
  }

  private void redirectIfUserNotLoggedIn() {
    FirebaseUser user = firebaseAuth.getCurrentUser();
    if (user == null) {
      Intent openLoginPageIntent = new Intent(AddNewStory.this, UserLogin.class);
      openLoginPageIntent.putExtra(Constants.INTENT_MESSAGE, "You need to be logged in to post a story.");
      startActivity(openLoginPageIntent);
      finish();
    }
  }

  private static boolean areInputsValid(TextView inputStoryTitle, TextView inputStoryMainBody, ImageView inputStoryImageView) {
    if(inputStoryTitle.getText().toString().isEmpty()) {
      inputStoryTitle.setError("Title cannot be empty.");
      inputStoryTitle.requestFocus();
      return false;
    }
    if(inputStoryMainBody.getText().toString().isEmpty()) {
      inputStoryMainBody.setError("Description cannot be empty.");
      inputStoryMainBody.requestFocus();
      return false;
    }
    if (inputStoryImageView.getDrawable() == null) {
      return false;
    }
    return true;
  }
}
