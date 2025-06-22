import { defineConfig } from 'vite';

export default defineConfig({
  define: {
    global: 'window'
  },
  server: {
    host: '0.0.0.0',
    port: 1234,
    allowedHosts: ['game.s-sal.at', 'localhost', 'card_game_front'],
    hmr: {
        host: 'localhost',
        port: 1234,
    },
    proxy: {
      '/api': {
        target: 'http://j_spring_backend:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
        ws: true
      },
    //   '/api/ws': {
    //     target: 'http://j_spring_backend:8080',
    //     changeOrigin: true,
    //     ws: true,
    //     rewrite: (path) => path.replace(/^\/api/, ''),
    //   }
    }
  }
});
