# 🧠 Applet Suite Backend

Welcome to the **Applet Suite Backend**, a powerful and efficient REST API built with **Spring Boot**.  
This backend provides secure and fast communication for all applets in the **Applet Suite** project — including the calculator, chatbot, weather, converter, color generator, password creator, movie searcher, and audio player.

---

## ⚙️ Features

- 🔄 RESTful API endpoints for each applet  
- ⚡ Fast and secure communication with the React frontend  
- 🌦️ Real-time weather data integration  
- 🎬 Movie search powered by external APIs  
- 🤖 Chatbot with customizable responses  
- 🔐 Password generator and converter utilities  
- 🎨 Random color generator and audio playback handling  
- 🧮Useful calculator with operations history 
- 🧩 Configurable environment through `application.properties`  
---
## 🧱 Project Structure

Backend/
├── src/
│   └── main/
│       ├── java/com/applet/applet_backend/
│       │   ├── config/                     # Global configuration (CORS, REST templates, static resources)
│       │   ├── Controllers/                # REST endpoints for each applet
│       │   │   ├── CalculadoraController.java
│       │   │   ├── ChatbotController.java
│       │   │   ├── ClimaController.java
│       │   │   ├── ColoresController.java
│       │   │   ├── ConversorController.java
│       │   │   ├── PasswordController.java
│       │   │   ├── PeliculasController.java
│       │   │   └── ReproductorController.java
│       │   ├── models/                     # Request and response objects
│       │   │   ├── BotRequest.java
│       │   │   └── BotResponse.java                        
│       │   ├── service/                    # Business logic (e.g., ChatbotService.java)
│       │   │   └── ChatbotService.java    
│       │   └── AppletsApplication.java     # Spring Boot main class
│       └── resources/
│           ├── static/                     # Static assets (if needed)
│           ├── templates/                  # HTML templates (if used)
│           └── application.properties.example  # Safe example for public repos
├── .env                                    # Local environment variables (not uploaded)
├── .gitignore
└── HELP.md

---

## 🚀 Technologies Used

- **Java 17+**
- **Spring Boot**
- **Maven**
- **REST API**
- **Environment Variables (.env)**
- **External APIs (Weather, Movies, Chatbot, etc.)**

---

## 🔒 Environment Setup

To keep your API keys safe, use a `application.properties` or `.env` file in your project root (not uploaded to GitHub).

Example:

application.properties

# YouTube API Configuration
youtube.api.key=TU_CLAVE_YOUTUBE_AQUI

# Google Gemini Configuration
google.api.key=TU_CLAVE_GEMINI_AQUI

# OMDB API Configuration
omdb.apikey=TU_CLAVE_OMDB_AQUI

# WeatherAPI Configuration
weatherapi.key=TU_CLAVE_WEATHER_AQUI

See the file:
application.properties.example

---
## ▶️ How to Run the Project

Clone the repository:


git clone https://github.com/yourusername/Applet-suite-Backend.git
Navigate to the project folder:

cd Applet-suite-Backend

Add your  file with the required API keys.

Run the project:

mvn spring-boot:run

The backend will be available at:

http://localhost:8080

---

## 👥 Developed By
Marlon Pérez R.

https://portfolio-mu-fawn-47.vercel.app/

https://github.com/MarlonPerezR

https://www.linkedin.com/in/marlonpérez/