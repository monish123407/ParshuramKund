#!/bin/bash
# Fully Automated Local Build and Deploy Script

# Terminal Color Codes
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}====================================================${NC}"
echo -e "${BLUE}   PARSHURAM KUND PORTAL - AUTOMATED DEPLOYMENT      ${NC}"
echo -e "${BLUE}====================================================${NC}"

# Target Server Configuration
SERVER_IP="10.0.104.95"
TARGET_DIR="/var/www/mela"

# Prompt for Username
read -p "Enter SSH Username for server $SERVER_IP (default: ubuntu): " SSH_USER
SSH_USER=${SSH_USER:-ubuntu}

# Check for private key file
read -p "Enter path to SSH Private Key file (.pem / .key) if any (press Enter for password authentication): " KEY_PATH

SSH_OPT=""
if [ -n "$KEY_PATH" ]; then
  if [ -f "$KEY_PATH" ]; then
    SSH_OPT="-i $KEY_PATH"
    echo -e "${GREEN}Using private key: $KEY_PATH${NC}"
  else
    echo -e "${RED}Key file not found at $KEY_PATH. Proceeding with password...${NC}"
  fi
fi

# Step 1: Build the backend project
echo -e "\n${BLUE}[Step 1/4] Packaging Spring Boot Backend JAR...${NC}"
cd "/Users/monish/Desktop/ParshuramKund/ParshuramKund Backend"
if ./mvnw clean package -DskipTests; then
  echo -e "${GREEN}✔ Backend packaged successfully!${NC}"
else
  echo -e "${RED}✘ Backend compilation failed. Exiting...${NC}"
  exit 1
fi

# Step 2: Build the frontend project
echo -e "\n${BLUE}[Step 2/4] Compiling Angular Frontend Static Assets...${NC}"
cd "/Users/monish/Desktop/ParshuramKund/ParshuramKund"
npm install
if npm run build --configuration=production; then
  echo -e "${GREEN}✔ Frontend compiled successfully!${NC}"
else
  echo -e "${RED}✘ Frontend compilation failed. Exiting...${NC}"
  exit 1
fi

# Step 3: Copy Backend JAR to server
echo -e "\n${BLUE}[Step 3/4] Uploading Backend JAR to $SERVER_IP...${NC}"
scp $SSH_OPT "/Users/monish/Desktop/ParshuramKund/ParshuramKund Backend/target/ParshuramKund-0.0.1-SNAPSHOT.jar" "${SSH_USER}@${SERVER_IP}:${TARGET_DIR}/backend/"
if [ $? -eq 0 ]; then
  echo -e "${GREEN}✔ Backend JAR uploaded successfully!${NC}"
else
  echo -e "${RED}✘ Backend upload failed. Make sure directory permissions are configured. Exiting...${NC}"
  exit 1
fi

# Step 4: Copy Frontend Assets to server
echo -e "\n${BLUE}[Step 4/4] Uploading Frontend Static Assets to $SERVER_IP...${NC}"
scp -r $SSH_OPT "/Users/monish/Desktop/ParshuramKund/ParshuramKund/dist/ParshuramKund/browser/"* "${SSH_USER}@${SERVER_IP}:${TARGET_DIR}/frontend/"
if [ $? -eq 0 ]; then
  echo -e "${GREEN}✔ Frontend assets uploaded successfully!${NC}"
  echo -e "\n${GREEN}====================================================${NC}"
  echo -e "${GREEN}          DEPLOYMENT COMPLETED SUCCESSFULLY!        ${NC}"
  echo -e "${GREEN}====================================================${NC}"
  echo -e "Next Steps on Server $SERVER_IP:"
  echo -e "1. Restart backend service:  ${BLUE}sudo systemctl restart mela-backend${NC}"
  echo -e "2. Restart Nginx:            ${BLUE}sudo systemctl restart nginx${NC}"
else
  echo -e "${RED}✘ Frontend upload failed. Exiting...${NC}"
  exit 1
fi
