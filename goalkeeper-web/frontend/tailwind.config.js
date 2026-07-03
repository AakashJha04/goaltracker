/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#14161B',
        slate: '#5B6270',
        canvas: '#EDF0F5',
        surface: '#FFFFFF',
        line: '#E1E6EF',
        navy: {
          DEFAULT: '#0F1B33',
          700: '#16264A',
          600: '#1D3160',
        },
        cobalt: {
          DEFAULT: '#2647CE',
          600: '#1E3AAE',
          700: '#182F8C',
        },
        amber: {
          DEFAULT: '#E8A23B',
          bright: '#F4B454',
        },
        danger: '#C5453B',
      },
      fontFamily: {
        display: ['"Space Grotesk"', 'system-ui', 'sans-serif'],
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['"IBM Plex Mono"', 'ui-monospace', 'monospace'],
      },
      boxShadow: {
        card: '0 1px 2px rgba(20,22,27,0.04), 0 8px 24px rgba(20,22,27,0.06)',
        focus: '0 0 0 3px rgba(38,71,206,0.22)',
      },
      borderRadius: {
        xl2: '18px',
      },
    },
  },
  plugins: [],
};
