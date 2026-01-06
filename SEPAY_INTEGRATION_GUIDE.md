# Hướng Dẫn Tích Hợp Thanh Toán Sepay

## Tổng Quan Luồng Thanh Toán

Hệ thống thanh toán Sepay được tích hợp hoàn chỉnh với các tính năng:
- ✅ Tạo QR code thanh toán tự động
- ✅ Webhook nhận thông báo từ Sepay
- ✅ Verify chữ ký bảo mật
- ✅ Tự động kích hoạt subscription
- ✅ Hết hạn giao dịch pending tự động
- ✅ Logging chi tiết
- ✅ API kiểm tra trạng thái

---

## 1. Cấu Hình (application.properties)

```properties
# Thông tin tài khoản ngân hàng nhận tiền
sepay.qr.account-number=0456818196868
sepay.qr.bank=MBBank
sepay.qr.currency=VND

# Thời gian hết hạn QR (phút)
sepay.qr.expire-minutes=10

# Secret key để verify webhook (lấy từ Sepay dashboard)
sepay.qr.webhook-secret=Kx7R9mQ2ZpN4cW5FJdE8TtL3A6S0VYH1UoI

# Whitelist IPs của Sepay (optional - tăng bảo mật)
sepay.qr.webhook-ips=103.166.182.0/24,203.162.0.0/16
```

⚠️ **LƯU Ý:** 
- `webhook-secret` phải lấy từ dashboard Sepay
- Với bản **FREE**, Sepay có giới hạn:
  - Số lượng webhook/tháng
  - Thời gian webhook delay
  - Không có retry tự động

---

## 2. API Endpoints

### 2.1. Mua Gói Subscription (Tạo QR Thanh Toán)

**Endpoint:** `POST /api/v1/subscriptions/purchase`

**Request Body:**
```json
{
  "userId": 1,
  "planId": 2
}
```

**Response:** (201 Created)
```json
{
  "transactionId": 123,
  "qrUrl": "https://qr.sepay.vn/img?acc=0456818196868&bank=MBBank&amount=299000&des=Thanh%20toan%20SUB_123",
  "amount": 299000,
  "content": "Thanh toan goi Premium Monthly"
}
```

**Luồng xử lý:**
1. Tạo Subscription với status `PENDING_PAYMENT`
2. Tạo PaymentTransaction với status `PENDING`
3. Generate QR code với mã `SUB_{transactionId}` trong description
4. Trả về QR URL cho frontend hiển thị

**Frontend cần:**
- Hiển thị QR code (có thể dùng `<img src="{qrUrl}">`)
- Hướng dẫn user quét mã và chuyển khoản
- Poll API kiểm tra trạng thái thanh toán

---

### 2.2. Kiểm Tra Trạng Thái Thanh Toán

**Endpoint:** `GET /api/v1/payments/transactions/{transactionId}/status`

**Response:**
```json
{
  "transactionId": 123,
  "status": "PENDING",  // hoặc SUCCESS, EXPIRED, FAILED
  "amount": 299000,
  "provider": "SEPAY",
  "createdAt": "2026-01-05T10:30:00Z",
  "paidAt": ""  // hoặc "2026-01-05T10:35:22" nếu đã thanh toán
}
```

**Frontend nên:**
- Poll endpoint này mỗi 3-5 giây sau khi hiển thị QR
- Khi status = `SUCCESS` → Chuyển sang màn hình thành công
- Khi status = `EXPIRED` → Thông báo hết hạn, tạo giao dịch mới
- Timeout sau 10 phút (theo `expire-minutes`)

---

### 2.3. Lịch Sử Giao Dịch Của User

**Endpoint:** `GET /api/v1/users/{userId}/payments`

**Response:**
```json
[
  {
    "id": 123,
    "subscription": { ... },
    "provider": "SEPAY",
    "amount": 299000,
    "status": "SUCCESS",
    "externalRef": "SEPAY_20260105_ABCD1234",
    "createdAt": "2026-01-05T10:30:00Z",
    "paidAt": "2026-01-05T10:35:22"
  }
]
```

