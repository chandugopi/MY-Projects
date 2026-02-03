# ML/Data Mining Project - Job Application Success Predictor

A machine learning project demonstrating data mining techniques, classification models, and clustering for predicting job application outcomes.

## 📊 Project Overview

This project analyzes job application data to:
- Predict application success probability
- Identify key factors affecting hiring decisions
- Segment job seekers into clusters for targeted recommendations

## ✨ Features

| Analysis Type | Techniques Used |
|--------------|-----------------|
| **EDA** | Pandas profiling, Seaborn visualizations |
| **Classification** | Logistic Regression, Random Forest, XGBoost |
| **Clustering** | KMeans, Elbow Method |
| **Imbalance Handling** | SMOTE, Class Weights |
| **Feature Analysis** | Feature Importance, SHAP Values |

## 🔧 Technology Stack

| Library | Purpose |
|---------|---------|
| Python 3.11+ | Core language |
| Pandas | Data manipulation |
| NumPy | Numerical computing |
| Scikit-learn | ML models & preprocessing |
| XGBoost | Gradient boosting |
| Matplotlib/Seaborn | Visualizations |
| SHAP | Model explainability |

## 📁 Project Structure

```
ml-data-mining-project/
├── notebooks/
│   ├── 01_eda.ipynb              # Exploratory Data Analysis
│   └── 02_modeling.ipynb         # Model Training & Evaluation
├── data/
│   └── job_applications.csv      # Dataset
├── models/
│   └── best_model.pkl           # Saved model
├── src/
│   ├── preprocessing.py         # Data preprocessing
│   └── evaluation.py            # Model evaluation utils
└── README.md
```

## 🚀 Quick Start

```bash
# Create virtual environment
python -m venv venv
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Run notebooks
jupyter notebook
```

## 📈 Model Performance

| Model | Accuracy | Precision | Recall | F1-Score |
|-------|----------|-----------|--------|----------|
| Logistic Regression | 78% | 0.76 | 0.73 | 0.74 |
| Random Forest | 85% | 0.84 | 0.82 | 0.83 |
| XGBoost | 87% | 0.86 | 0.85 | 0.85 |

## 🎯 Key Insights

1. **Years of experience** is the strongest predictor
2. **Education level** has moderate impact
3. **Skills match** significantly affects success
4. **3 distinct clusters** of applicants identified

## 📋 Interview Discussion Points

1. **Why these models?**
   - Logistic Regression: Baseline, interpretable coefficients
   - Random Forest: Handles non-linearity, feature importance
   - XGBoost: State-of-the-art performance

2. **How did you handle imbalanced data?**
   - SMOTE for oversampling minority class
   - Class weights for cost-sensitive learning

3. **What metrics did you use?**
   - Precision-Recall due to class imbalance
   - ROC-AUC for threshold selection
