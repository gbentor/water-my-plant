from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from water_my_plant.dal.base import Base, engine
from water_my_plant.http_handlers import auth, plants, watering, sensors

# Custom logging configuration
LOG_CONFIG = {
    "version": 1,
    "disable_existing_loggers": False,
    "formatters": {
        "default": {
            "()": "uvicorn.logging.DefaultFormatter",
            "fmt": "%(levelprefix)s %(asctime)s - %(message)s",
            "datefmt": "%Y-%m-%d %H:%M:%S",  # Human-readable format
        },
    },
    "handlers": {
        "default": {
            "formatter": "default",
            "class": "logging.StreamHandler",
            "stream": "ext://sys.stderr",
        },
    },
    "loggers": {
        "uvicorn": {"handlers": ["default"], "level": "INFO"},
    },
}

# Create database tables
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="Water My Plant API",
    description="API for tracking plant watering schedules",
    version="1.0.0"
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, replace with specific origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(auth.router)
app.include_router(plants.router)
app.include_router(watering.router)
app.include_router(sensors.router)


@app.get("/")
async def root():
    """Root endpoint."""
    return {
        "message": "Welcome to Water My Plant API",
        "docs_url": "/docs",
        "redoc_url": "/redoc"
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("water_my_plant.main:app", host="0.0.0.0", port=8000, reload=True, log_config=LOG_CONFIG)
