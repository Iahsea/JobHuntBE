# Test API Thanh Toán Sepay

## Bước 1: Tạo Giao Dịch Mới (Mua Gói)

**Endpoint:** `POST http://localhost:8089/api/v1/subscriptions/purchase`

**Request:**
```json
{
  "userId": 1,
  "planId": 1
}
```

**Response mẫu:**
```json
{
  "transactionId": 5,
  "qrUrl": "https://qr.sepay.vn/img?acc=0456818196868&bank=MBBank&amount=299000&des=Thanh%20toan%20SUB_5",
  "amount": 299000,
  "content": "Thanh toan goi Premium Monthly"
}
```

✅ **Kiểm tra:**
- Response trả về đầy đủ thông tin
- transactionId được tạo
- qrUrl có format đúng

---

## Bước 2: Hiển thị QR Code

**Cách 1: Dùng img tag (HTML)**
```html
<img src="https://qr.sepay.vn/img?acc=0456818196868&bank=MBBank&amount=299000&des=Thanh%20toan%20SUB_5" 
     alt="QR Payment" 
     width="300" />
```

**Cách 2: Test trong browser**
- Copy `qrUrl` từ response
- Paste vào trình duyệt
- Sẽ thấy QR code hiển thị

---

## Bước 3: Kiểm Tra Trạng Thái (Polling)

**Endpoint:** `GET http://localhost:8089/api/v1/payments/transactions/5/status`

**Response (trước khi thanh toán):**
```json
{
  "transactionId": 5,
  "status": "PENDING",
  "amount": 299000,
  "provider": "SEPAY",
  "createdAt": "2026-01-05T10:30:00Z",
  "paidAt": ""
}
```

**Response (sau khi thanh toán thành công):**
```json
{
  "transactionId": 5,
  "status": "SUCCESS",
  "amount": 299000,
  "provider": "SEPAY",
  "createdAt": "2026-01-05T10:30:00Z",
  "paidAt": "2026-01-05T10:35:22"
}
```

---

## Bước 4: Test Webhook (Mock)

### 4.1. Tính Signature

**Công thức:**
```
payload = transaction_id + amount + description + status
signature = HMAC-SHA256(payload, webhook_secret) → base64
```

**Ví dụ với Node.js:**
```javascript
const crypto = require('crypto');

const secret = 'Kx7R9mQ2ZpN4cW5FJdE8TtL3A6S0VYH1UoI';
const transactionId = 'TEST_001';
const amount = '299000';
const description = 'Thanh toan SUB_5';
const status = 'SUCCESS';

const payload = transactionId + amount + description + status;
const signature = crypto.createHmac('sha256', secret)
  .update(payload, 'utf8')
  .digest('base64');

console.log('Payload:', payload);
console.log('Signature:', signature);
```

### 4.2. Gửi Webhook Request

**cURL:**
```bash
curl -X POST http://localhost:8089/api/v1/payments/sepay/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "TEST_001",
    "amount": 299000,
    "description": "Thanh toan SUB_5",
    "status": "SUCCESS",
    "signature": "YOUR_CALCULATED_SIGNATURE"
  }'
```

**Postman:**
```json
POST http://localhost:8089/api/v1/payments/sepay/webhook
Content-Type: application/json

{
  "transaction_id": "TEST_001",
  "amount": 299000,
  "description": "Thanh toan SUB_5",
  "status": "SUCCESS",
  "signature": "YOUR_CALCULATED_SIGNATURE"
}
```

---

## Bước 5: Kiểm Tra Subscription Đã Active

**Endpoint:** `GET http://localhost:8089/api/v1/subscriptions/5`

**Response:**
```json
{
  "id": 5,
  "user": { ... },
  "plan": { ... },
  "status": "ACTIVE",
  "startAt": "2026-01-05T10:35:22",
  "endAt": "2026-02-05T10:35:22",
  "currentPeriodStart": "2026-01-05T10:35:22",
  "currentPeriodEnd": "2026-02-05T10:35:22"
}
```

---

## Bước 6: Xem Lịch Sử Giao Dịch

**Endpoint:** `GET http://localhost:8089/api/v1/users/1/payments`

**Response:**
```json
[
  {
    "id": 5,
    "subscription": {
      "id": 5,
      "status": "ACTIVE"
    },
    "provider": "SEPAY",
    "amount": 299000,
    "status": "SUCCESS",
    "externalRef": "TEST_001",
    "createdAt": "2026-01-05T10:30:00Z",
    "paidAt": "2026-01-05T10:35:22"
  }
]
```

---

## Test Cases

### ✅ Happy Path
1. Tạo transaction → PENDING
2. QR code được generate
3. Gửi webhook → SUCCESS
4. Transaction → SUCCESS
5. Subscription → ACTIVE

