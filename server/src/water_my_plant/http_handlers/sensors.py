from http import HTTPStatus
from typing import Annotated

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from starlette.responses import JSONResponse

from water_my_plant.application.models import MoistureData, SensorRegister, MoistureDataResponse, BoardRegister
from water_my_plant.business_logic.sensors_service import get_sensor, register_sensor, get_all_sensors, \
    get_sensor_by_id, cache_moisture_data, get_cached_sensor_data, register_board, get_all_boards
from water_my_plant.dal.base import get_db
from water_my_plant.dal.models import User
from water_my_plant.http_handlers.security import get_current_active_user

router = APIRouter(prefix="/sensors", tags=["sensors"])

@router.get("/", status_code=HTTPStatus.OK)
def get_sensors(
    db: Annotated[Session, Depends(get_db)],
    current_user: Annotated[User, Depends(get_current_active_user)]
):
    """Get all sensors for a user."""
    return get_all_sensors(db, current_user.id)


@router.get("/{sensor_id}", status_code=HTTPStatus.OK)
def get_single_sensor(
    sensor_id: str,
    db: Annotated[Session, Depends(get_db)],
):
    """Get all sensors for a user."""
    sensor = get_sensor_by_id(db, sensor_id)
    return sensor


@router.post("/register", status_code=HTTPStatus.OK)
def register_moisture_sensor(
    sensor: SensorRegister,
    db: Annotated[Session, Depends(get_db)],
    current_user: Annotated[User, Depends(get_current_active_user)]
):
    existing_sensor = get_sensor(db, sensor.sensor_name, current_user.id)
    if existing_sensor:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Sensor with this id and name already exists"
        )
    return register_sensor(db=db, sensor_hardware_id=sensor.sensor_hardware_id, sensor_name=sensor.sensor_name, owner_id=current_user.id, sensor_type=sensor.type, mac_address=sensor.board_mac_address)


@router.post("/boards/register", status_code=HTTPStatus.OK)
def register_new_board(
    board: BoardRegister,
    db: Annotated[Session, Depends(get_db)],
    current_user: Annotated[User, Depends(get_current_active_user)]
):
    try:
        return register_board(db=db, mac_address=board.mac_address, board_name=board.board_name,
                              owner_id=current_user.id)
    except ValueError:
        return JSONResponse(
            status_code=status.HTTP_409_CONFLICT,
            content={"message": "Board with this MAC address or name already exists"}
        )


@router.get("/boards/all", status_code=HTTPStatus.OK)
def get_boards(
    db: Annotated[Session, Depends(get_db)],
    current_user: Annotated[User, Depends(get_current_active_user)]
):
    return get_all_boards(db, current_user.id)


@router.post("/moisture", status_code=HTTPStatus.OK)
def receive_moisture_data(
    moisture_data: MoistureData,
):
    cache_moisture_data(device_mac_address=moisture_data.device_mac_address, sensor_hardware_id=moisture_data.sensor_id, moisture_data=moisture_data.moisture)


@router.get("/moisture/{sensor_id}", response_model=MoistureDataResponse)
def get_all_sensor_moisture_data(
    sensor_id: str,
    db: Annotated[Session, Depends(get_db)],
):
    sensor_data = get_cached_sensor_data(db=db, sensor_id=sensor_id)
    return MoistureDataResponse(sensor_id=sensor_id, moisture=sensor_data)
