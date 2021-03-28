package com.stories.StoryList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.Stories.StoryList.R;
import com.squareup.picasso.Picasso;

public class StoryListAdapter extends RecyclerView.Adapter<StoryListAdapter.StoryListHolder> {

  String titles[], descriptions[];
  String images[];
  Context ctx;

  // public StoryListAdapter(Context ct, String s1[], String s2[], int img[]) {
  public StoryListAdapter(Context ct, String s1[], String s2[], String img[]) {
    this.ctx = ct;
    this.titles = s1;
    this.descriptions = s2;
    this.images = img;
  }
  @Override
  public StoryListAdapter.StoryListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    LayoutInflater layoutInflater = LayoutInflater.from(ctx);
    View storyItemView = layoutInflater.inflate(R.layout.story_item, parent, false);
    return new StoryListHolder(storyItemView);
  }

  @Override
  public void onBindViewHolder(StoryListAdapter.StoryListHolder holder, int position) {
    holder.setIsRecyclable(false);
    holder.title.setText(titles[position]);
    holder.description.setText(descriptions[position]);
    // holder.storyImage.setImageResource(images[position]);
    Picasso.get()
        .load(images[position]) //"https://www.tutorialspoint.com/images/tp-logo-diamond.png")
        // .centerCrop()
        .into(holder.storyImage);
  }

  @Override
  public int getItemCount() {
    return titles.length;
  }

  public class StoryListHolder extends RecyclerView.ViewHolder {

    TextView title, description;
    ImageView storyImage, likeButton;
    public StoryListHolder(View itemView) {
      super(itemView);
      title = (TextView) itemView.findViewById(R.id.StoryTitle);
      description = (TextView) itemView.findViewById(R.id.StoryMainBody);
      storyImage = (ImageView) itemView.findViewById(R.id.StoryImage);
      likeButton = (ImageView) itemView.findViewById(R.id.LikeButtonImage);
      setLikeClickListener(likeButton );
    }

    public void setLikeClickListener(ImageView likeButton) {
      likeButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          Log.i("Metadata", "Like button clicked.");
          Toast.makeText(ctx, "Like Button Clicked.", Toast.LENGTH_SHORT).show();
        }
      });
    }
  }
}
