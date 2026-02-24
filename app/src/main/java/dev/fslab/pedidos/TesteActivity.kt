package dev.fslab.pedidos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import dev.fslab.pedidos.ui.screens.TesteScreen
import dev.fslab.pedidos.ui.theme.PedidosTheme

/**
 * TesteActivity - Exemplifica navegação via Intent (fora do NavHost).
 * É uma Activity completamente independente da MainActivity.
 */
class TesteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PedidosTheme {
                Scaffold(modifier = androidx.compose.ui.Modifier.fillMaxSize()) { innerPadding ->
                    TesteScreen(
                        onVoltar = { finish() }  // finish() encerra a Activity e volta para a anterior
                    )
                }
            }
        }
    }
}

