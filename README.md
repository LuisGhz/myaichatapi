# MyAIChat 🤖💬

> Una aplicación de chat inteligente que combina múltiples modelos de IA de última generación para ofrecer conversaciones fluidas, naturales y productivas.

## 🌟 ¿Qué es MyAIChat?

MyAIChat es una API REST moderna desarrollada con **Spring Boot** y **Spring AI** que permite a los usuarios interactuar con los modelos de inteligencia artificial más avanzados disponibles. La aplicación está diseñada para ofrecer una experiencia de chat intuitiva y potente, con funcionalidades avanzadas como procesamiento de imágenes, transcripción de audio y búsqueda web integrada.

- Conversación natural con IA para tareas cotidianas y profesionales.
- Soporte multimodal: texto, imágenes (JPEG/PNG/GIF) y audio (MP3/WAV/M4A).
- Transcripción de voz a texto y análisis contextual de audio e imágenes.
- Prompts personalizables y plantillas reutilizables para flujos de trabajo.
- Historial persistente con títulos automáticos y gestión de favoritos.
- Integración multi-proveedor (OpenAI, Google Vertex AI) con estrategia de fallbacks.
- Autenticación segura (OAuth2 con GitHub) y gestión de sesiones con JWT.
- Almacenamiento en la nube (AWS S3) y CDN para archivos multimedia.
- Base de datos PostgreSQL con JPA/Hibernate y optimización de rendimiento.
- Gestión de tokens, control de costes y límites configurables por conversación.
- API RESTful documentada, manejo centralizado de errores y logging detallado.
- Herramientas para desarrolladores: testing automatizado, migraciones y patrones de diseño.

## 🤖 Modelos de IA Compatibles

### OpenAI

