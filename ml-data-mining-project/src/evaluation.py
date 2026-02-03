"""
Model evaluation utilities.
"""
import pandas as pd
import numpy as np
from sklearn.metrics import (
    accuracy_score, precision_score, recall_score, f1_score,
    roc_auc_score, confusion_matrix, classification_report,
    roc_curve, precision_recall_curve
)
import matplotlib.pyplot as plt
import seaborn as sns

def evaluate_model(model, X_test, y_test, model_name='Model'):
    """
    Comprehensive model evaluation.
    
    Returns metrics dictionary and prints report.
    """
    y_pred = model.predict(X_test)
    y_prob = model.predict_proba(X_test)[:, 1] if hasattr(model, 'predict_proba') else None
    
    metrics = {
        'model': model_name,
        'accuracy': accuracy_score(y_test, y_pred),
        'precision': precision_score(y_test, y_pred),
        'recall': recall_score(y_test, y_pred),
        'f1': f1_score(y_test, y_pred)
    }
    
    if y_prob is not None:
        metrics['roc_auc'] = roc_auc_score(y_test, y_prob)
    
    print(f"\n{'='*50}")
    print(f"{model_name} Results")
    print(f"{'='*50}")
    print(classification_report(y_test, y_pred))
    
    return metrics

def plot_confusion_matrix(y_test, y_pred, model_name='Model'):
    """Plot confusion matrix heatmap."""
    cm = confusion_matrix(y_test, y_pred)
    
    plt.figure(figsize=(8, 6))
    sns.heatmap(cm, annot=True, fmt='d', cmap='Blues',
                xticklabels=['Not Hired', 'Hired'],
                yticklabels=['Not Hired', 'Hired'])
    plt.title(f'{model_name} - Confusion Matrix')
    plt.ylabel('Actual')
    plt.xlabel('Predicted')
    plt.tight_layout()
    return plt.gcf()

def plot_roc_curves(models_results, X_test, y_test):
    """Plot ROC curves for multiple models."""
    plt.figure(figsize=(10, 8))
    
    for name, model in models_results.items():
        if hasattr(model, 'predict_proba'):
            y_prob = model.predict_proba(X_test)[:, 1]
            fpr, tpr, _ = roc_curve(y_test, y_prob)
            auc = roc_auc_score(y_test, y_prob)
            plt.plot(fpr, tpr, label=f'{name} (AUC = {auc:.3f})')
    
    plt.plot([0, 1], [0, 1], 'k--', label='Random')
    plt.xlabel('False Positive Rate')
    plt.ylabel('True Positive Rate')
    plt.title('ROC Curves Comparison')
    plt.legend()
    plt.tight_layout()
    return plt.gcf()

def create_comparison_table(metrics_list):
    """Create model comparison DataFrame."""
    df = pd.DataFrame(metrics_list)
    df = df.set_index('model')
    return df.round(4)

def plot_feature_importance(importance_df, top_n=15, title='Feature Importance'):
    """Plot feature importance bar chart."""
    plt.figure(figsize=(10, 8))
    
    top_features = importance_df.head(top_n)
    
    sns.barh(y=top_features['feature'], x=top_features['importance'], 
             palette='viridis')
    plt.xlabel('Importance')
    plt.ylabel('Feature')
    plt.title(title)
    plt.gca().invert_yaxis()
    plt.tight_layout()
    return plt.gcf()
