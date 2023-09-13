package com.github.whitescent.mastify.ui.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Stable
interface MastifyColorScheme {
  val primaryContent: Color
  val primaryGradient: Brush
  val accent: Color
  val accent10: Color
  val background: Color
  val secondaryBackground: Color
  val bottomBarBackground: Color
  val cardBackground: Color
  val secondaryContent: Color
  val cardCaption: Color
  val cardCaption60: Color
  val cardMenu: Color
  val cardAction: Color
  val cardLike: Color
  val replyLine: Color
  val hintText: Color
  val reblogged: Color
  val replyTextFieldBackground: Color
  val replyTextFieldBorder: Color
  val followButton: Color
  val unfollowButton: Color
  val defaultHeader: Color
  val divider: Color
  val bottomSheetBackground: Color
  val bottomSheetItemBackground: Color
  val bottomSheetItemSelectedIcon: Color
  val bottomSheetItemSelectedBackground: Color
  val bottomSheetSelectedBorder: Color
  val isLight: Boolean
}

open class ColorSchemeImpl(
  override val primaryContent: Color,
  override val primaryGradient: Brush,
  override val accent: Color,
  override val accent10: Color,
  override val background: Color,
  override val secondaryBackground: Color,
  override val bottomBarBackground: Color,
  override val cardBackground: Color,
  override val secondaryContent: Color,
  override val cardCaption: Color = Color(0xFFBAC9DF),
  override val cardCaption60: Color = Color(0x99BAC9DF),
  override val cardMenu: Color = Color(0xFFBAC9DF),
  override val cardAction: Color = Color(0xFF7E8C9F),
  override val cardLike: Color = Color(0xFFEF7096),
  override val replyLine: Color,
  override val hintText: Color,
  override val reblogged: Color,
  override val replyTextFieldBackground: Color,
  override val replyTextFieldBorder: Color,
  override val followButton: Color,
  override val unfollowButton: Color,
  override val defaultHeader: Color,
  override val divider: Color,
  override val bottomSheetBackground: Color,
  override val bottomSheetItemBackground: Color,
  override val bottomSheetItemSelectedIcon: Color,
  override val bottomSheetItemSelectedBackground: Color,
  override val bottomSheetSelectedBorder: Color,
  override val isLight: Boolean,
) : MastifyColorScheme

object LightColorScheme : ColorSchemeImpl(
  primaryContent = Color(0xFF081B34),
  primaryGradient = Brush.linearGradient(listOf(Color(0xFF143D73), Color(0xFF081B34))),
  accent = Color(0xFF046FFF),
  accent10 = Color(0xE6046FFF).copy(alpha = 0.1f),
  background = Color.White,
  secondaryBackground = Color.White,
  bottomBarBackground = Color.White,
  cardBackground = Color.White,
  secondaryContent = Color(0xFF7489A6),
  replyLine = Color(0xFFcfd9de),
  hintText = Color(0xFF7489A6),
  reblogged = Color(0xFF046FFF),
  replyTextFieldBackground = Color(0xFFF4F4F4),
  replyTextFieldBorder = Color(0xFFE6E6E6),
  followButton = Color(0xFF046FFF),
  unfollowButton = Color.White,
  defaultHeader = Color(0xFF046FFF),
  divider = Color(0xFFD7D7D7).copy(0.5f),
  bottomSheetBackground = Color.White,
  bottomSheetItemBackground = Color(0xFFE2E4E9).copy(0.4f),
  bottomSheetItemSelectedIcon = Color(0xFF1E72E2),
  bottomSheetItemSelectedBackground = Color(0xFFE2E4E9).copy(0.4f),
  bottomSheetSelectedBorder = Color(0xFFAAC8F5),
  isLight = true
)

object DarkColorScheme : ColorSchemeImpl(
  primaryContent = Color.White,
  primaryGradient = Brush.linearGradient(listOf(Color(0xFF143D73), Color(0xFF081B34))),
  accent = Color(0xFF046FFF),
  accent10 = Color(0xE6046FFF).copy(alpha = 0.1f),
  background = Color(0xFF141417),
  secondaryBackground = Color.Black,
  bottomBarBackground = Color(0xFF242424),
  cardBackground = Color(0x0FFFFFFF),
  secondaryContent = Color(0xFF7489A6),
  replyLine = Color(0xFF333638),
  hintText = Color(0xFF7489A6),
  reblogged = Color(0xFF046FFF),
  replyTextFieldBackground = Color(0xFF282828),
  replyTextFieldBorder = Color(0xFF454545),
  followButton = Color.White,
  unfollowButton = Color.Black,
  defaultHeader = Color(0xFF1f9ff1),
  divider = Color(0xFFD7D7D7).copy(0.1f),
  bottomSheetBackground = Color(0xFF31323A),
  bottomSheetItemBackground = Color(0xFF24252B).copy(0.6f),
  bottomSheetItemSelectedIcon = Color.White,
  bottomSheetItemSelectedBackground = Color(0xFF4E5059).copy(0.4f),
  bottomSheetSelectedBorder = Color.White.copy(0.2f),
  isLight = false
)

val LocalMastifyColors = staticCompositionLocalOf<MastifyColorScheme> { LightColorScheme }
