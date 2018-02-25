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
import android.support.v7.widget.GridLayoutManager;
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

/**
 * Created by nitha on 10-01-2018.
 */

public class MoviesFragment extends Fragment {

    private RecyclerView gridView;
    private MoviesRecycleAdapter moviesRecycleAdapter;
    private Context context;
    private MoviesTask moviesTask;
    private RecyclerView.LayoutManager layoutManager;
    private String sortBy;

    public MoviesFragment() {
    }

    public void setSort(String s) {
        sortBy = s;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_movies, container, false);
        context = getContext();
        gridView = (RecyclerView) fragmentView.findViewById(R.id.movies_grid);

        if (sortBy.equals("FAVORITES")) {
            String[] projection = {Moviedb.ID_COLUMN, Moviedb.THUMB_COLUMN};
            Cursor cursor = getContext().getContentResolver().query(Moviedb.MOVIES_URI, projection, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                Moviedb.IDs = new String[cursor.getCount()];
                Moviedb.THUMBs = new String[cursor.getCount()];
                for (int i = 0; i < cursor.getCount(); i++) {
                    Moviedb.IDs[i] = cursor.getString(cursor.getColumnIndex(Moviedb.ID_COLUMN));
                    Moviedb.THUMBs[i] = cursor.getString(cursor.getColumnIndex(Moviedb.THUMB_COLUMN));
                    cursor.moveToNext();
                }
                initializeGridAdapter();
            }
        } else {
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                moviesTask = new MoviesTask();
                moviesTask.execute(sortBy);
            }
            //if there is no internet connection
            else Toast.makeText(context, "No Network Connection", Toast.LENGTH_SHORT).show();
        }
        return fragmentView;
    }

    private void initializeGridAdapter() {
        moviesRecycleAdapter = new MoviesRecycleAdapter(context, getActivity().getSupportFragmentManager());
        gridView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(context, 2);
        gridView.setLayoutManager(layoutManager);
        //inflate grid from adapter:
        gridView.setAdapter(moviesRecycleAdapter);
    }

    private class MoviesTask extends AsyncTask<String, Void, String> {

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
        protected String doInBackground(String... params) {

            try {
                // params is to know how to sort data
                Uri uri = Uri.parse(Moviedb.getMoviesUrl(params[0]));
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
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                Moviedb.IDs = new String[jsonArray.length()];
                Moviedb.THUMBs = new String[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    Moviedb.IDs[i] = jsonArray.getJSONObject(i).getString("id");
                    Moviedb.THUMBs[i] = jsonArray.getJSONObject(i).getString("poster_path");
                }

                initializeGridAdapter();
                progressDialog.dismiss();

            } catch (JSONException e) {
            }

        }
    }
}
