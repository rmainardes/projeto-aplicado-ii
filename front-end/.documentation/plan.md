

# Plano: Front-end Food Truck App

## Resumo

App React com 4 telas principais (Login, Clientes, Produtos, Pedidos) conectando à API REST em `localhost:8080`. Design moderno e limpo, responsivo para mobile. Sem autenticação JWT por enquanto (backend ainda não implementou), mas a estrutura ficará preparada para adicionar depois.

## Arquitetura

```text
src/
├── lib/
│   └── api.ts              # Axios instance (baseURL, interceptor JWT futuro)
├── types/
│   └── api.ts              # Tipos: ClienteDTO, ProdutoDTO, PedidoDTO, enums
├── pages/
│   ├── Index.tsx            # Dashboard / redirect
│   ├── Clientes.tsx         # CRUD clientes
│   ├── Produtos.tsx         # CRU produtos + ativar/desativar + filtro
│   └── Pedidos.tsx          # Criar pedido, listar, atualizar status
├── components/
│   ├── Layout.tsx           # Sidebar/navbar + conteúdo
│   ├── clientes/
│   │   └── ClienteForm.tsx  # Dialog form criar/editar
│   ├── produtos/
│   │   └── ProdutoForm.tsx  # Dialog form criar/editar
│   └── pedidos/
│       ├── PedidoForm.tsx   # Dialog criar pedido (selecionar cliente, itens, etc.)
│       └── PedidoDetail.tsx # Visualizar/editar pedido, adicionar/remover itens
```

## Endpoints mapeados

| Tela | Ação | Método | Endpoint |
|------|------|--------|----------|
| Clientes | Listar | GET | `/clientes` |
| Clientes | Criar | POST | `/clientes` |
| Clientes | Editar | PUT | `/clientes/{id}` |
| Clientes | Excluir | DELETE | `/clientes/{id}` |
| Produtos | Listar | GET | `/produtos` |
| Produtos | Criar | POST | `/produtos` |
| Produtos | Editar | PUT | `/produtos/{id}` |
| Produtos | Desativar | DELETE | `/produtos/{id}` |
| Pedidos | Listar | GET | `/pedidos` |
| Pedidos | Criar | POST | `/pedidos` (com itens) |
| Pedidos | Editar status | PUT | `/pedidos/{id}` |
| Pedidos | Add item | POST | `/pedidos/{id}/itens` |
| Pedidos | Remove item | DELETE | `/pedidos/{id}/itens/{id_item}` |

## Etapas de implementação

1. **Tipos e API client** -- Criar tipos TypeScript dos DTOs e instância Axios apontando para `localhost:8080`, preparado para JWT header futuro.

2. **Layout e navegação** -- Sidebar responsiva (collapsa em mobile) com links: Clientes, Produtos, Pedidos. Usar ícones Lucide.

3. **Tela Clientes** -- Tabela com busca, botão criar, dialog para form (nome, contato), editar inline, confirmar exclusão.

4. **Tela Produtos** -- Tabela com filtro ativo/inativo (toggle), botão criar, dialog form (nome, descrição, preço, estoque). Botão desativar (DELETE) e reativar (PUT com ativo=true). Badge de status.

5. **Tela Pedidos** -- Listagem com status colorido (pendente/preparando/pronto/entregue/cancelado). Form de criação: selecionar cliente (dropdown), tipo pedido, forma pagamento, observação, e adicionar itens (selecionar produto + quantidade). Detalhe do pedido com possibilidade de alterar status e gerenciar itens.

6. **Dashboard (Index)** -- Visão rápida com cards resumo (total pedidos do dia, pedidos pendentes, etc).

## Detalhes técnicos

- **HTTP client**: Axios com `baseURL: "http://localhost:8080"` e interceptor preparado para `Authorization: Bearer <token>`
- **State/cache**: React Query (`@tanstack/react-query`) para fetch, mutations com invalidação automática
- **Forms**: React Hook Form + Zod para validação client-side
- **UI**: shadcn/ui components já instalados (Table, Dialog, Card, Form, Input, Button, Select, Badge, Tabs)
- **Responsivo**: Layout com sidebar que vira drawer em telas < 768px
- **Enums traduzidos**: FormaPagamento, StatusPedido, TipoPedido mostrados com labels amigáveis em PT-BR

