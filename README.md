<<<<<<< HEAD
# DialGenie - Enterprise AI-Powered Telecalling SaaS Platform

A production-ready, scalable platform for automated AI-powered telephone calls with intelligent conversation management, sentiment analysis, and lead scoring.

## 🚀 Features

### Core Capabilities
- **AI-Powered Calls**: Dynamic conversation using LangChain + OpenAI/Azure
- **Intelligent Lead Management**: Excel upload, validation, deduplication, scoring
- **Campaign Management**: Create, monitor, and optimize calling campaigns
- **Call Lifecycle Management**: Initiate, monitor, track, and record calls via Twilio
- **Conversation Intelligence**: Real-time speech-to-text, sentiment analysis, objection handling
- **Follow-up Automation**: Smart scheduling based on call outcomes
- **Comprehensive Call Analytics**: Transcripts, recordings, summaries, quality metrics

### Enterprise Features
- **Multi-Tenant Architecture**: Isolated organizations with proper RBAC
- **JWT Authentication**: Industry-standard token-based security
- **Audit Logging**: Complete activity tracking for compliance
- **Rate Limiting**: API protection against abuse
- **Horizontal Scaling**: Async processing with Kafka
- **High Availability**: Redis caching, connection pooling, failover mechanisms
- **Compliance**: DND flag support, GDPR-ready audit logs

## 📋 Architecture

### Microservices Design
```
┌─────────────────────────────────────────────────────┐
│                    Nginx + React                     │
│              (Frontend - Port 3000/80)               │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│           API Gateway (Spring Cloud Gateway)        │
│                  (Port 8080)                        │
└──────────┬──────────┬──────────┬────────────────────┘
           │          │          │
    ┌──────▼──┐  ┌────▼────┐  ┌──▼───────┐
    │  Auth   │  │  Lead   │  │Campaign  │
    │ Service │  │ Service │  │ Service  │
    │(8081)   │  │ (8082)  │  │ (8083)   │
    └────────┬┘  └────┬────┘  └──┬───────┘
             │        │          │
             │   ┌────▼────┐     │
             │   │  Call   │     │
             └──▶│ Service │◀────┘
                 │ (8084)  │
                 └────┬────┘
                      │
    ┌─────────────────▼─────────────────┐
    │    Python AI Service (8085)       │
    │  (LangChain + OpenAI/Azure)      │
    └─────────────────────────────────┘

Shared Services:
- Kafka: Event-driven communication
- Redis: Caching & conversation memory
- SQLite: Persistent data storage
```

## 🏗️ Technology Stack

### Backend
- **Java 17** with Spring Boot 3.2
- **Spring Cloud Gateway** for API routing
- **Spring Data JPA** with SQLite
- **Spring Security** + JWT
- **Spring Kafka** for async communication
- **Spring Data Redis** for caching

### AI & NLP
- **Python 3.11** with FastAPI
- **LangChain** for LLM orchestration
- **OpenAI API** or **Azure Speech Services**
- **Transformers** for sentiment analysis
- **Redis** for conversation memory

### Infrastructure
- **Docker** & **Docker Compose**
- **Kafka** & **Zookeeper** (event streaming)
- **Redis** (caching & session management)
- **SQLite** (lightweight embedded database)
- **Nginx** (reverse proxy & static content)

### Frontend
- **React 18** with Hooks
- **Vite** for build optimization
- **Material-UI** for responsive design
- **Axios** for API communication
- **Recharts** for analytics

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Node.js 18+ (for frontend development)
- Java 17 (for backend development)
- Python 3.11 (for AI service development)

### Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourorgs/dialgenie.git
   cd dialgenie
   ```

2. **Start infrastructure**
   ```bash
   cd infrastructure
   cp .env.example .env
   docker-compose up -d
   ```

3. **Build backend modules**
   ```bash
   cd backend/shared
   mvn clean install
   cd ../auth-service && mvn spring-boot:run
   cd ../lead-service && mvn spring-boot:run
   cd ../campaign-service && mvn spring-boot:run
   cd ../call-service && mvn spring-boot:run
   cd ../api-gateway && mvn spring-boot:run
   ```

4. **Start AI service**
   ```bash
   cd ai-service
   pip install -r requirements.txt
   python -m uvicorn src.main.python.main:app --reload --port 8085
   ```

5. **Start frontend**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

6. **Access the application**
   - Frontend: http://localhost:3000
   - API Gateway: http://localhost:8080
   - Kafka UI: http://localhost:8161 (optional)

## 📚 API Endpoints

### Authentication
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/refresh` - Refresh JWT token
- `GET /api/v1/auth/validate` - Validate token

### Leads
- `POST /api/v1/leads` - Create lead
- `GET /api/v1/leads/{leadId}` - Get lead details
- `GET /api/v1/leads/campaign/{campaignId}` - Get campaign leads
- `PUT /api/v1/leads/{leadId}/status` - Update lead status
- `PUT /api/v1/leads/{leadId}/solution` - Update solution

