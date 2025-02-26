const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://localhost:9859/anything/sample&renew=true");
    await cas.loginWith(page);

    await cas.goto(page, "https://localhost:8443/cas");
    await cas.assertCookie(page, false);
    
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas&renew=true");
    await cas.loginWith(page);

    await cas.goto(page, "https://localhost:8443/cas");
    await cas.assertCookie(page);

    await browser.close();
})();
