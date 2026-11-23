package bd.cineplex.tv.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val destinations = listOf("Home", "Movies", "Series", "My List", "Settings")

@Composable
fun CineplexApp() {
    var selected by remember { mutableStateOf("Home") }
    Scaffold(
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = selected == destination,
                        onClick = { selected = destination },
                        icon = {},
                        label = { Text(destination) },
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("CINEPLEX", style = MaterialTheme.typography.headlineLarge)
            Text(selected, style = MaterialTheme.typography.titleLarge)
            ElevatedCard {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Cineplex source", style = MaterialTheme.typography.titleMedium)
                    Text("http://103.136.200.66/")
                    Text("The server is reachable, but in-app catalog and playback access are not confirmed yet.")
                    Text("No catalog items are shown until the connector can verify them.", style = MaterialTheme.typography.bodySmall)
                }
            }
            Text("Reference design: featured title and poster rows will appear here after the source connector is verified.")
        }
    }
}
