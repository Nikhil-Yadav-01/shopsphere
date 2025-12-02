# ShopSphere - Remaining Work Organized by File

**Status**: Priority mapping to exact files that need to be created/modified  
**Total Files to Create**: ~200+ files  
**Total Code Lines**: ~15,000+ lines

---

## 📁 Priority 1: Core Commerce Flow - Files to Create

### 1.1 Order Service (`services/order-service/`)

**Controllers** (New)
- [ ] `src/main/java/com/rudraksha/shopsphere/order/controller/OrderController.java`
  - POST /orders (create)
  - GET /orders/{id} (get)
  - GET /orders (list with pagination)
  - PUT /orders/{id}/cancel (cancel)
  - PUT /orders/{id}/status (update status - admin)

**Services** (Enhance)
- [ ] `src/main/java/com/rudraksha/shopsphere/order/service/OrderService.java` (interface)
- [ ] `src/main/java/com/rudraksha/shopsphere/order/service/impl/OrderServiceImpl.java` (implementation)

**DTOs** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/order/dto/request/OrderDetailResponse.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/order/dto/response/OrderListResponse.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/order/dto/response/OrderStatusResponse.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/order/dto/request/OrderStatusUpdateRequest.java`

**Mappers** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/order/mapper/OrderMapper.java` (MapStruct)

**Exceptions** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/order/exception/OrderNotFoundException.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/order/exception/OrderStateException.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/order/exception/InvalidOrderStateException.java`

**Events** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/order/events/producer/OrderEventProducer.java`
  - Methods: publishOrderPlaced(), publishOrderCancelled()
- [ ] `src/main/java/com/rudraksha/shopsphere/order/events/consumer/PaymentConfirmedConsumer.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/order/events/consumer/InventoryReservedConsumer.java`

**Tests** (Create)
- [ ] `src/test/java/com/rudraksha/shopsphere/order/controller/OrderControllerTest.java`
- [ ] `src/test/java/com/rudraksha/shopsphere/order/service/OrderServiceImplTest.java`

**Configuration** (Enhance)
- [ ] `src/main/resources/application.yml` (add Kafka topics, event configuration)
- [ ] `src/main/resources/db/migration/V2__add_order_status_tracking.sql` (add tables)

---

### 1.2 Shipping Service (`services/shipping-service/`)

**Controllers** (New)
- [ ] `src/main/java/com/rudraksha/shopsphere/shipping/controller/ShippingController.java`
  - POST /shipments (create)
  - GET /shipments/{id}
  - PUT /shipments/{id}/track (update tracking)
  - GET /shipments/{id}/tracking (get tracking details)
  - GET /shipments (list)

**Services** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/shipping/service/ShippingService.java` (interface)
- [ ] `src/main/java/com/rudraksha/shopsphere/shipping/service/impl/ShippingServiceImpl.java` (implementation)
- [ ] `src/main/java/com/rudraksha/shopsphere/shipping/service/CarrierAdapter.java` (abstract)
- [ ] `src/main/java/com/rudraksha/shopsphere/shipping/service/impl/MockCarrierImpl.java` (mock carrier)

**Entities** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/shipping/entity/Shipment.java` (if not exists)
- [ ] `src/main/java/com/rudraksha/shopsphere/shipping/entity/TrackingEvent.java` (if not exists)
- [ ] `src/main/java/com/rudraksha/shopsphere/shipping/entity/ShippingLabel.java` (new)

**DTOs** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/shipping/dto/request/CreateShipmentRequest.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/shipping/dto/request/UpdateTrackingRequest.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/shipping/dto/response/ShipmentResponse.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/shipping/dto/response/TrackingResponse.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/shipping/dto/response/ShippingLabelResponse.java`

**Mappers** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/shipping/mapper/ShippingMapper.java`

**Repositories** (Create if not exists)
- [ ] `src/main/java/com/rudraksha/shopsphere/shipping/repository/ShipmentRepository.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/shipping/repository/TrackingEventRepository.java`

**Exceptions** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/shipping/exception/ShipmentNotFoundException.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/shipping/exception/InvalidShipmentStateException.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/shipping/exception/ShippingCostException.java`

**Events** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/shipping/events/consumer/OrderFulfilledConsumer.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/shipping/events/producer/ShippingEventProducer.java`
  - Methods: publishShipmentCreated(), publishShipmentDelivered()

