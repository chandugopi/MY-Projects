"""
Dataset generator for Job Application Success Predictor.
Generates synthetic job application data for ML modeling.
"""
import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import random

def generate_job_application_data(n_samples=1000, random_state=42):
    """Generate synthetic job application dataset."""
    np.random.seed(random_state)
    random.seed(random_state)
    
    # Feature definitions
    education_levels = ['High School', 'Bachelor', 'Master', 'PhD']
    education_weights = [0.15, 0.50, 0.30, 0.05]
    
    job_categories = ['Software Engineer', 'Data Scientist', 'Product Manager', 
                     'UX Designer', 'DevOps Engineer', 'Business Analyst']
    
    company_sizes = ['Startup', 'Small', 'Medium', 'Large', 'Enterprise']
    
    # Generate features
    data = {
        'applicant_id': [f'APP{i:04d}' for i in range(1, n_samples + 1)],
        'years_experience': np.clip(np.random.exponential(5, n_samples), 0, 25).round(1),
        'education_level': np.random.choice(education_levels, n_samples, p=education_weights),
        'skills_match_score': np.random.beta(5, 2, n_samples).round(2),
        'job_category': np.random.choice(job_categories, n_samples),
        'company_size': np.random.choice(company_sizes, n_samples),
        'salary_expectation': np.random.normal(95000, 25000, n_samples).clip(40000, 200000).round(-3),
        'has_referral': np.random.choice([0, 1], n_samples, p=[0.75, 0.25]),
        'num_previous_applications': np.random.poisson(2, n_samples),
        'days_since_last_job': np.random.exponential(180, n_samples).clip(0, 1000).round(0),
        'interview_score': np.random.normal(70, 15, n_samples).clip(0, 100).round(1),
        'technical_test_score': np.random.normal(75, 20, n_samples).clip(0, 100).round(1),
        'cultural_fit_score': np.random.normal(72, 12, n_samples).clip(0, 100).round(1),
    }
    
    df = pd.DataFrame(data)
    
    # Encode education for success calculation
    education_map = {'High School': 0, 'Bachelor': 1, 'Master': 2, 'PhD': 3}
    df['education_encoded'] = df['education_level'].map(education_map)
    
    # Calculate success probability based on features
    success_prob = (
        0.15 * (df['years_experience'] / 25) +
        0.10 * (df['education_encoded'] / 3) +
        0.20 * df['skills_match_score'] +
        0.05 * df['has_referral'] +
        0.15 * (df['interview_score'] / 100) +
        0.20 * (df['technical_test_score'] / 100) +
        0.10 * (df['cultural_fit_score'] / 100) +
        0.05 * np.random.uniform(0, 1, n_samples)  # Random noise
    )
    
    # Add some non-linearity
    success_prob = np.clip(success_prob + 0.1 * (success_prob > 0.5), 0, 1)
    
    # Generate binary outcome
    df['hired'] = (np.random.uniform(0, 1, n_samples) < success_prob).astype(int)
    
    # Drop encoded column
    df = df.drop('education_encoded', axis=1)
    
    # Add application date
    base_date = datetime(2024, 1, 1)
    df['application_date'] = [base_date + timedelta(days=random.randint(0, 365)) 
                              for _ in range(n_samples)]
    
    return df

if __name__ == '__main__':
    # Generate dataset
    df = generate_job_application_data(n_samples=2000)
    
    # Save to CSV
    df.to_csv('data/job_applications.csv', index=False)
    
    # Print summary
    print("Dataset Generated!")
    print(f"Shape: {df.shape}")
    print(f"Target distribution:\n{df['hired'].value_counts(normalize=True)}")
    print(f"\nFeatures:\n{df.dtypes}")
