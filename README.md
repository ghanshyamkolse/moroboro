# MoroBoro — YouTube Mobile Hub 📱✨

**MoroBoro** is a premium, mobile-first single page application (SPA) built with Spring Boot and Vanilla Web technologies. It is designed to act as a distraction-free, personal portal for viewing public video uploads and playlists from your YouTube channel.

On desktop browsers, MoroBoro renders inside a sleek, bezel-less smartphone mockup frame. On mobile screens, it adapts responsively to a full-screen application.

---

## 🚀 Key Features

*   **Distraction-Free Personal Feed:** Shows only uploads and playlists from your own YouTube channel—no algorithmic suggestions, comments, or side-bar distractions.
*   **Custom User Registry (Keyless & Loginless Setup)**: Register and log in directly using your email and password. No Google OAuth configuration (`GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`) or Google Developer keys (`YOUTUBE_API_KEY`) are required.
*   **Auto-Channel Metadata Scraping**: Simply provide your YouTube custom handle (e.g., `@GoogleTech`) during sign-up. The server automatically scrapes your channel metadata, public uploads, and playlists.
*   **Minimizable Floating Player (PiP):** Tap a video to play. You can minimize the player into a small floating card in the bottom-right corner, letting you search or browse playlists while the video continues to play.
*   **Neon UI Personalization:** Choose between 5 custom glassmorphic themes (YouTube Red, Purple, Cyan, Emerald Green, and Amber Yellow) with persistent client-side states.
*   **Demo Sandbox Mode:** Instant previews are enabled out of the box using handpicked, high-quality tech videos if you are logged out.

---

## 🛠️ Technology Stack

*   **Backend:** Java 25, Spring Boot 4.0.6 (Spring Modulith Core, Spring Web MVC, Spring Security).
*   **Database:** PostgreSQL (Spring Data JDBC for user registry mappings).
*   **Frontend:** HTML5 (Semantic Structure), CSS3 (HSL custom variable themes, Glassmorphism, CSS grid/flexbox), JavaScript (ES6+).
*   **Integrations:** YouTube XML RSS Feeds, YouTube HTML InitialData Scraping, YouTube Iframe Player API.

---

## 📦 Project Structure

```text
moroboro/
├── .github/                 # GitHub modernize hooks
├── .vscode/                 # IDE workspace configurations
└── moroboro/                # Spring Boot application
    ├── src/main/java/.../   # Backend Java sources
    │   ├── config/          # Spring Security configurations
    │   ├── youtube/         # User entities, repository, authentication controller, and scraper services
    │   └── MoroboroApplication.java
    ├── src/main/resources/
    │   ├── static/          # Single Page Application root
    │   │   ├── assets/      # Neon default banners and avatars
    │   │   ├── css/         # Custom responsive stylesheets
    │   │   ├── js/          # View router and auth forms event handlers
    │   │   └── index.html   # Mobile frame structures
    │   ├── schema.sql       # Database table initializations
    │   └── application.properties
    └── .env.example         # Template for environment keys
```

---

## ⚙️ Quick Start & Configuration

### Prerequisites
*   Java Development Kit (JDK) 25
*   Maven (or use the included wrapper `./mvnw`)
*   A running PostgreSQL database instance (configured in `application.properties`)

### 1. Run the Server
From the `moroboro` root directory, execute:
```powershell
./mvnw spring-boot:run
```
Once initialized, visit `http://localhost:8080` in your web browser.

### 2. Register & Link Your YouTube Channel
1. Open the **Settings** tab (gear icon) in the app navigation.
2. Select the **Register** tab.
3. Enter your **Email Address**, **Password**, and **YouTube Custom Handle** (e.g. `@GoogleTech` or `GoogleTech`).
4. Click **Register & Link Channel**. The backend will scrape the channel metadata (resolving the handle to `UC...` ID) and save your profile.
5. Go to **Sign In**, enter your email/password, and log in. Your distraction-free personal feed is now loaded!

---

## 🛡️ Security & Privacy
*   **Encrypted Credentials:** User passwords are encrypted using Spring Security's `BCryptPasswordEncoder` before being stored in the database.
*   **No Developer Access Required:** Users do not need to register on Google Cloud Console or deal with OAuth registrations, client credentials, redirect URLs, or API key quotas.