**Tests** (Create)
- [ ] `src/test/java/com/rudraksha/shopsphere/shipping/service/ShippingServiceImplTest.java`
- [ ] `src/test/java/com/rudraksha/shopsphere/shipping/controller/ShippingControllerTest.java`

**Configuration** (Create)
- [ ] `src/main/resources/application.yml` (Kafka, carrier configuration)
- [ ] `src/main/resources/db/migration/V1__create_shipping_tables.sql`

---

### 1.3 Returns Service (`services/returns-service/`)

**Entities** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/returns/entity/ReturnRequest.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/returns/entity/RMA.java` (Return Merchandise Authorization)
- [ ] `src/main/java/com/rudraksha/shopsphere/returns/entity/ReturnItem.java`

**Repositories** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/returns/repository/ReturnRequestRepository.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/returns/repository/RMARepository.java`

**Controllers** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/returns/controller/ReturnsController.java`
  - POST /returns (create return request)
  - GET /returns/{id}
  - PUT /returns/{id}/approve (approve)
  - PUT /returns/{id}/reject (reject)
  - GET /returns (list)

**Services** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/returns/service/ReturnsService.java` (interface)
- [ ] `src/main/java/com/rudraksha/shopsphere/returns/service/impl/ReturnsServiceImpl.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/returns/service/RMAGenerator.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/returns/service/ReturnEligibilityService.java`

**DTOs** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/returns/dto/request/CreateReturnRequest.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/returns/dto/request/ReturnApprovalRequest.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/returns/dto/response/ReturnResponse.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/returns/dto/response/RMAResponse.java`

**Mappers** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/returns/mapper/ReturnsMapper.java`

**Exceptions** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/returns/exception/ReturnNotFoundException.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/returns/exception/InvalidReturnStateException.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/returns/exception/ReturnWindowExpiredException.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/returns/exception/ReturnNotEligibleException.java`

**Events** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/returns/events/consumer/ShipmentDeliveredConsumer.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/returns/events/producer/ReturnEventProducer.java`

**Tests** (Create)
- [ ] `src/test/java/com/rudraksha/shopsphere/returns/service/ReturnsServiceImplTest.java`
- [ ] `src/test/java/com/rudraksha/shopsphere/returns/controller/ReturnsControllerTest.java`

**Configuration** (Create)
- [ ] `src/main/resources/application.yml`
- [ ] `src/main/resources/db/migration/V1__create_returns_tables.sql`

---

### 1.4 Notification Service Enhancement

**Entities** (Possibly new)
- [ ] `src/main/java/com/rudraksha/shopsphere/notification/entity/Notification.java` (if not exists)
- [ ] `src/main/java/com/rudraksha/shopsphere/notification/entity/NotificationTemplate.java` (if not exists)

**Services** (Create/Enhance)
- [ ] `src/main/java/com/rudraksha/shopsphere/notification/service/SmsService.java` (interface)
- [ ] `src/main/java/com/rudraksha/shopsphere/notification/service/impl/SmsServiceImpl.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/notification/service/PushNotificationService.java` (interface)
- [ ] `src/main/java/com/rudraksha/shopsphere/notification/service/impl/PushNotificationServiceImpl.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/notification/service/TemplateService.java` (interface)
- [ ] `src/main/java/com/rudraksha/shopsphere/notification/service/impl/TemplateServiceImpl.java`
- [ ] Enhance: `src/main/java/com/rudraksha/shopsphere/notification/service/EmailService.java`

