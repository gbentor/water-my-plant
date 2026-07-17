import time
from typing import Type
from uuid import uuid4

from sqlalchemy.orm import Session

from water_my_plant.application.models import SensorResponse
from water_my_plant.business_logic.plant_service import get_user_plants
from water_my_plant.dal.cache_service import get_cached_moisture_data, cache_moisture_record
from water_my_plant.dal.db_service import (get_all_sensors_for_user, get_single_sensor, register_sensor_in_db,
                                           get_single_board, register_board_in_db, get_all_boards_for_user)
from water_my_plant.dal.models import Sensor, SensorType, Board

MAX_WET_VALUE = 900
MAX_DRY_VALUE = 4100


def register_sensor(db: Session, sensor_hardware_id: str, sensor_name: str, owner_id: str, sensor_type, mac_address: str) -> Sensor:
    """Create a new plant for a user."""
    db_sensor = Sensor(
        id=str(uuid4()),
        mac_address=mac_address,
        sensor_hardware_id=sensor_hardware_id,
        sensor_name=sensor_name,
        owner_id=owner_id,
        type=SensorType(sensor_type),
    )
    return register_sensor_in_db(db, db_sensor)


def get_all_sensors(db: Session, owner_id: str) -> list[SensorResponse]:
    """Get all plants for a user."""
    registered_sensors = get_all_sensors_for_user(db, owner_id)
    user_plants = get_user_plants(db, owner_id)
    sensors = []
    for sensor in registered_sensors:
        used_by = None
        for plant in user_plants:
            if sensor.id == plant.sensor_id:
                used_by = plant.name

        sensors.append(SensorResponse(
            sensor_hardware_id=sensor.sensor_hardware_id,
            sensor_name=sensor.sensor_name,
            type=sensor.type,
            used_by=used_by
        ))
    return sensors


def get_sensor(db: Session, sensor_name: str, owner_id: str, sensor_id: str | None = None) -> Sensor | None:
    """Get a plant by name for a specific user."""
    return get_single_sensor(db, owner_id=owner_id, sensor_name=sensor_name, sensor_id=sensor_id)


def get_sensor_by_id(db: Session, sensor_id: str) -> Sensor | None:
    return get_single_sensor(db, sensor_id=sensor_id)


def cache_moisture_data(device_mac_address: str, sensor_hardware_id: str, moisture_data: float) -> None:
    normalized = round((MAX_DRY_VALUE - moisture_data) / (MAX_DRY_VALUE - MAX_WET_VALUE) * 100, 2)
    moisture = max(0, min(100, normalized))
    cache_key = f"{device_mac_address}-{sensor_hardware_id}"
    cache_moisture_record(cache_key, moisture)
    print(f"Cached moisture data for sensor {device_mac_address}-{sensor_hardware_id}: {moisture}, orig_value: {moisture_data}")


def get_cached_sensor_data(db: Session, sensor_id: str) -> list[float]:
    sensor = get_single_sensor(db, sensor_id=sensor_id)
    sensor_key_prefix = f"{sensor.mac_address}-{sensor.sensor_hardware_id}"
    moisture_data = [x for x in get_cached_moisture_data(sensor_key_prefix) if x is not None]
    return moisture_data


def get_board(db: Session, mac_address: str, board_name: str, owner_id: str) -> Board | None:
    """Get a plant by name for a specific user."""
    return get_single_board(db, owner_id, mac_address=mac_address, board_name=board_name)


def get_all_boards(db: Session, owner_id: str) -> list[Type[Board]]:
    """Get all plants for a user."""
    return get_all_boards_for_user(db, owner_id) or []


def register_board(db: Session, mac_address: str, board_name: str, owner_id: str) -> Sensor:
    """Create a new plant for a user."""
    stripped_mac_address = mac_address.replace(":", "").upper()
    existing_board = get_all_boards(db, owner_id)
    if any(board.mac_address == stripped_mac_address or board.board_name == board_name for board in existing_board):
        raise ValueError("Board with this id or name already exists")

    db_board = Board(
        mac_address=stripped_mac_address,
        board_name=board_name,
        owner_id=owner_id,
    )
    return register_board_in_db(db, db_board)
