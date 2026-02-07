/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                primary: {
                    bg: '#0E0E0E', // Pure Black
                    surface: '#161616', // Dark Charcoal
                },
                accent: {
                    DEFAULT: '#C65A1E', // Burnt Orange
                    hover: '#a84a15',
                },
                text: {
                    main: '#FFFFFF',
                    body: '#9CA3AF', // Light Gray
                },
                // Keeping 'coffee' alias for backward compatibility during refactor, mapping to new theme
                coffee: {
                    50: '#161616',
                    100: '#1c1c1c',
                    200: '#2a2a2a',
                    300: '#C65A1E', // Accent
                    400: '#C65A1E',
                    500: '#C65A1E', // Primary Action
                    600: '#a84a15',
                    700: '#161616',
                    800: '#0E0E0E',
                    900: '#0E0E0E',
                }
            }
        },
    },
    plugins: [],
}
