package com.nitharshanaan.android.movieindex2;


import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {

    private RecyclerView listView;
    private Context context;
    private MovieDetailTask movieDetailTask;
    private MovieDetailRecycleAdapter movieDetailRecycleAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private Map<String, String> details = new HashMap<>();
    private String[] keys, names; // trailers
    private String[] authors, contents; // reviews
    private boolean isFavorite = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_detail, container, false);
        context = getContext();
        listView = fragmentView.findViewById(R.id.detail_list);

        // check if movie is in favorites to get it from DB or fetch from internet:
        Cursor cursor = context.getContentResolver().query(Moviedb.MOVIES_URI.buildUpon().appendPath(Moviedb.ID).build(), null, null, null, null);
        if (cursor.getCount() > 0) {
            //movie is saved
            isFavorite = true;
            cursor.moveToFirst();
            // get basic info:
            details.put(Moviedb.THUMB_COLUMN, cursor.getString(cursor.getColumnIndex(Moviedb.THUMB_COLUMN)));
            details.put(Moviedb.TITLE_COLUMN, cursor.getString(cursor.getColumnIndex(Moviedb.TITLE_COLUMN)));
            details.put(Moviedb.DESCRIPTION_COLUMN, cursor.getString(cursor.getColumnIndex(Moviedb.DESCRIPTION_COLUMN)));
            details.put(Moviedb.DATE_COLUMN, cursor.getString(cursor.getColumnIndex(Moviedb.DATE_COLUMN)));
            details.put(Moviedb.VOTE_COLUMN, cursor.getString(cursor.getColumnIndex(Moviedb.VOTE_COLUMN)));
            details.put(Moviedb.DURATION_COLUMN, cursor.getString(cursor.getColumnIndex(Moviedb.DURATION_COLUMN)));
            cursor.close();
            // get trailers:
            cursor = context.getContentResolver().query(Moviedb.TRAILERS_URI.buildUpon().appendPath(Moviedb.ID).build(), null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                keys = new String[cursor.getCount()];
                names = new String[cursor.getCount()];
                for (int i = 0; i < cursor.getCount(); i++) {
                    keys[i] = cursor.getString(cursor.getColumnIndex(Moviedb.KEY_COLUMN));
                    names[i] = cursor.getString(cursor.getColumnIndex(Moviedb.NAME_COLUMN));
                    cursor.moveToNext();
                }
                Moviedb.TRAILER = keys[0];
                cursor.close();
            }
            // get reviews:
            cursor = context.getContentResolver().query(Moviedb.REVIEWS_URI.buildUpon().appendPath(Moviedb.ID).build(), null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                authors = new String[cursor.getCount()];
                contents = new String[cursor.getCount()];
                for (int i = 0; i < cursor.getCount(); i++) {
                    authors[i] = cursor.getString(cursor.getColumnIndex(Moviedb.AUTHOR_COLUMN));
                    contents[i] = cursor.getString(cursor.getColumnIndex(Moviedb.CONTENT_COLUMN));
                    cursor.moveToNext();
                }
                cursor.close();
            }
            callRecycleAdapter();
        } else {

            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                movieDetailTask = new MovieDetailTask();
                movieDetailTask.execute();
            }
            //if there is no internet connection
            else Toast.makeText(context, "No Network Connection", Toast.LENGTH_SHORT).show();

        }
        return fragmentView;
    }

    void callRecycleAdapter() {

        movieDetailRecycleAdapter = new MovieDetailRecycleAdapter(context, details, keys, names, authors, contents, isFavorite);
        layoutManager = new LinearLayoutManager(context);
        listView.setHasFixedSize(true);
        listView.setLayoutManager(layoutManager);
        //listView.addItemDecoration(movieDetailRecycleAdapter.createListDividerInstance());
        listView.setAdapter(movieDetailRecycleAdapter);
    }

    private class MovieDetailTask extends AsyncTask<Void, Void, String> {

        private HttpURLConnection httpURLConnection;
        private String strJSON;
        private BufferedReader reader;
        private ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {

            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Please Wait...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {

            try {
                //http://api.themoviedb.org/3/movie/209112?api_key=4b3e9ea2eb637b5831c06e63bac47120&append_to_response=trailers,reviews
                Uri uri = Uri.parse(Moviedb.getMovieUrl());
                URL url = new URL(uri.toString());
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = httpURLConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                strJSON = buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return strJSON;
        }

        @Override
        protected void onPostExecute(String result) {

            try {
                //Read JSON file:
                JSONObject jsonObject = new JSONObject(result);
                //basic info:
                details.put(Moviedb.THUMB_COLUMN, jsonObject.getString("poster_path"));
                details.put(Moviedb.TITLE_COLUMN, jsonObject.getString("original_title"));
                details.put(Moviedb.DESCRIPTION_COLUMN, jsonObject.getString("overview"));
                details.put(Moviedb.DATE_COLUMN, jsonObject.getString("release_date").substring(0, 4));
                details.put(Moviedb.VOTE_COLUMN, jsonObject.getString("vote_average"));
                details.put(Moviedb.DURATION_COLUMN, jsonObject.getString("runtime"));
                //trailers:
                JSONArray trailers = jsonObject.getJSONObject("trailers").getJSONArray("youtube");
                if (trailers.length() > 0) {
                    keys = new String[trailers.length()];
                    names = new String[trailers.length()];
                    for (int i = 0; i < trailers.length(); i += 1) {
                        keys[i] = trailers.getJSONObject(i).getString("source");
                        names[i] = trailers.getJSONObject(i).getString("name");
                    }
                    Moviedb.TRAILER = keys[0];
                } else {
                    keys = new String[1];
                    names = new String[1];
                    keys[0] = "No Trailer";
                    names[0] = "No Trailer";
                }

                //reviews:
                JSONArray reviews = jsonObject.getJSONObject("reviews").getJSONArray("results");
                if (reviews.length() > 0) {
                    authors = new String[reviews.length()];
                    contents = new String[reviews.length()];
                    for (int i = 0; i < reviews.length(); i += 1) {
                        authors[i] = reviews.getJSONObject(i).getString("author");
                        contents[i] = reviews.getJSONObject(i).getString("content");
                    }
                } else {
                    authors = new String[1];
                    contents = new String[1];
                    authors[0] = "No Reviews";
                    contents[0] = "No Reviews";
                }


            } catch (JSONException e) {
            }

            callRecycleAdapter();
            progressDialog.dismiss();
        }
    }
}
