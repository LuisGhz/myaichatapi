# MyAIChat

Chat application API integrated with AI developed with Spring Boot, Spring AI as the main technologies. It supports the following models.

- [GPT 4O](https://platform.openai.com/docs/models/gpt-4o)
- [GPT 4O Mini](https://platform.openai.com/docs/models/gpt-4o-mini)
- [Gemini 2.0 Flash Lite](https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-0-flash-lite?hl=es-419)
- [Gemini 2.0 Flash](https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-0-flash?hl=es-419)
- [Gemini 2.5 Flash Preview 04 17](https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-5-flash?hl=es-419)
- [Gemini 2.5 Pro Preview 03 25](https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-5-pro?hl=es-419)

## Required env variables
- OPENAI_API_KEY
- GEMINI_PROJECT_ID
- GEMINI_LOCATION
- DB_URL: JDBC Postgres URL
- CDN_DOMAIN
- S3_ACCESS_KEY
- S3_SECRET_KEY
- S3_BUCKET_NAME

## Links References
- [Spring AI OpenAI](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html)
- [Spring AI VertexAI Gemini](https://docs.spring.io/spring-ai/reference/api/chat/vertexai-gemini-chat.html)
- [OpenAI Models](https://platform.openai.com/docs/models)
- [Gemini api models](https://ai.google.dev/gemini-api/docs/models?hl=es-419)
- [Gemini vertex models](https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-5-flash?hl=es-419)
- [Vertex Locations](https://cloud.google.com/vertex-ai/generative-ai/docs/learn/locations?hl=es-419)
- [Tutorial Reference To integrate Gemini to SpringBoot](https://loiane.com/2025/01/intelligent-java-applications-using-spring-ai-and-gemini/)