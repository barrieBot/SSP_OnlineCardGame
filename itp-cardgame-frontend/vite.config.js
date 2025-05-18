import { defineConfig } from 'vite'

export default defineConfig({
    server: {
        host: '0.0.0.0',
        port: 1234,
        allowedHosts: ['game.s-sal.at', 'localhost', 'card_game_front'],
        proxy: {
            '/api': {
                target: 'http://j_spring_backend:8080',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api/, '')
            },
        },
    },
})