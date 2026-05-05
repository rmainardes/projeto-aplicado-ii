import { createContext, useContext, useState, ReactNode } from "react";
import axios from "axios";

interface Usuario {
  id: number;
  nome: string;
  email: string;
  role: "ADMIN" | "FUNCIONARIO";
}
interface AuthCtx {
  usuario: Usuario | null;
  token: string | null;
  login: (email: string, senha: string) => Promise<void>;
  logout: () => void;
  isAdmin: () => boolean;
}

const AuthContext = createContext<AuthCtx | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() =>
    localStorage.getItem("token"),
  );
  const [usuario, setUsuario] = useState<Usuario | null>(() => {
    const u = localStorage.getItem("usuario");
    return u ? JSON.parse(u) : null;
  });

  const login = async (email: string, senha: string) => {
    const { data } = await axios.post("/api/auth/login", { email, senha });
    setToken(data.token);
    setUsuario(data.usuario);
    localStorage.setItem("token", data.token); // ← persiste
    localStorage.setItem("usuario", JSON.stringify(data.usuario));
  };

  const logout = () => {
    setToken(null);
    setUsuario(null);
    localStorage.removeItem("token"); // ← limpa
    localStorage.removeItem("usuario");
  };

  const isAdmin = () => usuario?.role === "ADMIN";

  return (
    <AuthContext.Provider value={{ usuario, token, login, logout, isAdmin }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth fora do AuthProvider");
  return ctx;
};
