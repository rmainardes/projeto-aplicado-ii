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
  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [token, setToken] = useState<string | null>(null);

  const login = async (email: string, senha: string) => {
    const { data } = await axios.post("api/auth/login", { email, senha });
    setToken(data.token);
    setUsuario(data.usuario);
    // Injeta o token em todas as requests futuras
    axios.defaults.headers.common["Authorization"] = `Bearer ${data.token}`;
  };

  const logout = () => {
    setToken(null);
    setUsuario(null);
    delete axios.defaults.headers.common["Authorization"];
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
