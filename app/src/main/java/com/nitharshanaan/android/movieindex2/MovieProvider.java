package com.nitharshanaan.android.movieindex2;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by nitha on 20-01-2018.
 */

public class MovieProvider extends ContentProvider {

    // Creates a UriMatcher object.
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(Moviedb.CONTENT_AUTHORITY, Moviedb.MOVIES_TABLE, Moviedb.MOVIES_MATCH);
        uriMatcher.addURI(Moviedb.CONTENT_AUTHORITY, Moviedb.MOVIES_TABLE + "/#", Moviedb.MOVIE_MATCH);
        //uriMatcher.addURI(Moviedb.CONTENT_AUTHORITY, Moviedb.MOVIES_TABLE+"/*", Moviedb.FAVORITES_MATCH);
        uriMatcher.addURI(Moviedb.CONTENT_AUTHORITY, Moviedb.TRAILERS_TABLE + "/#", Moviedb.TRAILERS_MATCH);
        uriMatcher.addURI(Moviedb.CONTENT_AUTHORITY, Moviedb.REVIEWS_TABLE + "/#", Moviedb.REVIEWS_MATCH);
    }

    private MovieDB dbHelper; //database helper object
    private SQLiteDatabase db; //database object

    @Override
    public boolean onCreate() {
        dbHelper = new MovieDB(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case Moviedb.MOVIE_MATCH:
                db.delete(Moviedb.MOVIES_TABLE, Moviedb.ID_COLUMN + " == " + uri.getLastPathSegment(), null);
                db.delete(Moviedb.TRAILERS_TABLE, Moviedb.ID_COLUMN + " == " + uri.getLastPathSegment(), null);
                db.delete(Moviedb.REVIEWS_TABLE, Moviedb.ID_COLUMN + " == " + uri.getLastPathSegment(), null);
                getContext().getContentResolver().notifyChange(uri, null);
                return 1;
            default:
                throw new UnsupportedOperationException("URI not matched!");
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        db = dbHelper.getWritableDatabase();
        Uri newUri = null;
        switch (uriMatcher.match(uri)) {
            case Moviedb.MOVIES_MATCH: {
                long id = db.insert(Moviedb.MOVIES_TABLE, null, contentValues);
                if (id > 0) {
                    newUri = ContentUris.withAppendedId(Moviedb.MOVIES_URI, id);
                    getContext().getContentResolver().notifyChange(newUri, null);
                    break;
                }
            }
            case Moviedb.TRAILERS_MATCH: {
                long id = db.insert(Moviedb.TRAILERS_TABLE, null, contentValues);
                if (id > 0) {
                    newUri = ContentUris.withAppendedId(Moviedb.TRAILERS_URI, id);
                    getContext().getContentResolver().notifyChange(newUri, null);
                    break;
                }
            }
            case Moviedb.REVIEWS_MATCH: {
                long id = db.insert(Moviedb.REVIEWS_TABLE, null, contentValues);
                if (id > 0) {
                    newUri = ContentUris.withAppendedId(Moviedb.REVIEWS_URI, id);
                    getContext().getContentResolver().notifyChange(newUri, null);
                    break;
                }
            }
            default:
                throw new UnsupportedOperationException("URI not matched!");
        }
        db.close();
        return newUri;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        db = dbHelper.getReadableDatabase();
        Cursor newCursor = null;
        switch (uriMatcher.match(uri)) {
            case Moviedb.MOVIES_MATCH: {
                newCursor = db.query(Moviedb.MOVIES_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                newCursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            }
            case Moviedb.MOVIE_MATCH: {
                newCursor = db.query(Moviedb.MOVIES_TABLE, projection, Moviedb.ID_COLUMN + " == " + uri.getLastPathSegment(), selectionArgs, null, null, sortOrder);
                newCursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            }
            case Moviedb.TRAILERS_MATCH: {
                newCursor = db.query(Moviedb.TRAILERS_TABLE, projection, Moviedb.ID_COLUMN + " == " + uri.getLastPathSegment(), selectionArgs, null, null, sortOrder);
                newCursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            }
            case Moviedb.REVIEWS_MATCH: {
                newCursor = db.query(Moviedb.REVIEWS_TABLE, projection, Moviedb.ID_COLUMN + " == " + uri.getLastPathSegment(), selectionArgs, null, null, sortOrder);
                newCursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("URI not matched!");
        }
        //db.close();   // causes crash!!
        return newCursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    /**********************************DB Helper***********************************/
    private class MovieDB extends SQLiteOpenHelper {

        public MovieDB(Context context) {
            super(context, Moviedb.DATABASE_NAME, null, Moviedb.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL("CREATE TABLE " + Moviedb.MOVIES_TABLE +
                    " (" + Moviedb._ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Moviedb.ID_COLUMN + " TEXT NOT NULL, " +
                    Moviedb.THUMB_COLUMN + " TEXT NOT NULL, " + Moviedb.TITLE_COLUMN + " TEXT NOT NULL, " +
                    Moviedb.DESCRIPTION_COLUMN + " TEXT NOT NULL, " + Moviedb.DATE_COLUMN + " TEXT NOT NULL, " +
                    Moviedb.VOTE_COLUMN + " TEXT NOT NULL, " + Moviedb.DURATION_COLUMN + " TEXT NOT NULL);");
            db.execSQL("CREATE TABLE " + Moviedb.TRAILERS_TABLE +
                    " (" + Moviedb._ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Moviedb.ID_COLUMN + " TEXT NOT NULL, " +
                    Moviedb.KEY_COLUMN + " TEXT NOT NULL, " + Moviedb.NAME_COLUMN + " TEXT NOT NULL);");
            db.execSQL("CREATE TABLE " + Moviedb.REVIEWS_TABLE +
                    " (" + Moviedb._ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Moviedb.ID_COLUMN + " TEXT NOT NULL, " +
                    Moviedb.AUTHOR_COLUMN + " TEXT NOT NULL, " + Moviedb.CONTENT_COLUMN + " TEXT NOT NULL);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
            db.execSQL("DROP TABLE IF EXISTS " + Moviedb.MOVIES_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + Moviedb.REVIEWS_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + Moviedb.TRAILERS_TABLE);
            onCreate(db);
        }
    }
}
