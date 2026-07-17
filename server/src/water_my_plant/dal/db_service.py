from typing import Type

from sqlalchemy.orm import Session

from water_my_plant.dal.models import Sensor, Board


_NOT_PROVIDED = object()


def register_sensor_in_db(db: Session, sensor: Sensor) -> Sensor:
    """Create a new plant for a user."""
    db.add(sensor)
    db.commit()
    db.refresh(sensor)
    return sensor


def get_single_sensor(
        db: Session, *,
        owner_id: str | None = _NOT_PROVIDED,
        sensor_id: str | None = _NOT_PROVIDED,
        sensor_name: str | None = _NOT_PROVIDED,
        sensor_hardware_id: str | None = _NOT_PROVIDED,
        mac_address: str | None = _NOT_PROVIDED,
) -> Sensor | None:
    """Get a plant by name for a specific user."""
    filters = []
    if owner_id is not _NOT_PROVIDED:
        filters.append(Sensor.owner_id == owner_id)
    if sensor_id is not _NOT_PROVIDED:
        filters.append(Sensor.id == sensor_id)
    if sensor_name is not _NOT_PROVIDED:
        filters.append(Sensor.sensor_name == sensor_name)
    if sensor_hardware_id is not _NOT_PROVIDED:
        filters.append(Sensor.sensor_hardware_id == sensor_hardware_id)
    if mac_address is not _NOT_PROVIDED:
        filters.append(Sensor.mac_address == mac_address)

    return db.query(Sensor).filter(
        *filters
    ).first()


def get_all_sensors_for_user(db: Session, owner_id: str) -> list[Type[Sensor]]:
    """Get all plants for a user."""
    return db.query(Sensor).filter(Sensor.owner_id == owner_id).all()


def get_single_board(db: Session, owner_id: str, *, mac_address: str | None = None, board_name: str | None = None) -> Board | None:
    """Get a plant by name for a specific user."""
    filters = []
    if mac_address:
        filters.append(Board.mac_address == mac_address)
    if board_name:
        filters.append(Board.board_name == board_name)

    return db.query(Board).filter(
        Board.owner_id == owner_id,
        *filters
    ).first()


def register_board_in_db(db: Session, board: Board) -> Board:
    """Create a new plant for a user."""
    db.add(board)
    db.commit()
    db.refresh(board)
    return board


def get_all_boards_for_user(db: Session, owner_id: str) -> list[Type[Sensor]]:
    """Get all plants for a user."""
    return db.query(Board).filter(Board.owner_id == owner_id).all()