**Event Consumers** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/notification/events/consumer/OrderPlacedConsumer.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/notification/events/consumer/PaymentConfirmedConsumer.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/notification/events/consumer/ShipmentCreatedConsumer.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/notification/events/consumer/ShipmentDeliveredConsumer.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/notification/events/consumer/ReturnApprovedConsumer.java`

**Email Templates** (Create - FreeMarker)
- [ ] `src/main/resources/templates/order-confirmation.ftl`
- [ ] `src/main/resources/templates/payment-receipt.ftl`
- [ ] `src/main/resources/templates/shipping-notification.ftl`
- [ ] `src/main/resources/templates/delivery-notification.ftl`
- [ ] `src/main/resources/templates/return-approved.ftl`

**DTOs** (Enhance)
- [ ] `src/main/java/com/rudraksha/shopsphere/notification/dto/SmsRequest.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/notification/dto/PushNotificationRequest.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/notification/dto/TemplatePreviewRequest.java`

**Tests** (Create)
- [ ] `src/test/java/com/rudraksha/shopsphere/notification/service/EmailServiceImplTest.java`
- [ ] `src/test/java/com/rudraksha/shopsphere/notification/service/SmsServiceImplTest.java`
- [ ] `src/test/java/com/rudraksha/shopsphere/notification/events/OrderPlacedConsumerTest.java`

**Configuration** (Enhance)
- [ ] `src/main/resources/application.yml` (Twilio, Firebase, SMTP config)

---

### 1.5 Cart Service Enhancement

**Services** (Enhance)
- [ ] Modify: `src/main/java/com/rudraksha/shopsphere/cart/service/impl/CartServiceImpl.java`
  - Add null safety checks
  - Add inventory validation
  - Add price calculation with pricing service
  - Add cart expiry logic
  - Add validation error messages

**DTOs** (Enhance)
- [ ] Add validation annotations to existing DTOs

**Utilities** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/cart/util/CartValidationUtil.java`

**Tests** (Create)
- [ ] `src/test/java/com/rudraksha/shopsphere/cart/service/CartServiceImplTest.java`

---

## 📁 Priority 2: Customer Engagement - Files to Create

### 2.1 Coupon Service (`services/coupon-service/`)

**Entities** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/coupon/entity/Coupon.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/coupon/entity/CouponRedemption.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/coupon/entity/CouponRule.java`

**Repositories** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/coupon/repository/CouponRepository.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/coupon/repository/CouponRedemptionRepository.java`

**Controllers** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/coupon/controller/CouponController.java`

**Services** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/coupon/service/CouponService.java` (interface)
- [ ] `src/main/java/com/rudraksha/shopsphere/coupon/service/impl/CouponServiceImpl.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/coupon/service/CouponValidationService.java`

**DTOs** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/coupon/dto/CouponResponse.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/coupon/dto/CouponValidationRequest.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/coupon/dto/CouponValidationResponse.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/coupon/dto/CouponRedemptionRequest.java`

**Mappers** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/coupon/mapper/CouponMapper.java`

**Exceptions** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/coupon/exception/CouponNotFoundException.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/coupon/exception/CouponExpiredException.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/coupon/exception/CouponLimitExceededException.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/coupon/exception/InvalidCouponException.java`

**Events** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/coupon/events/consumer/OrderPlacedConsumer.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/coupon/events/producer/CouponEventProducer.java`

**Tests** (Create)
- [ ] `src/test/java/com/rudraksha/shopsphere/coupon/service/CouponServiceImplTest.java`
- [ ] `src/test/java/com/rudraksha/shopsphere/coupon/controller/CouponControllerTest.java`

**Configuration** (Create)
- [ ] `src/main/resources/application.yml`
- [ ] `src/main/resources/db/migration/V1__create_coupon_tables.sql`

---

### 2.2 Review Service (`services/review-service/`)

**Entities** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/review/entity/Review.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/review/entity/ReviewReport.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/review/entity/ReviewRating.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/review/entity/ReviewModeration.java`

**Repositories** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/review/repository/ReviewRepository.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/review/repository/ReviewReportRepository.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/review/repository/ReviewRatingRepository.java`

**Controllers** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/review/controller/ReviewController.java`

**Services** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/review/service/ReviewService.java` (interface)
- [ ] `src/main/java/com/rudraksha/shopsphere/review/service/impl/ReviewServiceImpl.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/review/service/ModerationService.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/review/service/impl/ModerationServiceImpl.java`

**DTOs** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/review/dto/ReviewCreateRequest.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/review/dto/ReviewResponse.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/review/dto/ReviewListResponse.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/review/dto/ReviewReportRequest.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/review/dto/ReviewStatsResponse.java`

**Mappers** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/review/mapper/ReviewMapper.java`

**Exceptions** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/review/exception/ReviewNotFoundException.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/review/exception/UnauthorizedReviewException.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/review/exception/DuplicateReviewException.java`

**Events** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/review/events/consumer/OrderDeliveredConsumer.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/review/events/producer/ReviewEventProducer.java`

