package com.example.anyjob.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.anyjob.PostInfo;
import com.example.anyjob.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import static com.example.anyjob.Util.isStorageUrl;

public class PostActivity extends BasicActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        PostInfo postInfo = (PostInfo) getIntent().getSerializableExtra("postInfo");
        TextView titleTextView = findViewById(R.id.item_titleTextView);
        titleTextView.setText(postInfo.getTitle());

        TextView createdAtTextView = findViewById(R.id.item_createdAtTextView);
        createdAtTextView.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(postInfo.getcreatedAt()));

        LinearLayout textcontentsLayout = findViewById(R.id.item_textcontentsLayout);
        LinearLayout imagecontentsLayout = findViewById(R.id.item_imagecontentsLayout);
        ViewGroup.LayoutParams imagelayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ViewGroup.LayoutParams textlayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ArrayList<String> contentsList = postInfo.getDescription();


        if(textcontentsLayout.getTag() == null || !textcontentsLayout.getTag().equals(contentsList)){
            textcontentsLayout.setTag(contentsList);
            textcontentsLayout.removeAllViews();
            imagecontentsLayout.setTag(contentsList);
            imagecontentsLayout.removeAllViews();

            for(int i=0; i<contentsList.size(); i++){
                String contents = contentsList.get(i);
                if(isStorageUrl(contents)){
                    ImageView imageView = new ImageView(this);
                    imageView.setLayoutParams(imagelayoutParams);
                    imagecontentsLayout.addView(imageView);
                    Glide.with(this).load(contents).override(300).thumbnail(0.1f).into(imageView);
                }else {
                    TextView textView = new TextView(PostActivity.this);
                    textView.setLayoutParams(textlayoutParams);
                    textView.setText(contents);
                    textView.setTextColor(Color.rgb(0,0,0));
                    textView.setMaxLines(2);
                    textcontentsLayout.addView(textView);
                }
            }
        }

    }
}
