package dev.fslab.pedidos.model

/**
 * UserRole - Tipos de usuários do sistema FilaCidadã
 */
enum class UserRole {
    ADMIN_PLATAFORMA,
    ADMIN_INSTITUICAO,
    OPERADOR,
    USUARIO_FINAL
}

/**
 * User - Representa um usuário autenticado
 */
data class User(
    val id: String,
    val nome: String,
    val email: String,
    val role: UserRole,
    val avatar: String? = null,
    val fusoHorario: String = "America/Manaus"
)
