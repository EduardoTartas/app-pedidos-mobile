package dev.fslab.pedidos.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dev.fslab.pedidos.MainActivity
import dev.fslab.pedidos.network.NPaaSRepository
import dev.fslab.pedidos.network.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PedidosFirebaseService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        const val CHANNEL_ID = "pedidos_notificacoes"
        const val CHANNEL_NAME = "App de Pedidos"
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Novo token FCM recebido")
        getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
            .edit().putString("fcm_token", token).apply()

        // So envia ao backend se houver sessao ativa
        val accessToken = TokenManager.getAccessToken() ?: return
        CoroutineScope(Dispatchers.IO).launch {
            NPaaSRepository.registrarDispositivo(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Mensagem recebida de: ${remoteMessage.from}")

        // Exibe notificacao local quando o app esta em foreground
        remoteMessage.notification?.let { notif ->
            exibirNotificacaoLocal(
                titulo = notif.title ?: "App de Pedidos",
                corpo = notif.body ?: "",
                dados = remoteMessage.data
            )
        }

        // Notificacoes silenciosas
        if (remoteMessage.notification == null && remoteMessage.data.isNotEmpty()) {
            processarDadosSilenciosos(remoteMessage.data)
        }
    }

    private fun exibirNotificacaoLocal(titulo: String, corpo: String, dados: Map<String, String>) {
        criarCanalNotificacao()
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            dados.forEach { (k, v) -> putExtra(k, v) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Fallback icon
            .setContentTitle(titulo)
            .setContentText(corpo)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notif)
    }

    private fun processarDadosSilenciosos(data: Map<String, String>) {
        Log.d(TAG, "Dados silenciosos: $data")
    }

    private fun criarCanalNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificacoes do App de Pedidos"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
    }
}
