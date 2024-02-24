import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        'primary': 'rgb(var(--primary) / 1)',
        'onPrimary': 'rgb(var(--onPrimary) / 1)',
        'secondary': 'rgb(var(--secondary) / 1)',
        'background': 'rgb(var(--background) / 1)',
        'accent': 'rgb(var(--accent) / 1)',
      }
    }
  },
  plugins: [],
};
export default config;