**Tests** (Create)
- [ ] `src/test/java/com/rudraksha/shopsphere/review/service/ReviewServiceImplTest.java`
- [ ] `src/test/java/com/rudraksha/shopsphere/review/service/ModerationServiceImplTest.java`
- [ ] `src/test/java/com/rudraksha/shopsphere/review/controller/ReviewControllerTest.java`

**Configuration** (Create)
- [ ] `src/main/resources/application.yml`
- [ ] `src/main/resources/db/migration/V1__create_review_tables.sql`

---

### 2.3 Fraud Service (`services/fraud-service/`)

**Entities** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/fraud/entity/FraudCase.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/fraud/entity/FraudRuleLog.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/fraud/entity/FraudScore.java`

**Repositories** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/fraud/repository/FraudCaseRepository.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/fraud/repository/FraudRuleLogRepository.java`

**Controllers** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/fraud/controller/FraudController.java`

**Services** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/fraud/service/FraudScoringService.java` (interface)
- [ ] `src/main/java/com/rudraksha/shopsphere/fraud/service/impl/FraudScoringServiceImpl.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/fraud/service/RulesEngineService.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/fraud/service/impl/RulesEngineServiceImpl.java`

**DTOs** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/fraud/dto/FraudCheckRequest.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/fraud/dto/FraudScoreResponse.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/fraud/dto/FraudCaseResponse.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/fraud/dto/FraudRuleRequest.java`

**Mappers** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/fraud/mapper/FraudMapper.java`

**Exceptions** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/fraud/exception/FraudCheckException.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/fraud/exception/InvalidFraudRuleException.java`

**Events** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/fraud/events/consumer/CheckoutEventsConsumer.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/fraud/events/producer/FraudEventProducer.java`

**Tests** (Create)
- [ ] `src/test/java/com/rudraksha/shopsphere/fraud/service/FraudScoringServiceImplTest.java`
- [ ] `src/test/java/com/rudraksha/shopsphere/fraud/controller/FraudControllerTest.java`

**Configuration** (Create)
- [ ] `src/main/resources/application.yml`
- [ ] `src/main/resources/db/migration/V1__create_fraud_tables.sql`

---

### 2.4 Search Service (`services/search-service/`)

**Controllers** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/search/controller/SearchController.java`

**Services** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/search/service/SearchService.java` (interface)
- [ ] `src/main/java/com/rudraksha/shopsphere/search/service/impl/SearchServiceImpl.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/search/service/IndexerService.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/search/service/impl/IndexerServiceImpl.java`

**Configuration** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/search/config/ElasticsearchConfig.java`

**DTOs** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/search/dto/SearchRequest.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/search/dto/SearchResponse.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/search/dto/SearchResultItem.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/search/dto/FacetResponse.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/search/dto/SuggestionResponse.java`

**Events** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/search/events/consumer/ProductIndexConsumer.java`

**Tests** (Create)
- [ ] `src/test/java/com/rudraksha/shopsphere/search/service/SearchServiceImplTest.java`
- [ ] `src/test/java/com/rudraksha/shopsphere/search/controller/SearchControllerTest.java`

**Resources** (Create)
- [ ] `src/main/resources/application.yml` (Elasticsearch config)
- [ ] `src/main/resources/elasticsearch-mapping.json` (index mapping)

---

### 2.5 Recommendation Service (`services/recommendation-service/`)

**Controllers** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/recommendation/controller/RecommendationController.java`

**Services** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/recommendation/service/RecommendationService.java` (interface)
- [ ] `src/main/java/com/rudraksha/shopsphere/recommendation/service/impl/RecommendationServiceImpl.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/recommendation/ml/CollaborativeFilteringEngine.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/recommendation/ml/SimilarProductsEngine.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/recommendation/ml/TrendingEngine.java`

**DTOs** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/recommendation/dto/RecommendationRequest.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/recommendation/dto/RecommendationResponse.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/recommendation/dto/RecommendedProductItem.java`

