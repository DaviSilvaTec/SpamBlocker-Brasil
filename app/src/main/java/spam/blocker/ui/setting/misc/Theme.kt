package spam.blocker.ui.setting.misc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import spam.blocker.G
import spam.blocker.R
import spam.blocker.ui.setting.LabeledRow
import spam.blocker.ui.widgets.ColorPickerButton
import spam.blocker.ui.widgets.FlowRowSpaced
import spam.blocker.ui.widgets.PopupDialog
import spam.blocker.ui.widgets.Str
import spam.blocker.ui.widgets.StrokeButton


@Composable
fun EditThemeDialog(trigger: MutableState<Boolean>) {
    PopupDialog(trigger, transparentBackground = true) {
        FlowRowSpaced(10, vSpace = 20) {
            G.palette.allColors.forEach { color ->

                ColorPickerButton(
                    color = color.state.value,
                    text = Str(color.labelId),
                    clearLabel = Str(R.string.reset),
                    clearColor = color.default
                ) { newColor ->
                    if (newColor == null)
                        color.reset()
                    else
                        color.update(newColor)
                }
            }
        }
    }
}

@Composable
fun Theme() {
    if(spam.blocker.BuildConfig.BRAZIL_LOCAL_CONTACT_MATCHING) {
        val trigger=remember { mutableStateOf(false) }
        EditThemeDialog(trigger)
        androidx.compose.foundation.layout.Column {
            LabeledRow(R.string.theme) {
                StrokeButton("Claro",color=G.palette.infoBlue) { spam.blocker.ui.brasil.applyBrasilTheme(true) }
                StrokeButton("Escuro",color=G.palette.infoBlue) { spam.blocker.ui.brasil.applyBrasilTheme(false) }
            }
            StrokeButton("Personalizar cores",color=G.palette.infoBlue) { trigger.value=true }
        }
        return
    }
    LabeledRow(R.string.theme) {
        val trigger = remember { mutableStateOf(false) }
        EditThemeDialog(trigger)

        StrokeButton(
            Str(R.string.customize),
            color = G.palette.infoBlue
        ) {
            trigger.value = true
        }
    }
}