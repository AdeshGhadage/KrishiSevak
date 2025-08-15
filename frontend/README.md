# KrishiSevak - Intelligent Agricultural Advisor

KrishiSevak is an intelligent, multilingual agricultural advisor specifically designed for farmers, providing comprehensive farming support through voice and text interactions. The application integrates advanced features like plant disease detection, weather forecasting, price management, and government scheme assistance.

## 🌾 Features

### 🔍 Plant Disease Detection System
- **Image-based disease identification** using smartphone cameras
- **Treatment recommendations** with organic and chemical options
- **Disease severity assessment** and prevention strategies
- **Expert consultation options** for complex cases

### 🌤️ Local Weather & Irrigation Support
- **Village-level weather forecasts** based on GPS location
- **Crop-specific irrigation scheduling** based on soil moisture and rainfall prediction
- **Real-time weather updates** and alerts
- **Irrigation optimization** recommendations

### 💰 Fertilizer and Crop Price Management
- **Real-time market prices** for crops and fertilizers
- **Price trend analysis** and forecasting
- **Market location information** and contact details
- **Price alerts** and notifications

### 🏛️ Government Schemes Aid
- **Automated eligibility checking** for subsidies and schemes
- **Application process guidance** and document requirements
- **Scheme updates** and notifications
- **Status tracking** for applications

### 🗣️ Multilingual Voice Interface
- **Input and output** in Hindi, English, and over 10 regional languages
- **Voice commands** for all application features
- **Text-to-speech** responses
- **Language switching** capabilities

## 🛠️ Technology Stack

### Frontend (Android)
- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern UI toolkit
- **Material 3** - Design system
- **Navigation Component** - Screen navigation

### Camera & Media
- **CameraX** - Camera functionality
- **TensorFlow Lite** - On-device image processing
- **Room Database** - Local data storage

### Networking & APIs
- **Retrofit** - HTTP client
- **Open-Meteo** - Free weather API
- **OkHttp** - HTTP client library

### Speech & Language
- **Vosk** - Offline speech recognition
- **eSpeak-NG** - Text-to-speech engine
- **Multilingual support** for Indian languages

### Data & Storage
- **Room Database** - Local SQLite database
- **SharedPreferences** - User settings
- **File storage** - Image and document storage

## 📱 Screenshots

The application includes the following main screens:
- **Home Screen** - Feature overview and quick actions
- **Camera Screen** - Plant disease detection
- **Weather Screen** - Weather information and irrigation advice
- **Disease Screen** - Detailed disease analysis
- **Schemes Screen** - Government scheme information
- **Prices Screen** - Crop and fertilizer prices
- **Voice Screen** - Voice interface

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24 (API level 24) or higher
- Kotlin 1.8.0 or higher
- JDK 11 or higher

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/KrishiSevak.git
   cd KrishiSevak
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Open the project folder
   - Wait for Gradle sync to complete

3. **Configure API Keys** (if needed)
   - Update `AndroidManifest.xml` with your Google Maps API key
   - Configure any additional API endpoints

4. **Build and Run**
   - Connect an Android device or start an emulator
   - Click "Run" in Android Studio
   - The app will install and launch on your device

### Build Configuration

The project uses Gradle with the following key configurations:

```kotlin
android {
    compileSdk = 36
    minSdk = 24
    targetSdk = 36
    
    buildFeatures {
        compose = true
        viewBinding = true
        dataBinding = true
    }
}
```

## 📁 Project Structure

```
app/src/main/java/com/example/krishisevak/
├── data/
│   ├── models/           # Data models and entities
│   ├── local/            # Room database and DAOs
│   ├── remote/           # API services and DTOs
│   └── repository/       # Data repositories
├── ui/
│   ├── home/             # Home screen
│   ├── camera/           # Camera and disease detection
│   ├── weather/          # Weather and irrigation
│   ├── disease/          # Disease analysis
│   ├── schemes/          # Government schemes
│   ├── prices/           # Price management
│   ├── voice/            # Voice interface
│   └── theme/            # UI theme and styling
├── utils/                 # Utility classes
└── MainActivity.kt        # Main activity
```

## 🔧 Configuration

### Permissions
The application requires the following permissions:
- **Camera** - For plant disease detection
- **Location** - For weather and location-based services
- **Internet** - For API calls and data synchronization
- **Storage** - For saving images and documents
- **Microphone** - For voice interface

### API Configuration
- **Weather API**: Open-Meteo (free, no API key required)
- **Maps API**: Google Maps (requires API key)
- **Additional APIs**: Can be configured as needed

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### UI Tests
```bash
./gradlew connectedAndroidTest
```

## 📊 Data Models

The application uses comprehensive data models for:
- **Plants** - Crop information and characteristics
- **Diseases** - Disease details and treatments
- **Weather** - Current and forecast weather data
- **Prices** - Market price information
- **Schemes** - Government scheme details
- **Users** - Farmer profiles and preferences

## 🌐 API Integration

### Weather API (Open-Meteo)
- Free weather data service
- No API key required
- Supports global coverage
- Provides current weather and forecasts

### Future API Integrations
- **Plant Disease API** - For disease identification
- **Price APIs** - For market data
- **Government APIs** - For scheme information

## 🔒 Security & Privacy

- **Local data storage** for sensitive information
- **Secure API communication** using HTTPS
- **Permission-based access** to device features
- **User data protection** and privacy controls

## 🤝 Contributing

We welcome contributions to KrishiSevak! Please read our contributing guidelines:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

### Development Guidelines
- Follow Kotlin coding standards
- Use Material 3 design principles
- Implement proper error handling
- Add comprehensive documentation
- Write unit tests for new features

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Open-Meteo** for free weather data
- **TensorFlow** for machine learning capabilities
- **AndroidX** for modern Android development tools
- **Material Design** for UI/UX guidelines

## 📞 Support

For support and questions:
- **Email**: support@krishisevak.com
- **Documentation**: [docs.krishisevak.com](https://docs.krishisevak.com)
- **Issues**: [GitHub Issues](https://github.com/yourusername/KrishiSevak/issues)

## 🔮 Roadmap

### Phase 1 (Current)
- ✅ Basic UI structure
- ✅ Camera integration
- ✅ Weather display
- ✅ Navigation framework

### Phase 2 (Next)
- 🔄 Plant disease detection
- 🔄 Voice interface
- 🔄 Price management
- 🔄 Government schemes

### Phase 3 (Future)
- 📋 Advanced ML models
- 📋 Offline capabilities
- 📋 Expert consultation
- 📋 Community features

---

**KrishiSevak** - Empowering farmers with intelligent agricultural technology.
