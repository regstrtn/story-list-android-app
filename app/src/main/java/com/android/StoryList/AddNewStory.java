package com.android.StoryList;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.StoryList.util.Constants;
import com.example.StoryList.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
  private Uri imageUri;
  private ImageView inputStoryImageView;
  private TextView inputStoryTitle;
  private TextView inputStoryMainBody;
  private FirebaseFirestore dbRef;
  FirebaseStorage storage;
  private StorageReference storageRef;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_new_story);
    dbRef = FirebaseFirestore.getInstance();
    storage = FirebaseStorage.getInstance();
    ctx = this;

    inputStoryTitle = (TextView) findViewById(R.id.InputStoryTitle);
    inputStoryMainBody = (TextView) findViewById(R.id.InputStoryMainBody);
    Button postNewStoryButton = (Button) findViewById(R.id.PostNewStoryButton);
    postNewStoryButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        postNewStory(v);
      }
    });
    inputStoryImageView = (ImageView) findViewById(R.id.InputStoryImage);
    inputStoryImageView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        openImageGallery(v);
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.add_post_option_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
      case(R.id.DiscardPostButton):
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
    Toast.makeText(ctx,"Post Story Button clicked.", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(ctx, "Your story added successfully!", Toast.LENGTH_SHORT).show();
        Intent backToStoryFeed = new Intent(ctx, MainActivity.class);
        startActivity(backToStoryFeed);
      }
    }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(Exception e) {
        Toast.makeText(ctx, "Story could not be added.", Toast.LENGTH_SHORT).show();
      }
    });
  }

  public void goToFeedsActivity() {
    Intent goToFeedsPage = new Intent(ctx, MainActivity.class);
    startActivity(goToFeedsPage);
  }
}
