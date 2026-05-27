# MoroBoro — YouTube Mobile Hub 📱✨

**MoroBoro** is a premium, mobile-first single page application (SPA) built with Spring Boot and Vanilla Web technologies. It is designed to act as a distraction-free, personal portal for viewing videos, playlists, and uploads exclusively from your own YouTube channel.

On desktop browsers, MoroBoro renders inside a sleek, bezel-less smartphone mockup frame. On mobile screens, it adapts responsively to a full-screen application.

---

## 🚀 Key Features

*   **Distraction-Free Personal Feed:** Shows only uploads and playlists from your configured YouTube channel—no algorithmic suggestions or side-bar distractions.
*   **Minimizable Floating Player (PiP):** Tap a video to play it. Minimize the player into a small floating card in the bottom-right corner, letting you browse playlists or perform searches while the video continues to play.
*   **Security API Proxy:** A secure Spring Boot backend proxies all calls to the YouTube Data API v3, hiding your developer API keys from browser client-side network inspectors.
*   **Neon UI Personalization:** Choose between 5 custom glassmorphic themes (YouTube Red, Purple, Cyan, Emerald Green, and Amber Yellow) with persistent client-side states.
*   **Interactive Setup Guide:** In-app guide detailing how to generate a YouTube API Key and retrieve your Channel ID.
*   **Demo Sandbox Mode:** Instant previews are enabled out of the box using handpicked, high-quality programming and tech videos if no API credentials are supplied.

---

## 🛠️ Technology Stack

*   **Backend:** Java 25, Spring Boot 4.0.6 (Spring Modulith Core, Spring Web MVC, Spring Security).
*   **Frontend:** HTML5 (Semantic Structure), CSS3 (HSL custom variable themes, Glassmorphism, CSS grid/flexbox), JavaScript (ES6+).
*   **Integrations:** Google YouTube Data API v3, YouTube Iframe Player API.

---

## 📦 Project Structure

```text
moroboro/
├── .github/                 # GitHub modernize hooks
├── .vscode/                 # IDE workspace configurations
└── moroboro/                # Spring Boot application
    ├── src/main/java/.../   # Backend Java sources
    │   ├── config/          # Spring Security configurations
    │   ├── youtube/         # YouTube REST proxy service & controller
    │   └── MoroboroApplication.java
    ├── src/main/resources/
    │   ├── static/          # Single Page Application root
    │   │   ├── assets/      # Neon default banners and avatars
    │   │   ├── css/         # Custom responsive stylesheets
    │   │   ├── js/          # Iframe API & view router logic
    │   │   └── index.html   # Mobile frame structures
    │   └── application.properties
    └── .env.example         # Template for environment keys
```

---

## ⚙️ Quick Start & Configuration

### Prerequisites
*   Java Development Kit (JDK) 25
*   Maven (or use the included wrapper `./mvnw`)

### 1. Run the Server
From the `moroboro` root directory, execute:
```powershell
./mvnw spring-boot:run
```
Once initialized, visit `http://localhost:8080` in your web browser.

### 2. Configure Credentials (Two Methods)

#### Method A: Browser-based Setup (Easiest)
1. Open the **Settings** tab (gear icon) in the app.
2. Enter your **YouTube API Key** and **YouTube Channel ID**.
3. Click **Save & Sync**. The application will persist these in your browser's local storage and sync immediately.

#### Method B: Environment-based Setup
1. Copy the `moroboro/.env.example` file to `moroboro/.env`.
2. Populate the keys:
   ```env
   YOUTUBE_API_KEY=AIzaSy...
   YOUTUBE_CHANNEL_ID=UC...
   ```
3. Restart the Spring Boot server. The application will detect these backend credentials and sync automatically.

---

## 🛡️ License & Privacy
*   **Security:** Your credentials entered in the Settings panel are saved only in your local browser storage (`localStorage`) and are never sent to external servers other than your local backend.
