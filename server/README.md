# Water My Plant API

A FastAPI-based backend service for tracking plant watering schedules. This service allows users to manage their plants, record watering events, and track watering history.

## Features

- User authentication and authorization
- Plant management (create, read, update, delete)
- Watering event tracking
- Watering history per plant
- Fertilizer tracking

## Prerequisites

- Python 3.11+
- PostgreSQL
- Poetry (for dependency management)

## Setup

1. Clone the repository:
```bash
git clone <repository-url>
cd water-my-plant
```

2. Install dependencies:
```bash
poetry install
```

3. Set up environment variables:
Create a `.env` file in the project root with the following variables:
```
DATABASE_URL=postgresql://postgres:postgres@localhost:5432/water_my_plant
SECRET_KEY=your-secret-key-here
```

4. Create the database:
```bash
createdb water_my_plant
```

5. Set up database migrations:
```bash
# Create initial migration
alembic revision --autogenerate -m "Initial migration"

# Apply migrations
alembic upgrade head
```

6. Run the application:
```bash
poetry run uvicorn water_my_plant.main:app --reload
```

The API will be available at `http://localhost:8000`

## Database Migrations

The project uses Alembic for database migrations. Here are the common commands:

```bash
# Create a new migration after model changes
alembic revision --autogenerate -m "Description of changes"

# Apply pending migrations
alembic upgrade head

# Roll back one migration
alembic downgrade -1

# Roll back to a specific revision
alembic downgrade <revision_id>

# Show current migration version
alembic current

# Show migration history
alembic history
```

## API Documentation

Once the application is running, you can access:
- Swagger UI documentation: `http://localhost:8000/docs`
- ReDoc documentation: `http://localhost:8000/redoc`

## API Endpoints

### Authentication
- `POST /auth/register` - Register a new user
- `POST /auth/token` - Login and get access token

### Plants
- `POST /plants` - Create a new plant
- `GET /plants` - Get all plants for the current user
- `GET /plants/{plant_id}` - Get a specific plant
- `PUT /plants/{plant_id}` - Update plant details
- `DELETE /plants/{plant_id}` - Delete a plant

### Watering Events
- `POST /watering` - Record a watering event
- `GET /watering/plant/{plant_id}` - Get watering history for a plant
- `GET /watering/plant/{plant_id}/last` - Get the last watering event for a plant

## Development

### Running Tests
```bash
poetry run pytest
```

### Code Style
The project uses:
- Black for code formatting
- isort for import sorting
- mypy for type checking

Run the formatters:
```bash
poetry run black .
poetry run isort .
poetry run mypy .
```

## License

MIT 