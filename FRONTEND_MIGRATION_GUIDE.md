# 📋 Hướng Dẫn Frontend - Thay Đổi Chat File & Image

## 🎯 Tổng Quan Thay Đổi Backend

Backend đã được cập nhật để hỗ trợ gửi **hình ảnh** và **file** trong chat, ngoài tin nhắn text hiện có.

---

## 🔄 Thay Đổi API

### 1. ChatMessage Model - Đã Thêm Fields Mới

**Backend Entity đã thay đổi**, Frontend cần cập nhật TypeScript interface:

#### ❌ Cũ (Chỉ có text):
```typescript
interface ChatMessage {
  id: string;
  conversationId: number;
  message: string;
  sender: ParticipantInfo;
  createdDate: string;
  me: boolean;
}
```

#### ✅ Mới (Có text, image, file):
```typescript
enum MessageType {
  TEXT = 'TEXT',
  IMAGE = 'IMAGE',
  FILE = 'FILE'
}

interface ChatMessage {
  id: string;
  conversationId: number;
  message: string;
  sender: ParticipantInfo;
  createdDate: string;
  me: boolean;
  
  // 🆕 FIELDS MỚI
  messageType: MessageType;    // Loại message: TEXT, IMAGE, FILE
  fileUrl?: string;            // URL của file/image đã upload
  fileName?: string;           // Tên file gốc
  fileSize?: number;           // Kích thước file (bytes)
}
```

---

## 📡 API Endpoints Mới

### 1. Upload Chat File/Image (MỚI)

**Endpoint**: `POST /api/v1/files/chat`

**Parameters**:
- `type`: `'image'` hoặc `'file'` (query param)
- `file`: File cần upload (multipart/form-data)

**Request Example**:
```typescript
// Upload Image
uploadImage(file: File): Observable<UploadResponse> {
  const formData = new FormData();
  formData.append('file', file);
  
  return this.http.post<UploadResponse>(
    `${this.baseUrl}/files/chat?type=image`,
    formData
  );
}

// Upload File
uploadFile(file: File): Observable<UploadResponse> {
  const formData = new FormData();
  formData.append('file', file);
  
  return this.http.post<UploadResponse>(
    `${this.baseUrl}/files/chat?type=file`,
    formData
  );
}
```

**Response**:
```typescript
interface UploadResponse {
  uploadedFileName: string;  // e.g., "1234567890-image.jpg"
  uploadedAt: string;        // ISO timestamp
}
```

**Validation Rules**:
- **Images**: jpg, jpeg, png, gif, webp (max 50MB)
- **Files**: pdf, doc, docx, xls, xlsx, txt, zip, rar (max 50MB)

---

### 2. Send Message - Request Body Đã Thay Đổi

**Endpoint**: `POST /api/v1/messages/create` (giữ nguyên)

#### ❌ Request Cũ (Chỉ text):
```typescript
interface SendMessageRequest {
  conversationId: string;
  message: string;  // BẮT BUỘC
}
```

#### ✅ Request Mới (Text/Image/File):
```typescript
interface SendMessageRequest {
  conversationId: string;
  message?: string;              // KHÔNG BẮT BUỘC nữa
  messageType: MessageType;      // 🆕 BẮT BUỘC: TEXT, IMAGE, FILE
  
  // 🆕 CHO IMAGE/FILE messages
  fileUrl?: string;              // BẮT BUỘC nếu messageType = IMAGE hoặc FILE
  fileName?: string;             // Tên file hiển thị
  fileSize?: number;             // Size để hiển thị
}
```

**Examples**:

```typescript
// 1. Gửi TEXT message (như cũ, nhưng thêm messageType)
{
  conversationId: "conv-123",
  message: "Hello!",
  messageType: "TEXT"
}

// 2. Gửi IMAGE message (MỚI)
{
  conversationId: "conv-123",
  message: "Check this photo!",  // Optional caption
  messageType: "IMAGE",
  fileUrl: "1234567890-image.jpg",
  fileName: "vacation.jpg",
  fileSize: 156789
}

// 3. Gửi FILE message (MỚI)
{
  conversationId: "conv-123",
  message: "Here is the document",  // Optional description
  messageType: "FILE",
  fileUrl: "1234567890-contract.pdf",
  fileName: "contract.pdf",
  fileSize: 1024567
}
```

