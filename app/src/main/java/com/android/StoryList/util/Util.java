package com.android.StoryList.util;

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
  private Util () {

  }
}
