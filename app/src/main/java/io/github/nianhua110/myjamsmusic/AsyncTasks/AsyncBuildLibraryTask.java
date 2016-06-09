package io.github.nianhua110.myjamsmusic.AsyncTasks;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import io.github.nianhua110.myjamsmusic.DBHelpers.DBAccessHelper;
import io.github.nianhua110.myjamsmusic.DBHelpers.MediaStoreAccessHelper;
import io.github.nianhua110.myjamsmusic.R;
import io.github.nianhua110.myjamsmusic.Services.BuildMusicLibraryService;
import io.github.nianhua110.myjamsmusic.Utils.Common;

/**
 * Created by kankan on 2016/5/29.
 */
public class AsyncBuildLibraryTask extends AsyncTask<Void, Void, String>{

    private  String TAG =this.getClass().getSimpleName();
    private Context mContext;
    private Common mApp;
    private BuildMusicLibraryService mService;

    private String mCurrentTask = "";
    private int mOverallProgress = 0;
    private Date date = new Date();

    public ArrayList<OnBuildLibraryProgressUpdate> mBuildLibraryProgressUpdate;

    private String mMediaStoreSelection = null;
    private HashMap<String, String> mGenresHashMap = new HashMap<String, String>();
    private HashMap<String, Integer> mGenresSongCountHashMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> mAlbumsCountMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> mSongsCountMap = new HashMap<String, Integer>();
    private HashMap<String, Uri> mMediaStoreAlbumArtMap = new HashMap<String, Uri>();
    private HashMap<String, String> mFolderArtHashMap = new HashMap<String, String>();
    public AsyncBuildLibraryTask(Context mContext,  BuildMusicLibraryService mService) {
        this.mContext = mContext;
        this.mApp = (Common) mContext;
        this.mService = mService;
        mBuildLibraryProgressUpdate = new ArrayList<OnBuildLibraryProgressUpdate>();
        // TODO: 2016/5/29  
    
    }

    /**
     * Provides callback methods that expose this
     * AsyncTask's progress.
     *
     * @author Saravan Pantham
     */
    public interface OnBuildLibraryProgressUpdate {

        /**
         * Called when this AsyncTask begins executing
         * its doInBackground() method.
         */
        public void onStartBuildingLibrary();

        /**
         * Called whenever mOverall Progress has been updated.
         */
        public void onProgressUpdate(AsyncBuildLibraryTask task, String mCurrentTask,
                                     int overallProgress, int maxProgress,
                                     boolean mediaStoreTransferDone);

