package dev.fslab.pedidos.ui.viewmodel

import androidx.lifecycle.ViewModel
import dev.fslab.pedidos.model.AdicionalOpcao
import dev.fslab.pedidos.model.Endereco
import dev.fslab.pedidos.model.ItemCarrinho
import dev.fslab.pedidos.model.Prato
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class FormaPagamento(val label: String, val apiValue: String) {
    CARTAO_CREDITO("Cartão de Crédito", "cartao_credito"),
    CARTAO_DEBITO("Cartão de Débito", "cartao_debito"),
    PIX("Pix", "pix"),
    DINHEIRO("Dinheiro", "dinheiro")
}

/** Representa um conflito pendente de restaurante diferente */
data class ConflitoPendente(
    val nomeRestauranteAtual: String,   // nome do restaurante do carrinho atual
    val nomeRestauranteNovo: String,    // nome do restaurante que o user quer adicionar
    val itemParaAdicionar: ItemParaAdicionar
)

data class ItemParaAdicionar(
    val prato: Prato,
    val selecoes: Map<String, Set<String>>,
    val grupos: List<GrupoComOpcoes>,
    val observacao: String = "",
    val quantidade: Int = 1
)

class CarrinhoViewModel : ViewModel() {

    private val _itens = MutableStateFlow<List<ItemCarrinho>>(emptyList())

    private val _nomeRestaurante = MutableStateFlow("")
    val nomeRestaurante: StateFlow<String> = _nomeRestaurante.asStateFlow()

    private val _restauranteId = MutableStateFlow("")
    val restauranteId: StateFlow<String> = _restauranteId.asStateFlow()

    private val _taxaEntrega = MutableStateFlow(0.0)
    val taxaEntrega: StateFlow<Double> = _taxaEntrega.asStateFlow()

    private val _conflitoPendente = MutableStateFlow<ConflitoPendente?>(null)
    val conflitoPendente: StateFlow<ConflitoPendente?> = _conflitoPendente.asStateFlow()

    val itens: StateFlow<List<ItemCarrinho>> = _itens.asStateFlow()

    private val _enderecoSelecionado = MutableStateFlow<Endereco?>(null)
    val enderecoSelecionado: StateFlow<Endereco?> = _enderecoSelecionado.asStateFlow()

    private val _formaPagamento = MutableStateFlow(FormaPagamento.PIX)
    val formaPagamento: StateFlow<FormaPagamento> = _formaPagamento.asStateFlow()

    /** Quantidade total de itens no carrinho (considerando `quantidade` de cada item) */
    val totalItens: Int
        get() = _itens.value.sumOf { it.quantidade }

    /** Valor total do carrinho */
    val precoTotal: Double
        get() = _itens.value.sumOf { it.precoTotal * it.quantidade }

    /**
     * Verifica se o item é do mesmo restaurante antes de adicionar.
     * Retorna `true` se o item foi adicionado direto (sem conflito).
     * Retorna `false` se há conflito de restaurante — a UI deve mostrar o modal
     * e aguardar a resposta do usuário (substituir ou cancelar).
     */
    fun tentarAdicionarItem(
        prato: Prato,
        selecoes: Map<String, Set<String>>,
        grupos: List<GrupoComOpcoes>,
        restauranteId: String,
        nomeRestaurante: String,
        observacao: String = "",
        quantidade: Int = 1
    ): Boolean {
        val carrinhoAtualId = _restauranteId.value
        val carrinhoVazio = _itens.value.isEmpty()

        return if (carrinhoVazio || carrinhoAtualId == restauranteId) {
            // Mesmo restaurante ou carrinho vazio: adiciona direto
            _restauranteId.value = restauranteId
            _nomeRestaurante.value = nomeRestaurante
            adicionarItem(prato, selecoes, grupos, observacao, quantidade)
            true  // adicionado com sucesso
        } else {
            // Restaurante diferente: sinaliza conflito para a UI mostrar modal
            _conflitoPendente.value = ConflitoPendente(
                nomeRestauranteAtual = _nomeRestaurante.value,
                nomeRestauranteNovo = nomeRestaurante,
                itemParaAdicionar = ItemParaAdicionar(prato, selecoes, grupos, observacao, quantidade)
            )
            false  // conflito pendente — NÃO navegar de volta ainda
        }
    }

    /** Confirma a substituição: limpa o carrinho e adiciona o novo item */
    fun substituirCarrinho(
        restauranteId: String,
        nomeRestaurante: String
    ) {
        val conflito = _conflitoPendente.value ?: return
        limpar()
        _restauranteId.value = restauranteId
        _nomeRestaurante.value = nomeRestaurante
        adicionarItem(
            conflito.itemParaAdicionar.prato,
            conflito.itemParaAdicionar.selecoes,
            conflito.itemParaAdicionar.grupos,
            conflito.itemParaAdicionar.observacao,
            conflito.itemParaAdicionar.quantidade
        )
        _conflitoPendente.value = null
    }

    /** Cancela o conflito sem modificar o carrinho */
    fun cancelarConflito() {
        _conflitoPendente.value = null
    }

    /**
     * Adiciona um prato ao carrinho com as seleções atuais do ViewModel de personalização.
     */
    fun adicionarItem(
        prato: Prato,
        selecoes: Map<String, Set<String>>,
        grupos: List<GrupoComOpcoes>,
        observacao: String = "",
        quantidade: Int = 1
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
            quantidade = quantidade,
            precoTotal = precoTotal,
            observacao = observacao
        )

        _itens.value = _itens.value + novoItem
    }

    /** Incrementa a quantidade de um item pelo seu id */
    fun incrementarItem(itemId: String) {
        _itens.value = _itens.value.map { item ->
            if (item.id == itemId) item.copy(quantidade = item.quantidade + 1) else item
        }
    }

    /** Decrementa a quantidade; remove o item se chegar a 0 */
    fun decrementarItem(itemId: String) {
        val lista = _itens.value.map { item ->
            if (item.id == itemId) item.copy(quantidade = item.quantidade - 1) else item
        }
        _itens.value = lista.filter { it.quantidade > 0 }
    }

    /** Remove um item diretamente pelo id */
    fun removerItem(itemId: String) {
        _itens.value = _itens.value.filter { it.id != itemId }
    }

    fun definirRestaurante(nome: String, id: String = "", taxa: Double = 0.0) {
        _nomeRestaurante.value = nome
        if (id.isNotEmpty()) _restauranteId.value = id
        _taxaEntrega.value = taxa
    }

    fun selecionarEndereco(endereco: Endereco) {
        _enderecoSelecionado.value = null // Reseta para forçar recomposição
        _enderecoSelecionado.value = endereco
    }

    fun selecionarFormaPagamento(forma: FormaPagamento) {
        _formaPagamento.value = forma
    }

    fun limpar() {
        _itens.value = emptyList()
        _enderecoSelecionado.value = null
        _formaPagamento.value = FormaPagamento.PIX
        _nomeRestaurante.value = ""
        _restauranteId.value = ""
        _taxaEntrega.value = 0.0
        _conflitoPendente.value = null
    }
}
