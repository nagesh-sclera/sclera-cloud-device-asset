import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Dev server runs on 3000 because the API gateway CORS allow-list only permits
// http://localhost:3000 / http://127.0.0.1:3000.
export default defineConfig({
  plugins: [react()],
  server: { host: true, port: 3000 },
  preview: { host: true, port: 3000 },
})
