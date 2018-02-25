package com.nitharshanaan.android.movieindex2;

import android.net.Uri;

/**
 * Created by nitha on 15-01-2018.
 */

public class Moviedb {

    public static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185/";
    public static final String SORT_POPULARITY = "popular";
    public static final String SORT_PREF_KEY = "sort_list_pref";
    // Database:
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "moviedb.db";
    public static final String MOVIES_TABLE = "movies";
    public static final String TRAILERS_TABLE = "favorites";
    public static final String REVIEWS_TABLE = "reviews";
    // database columns:
    public static final String _ID_COLUMN = "_id";
    public static final String ID_COLUMN = "id";
    public static final String THUMB_COLUMN = "thumb";
    public static final String TITLE_COLUMN = "title";
    public static final String DESCRIPTION_COLUMN = "description";
    public static final String DATE_COLUMN = "date";
    public static final String VOTE_COLUMN = "vote";
    public static final String DURATION_COLUMN = "duration";
    public static final String FAVORITE_COLUMN = "favorite";
    public static final String KEY_COLUMN = "key";
    public static final String NAME_COLUMN = "name";
    public static final String AUTHOR_COLUMN = "author";
    public static final String CONTENT_COLUMN = "content";
    // Content Provider:
    public static final String CONTENT_AUTHORITY = "com.nitharshanaan.android.provider";
    public static final String CONTENT_URI = "content://" + CONTENT_AUTHORITY + "/";
    public static final int MOVIE_MATCH = 0, MOVIES_MATCH = 1, FAVORITES_MATCH = 2, TRAILERS_MATCH = 3, REVIEWS_MATCH = 4;
    // UriMatcher:
    public static final Uri MOVIES_URI = Uri.parse(CONTENT_URI + MOVIES_TABLE);
    public static final Uri TRAILERS_URI = Uri.parse(CONTENT_URI + TRAILERS_TABLE);
    public static final Uri REVIEWS_URI = Uri.parse(CONTENT_URI + REVIEWS_TABLE);
    private static final String API_KEY = BuildConfig.MOVIE_DB_API_KEY;
    public static String ID;
    public static String[] IDs, THUMBs;
    public static boolean TABLET_MODE = false;
    public static String TRAILER;

    public static String getMovieUrl() {
        return "http://api.themoviedb.org/3/movie/" + ID + "?api_key=" + API_KEY + "&append_to_response=trailers,reviews";
    }

    public static String getMoviesUrl(String sortBy) {
        return "http://api.themoviedb.org/3/movie/" + sortBy + "?api_key=" + API_KEY;

    }
}
