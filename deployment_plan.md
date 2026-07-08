# Secure Deployment Plan & Guide - Ubuntu Server

This guide outlines the production deployment plan for the Parshuram Kund Mela 2027 application (Spring Boot Backend + Angular Frontend + PostgreSQL Database) on an Ubuntu Server environment.

---

## 1. System Architecture

```mermaid
graph TD
    Client[Web Browser / Mobile Client] -->|HTTPS :443| Nginx[Nginx Reverse Proxy]
    Nginx -->|Static Files| Angular[Angular Static Assets]
    Nginx -->|Proxy Pass /api| SpringBoot[Spring Boot Backend :8081]
    SpringBoot -->|JDBC| PostgreSQL[PostgreSQL Database :5432]
    SpringBoot -->|SMTP| Gmail[Gmail SMTP Server]
    SpringBoot -->|HTTPS API| Gemini[Google Gemini API]
```

---

## 2. Prerequisites & Server Setup

Log in to your Ubuntu Server and update system packages:
```bash
sudo apt update && sudo apt upgrade -y
```

### Install Required Software
Install **Java 17 (OpenJDK)**, **Node.js 20+**, **PostgreSQL**, **Nginx**, and **Git**:
```bash
# Install OpenJDK 17
sudo apt install openjdk-17-jdk openjdk-17-jre -y

# Install Node.js (via NodeSource LTS)
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs

# Install PostgreSQL, Nginx, and Git
sudo apt install postgresql postgresql-contrib nginx git -y
```

Verify installations:
```bash
java -version
node -v
npm -v
psql --version
nginx -v
```

---

## 3. Database Configurations (Run on Database Server: 10.0.105.72)

1. Log in to your database server `10.0.105.72` and access the PostgreSQL command line:
   ```bash
   sudo -i -u postgres psql
   ```

2. Create the database, user, and grant privileges:
   ```sql
   CREATE DATABASE parshuramkund;
   CREATE USER mela_admin WITH PASSWORD 'SecureMelaPassword123';
   GRANT ALL PRIVILEGES ON DATABASE parshuramkund TO mela_admin;
   \q
   ```

3. **Configure Remote Connections**:
   By default, PostgreSQL only listens locally. Enable it to listen to connections from the backend application server `10.0.104.95`:
   
   - Edit the main configuration file:
     ```bash
     sudo nano /etc/postgresql/14/main/postgresql.conf
     ```
     Find `listen_addresses` and change it to:
     ```ini
     listen_addresses = '*'
     ```
   
   - Edit the Client Authentication configuration file:
     ```bash
     sudo nano /etc/postgresql/14/main/pg_hba.conf
     ```
     Append a line at the end to allow connections from the backend server IP:
     ```text
     # Allow connections from application server
     host    parshuramkund    mela_admin    10.0.104.95/32    md5
     ```

4. **Restart PostgreSQL**:
   ```bash
   sudo systemctl restart postgresql
   ```

---

## 4. Build and Package Application

It is recommended to package the applications locally and copy them via SCP/SFTP to save server resources.

### A. Backend Packaging (Spring Boot)
In the `ParshuramKund Backend` directory:
```bash
# Package backend fat jar
./mvnw clean package -DskipTests
```
The executable jar `ParshuramKund-0.0.1-SNAPSHOT.jar` is generated in `target/`. Copy this jar to `/var/www/mela/backend/` on the server.

### B. Frontend Packaging (Angular)
In the `ParshuramKund` directory:
```bash
# Install dependencies
npm install

# Build static production assets
npm run build --configuration=production
```
The static files compile inside the `dist/ParshuramKund/browser/` directory. Copy this entire folder to `/var/www/mela/frontend/` on the server.

---

## 5. Backend Service Deployment (Systemd)

To run the Spring Boot jar as a background service that auto-starts on boot:

1. Create a service config file:
   ```bash
   sudo nano /etc/systemd/system/mela-backend.service
   ```

2. Paste the configuration below (adapt environment secrets as needed):
   ```ini
   [Unit]
   Description=Parshuram Kund Backend Service
   After=syslog.target network.target postgresql.service

   [Service]
   User=www-data
   Type=simple
   WorkingDirectory=/var/www/mela/backend
   ExecStart=/usr/bin/java -jar ParshuramKund-0.0.1-SNAPSHOT.jar
   
   # Production Environmental Configurations
   Environment=SPRING_DATASOURCE_URL=jdbc:postgresql://10.0.105.72:5432/parshuramkund
   Environment=SPRING_DATASOURCE_USERNAME=mela_admin
   Environment=SPRING_DATASOURCE_PASSWORD=SecureMelaPassword123
   Environment=GEMINI_API_KEY=your_gemini_api_key_here
   
   # SMTP Credentials
   Environment=SPRING_MAIL_USERNAME=parshuramkundlohit@gmail.com
   Environment=SPRING_MAIL_PASSWORD=iwrx yczt errt lmgu
   
   SuccessExitStatus=143
   Restart=on-failure
   RestartSec=10

   [Install]
   WantedBy=multi-user.target
   ```

