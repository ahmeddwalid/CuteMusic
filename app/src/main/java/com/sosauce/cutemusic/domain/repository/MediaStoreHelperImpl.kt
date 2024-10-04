package com.sosauce.cutemusic.domain.repository

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.sosauce.cutemusic.domain.model.Album
import com.sosauce.cutemusic.domain.model.Artist
import com.sosauce.cutemusic.domain.model.Folder

class MediaStoreHelperImpl(
    private val context: Context
): MediaStoreHelper {

    override fun getMusics(): List<MediaItem> {
        val musics = mutableListOf<MediaItem>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            //MediaStore.Audio.Media.IS_FAVORITE,
        )



        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val folderColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            //val isFavColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_FAVORITE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val album = cursor.getString(albumColumn)
                val filePath = cursor.getString(folderColumn)
                val folder = filePath.substring(0, filePath.lastIndexOf('/'))
                val size = cursor.getLong(sizeColumn)
                //val isFavorite = cursor.getInt(isFavColumn) // 1 = is favorite, 0 = no
                val uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                val artUri = ContentUris.appendId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.buildUpon(), id
                ).appendPath("albumart").build()


                musics.add(
                        MediaItem
                            .Builder()
                            .setUri(uri)
                            .setMediaId(id.toString())
                            .setMediaMetadata(
                                MediaMetadata
                                    .Builder()
                                    .setIsBrowsable(false)
                                    .setIsPlayable(true)
                                    .setTitle(title)
                                    .setArtist(artist)
                                    .setAlbumTitle(album)
                                    .setArtworkUri(artUri)
                                    .setExtras(
                                        Bundle()
                                            .apply {
                                                putString("folder", folder)
                                                putLong("size", size)
                                                putString("path", filePath)
                                                putString("uri", uri.toString())
                                        // putInt("isFavorite", isFavorite)
                                    }).build()
                            )
                            .build()
                )
            }
        }

        return musics
    }


    override fun getAlbums(): List<Album> {
        val albums = mutableListOf<Album>()

        val projection = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
        )

        context.contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Audio.Albums.ALBUM} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val album = cursor.getString(albumColumn)
                val artist = cursor.getString(artistColumn)

                val albumInfo = Album(id, album, artist)
                albums.add(albumInfo)
            }
        }

        return albums
    }

    override fun getArtists(): List<Artist> {
        val artists = mutableListOf<Artist>()

        val projection = arrayOf(
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
        )

        context.contentResolver.query(
            MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Audio.Artists.ARTIST} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val artist = cursor.getString(artistColumn)

                val artistInfo = Artist(
                    id = id,
                    name = artist
                )
                artists.add(artistInfo)
            }
        }

        return artists
    }


    // Only gets folder with musics in them
    override fun getFoldersWithMusics(): List<Folder> {

        val folders = mutableListOf<Folder>()

        val projection = arrayOf(
            MediaStore.Audio.Media.DATA,
        )

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use {
            val folderPaths = mutableSetOf<String>()
            val dataIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (it.moveToNext()) {
                val filePath = it.getString(dataIndex)
                val folderPath = filePath.substring(0, filePath.lastIndexOf('/'))
                folderPaths.add(folderPath)
            }
            folderPaths.forEach { path ->
                val folderName = path.substring(path.lastIndexOf('/') + 1)
                folders.add(
                    Folder(
                        name = folderName,
                        path = path,
                    )
                )
            }
        }
        return folders
    }

    override suspend fun deleteMusics(
        uris: List<Uri>,
        intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        try {
            uris.forEach { uri ->
                context.contentResolver.delete(uri, null, null)
            }
        } catch (e: SecurityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intentSender = MediaStore.createDeleteRequest(
                    context.contentResolver,
                    uris
                ).intentSender

                intentSenderLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
        } catch (e: Exception) {
            Log.e(
                ContentValues.TAG,
                "Error trying to delete song: ${e.message} ${e.stackTrace.joinToString()}"
            )
        }
    }

    override suspend fun editMusic(
        uris: List<Uri>,
        intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intentSender = MediaStore.createWriteRequest(
                context.contentResolver,
                uris
            ).intentSender

            intentSenderLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
        }
    }
}