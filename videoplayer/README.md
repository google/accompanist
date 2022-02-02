# Compose implementation for ExoPlayer Ui


## Basic Usage

The basic usage is as follows:

```kotlin
val playerState = rememberVideoPlayerState()

VideoPlayer(playerState = playerState) {
    VideoPlayerControl(
        state = playerState,
        title = "Elephant Dream",
    )
}

LaunchedEffect(Unit) {
    playerState.player.setMediaItem(MediaItem.fromUri("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"))
    playerState.player.prepare()
    playerState.player.playWhenReady = true
}
```
This will display the video in your Compose layout that shows the video provided.