const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://github.com");
    await cas.loginWith(page);

    await page.waitForTimeout(1000);
    await cas.assertTicketParameter(page);

    await cas.goto(page, "https://localhost:8443/cas");
    await cas.assertCookie(page);

    await browser.close();
})();
