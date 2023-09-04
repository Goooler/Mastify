package com.github.whitescent.mastify.ui.component.status

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.whitescent.R
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.ui.component.AsyncBlurImage
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer
import kotlinx.collections.immutable.ImmutableList

private val ImageGridSpacing = 2.dp
private const val DefaultAspectRatio = 270f / 162f
private val DefaultMaxHeight = 400.dp

@Composable
fun StatusMedia(
  attachments: ImmutableList<Attachment>,
  modifier: Modifier = Modifier,
  onClick: ((Int) -> Unit)? = null
) {
  val mediaCount by remember(attachments.size) { mutableIntStateOf(attachments.size) }
  val boxAspectRatio = remember(mediaCount) {
    when (mediaCount) {
      in 2..4 -> DefaultAspectRatio
      1 -> attachments.first().meta?.original?.let { meta ->
        if (MediaType.fromString(attachments.first().type) == MediaType.VIDEO) {
          DefaultAspectRatio
        } else {
          (meta.width.toFloat() / meta.height.toFloat()).let {
            if (it.isNaN()) DefaultAspectRatio else it
          }
        }
      } ?: DefaultAspectRatio
      else -> null
    }
  }
  Box(
    modifier = modifier
      .let {
        if (mediaCount == 1) {
          it.heightIn(max = DefaultMaxHeight)
        } else { it }
      }
      .let {
        boxAspectRatio?.let { ratio ->
          it.aspectRatio(ratio)
        } ?: it
      }
      .clip(RoundedCornerShape(12.dp))
  ) {
    when (mediaCount) {
      3 -> {
        Row {
          StatusMediaItem(
            media = attachments[0],
            modifier = Modifier
              .weight(1f)
              .fillMaxSize(),
            onClick = {
              onClick?.invoke(0)
            }
          )
          WidthSpacer(value = ImageGridSpacing)
          Column(modifier = Modifier.weight(1f)) {
            attachments.drop(1).forEachIndexed { index, it ->
              StatusMediaItem(
                media = it,
                modifier = Modifier
                  .weight(1f)
                  .fillMaxSize(),
                onClick = {
                  onClick?.invoke(index + 1)
                }
              )
              if (it != attachments.last()) {
                HeightSpacer(value = ImageGridSpacing)
              }
            }
          }
        }
      }
      else -> {
        StatusMediaGrid(spacing = 2.dp) {
          attachments.forEachIndexed { index, it ->
            StatusMediaItem(
              media = it,
              modifier = Modifier,
              onClick = {
                onClick?.invoke(index)
              }
            )
          }
        }
      }
    }
  }
}

@Composable
fun StatusMediaItem(
  media: Attachment,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null
) {
  when (MediaType.fromString(media.type)) {
    MediaType.IMAGE -> {
      AsyncBlurImage(
        url = media.url,
        blurHash = media.blurhash ?: "",
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
          .fillMaxSize()
          .clickable {
            onClick?.invoke()
          }
      )
    }
    MediaType.VIDEO, MediaType.GIF -> {
      // show thumbnail
      Box(
        modifier = modifier
          .fillMaxSize()
          .clickable {
            onClick?.invoke()
          }
      ) {
        AsyncBlurImage(
          url = media.previewUrl,
          blurHash = media.blurhash ?: "",
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
        Image(
          painter = painterResource(id = R.drawable.play_circle_fill),
          contentDescription = null,
          modifier = Modifier.size(48.dp).align(Alignment.Center)
        )
      }
    }
    else -> Unit
  }
}

enum class MediaType {
  IMAGE, VIDEO, GIF, NULL;
  companion object {
    fun fromString(type: String): MediaType {
      return when (type) {
        "image" -> IMAGE
        "video" -> VIDEO
        "gifv" -> GIF
        else -> NULL
      }
    }
  }
}
