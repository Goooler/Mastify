package com.github.whitescent.mastify.screen.post

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.whitescent.R
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.database.model.AccountEntity
import com.github.whitescent.mastify.extensions.insertString
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.component.EmojiSheet
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.HtmlText
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.transitions.PostTransitions
import com.github.whitescent.mastify.utils.PostState
import com.github.whitescent.mastify.viewModel.PostViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@AppNavGraph
@Destination(style = PostTransitions::class)
@Composable
fun Post(
  viewModel: PostViewModel = hiltViewModel(),
  navigator: DestinationsNavigator
) {
  val focusRequester = remember { FocusRequester() }
  var isFocused by remember { mutableStateOf(false) }
  var openSheet by remember { mutableStateOf(false) }

  val sheetState = rememberModalBottomSheetState()
  val keyboard = LocalSoftwareKeyboardController.current
  val scope = rememberCoroutineScope()
  val postTextField = viewModel.postTextField
  val state = viewModel.uiState
  val instanceUiData = state.instanceUiData

  Column(
    modifier = Modifier.fillMaxSize(),
  ) {
    PostTopBar(viewModel.account!!) { navigator.popBackStack() }
    HeightSpacer(value = 4.dp)
    Column(
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .weight(1f)
        .background(AppTheme.colors.background),
    ) {
      HeightSpacer(value = 8.dp)
      BasicTextField(
        value = postTextField,
        onValueChange = viewModel::updateTextFieldValue,
        modifier = Modifier
          .fillMaxSize()
          .focusRequester(focusRequester)
          .onFocusChanged { isFocused = it.isFocused },
        textStyle = TextStyle(fontSize = 20.sp, color = AppTheme.colors.primaryContent),
        cursorBrush = SolidColor(AppTheme.colors.primaryContent),
      ) {
        Box {
          if (postTextField.text.isEmpty()) {
            Text(
              text = "最近有什么新鲜事呢？",
              color = Color(0xFFB6B6B6),
              style = TextStyle(fontSize = 18.sp, color = AppTheme.colors.primaryContent),
            )
          }
          it()
        }
      }
      HeightSpacer(value = 24.dp)
    }
    CenterRow(Modifier.padding(12.dp)) {
      Box(Modifier.weight(1f)) {
        Surface(
          color = AppTheme.colors.background,
          shape = RoundedCornerShape(6.dp),
          border = BorderStroke(1.dp, Color(0xFF777777))
        ) {
          CenterRow(Modifier.padding(vertical = 6.dp, horizontal = 12.dp)) {
            Icon(
              painter = painterResource(id = R.drawable.globe),
              contentDescription = null,
              modifier = Modifier.size(20.dp),
              tint = Color(0xFF777777)
            )
            WidthSpacer(value = 4.dp)
            Text(
              text = "公开",
              color = Color(0xFF777777)
            )
          }
        }
      }
      Text(
        text = buildAnnotatedString {
          pushStyle(
            SpanStyle(
              color = if (postTextField.text.length <= instanceUiData.maximumTootCharacters!!)
                AppTheme.colors.primaryContent.copy(alpha = 0.48f)
              else Color(0xFFF53232)
            )
          )
          append("${postTextField.text.length}/${instanceUiData.maximumTootCharacters}")
          pop()
        },
      )
    }
    AppHorizontalDivider()
    PostToolBar(
      modifier = Modifier.padding(12.dp),
      postState = state.postState,
      postStatus = viewModel::postStatus
    ) {
      openSheet = true
    }
  }
  if (openSheet) {
    EmojiSheet(
      sheetState = sheetState,
      emojis = state.emojis,
      onDismissRequest = { openSheet = false },
      onSelectEmoji = {
        viewModel.updateTextFieldValue(
          textFieldValue = viewModel.postTextField.copy(
            text = viewModel.postTextField.text.insertString(
              insert = it,
              index = viewModel.postTextField.selection.start
            ),
            selection = TextRange(viewModel.postTextField.selection.start + it.length)
          )
        )
        scope.launch {
          sheetState.hide()
        }.invokeOnCompletion {
          openSheet = false
          keyboard?.show()
        }
      }
    )
  }
  BackHandler(sheetState.isVisible) { openSheet = false }
  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
  }
  LaunchedEffect(state.postState) {
    if (state.postState is PostState.Success) {
      navigator.popBackStack()
    }
  }
}

@Composable
fun PostToolBar(
  modifier: Modifier = Modifier,
  postState: PostState,
  postStatus: () -> Unit,
  openEmojiPicker: () -> Unit,
) {
  CenterRow(
    modifier = modifier
      .imePadding()
      .fillMaxWidth()
      .navigationBarsPadding()
  ) {
    CenterRow(
      modifier = Modifier.weight(1f),
      horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
      ClickableIcon(
        painter = painterResource(id = R.drawable.image),
        tint = AppTheme.colors.primaryContent,
        modifier = Modifier.size(28.dp),
        onClick = { /*TODO*/ },
      )
      ClickableIcon(
        painter = painterResource(id = R.drawable.emoji),
        tint = AppTheme.colors.primaryContent,
        modifier = Modifier.size(28.dp),
        onClick = openEmojiPicker,
      )
      ClickableIcon(
        painter = painterResource(id = R.drawable.warning),
        tint = AppTheme.colors.primaryContent,
        modifier = Modifier.size(28.dp),
      )
      ClickableIcon(
        painter = painterResource(id = R.drawable.chart),
        tint = AppTheme.colors.primaryContent,
        modifier = Modifier.size(28.dp),
      )
    }
    IconButton(
      onClick = postStatus,
      colors = IconButtonDefaults.filledIconButtonColors(
        containerColor = when (postState != PostState.Failure) {
          true -> AppTheme.colors.accent
          else -> Color(0xFFF53232)
        },
        contentColor = Color.White,
      )
    ) {
      when (postState) {
        is PostState.Idle, PostState.Success, PostState.Failure -> {
          Icon(
            painter = painterResource(id = R.drawable.send),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
          )
        }
        is PostState.Posting -> CircularProgressIndicator(color = Color.White, strokeWidth = 4.dp)
      }
    }
  }
}

@Composable
fun PostTopBar(
  account: AccountEntity,
  back: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(AppTheme.colors.accent)
      .padding(horizontal = 16.dp)
      .padding(vertical = 12.dp)
  ) {
    Spacer(Modifier.statusBarsPadding())
    CenterRow {
      ClickableIcon(
        painter = painterResource(id = R.drawable.close),
        onClick = back,
        modifier = Modifier.size(28.dp),
        tint = Color.White
      )
      WidthSpacer(value = 8.dp)
      CircleShapeAsyncImage(
        model = account.profilePictureUrl,
        modifier = Modifier.size(36.dp),
        shape = AppTheme.shape.avatarShape,
        onClick = { }
      )
      WidthSpacer(value = 6.dp)
      Column(modifier = Modifier.weight(1f)) {
        HtmlText(
          text = account.realDisplayName,
          fontSize = 16.sp,
          overflow = TextOverflow.Ellipsis,
          maxLines = 1,
          fontWeight = FontWeight.Medium,
          color = Color.White
        )
        HeightSpacer(value = 2.dp)
        Text(
          text = account.fullName,
          color = Color.White,
          overflow = TextOverflow.Ellipsis,
          maxLines = 1,
          fontSize = 14.sp
        )
      }
    }
  }
}