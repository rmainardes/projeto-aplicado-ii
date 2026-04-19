import { useState } from "react";
import { useAuth } from "./context/AuthContext";
import { useNavigate } from "react-router-dom";
import { AlertCircle, Loader2 } from "lucide-react";
import { Alert, AlertDescription } from "@/components/ui/alert";

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErro("");
    setLoading(true);
    try {
      await login(email, senha);
      navigate("/");
    } catch (err: any) {
      setErro(err?.response?.data?.message ?? "Erro ao fazer login");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-background p-4">
      {/* Background decorative elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-0 right-0 w-96 h-96 bg-accent rounded-full blur-3xl opacity-10" />
        <div className="absolute bottom-0 left-0 w-96 h-96 bg-primary rounded-full blur-3xl opacity-5" />
      </div>

      {/* Login card */}
      <div className="relative w-full max-w-md">
        <div className="bg-card border border-border rounded-lg shadow-lg p-8 space-y-8">
          {/* Logo and branding */}
          <div className="flex flex-col items-center space-y-4">
            <div className="w-24 h-24 bg-primary rounded-full flex items-center justify-center shadow-md">
              <img
                src="/favicon.ico"
                alt="Food Truck Logo"
                className="w-16 h-16"
              />
            </div>
            <div className="text-center space-y-2">
              <h1 className="font-display text-3xl font-bold text-foreground tracking-tight">
                Food Truck Manager
              </h1>
              <p className="text-sm text-muted-foreground">
                Faça login para continuar
              </p>
            </div>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Email field */}
            <div className="space-y-2">
              <label
                htmlFor="email"
                className="text-sm font-medium text-foreground"
              >
                Email
              </label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="seu@email.com"
                required
                className="w-full px-3 py-2 bg-background border border-input rounded-md text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-colors"
              />
            </div>

            {/* Password field */}
            <div className="space-y-2">
              <label
                htmlFor="senha"
                className="text-sm font-medium text-foreground"
              >
                Senha
              </label>
              <input
                id="senha"
                type="password"
                value={senha}
                onChange={(e) => setSenha(e.target.value)}
                placeholder="••••••••"
                required
                className="w-full px-3 py-2 bg-background border border-input rounded-md text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-colors"
              />
            </div>

            {/* Error message */}
            {erro && (
              <Alert variant="destructive">
                <AlertCircle className="h-4 w-4" />
                <AlertDescription>{erro}</AlertDescription>
              </Alert>
            )}

            {/* Submit button */}
            <button
              type="submit"
              disabled={loading}
              className="w-full mt-6 px-4 py-2 bg-primary hover:bg-[hsl(22_90%_48%)] disabled:opacity-50 disabled:cursor-not-allowed text-primary-foreground font-medium rounded-md transition-colors flex items-center justify-center gap-2"
            >
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  Entrando...
                </>
              ) : (
                "Entrar"
              )}
            </button>
          </form>

          {/* Footer */}
          <div className="text-center text-xs text-muted-foreground">
            <p>© 2025 Food Truck Manager. Todos os direitos reservados.</p>
          </div>
        </div>
      </div>
    </div>
  );
}
