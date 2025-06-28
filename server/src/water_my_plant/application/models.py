from datetime import datetime
from typing import Optional, List
from pydantic import BaseModel, EmailStr, Field, ConfigDict


# User models
class UserBase(BaseModel):
    username: str


class UserCreate(UserBase):
    password: str


class UserLogin(BaseModel):
    username: str
    password: str


class UserResponse(UserBase):
    id: str
    is_active: bool
    created_at: datetime
    updated_at: datetime

    model_config = ConfigDict(
        from_attributes=True,
        json_encoders={
            datetime: lambda dt: dt.isoformat() + "Z" if dt.tzinfo is None else dt.isoformat()
        }
    )


# Plant models
class PlantBase(BaseModel):
    name: str
    type: str
    description: Optional[str] = None


class PlantCreate(PlantBase):
    pass


class PlantUpdate(BaseModel):
    name: Optional[str] = None
    type: Optional[str] = None
    description: Optional[str] = None


class PlantResponse(PlantBase):
    id: str
    owner_id: str
    created_at: datetime
    updated_at: datetime

    model_config = ConfigDict(
        from_attributes=True,
        json_encoders={
            datetime: lambda dt: dt.isoformat() + "Z" if dt.tzinfo is None else dt.isoformat()
        }
    )


# Watering event models
class WateringEventBase(BaseModel):
    fertilizer_used: bool = False
    notes: Optional[str] = None


class WateringEventCreate(WateringEventBase):
    plant_id: str


class WateringEventUpdate(WateringEventBase):
    pass


class WateringEventResponse(WateringEventBase):
    id: str
    plant_id: str
    watered_at: datetime
    created_at: datetime

    model_config = ConfigDict(
        from_attributes=True,
        json_encoders={
            datetime: lambda dt: dt.isoformat() + "Z" if dt.tzinfo is None else dt.isoformat()
        }
    )


# Token models
class Token(BaseModel):
    access_token: str
    token_type: str


class TokenData(BaseModel):
    username: Optional[str] = None 