---

### 2.4. Webhook từ Sepay (Tự Động)

**Endpoint:** `POST /api/v1/payments/sepay/webhook`

**Request Body (từ Sepay):**
```json
{
  "transaction_id": "SEPAY_20260105_ABCD1234",
  "amount": 299000,
  "description": "Thanh toan SUB_123",
  "status": "SUCCESS",
  "signature": "abcd1234..."
}
```

**Luồng xử lý:**
1. Verify chữ ký (HMAC-SHA256)
2. Parse mã `SUB_123` từ description
3. Tìm transaction trong DB
4. Kiểm tra:
   - Transaction chưa SUCCESS
   - Số tiền khớp
5. Update transaction → SUCCESS
6. Kích hoạt subscription → ACTIVE
7. Tính thời gian hết hạn subscription

⚠️ **Bảo mật webhook:**
- Verify signature bắt buộc
- Kiểm tra IP whitelist (optional)
- Idempotent: webhook gọi nhiều lần vẫn an toàn

---

## 3. Các Trạng Thái (PaymentStatus)

| Status | Ý nghĩa |
|--------|---------|
| `PENDING` | Đang chờ thanh toán |
| `SUCCESS` | Đã thanh toán thành công |
| `EXPIRED` | Hết hạn (quá 10 phút) |
| `FAILED` | Thanh toán thất bại |

---

## 4. Scheduled Job - Hết Hạn Giao Dịch

**Class:** `PaymentExpireJob`

**Chạy:** Mỗi 5 phút (cron: `0 */5 * * * *`)

**Chức năng:**
- Tìm tất cả transaction có status = `PENDING`
- Kiểm tra `createdAt` < (now - expire-minutes)
- Update status → `EXPIRED`

⚠️ **Lưu ý:**
- Với bản FREE Sepay, user cần tạo giao dịch mới nếu hết hạn
- Không nên set `expire-minutes` quá ngắn

---

## 5. Luồng Hoàn Chỉnh (End-to-End)

### 5.1. Frontend Flow

```
1. User chọn gói Premium → Click "Mua ngay"
   ↓
2. POST /subscriptions/purchase
   ← Response: { qrUrl, transactionId, amount }
   ↓
3. Hiển thị QR code + hướng dẫn
   ↓
4. Start polling (mỗi 3s):
   GET /payments/transactions/{id}/status
   ↓
5a. Status = SUCCESS → Chuyển trang "Thanh toán thành công"
5b. Status = EXPIRED → "Hết hạn, vui lòng thử lại"
5c. Timeout 10 phút → Stop polling
```

### 5.2. Backend Flow

```
Purchase Request
   ↓
SubscriptionService.createPendingSubscription()
   → Subscription (PENDING_PAYMENT)
   ↓
PaymentTransactionService.createPendingTransaction()
   → PaymentTransaction (PENDING)
   ↓
PaymentTransactionService.createQR()
   → QR URL với description "Thanh toan SUB_{id}"
   ↓
Response QR to Frontend
   ↓
[User quét QR và chuyển khoản]
   ↓
Sepay gửi webhook → POST /payments/sepay/webhook
   ↓
Verify signature → Parse SUB_123
   ↓
Update Transaction → SUCCESS
   ↓
SubscriptionService.activateSubscription()
   → Subscription (ACTIVE)
   → Set startAt, endAt, currentPeriodStart/End
```

---

## 6. Test Webhook Locally

Để test webhook trong môi trường dev:

### Option 1: Ngrok
```bash
ngrok http 8089
# Copy HTTPS URL: https://abc123.ngrok.io
# Paste vào Sepay webhook settings: https://abc123.ngrok.io/api/v1/payments/sepay/webhook
```

### Option 2: Mock Webhook (Postman/cURL)
```bash
curl -X POST http://localhost:8089/api/v1/payments/sepay/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "TEST_12345",
    "amount": 299000,
    "description": "Thanh toan SUB_1",
    "status": "SUCCESS",
    "signature": "<calculated_signature>"
  }'
```

