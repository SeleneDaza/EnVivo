package com.edu.uptc.EnVivo.playwright;

import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Tag("e2e")
class CreateEventTest extends BasePlaywrightTest {

    private static final String ADMIN_USER = System.getProperty("test.admin.user", "admin");
    private static final String ADMIN_PASS = System.getProperty("test.admin.pass", "admin123");

    @Test
    void adminPuedeCrearEvento() {
        login(ADMIN_USER, ADMIN_PASS);
        assertThat(page).hasURL(BASE_URL + "/admin");

        page.click("#open-admin-modal");
        Locator modal = page.locator("#admin-modal");
        assertThat(modal).isVisible();

        String eventName = "Concierto Test " + System.currentTimeMillis();

        // Usar name= en vez de id= porque th:field puede sobrescribir el id
        page.locator("#admin-modal form input[name='name']").fill(eventName);
        page.locator("#admin-modal form textarea[name='description']").fill("Descripcion de prueba automatizada con Playwright.");

        String fecha = LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE);
        // Setear fecha via JS para evitar problemas de formato en inputs type=date
        page.locator("#admin-modal form input[name='date']")
            .evaluate("(el, v) => { el.value = v; el.dispatchEvent(new Event('input')); }", fecha);

        // Submitir el form via JS — la navegación puede completarse antes de que waitForURL se registre
        page.locator("#admin-modal form").evaluate("form => form.submit()");

        // assertThat hace polling activo: funciona aunque la navegación ya completó
        assertThat(page).hasURL(
            Pattern.compile(".*/admin\\?.*"),
            new com.microsoft.playwright.assertions.PageAssertions.HasURLOptions().setTimeout(15000)
        );

        String resultUrl = page.url();
        assert resultUrl.contains("exito") : "Se esperaba ?exito pero la URL fue: " + resultUrl;
    }

    @Test
    void crearEventoSinNombreMuestraError() {
        login(ADMIN_USER, ADMIN_PASS);
        assertThat(page).hasURL(BASE_URL + "/admin");

        page.click("#open-admin-modal");
        Locator modal = page.locator("#admin-modal");
        assertThat(modal).isVisible();

        // Intentar enviar sin nombre (campo requerido)
        page.locator("#admin-modal section form button[type='submit']").click();

        // El formulario HTML5 debe bloquear el envío y el modal sigue visible
        assertThat(modal).isVisible();
        assertThat(page).hasURL(BASE_URL + "/admin");
    }
}
