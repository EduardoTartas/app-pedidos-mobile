# App Pedidos - Mobile

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-SDK_36-green.svg)](https://developer.android.com/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose-brightgreen.svg)](https://developer.android.com/jetpack/compose)

Aplicativo mobile oficial para o ecossistema de delivery **App Pedidos**, desenvolvido nativamente para Android utilizando Kotlin e os mais modernos padrões de desenvolvimento do Google, incluindo Jetpack Compose.

## Sobre o Projeto

O aplicativo atua como a interface direta entre clientes e restaurantes, oferecendo uma experiência de usuário fluida e intuitiva para a realização e o acompanhamento de pedidos de delivery. O foco principal é a **Velocidade de Conversão (Speed to Cart)**, **Clareza Operacional** e **Confiança**.

**Principais Objetivos:**
- Proporcionar uma navegação intuitiva por restaurantes e cardápios.
- Facilitar a customização de pedidos com opções e adicionais de forma clara.
- Garantir acessibilidade e alta performance na renderização de interface.
- Gerenciamento completo do perfil, pedidos e endereços do cliente.

## Funcionalidades e Características

### Navegação e Pedidos
- **Exploração de Restaurantes:** Listagem e filtros de estabelecimentos locais.
- **Cardápio Dinâmico:** Visualização rica de itens, preços e opções customizáveis (adicionais).
- **Carrinho e Checkout:** Processo otimizado para conversão rápida de compras.
- **Histórico e Confirmação de Pedido:** Visualização clara do fluxo e status das compras realizadas.

### Gestão de Endereços
- Cadastro, listagem e seleção de múltiplos endereços para entrega.
- Integração com Google Maps para precisão e visualização de locais.

### Autenticação e Perfil
- **Login e Autenticação:** Fluxos completos de entrada, cadastro, recuperação de senha e completude de perfil.
- **Autenticação Segura:** Uso do Jetpack DataStore para gerenciamento seguro da sessão local.
- **Notificações:** Alertas e histórico de atualizações dos pedidos recebidas diretamente no app.

## Stack Tecnológica

O projeto adota uma arquitetura reativa e declarativa, utilizando o que há de mais atual no ecossistema Android:

- **Linguagem:** Kotlin 
- **Interface Gráfica (UI):** Jetpack Compose, Material Design 3, Coil (carregamento de imagens) e Navigation Compose.
- **Gerenciamento de Estado e Concorrência:** Kotlin Coroutines e ViewModel (StateFlow).
- **Rede e API:** Retrofit 2, OkHttp 3.
- **Armazenamento:** DataStore Preferences.

## Como Executar o Projeto

### Pré-requisitos
- [Android Studio](https://developer.android.com/studio) (versão mais recente recomendada).
- JDK 11 ou superior.
- Dispositivo físico ou Emulador rodando Android 9.0 (API 28) ou superior.

### Configuração de Variáveis (Local Properties)
Antes de realizar a compilação, é necessário configurar chaves de API. Crie ou edite o arquivo `local.properties` na raiz do projeto mobile:

```properties
# local.properties
sdk.dir=/caminho/para/seu/android/sdk
MAPS_API_KEY="SUA_CHAVE_DO_GOOGLE_MAPS"
GOOGLE_WEB_CLIENT_ID="SEU_CLIENT_ID_DO_GOOGLE_AUTH"
```

### Passo a Passo

1. Clone o repositório.
2. Abra a pasta `app-de-pedidos-mobile` no Android Studio.
3. Aguarde o *Gradle Sync* finalizar a sincronização das dependências.
4. Execute o projeto pressionando `Shift + F10` ou clicando no botão **Run** na IDE.

## Padrões de Design de Interface
O app adere a estritos padrões de qualidade visual e UX definidos para a plataforma:
- Alto contraste de status para visibilidade impecável.
- Espaçamentos e hierarquia visual projetados para aliviar a carga cognitiva durante a escolha do prato e opcionais.
- Componentes reutilizáveis baseados em Material Design 3.

## Licença

> Este projeto e seu código-fonte são restritos e de uso exclusivo do projeto **App Pedidos**. Todos os direitos reservados.
