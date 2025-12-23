# 🎯 Frontend - Quick Summary of Backend Changes

> **Gửi Frontend Team**: Backend đã thêm tính năng gửi **image** và **file** trong chat!

---

## ⚡ TL;DR - Thay Đổi Nhanh

### 1. API Request - Thêm Field `messageType`

**Cũ:**
```json
{
  "conversationId": "conv-123",
  "message": "Hello"
}
```

**Mới:**
```json
{
  "conversationId": "conv-123",
  "message": "Hello",
  "messageType": "TEXT"  ← BẮT BUỘC
}
```

### 2. API Response - Thêm 4 Fields

Response giờ có thêm:
```typescript
{
  messageType: "TEXT" | "IMAGE" | "FILE",
  fileUrl?: string,
  fileName?: string,
  fileSize?: number
}
```

### 3. Endpoints Mới

```
POST /api/v1/files/chat?type=image     ← Upload image
POST /api/v1/files/chat?type=file      ← Upload file
GET  /api/v1/public/chat/images/{name} ← View image
GET  /api/v1/public/chat/files/{name}  ← Download file
```

---

## 📋 TODO List Cho Frontend

### Bước 1: Update Models (5 phút)
```typescript
enum MessageType {
  TEXT = 'TEXT',
  IMAGE = 'IMAGE', 
  FILE = 'FILE'
}

// Thêm vào ChatMessageRequest:
messageType: MessageType;     // NEW
fileUrl?: string;             // NEW
fileName?: string;            // NEW
fileSize?: number;            // NEW

// Thêm vào ChatMessageResponse: (giống trên)
```

### Bước 2: Update Service (15 phút)

**Thêm methods:**
```typescript
// 1. Upload
uploadChatFile(file: File, type: 'image'|'file'): Observable<any>

// 2. Send image
sendImageMessage(conversationId, file, caption?): Observable<any>

// 3. Send file
sendFileMessage(conversationId, file, description?): Observable<any>

// 4. Get URLs
getImageUrl(fileUrl: string): string
getFileUrl(fileUrl: string): string
```

**Fix existing:**
```typescript
// Thêm messageType vào sendTextMessage
sendTextMessage(conversationId: string, message: string) {
  return this.http.post('/messages/create', {
    conversationId,
    message,
    messageType: 'TEXT'  // ← THÊM DÒNG NÀY
  });
}
```

### Bước 3: Update Component (20 phút)

**Add properties:**
```typescript
selectedFile: File | null = null;
```

**Add methods:**
```typescript
onImageSelected(event)    // Handle image selection
onFileSelected(event)     // Handle file selection
sendFileOrImage()         // Send file/image logic
isImageMessage(msg)       // Check message type
isFileMessage(msg)        // Check message type
```

### Bước 4: Update Template (30 phút)

**Add buttons:**
```html
<!-- Image button -->
<button (click)="imageInput.click()">📷</button>
<input #imageInput type="file" accept="image/*" 
       (change)="onImageSelected($event)" hidden>

<!-- File button -->
<button (click)="fileInput.click()">📎</button>
<input #fileInput type="file" 
       accept=".pdf,.doc,.docx,..." 
       (change)="onFileSelected($event)" hidden>
```

**Update message display:**
```html
<div *ngFor="let msg of messages">
  <!-- Text -->
  <p *ngIf="msg.messageType === 'TEXT'">{{ msg.message }}</p>
  
  <!-- Image -->
  <img *ngIf="msg.messageType === 'IMAGE'" 
       [src]="getImageUrl(msg.fileUrl)">
  
  <!-- File -->
  <a *ngIf="msg.messageType === 'FILE'" 
     [href]="getFileUrl(msg.fileUrl)" download>
    📄 {{ msg.fileName }}
  </a>
</div>
```

---

## ⚠️ Breaking Change

**QUAN TRỌNG**: Tất cả request gửi message PHẢI có field `messageType`!

```typescript
// ❌ Sẽ bị lỗi
{ conversationId: "...", message: "..." }

// ✅ Đúng
{ conversationId: "...", message: "...", messageType: "TEXT" }
```

---

## 🔄 Workflow Mới

### Gửi Text (như cũ, thêm messageType):
```
User gõ text → Click Send → POST /messages/create với messageType="TEXT"
```

### Gửi Image (MỚI):
```
User chọn image → Upload → Nhận uploadedFileName 
→ POST /messages/create với messageType="IMAGE" + fileUrl
```

### Gửi File (MỚI):
```
User chọn file → Upload → Nhận uploadedFileName 
→ POST /messages/create với messageType="FILE" + fileUrl
```

---

## 📦 File Validation

### Images
- **Types**: jpg, jpeg, png, gif, webp
- **Max Size**: 50MB

### Files  
- **Types**: pdf, doc, docx, xls, xlsx, txt, zip, rar
- **Max Size**: 50MB

**Frontend phải validate TRƯỚC KHI upload!**

---

## 🧪 Test Checklist

- [ ] Gửi text message → messageType = "TEXT"
- [ ] Upload image → Nhận uploadedFileName
- [ ] Gửi image message → Hiển thị đúng
- [ ] Upload file → Nhận uploadedFileName
- [ ] Gửi file message → Download được
- [ ] Socket.IO vẫn hoạt động
- [ ] Old messages vẫn hiển thị đúng

---

## 📚 Chi Tiết

**Full guide**: `FRONTEND_MIGRATION_GUIDE.md`  
**Code examples**: `angular-chat-integration.ts`, `angular-chat-component.ts`, `angular-chat-template.html`

---

## 🆘 Cần Giúp?

1. Đọc `FRONTEND_MIGRATION_GUIDE.md` để biết chi tiết
2. Copy code từ `angular-chat-*.ts/html` files
3. Test với Postman để hiểu API trước

---

**Estimated Time**: 1-2 hours  
**Complexity**: ⭐⭐ Medium  
**Status**: Backend ready, waiting for FE integration

Good luck! 🚀

