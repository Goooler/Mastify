@file:Suppress("AnimateAsStateLabel")

package com.github.whitescent.mastify.ui.component.status

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.network.model.account.Status
import com.github.whitescent.mastify.ui.component.AnimatedCountText
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.MyHtmlText
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.FormatFactory
import com.github.whitescent.mastify.utils.launchCustomChromeTab

@Composable
fun Status(
  modifier: Modifier = Modifier,
  status: Status,
  favouriteStatus: () -> Unit,
  unfavouriteStatus: () -> Unit
) {
  val avatar = status.reblog?.account?.avatar ?: status.account.avatar
  val reblogAvatar = status.account.avatar

  val displayName = status.reblog?.account?.displayName ?: status.account.displayName
  val reblogDisplayName = status.account.displayName

  val fullname = status.reblog?.account?.fullName ?: status.account.fullName
  val createdAt = status.reblog?.createdAt ?: status.createdAt
  val content = status.reblog?.content ?: status.content
  val application = status.reblog?.application ?: status.application
  val sensitive = status.reblog?.sensitive ?: status.sensitive
  val spoilerText = status.reblog?.spoilerText ?: status.spoilerText
  val mentions = status.reblog?.mentions ?: status.mentions
  val tags = status.reblog?.tags ?: status.tags
  val attachments = status.reblog?.attachments ?: status.attachments
  val repliesCount = status.reblog?.repliesCount ?: status.repliesCount
  val reblogsCount = status.reblog?.reblogsCount ?: status.reblogsCount
  val favouritesCount = status.reblog?.favouritesCount ?: status.favouritesCount
  val favourited = status.reblog?.favourited ?: status.favourited


  val context = LocalContext.current
  val primaryColor = AppTheme.colors.primaryContent
  var openDialog by rememberSaveable { mutableStateOf(false) }
  var targetMediaIndex by remember { mutableIntStateOf(0) }
  var media by remember { mutableStateOf<List<Status.Attachment>>(listOf()) }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp),
    shape = RoundedCornerShape(18.dp),
    color = AppTheme.colors.cardBackground
  ) {
    Column {
      status.reblog?.let {
        Column {
          CenterRow(
            Modifier
              .fillMaxWidth()
              .padding(top = 8.dp, bottom = 8.dp, start = 14.dp, end = 24.dp)) {
            CircleShapeAsyncImage(
              model = reblogAvatar,
              modifier = Modifier.size(24.dp)
            )
            WidthSpacer(value = 4.dp)
            Text(
              buildAnnotatedString {
                withStyle(
                  SpanStyle(
                    color = AppTheme.colors.cardCaption60,
                    fontSize = AppTheme.typography.statusRepost.fontSize,
                  )
                ) {
                  append("由 ")
                }
                withStyle(
                  SpanStyle(
                    color = AppTheme.colors.cardCaption,
                    fontSize = AppTheme.typography.statusRepost.fontSize,
                  )
                ) {
                  append(reblogDisplayName)
                }
                withStyle(
                  SpanStyle(
                    color = AppTheme.colors.cardCaption60,
                    fontSize = AppTheme.typography.statusRepost.fontSize,
                  )
                ) {
                  append(" 转发")
                }
              },
              modifier = Modifier.weight(1f)
            )
            Image(
              painter = painterResource(id = R.drawable.reblog),
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
          }
          Divider(thickness = 1.dp, color = AppTheme.colors.background)
        }
      }
      Column(modifier = Modifier.padding(12.dp)) {
        CenterRow(modifier = Modifier.fillMaxWidth()) {
          CircleShapeAsyncImage(
            model = avatar,
            modifier = Modifier.size(36.dp)
          )
          WidthSpacer(value = 7.dp)
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = displayName,
              style = AppTheme.typography.statusDisplayName,
              overflow = TextOverflow.Ellipsis,
              maxLines = 1
            )
            Text(
              text = fullname,
              style = AppTheme.typography.statusUsername.copy(
                color = AppTheme.colors.primaryContent.copy(alpha = 0.48f)
              ),
              overflow = TextOverflow.Ellipsis,
              maxLines = 1
            )
          }
          CenterRow {
            Text(
              text = FormatFactory.getTimeDiff(createdAt),
              style = AppTheme.typography.statusUsername.copy(
                color = AppTheme.colors.primaryContent.copy(alpha = 0.48f)
              ),
              overflow = TextOverflow.Ellipsis,
              maxLines = 1
            )
            WidthSpacer(value = 4.dp)
            ClickableIcon(
              painter = painterResource(id = R.drawable.more),
              tint = AppTheme.colors.cardMenu,
              modifier = Modifier.size(18.dp)
            )
          }
        }
        if (content.isNotEmpty()) {
          var mutableSensitive by remember(sensitive) { mutableStateOf(sensitive) }
          HeightSpacer(value = 4.dp)
          if (mutableSensitive) {
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = Color(0xFF3f3131)
            ) {
              CenterRow(
                modifier = Modifier
                  .clickable {
                    mutableSensitive = !mutableSensitive
                  }
                  .padding(8.dp)
              ) {
                Icon(
                  painter = painterResource(id = R.drawable.warning_circle),
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(24.dp)
                )
                WidthSpacer(value = 4.dp)
                Text(
                  text = spoilerText.ifEmpty { "敏感内容" },
                  color = Color.White
                )
              }
            }
          }
          AnimatedVisibility(visible = !mutableSensitive) {
            MyHtmlText(
              text = content,
              fontSize = 14.sp,
              maxLines = 11,
              overflow = TextOverflow.Ellipsis,
              color = AppTheme.colors.primaryContent,
            ) { span ->
              launchCustomChromeTab(
                context = context,
                uri = Uri.parse(span),
                toolbarColor = primaryColor.toArgb()
              )
            }
          }
        }
        if (attachments.isNotEmpty()) {
          HeightSpacer(value = 4.dp)
          StatusMedia(
            sensitive = sensitive,
            spoilerText = spoilerText,
            attachments = attachments,
            onClick = {
              media = attachments
              targetMediaIndex = it
              openDialog = true
            }
          )
        }
        HeightSpacer(value = 6.dp)
        StatusActionsRow(
          repliesCount = repliesCount,
          reblogsCount = reblogsCount,
          favouritesCount = favouritesCount,
          favourited = favourited,
          favouriteStatus = favouriteStatus,
          unfavouriteStatus = unfavouriteStatus
        )
      }
    }
  }
  if (openDialog) {
    StatusMediaDialog(
      avatar = avatar,
      content = content,
      media = media,
      targetMediaIndex = targetMediaIndex
    ) {
      openDialog = false
    }
  }
}

