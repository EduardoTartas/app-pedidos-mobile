package dev.fslab.pedidos.model

import com.google.gson.annotations.SerializedName

enum class NotificationType {
    @SerializedName("ORDER")
    ORDER,

    @SerializedName("PROMOTION")
    PROMOTION,

    @SerializedName("SYSTEM")
    SYSTEM
}

data class NotificationUiModel(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String,
    val title: String,
    val description: String,
    val createdAt: String,
    val isRead: Boolean,
    val type: NotificationType
)
