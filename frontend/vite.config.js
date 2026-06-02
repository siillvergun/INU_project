import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/auth": "http://localhost:8080",
      "/users": "http://localhost:8080",
      "/posts": "http://localhost:8080",
      "/comments": "http://localhost:8080"
    }
  }
});
