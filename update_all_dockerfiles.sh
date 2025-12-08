#!/bin/bash

# Find all Dockerfiles and update them
find f:/AppStorage/shopsphere/services -name "Dockerfile" | while read -r file; do
    echo "Processing: $file"
    
    # Create backup
    cp "$file" "$file.bak"
    
    # Replace localhost with ${MACHINE_IP}
    sed -i 's|http://localhost:|http://${MACHINE_IP}:|g' "$file"
    
    # Add ARG MACHINE_IP after ARG JAR_FILE if not already present
    if ! grep -q "ARG MACHINE_IP" "$file"; then
        sed -i '/^ARG JAR_FILE=/a ARG MACHINE_IP=127.0.0.1' "$file"
    fi
    
    echo "Updated: $file"
done

echo "All Dockerfiles updated!"
