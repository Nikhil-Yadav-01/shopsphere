// MongoDB initialization script for ShopSphere
// This script creates the necessary databases and collections

// Switch to admin database
db = db.getSiblingDB('admin');

// Create database user (already done in env vars, but ensuring it exists)
db.createUser({
  user: 'shopsphere',
  pwd: 'mongodb',
  roles: [
    { role: 'readWrite', db: 'shopsphere' },
    { role: 'dbAdmin', db: 'shopsphere' }
  ]
});

// Switch to shopsphere database
db = db.getSiblingDB('shopsphere');

// Create collections for products
db.createCollection('products', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['sku', 'name', 'price'],
      properties: {
        sku: {
          bsonType: 'string',
          description: 'Unique product SKU'
        },
        name: {
          bsonType: 'string',
          description: 'Product name'
        },
        description: {
          bsonType: 'string'
        },
        price: {
          bsonType: 'decimal',
          description: 'Product price'
        },
        currency: {
          bsonType: 'string',
          enum: ['USD', 'EUR', 'GBP']
        },
        categoryId: {
          bsonType: 'objectId'
        },
        status: {
          bsonType: 'string',
          enum: ['ACTIVE', 'INACTIVE', 'DRAFT']
        },
        images: {
          bsonType: 'array',
          items: {
            bsonType: 'string'
          }
        },
        attributes: {
          bsonType: 'object'
        }
      }
    }
  }
});

// Create indexes for products
db.products.createIndex({ sku: 1 }, { unique: true });
db.products.createIndex({ name: 1 });
db.products.createIndex({ categoryId: 1 });
db.products.createIndex({ status: 1 });

// Create collections for categories
db.createCollection('categories', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['name'],
      properties: {
        name: {
          bsonType: 'string'
        },
        description: {
          bsonType: 'string'
        },
        parentId: {
          bsonType: 'objectId'
        }
      }
    }
  }
});

// Create indexes for categories
db.categories.createIndex({ name: 1 });

// Create reviews collection
db.createCollection('reviews', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['productId', 'userId', 'rating'],
      properties: {
        productId: {
          bsonType: 'objectId'
        },
        userId: {
          bsonType: 'objectId'
        },
        rating: {
          bsonType: 'int',
          minimum: 1,
          maximum: 5
        },
        comment: {
          bsonType: 'string'
        },
        createdAt: {
          bsonType: 'date'
        }
      }
    }
  }
});

// Create indexes for reviews
db.reviews.createIndex({ productId: 1 });
db.reviews.createIndex({ userId: 1 });
db.reviews.createIndex({ createdAt: -1 });

// Create events collection for audit logging
db.createCollection('events', {
  capped: true,
  size: 67108864,
  max: 100000
});

db.events.createIndex({ timestamp: -1 });
db.events.createIndex({ entityType: 1, entityId: 1 });

print('MongoDB initialization complete');
