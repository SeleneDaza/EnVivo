package com.edu.uptc.EnVivo.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Tag("e2e")
class BuyEventTest extends BasePlaywrightTest {

    private static final String USER_NAME = System.getProperty("test.user.name", "usuario");
    private static final String USER_PASS = System.getProperty("test.user.pass", "user123");

    // ID de un evento activo con boletas disponibles (configurable por propiedad de sistema)
    private static final String EVENT_ID = System.getProperty("test.event.id", "1");

    @Test
    void usuarioPuedeComprarBoleta() {
        login(USER_NAME, USER_PASS);

        // Navegar a la página de compra del evento
        page.navigate(BASE_URL + "/buy-ticket/" + EVENT_ID);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        // ── Paso 1: Seleccionar boleta ──────────────────────────────────────
        Locator primerCheckbox = page.locator("input[type='checkbox'][name='ticketType']").first();
        assertThat(primerCheckbox).isVisible();
        primerCheckbox.check();

        // Habilitar y ajustar cantidad
        Locator cantidadInput = page.locator("input[data-ticket-quantity]").first();
        cantidadInput.fill("1");

        // Avanzar al paso 2
        page.click("#next-step");

        // ── Paso 2: Datos personales ────────────────────────────────────────
        assertThat(page.locator("[data-step='2']")).isVisible();

        page.fill("#fullName", "Test Comprador");
        page.fill("#document", "123456789");
        page.fill("#email", "test@envivo.com");
        page.fill("#phone", "3001234567");

        page.click("#next-step");

        // ── Paso 3: Pago ────────────────────────────────────────────────────
        assertThat(page.locator("[data-step='3']")).isVisible();

        page.fill("#cardHolder", "TEST COMPRADOR");
        page.fill("#cardNumber", "4111 1111 1111 1111");

        // Seleccionar mes de vencimiento futuro
        page.fill("#expiry", "2027-12");
        page.fill("#cvv", "123");

        page.click("#next-step");

        // ── Paso 4: Confirmación ────────────────────────────────────────────
        assertThat(page.locator("[data-step='4']")).isVisible();

        // Verificar que el resumen muestra datos del comprador
        assertThat(page.locator("#summaryName")).containsText("Test Comprador");
        assertThat(page.locator("#summaryEmail")).containsText("test@envivo.com");

        // Confirmar compra
        page.click("#confirm-step");
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        // Verificar que la compra fue procesada (no hay error en pantalla)
        assertThat(page.locator("#step-error")).isHidden();
    }

    @Test
    void compraRequiereAutenticacion() {
        // Sin login, Spring Security redirige al formulario de login
        page.navigate(BASE_URL + "/buy-ticket/" + EVENT_ID);
        page.waitForLoadState(LoadState.NETWORKIDLE);

        assertThat(page.locator("#username")).isVisible();
    }

    @Test
    void paginaCompraEventoHistoricoRedirigeAlInicio() {
        login(USER_NAME, USER_PASS);

        String historicoId = System.getProperty("test.historic.event.id", "999");
        page.navigate(BASE_URL + "/buy-ticket/" + historicoId);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        // Debe redirigir a / con mensaje de error
        String url = page.url();
        assert url.contains("/?error=") || url.equals(BASE_URL + "/")
                : "Se esperaba redirección al inicio, pero la URL fue: " + url;
    }
}
