from datetime import datetime
from typing import Optional, List
from pydantic import BaseModel, EmailStr, Field, ConfigDict, root_validator, validator, field_validator

from water_my_plant.dal.models import SensorType


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
    sensor_name: str | None = None


class PlantResponse(PlantBase):
    id: str
    owner_id: str
    sensor_id: str | None
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


# Sensor models
class MoistureData(BaseModel):
    device_mac_address: str
    sensor_id: int | str
    moisture: int | str


class SensorBase(BaseModel):
    sensor_hardware_id: str
    sensor_name: str
    type: SensorType

    @field_validator("type", mode='before')
    @classmethod
    def validate_sensor_type(cls, field_value: str):
        return field_value.lower()


class SensorRegister(SensorBase):
    board_mac_address: str


class BoardRegister(BaseModel):
    mac_address: str
    board_name: str


class SensorResponse(SensorBase):
    used_by: str | None = None


class MoistureDataResponse(BaseModel):
    sensor_id: str
    moisture: list[float]