---

### 3. Get Messages - Response Đã Thay Đổi

**Endpoint**: `GET /api/v1/messages?conversationId={id}` (giữ nguyên)

#### ❌ Response Cũ:
```typescript
{
  result: [
    {
      id: "123",
      conversationId: 1,
      message: "Hello",
      sender: {...},
      createdDate: "2025-12-23T10:00:00Z",
      me: true
    }
  ]
}
```

#### ✅ Response Mới:
```typescript
{
  result: [
    {
      id: "123",
      conversationId: 1,
      message: "Hello",
      sender: {...},
      createdDate: "2025-12-23T10:00:00Z",
      me: true,
      messageType: "TEXT",      // 🆕
      fileUrl: null,            // 🆕
      fileName: null,           // 🆕
      fileSize: null            // 🆕
    },
    {
      id: "124",
      conversationId: 1,
      message: "Check this!",
      sender: {...},
      createdDate: "2025-12-23T10:01:00Z",
      me: false,
      messageType: "IMAGE",                           // 🆕
      fileUrl: "1234567890-image.jpg",               // 🆕
      fileName: "screenshot.jpg",                     // 🆕
      fileSize: 245678                                // 🆕
    }
  ]
}
```

---

### 4. View/Download Endpoints (MỚI)

**View Image**:
```
GET /api/v1/public/chat/images/{fileName}
```
Example: `http://localhost:8089/api/v1/public/chat/images/1234567890-image.jpg`

**Download File**:
```
GET /api/v1/public/chat/files/{fileName}
```
Example: `http://localhost:8089/api/v1/public/chat/files/1234567890-document.pdf`

**Lưu ý**: Đây là **public endpoints**, không cần Authorization header.

---

## 🔧 Cần Thay Đổi Trong Frontend

### 1. Cập Nhật TypeScript Interfaces

```typescript
// models/chat.models.ts

export enum MessageType {
  TEXT = 'TEXT',
  IMAGE = 'IMAGE',
  FILE = 'FILE'
}

export interface ParticipantInfo {
  userId: string;
  username: string;
  name: string;
  avatar: string;
}

// 🔄 CẬP NHẬT: ChatMessageRequest
export interface ChatMessageRequest {
  conversationId: string;
  message?: string;              // Changed: Optional now
  messageType: MessageType;      // NEW: Required
  fileUrl?: string;              // NEW: For IMAGE/FILE
  fileName?: string;             // NEW: For IMAGE/FILE
  fileSize?: number;             // NEW: For IMAGE/FILE
}

// 🔄 CẬP NHẬT: ChatMessageResponse
export interface ChatMessageResponse {
  id: string;
  conversationId: number;
  me: boolean;
  message: string;
  sender: ParticipantInfo;
  createdDate: string;
  messageType: MessageType;      // NEW
  fileUrl?: string;              // NEW
  fileName?: string;             // NEW
  fileSize?: number;             // NEW
}

// 🆕 MỚI: UploadResponse
export interface UploadResponse {
  uploadedFileName: string;
  uploadedAt: string;
}
```

---

### 2. Cập Nhật Service Methods

