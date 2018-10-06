package com.example.yisen614.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import static android.support.constraint.Constraints.TAG;

public class PhotoGalleryFragment extends Fragment {

    private RecyclerView mRecyclerView;

    private List<Image> mItems = new ArrayList<>();

    private Downloader<PhotoHolder> downloader;

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
        Handler responseHandler = new Handler();

        downloader = new Downloader<>(responseHandler);
        downloader.setDownloadListener(new Downloader.DownloadListener<PhotoHolder>() {
            @Override
            public void onDownloaded(PhotoHolder target, Bitmap bitmap) {
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                target.bindDrawable(drawable);
            }
        });
        downloader.start();
        downloader.getLooper();
        Log.i("Downloader: ", "background work start");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        downloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        downloader.quit();
        Log.i("Downloader: ", "background work destroy");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        return view;
    }


    private void setupAdapter() {
        if (isAdded()) {
            mRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{

        private List<Image> items;

        public PhotoAdapter(List<Image> items) {
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
            Drawable placeholder = getResources().getDrawable(R.drawable.placeholder_image);
            photoHolder.bindDrawable(placeholder);
            downloader.queueDownload(photoHolder, item.getPic_url());
            photoHolder.bindGalleryItem(item);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }


    public class PhotoHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        private TextView desView;

        public PhotoHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            desView = itemView.findViewById(R.id.des_view);
        }

        public void bindGalleryItem(Image item) {
            desView.setText(item.getPic_url());
        }

        public void bindDrawable(Drawable drawable) {
            imageView.setImageDrawable(drawable);
        }
    }

    private class FetchItemsTask extends AsyncTask<String, Integer, List<Image>> {

        List<Image> items = new ArrayList<>();

        private String jsonString;

        @Override
        protected List<Image> doInBackground(String... params) {
            try {
                jsonString = new FlickrFetchr().getUrlString("https://pic.sogou.com/pics/channel/getAllRecomPicByTag.jsp?category=%E7%BE%8E%E5%A5%B3&tag=%E6%96%87%E8%89%BA&start=0&len=100");
                Log.i(TAG, "Fetched contents of URL: " + jsonString);
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to fetch URL: ", ioe);
            }
            return fetchItems();
        }

        @Override
        protected void onPostExecute(List<Image> items) {
            mItems = items;
            setupAdapter();
        }


        public List<Image> fetchItems() {
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
                items.add(item);
                Log.i("Image", item.toString());
            }
        }
    }
}