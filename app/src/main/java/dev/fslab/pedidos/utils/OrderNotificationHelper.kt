package dev.fslab.pedidos.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dev.fslab.pedidos.MainActivity
import dev.fslab.pedidos.R
import dev.fslab.pedidos.model.NotificationUiModel

object OrderNotificationHelper {
    private const val CHANNEL_ID = "pedidos_status"
    private const val CHANNEL_NAME = "Pedidos"

    fun canPostNotifications(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun showOrderConfirmed(
        context: Context,
        notification: NotificationUiModel
    ) {
        if (!canPostNotifications(context)) return

        ensureChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notification.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val systemNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_order)
            .setContentTitle(notification.title)
            .setContentText(notification.description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.description))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(0xFF22C55E.toInt())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()

        NotificationManagerCompat.from(context)
            .notify(notification.id.hashCode(), systemNotification)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        val existingChannel = manager.getNotificationChannel(CHANNEL_ID)
        if (existingChannel != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Avisos sobre pedidos realizados e andamento do pedido"
        }

        manager.createNotificationChannel(channel)
    }
}