```typescript
// services/chat.service.ts

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private baseUrl = 'http://localhost:8089/api/v1';

  constructor(private http: HttpClient) {}

  // 🆕 MỚI: Upload chat file
  uploadChatFile(file: File, type: 'image' | 'file'): Observable<UploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    
    return this.http.post<UploadResponse>(
      `${this.baseUrl}/files/chat?type=${type}`,
      formData
    );
  }

  // 🔄 CẬP NHẬT: Send text message - thêm messageType
  sendTextMessage(conversationId: string, message: string): Observable<ApiResponse<ChatMessageResponse>> {
    const request: ChatMessageRequest = {
      conversationId,
      message,
      messageType: MessageType.TEXT  // 🆕 THÊM FIELD NÀY
    };
    
    return this.http.post<ApiResponse<ChatMessageResponse>>(
      `${this.baseUrl}/messages/create`,
      request
    );
  }

  // 🆕 MỚI: Send image message
  sendImageMessage(
    conversationId: string,
    imageFile: File,
    caption?: string
  ): Observable<ApiResponse<ChatMessageResponse>> {
    return new Observable(observer => {
      // Step 1: Upload image
      this.uploadChatFile(imageFile, 'image').subscribe({
        next: (uploadResponse) => {
          // Step 2: Send message with image info
          const request: ChatMessageRequest = {
            conversationId,
            message: caption || 'Sent an image',
            messageType: MessageType.IMAGE,
            fileUrl: uploadResponse.uploadedFileName,
            fileName: imageFile.name,
            fileSize: imageFile.size
          };

          this.http.post<ApiResponse<ChatMessageResponse>>(
            `${this.baseUrl}/messages/create`,
            request
          ).subscribe({
            next: (response) => observer.next(response),
            error: (error) => observer.error(error),
            complete: () => observer.complete()
          });
        },
        error: (error) => observer.error(error)
      });
    });
  }

  // 🆕 MỚI: Send file message
  sendFileMessage(
    conversationId: string,
    file: File,
    description?: string
  ): Observable<ApiResponse<ChatMessageResponse>> {
    return new Observable(observer => {
      // Step 1: Upload file
      this.uploadChatFile(file, 'file').subscribe({
        next: (uploadResponse) => {
          // Step 2: Send message with file info
          const request: ChatMessageRequest = {
            conversationId,
            message: description || 'Sent a file',
            messageType: MessageType.FILE,
            fileUrl: uploadResponse.uploadedFileName,
            fileName: file.name,
            fileSize: file.size
          };

          this.http.post<ApiResponse<ChatMessageResponse>>(
            `${this.baseUrl}/messages/create`,
            request
          ).subscribe({
            next: (response) => observer.next(response),
            error: (error) => observer.error(error),
            complete: () => observer.complete()
          });
        },
        error: (error) => observer.error(error)
      });
    });
  }

  // Giữ nguyên
  getMessages(conversationId: string): Observable<ApiResponse<ChatMessageResponse[]>> {
    return this.http.get<ApiResponse<ChatMessageResponse[]>>(
      `${this.baseUrl}/messages?conversationId=${conversationId}`
    );
  }

  // 🆕 MỚI: Helper methods
  getImageUrl(fileUrl: string): string {
    return `${this.baseUrl}/public/chat/images/${fileUrl}`;
  }

  getFileUrl(fileUrl: string): string {
    return `${this.baseUrl}/public/chat/files/${fileUrl}`;
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  }
}
```

---

### 3. Cập Nhật Component Logic

