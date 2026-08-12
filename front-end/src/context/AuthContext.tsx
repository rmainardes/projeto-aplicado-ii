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
  ready: boolean;
  login: (email: string, senha: string) => Promise<void>;
  logout: () => void;
  isAdmin: () => boolean;
}

export let logoutGlobal: () => void = () => {};

// ── Context ───────────────────────────────────────────────────────────────────
// O JWT vive num cookie HttpOnly setado pelo back-end (não é mais lido/guardado
// em localStorage, pra não ficar acessível a um XSS). Sem acesso ao token, o
// front não sabe se a sessão é válida sem perguntar ao servidor — por isso o
// estado de autenticação é hidratado via GET /auth/me a cada carregamento.

const AuthContext = createContext<AuthCtx | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [ready, setReady] = useState(false);

  const logout = () => {
    setUsuario(null);
    axios.post("/api/auth/logout", null, { withCredentials: true }).catch(() => {});
  };

  const login = async (email: string, senha: string) => {
    const { data } = await axios.post(
      "/api/auth/login",
      { email, senha },
      { withCredentials: true },
    );
    setUsuario(data);
  };

  const isAdmin = () => usuario?.role === "ADMIN";

  useEffect(() => {
    // Registra o logout para uso externo (interceptor do axios em lib/api.ts)
    logoutGlobal = logout;

    axios
      .get("/api/auth/me", { withCredentials: true })
      .then(({ data }) => setUsuario(data))
      .catch(() => setUsuario(null))
      .finally(() => setReady(true));
  }, []);

  return (
    <AuthContext.Provider value={{ usuario, ready, login, logout, isAdmin }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth fora do AuthProvider");
  return ctx;
};
