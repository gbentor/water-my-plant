import os

from pydantic_settings import BaseSettings
from typing import Optional




class Settings(BaseSettings):
    # Database settings
    DATABASE_URL: str = os.getenv("db_url")

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