### Campaigns
- `POST /api/v1/campaigns` - Create campaign
- `GET /api/v1/campaigns/{campaignId}` - Get campaign
- `GET /api/v1/campaigns/organization/{orgId}` - List campaigns
- `PUT /api/v1/campaigns/{campaignId}/status` - Update status

### Calls
- `POST /api/v1/calls` - Initiate call
- `GET /api/v1/calls/{callLogId}` - Get call details
- `GET /api/v1/calls/history/{leadId}` - Get call history
- `PUT /api/v1/calls/{callLogId}/status` - Update call status
- `PUT /api/v1/calls/{callLogId}/outcome` - Update call outcome

### AI Service
- `POST /api/v1/ai/call/init` - Initialize AI call
- `POST /api/v1/ai/process-response` - Process user response
- `POST /api/v1/ai/call-summary` - Generate call summary

## 🗄️ Database Schema

The database includes comprehensive tables for:
- **Users & Organizations**: Multi-tenant support with roles
- **Campaigns**: Campaign management with status tracking
- **Leads**: Lead storage with concerns and solutions
- **Call Logs**: Complete call history with Twilio integration
- **Transcripts**: Full conversation transcripts and summaries
- **Follow-ups**: Automatic follow-up scheduling
- **Audit Logs**: Complete compliance audit trail
- **Metrics**: Campaign and API usage analytics

See `backend/shared/src/main/resources/schema.sql` for detailed schema.

## 🔐 Security

- **JWT Authentication**: Stateless authentication with expiring tokens
- **RBAC Authorization**: Role-based access control
- **Encryption**: Sensitive fields encrypted (phone, email)
- **Rate Limiting**: API endpoint protection
- **CORS**: Restricted cross-origin requests
- **Audit Logging**: All actions logged for compliance
- **DND Compliance**: Do-not-disturb flag support

## 📈 Monitoring & Metrics

### Key Metrics Tracked
- Call success rate
- AI response accuracy
- API latency
- Error rates
- Lead scores
- Conversion rates

### Monitoring Stack
- **Prometheus**: Metrics collection
- **Grafana**: Visualization dashboards
- **Structured Logging**: JSON format for analysis

## 🚢 Deployment

### Docker Compose (Development)
```bash
cd infrastructure
docker-compose up -d
```

### Production Deployment
See `infrastructure/DEPLOYMENT.md` for comprehensive deployment guide including:
- Kubernetes setup
- Environment configuration
- SSL/TLS setup
- Backup & recovery procedures
- Scaling strategies
- Monitoring setup

## 🔄 CI/CD Pipeline

Recommended setup:
- GitHub Actions for automated testing
- Docker image building and pushing
- Automated deployment to staging/production
- Database migrations
- Health check verification

Example workflow structure provided in `.github/workflows/`

## 📝 Configuration

All services use environment variables for configuration. See `infrastructure/.env.example` for complete list:

```bash
# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# Redis
REDIS_HOST=redis
REDIS_PORT=6379

# Twilio
TWILIO_ACCOUNT_SID=your-account-sid
TWILIO_AUTH_TOKEN=your-auth-token
TWILIO_PHONE_NUMBER=+1234567890

# OpenAI
OPENAI_API_KEY=sk-your-api-key
```

## 🔧 Development Guide

### Adding a New Microservice
1. Create service module in `backend/newservice`
2. Create POM with shared dependency
3. Extend `BaseService` pattern
4. Add routes to API Gateway
5. Add Docker configuration
6. Register in docker-compose.yml

### Adding Database Tables
1. Modify `backend/shared/src/main/resources/schema.sql`
2. Update JPA entities in `backend/shared/src/main/java`
3. Update repositories and DTOs
4. Run migrations on deployment

## 🤝 Contributing

1. Create feature branch
2. Make changes
3. Write/update tests
4. Submit pull request
5. Code review and merge

## 📄 License

Proprietary - DialGenie SaaS Platform

## 🆘 Support

- Documentation: See `/docs` folder
- Issues: GitHub Issues
- Contact: support@dialgenie.com

## 🎯 Roadmap

- [ ] Advanced AI training on call outcomes
- [ ] Voice clone customization
- [ ] Predictive lead scoring
- [ ] Multi-language support
- [ ] Mobile app
- [ ] Advanced analytics dashboard
- [ ] Integration with CRM systems
- [ ] Webhook system for external integration

## 📊 Project Statistics

- **Services**: 6 microservices + 1 AI service
- **Database Tables**: 15+ production-ready tables
- **APIs**: 20+ RESTful endpoints
- **Lines of Code**: 10,000+ lines of production code
- **Docker Containers**: 8+ containerized services

---

**Built with ❤️ for enterprise-grade AI-powered telecommunications**
=======
# DialGenie
AI-Powered Telecalling &amp; Lead Management System that automates customer outreach using conversational AI. The platform allows uploading leads via Excel, triggers automated voice calls using telephony APIs, processes conversations using LLM-based AI, generates call summaries, scores leads, and updates CRM records with analytics dashboards.
It is an ai powered platform
>>>>>>> b5b78878ceafdbf53daa22275cbd7d59d8e0f2ae
