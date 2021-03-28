package com.android.StoryList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import java.util.HashMap;
import java.util.Map;

public class AddNewStory extends AppCompatActivity {
  Context ctx;
  private static final int PICK_IMAGE = 100;
  private static final String TITLE_FIELD = "Title";
  private static final String MAIN_BODY_FIELD = "MainBody";
  private static final String IMAGE_URL_FIELD = "ImageUrl";
  private TextView loggedInUserName;
  private Uri imageUri;
  private ImageView inputStoryImageView, addStoryButton;
  private TextView inputStoryTitle, inputStoryMainBody;
  private ProgressBar progressBar;
  private FirebaseFirestore dbRef;
  private FirebaseStorage storage;
  private StorageReference storageRef;

  // Authentication fields.
  private FirebaseAuth firebaseAuth;
  FirebaseUser loggedInUser;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_new_story);
    dbRef = FirebaseFirestore.getInstance();
    storage = FirebaseStorage.getInstance();
    firebaseAuth = FirebaseAuth.getInstance();
    redirectIfUserNotLoggedIn();
    ctx = this;

    loggedInUserName = (TextView) findViewById(R.id.LoggedInUserName);
    inputStoryTitle = (TextView) findViewById(R.id.InputStoryTitle);
    inputStoryMainBody = (TextView) findViewById(R.id.InputStoryMainBody);
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
      Log.i("ImageURI", String.valueOf(imageUri));
      inputStoryImageView.setImageURI(imageUri);
    }
  }


  public void postNewStory(View v) {
    progressBar.setVisibility(View.VISIBLE);
    String storyTitle = inputStoryTitle.getText().toString();
    String storyMainBody = inputStoryMainBody.getText().toString();

    // String uploadedImageUrl = uploadImageAndGetUrl(imageUri);
    Map<String, Object> newStory = new HashMap<>();
    newStory.put(TITLE_FIELD, storyTitle);
    newStory.put(MAIN_BODY_FIELD, storyMainBody);
    newStory.put(IMAGE_URL_FIELD, "");
    uploadImageAndText(newStory, imageUri);

    Log.i("DataToSend", newStory.toString());
  }

  private void uploadImageAndText(Map<String, Object> newStory, Uri imageUri) {
    final Uri[] uploadedImageUrl = new Uri[1];
    storageRef = storage.getReference();
    StorageReference imagesRef = storageRef.child("images/"+imageUri.getLastPathSegment());
    UploadTask uploadTask = imagesRef.putFile(imageUri);

    uploadTask.addOnSuccessListener(
        new OnSuccessListener<TaskSnapshot>() {
          @Override
          public void onSuccess(TaskSnapshot taskSnapshot) {
            imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
              @Override
              public void onSuccess(Uri uri) {
                uploadedImageUrl[0] = uri;
                newStory.put(IMAGE_URL_FIELD, uri.toString());
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
        Intent backToStoryFeed = new Intent(ctx, MainActivity.class);
        startActivity(backToStoryFeed);
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
      loggedInUserName.setText(loggedInUser.getDisplayName());
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
}
