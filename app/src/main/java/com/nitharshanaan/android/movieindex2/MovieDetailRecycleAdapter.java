package com.nitharshanaan.android.movieindex2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;


public class MovieDetailRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private static Context context;
    private static boolean isFavorite;
    private final int TYPE_DETAIL = 0, TYPE_TRAILER = 1, TYPE_REVIEW = 2, TYPE_TRAILER_DIVIDER = 3, TYPE_REVIEW_DIVIDER = 4;
    //private String poster, title, description, date, vote, duration;
    private Map<String, String> details = new HashMap<>();
    private String[] keys, names; // trailers
    private String[] authors, contents; // reviews

    public MovieDetailRecycleAdapter(Context c, Map details, String[] keys, String[] names, String[] authors, String[] contents, boolean isFavorite) {
        context = c;
        this.details = details;
        this.keys = keys;
        this.names = names;
        this.authors = authors;
        this.contents = contents;
        this.isFavorite = isFavorite;
    }

    private void saveMovieInFavorites() {
        // if movie is saved before, don't save again
        if (isFavorite) {
            context.getContentResolver().delete(Moviedb.MOVIES_URI.buildUpon().appendPath(Moviedb.ID).build(), null, null);
            isFavorite = false;
            DetailHolder.fab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_border_white_48dp));
            Toast.makeText(context, "removed successfully from favorites", Toast.LENGTH_SHORT).show();
            return;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(Moviedb.ID_COLUMN, Moviedb.ID);
        contentValues.put(Moviedb.THUMB_COLUMN, details.get(Moviedb.THUMB_COLUMN));
        contentValues.put(Moviedb.TITLE_COLUMN, details.get(Moviedb.TITLE_COLUMN));
        contentValues.put(Moviedb.DESCRIPTION_COLUMN, details.get(Moviedb.DESCRIPTION_COLUMN));
        contentValues.put(Moviedb.DATE_COLUMN, details.get(Moviedb.DATE_COLUMN));
        contentValues.put(Moviedb.VOTE_COLUMN, details.get(Moviedb.VOTE_COLUMN));
        contentValues.put(Moviedb.DURATION_COLUMN, details.get(Moviedb.DURATION_COLUMN));
        context.getContentResolver().insert(Moviedb.MOVIES_URI, contentValues);
        contentValues.clear();
        for (int i = 0; i < keys.length; i++) {
            contentValues.put(Moviedb.ID_COLUMN, Moviedb.ID);
            contentValues.put(Moviedb.KEY_COLUMN, keys[i]);
            contentValues.put(Moviedb.NAME_COLUMN, names[i]);
            context.getContentResolver().insert(Moviedb.TRAILERS_URI.buildUpon().appendPath(Moviedb.ID).build(), contentValues);
            contentValues.clear();
        }
        for (int i = 0; i < authors.length; i++) {
            contentValues.put(Moviedb.ID_COLUMN, Moviedb.ID);
            contentValues.put(Moviedb.AUTHOR_COLUMN, authors[i]);
            contentValues.put(Moviedb.CONTENT_COLUMN, contents[i]);
            context.getContentResolver().insert(Moviedb.REVIEWS_URI.buildUpon().appendPath(Moviedb.ID).build(), contentValues);
            contentValues.clear();
        }
        isFavorite = true;
        DetailHolder.fab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_white_48dp));
        Toast.makeText(context, "saved successfully to favorites", Toast.LENGTH_SHORT).show();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view;
        switch (viewType) {
            case TYPE_DETAIL: {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.movie_detail_item, parent, false);
                return new DetailHolder(view);
            }
            case TYPE_TRAILER: {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.movie_trailer_item, parent, false);
                return new TrailerHolder(view);
            }
            case TYPE_REVIEW: {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.movie_review_item, parent, false);
                return new ReviewHolder(view);
            }
            case TYPE_TRAILER_DIVIDER: {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.movie_trailer_divider, parent, false);
                return new TrailerDivider(view);
            }
            case TYPE_REVIEW_DIVIDER: {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.movie_review_divider, parent, false);
                return new ReviewDivider(view);
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()) {
            case TYPE_DETAIL: {
                ((DetailHolder) holder).titleTxt.setText(details.get(Moviedb.TITLE_COLUMN));
                ((DetailHolder) holder).descriptionTxt.setText(details.get(Moviedb.DESCRIPTION_COLUMN));
                ((DetailHolder) holder).durationTxt.setText(details.get(Moviedb.DURATION_COLUMN) + " minutes.");
                ((DetailHolder) holder).dateTxt.setText(details.get(Moviedb.DATE_COLUMN));
                ((DetailHolder) holder).rateBar.setRating(Float.parseFloat(details.get(Moviedb.VOTE_COLUMN)) / 2);
                Picasso.with(context)
                        .load(Moviedb.IMAGE_BASE_URL + details.get(Moviedb.THUMB_COLUMN))
                        //.placeholder(R.drawable.ic_cached_black_48dp)
                        //.resize(400, 600).centerCrop()
                        .tag(context)
                        .into(((DetailHolder) holder).posterImg);
                // favorite FAB listener:
                ((DetailHolder) holder).fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        saveMovieInFavorites();
                    }
                });
                break;
            }
            case TYPE_TRAILER: {
                ((TrailerHolder) holder).trailerTxt.setText(names[position - 2]);
                ((TrailerHolder) holder).key = keys[position - 2];
                break;
            }
            case TYPE_REVIEW: {
                ((ReviewHolder) holder).authorTxt.setText(authors[position - 3 - keys.length]);
                ((ReviewHolder) holder).contentTxt.setText(contents[position - 3 - keys.length]);
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return 1 + keys.length + authors.length + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return TYPE_DETAIL;
        if (position == 1)
            return TYPE_TRAILER_DIVIDER;
        if (position > 1 && position <= keys.length + 1)
            return TYPE_TRAILER;
        if (position == keys.length + 2)
            return TYPE_REVIEW_DIVIDER;
        else return TYPE_REVIEW;

    }

    private static class DetailHolder extends RecyclerView.ViewHolder {

        private static FloatingActionButton fab;
        private TextView titleTxt, descriptionTxt, dateTxt, durationTxt;
        private RatingBar rateBar;
        private ImageView posterImg;

        public DetailHolder(View v) {
            super(v);
            posterImg = (ImageView) v.findViewById(R.id.movie_image);
            titleTxt = (TextView) v.findViewById(R.id.movie_name);
            descriptionTxt = (TextView) v.findViewById(R.id.movie_description);
            dateTxt = (TextView) v.findViewById(R.id.movie_year);
            rateBar = (RatingBar) v.findViewById(R.id.movie_rate);
            durationTxt = (TextView) v.findViewById(R.id.movie_duration);
            fab = (FloatingActionButton) v.findViewById(R.id.favorite_fab);
            if (isFavorite) {
                fab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_white_48dp));
            }

        }
    }

    private static class TrailerHolder extends RecyclerView.ViewHolder {

        private TextView trailerTxt;
        private String key;

        public TrailerHolder(View v) {
            super(v);
            trailerTxt = (TextView) v.findViewById(R.id.trailer);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + key));
                    context.startActivity(intent);
                }
            });
        }
    }

    private static class ReviewHolder extends RecyclerView.ViewHolder {

        private TextView authorTxt, contentTxt;

        public ReviewHolder(View v) {
            super(v);
            authorTxt = (TextView) v.findViewById(R.id.review_author);
            contentTxt = (TextView) v.findViewById(R.id.review_content);

        }
    }

    private static class TrailerDivider extends RecyclerView.ViewHolder {

        public TrailerDivider(View v) {
            super(v);
        }
    }

    private static class ReviewDivider extends RecyclerView.ViewHolder {

        public ReviewDivider(View v) {
            super(v);
        }
    }
/*
    private class ListDivider extends RecyclerView.ItemDecoration{

        View view = LayoutInflater.from(context).inflate(R.layout.movie_review_divider, null);

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            // this method only make a space available to draw the divider on it later in the right position
            if(parent.getChildAdapterPosition(view) == keys.length+1)
                outRect.set(0,this.view.getMeasuredHeight(),0,0);
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            //draw the divider itself in the room we get from above method, we must go to its location first
            view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.setPadding(0,parent.getChildAt(0).getMeasuredHeight(),0,0);
            view.draw(c);
            //c.drawColor(context.getResources().getColor(R.color.Blue));
        }
    }

    public ListDivider createListDividerInstance(){
        return new ListDivider();
    }*/
}