        /**
         * Called when this AsyncTask finishes executing
         * its onPostExecute() method.
         */
        public void onFinishBuildingLibrary(AsyncBuildLibraryTask task);

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected String doInBackground(Void... params) {
        mCurrentTask = mContext.getResources().getString(R.string.building_music_library);
        Cursor mediaStoreCursor = getSongsFromMediaStore();

        if(mediaStoreCursor != null){
            saveMediaStoreDataToDB(mediaStoreCursor);
        }
        try {
            Log.i(TAG," do in background");
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    void saveMediaStoreDataToDB(Cursor mediaStoreCursor ){
        try{
            Thread.sleep(3000);
            mApp.getDBAccessHelper().getWritableDatabase().beginTransaction();
            //clear out the table
            mApp.getDBAccessHelper()
                    .getWritableDatabase()
                    .delete(DBAccessHelper.MUSIC_LIBRARY_TABLE, null, null);
            //Tracks the progress of this method.
            int subProgress = 0;
            if(mediaStoreCursor.getCount() !=0){
                subProgress = 250000/(mediaStoreCursor.getCount());
            }else {
                subProgress = 250000/1;
            }
            buildGenresLibrary();
            buildArtistsLibrary();;
            buildAlbumsLibrary();;
            buildMediaStoreAlbumArtHash();

            final  int titleColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            final int artistColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            final int albumColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            final int albumIdColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            final int durationColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            final int trackColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
            final int yearColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.YEAR);
            final int dateAddedColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
            final int dateModifiedColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);
            final int filePathColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            final int idColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int albumArtistColIndex = mediaStoreCursor.getColumnIndex(MediaStoreAccessHelper.ALBUM_ARTIST);

            if(albumArtistColIndex == -1){
                albumArtistColIndex = artistColIndex;
            }

            Log.i(TAG, "media store cursor "+ mediaStoreCursor.getCount());
            for(int i = 0 ;i < mediaStoreCursor.getCount(); i++) {
                mediaStoreCursor.moveToPosition(i);
                mOverallProgress += subProgress;
                publishProgress();
                Log.i(TAG, "progress " +i);

                String songTitle = mediaStoreCursor.getString(titleColIndex);
                String songArtist = mediaStoreCursor.getString(artistColIndex);
                String songAlbum = mediaStoreCursor.getString(albumColIndex);
                String songAlbumId = mediaStoreCursor.getString(albumIdColIndex);
                String songAlbumArtist = mediaStoreCursor.getString(albumArtistColIndex);
                String songFilePath = mediaStoreCursor.getString(filePathColIndex);
                String songGenre = getSongGenre(songFilePath);
                String songDuration = mediaStoreCursor.getString(durationColIndex);
                String songTrackNumber = mediaStoreCursor.getString(trackColIndex);
                String songYear = mediaStoreCursor.getString(yearColIndex);
                String songDateAdded = mediaStoreCursor.getString(dateAddedColIndex);
                String songDateModified = mediaStoreCursor.getString(dateModifiedColIndex);
                String songId = mediaStoreCursor.getString(idColIndex);
                String numberOfAlbums = "" + mAlbumsCountMap.get(songArtist);
                String numberOfTracks = "" + mSongsCountMap.get(songAlbum + songArtist);
                String numberOfSongsInGenre = "" + getGenreSongsCount(songGenre);
                String songSource = DBAccessHelper.LOCAL;
                String songSavedPosition = "-1";

                String songAlbumArtPath = "";

                if (mMediaStoreAlbumArtMap.get(songAlbum) != null) {
                    songAlbumArtPath = mMediaStoreAlbumArtMap.get(songAlbumId).toString();
                }
                if (numberOfAlbums.equals("1"))
                    numberOfAlbums += " " + mContext.getResources().getString(R.string.album_small);
                else
                    numberOfAlbums += " " + mContext.getResources().getString(R.string.albums_small);

                if (numberOfTracks.equals("1"))
                    numberOfTracks += " " + mContext.getResources().getString(R.string.song_small);
                else
                    numberOfTracks += " " + mContext.getResources().getString(R.string.songs_small);

                if (numberOfSongsInGenre.equals("1"))
                    numberOfSongsInGenre += " " + mContext.getResources().getString(R.string.song_small);
                else
                    numberOfSongsInGenre += " " + mContext.getResources().getString(R.string.songs_small);
//Check if any of the other tags were empty/null and set them to "Unknown xxx" values.
                if (songArtist == null || songArtist.isEmpty()) {
                    songArtist = mContext.getResources().getString(R.string.unknown_artist);
                }

                if (songAlbumArtist == null || songAlbumArtist.isEmpty()) {
                    if (songArtist != null && !songArtist.isEmpty()) {
                        songAlbumArtist = songArtist;
                    } else {
                        songAlbumArtist = mContext.getResources().getString(R.string.unknown_album_artist);
                    }

                }

                if (songAlbum == null || songAlbum.isEmpty()) {
                    songAlbum = mContext.getResources().getString(R.string.unknown_album);
                    ;
                }

                if (songGenre == null || songGenre.isEmpty()) {
                    songGenre = mContext.getResources().getString(R.string.unknown_genre);
                }

                //Filter out track numbers and remove any bogus values.
                if (songTrackNumber != null) {
                    if (songTrackNumber.contains("/")) {
                        int index = songTrackNumber.lastIndexOf("/");
                        songTrackNumber = songTrackNumber.substring(0, index);
                    }
                    try {
                        if (Integer.parseInt(songTrackNumber) <= 0) {
                            songTrackNumber = "";
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        songTrackNumber = "";
                    }

                }

                long durationLong = 0;
                try {
                    durationLong = Long.parseLong(songDuration);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ContentValues values = new ContentValues();
                values.put(DBAccessHelper.SONG_TITLE, songTitle);
                values.put(DBAccessHelper.SONG_ARTIST, songArtist);
                values.put(DBAccessHelper.SONG_ALBUM, songAlbum);
                values.put(DBAccessHelper.SONG_ALBUM_ARTIST, songAlbumArtist);
                values.put(DBAccessHelper.SONG_DURATION, convertMillisToMinsSecs(durationLong));
                values.put(DBAccessHelper.SONG_FILE_PATH, songFilePath);
                values.put(DBAccessHelper.SONG_TRACK_NUMBER, songTrackNumber);
                values.put(DBAccessHelper.SONG_GENRE, songGenre);
                values.put(DBAccessHelper.SONG_YEAR, songYear);
                values.put(DBAccessHelper.SONG_ALBUM_ART_PATH, songAlbumArtPath);
                values.put(DBAccessHelper.SONG_LAST_MODIFIED, songDateModified);
                values.put(DBAccessHelper.SONG_ALBUM_ART_PATH, songAlbumArtPath);
                values.put(DBAccessHelper.BLACKLIST_STATUS, false);
                values.put(DBAccessHelper.ADDED_TIMESTAMP, date.getTime());
                values.put(DBAccessHelper.RATING, 0);
                values.put(DBAccessHelper.LAST_PLAYED_TIMESTAMP, songDateModified);
                values.put(DBAccessHelper.SONG_SOURCE, songSource);
                values.put(DBAccessHelper.SONG_ID, songId);
                values.put(DBAccessHelper.SAVED_POSITION, songSavedPosition);
                values.put(DBAccessHelper.ALBUMS_COUNT, numberOfAlbums);
                values.put(DBAccessHelper.SONGS_COUNT, numberOfTracks);
                values.put(DBAccessHelper.GENRE_SONG_COUNT, numberOfSongsInGenre);

                //Add all the entries to the database to build the songs library.
                mApp.getDBAccessHelper().getWritableDatabase().insert(DBAccessHelper.MUSIC_LIBRARY_TABLE,
                        null,
                        values);
            }
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            mApp.getDBAccessHelper().getWritableDatabase().setTransactionSuccessful();
            mApp.getDBAccessHelper().getWritableDatabase().endTransaction();
        }
    }

    private String getSongGenre(String filePath){
        if(mGenresHashMap != null){
            return  mGenresHashMap.get(filePath);
        }else {
            return  mContext.getResources().getString(R.string.unknown_genre);
        }
    }

    private  int getGenreSongsCount(String genre){
        if(mGenresSongCountHashMap != null){
            if(genre !=null){
                return mGenresSongCountHashMap.get(genre);
            }else {
                return 0;
            }
        }else{
            if(mGenresSongCountHashMap.get(mContext.getResources().getString(R.string.unknown_genre))!= null)
                return mGenresSongCountHashMap.get(mContext.getResources().getString(R.string.unknown_genre));
            else
                return  0;
        }
    }

    private  void buildGenresLibrary(){
        //Get a cursor of all genres in MediaStore
      //  Cursor generesCursor = mContext.getContentResolver()

        Cursor genersCursor = mContext.getContentResolver().query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                new String[] {MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME},
                null,
                null,
                null);
        //Iterate thru  all  generes in MediaStore
        for(genersCursor.moveToFirst(); !genersCursor.isAfterLast(); genersCursor.moveToNext()){
            String genreId = genersCursor.getString(0);
            String genreName = genersCursor.getString(1);
            Log.i(TAG, "genresId "+genreId+"genreName "+ genreName);
            if (genreName==null || genreName.isEmpty() ||
                    genreName.equals(" ") || genreName.equals("   ") ||
                    genreName.equals("    "))
                genreName = mContext.getResources().getString(R.string.unknown_genre);

            Cursor cursor = mContext.getContentResolver().query(makeGenreUri(genreId),
                                                                    new String[]{MediaStore.Audio.Media.DATA},
                                                                            mMediaStoreSelection,
                                                                            null,
                                                                            null);
            if(cursor != null){
                for(int i= 0; i< cursor.getCount() ; i++){
                    cursor.moveToPosition(i);
                    mGenresHashMap.put(cursor.getString(0), genreName);
                    mGenresSongCountHashMap.put(genreName, cursor.getCount());
                }
                cursor.close();
            }


        }
        if(genersCursor != null){
            genersCursor.close();
        }
    }

    private  Uri makeGenreUri(String genreId){
        String CONTENTDIR = MediaStore.Audio.Genres.Members.CONTENT_DIRECTORY;
        return  Uri.parse(new StringBuilder().append(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI.toString())
                .append("/")
                .append(genreId)
                .append("/")
                .append(CONTENTDIR)
                .toString());
    }
    /**
     * ""
     *
     **/
    private Cursor getSongsFromMediaStore(){
        Cursor musicFolderCursor = mApp.getDBAccessHelper().getAllMusicFolderPaths();
        Cursor mediaStoreCursor = null;
        String sortOrder = null;
        String projection[]={
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media._ID,
                MediaStoreAccessHelper.ALBUM_ARTIST
        };
        if(musicFolderCursor == null || musicFolderCursor.getCount() <1){
            // TODO: 2016/5/29
            Log.i(TAG ," musicFolder is null");
           // mediaStoreCursor = MediaStoreAccessHelper.
        }else{
            mMediaStoreSelection = buildMusicFoldersSelection(musicFolderCursor);
            mediaStoreCursor = MediaStoreAccessHelper.getAllSongsWithSelection(mContext,
                    mMediaStoreSelection,
                    projection,
                    sortOrder);
            musicFolderCursor.close();
        }

        for(int i = 0 ; i< mediaStoreCursor.getCount(); i++){
            mediaStoreCursor.moveToPosition(i);
            String  temp =  mediaStoreCursor.getString(mediaStoreCursor.getColumnIndex( MediaStore.Audio.Media.TITLE));
            Log.i(TAG, temp);
        }
        return  mediaStoreCursor;
    }

    /***
     *
     */
    private String buildMusicFoldersSelection(Cursor musicFoldersCursor){
        String mediaStoreSelection  = MediaStore.Audio.Media.IS_MUSIC+ "!=0 AND (";
        int folderPathColIndex = musicFoldersCursor.getColumnIndex(DBAccessHelper.FOLDER_PATH);
        int includeColIndex = musicFoldersCursor.getColumnIndex(DBAccessHelper.INCLUDE);

        for(int i =0; i<musicFoldersCursor.getCount(); i++){
            musicFoldersCursor.moveToPosition(i);
            boolean include = musicFoldersCursor.getInt(includeColIndex) > 0;
            String likeClause;
            if(include)
                likeClause = " LIKE";
            else
                likeClause = " NOT LIKE";

            if(i != 0 && !include)
                mediaStoreSelection +=" AND ";
            else if (i!=0 && include )
                mediaStoreSelection += " OR ";

            mediaStoreSelection += MediaStore.Audio.Media.DATA + likeClause
                    + "'%" + musicFoldersCursor.getString(folderPathColIndex)
                    +"/%'";
        }

        mediaStoreSelection +=")";

        Log.i(TAG, "mediaStoreSelection is :" + mediaStoreSelection);
        return  mediaStoreSelection;
    }

    /**
     * Builds a HashMap of all songs and their genres.
     */
    private void buildArtistsLibrary(){
        Cursor artistsCursor = mContext.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Artists.ARTIST, MediaStore.Audio.Artists.NUMBER_OF_ALBUMS},
                null, null, null);
        if(artistsCursor == null){
            return;
        }

