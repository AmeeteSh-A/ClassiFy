# 🎵 Classify

> **"Don't just listen. Analyze."**

Classify is an Android application that provides intelligent song recommendations based on **Audio Features** (Energy, Valence, Danceability) rather than sentiment or language.

Unlike standard engines that rely on collaborative filtering ("Users who liked X also liked Y"), Classify analyzes the mathematical "DNA" of the sound itself using **Cosine Similarity**.

---

## 🚀 Key Features

### 1. 🧬 Sound-First Architecture
The app runs on a curated database of **11,000+ songs** with pre-analyzed audio features.
* **Why?** Spotify deprecated public access to Audio Features in 2024.
* **How?** Classify bridges this gap by maintaining its own high-fidelity dataset, allowing for deep analysis without API rate limits.

### 2. 🔥 TuneMatch (Adaptive Discovery)
A "Tinder-style" exploration mode that learns in real-time.
* **Swipe Right (Like):** The engine updates the "Base Vector" to the liked song's features. Future suggestions immediately pivot to match this new vibe.
* **Swipe Left (Dislike):** The engine maintains the current trajectory but filters out that specific cluster.
* **Result:** A session-based playlist that evolves with your mood.

### 3. 📂 Playlist Centric & Global Search
* **Contextual Toggle:** Switch between recommending songs *within* your own playlist (rediscovery) or from the *entire* 11k global database (discovery).
* **Instant Export:** Convert any recommendation chain into a real Spotify playlist with one tap.

### 4. ⚡ Hybrid-Cloud Sync (Bandwidth Optimized)
Classify uses a **Read-Through Caching Strategy** to minimize data usage.
* **First Launch:** Bulk-copies the dataset from **Firebase Firestore** to a local **Room Database**.
* **Offline First:** All searches and recommendations run locally on the device (zero latency).
* **Smart Updates:** If a song is missing locally, it fetches from Cloud and updates the local cache transparently.

---

## 🛠️ Tech Stack

* **Language:** Java (Native Android)
* **Architecture:** MVVM (Model-View-ViewModel)
* **Mathematical Core:** Custom `SimilarityCalculator` using Cosine Similarity on n-dimensional feature vectors.
* **Data Layer:**
    * **Local:** Room Database (SQLite)
    * **Cloud:** Firebase Firestore
    * **Sync:** WorkManager
* **Networking:** Retrofit / OkHttp
* **Spotify Integration:** Spotify App Remote SDK (Playback & Auth)

---

## 📸 Screenshots

| TuneMatch (Discovery) | Smart Recommendations | Instant Export |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/c2870f0a-930b-4963-98e2-f920687c5bb1" width="250"> | *(Add Screenshot)* | *(Add Screenshot)* |

---

## ⚠️ Setup & Installation

**Note:** This project is in **Developer Mode** due to Spotify API restrictions.

1.  **Clone the Repo**
    ```bash
    git clone [https://github.com/Ameetesh-A/ClassiFy.git](https://github.com/Ameetesh-A/ClassiFy.git)
    ```
2.  **Configure API Keys**
    * Create an app on the [Spotify Developer Dashboard](https://developer.spotify.com/).
    * Add your `CLIENT_ID` to `core/Config.java`.
    * Add your `google-services.json` for Firebase integration.
3.  **Build**
    * Sync Gradle and run on an Android device (Spotify App must be installed).

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
