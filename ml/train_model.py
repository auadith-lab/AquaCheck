"""Train a Random Forest on the Water Potability dataset and export it for the Android app.

Real training (Kaggle "Water Potability" CSV, target column "Potability"):
    python train_model.py water_potability.csv

DEMO model on synthetic data (NOT real, only so the app runs without the CSV):
    python train_model.py --demo
"""
import json
import os
import sys

import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.impute import SimpleImputer
from sklearn.metrics import accuracy_score
from sklearn.model_selection import train_test_split

FEATURES = ["ph", "Hardness", "Solids", "Chloramines", "Sulfate",
            "Conductivity", "Organic_carbon", "Trihalomethanes", "Turbidity"]
TARGET = "Potability"
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                   "..", "app", "src", "main", "assets", "rf_model.json")


def synthetic_demo_data(n=3000, seed=0):
    """Random numbers with a made-up labelling rule. Meaningless for real water."""
    rng = np.random.default_rng(seed)
    df = pd.DataFrame({
        "ph": rng.normal(7.1, 1.5, n).clip(0, 14),
        "Hardness": rng.normal(196, 33, n).clip(47, 323),
        "Solids": rng.normal(22000, 8800, n).clip(320, 61000),
        "Chloramines": rng.normal(7.1, 1.6, n).clip(0.3, 13),
        "Sulfate": rng.normal(333, 41, n).clip(129, 481),
        "Conductivity": rng.normal(426, 80, n).clip(181, 753),
        "Organic_carbon": rng.normal(14.3, 3.3, n).clip(2, 28),
        "Trihalomethanes": rng.normal(66, 16, n).clip(0, 124),
        "Turbidity": rng.normal(3.97, 0.78, n).clip(1.4, 6.7),
    })
    score = (-abs(df["ph"] - 7) - df["Turbidity"] / 3 - df["Chloramines"] / 6
             + rng.normal(0, 1.0, n))
    df[TARGET] = (score > np.quantile(score, 0.6)).astype(int)
    for col in ["ph", "Sulfate", "Trihalomethanes"]:  # mimic missing values
        df.loc[rng.random(n) < 0.15, col] = np.nan
    return df


def export_forest(model, is_demo, test_acc):
    trees = []
    for est in model.estimators_:
        t = est.tree_
        v = t.value[:, 0, :]
        p1 = v[:, 1] / v.sum(axis=1)  # probability of Potability = 1 at each node
        trees.append({
            "f": t.feature.tolist(),
            "t": t.threshold.tolist(),
            "l": t.children_left.tolist(),
            "r": t.children_right.tolist(),
            "p": [round(float(x), 5) for x in p1],
        })
    data = {"is_demo": is_demo, "test_accuracy": test_acc,
            "feature_names": FEATURES, "trees": trees}
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, "w") as fh:
        json.dump(data, fh, separators=(",", ":"))
    return data


def predict_from_json(data, X):
    """Same traversal the Android app uses (float32 inputs, '<=' goes left)."""
    X = np.asarray(X, dtype=np.float32).astype(np.float64)
    out = []
    for x in X:
        total = 0.0
        for tr in data["trees"]:
            n = 0
            while tr["l"][n] != -1:
                n = tr["l"][n] if x[tr["f"][n]] <= tr["t"][n] else tr["r"][n]
            total += tr["p"][n]
        out.append(total / len(data["trees"]))
    return np.array(out)


def main():
    args = sys.argv[1:]
    if not args:
        sys.exit(__doc__)
    is_demo = args[0] == "--demo"
    df = synthetic_demo_data() if is_demo else pd.read_csv(args[0])

    X, y = df[FEATURES], df[TARGET]
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y)

    # Missing values: median of the TRAINING set only (no test-set leakage).
    imp = SimpleImputer(strategy="median").fit(X_train)
    X_train, X_test = imp.transform(X_train), imp.transform(X_test)

    model = RandomForestClassifier(n_estimators=100, max_depth=12,
                                   min_samples_leaf=2, random_state=42, n_jobs=-1)
    model.fit(X_train, y_train)
    acc = float(accuracy_score(y_test, model.predict(X_test)))
    print(f"Rows: {len(df)} | train: {len(y_train)} | test: {len(y_test)}")
    print(f"Test accuracy: {acc:.4f}" + ("   (DEMO / synthetic data)" if is_demo else ""))

    # Demo accuracy is on made-up data, so it is not stored for display.
    data = export_forest(model, is_demo, None if is_demo else round(acc, 4))

    # Check that the exported JSON gives the same answers as scikit-learn.
    ref = model.predict_proba(X_test)[:, 1]
    got = predict_from_json(data, X_test)
    print(f"Export check: max |sklearn - json| = {np.abs(ref - got).max():.6f}, "
          f"label agreement = {np.mean((ref > 0.5) == (got > 0.5)):.4f}")
    print(f"Saved {os.path.normpath(OUT)} ({os.path.getsize(OUT) / 1e6:.2f} MB)")


if __name__ == "__main__":
    main()
