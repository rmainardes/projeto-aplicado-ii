import axios from "axios";
import type {
  ClienteDTO,
  ProdutoDTO,
  PedidoDTO,
  PedidoCriacaoDTO,
  ItemPedidoDTO,
} from "@/types/api";

const api = axios.create({
  baseURL: "/api",
  headers: { "Content-Type": "application/json" },
});

// Preparado para JWT futuro
// api.interceptors.request.use((config) => {
//   const token = localStorage.getItem("token");
//   if (token) config.headers.Authorization = `Bearer ${token}`;
//   return config;
// });

// ── Clientes ──
export const getClientes = () =>
  api.get<ClienteDTO[]>("/clientes").then((r) => r.data);
export const getCliente = (id: number) =>
  api.get<ClienteDTO>(`/clientes/${id}`).then((r) => r.data);
export const createCliente = (data: ClienteDTO) =>
  api.post("/clientes", data).then((r) => r.data);
export const updateCliente = (id: number, data: ClienteDTO) =>
  api.put<ClienteDTO>(`/clientes/${id}`, data).then((r) => r.data);
export const deleteCliente = (id: number) => api.delete(`/clientes/${id}`);

// ── Produtos ──
export const getProdutos = () =>
  api.get<ProdutoDTO[]>("/produtos").then((r) => r.data);
export const getProduto = (id: number) =>
  api.get<ProdutoDTO>(`/produtos/${id}`).then((r) => r.data);
export const createProduto = (data: ProdutoDTO) =>
  api.post("/produtos", data).then((r) => r.data);
export const updateProduto = (id: number, data: ProdutoDTO) =>
  api.put<ProdutoDTO>(`/produtos/${id}`, data).then((r) => r.data);
export const deleteProduto = (id: number) => api.delete(`/produtos/${id}`);

// ── Pedidos ──
export const getPedidos = () =>
  api.get<PedidoDTO[]>("/pedidos").then((r) => r.data);
export const getPedido = (id: number) =>
  api.get<PedidoDTO>(`/pedidos/${id}`).then((r) => r.data);
export const createPedido = (data: PedidoCriacaoDTO) =>
  api.post("/pedidos", data).then((r) => r.data);
export const updatePedido = (id: number, data: PedidoDTO) =>
  api.put<PedidoDTO>(`/pedidos/${id}`, data).then((r) => r.data);

// ── Itens do Pedido ──
export const addItemPedido = (idPedido: number, item: ItemPedidoDTO) =>
  api.post(`/pedidos/${idPedido}/itens`, item).then((r) => r.data);
export const removeItemPedido = (idPedido: number, idItem: number) =>
  api.delete(`/pedidos/${idPedido}/itens/${idItem}`);

export default api;
