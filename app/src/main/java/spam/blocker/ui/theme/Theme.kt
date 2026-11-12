package spam.blocker.ui.theme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import spam.blocker.BuildConfig
import spam.blocker.G
import spam.blocker.ui.brasil.BrasilTypography
import spam.blocker.ui.isLight

@Composable fun AppTheme(content:@Composable ()->Unit) {
    val C=G.palette
    val scheme=if(C.background.isLight()) lightColorScheme() else darkColorScheme()
    MaterialTheme(colorScheme=scheme.copy(
        background=C.background,surface=C.background,surfaceContainer=C.dialogBg,
        primary=C.teal200,onPrimary=C.background,secondary=C.infoBlue,
        onBackground=C.textGrey,onSurface=C.textGrey,onSurfaceVariant=C.textGrey,
        error=C.error,outline=C.textGrey.copy(alpha=.3f)
    ),typography=if(BuildConfig.BRAZIL_LOCAL_CONTACT_MATCHING) BrasilTypography else Typography(),content=content)
}
