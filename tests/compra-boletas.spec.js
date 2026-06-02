// @ts-check
const { test, expect } = require('@playwright/test');

// ─── helpers ────────────────────────────────────────────────────────────────

async function login(page) {
    await page.goto('/');
    await page.locator('#username').fill('Sofia');
    await page.locator('#password').fill('123');
    await page.getByRole('button', { name: 'Ingresar' }).click();
    await page.waitForURL('**/main**');
}

async function filtrarYAbrirEvento(page) {
    // Aplicar filtro de entradas disponibles
    await page.locator('a', { hasText: 'Con entradas disponibles' }).first().click();
    await page.waitForURL(/soloDisponibles=true/);

    // Click en la primera tarjeta de evento
    await page.locator('.js-event-card').first().waitFor({ state: 'visible' });
    await page.locator('.js-event-card').first().click();

    // Esperar que el modal sea visible (Tailwind quita la clase hidden)
    await page.locator('#eventModal').waitFor({ state: 'visible' });

    // Esperar que el backend llene el href del botón "Comprar entradas"
    await page.waitForFunction(() => {
        const btn = document.getElementById('buyTicketButton');
        return btn !== null && btn.getAttribute('href') !== '#';
    }, null, { timeout: 15_000 });

    // Click en "Comprar entradas"
    await page.locator('#buyTicketButton').click();
    await page.waitForURL(/\/buy-ticket\//);
}

async function paso1SeleccionarBoleta(page) {
    // Esperar que el paso 1 sea visible
    await page.locator('[data-step="1"]').waitFor({ state: 'visible' });

    // Marcar la primera boleta disponible
    await page.locator('input[name="ticketType"]').first().check();

    await page.locator('#next-step').click();

    // Esperar que el paso 2 aparezca
    await page.locator('[data-step="2"]').waitFor({ state: 'visible' });
}

async function paso2DatosPersonales(page, nombre) {
    await page.locator('#fullName').fill(nombre);
    await page.locator('#document').fill('12345678');
    await page.locator('#email').fill(`${nombre.toLowerCase().replace(/\s+/g, '')}@test.com`);
    await page.locator('#phone').fill('3001234567');

    await page.locator('#next-step').click();

    // Esperar que el paso 3 aparezca
    await page.locator('[data-step="3"]').waitFor({ state: 'visible' });
}

async function paso3DatosDeTarjeta(page, { titular, numero, cvv, tipo }) {
    await page.locator('#cardHolder').fill(titular);

    // Radio está oculto con class="hidden" — force:true lo marca sin necesidad de visibilidad
    const tipoValue = tipo === 'Visa' ? 'VISA' : 'MASTERCARD';
    await page.locator(`input[name="cardType"][value="${tipoValue}"]`).check({ force: true });

    // Número de tarjeta — fill dispara el oninput que filtra no-dígitos
    await page.locator('#cardNumber').fill(numero);

    // type="month" no acepta fill() en Chromium/Windows — se asigna directo al DOM
    await page.locator('#expiry').evaluate((el) => {
        el.value = '2028-06';
        el.dispatchEvent(new Event('input', { bubbles: true }));
        el.dispatchEvent(new Event('change', { bubbles: true }));
    });

    await page.locator('#cvv').fill(cvv);

    await page.locator('#next-step').click();

    // Esperar que el paso 4 aparezca
    await page.locator('[data-step="4"]').waitFor({ state: 'visible' });
}

async function confirmarCompra(page) {
    await page.locator('#confirm-step').click();

    // Esperar resultado del backend (éxito o error), máximo 30 s
    const exito = page.locator('#step-error.bg-success\\/10');
    const error = page.locator('#step-error.bg-error\\/5');

    await Promise.race([
        exito.waitFor({ state: 'visible', timeout: 30_000 }),
        error.waitFor({ state: 'visible', timeout: 30_000 }),
    ]);

    return { exito, error };
}

// ─── tests ──────────────────────────────────────────────────────────────────

test.describe('Compra de boletas', () => {

    test('Happy path — Visa (Ana)', async ({ page }) => {
        await login(page);
        await filtrarYAbrirEvento(page);
        await paso1SeleccionarBoleta(page);
        await paso2DatosPersonales(page, 'Ana');
        await paso3DatosDeTarjeta(page, {
            titular: 'Ana',
            numero:  '4111111111111111',
            cvv:     '123',
            tipo:    'Visa',
        });

        await expect(page.locator('#summaryName')).toHaveText('Ana');
        await expect(page.locator('#summaryCard')).toContainText('VISA');

        const { exito } = await confirmarCompra(page);
        await expect(exito).toBeVisible();
    });

    test('Happy path — Mastercard (Alicia)', async ({ page }) => {
        await login(page);
        await filtrarYAbrirEvento(page);
        await paso1SeleccionarBoleta(page);
        await paso2DatosPersonales(page, 'Alicia');
        await paso3DatosDeTarjeta(page, {
            titular: 'Alicia',
            numero:  '5111111111111111',
            cvv:     '123',
            tipo:    'Mastercard',
        });

        await expect(page.locator('#summaryName')).toHaveText('Alicia');
        await expect(page.locator('#summaryCard')).toContainText('MASTERCARD');

        const { exito } = await confirmarCompra(page);
        await expect(exito).toBeVisible();
    });

    test('Tarjeta inválida — rechazo esperado (Lucia)', async ({ page }) => {
        await login(page);
        await filtrarYAbrirEvento(page);
        await paso1SeleccionarBoleta(page);
        await paso2DatosPersonales(page, 'Lucia');
        await paso3DatosDeTarjeta(page, {
            titular: 'Lucia',
            numero:  '1234567812345678',
            cvv:     '563',
            tipo:    'Visa',
        });

        await expect(page.locator('#summaryName')).toHaveText('Lucia');

        const { error } = await confirmarCompra(page);
        await expect(error).toBeVisible();
        await expect(error).not.toHaveClass(/bg-success/);
    });

});
