import {
  createContext,
  useContext,
  useState,
  useEffect,
  ReactNode,
} from "react";
import axios from "axios";

// ── Interfaces ──────────────────────────────────────────────────────────────

interface Usuario {
  id: number;
  nome: string;
  email: string;
  role: "ADMIN" | "FUNCIONARIO";
}

interface AuthCtx {
  usuario: Usuario | null;
  token: string | null;
  ready: boolean;
  login: (email: string, senha: string) => Promise<void>;
  logout: () => void;
  isAdmin: () => boolean;
}

export let logoutGlobal: () => void = () => {};

// ── Helpers ──────────────────────────────────────────────────────────────────

function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload.exp * 1000 < Date.now();
  } catch {
    return true;
  }
}

function getStoredAuth(): { token: string | null; usuario: Usuario | null } {
  const token = localStorage.getItem("token");
  const u = localStorage.getItem("usuario");

  if (!token || isTokenExpired(token)) {
    localStorage.removeItem("token");
    localStorage.removeItem("usuario");
    return { token: null, usuario: null };
  }

  return { token, usuario: u ? JSON.parse(u) : null };
}

// ── Context ───────────────────────────────────────────────────────────────────

const AuthContext = createContext<AuthCtx | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const stored = getStoredAuth();

  const [token, setToken] = useState<string | null>(stored.token);
  const [usuario, setUsuario] = useState<Usuario | null>(stored.usuario);
  const [ready, setReady] = useState(false);

  const logout = () => {
    setToken(null);
    setUsuario(null);
    localStorage.removeItem("token");
    localStorage.removeItem("usuario");
  };

  const login = async (email: string, senha: string) => {
    const { data } = await axios.post("/api/auth/login", { email, senha });
    setToken(data.token);
    setUsuario(data.usuario);
    localStorage.setItem("token", data.token);
    localStorage.setItem("usuario", JSON.stringify(data.usuario));
  };

  const isAdmin = () => usuario?.role === "ADMIN";

  // Registra o logout para uso externo (interceptor do axios)
  // e sinaliza que o AuthProvider terminou de inicializar
  useEffect(() => {
    logoutGlobal = logout;
    setReady(true);
  }, []);

  return (
    <AuthContext.Provider
      value={{ usuario, token, ready, login, logout, isAdmin }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth fora do AuthProvider");
  return ctx;
};
