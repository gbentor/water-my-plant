from typing import List, Optional, Type
from uuid import uuid4

from sqlalchemy.orm import Session

from water_my_plant.application.models import PlantCreate, PlantUpdate
from water_my_plant.dal.cache_service import clear_sensor_moisture_cache
from water_my_plant.dal.db_service import get_single_sensor
from water_my_plant.dal.models import Plant, User


def create_plant(db: Session, plant: PlantCreate, owner_id: str) -> Plant:
    """Create a new plant for a user."""
    db_plant = Plant(
        id=str(uuid4()),
        name=plant.name,
        type=plant.type,
        description=plant.description,
        owner_id=owner_id
    )
    db.add(db_plant)
    db.commit()
    db.refresh(db_plant)
    return db_plant


def get_plant_by_name(db: Session, name: str, owner_id: str) -> Optional[Plant]:
    """Get a plant by name for a specific user."""
    return db.query(Plant).filter(
        Plant.name == name,
        Plant.owner_id == owner_id
    ).first()


def get_plant_by_id(db: Session, plant_id: str, owner_id: str) -> Optional[Plant]:
    """Get a plant by name for a specific user."""
    return db.query(Plant).filter(
        Plant.id == plant_id,
        Plant.owner_id == owner_id
    ).first()


def get_user_plants(db: Session, owner_id: str) -> List[Type[Plant]]:
    """Get all plants for a user."""
    return db.query(Plant).filter(Plant.owner_id == owner_id).all()


def update_plant(db: Session, plant_id: str, plant_update: PlantUpdate, owner_id: str) -> Optional[Plant]:
    """Update a plant's details."""
    db_plant = db.query(Plant).filter(
        Plant.id == plant_id,
        Plant.owner_id == owner_id
    ).first()
    
    if not db_plant:
        return None

    update_data = plant_update.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(db_plant, field, value)

    if plant_update.sensor_name is not None:
        if plant_update.sensor_name != "Remove Sensor":
            # clear new sensor moisture cache
            sensor = get_single_sensor(db, owner_id=owner_id, sensor_name=plant_update.sensor_name)
            clear_sensor_moisture_cache(f"{sensor.mac_address}-{sensor.sensor_hardware_id}")
            db_plant.sensor_id = sensor.id
        else:
            db_plant.sensor_id = None

    db.commit()
    db.refresh(db_plant)
    return db_plant


def delete_plant(db: Session, plant_id: str, owner_id: str) -> bool:
    """Delete a plant."""
    db_plant = db.query(Plant).filter(
        Plant.id == plant_id,
        Plant.owner_id == owner_id
    ).first()
    
    if not db_plant:
        return False
    
    db.delete(db_plant)
    db.commit()
    return True 