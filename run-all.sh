#!/bin/bash

# DialGenie - Complete Application Startup Script (Local Machine - Linux/Mac/WSL)
# Runs entire application - all services in one command

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

# Arrays to track PIDs
declare -a PIDS

print_header() {
    echo ""
    echo "╔════════════════════════════════════════════════════════╗"
    echo "║       DialGenie - Local Application Startup            ║"
    echo "╚════════════════════════════════════════════════════════╝"
    echo ""
}

print_step() {
    echo -e "${BLUE}[$1/8]${NC} $2"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

# Header
print_header

# Check prerequisites
print_step "1" "Checking prerequisites..."

for cmd in java mvn node npm python3; do
    if ! command -v $cmd &> /dev/null; then
        print_error "$cmd not found"
        exit 1
    fi
done
print_success "All prerequisites installed"
echo ""

# Create logs directory
print_step "2" "Creating logs directory..."
mkdir -p logs
print_success "Logs directory ready"
echo ""

# Initialize database
print_step "3" "Initializing database..."
if [ -f "backend/shared/src/main/resources/schema.sql" ]; then
    sqlite3 dialgenie.db < backend/shared/src/main/resources/schema.sql
    print_success "Database created with test data"
else
    print_error "Schema file not found"
    exit 1
fi
echo ""

# Build shared module
print_step "4" "Building shared module..."
cd backend/shared
mvn clean install -q -DskipTests
cd ../..
print_success "Shared module compiled"
echo ""

# Start backend services
print_step "5" "Starting backend services..."

start_service() {
    local service=$1
    local port=$2
    (cd backend/$service && mvn spring-boot:run > ../../logs/${service}.log 2>&1 &)
    local pid=$!
    PIDS+=($pid)
    echo -e "  ${GREEN}✓${NC} $service (port $port) [PID: $pid]"
    sleep 2
}

start_service "auth-service" 8081
start_service "lead-service" 8082
start_service "campaign-service" 8083
start_service "call-service" 8084
start_service "api-gateway" 8080
print_success "Backend services started"
echo ""

# Start AI service
print_step "6" "Starting AI service..."
(cd ai-service && pip install -r requirements.txt -q && python3 -m uvicorn src.main.python.main:app --port 8085 > ../logs/ai-service.log 2>&1 &)
local ai_pid=$!
PIDS+=($ai_pid)
echo -e "  ${GREEN}✓${NC} AI Service (port 8085) [PID: $ai_pid]"
print_success "AI service started"
echo ""

# Start frontend
print_step "7" "Starting frontend..."
(cd frontend && npm install -q && npm run dev > ../logs/frontend.log 2>&1 &)
local frontend_pid=$!
PIDS+=($frontend_pid)
echo -e "  ${GREEN}✓${NC} Frontend (port 3000) [PID: $frontend_pid]"
print_success "Frontend started"
echo ""

# Display summary
print_step "8" "Application startup complete!"
echo ""

echo "╔════════════════════════════════════════════════════════╗"
echo -e "║          ${GREEN}✓ DialGenie is Running!${NC}              ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""

echo -e "${YELLOW}🌐 Service URLs:${NC}"
echo "  Frontend:           ${GREEN}http://localhost:3000${NC}"
echo "  API Gateway:        ${GREEN}http://localhost:8080${NC}"
echo "  Auth Service:       ${GREEN}http://localhost:8081${NC}"
echo "  Lead Service:       ${GREEN}http://localhost:8082${NC}"
echo "  Campaign Service:   ${GREEN}http://localhost:8083${NC}"
echo "  Call Service:       ${GREEN}http://localhost:8084${NC}"
echo "  AI Service:         ${GREEN}http://localhost:8085${NC}"
echo ""

echo -e "${YELLOW}👤 Default Login Credentials:${NC}"
echo "  Email:    admin@dialgenie.com"
echo "  Password: admin@123"
echo ""

echo -e "${YELLOW}📋 Active Services:${NC}"
for pid in "${PIDS[@]}"; do
    if kill -0 $pid 2>/dev/null; then
        echo "  ✓ Service running (PID: $pid)"
    fi
done
echo ""

echo -e "${YELLOW}📁 View logs:${NC} tail -f logs/*.log"
echo ""

echo -e "${YELLOW}⏹️  To stop all services:${NC} Press Ctrl+C"
echo ""

# Trap Ctrl+C to kill all background processes
trap 'echo ""; echo "Shutting down..."; kill "${PIDS[@]}" 2>/dev/null; echo "✓ All services stopped"; exit 0' INT

# Wait for any process to exit
wait
