package com.edu.uptc.EnVivo.playwright;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class BasePlaywrightTest {

    protected static final String BASE_URL = System.getProperty("playwright.base-url", "http://localhost:8080");

    private static final List<String> CHROME_PATHS = List.of(
        "C:\\Program Files\\BraveSoftware\\Brave-Browser\\Application\\brave.exe",
        "C:\\Program Files (x86)\\BraveSoftware\\Brave-Browser\\Application\\brave.exe",
        System.getenv("LOCALAPPDATA") != null
            ? System.getenv("LOCALAPPDATA") + "\\BraveSoftware\\Brave-Browser\\Application\\brave.exe"
            : "",
        "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
        "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
        System.getenv("LOCALAPPDATA") != null
            ? System.getenv("LOCALAPPDATA") + "\\Google\\Chrome\\Application\\chrome.exe"
            : ""
    );

    static Playwright playwright;
    static Browser browser;

    BrowserContext context;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        boolean headless = Boolean.parseBoolean(System.getProperty("playwright.headless", "true"));
        int slowMo = Integer.parseInt(System.getProperty("playwright.slow-mo", headless ? "0" : "800"));
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setSlowMo(slowMo);

        // Usar Chrome del sistema si Playwright no tiene sus propios binarios descargados
        String customExe = System.getProperty("playwright.chrome-exe");
        if (customExe != null) {
            options.setExecutablePath(Path.of(customExe));
        } else {
            for (String candidate : CHROME_PATHS) {
                if (!candidate.isEmpty() && Files.exists(Path.of(candidate))) {
                    options.setExecutablePath(Path.of(candidate));
                    break;
                }
            }
        }

        browser = playwright.chromium().launch(options);
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    protected void login(String username, String password) {
        page.navigate(BASE_URL + "/login");
        page.fill("#username", username);
        page.fill("#password", password);
        page.click("button[type='submit']");
        // Esperar navegación fuera del login (DOMCONTENTLOADED es suficiente; NETWORKIDLE falla con CDNs externos)
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }
}
