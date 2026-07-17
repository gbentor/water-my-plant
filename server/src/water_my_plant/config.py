import os

from pydantic_settings import BaseSettings
from typing import Optional


DB_PASSWORD = os.getenv("db_password")

class Settings(BaseSettings):
    # Database settings
    DATABASE_URL: str = f"postgresql://postgres.bewdkxqpgndjplvfqjha:{DB_PASSWORD}@aws-1-ap-northeast-2.pooler.supabase.com:5432/postgres"

    # JWT settings
    SECRET_KEY: str = "your-secret-key-here"  # Change this in production
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 180
    
    # Application settings
    APP_NAME: str = "Water My Plant"
    DEBUG: bool = False
    
    class Config:
        env_file = ".env"


settings = Settings() 