3. Configure folder permissions:
   ```bash
   sudo mkdir -p /var/www/mela/backend/aadhar-photos
   sudo chown -R www-data:www-data /var/www/mela
   ```

4. Enable and start the backend service:
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable mela-backend
   sudo systemctl start mela-backend
   ```

---

## 6. Nginx Reverse Proxy Setup

Nginx will serve the static frontend assets directly and proxy `/api/` calls to the Spring Boot backend service on port `8081`.

1. Create Nginx site configuration:
   ```bash
   sudo nano /etc/nginx/sites-available/parshuramkund
   ```

2. Add virtual host configuration (replace `yourdomain.com` with your domain/public IP):
   ```nginx
    server {
        listen 80;
        server_name 10.0.104.95;

       root /var/www/mela/frontend;
       index index.html;

       # Serve static Angular files
       location / {
           try_files $uri $uri/ /index.html;
       }

       # Proxy API requests to Spring Boot
       location /api/ {
           proxy_pass http://localhost:8081/api/;
           proxy_http_version 1.1;
           proxy_set_header Upgrade $http_upgrade;
           proxy_set_header Connection 'upgrade';
           proxy_set_header Host $host;
           proxy_cache_bypass $http_upgrade;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
           
           # Allow up to 10MB file uploads (Aadhaar uploads)
           client_max_body_size 10M;
       }

       error_page 404 /index.html;
   }
   ```

3. Enable the site and restart Nginx:
   ```bash
   sudo ln -s /etc/nginx/sites-available/parshuramkund /etc/nginx/sites-enabled/
   sudo rm /etc/nginx/sites-enabled/default
   sudo nginx -t
   sudo systemctl restart nginx
   ```

---

## 7. SSL Security Certificate (HTTPS Encryption)

To protect sensitive Aadhaar numbers and photo uploads with HTTPS encryption:

### Option A: Self-Signed SSL Certificate (For Local Private LAN Setup)
Since `10.0.104.95` is a private IP address, you can generate a self-signed SSL certificate directly on the Nginx app server:

1. Generate key and certificate:
   ```bash
   sudo mkdir -p /etc/nginx/ssl
   sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
     -keyout /etc/nginx/ssl/mela.key \
     -out /etc/nginx/ssl/mela.crt \
     -subj "/C=IN/ST=Arunachal Pradesh/L=Tezu/O=District Administration Lohit/CN=10.0.104.95"
   ```

2. Update your Nginx configuration `/etc/nginx/sites-available/parshuramkund` to listen on port 443 with SSL:
   ```nginx
   server {
       listen 80;
       server_name 10.0.104.95;
       return 301 https://$host$request_uri;
   }

   server {
       listen 443 ssl;
       server_name 10.0.104.95;

       ssl_certificate /etc/nginx/ssl/mela.crt;
       ssl_certificate_key /etc/nginx/ssl/mela.key;

       root /var/www/mela/frontend;
       index index.html;

       location / {
           try_files $uri $uri/ /index.html;
       }

       location /api/ {
           proxy_pass http://localhost:8081/api/;
           proxy_http_version 1.1;
           proxy_set_header Upgrade $http_upgrade;
           proxy_set_header Connection 'upgrade';
           proxy_set_header Host $host;
           proxy_cache_bypass $http_upgrade;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
           client_max_body_size 10M;
       }
   }
   ```

3. Restart Nginx:
   ```bash
   sudo systemctl restart nginx
   ```

---

### Option B: Let's Encrypt SSL (For Public Domain Setup)
If your app server is accessible from the internet and mapped to a public domain (e.g. `mela.lohit.gov.in` pointing to your server's public IP):

1. Install Certbot:
   ```bash
   sudo apt install certbot python3-certbot-nginx -y
   ```

2. Obtain and apply the certificate:
   ```bash
   sudo certbot --nginx -d mela.lohit.gov.in
   ```
   *Follow the interactive prompt to automatically redirect all HTTP traffic to HTTPS.*

---

## 8. Backup & Maintenance Recommendations

> [!WARNING]
> Regularly back up the PostgreSQL database and Aadhaar upload files.

- **PostgreSQL Database Backup**:
  ```bash
  pg_dump -U mela_admin parshuramkund > /var/backups/mela_db_$(date +%F).sql
  ```
- **Aadhaar Uploads Backup**:
  ```bash
  tar -czf /var/backups/aadhar_photos_$(date +%F).tar.gz /var/www/mela/backend/aadhar-photos/
  ```
