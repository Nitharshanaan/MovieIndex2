package com.nitharshanaan.android.movieindex2;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by nitha on 16-01-2018.
 */

public class MoviesRecycleAdapter extends RecyclerView.Adapter<MoviesRecycleAdapter.ViewHolder> {

    private static Context context;
    private static FragmentManager fm;

    // Provide a suitable constructor (depends on the kind of dataset)
    public MoviesRecycleAdapter(Context c, FragmentManager f) {
        context = c;
        fm = f;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MoviesRecycleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_thumb_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Picasso.with(context)
                .load(Moviedb.IMAGE_BASE_URL + Moviedb.THUMBs[position])
                //.placeholder(R.drawable.ic_cached_black_48dp)
                //.resize(400, 600).centerCrop()
                .tag(context)
                .into(holder.imageView);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return Moviedb.THUMBs.length;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private ImageView imageView;

        public ViewHolder(View v) {
            super(v);
            imageView = v.findViewById(R.id.movie_thumb_item);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Check if phone or tablet:
                    Moviedb.ID = Moviedb.IDs[getAdapterPosition()];
                    if (Moviedb.TABLET_MODE) {
                        //fragment transaction for tablet
                        DetailFragment detailFragment = new DetailFragment();
                        fm.beginTransaction().replace(R.id.fragment_tablet, detailFragment).commit();

                    } else {
                        Intent intent = new Intent(context, DetailActivity.class);
                        context.startActivity(intent);
                    }

                }
            });
        }
    }
}