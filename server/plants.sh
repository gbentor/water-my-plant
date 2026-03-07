#!/bin/bash

# Ensure background processes are killed when you stop the script (Ctrl+C)
trap "kill 0" EXIT

echo "--- Starting Plant Server and Ngrok ---"

# Command 1: Navigate and Run Python
(cd /home/gbentor/Projects/water-my-plant/server/ && poetry run python src/water_my_plant/main.py) &

# Command 2: Start Ngrok
ngrok http 8000 &

# Keep the script alive so you can see the logs
wait
