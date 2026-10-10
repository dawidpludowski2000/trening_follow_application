package pl.dawid.dzienniktreningowy.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
private val Light = lightColorScheme(primary=Color(0xFF006C50),onPrimary=Color.White,
    primaryContainer=Color(0xFF9DF3CE),onPrimaryContainer=Color(0xFF002116),
    secondary=Color(0xFF4D6358),background=Color(0xFFF5F8F4),surface=Color(0xFFF5F8F4))
private val Dark = darkColorScheme(primary=Color(0xFF81D6B3),onPrimary=Color(0xFF003828),
    primaryContainer=Color(0xFF00513B),onPrimaryContainer=Color(0xFF9DF3CE))
@Composable fun DziennikTreningowyTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme=if(isSystemInDarkTheme()) Dark else Light, typography=Typography(), content=content)
}
