package com.uniremote.net

import android.content.Context
import java.util.regex.Pattern

/**
 * Manages the YouTube Playlist quick-open feature.
 * Extracts playlist IDs from URLs or raw input and saves the user's custom playlist.
 */
object YouTubeHelper {
    private const val PREFS_NAME = "uniremote_youtube"
    private const val KEY_PLAYLIST_ID = "saved_playlist_id"
    private const val KEY_PLAYLIST_NAME = "saved_playlist_name"
    private const val KEY_PLAYLIST_RAW_URL = "saved_playlist_raw_url"
    private const val KEY_YOUTUBE_PACKAGE = "saved_youtube_package"

    const val DEFAULT_YOUTUBE_PACKAGE = "com.google.android.youtube.tv"

    fun getYoutubePackage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_YOUTUBE_PACKAGE, null)
        return if (saved.isNullOrEmpty() || saved == "com.google.android.youtube.tvunplugged") {
            DEFAULT_YOUTUBE_PACKAGE
        } else {
            saved
        }
    }

    fun setYoutubePackage(context: Context, packageName: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_YOUTUBE_PACKAGE, packageName).apply()
    }

    fun getPlaylistId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PLAYLIST_ID, null)
    }

    fun getPlaylistName(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PLAYLIST_NAME, null)
    }

    fun getPlaylistRawUrl(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PLAYLIST_RAW_URL, null)
    }

    fun savePlaylist(context: Context, playlistId: String, name: String?, rawUrl: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val trimmedName = name?.trim()?.takeIf { it.isNotEmpty() }
        val trimmedUrl = rawUrl?.trim()?.takeIf { it.isNotEmpty() }

        prefs.edit()
            .putString(KEY_PLAYLIST_ID, playlistId)
            .putString(KEY_PLAYLIST_NAME, trimmedName)
            .putString(KEY_PLAYLIST_RAW_URL, trimmedUrl)
            .apply()
    }

    fun clearPlaylist(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_PLAYLIST_ID)
            .remove(KEY_PLAYLIST_NAME)
            .remove(KEY_PLAYLIST_RAW_URL)
            .apply()
    }

    /**
     * Extracts a YouTube Playlist ID from a URL, URI query parameter, or direct ID string.
     * Supports formats like:
     * - https://www.youtube.com/playlist?list=PL1234567890abcdef
     * - https://youtu.be/xxx?list=PL1234567890abcdef
     * - https://www.youtube.com/watch?v=xxx&list=PL1234567890abcdef
     * - https://music.youtube.com/playlist?list=PL1234567890abcdef
     * - PL1234567890abcdef / RD... / WL / LL
     */
    fun extractPlaylistId(input: String?): String? {
        if (input.isNullOrBlank()) return null
        val trimmed = input.trim()

        // 1. Look for list= query parameter in URL or URI
        val pattern = Pattern.compile("[?&]list=([a-zA-Z0-9_-]+)")
        val matcher = pattern.matcher(trimmed)
        if (matcher.find()) {
            return matcher.group(1)
        }

        // 2. Direct playlist ID (alphanumeric with underscores / hyphens)
        if (!trimmed.contains("://") && !trimmed.contains("?") && !trimmed.contains("/") &&
            trimmed.matches(Regex("^[a-zA-Z0-9_-]{2,100}$"))) {
            return trimmed
        }

        return null
    }

    /**
     * Builds the direct playlist browsing URL.
     * Android TV YouTube app opens the playlist overview page without auto-playing any video.
     */
    fun buildPlaylistPageUrl(playlistId: String): String =
        "https://www.youtube.com/playlist?list=$playlistId"
}
