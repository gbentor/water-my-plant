from datetime import datetime, timezone
from typing import Optional

from sqlalchemy import Boolean, Column, DateTime, ForeignKey, String, Text
from sqlalchemy.orm import relationship

from water_my_plant.dal.base import Base


def local_now():
    """Get current datetime in local timezone."""
    return datetime.now().astimezone()


class User(Base):
    __tablename__ = "users"

    id = Column(String, primary_key=True)
    username = Column(String, unique=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime(timezone=True), default=local_now)
    updated_at = Column(DateTime(timezone=True), default=local_now, onupdate=local_now)

    plants = relationship("Plant", back_populates="owner", cascade="all, delete-orphan")


class Plant(Base):
    __tablename__ = "plants"

    id = Column(String, primary_key=True)
    name = Column(String, nullable=False)
    type = Column(String, nullable=False)
    description = Column(Text, nullable=True)
    owner_id = Column(String, ForeignKey("users.id"), nullable=False)
    created_at = Column(DateTime(timezone=True), default=local_now)
    updated_at = Column(DateTime(timezone=True), default=local_now, onupdate=local_now)

    owner = relationship("User", back_populates="plants")
    watering_events = relationship("WateringEvent", back_populates="plant", cascade="all, delete-orphan")

    __table_args__ = (
        # Ensure plant names are unique per user
        {"sqlite_autoincrement": True},
    )


class WateringEvent(Base):
    __tablename__ = "watering_events"

    id = Column(String, primary_key=True)
    plant_id = Column(String, ForeignKey("plants.id"), nullable=False)
    watered_at = Column(DateTime(timezone=True), default=local_now)
    fertilizer_used = Column(Boolean, default=False)
    notes = Column(Text, nullable=True)
    created_at = Column(DateTime(timezone=True), default=local_now)

    plant = relationship("Plant", back_populates="watering_events") 