from typing import Annotated, List

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from water_my_plant.application.models import WateringEventCreate, WateringEventResponse, WateringEventUpdate
from water_my_plant.business_logic.watering_service import (
    create_watering_event,
    get_plant_watering_history,
    get_last_watering_event,
    update_watering_event,
    delete_watering_event
)
from water_my_plant.dal.base import get_db
from water_my_plant.dal.models import User
from water_my_plant.http_handlers.auth import get_current_active_user

router = APIRouter(prefix="/watering", tags=["watering"])


@router.post("", response_model=WateringEventResponse)
def record_watering(
    event: WateringEventCreate,
    db: Annotated[Session, Depends(get_db)],
    current_user: Annotated[User, Depends(get_current_active_user)]
):
    """Record a watering event for a plant."""
    watering_event = create_watering_event(db, event, current_user.id)
    if not watering_event:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Plant not found"
        )
    return watering_event


@router.get("/plant/{plant_id}", response_model=List[WateringEventResponse])
def get_watering_history(
    plant_id: str,
    db: Annotated[Session, Depends(get_db)],
    current_user: Annotated[User, Depends(get_current_active_user)]
):
    """Get watering history for a plant."""
    return get_plant_watering_history(db, plant_id, current_user.id)


@router.get("/plant/{plant_id}/last", response_model=WateringEventResponse)
def get_last_watering(
    plant_id: str,
    db: Annotated[Session, Depends(get_db)],
    current_user: Annotated[User, Depends(get_current_active_user)]
):
    """Get the last watering event for a plant."""
    last_event = get_last_watering_event(db, plant_id, current_user.id)
    if not last_event:
        raise HTTPException(
            status_code=status.HTTP_204_NO_CONTENT,
            detail="Plant not found or no watering events recorded"
        )
    return last_event


@router.put("/{event_id}", response_model=WateringEventResponse)
def update_watering(
    event_id: str,
    event_update: WateringEventUpdate,
    db: Annotated[Session, Depends(get_db)],
    current_user: Annotated[User, Depends(get_current_active_user)]
):
    """Update a watering event."""
    updated_event = update_watering_event(db, event_id, event_update, current_user.id)
    if not updated_event:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Watering event not found"
        )
    return updated_event


@router.delete("/{event_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_watering(
    event_id: str,
    db: Annotated[Session, Depends(get_db)],
    current_user: Annotated[User, Depends(get_current_active_user)]
):
    """Delete a watering event."""
    if not delete_watering_event(db, event_id, current_user.id):
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Watering event not found"
        ) 