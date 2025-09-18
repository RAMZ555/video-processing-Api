# video-processing-Api

# 🎥 Video Processing API - Spring Boot Backend

**Professional video processing platform built with Spring Boot + FFmpeg for enterprise-grade video manipulation**

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.0-red.svg)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://www.docker.com/)

---

## 🚀 **What This System Does**

A complete video processing backend that handles **everything** from upload to multi-quality output generation with **Indian language support**. Built for the Backend Engineer assignment with **zero boilerplate code** using modern Java practices.

### **✅ All 5 Levels Implemented:**
- **Level 1**: Video Upload & Automatic Metadata Extraction
- **Level 2**: Precise Video Trimming with FFmpeg
- **Level 3**: Text/Image Overlays + Watermarking (Hindi, Tamil, Telugu support!)
- **Level 4**: Async Job Queue with Real-time Status Updates
- **Level 5**: Multiple Quality Generation (480p/720p/1080p)

---

## 🛠 **Technology Stack**

| Component | Technology | Purpose |
|-----------|------------|---------|
| **Backend Framework** | Spring Boot 3.2.0 | RESTful API with auto-configuration |
| **Database** | MySQL 8.0 | Video metadata & job tracking |
| **Cache/Queue** | Redis 7.0 | Async job status & fast lookups |
| **Video Processing** | FFmpeg | Professional video manipulation |
| **ORM** | Spring Data JPA + Hibernate | Zero-boilerplate database operations |
| **Code Simplification** | Lombok | Eliminates getter/setter boilerplate |
| **Documentation** | OpenAPI 3.0 (Swagger) | Interactive API documentation |
| **Containerization** | Docker + Docker Compose | Production deployment |
| **Build Tool** | Maven | Dependency management |
| **Real-time Updates** | WebSocket (STOMP) | Live job progress updates |

---

## 🏗 **System Architecture**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client Apps   │    │  Load Balancer  │    │     CDN         │
│ (Web/Mobile)    │◄──►│    (Nginx)      │◄──►│ (Static Files)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │
         ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Spring Boot    │    │     Redis       │    │     MySQL       │
│  Video API      │◄──►│  Job Queue &    │    │  Video Metadata │
│  (Port 8080)    │    │  Status Cache   │    │  & Relationships│
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │
         ▼
┌─────────────────┐    ┌─────────────────┐
│    FFmpeg       │    │  File Storage   │
│  Video Engine   │◄──►│  Uploads/Output │
└─────────────────┘    └─────────────────┘
```

---

## 📁 **Project Structure**

```
video-processing-api/
├── src/main/java/com/videoprocessing/
│   ├── VideoProcessingApplication.java       # 🚀 Main Spring Boot app
│   ├── entity/                               # 📊 JPA Entities (zero boilerplate)
│   │   ├── Video.java                        # Core video metadata
│   │   ├── TrimmedVideo.java                 # Trimmed video references
│   │   ├── VideoOverlay.java                 # Overlay configurations
│   │   ├── ProcessingJob.java                # Async job tracking
│   │   └── VideoQualityVersion.java          # Quality variants
│   ├── repository/                           # 🗄️ Spring Data JPA (zero code!)
│   │   ├── VideoRepository.java              
│   │   ├── ProcessingJobRepository.java      
│   │   └── [Other repositories...]
│   ├── dto/                                  # 📋 Request/Response models
│   │   ├── VideoResponse.java
│   │   ├── TrimVideoRequest.java
│   │   ├── AddOverlayRequest.java
│   │   └── ApiResponse.java
│   ├── service/                              # 💼 Business logic
│   │   └── VideoProcessingService.java       # Core processing engine
│   ├── controller/                           # 🌐 REST endpoints
│   │   ├── VideoController.java              # Main API routes
│   │   └── AdminController.java              # Admin functions
│   └── config/                               # ⚙️ Configuration
│       ├── AsyncConfig.java                  # Async processing setup
│       ├── WebSocketConfig.java              # Real-time updates
│       └── OpenApiConfig.java                # API documentation
├── src/main/resources/
│   ├── application.yml                       # Main configuration
│   ├── application-dev.yml                   # Development settings
│   └── application-prod.yml                  # Production settings
├── docker-compose.yml                        # 🐳 Multi-service setup
├── Dockerfile                                # Production container
├── pom.xml                                   # Maven dependencies
└── fonts/                                    # 🌏 Indian language fonts
    ├── NotoSansDevanagari-Regular.ttf        # Hindi
    ├── NotoSansTamil-Regular.ttf             # Tamil
    └── [Other language fonts...]
