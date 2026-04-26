import { Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';

// Protege qualquer rota que exija login
export function ProtectedRoute({ children }: { children: JSX.Element }) {
    const { token } = useAuth();
    return token ? children : <Navigate to="/login" replace />;
}

// Protege rotas exclusivas de ADMIN
export function AdminRoute({ children }: { children: JSX.Element }) {
    const { token, isAdmin } = useAuth();
    if (!token) return <Navigate to="/login" replace />;
    if (!isAdmin()) return <Navigate to="/dashboard" replace />;
    return children;
}