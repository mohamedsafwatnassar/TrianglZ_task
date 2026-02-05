ChatApp - Real-Time Chat Application
A modern Android chat application built with Clean Architecture, Jetpack Compose, and Firebase. Features real-time messaging, media sharing, offline support, and background message sending.
📋 Features
Core Features

✅ Real-time Messaging: Instant message delivery using Firebase Realtime Database
✅ Media Sharing: Send images and videos (up to 5 per message)
✅ Username Setup: One-time username configuration with device ID identification
✅ Message Status: Visual indicators (Sending, Sent, Failed)
✅ Retry Failed Messages: Tap to retry failed message delivery
✅ Message Deletion: Delete your own messages
✅ Pagination: Load older messages on scroll with loading indicators
✅ Offline Support: Messages queued for sending when network is unavailable

Bonus Features

✅ Multiple Media Support: Send up to 5 media items in a single message
🔄 Typing Indicators: (Implementation ready, needs UI integration)

Technical Highlights

✅ WorkManager Integration: Reliable message sending with retry logic
✅ Foreground Notifications: User-visible progress for message sending
✅ Granular Permissions: Scoped storage access (no full storage permission)
✅ Material 3 Design: Modern UI following Material Design 3 guidelines
✅ Clean Architecture: Separation of concerns with domain, data, and presentation layers
✅ MVI Pattern: Unidirectional data flow with immutable state
✅ Comprehensive Testing: Unit tests for critical components
