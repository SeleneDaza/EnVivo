// @ts-check
const { defineConfig } = require('@playwright/test');

module.exports = defineConfig({
    testDir: './tests',
    timeout: 60_000,
    use: {
        baseURL: 'http://localhost:8080',
        headless: false,
        slowMo: 600,
        viewport: { width: 1280, height: 800 },
        video: 'off',
    },
    reporter: [['list']],
});
