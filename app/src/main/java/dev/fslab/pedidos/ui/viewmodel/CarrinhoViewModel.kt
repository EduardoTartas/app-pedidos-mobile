package dev.fslab.pedidos.ui.viewmodel

import androidx.lifecycle.ViewModel
import dev.fslab.pedidos.model.AdicionalOpcao
import dev.fslab.pedidos.model.ItemCarrinho
import dev.fslab.pedidos.model.Prato
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CarrinhoViewModel : ViewModel() {

    private val _itens = MutableStateFlow<List<ItemCarrinho>>(emptyList())
    val itens: StateFlow<List<ItemCarrinho>> = _itens.asStateFlow()

    /** Quantidade total de itens no carrinho (considerando `quantidade` de cada item) */
    val totalItens: Int
        get() = _itens.value.sumOf { it.quantidade }

    /** Valor total do carrinho */
    val precoTotal: Double
        get() = _itens.value.sumOf { it.precoTotal * it.quantidade }

    /**
     * Adiciona um prato ao carrinho com as seleções atuais do ViewModel de personalização.
     */
    fun adicionarItem(
        prato: Prato,
        selecoes: Map<String, Set<String>>,
        grupos: List<GrupoComOpcoes>
    ) {
        val opcoesSelecionadas: List<AdicionalOpcao> = grupos.flatMap { gc ->
            val ids = selecoes[gc.grupo.id] ?: emptySet()
            gc.opcoes.filter { it.id in ids }
        }

        val precoExtras = opcoesSelecionadas.sumOf { it.preco }
        val precoTotal = prato.preco + precoExtras

        val novoItem = ItemCarrinho(
            prato = prato,
            selecoes = selecoes,
            opcoesSelecionadas = opcoesSelecionadas,
            quantidade = 1,
            precoTotal = precoTotal
        )

        _itens.value = _itens.value + novoItem
    }

    fun limpar() {
        _itens.value = emptyList()
    }
}
