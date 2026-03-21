package dev.fslab.pedidos.model

/**
 * User - Representa o usuário autenticado retornado pela API app-pedidos
 */
data class User(
    val id: String,
    val nome: String,
    val email: String,
    val cpf: String?,
    val telefone: String?,
    val status: String,
    val isAdmin: Boolean,
    val fotoPerfil: String?,
    val createdAt: String?,
    val updatedAt: String?
)

/**
 * Converte o payload remoto em domínio local
 */
fun RemoteUser.toUser(): User = User(
    id = id,
    nome = nome,
    email = email,
    cpf = cpf,
    telefone = telefone,
    status = status.ifBlank { "ativo" },
    isAdmin = isAdmin,
    fotoPerfil = fotoPerfil,
    createdAt = createdAt,
    updatedAt = updatedAt
)
