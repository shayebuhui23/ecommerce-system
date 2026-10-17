import animate from 'tailwindcss-animate'

export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{vue,js}'],
  theme: {
    extend: {
      colors: {
        ink: {
          900: '#0B0E1A',
          800: '#121629',
          700: '#1A1F36',
          600: '#262B40'
        },
        brand: {
          50: '#EEF0FF',
          100: '#DFE3FF',
          300: '#A5AEFF',
          400: '#8A92FF',
          500: '#6366F1',
          600: '#4F46E5',
          700: '#4338CA'
        },
        mint: '#22D3B6'
      },
      fontFamily: {
        sans: ['"PingFang SC"', '"Microsoft YaHei"', 'system-ui', 'sans-serif'],
        mono: ['Bahnschrift', '"DIN Alternate"', 'Consolas', 'monospace']
      },
      boxShadow: {
        glass: '0 24px 60px -20px rgba(15, 18, 34, 0.55)',
        soft: '0 12px 32px -16px rgba(31, 36, 48, 0.35)'
      },
      keyframes: {
        floaty: {
          '0%, 100%': { transform: 'translate3d(0, 0, 0)' },
          '50%': { transform: 'translate3d(0, -18px, 0)' }
        },
        drift: {
          '0%': { transform: 'translate3d(0, 0, 0) scale(1)' },
          '50%': { transform: 'translate3d(28px, -24px, 0) scale(1.08)' },
          '100%': { transform: 'translate3d(0, 0, 0) scale(1)' }
        },
        rise: {
          from: { opacity: '0', transform: 'translate3d(0, 18px, 0)' },
          to: { opacity: '1', transform: 'translate3d(0, 0, 0)' }
        }
      },
      animation: {
        floaty: 'floaty 7s ease-in-out infinite',
        drift: 'drift 16s ease-in-out infinite',
        rise: 'rise 0.5s ease-out both'
      }
    }
  },
  plugins: [animate]
}
