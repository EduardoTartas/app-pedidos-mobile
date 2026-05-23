package dev.fslab.pedidos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.pedidos.model.AdicionalGrupo
import dev.fslab.pedidos.model.AdicionalOpcao
import dev.fslab.pedidos.model.Prato
import dev.fslab.pedidos.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════
// MODELOS INTERNOS DE UI
// ═══════════════════════════════════════════

/** Um grupo com suas opções já carregadas */
data class GrupoComOpcoes(
    val grupo: AdicionalGrupo,
    val opcoes: List<AdicionalOpcao>
)

// ═══════════════════════════════════════════
// ESTADO
// ═══════════════════════════════════════════

sealed class PersonalizacaoUiState {
    object Idle : PersonalizacaoUiState()
    object Loading : PersonalizacaoUiState()
    data class Success(
        val prato: Prato,
        val grupos: List<GrupoComOpcoes>,
        /** grupoId → conjunto de opcaoIds selecionadas */
        val selecoes: Map<String, Set<String>> = emptyMap(),
        val observacao: String = ""
    ) : PersonalizacaoUiState()
    data class Error(val message: String) : PersonalizacaoUiState()
}

// ═══════════════════════════════════════════
// VIEWMODEL
// ═══════════════════════════════════════════

class PratoPersonalizacaoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<PersonalizacaoUiState>(PersonalizacaoUiState.Idle)
    val uiState: StateFlow<PersonalizacaoUiState> = _uiState.asStateFlow()

    fun carregarGrupos(prato: Prato) {
        viewModelScope.launch {
            _uiState.value = PersonalizacaoUiState.Loading
            try {
                // 1. Buscar grupos do prato
                val gruposResp = RetrofitClient.adicionalApi.listarGruposPorPrato(prato.id)
                val grupos = if (gruposResp.isSuccessful) {
                    gruposResp.body()?.data?.filter { it.ativo } ?: emptyList()
                } else {
                    emptyList()
                }

                // 2. Para cada grupo, buscar opções em paralelo
                val gruposComOpcoes = grupos.map { grupo ->
                    async {
                        val opcoesResp = RetrofitClient.adicionalApi.listarOpcoesPorGrupo(grupo.id)
                        val opcoes = if (opcoesResp.isSuccessful) {
                            opcoesResp.body()?.data?.filter { it.ativo } ?: emptyList()
                        } else {
                            emptyList()
                        }
                        GrupoComOpcoes(grupo = grupo, opcoes = opcoes)
                    }
                }.awaitAll()

                _uiState.value = PersonalizacaoUiState.Success(
                    prato = prato,
                    grupos = gruposComOpcoes,
                    selecoes = emptyMap()
                )
            } catch (e: Exception) {
                _uiState.value = PersonalizacaoUiState.Error(
                    e.localizedMessage ?: "Erro ao carregar opções do prato."
                )
            }
        }
    }

    /**
     * Alterna seleção de uma opção dentro de um grupo.
     * Respeita o limite `max` do grupo.
     */
    fun selecionar(grupoId: String, opcaoId: String, max: Int) {
        val current = _uiState.value as? PersonalizacaoUiState.Success ?: return
        val selecoes = current.selecoes.toMutableMap()
        val selecionadas = selecoes[grupoId]?.toMutableSet() ?: mutableSetOf()

        when {
            // Radio (max 1): substitui a seleção
            max == 1 -> {
                selecoes[grupoId] = setOf(opcaoId)
            }
            // Checkbox: toggle
            selecionadas.contains(opcaoId) -> {
                selecionadas.remove(opcaoId)
                selecoes[grupoId] = selecionadas
            }
            // Adicionar apenas se abaixo do limite
            selecionadas.size < max -> {
                selecionadas.add(opcaoId)
                selecoes[grupoId] = selecionadas
            }
        }
        _uiState.value = current.copy(selecoes = selecoes)
    }

    fun aoMudarObservacao(texto: String) {
        val current = _uiState.value as? PersonalizacaoUiState.Success ?: return
        _uiState.value = current.copy(observacao = texto)
    }

    /** Preço total = preço base + soma das opções selecionadas */
    fun precoTotal(): Double {
        val state = _uiState.value as? PersonalizacaoUiState.Success ?: return 0.0
        val extras = state.grupos.sumOf { gc ->
            val ids = state.selecoes[gc.grupo.id] ?: emptySet()
            gc.opcoes.filter { it.id in ids }.sumOf { it.preco }
        }
        return state.prato.preco + extras
    }

    /**
     * Valida se todos os grupos obrigatórios atingiram o mínimo de seleções.
     * Retorna lista de nomes dos grupos ainda não satisfeitos.
     * Lista vazia = pode adicionar ao carrinho.
     */
    fun validarObrigatorios(): List<String> {
        val state = _uiState.value as? PersonalizacaoUiState.Success ?: return emptyList()
        return state.grupos
            .filter { gc ->
                gc.grupo.obrigatorio &&
                (state.selecoes[gc.grupo.id]?.size ?: 0) < gc.grupo.min
            }
            .map { it.grupo.nome }
    }

    fun resetar() {
        _uiState.value = PersonalizacaoUiState.Idle
    }
}
