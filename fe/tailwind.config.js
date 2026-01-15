/** @type {import('tailwindcss').Config} */
module.exports = {
  // Enable class-based dark mode so adding `dark` to <html> toggles styles
  darkMode: 'class',
  content: [
    './index.html',
    './src/**/*.{js,ts,jsx,tsx,html}'
  ],
  theme: {
    extend: {},
  },
  plugins: [
    require('tw-animate-css')
  ],
};
