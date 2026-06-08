package dev.fslab.pedidos.model

/**
 * Representa um item adicionado ao carrinho,
 * com o prato base, as opções selecionadas e o preço calculado.
 */
data class ItemCarrinho(
    val id: String = java.util.UUID.randomUUID().toString(),
    val prato: Prato,
    /** grupoId → lista de opcaoIds selecionadas */
    val selecoes: Map<String, Set<String>>,
    /** Opções completas para exibição e cálculo */
    val opcoesSelecionadas: List<AdicionalOpcao>,
    val quantidade: Int = 1,
    val precoTotal: Double,
    val observacao: String = ""
)
