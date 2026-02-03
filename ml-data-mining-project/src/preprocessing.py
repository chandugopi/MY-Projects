"""
Preprocessing utilities for Job Application dataset.
"""
import pandas as pd
import numpy as np
from sklearn.preprocessing import StandardScaler, LabelEncoder
from sklearn.model_selection import train_test_split
from imblearn.over_sampling import SMOTE

def load_data(filepath='data/job_applications.csv'):
    """Load the job applications dataset."""
    return pd.read_csv(filepath, parse_dates=['application_date'])

def preprocess_features(df):
    """
    Preprocess features for ML modeling.
    
    Returns:
        X: Feature matrix
        y: Target vector
        feature_names: List of feature names
    """
    df = df.copy()
    
    # Drop non-predictive columns
    drop_cols = ['applicant_id', 'application_date']
    df = df.drop(columns=drop_cols)
    
    # Encode categorical variables
    categorical_cols = ['education_level', 'job_category', 'company_size']
    
    # One-hot encode
    df = pd.get_dummies(df, columns=categorical_cols, drop_first=True)
    
    # Separate features and target
    X = df.drop('hired', axis=1)
    y = df['hired']
    
    return X, y, X.columns.tolist()

def split_and_scale(X, y, test_size=0.2, random_state=42):
    """
    Split data and scale features.
    
    Returns:
        X_train, X_test, y_train, y_test, scaler
    """
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=test_size, random_state=random_state, stratify=y
    )
    
    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)
    
    return X_train_scaled, X_test_scaled, y_train, y_test, scaler

def handle_imbalance(X_train, y_train, method='smote', random_state=42):
    """
    Handle class imbalance using SMOTE or class weights.
    """
    if method == 'smote':
        smote = SMOTE(random_state=random_state)
        X_resampled, y_resampled = smote.fit_resample(X_train, y_train)
        return X_resampled, y_resampled
    else:
        # Return original for class_weight approach
        return X_train, y_train

def get_feature_importance(model, feature_names, model_type='tree'):
    """
    Extract feature importance from trained model.
    """
    if hasattr(model, 'feature_importances_'):
        importance = model.feature_importances_
    elif hasattr(model, 'coef_'):
        importance = np.abs(model.coef_[0])
    else:
        return None
    
    importance_df = pd.DataFrame({
        'feature': feature_names,
        'importance': importance
    }).sort_values('importance', ascending=False)
    
    return importance_df
