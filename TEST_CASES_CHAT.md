# Test Cases - Chat File & Image

## 🧪 Test Scenarios

### Test 1: Upload Image
**Mục đích**: Kiểm tra upload image thành công

**Steps**:
1. Mở Postman
2. POST `/api/v1/files/chat?type=image`
3. Header: `Authorization: Bearer {token}`
4. Body: form-data, key="file", chọn 1 image file (jpg, png, etc.)
5. Click Send

**Expected Result**:
- Status: 200 OK
- Response có `uploadedFileName` và `uploadedAt`
- File xuất hiện trong `public/chat/images/`

**Actual Result**: ________________

---

### Test 2: Upload File (PDF/DOC)
**Mục đích**: Kiểm tra upload file document thành công

**Steps**:
1. Mở Postman
2. POST `/api/v1/files/chat?type=file`
3. Header: `Authorization: Bearer {token}`
4. Body: form-data, key="file", chọn 1 document file (pdf, doc, etc.)
5. Click Send

**Expected Result**:
- Status: 200 OK
- Response có `uploadedFileName` và `uploadedAt`
- File xuất hiện trong `public/chat/files/`

**Actual Result**: ________________

---

### Test 3: Send Text Message
**Mục đích**: Kiểm tra gửi text message cơ bản

**Steps**:
1. POST `/api/v1/messages/create`
2. Header: `Authorization: Bearer {token}`, `Content-Type: application/json`
3. Body:
```json
{
  "conversationId": "YOUR_CONVERSATION_ID",
  "message": "Hello!",
  "messageType": "TEXT"
}
```

**Expected Result**:
- Status: 200 OK
- Response trả về message object với đầy đủ thông tin
- Message được broadcast qua Socket.IO
- Message xuất hiện trong database

**Actual Result**: ________________

---

### Test 4: Send Image Message
**Mục đích**: Kiểm tra gửi image message sau khi upload

**Pre-condition**: Đã upload image (Test 1)

**Steps**:
1. Copy `uploadedFileName` từ Test 1
2. POST `/api/v1/messages/create`
3. Header: `Authorization: Bearer {token}`, `Content-Type: application/json`
4. Body:
```json
{
  "conversationId": "YOUR_CONVERSATION_ID",
  "message": "Check this image!",
  "messageType": "IMAGE",
  "fileUrl": "{uploadedFileName}",
  "fileName": "vacation.jpg",
  "fileSize": 156789
}
```

**Expected Result**:
- Status: 200 OK
- Response có messageType = "IMAGE"
- Response có fileUrl, fileName, fileSize
- Message được broadcast qua Socket.IO

**Actual Result**: ________________

---

### Test 5: Send File Message
**Mục đích**: Kiểm tra gửi file message sau khi upload

**Pre-condition**: Đã upload file (Test 2)

**Steps**:
1. Copy `uploadedFileName` từ Test 2
2. POST `/api/v1/messages/create`
3. Body:
```json
{
  "conversationId": "YOUR_CONVERSATION_ID",
  "message": "Here is the document",
  "messageType": "FILE",
  "fileUrl": "{uploadedFileName}",
  "fileName": "contract.pdf",
  "fileSize": 1024567
}
```

**Expected Result**:
- Status: 200 OK
- Response có messageType = "FILE"
- Response có fileUrl, fileName, fileSize

**Actual Result**: ________________

---

### Test 6: Get Messages List
**Mục đích**: Kiểm tra lấy danh sách messages

**Steps**:
1. GET `/api/v1/messages?conversationId=YOUR_CONVERSATION_ID`
2. Header: `Authorization: Bearer {token}`

**Expected Result**:
- Status: 200 OK
- Response có array của messages
- Mỗi message có đầy đủ fields
- Messages sắp xếp theo thời gian

**Actual Result**: ________________

---

### Test 7: View Image
**Mục đích**: Kiểm tra xem image đã upload

**Steps**:
1. GET `/api/v1/public/chat/images/{uploadedFileName}`
2. Không cần Authorization header (public endpoint)
3. Hoặc mở trong browser

**Expected Result**:
- Status: 200 OK
- Image hiển thị đúng
- Content-Type: image/jpeg hoặc image/png

**Actual Result**: ________________

---

### Test 8: Download File
**Mục đích**: Kiểm tra download file đã upload

**Steps**:
1. GET `/api/v1/public/chat/files/{uploadedFileName}`
2. Không cần Authorization header
3. Hoặc mở trong browser

**Expected Result**:
- Status: 200 OK
- File download về máy
- Content-Disposition header có attachment

**Actual Result**: ________________

---

### Test 9: Invalid File Type
**Mục đích**: Kiểm tra validation file type

**Steps**:
1. POST `/api/v1/files/chat?type=image`
2. Upload file .exe hoặc .sh

**Expected Result**:
- Status: 400 Bad Request
- Error message: "Invalid file extension"

**Actual Result**: ________________

---

### Test 10: File Size Limit
**Mục đích**: Kiểm tra file size > 50MB bị reject

**Steps**:
1. POST `/api/v1/files/chat?type=image`
2. Upload file > 50MB

**Expected Result**:
- Status: 400 Bad Request
- Error về file size

**Actual Result**: ________________

---

### Test 11: Unauthorized Access
**Mục đích**: Kiểm tra authentication

**Steps**:
1. POST `/api/v1/files/chat?type=image`
2. Không có Authorization header hoặc token sai

**Expected Result**:
- Status: 401 Unauthorized

**Actual Result**: ________________

---

### Test 12: Socket.IO Real-time
**Mục đích**: Kiểm tra real-time messaging

**Steps**:
1. Mở 2 browser tabs
2. Connect 2 users vào cùng conversation
3. User 1 gửi image message
4. Quan sát User 2

**Expected Result**:
- User 2 nhận được message ngay lập tức qua Socket.IO
- Message hiển thị đúng với image

**Actual Result**: ________________

---

### Test 13: Invalid Conversation ID
**Mục đích**: Kiểm tra validation conversationId

**Steps**:
1. POST `/api/v1/messages/create`
2. Body có conversationId không tồn tại hoặc user không có quyền

**Expected Result**:
- Status: 400 Bad Request
- Error: "Conversation ID is invalid"

**Actual Result**: ________________

---

## 📊 Test Summary

| Test # | Test Name | Status | Notes |
|--------|-----------|--------|-------|
| 1 | Upload Image | ⬜ | |
| 2 | Upload File | ⬜ | |
| 3 | Send Text | ⬜ | |
| 4 | Send Image | ⬜ | |
| 5 | Send File | ⬜ | |
| 6 | Get Messages | ⬜ | |
| 7 | View Image | ⬜ | |
| 8 | Download File | ⬜ | |
| 9 | Invalid Type | ⬜ | |
| 10 | File Size | ⬜ | |
| 11 | Auth | ⬜ | |
| 12 | Socket.IO | ⬜ | |
| 13 | Invalid Conv | ⬜ | |

Legend: ✅ Pass | ❌ Fail | ⬜ Not Tested

---

## 🔍 Checklist Before Testing

- [ ] Backend server đang chạy (port 8089)
- [ ] Database đang chạy
- [ ] Thư mục `public/chat/images` đã tạo
- [ ] Thư mục `public/chat/files` đã tạo
- [ ] Có access token hợp lệ
- [ ] Có conversationId hợp lệ
- [ ] Postman hoặc testing tool đã sẵn sàng

---

**Test Date**: _________________
**Tester**: _________________
**Environment**: Development / Staging / Production
**Overall Result**: ✅ Pass / ❌ Fail

