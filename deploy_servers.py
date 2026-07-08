import sys
import os
import subprocess
import time

# Try importing paramiko, install if not present
try:
    import paramiko
except ImportError:
    print("Installing paramiko...")
    subprocess.check_call([sys.executable, "-m", "pip", "install", "paramiko"])
    import paramiko

# Server Credentials
DB_IP = "10.0.105.72"
DB_USER = "kunddata"
DB_PASS = "KuP2oMuPr-!i"

APP_IP = "10.0.104.95"
APP_USER = "kundapp"
APP_PASS = "z@fefof97raN"

LOCAL_DIR = "/Users/monish/Desktop/ParshuramKund"

def run_ssh_command(ip, username, password, command, use_sudo=False):
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    try:
        ssh.connect(ip, username=username, password=password, timeout=15)
        
        full_command = f"sudo -S {command}" if use_sudo else command
        stdin, stdout, stderr = ssh.exec_command(full_command)
        
        if use_sudo:
            stdin.write(password + "\n")
            stdin.flush()
            
        out = stdout.read().decode()
        err = stderr.read().decode()
        ssh.close()
        return True, out, err
    except Exception as e:
        return False, "", str(e)

def upload_folder_sftp(sftp, local_dir, remote_dir):
    for root, dirs, files in os.walk(local_dir):
        # Calculate relative path
        rel_path = os.path.relpath(root, local_dir)
        if rel_path == ".":
            target_dir = remote_dir
        else:
            target_dir = os.path.join(remote_dir, rel_path).replace("\\", "/")
            
        # Create remote directory
        try:
            sftp.mkdir(target_dir)
        except OSError:
            pass  # Already exists
            
        for file in files:
            local_file = os.path.join(root, file)
            remote_file = os.path.join(target_dir, file).replace("\\", "/")
            print(f"Uploading {file} -> {remote_file}")
            sftp.put(local_file, remote_file)