### ⚠️ Edge Cases

#### 1. Webhook với signature sai
```json
{
  "transaction_id": "TEST_001",
  "amount": 299000,
  "description": "Thanh toan SUB_5",
  "status": "SUCCESS",
  "signature": "invalid_signature"
}
```
**Kết quả:** `400 Bad Request - Invalid SePay signature`

---

#### 2. Webhook với số tiền sai
```json
{
  "transaction_id": "TEST_001",
  "amount": 100000,  // Sai số tiền
  "description": "Thanh toan SUB_5",
  "status": "SUCCESS",
  "signature": "<valid_signature>"
}
```
**Kết quả:** `400 Bad Request - Paid amount mismatch`

---

#### 3. Webhook duplicate (gọi 2 lần)
- Lần 1: SUCCESS → Update transaction
- Lần 2: ERROR → "Transaction already processed"

---

#### 4. Transaction hết hạn
- Chờ > 10 phút
- PaymentExpireJob chạy → status = EXPIRED
- Gửi webhook → Vẫn update được (nếu chữ ký đúng)

---

## Script Test Tự Động

**test-payment-flow.sh** (Bash)
```bash
#!/bin/bash

# 1. Tạo transaction
echo "Creating transaction..."
RESPONSE=$(curl -s -X POST http://localhost:8089/api/v1/subscriptions/purchase \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"planId":1}')

TX_ID=$(echo $RESPONSE | jq -r '.transactionId')
echo "Transaction ID: $TX_ID"

# 2. Kiểm tra status
echo "Checking status..."
curl -s http://localhost:8089/api/v1/payments/transactions/$TX_ID/status | jq

# 3. Calculate signature
SECRET="Kx7R9mQ2ZpN4cW5FJdE8TtL3A6S0VYH1UoI"
TX_REF="TEST_001"
AMOUNT="299000"
DESC="Thanh toan SUB_$TX_ID"
STATUS="SUCCESS"

PAYLOAD="$TX_REF$AMOUNT$DESC$STATUS"
SIGNATURE=$(echo -n "$PAYLOAD" | openssl dgst -sha256 -hmac "$SECRET" -binary | base64)

echo "Signature: $SIGNATURE"

# 4. Gửi webhook
echo "Sending webhook..."
curl -X POST http://localhost:8089/api/v1/payments/sepay/webhook \
  -H "Content-Type: application/json" \
  -d "{
    \"transaction_id\": \"$TX_REF\",
    \"amount\": $AMOUNT,
    \"description\": \"$DESC\",
    \"status\": \"$STATUS\",
    \"signature\": \"$SIGNATURE\"
  }"

# 5. Kiểm tra lại status
echo "Checking final status..."
curl -s http://localhost:8089/api/v1/payments/transactions/$TX_ID/status | jq
```

---

## Logs Để Kiểm Tra

Khi test, check logs để thấy luồng:

```log
INFO: User 1 purchasing plan 1
INFO: Creating pending transaction for subscription 10, plan BASIC_MONTH, amount 299000
INFO: Created transaction 5 with status PENDING
INFO: Generated QR code for transaction 5, invoice SUB_5
INFO: Created QR payment for transaction 5

[User quét QR và chuyển khoản...]

INFO: Received Sepay webhook: transactionId=TEST_001, amount=299000, status=SUCCESS
INFO: Processing Sepay webhook: txId=TEST_001, amount=299000, status=SUCCESS
INFO: Webhook signature verified successfully
INFO: Extracted transaction ID: 5
INFO: Found transaction in DB: id=5, status=PENDING, amount=299000
INFO: Business validation passed
INFO: Updated transaction 5 to SUCCESS status
INFO: Activated subscription 10 successfully
```

---

## Checklist Trước Khi Deploy Production

- [ ] Cập nhật `webhook-secret` thật từ Sepay dashboard
- [ ] Cập nhật `account-number` và `bank` đúng
- [ ] Test webhook với ngrok hoặc server thật
- [ ] Enable HTTPS cho webhook endpoint
- [ ] Set up monitoring/alerting cho webhook failures
- [ ] Test với nhiều kịch bản (success, fail, duplicate, expired)
- [ ] Kiểm tra scheduled job chạy đúng
- [ ] Log rotation (tránh đầy disk)
- [ ] Backup database trước khi deploy

---

## Liên Hệ Support

Nếu gặp vấn đề:
1. Check logs server
2. Check Sepay dashboard (webhook history)
3. Verify configuration trong application.properties
4. Test với mock webhook trước
5. Contact Sepay support (nếu webhook không được gọi)
