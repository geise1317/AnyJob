package com.example.anyjob.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.anyjob.PostInfo;
import com.example.anyjob.R;
import com.example.anyjob.activity.PostActivity;
import com.example.anyjob.listener.OnPostListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import static com.example.anyjob.Util.isStorageUrl;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {
    private ArrayList<PostInfo> mDataset;
    private Activity activity;
    private OnPostListener onPostListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one com.example.anyjob.view per item, and
    // you provide access to all the views for a data item in a com.example.anyjob.view holder
    static class MainViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        MainViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MainAdapter(Activity activity, ArrayList<PostInfo> myDataset) {
        this.mDataset = myDataset;
        this.activity = activity;
    }

    public void setOnPostListener(OnPostListener onPostListener){
        this.onPostListener = onPostListener;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public MainAdapter.MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new com.example.anyjob.view
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        final MainViewHolder mainViewHolder = new MainViewHolder(cardView);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, PostActivity.class);
                intent.putExtra("postInfo", mDataset.get(mainViewHolder.getAdapterPosition()));
                activity.startActivity(intent);
            }
        });

        cardView.findViewById(R.id.post_menu).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showPopup(v, mainViewHolder.getAdapterPosition());
            }
        });

        return mainViewHolder;
    }

    // Replace the contents of a com.example.anyjob.view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final MainViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        TextView titleTextView = cardView.findViewById(R.id.item_titleTextView);
        titleTextView.setText(mDataset.get(position).getTitle());

        TextView createdAtTextView = cardView.findViewById(R.id.item_createdAtTextView);
        createdAtTextView.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(mDataset.get(position).getcreatedAt()));

        LinearLayout textcontentsLayout = cardView.findViewById(R.id.item_textcontentsLayout);
        LinearLayout imagecontentsLayout = cardView.findViewById(R.id.item_imagecontentsLayout);
        ViewGroup.LayoutParams imagelayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ViewGroup.LayoutParams textlayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ArrayList<String> contentsList = mDataset.get(position).getDescription();

        if(textcontentsLayout.getTag() == null || !textcontentsLayout.getTag().equals(contentsList)){
            textcontentsLayout.removeAllViews();
            imagecontentsLayout.removeAllViews();
            final int MAX_ITEM_NUM = 2;
            for(int i=0; i<contentsList.size(); i++){
                if(i==MAX_ITEM_NUM){

                    break;
                }
                String contents = contentsList.get(i);
                if(isStorageUrl(contents)){
                    ImageView imageView = new ImageView(activity);
                    imageView.setLayoutParams(imagelayoutParams);
                    imagecontentsLayout.addView(imageView);
                    Glide.with(activity).load(contents).override(300).thumbnail(0.1f).into(imageView);
                }else {
                    TextView textView = new TextView(activity);
                    textView.setLayoutParams(textlayoutParams);
                    textView.setText(contents);
                    textView.setTextColor(Color.rgb(0,0,0));
                    textView.setMaxLines(2);
                    textcontentsLayout.addView(textView);
                }
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    private void showPopup(View v, final int position) {
        PopupMenu popup = new PopupMenu(activity, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_post_edit:
                        onPostListener.onEdit(position);
                        return true;
                    case R.id.menu_post_delete:
                        onPostListener.onDelete(position);
                        return true;
                    default:
                        return false;
                }
            }
        });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.post, popup.getMenu());

        popup.show();
    }

    private void startToast(String msg){
        Toast.makeText(activity, msg,
                Toast.LENGTH_SHORT).show();
    }
}
