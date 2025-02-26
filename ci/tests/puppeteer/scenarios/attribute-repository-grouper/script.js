const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.authenticate({"username":"GrouperSystem", "password": "@4HHXr6SS42@IHz2"});
    await cas.goto(page, "https://localhost/grouper");
    await page.waitForTimeout(10000);
    await cas.gotoLogin(page);
    await cas.loginWith(page, "GrouperSystem", "Mellon");
    await page.waitForTimeout(1000);
    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "grouperGroups");
    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "etc:grouperUi:grouperUiUserData");
    await cas.assertCookie(page);
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.assertTicketParameter(page);
    await browser.close();
})();
