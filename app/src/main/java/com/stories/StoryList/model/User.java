package com.stories.StoryList.model;

import android.net.Uri;

public class User {
  public String displayName;
  public Uri photoUri;
  public User() {

  }

  public User (String displayName) {
    this.displayName = displayName;
  }

  public User (String displayName, Uri photoUri) {
    this.displayName = displayName;
    this.photoUri = photoUri;
  }

}
