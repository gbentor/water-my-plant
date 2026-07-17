import time

from diskcache import Cache

CACHE_EXPIRY_TIME = 60 * 60 * 24 * 30  # 1 month
CACHE_SIZE_LIMIT = 5 * 1024 * 1024  # 5 MB

MOISTURE_CACHE_KEY = "moisture_value"

moisture_data_cache = Cache('moisture_data_cache', size_limit=CACHE_SIZE_LIMIT)


def cache_moisture_record(sensor_cache_key_prefix: str, moisture_value: float) -> None:
    moisture_data_cache.set(f"{sensor_cache_key_prefix}-{int(time.time())}", {MOISTURE_CACHE_KEY: moisture_value}, expire=CACHE_EXPIRY_TIME)


def get_cached_moisture_data(sensor_cache_key_prefix: str) -> list[float]:
    sensor_data = []
    for key in moisture_data_cache.iterkeys():
        if key.startswith(sensor_cache_key_prefix):
            timestamp = key.split("-", maxsplit=1)[-1]
            sensor_data.append((timestamp, moisture_data_cache.get(key, {}).get(MOISTURE_CACHE_KEY)))

    sensor_data.sort(key=lambda x: x[0])
    sensor_data = [data[1] for data in sensor_data]

    return sensor_data


def clear_sensor_moisture_cache(sensor_cache_key_prefix: str) -> None:
    for key in moisture_data_cache.iterkeys():
        if key.startswith(sensor_cache_key_prefix):
            moisture_data_cache.delete(key)
