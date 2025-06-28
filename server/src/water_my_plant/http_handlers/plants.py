from typing import Annotated, List

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from water_my_plant.application.models import PlantCreate, PlantResponse, PlantUpdate
from water_my_plant.business_logic.plant_service import (
    create_plant,
    delete_plant,
    get_plant_by_name,
    get_user_plants,
    update_plant,
    get_plant_by_id
)
from water_my_plant.dal.base import get_db
from water_my_plant.dal.models import User
from water_my_plant.http_handlers.auth import get_current_active_user

router = APIRouter(prefix="/plants", tags=["plants"])


@router.post("", response_model=PlantResponse)
def create_new_plant(
    plant: PlantCreate,
    db: Annotated[Session, Depends(get_db)],
    current_user: Annotated[User, Depends(get_current_active_user)]
):
    """Create a new plant."""
    # Check if plant name already exists for this user
    existing_plant = get_plant_by_name(db, plant.name, current_user.id)
    if existing_plant:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Plant with this name already exists"
        )
    return create_plant(db=db, plant=plant, owner_id=current_user.id)


@router.get("", response_model=List[PlantResponse])
def get_plants(
    db: Annotated[Session, Depends(get_db)],
    current_user: Annotated[User, Depends(get_current_active_user)]
):
    """Get all plants for the current user."""
    return get_user_plants(db, current_user.id)


@router.get("/{plant_id}", response_model=PlantResponse)
def get_plant(
    plant_id: str,
    db: Annotated[Session, Depends(get_db)],
    current_user: Annotated[User, Depends(get_current_active_user)]
):
    """Get a specific plant."""
    plant = get_plant_by_id(db, plant_id, current_user.id)
    if not plant:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Plant not found"
        )
    return plant


@router.put("/{plant_id}", response_model=PlantResponse)
def update_plant_details(
    plant_id: str,
    plant_update: PlantUpdate,
    db: Annotated[Session, Depends(get_db)],
    current_user: Annotated[User, Depends(get_current_active_user)]
):
    """Update plant details."""
    updated_plant = update_plant(db, plant_id, plant_update, current_user.id)
    if not updated_plant:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Plant not found"
        )
    return updated_plant


@router.delete("/{plant_id}", status_code=status.HTTP_204_NO_CONTENT)
def remove_plant(
    plant_id: str,
    db: Annotated[Session, Depends(get_db)],
    current_user: Annotated[User, Depends(get_current_active_user)]
):
    """Delete a plant."""
    if not delete_plant(db, plant_id, current_user.id):
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Plant not found"
        ) 