**Events** (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/recommendation/events/consumer/BehaviorEventConsumer.java`

**Tests** (Create)
- [ ] `src/test/java/com/rudraksha/shopsphere/recommendation/service/RecommendationServiceImplTest.java`
- [ ] `src/test/java/com/rudraksha/shopsphere/recommendation/controller/RecommendationControllerTest.java`

**Configuration** (Create)
- [ ] `src/main/resources/application.yml`

---

## 📁 Priority 3: Infrastructure Services - Files

### 3.1-3.5 Similar pattern to Priority 1-2

Each service (Media, Admin, Batch, Analytics, WebSocket) follows the same pattern:
- Controllers (4-5 endpoints)
- Services (interface + implementation)
- Entities (2-3)
- Repositories (2-3)
- DTOs (3-5)
- Mappers (1)
- Exceptions (2-4)
- Events (where applicable)
- Tests (3-5 test classes)
- Configuration (application.yml)
- Database migrations (V1 + incremental)

**Total files for Priority 3**: ~80 files

---

## 📁 Priority 4: API Gateway & Infrastructure

### API Gateway Enhancement

**Filters** (Create)
- [ ] Enhance: `src/main/java/com/rudraksha/shopsphere/gateway/filter/AuthenticationFilter.java`
- [ ] Enhance: `src/main/java/com/rudraksha/shopsphere/gateway/filter/AuthorizationFilter.java`
- [ ] Enhance/Create: `src/main/java/com/rudraksha/shopsphere/gateway/filter/RateLimitingFilter.java`
- [ ] Create: `src/main/java/com/rudraksha/shopsphere/gateway/filter/RequestIdFilter.java`
- [ ] Create: `src/main/java/com/rudraksha/shopsphere/gateway/filter/MetricsFilter.java`
- [ ] Create: `src/main/java/com/rudraksha/shopsphere/gateway/filter/LoggingFilter.java`

**Controllers** (Enhance/Create)
- [ ] Enhance: `src/main/java/com/rudraksha/shopsphere/gateway/controller/HealthController.java`
- [ ] Create/Enhance: `src/main/java/com/rudraksha/shopsphere/gateway/controller/RouteAdminController.java`

**Configuration** (Enhance)
- [ ] Enhance: `src/main/java/com/rudraksha/shopsphere/gateway/config/GatewayConfig.java`
- [ ] Create: `src/main/java/com/rudraksha/shopsphere/gateway/config/ExceptionHandlingConfig.java`

**Tests** (Create)
- [ ] `src/test/java/com/rudraksha/shopsphere/gateway/filter/AuthenticationFilterTest.java`
- [ ] `src/test/java/com/rudraksha/shopsphere/gateway/filter/RateLimitingFilterTest.java`
- [ ] `src/test/java/com/rudraksha/shopsphere/gateway/controller/HealthControllerTest.java`

---

### Discovery & Config Server

**Discovery** (Create/Enhance)
- [ ] Enhance: `src/main/java/com/rudraksha/shopsphere/discovery/config/EurekaServerConfig.java`
- [ ] Create: `src/main/java/com/rudraksha/shopsphere/discovery/indicator/ServiceHealthIndicator.java`

**Config Server** (Create/Enhance)
- [ ] Enhance: `src/main/java/com/rudraksha/shopsphere/config/config/ConfigServerConfig.java`

---

### Checkout Enhancement

**Services** (Enhance)
- [ ] Enhance: `src/main/java/com/rudraksha/shopsphere/checkout/service/impl/CheckoutServiceImpl.java`
  - Add tax calculation
  - Add shipping cost
  - Add promotion application
  - Add payment pre-auth
  - Add saga orchestration

**Clients** (Create if not exists)
- [ ] Create: `src/main/java/com/rudraksha/shopsphere/checkout/api/TaxServiceClient.java` (Feign)
- [ ] Create: `src/main/java/com/rudraksha/shopsphere/checkout/api/PromotionServiceClient.java` (Feign)

**Tests** (Enhance)
- [ ] Enhance: `src/test/java/com/rudraksha/shopsphere/checkout/service/CheckoutServiceImplTest.java`

---

## 📁 Priority 5: Kafka Event Infrastructure

### Event Definitions (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/events/UserCreatedEvent.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/events/ProductCreatedEvent.java`
- [ ] ... (15+ event classes across common module)

### Topic Configuration (Create)
- [ ] `k8s/kafka-topics-configmap.yaml` (Kubernetes ConfigMap)
- [ ] `infra/kafka/topics.yml` (topic definitions)

### Event Producers (Already listed in services above, but consolidated here)

### Event Consumers (Already listed in services above)

### DLQ Infrastructure (Create)
- [ ] `src/main/java/com/rudraksha/shopsphere/kafka/DLQHandler.java`
- [ ] `src/main/java/com/rudraksha/shopsphere/kafka/DLQAdminController.java`

---

## 📁 Priority 6: Testing Infrastructure

### Test Configuration (Create)
- [ ] `src/test/java/com/rudraksha/shopsphere/shared/test/IntegrationTestBase.java` (enhance)
- [ ] `src/test/java/com/rudraksha/shopsphere/shared/test/EmbeddedKafkaConfig.java` (enhance)
- [ ] `src/test/java/com/rudraksha/shopsphere/shared/test/MockDataFactory.java` (enhance)

### Contract Tests (Create)
- [ ] `src/test/resources/contracts/` (Pact contract files)

### E2E Tests (Create)
- [ ] `src/test/java/com/rudraksha/shopsphere/e2e/OrderToReturnE2ETest.java`
- [ ] `src/test/java/com/rudraksha/shopsphere/e2e/AdminWorkflowE2ETest.java`
- [ ] `src/test/java/com/rudraksha/shopsphere/e2e/SearchRecommendationE2ETest.java`

---

## 📁 Priority 7: Documentation

### API Documentation (Create)
- [ ] `docs/api/README.md` (API overview)
- [ ] `docs/api/OPENAPI.md` (generated from code)
- [ ] `docs/postman-collection.json` (Postman import)

### Deployment Guides (Create/Enhance)
- [ ] Enhance: `DOCKER_COMPOSE.md`
- [ ] Enhance: `k8s/README.md`
- [ ] Enhance: `terraform/README.md`

### Operational Guides (Create)
- [ ] `docs/MONITORING.md`
- [ ] `docs/TROUBLESHOOTING.md`
- [ ] `docs/DATABASE-MANAGEMENT.md`
- [ ] `docs/DISASTER-RECOVERY.md`

### Developer Guides (Create)
- [ ] `CONTRIBUTING.md`
- [ ] `docs/ARCHITECTURE.md`
- [ ] `docs/FAQ.md`

### CI/CD Documentation (Create)
- [ ] `.github/workflows/ci.yml` (enhance)
- [ ] `.github/workflows/cd.yml` (create)

---

## 📁 Priority 8: Performance & Optimization

### Database Scripts (Create)
- [ ] `docs/database/indexes.sql` (index creation)
- [ ] `docs/database/queries.sql` (optimized queries)
- [ ] `docs/database/tuning.sql` (parameter tuning)

### Load Testing (Create)
- [ ] `performance/k6/scripts/user-flow.js` (K6 scenario)
- [ ] `performance/k6/scripts/product-browse.js`
- [ ] `performance/k6/scripts/checkout.js`
- [ ] `performance/k6/README.md` (load testing guide)

### Configuration (Create)
- [ ] `docs/CACHING-STRATEGY.md`
- [ ] `docs/PERFORMANCE-TUNING.md`
- [ ] `docs/SCALING.md`

---

## 📊 Summary

| Priority | # Files | # Services | Status |
|----------|---------|-----------|--------|
| 1 | ~40 | 5 | 🔴 |
| 2 | ~50 | 5 | 🔴 |
| 3 | ~80 | 5 | 🔴 |
| 4 | ~25 | 4 | 🟡 |
| 5 | ~15 | - | 🔴 |
| 6 | ~20 | - | 🔴 |
| 7 | ~15 | - | 🟡 |
| 8 | ~10 | - | 🔴 |
| **TOTAL** | **~255** | **25+** | **🔴** |

---

## 🚀 Next Steps

1. Start with Priority 1.1 (Order Service)
2. Create files in this order:
   - Entities
   - Repositories
   - DTOs
   - Mappers
   - Service interfaces
   - Service implementations
   - Controllers
   - Exceptions
   - Event producers/consumers
   - Tests
3. Test each service independently
4. Then test end-to-end flow
5. Move to next priority

**Estimated effort**: 10 hours per Priority 1 service, less for others due to pattern repetition.

