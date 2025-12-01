#!/bin/bash
set -e

# Update system packages
yum update -y
yum install -y docker curl wget

# Start Docker
systemctl start docker
systemctl enable docker

# Create directory for MongoDB data
mkdir -p /data/mongodb
chmod 777 /data/mongodb

# Run MongoDB container
docker run -d \
  --name mongodb \
  --restart unless-stopped \
  -e MONGO_INITDB_ROOT_USERNAME=${mongo_username} \
  -e MONGO_INITDB_ROOT_PASSWORD=${mongo_password} \
  -e MONGO_INITDB_DATABASE=shopsphere \
  -p 27017:27017 \
  -v /data/mongodb:/data/db \
  mongo:7-alpine \
  mongod --auth

# Wait for MongoDB to start
sleep 10

# Initialize database and collections
docker exec mongodb mongosh -u ${mongo_username} -p ${mongo_password} --authenticationDatabase admin << 'MONGO_INIT'
use shopsphere;

// Create collections with validation
db.createCollection('products', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['sku', 'name', 'price'],
      properties: {
        sku: { bsonType: 'string' },
        name: { bsonType: 'string' },
        description: { bsonType: 'string' },
        price: { bsonType: 'decimal' },
        currency: { bsonType: 'string' },
        categoryId: { bsonType: 'objectId' },
        status: { bsonType: 'string' },
        images: { bsonType: 'array' },
        attributes: { bsonType: 'object' }
      }
    }
  }
});

// Create indexes
db.products.createIndex({ sku: 1 }, { unique: true });
db.products.createIndex({ name: 1 });
db.products.createIndex({ categoryId: 1 });
db.products.createIndex({ status: 1 });

db.createCollection('categories');
db.categories.createIndex({ name: 1 });

db.createCollection('reviews');
db.reviews.createIndex({ productId: 1 });
db.reviews.createIndex({ userId: 1 });
db.reviews.createIndex({ createdAt: -1 });

db.createCollection('events', { capped: true, size: 67108864, max: 100000 });
db.events.createIndex({ timestamp: -1 });
db.events.createIndex({ entityType: 1, entityId: 1 });

print('MongoDB initialization complete');
MONGO_INIT

echo "MongoDB initialized successfully"
