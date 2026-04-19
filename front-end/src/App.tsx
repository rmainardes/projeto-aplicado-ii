import { Navigate, Route, Routes } from "react-router-dom";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { Toaster } from "@/components/ui/toaster";
import Layout from "@/components/Layout";
import Index from "@/pages/Index";
import Clientes from "./pages/Clientes";
import Produtos from "./pages/Produtos";
import Pedidos from "./pages/Pedidos";
import NotFound from "./pages/NotFound";
import LoginPage from "./LoginPage";
import { useAuth } from "@/context/AuthContext";

function ProtectedRoute({ children }: { children: JSX.Element }) {
  const { token } = useAuth();
  return token ? children : <Navigate to="/login" replace />;
}

function ProtectedLayout() {
  const { token } = useAuth();
  if (!token) return <Navigate to="/login" replace />;
  return <Layout />; // Layout usa <Outlet /> internamente
}

const App = () => (
  <>
    <Toaster />
    <Sonner />
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedLayout />}>
        <Route path="/" element={<Index />} />
        <Route path="/clientes" element={<Clientes />} />
        <Route path="/produtos" element={<Produtos />} />
        <Route path="/pedidos" element={<Pedidos />} />
      </Route>
      <Route path="*" element={<NotFound />} />
    </Routes>
  </>
);

export default App;