**Tính signature:**
```javascript
// Node.js example
const crypto = require('crypto');
const secret = 'Kx7R9mQ2ZpN4cW5FJdE8TtL3A6S0VYH1UoI';
const payload = 'TEST_12345' + '299000' + 'Thanh toan SUB_1' + 'SUCCESS';
const signature = crypto.createHmac('sha256', secret)
  .update(payload)
  .digest('base64');
console.log(signature);
```

---

## 7. Giới Hạn Bản FREE Sepay

⚠️ **Lưu ý quan trọng:**

1. **Webhook delay:** 
   - Bản FREE có thể delay 30s - 2 phút
   - Frontend cần polling để UX tốt hơn

2. **Số lượng webhook:**
   - Giới hạn số webhook/tháng
   - Nên có retry logic nếu webhook fail

3. **Không có retry:**
   - Webhook fail → không tự retry
   - Cần implement polling/fallback

4. **Rate limiting:**
   - Giới hạn request/phút
   - Cẩn thận khi test

### Giải pháp:

✅ **Luôn dùng polling** để kiểm tra status
✅ **Webhook chỉ là bonus** để update realtime
✅ **Set expire-minutes = 10-15 phút** (đủ thời gian)
✅ **Log tất cả webhook** để debug

---

## 8. Troubleshooting

### 8.1. Webhook không được gọi

**Nguyên nhân:**
- URL webhook sai trong Sepay dashboard
- Server không public (localhost)
- Firewall block

**Giải pháp:**
- Kiểm tra URL trong Sepay settings
- Dùng ngrok cho local dev
- Check logs của Sepay

---

### 8.2. Signature verification failed

**Nguyên nhân:**
- Secret key sai
- Thứ tự payload sai
- Encoding sai

**Giải pháp:**
```java
// Đúng thứ tự:
String payload = transactionId + amount + description + status;
```

---

### 8.3. Transaction not found

**Nguyên nhân:**
- Description không chứa `SUB_{id}`
- User tự nhập nội dung chuyển khoản

**Giải pháp:**
- Hướng dẫn user **KHÔNG SỬA** nội dung chuyển khoản
- Parse regex chặt chẽ: `SUB_(\\d+)`

---

## 9. Security Checklist

✅ **Luôn verify signature** (bắt buộc)  
✅ **Whitelist IPs** (nếu Sepay cung cấp)  
✅ **Check transaction status** (tránh duplicate)  
✅ **Validate amount** (tránh user chuyển sai số tiền)  
✅ **Log tất cả webhook** (để audit)  
✅ **Rate limiting** cho API public  
✅ **HTTPS** cho webhook endpoint  

---

## 10. Monitoring & Logging

Các log quan trọng đã được thêm:

```log
INFO: Creating pending transaction for subscription 1, plan PREMIUM_MONTH, amount 299000
INFO: Created transaction 123 with status PENDING
INFO: Generated QR code for transaction 123, invoice SUB_123
INFO: Processing Sepay webhook: txId=SEPAY_123, amount=299000, status=SUCCESS
INFO: Webhook signature verified successfully
INFO: Found transaction in DB: id=123, status=PENDING, amount=299000
INFO: Business validation passed
INFO: Updated transaction 123 to SUCCESS status
INFO: Activated subscription 45 successfully
```

**Nên theo dõi:**
- Success rate của webhook
- Thời gian từ tạo QR → SUCCESS
- Số lượng EXPIRED transactions
- Error rate

---

## Tổng Kết

✅ **Đã hoàn thiện:**
1. API mua gói với QR tự động
2. API kiểm tra trạng thái
3. Webhook handler với security đầy đủ
4. Scheduled job hết hạn giao dịch
5. Logging chi tiết
6. Error handling toàn diện

✅ **Phù hợp với bản FREE Sepay:**
- Polling để bù webhook delay
- Timeout hợp lý (10 phút)
- Signature verification bắt buộc

🚀 **Sẵn sàng production!**
