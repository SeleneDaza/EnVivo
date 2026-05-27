package com.edu.uptc.EnVivo.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

// Requires a running server at playwright.base-url (default http://localhost:8080).
// Configurable via system properties — see constants below.

@Tag("e2e")
class BuyEventTest extends BasePlaywrightTest {

    // ── Credenciales ────────────────────────────────────────────────────────
    private static final String USER_NAME   = System.getProperty("test.user.name",  "Sofia");
    private static final String USER_PASS   = System.getProperty("test.user.pass",  "123");

    // ── Evento ──────────────────────────────────────────────────────────────
    private static final String EVENT_ID    = System.getProperty("test.event.id",   "1");

    // ── Datos del comprador ──────────────────────────────────────────────────
    private static final String BUYER_NAME  = System.getProperty("test.buyer.name",     "Sofia Test");
    private static final String BUYER_DOC   = System.getProperty("test.buyer.doc",      "123456789");
    private static final String BUYER_EMAIL = System.getProperty("test.buyer.email",    "sofia@envivo.com");
    private static final String BUYER_PHONE = System.getProperty("test.buyer.phone",    "3001234567");

    // ── Tarjeta ──────────────────────────────────────────────────────────────
    // Tipo: VISA | MASTERCARD | NU
    private static final String CARD_TYPE   = System.getProperty("test.card.type",    "VISA");
    private static final String CARD_NUMBER = System.getProperty("test.card.number",  "4111111111111111");
    private static final String CARD_EXPIRY = System.getProperty("test.card.expiry",  "2027-12");
    private static final String CARD_CVV    = System.getProperty("test.card.cvv",     "123");

    @Test
    void usuarioPuedeComprarBoleta() {
        login(USER_NAME, USER_PASS);

        page.navigate(BASE_URL + "/buy-ticket/" + EVENT_ID);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        // ── Paso 1: Seleccionar boleta ──────────────────────────────────────
        Locator primerCheckbox = page.locator("input[type='checkbox'][name='ticketType']").first();
        assertThat(primerCheckbox).isVisible();
        primerCheckbox.check();

        Locator cantidadInput = page.locator("input[data-ticket-quantity]").first();
        cantidadInput.fill("1");

        page.click("#next-step");

        // ── Paso 2: Datos personales ────────────────────────────────────────
        assertThat(page.locator("[data-step='2']")).isVisible();

        page.fill("#fullName", BUYER_NAME);
        page.fill("#document", BUYER_DOC);
        page.fill("#email",    BUYER_EMAIL);
        page.fill("#phone",    BUYER_PHONE);

        page.click("#next-step");

        // ── Paso 3: Pago ────────────────────────────────────────────────────
        assertThat(page.locator("[data-step='3']")).isVisible();

        // Seleccionar tipo de tarjeta (los radio son hidden — usar JS para hacer click)
        page.locator("input[name='cardType'][value='" + CARD_TYPE + "']")
            .evaluate("el => el.click()");

        page.fill("#cardHolder", BUYER_NAME.toUpperCase());
        page.fill("#cardNumber", CARD_NUMBER);
        page.locator("#expiry").evaluate("(el, v) => { el.value = v; el.dispatchEvent(new Event('input')); }", CARD_EXPIRY);
        page.fill("#cvv", CARD_CVV);

        page.click("#next-step");

        // ── Paso 4: Confirmación ────────────────────────────────────────────
        assertThat(page.locator("[data-step='4']")).isVisible();

        assertThat(page.locator("#summaryName")).containsText(BUYER_NAME);
        assertThat(page.locator("#summaryEmail")).containsText(BUYER_EMAIL);

        // Confirmar compra — STOMP connect + POST /api/purchases/checkout
        page.click("#confirm-step");

        // showSuccess() revela #step-error con clase bg-success (hasta 25 s)
        page.locator("#step-error").waitFor(new Locator.WaitForOptions().setTimeout(25_000));
        assertThat(page.locator("#step-error")).containsText("exitosamente");
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

        String url = page.url();
        assert url.contains("/?error=") || url.equals(BASE_URL + "/")
                : "Se esperaba redirección al inicio, pero la URL fue: " + url;
    }
}