```typescript
// components/chat.component.ts

export class ChatComponent implements OnInit {
  messages: ChatMessageResponse[] = [];
  messageText = '';
  selectedFile: File | null = null;

  constructor(private chatService: ChatService) {}

  // 🔄 CẬP NHẬT: Send message - handle cả text và file
  sendMessage(): void {
    if (this.selectedFile) {
      this.sendFileOrImage();
    } else if (this.messageText.trim()) {
      this.sendTextMessage();
    }
  }

  // Text message (cập nhật nhẹ)
  sendTextMessage(): void {
    const message = this.messageText.trim();
    this.chatService.sendTextMessage(this.conversationId, message).subscribe({
      next: (response) => {
        console.log('Message sent:', response);
        this.messageText = '';
      },
      error: (error) => console.error('Error:', error)
    });
  }

  // 🆕 MỚI: Handle file/image
  sendFileOrImage(): void {
    if (!this.selectedFile) return;

    const isImage = this.isImageFile(this.selectedFile);
    const sendMethod = isImage
      ? this.chatService.sendImageMessage(
          this.conversationId,
          this.selectedFile,
          this.messageText
        )
      : this.chatService.sendFileMessage(
          this.conversationId,
          this.selectedFile,
          this.messageText
        );

    sendMethod.subscribe({
      next: (response) => {
        console.log('File sent:', response);
        this.selectedFile = null;
        this.messageText = '';
      },
      error: (error) => console.error('Error:', error)
    });
  }

  // 🆕 MỚI: Handle file selection
  onImageSelected(event: any): void {
    const file: File = event.target.files[0];
    if (!file) return;

    // Validate
    const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    if (!validTypes.includes(file.type)) {
      alert('Invalid image type');
      return;
    }

    if (file.size > 50 * 1024 * 1024) {
      alert('File too large (max 50MB)');
      return;
    }

    this.selectedFile = file;
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (!file) return;

    // Validate
    const validExtensions = ['.pdf', '.doc', '.docx', '.xls', '.xlsx', '.txt', '.zip', '.rar'];
    const isValid = validExtensions.some(ext => 
      file.name.toLowerCase().endsWith(ext)
    );

    if (!isValid) {
      alert('Invalid file type');
      return;
    }

    if (file.size > 50 * 1024 * 1024) {
      alert('File too large (max 50MB)');
      return;
    }

    this.selectedFile = file;
  }

  // 🆕 MỚI: Helper methods
  isImageFile(file: File): boolean {
    return file.type.startsWith('image/');
  }

  isTextMessage(message: ChatMessageResponse): boolean {
    return message.messageType === MessageType.TEXT;
  }

  isImageMessage(message: ChatMessageResponse): boolean {
    return message.messageType === MessageType.IMAGE;
  }

  isFileMessage(message: ChatMessageResponse): boolean {
    return message.messageType === MessageType.FILE;
  }

  getImageUrl(message: ChatMessageResponse): string {
    return this.chatService.getImageUrl(message.fileUrl!);
  }

  getFileUrl(message: ChatMessageResponse): string {
    return this.chatService.getFileUrl(message.fileUrl!);
  }

  getFileSize(message: ChatMessageResponse): string {
    return this.chatService.formatFileSize(message.fileSize || 0);
  }
}
```

---

### 4. Cập Nhật Template HTML

```html
<!-- chat.component.html -->

<!-- Message List -->
<div class="messages-container">
  <div *ngFor="let message of messages" class="message" [ngClass]="{'me': message.me}">
    
    <!-- TEXT Message -->
    <div *ngIf="isTextMessage(message)" class="text-message">
      <p>{{ message.message }}</p>
    </div>

    <!-- 🆕 IMAGE Message -->
    <div *ngIf="isImageMessage(message)" class="image-message">
      <p *ngIf="message.message">{{ message.message }}</p>
      <img 
        [src]="getImageUrl(message)" 
        [alt]="message.fileName"
        class="chat-image"
        (click)="openImagePreview(message)">
      <small>{{ message.fileName }} ({{ getFileSize(message) }})</small>
    </div>

    <!-- 🆕 FILE Message -->
    <div *ngIf="isFileMessage(message)" class="file-message">
      <p *ngIf="message.message">{{ message.message }}</p>
      <a [href]="getFileUrl(message)" [download]="message.fileName" class="file-link">
        <i class="icon-file"></i>
        <div>
          <strong>{{ message.fileName }}</strong>
          <small>{{ getFileSize(message) }}</small>
        </div>
        <i class="icon-download"></i>
      </a>
    </div>

    <span class="timestamp">{{ message.createdDate | date:'short' }}</span>
  </div>
</div>

<!-- Input Area -->
<div class="input-area">
  <!-- 🆕 Image Upload Button -->
  <button type="button" (click)="imageInput.click()">
    <i class="icon-image"></i>
  </button>
  <input #imageInput type="file" accept="image/*" 
         (change)="onImageSelected($event)" style="display: none">

  <!-- 🆕 File Upload Button -->
  <button type="button" (click)="fileInput.click()">
    <i class="icon-paperclip"></i>
  </button>
  <input #fileInput type="file" 
         accept=".pdf,.doc,.docx,.xls,.xlsx,.txt,.zip,.rar"
         (change)="onFileSelected($event)" style="display: none">

  <!-- Text Input -->
  <textarea [(ngModel)]="messageText" 
            placeholder="Type a message..."
            (keydown.enter)="sendMessage()">
  </textarea>

  <!-- Send Button -->
  <button type="button" (click)="sendMessage()" 
          [disabled]="!messageText.trim() && !selectedFile">
    <i class="icon-send"></i>
  </button>
</div>

<!-- 🆕 File Preview (when selected) -->
<div *ngIf="selectedFile" class="file-preview">
  <span>{{ selectedFile.name }}</span>
  <button (click)="selectedFile = null">Remove</button>
</div>
```