@Composable
fun StatusActionsRow(
  repliesCount: Int,
  reblogsCount: Int,
  favouritesCount: Int,
  favourited: Boolean,
  favouriteStatus: () -> Unit,
  unfavouriteStatus: () -> Unit
) {

  val favouritedColor = AppTheme.colors.cardLike
  val unfavouritedColor = AppTheme.colors.cardAction

  var favState by remember(favourited) { mutableStateOf(favourited) }
  var animatedFavCount by remember(favouritesCount) { mutableIntStateOf(favouritesCount) }
  val animatedFavIconColor by animateColorAsState(
    targetValue = if (favState) favouritedColor else unfavouritedColor,
  )

  CenterRow(
    modifier = Modifier.padding(12.dp)
  ) {
    CenterRow(modifier = Modifier.weight(1f)) {
      ClickableIcon(
        painter = painterResource(id = R.drawable.chat),
        modifier = Modifier
          .size(20.dp),
        tint = AppTheme.colors.cardAction
      )
      WidthSpacer(value = 2.dp)
      Text(
        text = repliesCount.toString(),
        style = AppTheme.typography.statusActions
      )
      WidthSpacer(value = 24.dp)
      ClickableIcon(
        painter = painterResource(id = R.drawable.heart),
        modifier = Modifier
          .size(20.dp),
        tint = animatedFavIconColor
      ) {
        favState = !favState
        if (favState) {
          animatedFavCount += 1
          favouriteStatus()
        } else {
          animatedFavCount -=1
          unfavouriteStatus()
        }
      }
      WidthSpacer(value = 2.dp)
      AnimatedCountText(
        count = animatedFavCount,
        style = AppTheme.typography.statusActions,
      )
      WidthSpacer(value = 24.dp)
      ClickableIcon(
        painter = painterResource(id = R.drawable.repost),
        modifier = Modifier
          .size(20.dp),
        tint = AppTheme.colors.cardAction
      )
      WidthSpacer(value = 2.dp)
      Text(
        text = reblogsCount.toString(),
        color = AppTheme.colors.cardAction,
      )
    }
    CenterRow {
      Surface(
        modifier = Modifier
          .size(height = 16.dp, width = 1.dp)
          .clip(RoundedCornerShape(100.dp)),
        color = AppTheme.colors.cardAction.copy(alpha = 0.12f)
      ) { }
      WidthSpacer(value = 16.dp)
      ClickableIcon(
        painter = painterResource(id = R.drawable.share),
        modifier = Modifier
          .size(20.dp),
        tint = AppTheme.colors.cardAction
      )
    }
  }
}