# 💧 AquaCheck — AI Water Potability Predictor

[![GitHub Pages](https://img.shields.io/badge/Live%20Demo-GitHub%20Pages-0284c7?style=flat&logo=github)](https://auadith-lab.github.io/AquaCheck/)
[![GitHub Repo](https://img.shields.io/badge/GitHub-Repository-181717?style=flat&logo=github)](https://github.com/auadith-lab/AquaCheck)

An intelligent, real-time water quality analyzer and potability predictor powered by an on-device **Random Forest Classifier** machine learning model. Available both as a modern **Native Android Application** (Jetpack Compose + Material 3) and a responsive **Standalone Web Application** (100% in-browser, zero server dependency).

🌐 **Live Web Application**: [https://auadith-lab.github.io/AquaCheck/](https://auadith-lab.github.io/AquaCheck/)  
📦 **GitHub Repository**: [https://github.com/auadith-lab/AquaCheck](https://github.com/auadith-lab/AquaCheck)

---

## 📱 Key Features

### 🤖 Native Android Application (`/app`)
- **Modern Jetpack Compose UI**: Built with Material 3 design, edge-to-edge layout, responsive cards, and clean azure/ocean theme.
- **On-Device ML Inference**: Pure Kotlin decision tree traversal (100 trees) without bulky native runtimes (TensorFlow Lite, ONNX, etc.).
- **Interactive Water Quality Parameters**: Input validation and constraints across all 9 standard water quality metrics.
- **Ensemble Consensus Probability**: Displays tree voting percentage and clear potability verdict (**Predicted Potable** / **Predicted Non-Potable**).
- **Embedded Model Asset**: Ships with `app/src/main/assets/rf_model.json` for instant offline inference anywhere without internet access.

### 🌐 Standalone Web Application (`/web`)
- **Universal Browser Compatibility**: Runs in Google Chrome, Microsoft Edge, Firefox, and Safari with zero configuration.
- **Client-Side Tree Inference**: Directly traverses the same 100 decision trees in pure JavaScript with identical mathematical output to scikit-learn.
- **Real-Time Reactive Sliders**: Sliders and numeric inputs with instant live prediction updates.
- **One-Tap Sample Presets**:
  - 🚰 **Municipal Tap Water**: Balanced mineral content meeting international drinking standards.
  - ⛰️ **Natural Mountain Spring**: Pristine natural water with low turbidity and organic matter.
  - 🏭 **Industrial Contaminated**: High TDS, heavy chloramines, elevated sulfates, and acidity.
  - 🍂 **Stagnant Turbid Pond**: High turbidity, organic carbon, and alkaline imbalance.
- **WHO / EPA Reference Standards Checklist**: Live indicators displaying whether each laboratory reading falls within international drinking water guidelines.
- **Zero Server Setup**: Launch immediately with `run_web_app.bat` or open `web/index.html` directly.

---

## 📂 Project Structure

```text
AquaCheck/
├── app/
│   ├── src/main/
│   │   ├── assets/
│   │   │   └── rf_model.json         # Exported 100-tree Random Forest model (1.1 MB)
│   │   ├── java/com/aquacheck/
│   │   │   ├── MainActivity.kt       # Jetpack Compose UI & Water Potability Dashboard
│   │   │   └── RandomForest.kt       # Pure Kotlin decision tree traversal engine
│   │   ├── res/                      # Android resources, strings & themes
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── ml/
│   ├── train_model.py                # Model training and JSON export script (scikit-learn)
│   └── ...                           # (Optional) Kaggle water_potability.csv dataset
├── web/
│   ├── index.html                    # Responsive glassmorphism web dashboard
│   └── model.js                      # Pure JavaScript model structure for web inference
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── local.properties                  # Android SDK path configuration
├── run_web_app.bat                   # One-click launcher for the interactive web app
├── install_apk.bat                   # ADB script to install APK to connected Android device
├── index.html                        # Root web preview shortcut
└── README.md                         # Comprehensive documentation
```

---

## 🧪 Laboratory Features & WHO / EPA Guidelines

The model evaluates **9 primary physical and chemical water quality parameters**:

| Feature | Unit | Typical Range | WHO / EPA Safe Guideline | Significance |
| :--- | :--- | :--- | :--- | :--- |
| **pH** | — | `0.0 – 14.0` | `6.5 – 8.5` | Acidic water corrodes pipes; alkaline water causes scaling. |
| **Hardness** | mg/L | `0 – 350` | `< 300 mg/L` | Calcium and magnesium mineral content. |
| **Solids (TDS)** | ppm | `0 – 60,000` | `< 500 – 1,000 ppm` | Total dissolved solids; high levels impart bitter taste. |
| **Chloramines** | ppm | `0.0 – 15.0` | `< 4.0 mg/L` | Common municipal disinfectant; high levels cause skin/eye irritation. |
| **Sulfate** | mg/L | `0 – 500` | `< 250 – 400 mg/L` | Naturally occurring minerals; excess causes laxative effects. |
| **Conductivity** | μS/cm | `0 – 800` | `< 400 – 700 μS/cm` | Electrical conductivity indicative of dissolved ionic contaminants. |
| **Organic Carbon** | ppm | `0.0 – 30.0` | `< 2.0 – 4.0 ppm` | Total Organic Carbon (TOC) indicating decaying matter or pathogens. |
| **Trihalomethanes** | μg/L | `0 – 140` | `< 80 μg/L` | Disinfection byproducts formed when chlorine reacts with organic matter. |
| **Turbidity** | NTU | `0.0 – 8.0` | `< 5.0 NTU` (ideal `< 1.0`) | Water cloudiness caused by suspended colloids, silt, and microorganisms. |

---

## 🧠 Machine Learning Model & Inference

### Model Architecture
- **Algorithm**: Random Forest Classifier (`n_estimators=100`, `max_depth=12`, `min_samples_leaf=2`)
- **Missing Value Handling**: Median imputation computed strictly from the training split (zero data leakage)
- **JSON Serialization**: Compact tree structure encoding arrays:
  - `f`: Feature indices for each decision node
  - `t`: Splitting threshold values
  - `l`: Left child node pointer (`-1` indicates leaf)
  - `r`: Right child node pointer (`-1` indicates leaf)
  - `p`: Leaf probability distribution for class 1 (Potable)
- **Mathematical Parity**: Traversal uses `float32` comparison logic matching scikit-learn's exact numerical boundaries across Python, Kotlin, and JavaScript.

---

## 🚀 Getting Started

### Option 1: Instant Web Dashboard (Recommended)
1. Double-click `run_web_app.bat` or open `web/index.html` in your browser.
2. Select any quick preset or adjust parameter sliders to see immediate predictions and guideline compliance.

### Option 2: Android App
1. Build the APK using Gradle:
   ```cmd
   .\gradlew.bat assembleDebug
   ```
2. The output APK will be generated at `app\build\outputs\apk\debug\app-debug.apk`.
3. Connect your Android phone with USB Debugging enabled, and run:
   ```cmd
   .\install_apk.bat
   ```

### Option 3: Retraining the Model (`ml/train_model.py`)
To train on the Kaggle Water Potability dataset:
```bash
python ml/train_model.py water_potability.csv
```
Or to re-generate the synthetic demo baseline:
```bash
python ml/train_model.py --demo
```
This automatically updates `app/src/main/assets/rf_model.json`.

---

## ⚠️ Educational Disclaimer
This software is designed for educational and machine learning demonstration purposes. Algorithm predictions cannot prove chemical purity or biological safety. Real-world drinking water safety requires certified laboratory analysis in compliance with local public health authorities and WHO standards.
