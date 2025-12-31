# Typing Indicator Feature

## Overview
This feature allows real-time typing indicators in the chat system. When a user is typing a message, other participants in the conversation will be notified.

## Architecture

### Backend Components

1. **TypingEventRequest.java** - DTO for typing events
   - `conversationId`: ID of the conversation
   - `userId`: ID of the user who is typing
   - `userName`: Name of the user who is typing
   - `isTyping`: Boolean indicating if user is typing (true) or stopped typing (false)

2. **SocketHandler.java** - WebSocket event handler
   - `@OnEvent("typing")`: Listens for typing events from clients
   - Broadcasts typing status to other participants in the conversation
   - Uses `userTyping` event to notify clients

3. **ConversationService.java** - Added method
   - `getConversationById(Long conversationId)`: Retrieves conversation by ID

4. **WebSocketSessionService.java** - Added method
   - `getSessionsByUserIds(List<String> userIds)`: Gets active WebSocket sessions for specific users

## How It Works

### Flow
1. User starts typing in a conversation
2. Frontend emits a `typing` event with TypingEventRequest data
3. Backend receives the event in SocketHandler
4. Backend fetches the conversation and its participants
5. Backend finds active WebSocket sessions of other participants (excluding sender)
6. Backend broadcasts `userTyping` event to all other participants
7. Other participants receive the event and display typing indicator

### WebSocket Events

#### Client → Server: `typing`
```javascript
socket.emit('typing', {
    conversationId: 123,
    userId: 456,
    userName: "John Doe",
    isTyping: true
});
```

#### Server → Client: `userTyping`
```javascript
socket.on('userTyping', (data) => {
    // data contains: conversationId, userId, userName, isTyping
    if (data.isTyping) {
        showTypingIndicator(data.userName);
    } else {
        hideTypingIndicator(data.userName);
    }
});
```

## Frontend Integration Example

### React/Vue/Angular Example
```javascript
import io from 'socket.io-client';

// Connect to WebSocket server
const socket = io('http://localhost:8080', {
    query: { token: yourAuthToken }
});

// When user types
const handleTyping = (conversationId, userId, userName) => {
    socket.emit('typing', {
        conversationId: conversationId,
        userId: userId,
        userName: userName,
        isTyping: true
    });
};

// When user stops typing (use debounce)
const handleStopTyping = (conversationId, userId, userName) => {
    socket.emit('typing', {
        conversationId: conversationId,
        userId: userId,
        userName: userName,
        isTyping: false
    });
};

// Listen for typing events from others
socket.on('userTyping', (data) => {
    console.log(`${data.userName} is typing...`);
    // Update UI to show typing indicator
});

// Example with debounce
let typingTimer;
const TYPING_TIMEOUT = 3000; // 3 seconds

inputField.addEventListener('input', () => {
    // User is typing
    handleTyping(currentConversationId, currentUserId, currentUserName);
    
    // Clear existing timer
    clearTimeout(typingTimer);
    
    // Set timer to stop typing after 3 seconds of inactivity
    typingTimer = setTimeout(() => {
        handleStopTyping(currentConversationId, currentUserId, currentUserName);
    }, TYPING_TIMEOUT);
});
```

### Complete React Component Example
```jsx
import { useEffect, useState, useRef } from 'react';
import io from 'socket.io-client';

const ChatComponent = ({ conversationId, userId, userName }) => {
    const [message, setMessage] = useState('');
    const [typingUsers, setTypingUsers] = useState([]);
    const socketRef = useRef(null);
    const typingTimerRef = useRef(null);

    useEffect(() => {
        // Connect to WebSocket
        socketRef.current = io('http://localhost:8080', {
            query: { token: localStorage.getItem('authToken') }
        });

        // Listen for typing events
        socketRef.current.on('userTyping', (data) => {
            if (data.conversationId === conversationId) {
                if (data.isTyping) {
                    setTypingUsers(prev => [...prev, data.userName]);
                } else {
                    setTypingUsers(prev => prev.filter(name => name !== data.userName));
                }
            }
        });

        return () => {
            socketRef.current.disconnect();
        };
    }, [conversationId]);

    const handleInputChange = (e) => {
        setMessage(e.target.value);

        // Emit typing event
        socketRef.current.emit('typing', {
            conversationId,
            userId,
            userName,
            isTyping: true
        });

        // Clear existing timer
        clearTimeout(typingTimerRef.current);

        // Set timer to stop typing
        typingTimerRef.current = setTimeout(() => {
            socketRef.current.emit('typing', {
                conversationId,
                userId,
                userName,
                isTyping: false
            });
        }, 3000);
    };

    return (
        <div>
            <div className="messages">
                {/* Your messages here */}
            </div>
            
            {typingUsers.length > 0 && (
                <div className="typing-indicator">
                    {typingUsers.join(', ')} {typingUsers.length === 1 ? 'is' : 'are'} typing...
                </div>
            )}
            
            <input 
                type="text" 
                value={message} 
                onChange={handleInputChange}
                placeholder="Type a message..."
            />
        </div>
    );
};

export default ChatComponent;
```

## Best Practices

1. **Debouncing**: Always use debouncing to prevent sending too many typing events
2. **Timeout**: Stop typing indicator after 3-5 seconds of inactivity
3. **Stop on Send**: Send `isTyping: false` immediately when user sends a message
4. **Clear on Leave**: Send `isTyping: false` when user leaves the conversation
5. **UI Feedback**: Show typing indicator only for the current conversation

## Testing

### Manual Testing
1. Open two browser windows with different users
2. Start conversation between users
3. Type in one window
4. Verify typing indicator appears in the other window
5. Stop typing and verify indicator disappears after timeout

### Postman/Socket.io Client Testing
```javascript
// Connect with token
const socket = io('http://localhost:8080?token=YOUR_TOKEN');

// Test typing event
socket.emit('typing', {
    conversationId: 1,
    userId: 123,
    userName: "Test User",
    isTyping: true
});

// Listen for response
socket.on('userTyping', (data) => {
    console.log('Received typing event:', data);
});
```

## Configuration

The typing indicator feature uses the existing WebSocket configuration. Ensure the following is properly configured in `application.properties`:

```properties
# Socket.io configuration (if applicable)
socketio.host=localhost
socketio.port=8080
```

## Troubleshooting

### Issue: Typing events not received
- Verify WebSocket connection is established
- Check authentication token is valid
- Ensure conversationId exists in database

### Issue: Multiple typing indicators
- Check debouncing logic
- Verify `isTyping: false` is sent when needed

### Issue: Typing indicator doesn't clear
- Increase timeout duration
- Send `isTyping: false` explicitly when user leaves conversation
- Handle disconnect events properly

## Future Enhancements

1. **Read Receipts**: Show when message is read
2. **Presence Indicators**: Show online/offline status
3. **Multiple Typers**: Show "User1, User2, and 3 others are typing..."
4. **Typing Speed**: Adjust timeout based on typing speed

