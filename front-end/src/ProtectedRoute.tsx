import { Navigate } from "react-router-dom";
import { useAuth } from "./context/AuthContext";

export function ProtectedRoute({ children }: { children: JSX.Element }) {
  const { token, ready } = useAuth();

  if (!ready) return null;

  return token ? children : <Navigate to="/login" replace />;
}

export function AdminRoute({ children }: { children: JSX.Element }) {
  const { token, isAdmin, ready } = useAuth();

  if (!ready) return null;

  if (!token) return <Navigate to="/login" replace />;
  if (!isAdmin()) return <Navigate to="/dashboard" replace />;
  return children;
}