---

## 🔄 Socket.IO - Không Thay Đổi

Socket.IO vẫn hoạt động như cũ. Khi nhận event `message`, chỉ cần parse và kiểm tra `messageType`:

```typescript
socket.on('message', (data: string) => {
  const message: ChatMessageResponse = JSON.parse(data);
  
  // Handle dựa vào messageType
  switch (message.messageType) {
    case MessageType.TEXT:
      // Hiển thị text như cũ
      break;
    case MessageType.IMAGE:
      // Hiển thị image
      break;
    case MessageType.FILE:
      // Hiển thị file link
      break;
  }
});
```

---

## ✅ Checklist Thay Đổi Frontend

### 1. TypeScript Models
- [ ] Thêm enum `MessageType`
- [ ] Cập nhật interface `ChatMessageRequest` (thêm messageType, fileUrl, fileName, fileSize)
- [ ] Cập nhật interface `ChatMessageResponse` (thêm các field mới)
- [ ] Tạo interface `UploadResponse`

### 2. Service
- [ ] Thêm method `uploadChatFile(file, type)`
- [ ] Cập nhật `sendTextMessage()` - thêm messageType
- [ ] Thêm method `sendImageMessage(conversationId, file, caption)`
- [ ] Thêm method `sendFileMessage(conversationId, file, description)`
- [ ] Thêm helper `getImageUrl(fileUrl)`
- [ ] Thêm helper `getFileUrl(fileUrl)`
- [ ] Thêm helper `formatFileSize(bytes)`

### 3. Component
- [ ] Thêm `selectedFile: File | null`
- [ ] Cập nhật `sendMessage()` - handle cả file và text
- [ ] Thêm `onImageSelected(event)`
- [ ] Thêm `onFileSelected(event)`
- [ ] Thêm helper methods: `isTextMessage()`, `isImageMessage()`, `isFileMessage()`
- [ ] Thêm `getImageUrl()`, `getFileUrl()`, `getFileSize()`

### 4. Template
- [ ] Thêm buttons upload image và file
- [ ] Thêm input file elements (hidden)
- [ ] Cập nhật message display - thêm cases cho IMAGE và FILE
- [ ] Thêm file preview khi selected
- [ ] Style cho image và file messages

### 5. Validation
- [ ] Validate file types (image: jpg, png, gif, webp | file: pdf, doc, etc.)
- [ ] Validate file size (max 50MB)
- [ ] Show error messages khi validation fail

---

## 🧪 Testing Frontend

### Test Cases:
1. **Upload Image**: Chọn image → Upload thành công → Gửi message
2. **Upload File**: Chọn file → Upload thành công → Gửi message
3. **Send Text**: Gõ text → Gửi thành công
4. **View Image**: Click vào image trong chat → Hiển thị full size
5. **Download File**: Click vào file link → Download thành công
6. **Socket.IO**: Gửi message → User khác nhận real-time

---

## 🚨 Breaking Changes

### ⚠️ CẨN THẬN:
1. **ChatMessageRequest.message** không còn bắt buộc (`@NotBlank` đã bỏ)
2. **ChatMessageRequest.messageType** BẮT BUỘC phải có
3. Response luôn có thêm 4 fields mới (messageType, fileUrl, fileName, fileSize)

### Migration:
```typescript
// ❌ Code cũ - SẼ LỖI
const request = {
  conversationId: 'conv-123',
  message: 'Hello'
  // Thiếu messageType
};

// ✅ Code mới - OK
const request = {
  conversationId: 'conv-123',
  message: 'Hello',
  messageType: MessageType.TEXT  // PHẢI CÓ
};
```

---

## 📞 Support

Nếu có vấn đề:
1. Check API response structure trong Network tab
2. Verify file upload thành công (check uploadedFileName)
3. Verify messageType được gửi đúng
4. Check console errors

**Full documentation**: Xem các file `angular-chat-*.ts` và `.html` trong project để có complete code example.

---

**Last Updated**: December 23, 2025  
**Backend Version**: 1.0.0  
**Status**: ✅ Ready for Frontend Integration

