# DastarkhanAI

AI-powered food scanner for Android: scan barcodes for dietary compatibility (OpenFoodFacts) or photograph meals for calorie and macro estimates.

## Problem

People with dietary restrictions (halal, lactose-free, vegan, allergies) struggle to quickly check whether a product is safe for them. Reading ingredient labels is slow and error-prone, especially for imported products. Estimating calories from a plate without manual logging is also hard.

## Features

- Email/password auth with a personal dietary profile (halal, lactose-free, vegan, custom allergies)
- **Bottom navigation**: home, meal history, favorites, profile
- **Meal history** and **favorites** synced via Supabase (`food_entries`)
- **Theme**: light / dark / system (stored with DataStore)
- Barcode scanning with product lookup from **OpenFoodFacts** and compatibility vs your preferences (green/red with reasons)
- Meal photo analysis with a **3-tier pipeline**: **Roboflow** (cloud) → **HuggingFace** Inference API → **TensorFlow Lite** on-device (`food_model.tflite` in assets)
- Per-serving calorie and macro estimates (protein, fat, carbs)
- **Scan history** (`scan_history`) for barcode and legacy meal flows

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM, single activity |
| DI | Hilt |
| Auth & database | Supabase (Auth + PostgREST) |
| Local preferences | DataStore (theme) |
| Networking | Ktor Client |
| Camera | CameraX + ML Kit Barcode |
| AI (cloud) | Roboflow Serverless, HuggingFace Inference API |
| AI (on-device) | TensorFlow Lite |
| Product data | OpenFoodFacts API |

## Requirements

- Android Studio Ladybug (2024.2) or newer
- JDK 17
- Device or emulator **API 26+** (Android 8.0)
- Supabase project (free tier is enough)
- HuggingFace API token (Read)
- **Roboflow API key** (recommended as the first cloud tier; if missing or low confidence, the app falls back to HuggingFace, then TFLite)

## Installation

1. **Clone** the repository:

```bash
git clone https://github.com/omuraisuuuuu/DastarkhanAI.git
cd DastarkhanAI
```

2. **Supabase** — create a project at [supabase.com](https://supabase.com), then in the SQL Editor run the script in [`docs/SETUP_RU.md`](docs/SETUP_RU.md) for `profiles` and `scan_history`.

   Also create the **meal history / favorites** table (used by the History and Favorites tabs):

```sql
CREATE TABLE food_entries (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
  name TEXT NOT NULL,
  calories REAL NOT NULL,
  protein REAL,
  fat REAL,
  carbs REAL,
  eaten_at TIMESTAMPTZ NOT NULL,
  is_favorite BOOLEAN DEFAULT FALSE
);

ALTER TABLE food_entries ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users manage own food entries"
  ON food_entries FOR ALL
  USING (auth.uid() = user_id);
```

3. **API keys** — add to **`local.properties`** in the project root (no quotes around values):

```properties
SUPABASE_URL=https://YOUR_PROJECT_ID.supabase.co
SUPABASE_KEY=your_supabase_anon_key
HF_API_TOKEN=hf_your_token
ROBOFLOW_API_KEY=your_roboflow_private_api_key
```

4. Open the project in Android Studio, **Sync Gradle**, connect a device or emulator, and **Run**.

The on-device model ships as `app/src/main/assets/food_model.tflite` with `food_labels.txt`; no extra download is required for offline fallback.

## Usage

1. Register, then complete **profile setup** (body metrics and dietary toggles).
2. Use the bottom tabs: **Home** (scan barcode or meal), **History**, **Favorites**, **Profile** (theme and account).
3. **Barcode** — aim at the code; the app loads product data and checks compatibility.
4. **Meal** — take a photo; the app shows detected foods, source (Roboflow / HuggingFace / Local model), and nutrition estimates.

## Project structure

```
DastarkhanAI/
├── app/src/main/java/com/pm/foodscanner/
│   ├── data/           # APIs (OpenFoodFacts, Roboflow, HuggingFace), models, repositories
│   ├── di/             # Hilt modules
│   ├── navigation/     # Compose NavHost
│   └── ui/             # auth, main (tabs), home, barcode, meal, history, favorites, profile, profiletab, theme
├── app/src/main/assets/# food_model.tflite, food_labels.txt
├── docs/               # Setup (Russian): SETUP_RU.md
├── AUDIT.md
├── LICENSE
└── README.md
```

## Notes

- First **HuggingFace** request can take **10–20 seconds** (model cold start).
- Calorie and nutrient values are **estimates**, not medical advice.
- **OpenFoodFacts** coverage varies by region; for some local products, meal scanning may be more reliable than barcode.
- **Roboflow** uses the project’s configured model endpoint in code; ensure your key has access if you change the deployment.

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file.