- **[GPT-4O](https://platform.openai.com/docs/models/gpt-4o)** - El modelo más avanzado para tareas complejas.
- **[GPT-4O Mini](https://platform.openai.com/docs/models/gpt-4o-mini)** - Versión optimizada para velocidad y eficiencia.
- **[GPT-4.1](https://platform.openai.com/docs/models/gpt-4.1)** - Modelo para tareas más complejas y solución de problemas.
- **[GPT-4.1 mini](https://platform.openai.com/docs/models/gpt-4.1-mini)** - Versión que proporciona un balance entre inteligencía y velocidad.

### Google Gemini (Vertex AI)

- **[Gemini 2.0 Flash Lite](https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-0-flash-lite)** - Rápido y eficiente para tareas cotidianas
- **[Gemini 2.0 Flash](https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-0-flash)** - Equilibrio perfecto entre velocidad y capacidad
- **[Gemini 2.5 Flash](https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-5-flash)** - Última generación con capacidades mejoradas
- **[Gemini 2.5 Pro](https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-5-pro)** - Máximo rendimiento para tareas profesionales

## ✨ Funcionalidades Principales

### 💬 Sistema de Chat Avanzado

- **Conversaciones ilimitadas** con historial persistente
- **Títulos automáticos** generados por IA para cada chat
- **Gestión de favoritos** para conversaciones importantes
- **Control de tokens** con límites personalizables por conversación
- **Soporte multi-modal** (texto + imágenes)

### 🖼️ Procesamiento de Imágenes

- **Formatos soportados**: JPEG, PNG, GIF
- **Análisis inteligente** de contenido visual
- **Descripción automática** de imágenes
- **Respuestas contextuales** basadas en el contenido visual

### 🎙️ Transcripción de Audio

- **Conversión voz a texto** con alta precisión
- **Múltiples formatos** de audio soportados
- **Procesamiento en tiempo real**
- **Integración con modelos de chat** para análisis posterior

### 🌐 Búsqueda Web Integrada

- **Información actualizada** directamente en las conversaciones
- **Verificación de hechos** en tiempo real
- **Contexto ampliado** para respuestas más precisas

### 📝 Prompts Personalizados

- **Plantillas reutilizables** para tareas específicas
- **Sistema de mensajes** estructurado por prompt
- **Gestión completa** (crear, editar, eliminar)
- **Compartir contexto** entre conversaciones

### 🔐 Seguridad y Autenticación

- **OAuth2 con GitHub** para autenticación segura
- **JWT tokens** para sesiones seguras
- **Control de acceso** basado en usuarios
- **Validación robusta** de entrada de datos

### ☁️ Almacenamiento en la Nube

- **AWS S3** para archivos multimedia
- **CDN integrado** para acceso rápido
- **Gestión automática** de archivos temporales
- **Optimización de almacenamiento**

## 🏗️ Arquitectura Técnica

### Stack Tecnológico

- **Backend**: Spring Boot 3.x, Spring AI, Spring Security
- **Base de Datos**: PostgreSQL con JPA/Hibernate
- **Almacenamiento**: AWS S3 con CDN
- **Autenticación**: OAuth2 + JWT
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Build**: Maven
- **Logging**: Log4j2

### Patrones de Diseño Implementados

- **Strategy Pattern** para múltiples proveedores de IA
- **Repository Pattern** para acceso a datos
- **DTO Pattern** para transferencia de datos
- **Builder Pattern** para construcción de objetos complejos
- **Service Layer** para lógica de negocio

## 🔧 Configuración

### Variables de Entorno Requeridas

#### APIs de Inteligencia Artificial

```bash
OPENAI_API_KEY=tu_clave_openai                # Clave de API de OpenAI
GEMINI_PROJECT_ID=tu_proyecto_google          # ID del proyecto de Google Cloud
GEMINI_LOCATION=us-central1                   # Región de Vertex AI
```

#### Base de Datos

```bash
DB_URL=jdbc:postgresql://localhost:5432/myaichat  # URL de PostgreSQL
```

#### Almacenamiento y CDN

```bash
CDN_DOMAIN=https://tu-cdn.com/                # Dominio de tu CDN
S3_ACCESS_KEY=tu_access_key_aws               # Clave de acceso AWS S3
S3_SECRET_KEY=tu_secret_key_aws               # Clave secreta AWS S3
S3_BUCKET_NAME=tu-bucket-s3                   # Nombre del bucket S3
```

#### Autenticación GitHub (Opcional)

```bash
GITHUB_CLIENT_ID=tu_client_id                 # ID de aplicación GitHub
GITHUB_CLIENT_SECRET=tu_client_secret         # Secret de aplicación GitHub
ALLOWED_ORIGINS=http://localhost:3000         # Orígenes permitidos para CORS
```

### Configuración de Base de Datos

La aplicación utiliza PostgreSQL con las siguientes características:

- **Pool de conexiones optimizado** con HikariCP
- **Migraciones automáticas** con Hibernate DDL
- **Queries optimizadas** con índices apropiados
- **Transacciones ACID** para consistencia de datos

### Límites de Archivos

- **Tamaño máximo por archivo**: 2MB
- **Formatos de imagen**: JPEG, PNG, GIF
- **Formatos de audio**: MP3, WAV, M4A
- **Almacenamiento**: AWS S3 con CDN para acceso rápido

## 🚀 Casos de Uso

### Para Desarrolladores

- **Prototipado rápido** de ideas con IA
- **Análisis de código** y documentación
- **Generación de tests** automatizados
- **Revisión de arquitecturas** de software

### Para Creadores de Contenido

- **Generación de ideas** para artículos y videos
- **Análisis de imágenes** para descripción automática
- **Transcripción de podcasts** y entrevistas
- **Optimización de contenido** para SEO

### Para Profesionales

- **Análisis de documentos** técnicos
- **Resúmenes ejecutivos** automáticos
- **Traducción de contenido** especializado
- **Investigación de mercado** con datos actualizados

### Para Estudiantes

- **Explicación de conceptos** complejos
- **Resolución de problemas** paso a paso
- **Análisis de imágenes** científicas
- **Práctica de idiomas** con conversación natural

## 🔄 Flujo de Trabajo Típico

1. **Autenticación**: El usuario se autentica via GitHub OAuth2
2. **Selección de Modelo**: Elige el modelo de IA más apropiado para su tarea
3. **Configuración**: Ajusta parámetros como tokens máximos o búsqueda web
4. **Interacción**: Envía mensajes de texto, imágenes o archivos de audio
5. **Procesamiento**: La IA procesa la entrada y genera una respuesta contextual
6. **Gestión**: Organiza conversaciones con títulos, favoritos y categorías
7. **Reutilización**: Usa prompts personalizados para tareas recurrentes

## 📊 Métricas y Monitoreo

### Control de Costos

- **Tracking de tokens** por conversación y usuario
- **Límites configurables** por chat
- **Estadísticas de uso** por modelo
- **Optimización automática** de requests

### Performance

- **Pool de conexiones** optimizado para base de datos
- **Cache inteligente** para requests frecuentes
- **Compresión de archivos** para reducir latencia
- **Logging detallado** para debugging

## 🔗 Referencias y Documentación

### Spring AI

- **[OpenAI Integration](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html)** - Documentación oficial
- **[Vertex AI Gemini](https://docs.spring.io/spring-ai/reference/api/chat/vertexai-gemini-chat.html)** - Guía de integración

### Modelos de IA

- **[OpenAI Models](https://platform.openai.com/docs/models)** - Especificaciones técnicas
- **[Gemini API](https://ai.google.dev/gemini-api/docs/models)** - Documentación completa
- **[Gemini Pricing](https://ai.google.dev/gemini-api/docs/pricing)** - Información de costos

### Google Cloud

- **[Vertex AI Locations](https://cloud.google.com/vertex-ai/generative-ai/docs/learn/locations)** - Regiones disponibles
- **[Gemini Models](https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-5-flash)** - Especificaciones

## Google custom search

- **[Custom search](https://developers.google.com/custom-search/v1/overview)** - Documentación Google Custom Search API
- **[Custom search engine](https://programmablesearchengine.google.com/controlpanel/all)** - Crear un buscador programable que será necesario para el Custom Search API

## Tool function calling
- **[Tool function calling post](https://golb.hplar.ch/2025/01/spring-ai-tool.html)** - Un post acerca de tool function calling, en este proyecto se utiliza para realizar las busquedas con Google Custom Search API

### Tutoriales

- **[Spring AI + Gemini](https://loiane.com/2025/01/intelligent-java-applications-using-spring-ai-and-gemini/)** - Tutorial completo de integración