```

---

## 🚀 **Quick Start Guide**

### **Prerequisites**
- Java 17+
- Docker & Docker Compose
- Git

### **1. Clone & Setup**
```bash
git clone https://github.com/yourusername/video-processing-api.git
cd video-processing-api
```

### **2. One-Command Deployment**
```bash
# Start everything with Docker Compose
docker-compose up -d

# Check services are running
docker-compose ps
```

### **3. Access the Application**
- **API**: http://localhost:8080
- **Swagger Docs**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

### **4. Test Video Upload**
```bash
curl -X POST "http://localhost:8080/api/v1/upload" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@your-video.mp4"
```

---

## 🎯 **Core Features Explained**

### **🎬 Level 1: Video Upload & Metadata**
- **Smart Upload Handling**: Validates video files, generates UUIDs
- **Automatic Metadata**: Extracts duration, resolution, file size using FFmpeg
- **Database Storage**: Stores all metadata in MySQL with optimized indexes
- **File Management**: Organized storage in uploads directory

**API Endpoint**: `POST /api/v1/upload`

### **✂️ Level 2: Video Trimming**
- **Precise Cutting**: Frame-accurate trimming with start/end timestamps
- **Fast Processing**: Uses FFmpeg copy codec for speed
- **Relationship Tracking**: Links trimmed videos to originals
- **Async Processing**: Non-blocking operations with job IDs

**API Endpoint**: `POST /api/v1/trim`
```json
{
  "videoId": 1,
  "startTime": 10.5,
  "endTime": 25.0
}
```

### **🌈 Level 3: Overlays & Watermarking**
- **Text Overlays**: Support for 10+ Indian languages
- **Font Management**: Noto fonts for proper rendering
- **Image Overlays**: PNG/JPG overlay support with positioning
- **Watermarking**: Logo placement with opacity control
- **Timing Control**: Start/end time specifications

**Supported Languages**: Hindi, Tamil, Telugu, Bengali, Marathi, Gujarati, Kannada, Malayalam, Punjabi, Odia

**API Endpoints**: `POST /api/v1/overlay`, `POST /api/v1/watermark`

### **⚡ Level 4: Async Job Queue**
- **Immediate Response**: API returns job ID instantly
- **Background Processing**: Spring @Async with thread pools
- **Redis Integration**: Fast job status lookups
- **Real-time Updates**: WebSocket notifications
- **Status Tracking**: PENDING → PROCESSING → COMPLETED
- **Error Handling**: Failed job retry mechanisms

**API Endpoints**: 
- `GET /api/v1/status/{jobId}` - Check job status
- `GET /api/v1/result/{jobId}` - Download processed video

### **📱 Level 5: Multiple Quality Generation**
- **Smart Encoding**: H.264 with optimal settings
- **Multiple Formats**: 480p (mobile), 720p (tablet), 1080p (desktop)
- **Parallel Processing**: Multiple qualities generated simultaneously  
- **Storage Optimization**: Efficient file size management
- **CDN Ready**: Optimized for content delivery networks

**API Endpoint**: `POST /api/v1/qualities`
```json
{
  "videoId": 1,
  "qualities": ["480p", "720p", "1080p"]
}
```

---

