import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { resolve } from 'node:path'

export default defineConfig({
  base: '/admin/',
  plugins: [react()],
  resolve: {
    alias: { '@': resolve(__dirname, 'src') },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
      '/assets': 'http://localhost:8080',
      '/uploads': 'http://localhost:8080',
    },
  },
  build: {
    outDir: '../backend/src/main/resources/static/admin',
    emptyOutDir: true,
  },
})
