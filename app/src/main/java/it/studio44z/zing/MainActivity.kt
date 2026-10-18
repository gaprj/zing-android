package it.studio44z.zing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import it.studio44z.zing.data.PantryViewModel
import it.studio44z.zing.ui.DispensaTheme
import it.studio44z.zing.ui.PantryApp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: PantryViewModel = viewModel()
            val settings by vm.settings.collectAsState()
            DispensaTheme(darkTheme = settings.darkTheme) {
                PantryApp(viewModel = vm)
            }
        }
    }
}
