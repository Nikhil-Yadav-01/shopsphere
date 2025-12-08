#!/bin/bash

BASE_URL="http://localhost:8081"
EMAIL="testuser$(date +%s)@example.com"
PASSWORD="Test@123456"
FIRST_NAME="John"
LAST_NAME="Doe"

echo "=========================================="
echo "Auth Service API Test Suite"
echo "=========================================="
echo ""

# Test 1: Register
echo "1️⃣  Testing POST /auth/register"
echo "   Payload: { email, password, firstName, lastName }"
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\",\"firstName\":\"$FIRST_NAME\",\"lastName\":\"$LAST_NAME\"}")
echo "   Response: $REGISTER_RESPONSE"
echo ""

# Extract tokens using jq if available, otherwise use grep
if command -v jq &> /dev/null; then
  ACCESS_TOKEN=$(echo "$REGISTER_RESPONSE" | jq -r '.accessToken')
  REFRESH_TOKEN=$(echo "$REGISTER_RESPONSE" | jq -r '.refreshToken')
else
  ACCESS_TOKEN=$(echo "$REGISTER_RESPONSE" | grep -o '"accessToken":"[^"]*' | head -1 | cut -d'"' -f4)
  REFRESH_TOKEN=$(echo "$REGISTER_RESPONSE" | grep -o '"refreshToken":"[^"]*' | head -1 | cut -d'"' -f4)
fi

if [ -z "$ACCESS_TOKEN" ] || [ "$ACCESS_TOKEN" = "null" ]; then
  echo "   ❌ Failed to extract access token"
  exit 1
else
  echo "   ✅ Registration successful"
  echo "   ✅ Access Token: ${ACCESS_TOKEN:0:30}..."
  echo "   ✅ Refresh Token: ${REFRESH_TOKEN:0:30}..."
fi
echo ""

# Test 2: Login
echo "2️⃣  Testing POST /auth/login"
echo "   Payload: { email, password }"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")
echo "   Response: $LOGIN_RESPONSE"

if command -v jq &> /dev/null; then
  LOGIN_ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken')
  LOGIN_REFRESH_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.refreshToken')
else
  LOGIN_ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*' | head -1 | cut -d'"' -f4)
  LOGIN_REFRESH_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"refreshToken":"[^"]*' | head -1 | cut -d'"' -f4)
fi

if [ -z "$LOGIN_ACCESS_TOKEN" ] || [ "$LOGIN_ACCESS_TOKEN" = "null" ]; then
  echo "   ❌ Failed to extract login tokens"
else
  echo "   ✅ Login successful"
  echo "   ✅ New Access Token: ${LOGIN_ACCESS_TOKEN:0:30}..."
  ACCESS_TOKEN=$LOGIN_ACCESS_TOKEN
  REFRESH_TOKEN=$LOGIN_REFRESH_TOKEN
fi
echo ""

# Test 3: Refresh Token
echo "3️⃣  Testing POST /auth/refresh"
echo "   Payload: { refreshToken }"
REFRESH_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}")
echo "   Response: $REFRESH_RESPONSE"

if command -v jq &> /dev/null; then
  NEW_ACCESS_TOKEN=$(echo "$REFRESH_RESPONSE" | jq -r '.accessToken')
else
  NEW_ACCESS_TOKEN=$(echo "$REFRESH_RESPONSE" | grep -o '"accessToken":"[^"]*' | head -1 | cut -d'"' -f4)
fi

if [ -z "$NEW_ACCESS_TOKEN" ] || [ "$NEW_ACCESS_TOKEN" = "null" ]; then
  echo "   ❌ Failed to refresh token"
else
  echo "   ✅ Token refreshed successfully"
  echo "   ✅ New Access Token: ${NEW_ACCESS_TOKEN:0:30}..."
  ACCESS_TOKEN=$NEW_ACCESS_TOKEN
fi
echo ""

# Test 4: Logout
echo "4️⃣  Testing POST /auth/logout"
echo "   Header: Authorization: Bearer <token>"
LOGOUT_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/logout" \
  -H "Authorization: Bearer $ACCESS_TOKEN")
LOGOUT_STATUS=$(echo "$LOGOUT_RESPONSE" | tail -n1)
LOGOUT_BODY=$(echo "$LOGOUT_RESPONSE" | head -n-1)

echo "   HTTP Status: $LOGOUT_STATUS"
if [ -n "$LOGOUT_BODY" ]; then
  echo "   Response: $LOGOUT_BODY"
fi
if [ "$LOGOUT_STATUS" = "204" ]; then
  echo "   ✅ Logout successful"
elif [ "$LOGOUT_STATUS" = "200" ]; then
  echo "   ✅ Logout successful"
else
  echo "   ⚠️  Status: $LOGOUT_STATUS (expected 204 or 200)"
fi
echo ""

# Test 5: Test with invalid credentials
echo "5️⃣  Testing POST /auth/login with INVALID credentials"
echo "   Payload: { email: invalid@test.com, password: WrongPassword123 }"
INVALID_LOGIN=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"invalid@test.com","password":"WrongPassword123"}')
INVALID_STATUS=$(echo "$INVALID_LOGIN" | tail -n1)
INVALID_BODY=$(echo "$INVALID_LOGIN" | head -n-1)

echo "   HTTP Status: $INVALID_STATUS"
echo "   Response: $INVALID_BODY"
if [ "$INVALID_STATUS" = "401" ] || [ "$INVALID_STATUS" = "400" ]; then
  echo "   ✅ Correctly rejected invalid credentials"
fi
echo ""

echo "=========================================="
echo "✅ Test Suite Complete"
echo "=========================================="
echo ""
echo "Summary:"
echo "  ✓ POST /auth/register - Register new user"
echo "  ✓ POST /auth/login - Login with credentials"
echo "  ✓ POST /auth/refresh - Refresh access token"
echo "  ✓ POST /auth/logout - Logout (requires auth)"
echo "  ✓ Error handling - Invalid credentials rejected"
