#!/bin/bash

# ============================================
# Database Initialization Script
# Timetable Back
# ============================================

# === SETTINGS ===
DB_HOST="localhost"
DB_USER="postgres"
DB_PASS="8574"
DB_NAME="timetable"

export PGPASSWORD=$DB_PASS

echo "=== Database Initialization ==="

# Execute SQL script for table creation
psql -h $DB_HOST -U $DB_USER -d $DB_NAME -f src/main/resources/db/init.sql
if [ $? -ne 0 ]; then
    echo "Error creating tables!"
    exit 1
fi

echo "Tables created successfully"

# Execute script with initial data
if [ -f "src/main/resources/db/data.sql" ]; then
    echo "Executing data.sql..."
    psql -h $DB_HOST -U $DB_USER -d $DB_NAME -f src/main/resources/db/data.sql
fi

echo ""
echo "=== Database ready ==="
