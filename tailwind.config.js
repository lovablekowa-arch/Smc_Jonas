/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        background: '#090C10',
        surface: '#0F141C',
        'surface-variant': '#161E2B',
        'surface-elevated': '#1E2838',
        border: '#263346',
        'border-subtle': '#1B2432',
        buy: {
          DEFAULT: '#00E676',
          dark: '#052E16',
          glow: '#10B981',
          border: '#065F46'
        },
        sell: {
          DEFAULT: '#FF3B30',
          dark: '#2B0D0E',
          glow: '#EF4444',
          border: '#7F1D1D'
        },
        sniper: {
          DEFAULT: '#FFB300',
          dark: '#332300',
          glow: '#FFCA28'
        },
        setup: {
          DEFAULT: '#2979FF',
          dark: '#0D1B3E'
        },
        watchlist: {
          DEFAULT: '#A855F7',
          dark: '#2E1065'
        },
        liquidity: {
          DEFAULT: '#00E5FF',
          dark: '#003840'
        }
      },
      fontFamily: {
        mono: ['JetBrains Mono', 'Fira Code', 'Menlo', 'monospace']
      }
    },
  },
  plugins: [],
}
