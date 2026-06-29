#!/bin/bash

# Load environment variables from .env file if it exists
if [ -f .env ]; then
  echo "Loading environment variables from .env..."
  set -a
  source .env
  set +a
fi

echo "========================================================"
echo "      Starting Parshuram Kund Mela 2027 Application    "
echo "========================================================"

# Function to clear ports
kill_port() {
  local port=$1
  local pid=$(lsof -t -i:$port)
  if [ -n "$pid" ]; then
    echo "Port $port is currently in use by process $pid. Stopping it..."
    kill -9 $pid 2>/dev/null
    sleep 1.5
  fi
}

# Free ports 8081 and 4200
kill_port 8081
kill_port 4200

# Cleanup on exit (Ctrl+C)
cleanup() {
  echo -e "\nStopping backend and frontend servers..."
  kill $BACKEND_PID $FRONTEND_PID 2>/dev/null
  exit 0
}
trap cleanup SIGINT SIGTERM

# Start Backend
echo "Starting Spring Boot Backend (Port 8080)..."
cd "ParshuramKund Backend" || exit 1
./mvnw spring-boot:run &
BACKEND_PID=$!
cd ..

# Start Frontend
echo "Starting Angular Frontend (Port 4200)..."
cd "ParshuramKund" || exit 1
npm start &
FRONTEND_PID=$!
cd ..

echo "--------------------------------------------------------"
echo "✓ Backend running (PID: $BACKEND_PID)"
echo "✓ Frontend running (PID: $FRONTEND_PID)"
echo "Visit the application at: http://localhost:4200"
echo "Press Ctrl+C to stop both servers."
echo "--------------------------------------------------------"

# Wait for both background processes
wait $BACKEND_PID $FRONTEND_PID
