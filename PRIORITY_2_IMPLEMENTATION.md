# Priority 2 — Customer Engagement Services — Implementation Status

**Started**: December 2, 2025  
**Target**: Week 2 (35-40 hours)  
**Goal**: Enable product discovery, reviews, fraud prevention, recommendations

---

## ✅ Services Scaffolded (Phase 1)

### ✅ 1. Coupon Service (SCAFFOLDED)

**Files Created**: 11 files  
**Components**:
- ✅ Entities: `Coupon.java`, `CouponRedemption.java`
- ✅ Repositories: `CouponRepository.java`, `CouponRedemptionRepository.java`
- ✅ Controller: `CouponController.java` (POST, GET, PUT, DELETE, POST /validate)
- ✅ Service Interface: `CouponService.java`
- ✅ Service Implementation: `CouponServiceImpl.java` (full validation logic)
- ✅ DTOs: `CouponResponse.java`, `CouponValidationRequest.java`, `CouponValidationResponse.java`
- ✅ Mapper: `CouponMapper.java` (MapStruct)
- ✅ Exceptions: `CouponNotFoundException.java`, `CouponExpiredException.java`, `CouponLimitExceededException.java`, `InvalidCouponException.java`

**Logic Implemented**:
- Coupon validation with expiry, usage limits, per-user limits
- Discount calculation (PERCENTAGE and FIXED_AMOUNT types)
- Redemption tracking with status (APPLIED, CANCELLED)
- Order value validation
- Category-based applicability

**Status**: Ready for event producers/consumers and tests

---

### ⏳ 2. Review Service (TODO)

**Components to Create**:
- Entities: `Review.java`, `ReviewReport.java`, `ReviewRating.java`, `ReviewModeration.java`
- Repositories (4)
- Controller with 5 endpoints: POST /create, GET /{id}, PUT /{id}, DELETE, POST /report
- Service + ServiceImpl with moderation pipeline
- ModerationService (profanity + spam detection)
- DTOs (5): ReviewCreateRequest, ReviewResponse, ReviewListResponse, ReviewReportRequest, ReviewStatsResponse
- Mapper
- Exceptions (3): ReviewNotFoundException, UnauthorizedReviewException, DuplicateReviewException

**Kafka Events**:
- Consumer: OrderDeliveredConsumer
- Producer: ReviewEventProducer

---

### ⏳ 3. Fraud Service (TODO)

**Components to Create**:
- Entities: `FraudCase.java`, `FraudRuleLog.java`, `FraudScore.java`
- Repositories (2)
- Controller: GET /cases, GET /cases/{id}, PUT /cases/{id}/status, POST /rules
- FraudScoringService (0-100 score based on 7 risk factors)
- RulesEngineService (DSL-based rule execution)
- DTOs (4): FraudCheckRequest, FraudScoreResponse, FraudCaseResponse, FraudRuleRequest
- Mapper
- Exceptions (2): FraudCheckException, InvalidFraudRuleException

**Fraud Factors**:
- Shipping address mismatch
- Billing address mismatch
- Card velocity (multiple txns in short time)
- Amount anomaly
- Device fingerprinting
- Email verification status
- User history

**Kafka Events**:
- Consumer: CheckoutEventsConsumer
- Producer: FraudEventProducer

---

### ⏳ 4. Search Service (TODO)

**Components to Create**:
- SearchController (GET /search, GET /suggestions, GET /facets)
- SearchService + SearchServiceImpl
- IndexerService + IndexerServiceImpl
- ElasticsearchConfig
- DTOs (5): SearchRequest, SearchResponse, SearchResultItem, FacetResponse, SuggestionResponse

**Elasticsearch Features**:
- Keyword search with boosting
- Faceted search (category, price, brand)
- Autocomplete/suggestions
- Sort by relevance/price/rating

**Kafka Events**:
- Consumer: ProductIndexConsumer (ProductCreated, ProductUpdated, ProductDeleted)

---

### ⏳ 5. Recommendation Service (TODO)

**Components to Create**:
- RecommendationController (4 endpoints: for-you, similar/{id}, trending, personalized)
- RecommendationService + RecommendationServiceImpl
- CollaborativeFilteringEngine (mock)
- SimilarProductsEngine (mock)
- TrendingEngine
- DTOs (3): RecommendationRequest, RecommendationResponse, RecommendedProductItem
- Exceptions (1): RecommendationException

**Recommendation Engines**:
- User-based CF: "Users like you also liked..."
- Item-based CF: "Users who bought X also bought Y..."
- Similar products by attributes/embedding
- Trending by view count, cart adds, purchases, ratings, momentum

**Kafka Events**:
- Consumer: BehaviorEventConsumer

---

## 📋 Next Steps (In Order)

1. **Complete Coupon Service**
   - [ ] Add Kafka event producer/consumer
   - [ ] Add database migration (V1__create_coupon_tables.sql)
   - [ ] Add unit tests (5+ tests)
   - [ ] Update pom.xml if needed

2. **Scaffold & Implement Review Service** (8 hours)

3. **Scaffold & Implement Fraud Service** (8 hours)

4. **Scaffold & Implement Search Service** (7 hours)

5. **Scaffold & Implement Recommendation Service** (8 hours)

6. **Add all Kafka event wiring** (done per service)

7. **Database migrations** (done per service)

8. **Unit tests** (5+ per service = 25+ tests total)

9. **Integration tests** (3-5 per service = 15+ tests total)

---

## 💾 Files Summary

- **Total files created so far**: 11
- **Total files to create**: ~50-60
- **Code lines written**: ~1,500
- **Code lines to write**: ~4,000-5,000

---

## ✅ Scaffolding Pattern (Followed)

Each service follows strict structure:
```
service-name/
├── src/main/java/com/rudraksha/shopsphere/<service>/
│   ├── controller/      # REST endpoints
│   ├── service/         # Business logic interfaces
│   │   └── impl/        # Implementations
│   ├── entity/          # JPA entities
│   ├── repository/      # Data access
│   ├── dto/             # Request/Response DTOs
│   ├── mapper/          # MapStruct mappers
│   ├── exception/       # Custom exceptions
│   ├── events/          # Kafka producers/consumers
│   │   ├── producer/
│   │   └── consumer/
│   └── config/          # Spring configs
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/    # Flyway migrations
├── src/test/java/
└── pom.xml
```

---

## 🔧 What's Ready to Commit

1. Coupon Service fully scaffolded
2. All entities, repositories, services, controllers, DTOs, mappers, exceptions
3. Validation logic implemented
4. Discount calculation engine

**Commit Message**:
```
feat(coupon-service): scaffold complete coupon service with validation logic

- Add Coupon and CouponRedemption entities with indexes
- Add CouponRepository and CouponRedemptionRepository with queries
- Implement CouponService with validation, discount calculation, redemption
- Add CouponController with CRUD + validation endpoints
- Add CouponMapper and comprehensive DTOs
- Add 4 custom exceptions (NotFound, Expired, LimitExceeded, Invalid)
- Support PERCENTAGE and FIXED_AMOUNT discount types
- Implement usage limits, per-user limits, expiry checks, minimum order validation
- Ready for Kafka event integration and tests
```

---

## 🎯 Timeline

- **Phase 1 (Today)**: Scaffold all 5 services (Coupon done, 4 to go)
- **Phase 2 (Tomorrow)**: Add Kafka events, database migrations
- **Phase 3 (Day 3)**: Add unit and integration tests
- **Phase 4 (Day 4-5)**: Integration testing and bug fixes

**Expected Completion**: 2-3 days (35 hours / 5 days avg)

---
