package com.example.yisen614.photogallery;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.support.constraint.Constraints.TAG;

public class PhotoGalleryFragment extends Fragment {

    private RecyclerView mRecyclerView;

    private List<Image> mItems = new ArrayList<>();

    public PhotoGalleryFragment() {
    }

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }


    private void setupAdapter() {
        if (isAdded()) {
            mRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<Image> items;

        PhotoAdapter(List<Image> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.recycler_view_item, viewGroup, false);
            return new PhotoHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder photoHolder, int i) {
            Image item = items.get(i);
            photoHolder.bindGalleryItem(item);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }


    class PhotoHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        private CardView cardView;

        private TextView desView;

        @SuppressLint("ClickableViewAccessibility")
        PhotoHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            desView = itemView.findViewById(R.id.des_view);
            cardView = itemView.findViewById(R.id.cards);

            cardView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            return handleOnTouchStart(event);
                        case MotionEvent.ACTION_MOVE:
                            return handleOnMove(event);
                        case MotionEvent.ACTION_UP:
                            return handleOnTouchEnd(event);
                        default:
                            return handleDefault(event);
                    }
                }
            });
        }

        void bindGalleryItem(Image item) {
            Glide.with(Objects.requireNonNull(getActivity())).load(item.getPic_url()).into(imageView);
            desView.setText(item.getTags());
        }

        private Boolean handleOnTouchStart(MotionEvent motionEvent) {
            cardView.animate().scaleX(0.95f).scaleY(0.95f).translationZ(0F).setDuration(100);
            return true;
        }

        private Boolean handleOnTouchEnd(MotionEvent motionEvent) {
            cardView.animate().scaleX(1F).scaleY(1F).translationZ(20F).setDuration(200);
            return true;
        }

        private Boolean handleOnMove(MotionEvent motionEvent) {
            return true;
        }

        private Boolean handleDefault(MotionEvent motionEvent) {
            cardView.animate().scaleX(0.95f).scaleY(0.95f).translationZ(0F).setDuration(100);
            cardView.animate().scaleX(1F).scaleY(1F).translationZ(20F).setDuration(200);
            return true;
        }

    }

    private class FetchItemsTask extends AsyncTask<String, Integer, List<Image>> {

        List<Image> items = new ArrayList<>();

        private String jsonString;

        @Override
        protected List<Image> doInBackground(String... params) {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://pic.sogou.com/pics/channel/getAllRecomPicByTag.jsp?category=%E7%BE%8E%E5%A5%B3&tag=%E6%96%87%E8%89%BA&start=0&len=1000")
                        .build();

                Response response = client.newCall(request).execute();

                jsonString = response.body().string();

                Log.i(TAG, "Fetched contents of URL: " + jsonString);
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to fetch URL: ", ioe);
            }
            return fetchItems();
        }

        @Override
        protected void onPostExecute(List<Image> items) {
            mItems.addAll(items);
            setupAdapter();
        }

        List<Image> fetchItems() {
            try {
                Log.i(TAG, "Received JSON: " + jsonString);
                JSONObject jsonBody = new JSONObject(jsonString);
                parseItems(items, jsonBody);
            } catch (JSONException je) {
                Log.e(TAG, "Failed to parse JSON", je);
            }
            return items;
        }

        private void parseItems(List<Image> items, JSONObject jsonBody) throws JSONException {

            JSONArray photoJsonArray = jsonBody.getJSONArray("all_items");

            for (int i = 0; i < photoJsonArray.length(); i++) {
                JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
                Image item = new Image();
                item.setId(photoJsonObject.getInt("id"));
                item.setTitle(photoJsonObject.getString("title"));
                item.setPic_url(photoJsonObject.getString("pic_url"));
                item.setPage_url(photoJsonObject.getString("page_url"));
                JSONArray array = photoJsonObject.getJSONArray("tags");
                item.setTags(array.toString());
                items.add(item);
                Log.i("image:", item.getPic_url());
            }
        }
    }
}