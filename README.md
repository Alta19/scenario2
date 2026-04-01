# Periplus Cart Automation

End-to-end automated test for adding a book to the shopping cart on [periplus.com](https://www.periplus.com) using **Java**, **Selenium WebDriver**, and **TestNG**.

---

## How It Works

The test runs through the following steps automatically:

1. Launches Chrome using the configured profile mode
2. Navigates to the Periplus login page
3. Logs in using credentials from `config.properties`
4. Searches for the configured book title
5. Clicks the first matching result
6. Adds the book to the cart
7. Handles any popup modal that appears after adding
8. Navigates to the checkout cart page
9. Verifies the book title is present in the cart

---

## Requirements

- Java 11+
- Maven
- Google Chrome
- ChromeDriver (matching your Chrome version)

---

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/Alta19/scenario2.git
cd scenario2
```

### 2. Configure `src/test/resources/config.properties`

All test parameters are driven from this file. No hardcoded values exist in the source code.

| Key | Description | Example |
|---|---|---|
| `periplus.email` | Account email for login | `user@email.com` |
| `periplus.password` | Account password | `yourpassword` |
| `periplus.bookTitle` | Full title of the book to add | `Kingdom Come: DC Compact Comics Edition` |
| `periplus.baseUrl` | Base URL of the store | `https://www.periplus.com` |
| `periplus.useLocalProfile` | Use persistent Chrome profile (`Y` or `N`) | `Y` |

### 3. Run the test

```bash
mvn test
```

Or use the **Test Runner for Java** beaker icon in VS Code.

---

## `useLocalProfile` — Y or N?

This is the most important setting. It controls how Chrome launches.

### `Y` — Local Profile Mode (Recommended)

Chrome launches using a persistent profile stored in the `local-chrome-profile/` folder inside the project. This folder is automatically created on first run.

**Why use this?**
Periplus is protected by Cloudflare, a bot-detection system that analyzes your browser fingerprint, cookies, and browsing history. A fresh bot browser with no cookies or history is immediately flagged.

Using a persistent profile means Chrome retains cookies, session data, and history between runs — making it appear as a real returning user.

> **Note:** All Chrome windows must be fully closed before running the test in this mode. If Chrome is already open using the same profile, the driver will crash with a `DevToolsActivePort` error.

### `N` — Standard ChromeDriver (May get blocked)

Chrome launches as a clean, fresh disposable browser with no history or cookies.

**Why avoid this?**
Cloudflare will likely detect the automated bot signature and block access with a 403/500 error or a CAPTCHA challenge page. This mode is included for completeness but is not reliable for sites with active bot protection.

---

## Bot Detection — What to Do If Blocked

If the test fails with an `HTTP 500` or is stuck on a Cloudflare challenge screen:

1. **Restart your router/modem** to get a new IP address — repeated failed bot attempts can flag your IP
2. Switch to `useLocalProfile=Y` if not already using it //reccomended to use 'N'
3. Manually open the `local-chrome-profile/` Chrome instance, browse Periplus normally for a few minutes, then re-run the test
4. Ensure your Periplus account credentials are valid and the account is not locked

---

## Project Structure

```
scenario2/
├── src/
│   └── test/
│       ├── java/com/onlinestore/
│       │   └── CartTest.java        # Main test class
│       └── resources/
│           └── config.properties    # All test parameters
├── local-chrome-profile/            # Persistent Chrome session (auto-created)
├── pom.xml
└── README.md
```

---

## Notes

- The book search is dynamic — changing `periplus.bookTitle` in `config.properties` will search for a different book without any code changes
- The cart verification checks that the book title appears in the checkout page body text. If the cart is empty, Periplus auto-removes the item, so its title will not appear — causing the test to correctly fail
- The `local-chrome-profile/` folder should be added to `.gitignore` to avoid committing personal session data
