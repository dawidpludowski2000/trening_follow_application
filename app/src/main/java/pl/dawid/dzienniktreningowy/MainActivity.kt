package pl.dawid.dzienniktreningowy
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.dawid.dzienniktreningowy.ui.TrainingApp
import pl.dawid.dzienniktreningowy.ui.TrainingViewModel
import pl.dawid.dzienniktreningowy.ui.theme.DziennikTreningowyTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { DziennikTreningowyTheme { val model: TrainingViewModel = viewModel(); TrainingApp(model) } }
    }
}