def deploy():
    print("==========================================================")
    print("      STARTING FULL REMOTE DEPLOYMENT TO SERVERS")
    print("==========================================================")

    # -------------------------------------------------------------------------
    # STEP 1: CONFIGURE DATABASE SERVER
    # -------------------------------------------------------------------------
    print(f"\n[1/5] Connecting to Database Server ({DB_IP}) as '{DB_USER}'...")
    
    # Check if postgresql is installed, install if missing
    success, out, err = run_ssh_command(DB_IP, DB_USER, DB_PASS, "dpkg -l | grep postgresql", use_sudo=False)
    if not success or "postgresql" not in out:
        print("PostgreSQL is not installed on Database server. Installing...")
        success, out, err = run_ssh_command(DB_IP, DB_USER, DB_PASS, "apt update && apt install postgresql postgresql-contrib -y", use_sudo=True)
        if not success:
            print(f"Failed to install PostgreSQL: {err}")
            sys.exit(1)
        print("PostgreSQL successfully installed!")

    # Set up database, user, and schema config commands
    pg_commands = [
        "sudo -u postgres psql -c \"CREATE DATABASE parshuramkund;\"",
        "sudo -u postgres psql -c \"CREATE USER mela_admin WITH PASSWORD 'SecureMelaPassword123';\"",
        "sudo -u postgres psql -c \"GRANT ALL PRIVILEGES ON DATABASE parshuramkund TO mela_admin;\""
    ]
    for cmd in pg_commands:
        run_ssh_command(DB_IP, DB_USER, DB_PASS, cmd, use_sudo=True)

    # Configure postgresql.conf and pg_hba.conf for remote connection
    config_commands = [
        "sed -i \"s/#listen_addresses = 'localhost'/listen_addresses = '*'/g\" /etc/postgresql/*/main/postgresql.conf",
        "echo \"host    parshuramkund    mela_admin    10.0.104.95/32    md5\" >> /etc/postgresql/*/main/pg_hba.conf",
        "systemctl restart postgresql"
    ]
    print("Configuring remote access configuration on Database Server...")
    for cmd in config_commands:
        run_ssh_command(DB_IP, DB_USER, DB_PASS, cmd, use_sudo=True)
    print("✔ Database server configuration complete!")

    # -------------------------------------------------------------------------
    # STEP 2: BUILD BUNDLES LOCALLY
    # -------------------------------------------------------------------------
    print("\n[2/5] Packaging Backend executable fat JAR locally...")
    os.chdir(os.path.join(LOCAL_DIR, "ParshuramKund Backend"))
    if subprocess.call(["./mvnw", "clean", "package", "-DskipTests"]) != 0:
        print("Backend packaging failed!")
        sys.exit(1)

    print("\nCompiling Frontend static assets locally...")
    os.chdir(os.path.join(LOCAL_DIR, "ParshuramKund"))
    if subprocess.call(["npm", "run", "build", "--configuration=production"]) != 0:
        print("Frontend compilation failed!")
        sys.exit(1)
    print("✔ Local builds successfully compiled!")

    # -------------------------------------------------------------------------
    # STEP 3: PREPARE APP SERVER DIRECTORIES
    # -------------------------------------------------------------------------
    print(f"\n[3/5] Connecting to App Server ({APP_IP}) as '{APP_USER}'...")
    
    # Install java, nginx, certbot
    success, out, err = run_ssh_command(APP_IP, APP_USER, APP_PASS, "apt update && apt install openjdk-17-jdk openjdk-17-jre nginx certbot python3-certbot-nginx -y", use_sudo=True)
    if not success:
        print(f"Failed to install prerequisites on App Server: {err}")
        sys.exit(1)

    # Setup directories and adjust permission ownership to let SFTP upload
    setup_commands = [
        "mkdir -p /var/www/mela/backend/aadhar-photos /var/www/mela/frontend",
        "chown -R kundapp:kundapp /var/www/mela"
    ]
    for cmd in setup_commands:
        run_ssh_command(APP_IP, APP_USER, APP_PASS, cmd, use_sudo=True)

    # -------------------------------------------------------------------------
    # STEP 4: UPLOAD BUILDS VIA SFTP
    # -------------------------------------------------------------------------
    print("\n[4/5] Uploading local built files to the App Server via SFTP...")
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    try:
        ssh.connect(APP_IP, username=APP_USER, password=APP_PASS, timeout=15)
        sftp = ssh.open_sftp()
        
        # Upload Backend JAR
        local_jar = os.path.join(LOCAL_DIR, "ParshuramKund Backend/target/ParshuramKund-0.0.1-SNAPSHOT.jar")
        remote_jar = "/var/www/mela/backend/ParshuramKund-0.0.1-SNAPSHOT.jar"
        print(f"Uploading JAR: {local_jar} -> {remote_jar}")
        sftp.put(local_jar, remote_jar)
        
        # Upload Frontend directory
        local_frontend = os.path.join(LOCAL_DIR, "ParshuramKund/dist/ParshuramKund/browser")
        remote_frontend = "/var/www/mela/frontend"
        print("Uploading frontend browser assets...")
        upload_folder_sftp(sftp, local_frontend, remote_frontend)
        
        sftp.close()
        ssh.close()
        print("✔ SFTP uploads completed successfully!")
    except Exception as e:
        print(f"SFTP Upload failed: {e}")
        sys.exit(1)

    # -------------------------------------------------------------------------
    # STEP 5: SETUP SYSTEMD & NGINX
    # -------------------------------------------------------------------------
    print("\n[5/5] Configuring systemd services and Nginx virtual host on App Server...")

    # 1. Systemd service contents
    service_content = f"""[Unit]
Description=Parshuram Kund Backend Service
After=syslog.target network.target

[Service]
User=www-data
Type=simple
WorkingDirectory=/var/www/mela/backend
ExecStart=/usr/bin/java -jar ParshuramKund-0.0.1-SNAPSHOT.jar
Environment=SPRING_DATASOURCE_URL=jdbc:postgresql://{DB_IP}:5432/parshuramkund
Environment=SPRING_DATASOURCE_USERNAME=mela_admin
Environment=SPRING_DATASOURCE_PASSWORD=SecureMelaPassword123
Environment=SPRING_MAIL_USERNAME=parshuramkundlohit@gmail.com
Environment=SPRING_MAIL_PASSWORD=iwrx\\syczt\\serrt\\slmgu
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
"""
    # 2. Nginx configuration contents
    nginx_content = f"""server {{
    listen 80;
    server_name {APP_IP};

    root /var/www/mela/frontend;
    index index.html;

    location / {{
        try_files $uri $uri/ /index.html;
    }}

    location /api/ {{
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
    }}
}}
"""
    
    # Write files temporarily on app server, then move to secure folders via sudo
    run_ssh_command(APP_IP, APP_USER, APP_PASS, f"echo '{service_content}' > /tmp/mela-backend.service")
    run_ssh_command(APP_IP, APP_USER, APP_PASS, f"echo '{nginx_content}' > /tmp/parshuramkund")
    
    post_setup_commands = [
        "mv /tmp/mela-backend.service /etc/systemd/system/",
        "mv /tmp/parshuramkund /etc/nginx/sites-available/",
        "ln -sf /etc/nginx/sites-available/parshuramkund /etc/nginx/sites-enabled/",
        "rm -f /etc/nginx/sites-enabled/default",
        "chown -R www-data:www-data /var/www/mela",
        "systemctl daemon-reload",
        "systemctl enable mela-backend",
        "systemctl restart mela-backend",
        "systemctl restart nginx"
    ]
    for cmd in post_setup_commands:
        run_ssh_command(APP_IP, APP_USER, APP_PASS, cmd, use_sudo=True)

    print("\n==========================================================")
    print("      SUCCESS: APP DEPLOYED ON BOTH PRODUCTION SERVERS!")
    print("==========================================================")

if __name__ == "__main__":
    deploy()
