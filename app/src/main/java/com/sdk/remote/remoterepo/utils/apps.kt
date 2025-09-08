package com.sdk.remote.remoterepo.utils

import com.sdk.remote.androidremote.R
import com.sdk.remote.androidremote.models.App

val apps = listOf(
    App(
        com.connectsdk.R.drawable.lgcast_noti_icon,
        "Netflix",
        "https://www.netflix.com/title.*",
        pkgName = "com.netflix.ninja",
        activityName = "com.netflix.ninja.MainActivity",
    ),
    App(
        com.connectsdk.R.drawable.lgcast_noti_icon,
        "Youtube",
        "https://www.youtube.com",
        pkgName = "com.google.android.youtube.tv",
        activityName = "com.google.android.apps.youtube.tv.activity.ShellActivity",
    ),
    App(
        com.connectsdk.R.drawable.lgcast_noti_icon,
        "Spotify",
        "https://open.spotify.com/playlist",
        pkgName = "com.spotify.tv.android",
        activityName = "com.spotify.tv.android.SpotifyTVActivity",
    ),
    App(
        com.connectsdk.R.drawable.lgcast_noti_icon,
        "Play Store",
        "https://play.google.com/store",
        pkgName = "com.android.vending",
        activityName = "com.google.android.finsky.tvmainactivity.TvMainActivity",
    ),
    App(
        com.connectsdk.R.drawable.lgcast_noti_icon,
        "Youtube Kids",
        "https://www.youtubekids.com/app",
    ),
    App(
        com.connectsdk.R.drawable.lgcast_noti_icon,
        "Youtube Music",
        "https://music.youtube.com/",

        ),
    App(
        com.connectsdk.R.drawable.lgcast_noti_icon,
        "Prime video",
        "https://www.primevideo.com",
        pkgName = "com.amazon.amazonvideo.livingroom",
    )
)
