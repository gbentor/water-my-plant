from typing import List, Optional
from uuid import uuid4

from sqlalchemy.orm import Session

from water_my_plant.application.models import WateringEventCreate, WateringEventUpdate
from water_my_plant.dal.models import WateringEvent, Plant, local_now


def create_watering_event(db: Session, event: WateringEventCreate, owner_id: str) -> Optional[WateringEvent]:
    """Create a new watering event for a plant."""
    # Verify plant belongs to user
    plant = db.query(Plant).filter(
        Plant.id == event.plant_id,
        Plant.owner_id == owner_id
    ).first()
    
    if not plant:
        return None
    
    db_event = WateringEvent(
        id=str(uuid4()),
        plant_id=event.plant_id,
        fertilizer_used=event.fertilizer_used,
        notes=event.notes,
        watered_at=local_now()
    )
    db.add(db_event)
    db.commit()
    db.refresh(db_event)
    return db_event


def get_plant_watering_history(db: Session, plant_id: str, owner_id: str) -> List[WateringEvent]:
    """Get watering history for a plant."""
    # Verify plant belongs to user
    plant = db.query(Plant).filter(
        Plant.id == plant_id,
        Plant.owner_id == owner_id
    ).first()
    
    if not plant:
        return []
    
    return db.query(WateringEvent).filter(
        WateringEvent.plant_id == plant_id
    ).order_by(WateringEvent.watered_at.desc()).all()


def get_last_watering_event(db: Session, plant_id: str, owner_id: str) -> Optional[WateringEvent]:
    """Get the last watering event for a plant."""
    # Verify plant belongs to user
    plant = db.query(Plant).filter(
        Plant.id == plant_id,
        Plant.owner_id == owner_id
    ).first()
    
    if not plant:
        return None
    
    return db.query(WateringEvent).filter(
        WateringEvent.plant_id == plant_id
    ).order_by(WateringEvent.watered_at.desc()).first()


def update_watering_event(db: Session, event_id: str, event_update: WateringEventUpdate, owner_id: str) -> Optional[WateringEvent]:
    """Update a watering event."""
    # Get the event and verify plant belongs to user
    event = db.query(WateringEvent).join(Plant).filter(
        WateringEvent.id == event_id,
        Plant.owner_id == owner_id
    ).first()
    
    if not event:
        return None
    
    # Update fields
    update_data = event_update.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(event, field, value)
    
    db.commit()
    db.refresh(event)
    return event


def delete_watering_event(db: Session, event_id: str, owner_id: str) -> bool:
    """Delete a watering event."""
    # Get the event and verify plant belongs to user
    event = db.query(WateringEvent).join(Plant).filter(
        WateringEvent.id == event_id,
        Plant.owner_id == owner_id
    ).first()
    
    if not event:
        return False
    
    db.delete(event)
    db.commit()
    return True 