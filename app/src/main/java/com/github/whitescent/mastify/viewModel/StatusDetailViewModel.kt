package com.github.whitescent.mastify.viewModel

import android.content.ClipData
import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.connyduck.calladapter.networkresult.fold
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.repository.InstanceRepository
import com.github.whitescent.mastify.mapper.status.toUiData
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.emoji.Emoji
import com.github.whitescent.mastify.network.model.status.NewStatus
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.screen.navArgs
import com.github.whitescent.mastify.screen.other.StatusDetailNavArgs
import com.github.whitescent.mastify.utils.PostState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class StatusDetailViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  private val api: MastodonApi,
  private val instanceRepository: InstanceRepository
) : ViewModel() {

  private var isInitialLoad = false

  val navArgs: StatusDetailNavArgs = savedStateHandle.navArgs()

  var replyField by mutableStateOf(TextFieldValue(""))
    private set

  var uiState by mutableStateOf(StatusDetailUiState())
    private set

  fun onStatusAction(action: StatusAction, context: Context) {
    val clipManager =
      context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    viewModelScope.launch {
      when (action) {
        is StatusAction.Favorite -> {
          if (action.favorite) api.favouriteStatus(action.id) else api.unfavouriteStatus(action.id)
        }
        is StatusAction.Reblog -> {
          if (action.reblog) api.reblogStatus(action.id) else api.unreblogStatus(action.id)
        }
        is StatusAction.Bookmark -> {
          if (action.bookmark) api.bookmarkStatus(action.id) else api.unbookmarkStatus(action.id)
        }
        is StatusAction.CopyText -> {
          clipManager.setPrimaryClip(ClipData.newPlainText("PLAIN_TEXT_LABEL", action.text))
        }
        is StatusAction.CopyLink -> {
          clipManager.setPrimaryClip(ClipData.newPlainText("PLAIN_TEXT_LABEL", action.link))
        }
        is StatusAction.Mute -> Unit
        is StatusAction.Block -> Unit
        is StatusAction.Report -> Unit
      }
    }
  }

  fun replyToStatus() {
    uiState = uiState.copy(postState = PostState.Posting)
    viewModelScope.launch {
      api.createStatus(
        idempotencyKey = UUID.randomUUID().toString(),
        status = NewStatus(
          status = "${navArgs.status.account.fullname} ${replyField.text}",
          warningText = "",
          inReplyToId = navArgs.status.actionableId,
          visibility = "public", // TODO
          sensitive = false, // TODO
          mediaIds = null,
          mediaAttributes = null,
          scheduledAt = null,
          poll = null,
          language = null,
        ),
      ).fold(
        { status ->
          uiState = uiState.copy(
            postState = PostState.Success,
            descendants = uiState.descendants.toMutableList().also {
              it.add(0, status.toUiData())
            }.toImmutableList()
          )
          replyField = replyField.copy(text = "")
          delay(50)
          uiState = uiState.copy(postState = PostState.Idle)
        },
        {
          it.printStackTrace()
          uiState = uiState.copy(postState = PostState.Failure)
        }
      )
    }
  }

  init {
    uiState = uiState.copy(loading = true)
    viewModelScope.launch {
      api.statusContext(navArgs.status.actionableId).fold(
        {
          uiState = uiState.copy(
            loading = false,
            instanceEmojis = instanceRepository.getEmojis().toImmutableList(),
            ancestors = it.ancestors.toUiData().toImmutableList(),
            descendants = reorderDescendants(it.descendants),
          )
          isInitialLoad = true
        },
        {
          it.printStackTrace()
          uiState = uiState.copy(loading = false, loadError = true)
        }
      )
    }
  }

  fun updateTextFieldValue(textFieldValue: TextFieldValue) { replyField = textFieldValue }

  private fun reorderDescendants(descendants: List<Status>): ImmutableList<StatusUiData> {
    if (descendants.isEmpty() || descendants.size == 1)
      return descendants.toUiData().toImmutableList()

    // remove some replies that did not reply to the main status
    val replyList = descendants.filter { it.inReplyToId == navArgs.status.actionableId }
    val finalList = mutableListOf<Status>()

    fun searchSubReplies(current: String): List<Status> {
      val subReplies = mutableListOf<Status>()
      var now = current
      descendants.forEach {
        if (it.inReplyToId == now) {
          subReplies.add(it)
          now = it.id
        }
      }
      return subReplies
    }
    replyList.forEach { current ->
      val subReplies = searchSubReplies(current.id).toMutableList()
      if (subReplies.isNotEmpty()) {
        subReplies.add(0, current)
        finalList.addAll(subReplies)
      } else {
        finalList.add(current)
      }
    }
    return finalList.toUiData().toImmutableList()
  }
}

@Immutable
data class StatusDetailUiState(
  val loading: Boolean = false,
  val instanceEmojis: ImmutableList<Emoji> = persistentListOf(),
  val ancestors: ImmutableList<StatusUiData> = persistentListOf(),
  val descendants: ImmutableList<StatusUiData> = persistentListOf(),
  val loadError: Boolean = false,
  val postState: PostState = PostState.Idle
)
