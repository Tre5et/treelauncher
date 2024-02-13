import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  darkMode: 'class',
  theme: {
    colors: {
      'background': '#1c1b1f',
      'primary': '#00ff00',
      'onPrimary': '#000000',
      'secondary': '#4a4458'
    }
  },
  plugins: [],
};
export default config;
