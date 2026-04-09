import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-swc";
import path from "path";
import { componentTagger } from "lovable-tagger";

export default defineConfig(({ mode }) => {
  // Criamos o alias aqui fora para garantir que o 'path' seja lido
  const rootPath = path.resolve(__dirname, "./src");

  return {
    server: {
      host: "0.0.0.0",
      port: 8080,
      proxy: {
        "/api": {
          target: "http://localhost:8081",
          changeOrigin: true,
          secure: false,
          rewrite: (path) => path.replace(/^\/api/, ""),
        },
      },
    },
    // Aqui usamos o 'react' e o 'componentTagger' explicitamente
    plugins: [react(), mode === "development" && componentTagger()].filter(
      Boolean,
    ),
    resolve: {
      alias: {
        // Agora o Vite SABE que @ significa a pasta src
        "@": rootPath,
      },
    },
  };
});
