import { Navigate } from "react-router-dom";
import { useAuth } from "./context/AuthContext";

export function ProtectedRoute({ children }: { children: JSX.Element }) {
  const { usuario, ready } = useAuth();

  if (!ready) return null;

  return usuario ? children : <Navigate to="/login" replace />;
}

export function AdminRoute({ children }: { children: JSX.Element }) {
  const { usuario, isAdmin, ready } = useAuth();

  if (!ready) return null;

  if (!usuario) return <Navigate to="/login" replace />;
  if (!isAdmin()) return <Navigate to="/dashboard" replace />;
  return children;
}
