from datetime import datetime
from enum import StrEnum

from sqlalchemy import Boolean, Column, DateTime, ForeignKey, String, Text, UniqueConstraint
from sqlalchemy.dialects.postgresql import ENUM
from sqlalchemy.orm import relationship

from water_my_plant.dal.base import Base


def local_now():
    """Get current datetime in local timezone."""
    return datetime.now().astimezone()


class SensorType(StrEnum):
    """Enum for sensor types."""
    MOISTURE = "moisture"


class User(Base):
    __tablename__ = "users"

    id = Column(String, primary_key=True)
    username = Column(String, unique=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime(timezone=True), default=local_now)
    updated_at = Column(DateTime(timezone=True), default=local_now, onupdate=local_now)

    plants = relationship("Plant", back_populates="owner", cascade="all, delete-orphan")
    sensors = relationship("Sensor", back_populates="owner", cascade="all, delete-orphan")
    boards = relationship("Board", back_populates="owner", cascade="all, delete-orphan")


class Plant(Base):
    __tablename__ = "plants"

    id = Column(String, primary_key=True)
    name = Column(String, nullable=False)
    type = Column(String, nullable=False)
    description = Column(Text, nullable=True)
    owner_id = Column(String, ForeignKey("users.id"), nullable=False)
    sensor_id = Column(String, ForeignKey("sensors.id"), nullable=True, unique=True)
    created_at = Column(DateTime(timezone=True), default=local_now)
    updated_at = Column(DateTime(timezone=True), default=local_now, onupdate=local_now)

    owner = relationship("User", back_populates="plants")
    watering_events = relationship("WateringEvent", back_populates="plant", cascade="all, delete-orphan")
    sensor = relationship("Sensor", back_populates="plant", uselist=False)

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


class Sensor(Base):
    __tablename__ = "sensors"

    id = Column(String, primary_key=True)
    mac_address = Column(String, ForeignKey("boards.mac_address"), nullable=False)
    sensor_hardware_id = Column(String, nullable=False)
    sensor_name = Column(String, nullable=False)
    owner_id = Column(String, ForeignKey("users.id"), nullable=False)
    type = Column(ENUM(SensorType, name="sensor_type"), nullable=False)
    created_at = Column(DateTime(timezone=True), default=local_now)

    owner = relationship("User", back_populates="sensors")
    plant = relationship("Plant", back_populates="sensor", uselist=False)
    boards = relationship("Board", back_populates="sensors")

    __table_args__ = (
        UniqueConstraint('mac_address', 'sensor_hardware_id', name='uq_mac_sensor_hw'),
        UniqueConstraint('owner_id', 'sensor_name', name='uq_owner_sensor_name'),
    )

    def __repr__(self):
        return f"<Sensor(id={self.id}, sensor_name={self.sensor_name}, type={self.type}, owner_id={self.owner_id}, sensor_hardware_id={self.sensor_hardware_id}, mac_address={self.mac_address})>"


class Board(Base):
    __tablename__ = "boards"

    mac_address = Column(String, nullable=False, primary_key=True)
    board_name = Column(String,  nullable=False)
    owner_id = Column(String, ForeignKey("users.id"), nullable=False)
    created_at = Column(DateTime(timezone=True), default=local_now)

    owner = relationship("User", back_populates="boards")
    sensors = relationship("Sensor", back_populates="boards")

    __table_args__ = (
        UniqueConstraint('owner_id', 'board_name', name='uq_owner_board_name'),
    )
