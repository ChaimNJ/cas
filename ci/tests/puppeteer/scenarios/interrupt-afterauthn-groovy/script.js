const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);

    await cas.loginWith(page, "testuser", "testuser");
    await cas.assertTextContent(page, "#content h1", "Authentication Interrupt");
    await page.waitForTimeout(1000);
    await cas.submitForm(page, "#fm1");
    await cas.assertTextContent(page, "#content h1", "Authentication Succeeded with Warnings");
    await page.waitForTimeout(1000);
    await cas.submitForm(page, "#form");
    await cas.assertCookie(page);
    await page.waitForTimeout(1000);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await browser.close();
})();