        for(int i= 0; i< artistsCursor.getCount(); i++){
            artistsCursor.moveToPosition(i);
            mAlbumsCountMap.put(artistsCursor.getString(0), artistsCursor
            .getInt(1));
        }
        artistsCursor.close();
    }

    private void buildAlbumsLibrary(){
        Cursor albumsCursor = mContext. getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.NUMBER_OF_SONGS},
                null, null, null);

        if(albumsCursor == null){
            return;
        }

        for(int i=0; i < albumsCursor.getCount(); i++){
            albumsCursor.moveToPosition(i);
            mSongsCountMap.put(albumsCursor.getString(0)+albumsCursor.getString(1), albumsCursor.getInt(2));

        }
        albumsCursor.close();
    }

    private  void buildMediaStoreAlbumArtHash(){
        Cursor albumsCursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media.ALBUM_ID},
                MediaStore.Audio.Media.IS_ALARM+"=1",
                null, null);
        final  Uri ART_CONTENT_URI = Uri.parse("content://media/external/audio/albumart");
        if(albumsCursor == null){
            return;
        }
        for (int i = 0; i< albumsCursor.getCount(); i++){
            albumsCursor.moveToPosition(i);
            Uri albumArtUri = ContentUris.withAppendedId(ART_CONTENT_URI, albumsCursor.getLong(0));
            mMediaStoreAlbumArtMap.put(albumsCursor.getString(0), albumArtUri);
        }
        albumsCursor.close();
    }

    /**
     * Convert millisseconds to hh:mm:ss format.
     *
     * @param milliseconds The input time in milliseconds to format.
     * @return The formatted time string.
     */
    private String convertMillisToMinsSecs(long milliseconds) {

        int secondsValue = (int) (milliseconds / 1000) % 60;
        int minutesValue = (int) ((milliseconds / (1000*60)) % 60);
        int hoursValue  = (int) ((milliseconds / (1000*60*60)) % 24);

        String seconds = "";
        String minutes = "";
        String hours = "";

        if (secondsValue < 10) {
            seconds = "0" + secondsValue;
        } else {
            seconds = "" + secondsValue;
        }

        minutes = "" + minutesValue;
        hours = "" + hoursValue;

        String output = "";
        if (hoursValue!=0) {
            minutes = "0" + minutesValue;
            hours = "" + hoursValue;
            output = hours + ":" + minutes + ":" + seconds;
        } else {
            minutes = "" + minutesValue;
            hours = "" + hoursValue;
            output = minutes + ":" + seconds;
        }

        return output;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (mBuildLibraryProgressUpdate!=null)
            for (int i=0; i < mBuildLibraryProgressUpdate.size(); i++)
                if (mBuildLibraryProgressUpdate.get(i)!=null)
                    mBuildLibraryProgressUpdate.get(i).onFinishBuildingLibrary(this);

    }

    /**
     * Setter methods.
     */
    public void setOnBuildLibraryProgressUpdate(OnBuildLibraryProgressUpdate
                                                        buildLibraryProgressUpdate) {
        if (buildLibraryProgressUpdate!=null)
            mBuildLibraryProgressUpdate.add(buildLibraryProgressUpdate);
    }

}
