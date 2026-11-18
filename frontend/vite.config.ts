import react from '@vitejs/plugin-react'
import { defineConfig } from 'vitest/config'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: '127.0.0.1', port: 5173, strictPort: true,
    proxy: { '/api': { target: 'http://127.0.0.1:8080' } },
  },
  preview: { host: '127.0.0.1', port: 4173, strictPort: true },
  // Worker threads keep local tests inside the same managed sandbox; no fork permissions needed.
  test: { pool: 'threads', maxWorkers: 2, environment: 'jsdom', setupFiles: ['./src/test/setup.ts'], restoreMocks: true },
})
