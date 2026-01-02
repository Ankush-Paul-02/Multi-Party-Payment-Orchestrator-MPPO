# 📘 **Split Payment System – Full Backend Architecture (Notion Document)**

---

---

# 🏷 **1. Project Name**

**Multi-Party Payment Orchestrator (MPPO)**

*A scalable system enabling group ordering with split payments.*

---

# 🎯 **2. Overview**

This backend supports a feature where one user can create a **group order**, invite friends, **split the payment**, and the order is confirmed only when **all required payments succeed**.

The system has 3 core microservices:

1. **Group Service** – manages group orders, members, split logic
2. **Payment Service** – handles payment intents, webhooks, verification
3. **Order Service** – final order placement after full payment

Each service runs independently and communicates via events and APIs.

---

# 🧩 **3. High-Level Architecture**

- Client → API Gateway → Group Service
- Group Service → Payment Service → Payment Gateway
- Payment Service → Event Bus → Group Service
- Group Service → Order Service → Merchant Backend
- Notification Service (async)
- Databases (Postgres) + Caches (Redis)

---

# 🏛 **4. Microservices Breakdown**

---

## **4.1 Group Service**

### **Responsibilities**

- Create group order
- Add/remove members
- Compute split amounts (equal/custom)
- Track member join & payment status
- Group state machine:
    
    **DRAFT → OPEN → PENDING → CONFIRMED → EXPIRED/CANCELLED**
    
- Trigger order creation after all members pay
- Handle expiry & fallback policies
- Publish group events

### **Internal Modules**

- **Group Manager** (CRUD + validation)
- **Split Calculator**
- **State Machine Engine**
- **Member Status Tracker**
- **Expiry Scheduler**
- **Event Publisher**

### **APIs**

- `POST /group`
- `POST /group/{id}/members`
- `GET /group/{id}`
- `POST /group/{id}/force-finalize`

### **Publishes Events**

- `group.created`
- `member.added`
- `member.paid`
- `group.fully_paid`
- `group.finalized`
- `group.expired`

### **Subscribes to Events**

- `payment.succeeded`
- `payment.failed`

### **Storage**

- PostgreSQL (group_orders, group_members, status_history)
- Redis cache (group snapshot, locks, counters)

---

## **4.2 Payment Service**

### **Responsibilities**

- Generate payment intents/invoices
- Integrate with payment providers
- Provide hosted payment links
- Validate PSP webhooks
- Map internal `payment_id` ↔ provider payment id
- Handle retries and reconciliation
- Publish payment lifecycle events

### **Internal Modules**

- **Payment Intent Generator**
- **Provider Adapter Layer** (Stripe/Razorpay/PayPal)
- **Webhook Handler + Verifier**
- **Payment State Store**
- **Reconciliation Engine**

### **APIs**

- `POST /payments/intents`
- `GET /payments/{id}`
- `POST /payments/webhook`

### **Publishes Events**

- `payment.requested`
- `payment.succeeded`
- `payment.failed`
- `payment.refunded`

### **Subscribes to Events**

- `group.created`
- `group.expired`

### **Storage**

- PostgreSQL (payments, provider_map, webhook_events)
- Redis (idempotency keys, payment link cache)

---

## **4.3 Order Service**

### **Responsibilities**

- Listen to group.finalized
- Validate order items & stock
- Request final order creation
- Communicate with merchant backend
- Publish order results

### **Internal Modules**

- **Order Validator**
- **Merchant Integration Layer**
- **Order Orchestrator**
- **Order Status Manager**

### **APIs**

- `POST /order` (internal)
- `GET /order/{id}`

### **Subscribes to Events**

- `group.finalized`

### **Publishes Events**

- `order.created`
- `order.confirmed`
- `order.failed`

### **Storage**

- PostgreSQL (orders, order_items)
- Redis (order locks, status cache)

---

# 🔄 **5. Workflow (End-to-End)**

---

## **5.1 Group Creation Flow**

1. User creates cart
2. Enables “Split Payment”
3. Group Service creates `group_order`
4. Members added → invitations sent
5. Split amounts calculated
6. Group enters **OPEN** state

---

## **5.2 Payment Flow**

1. Group Service requests payment intents for each member
2. Payment Service calls Payment Gateway
3. Gateway generates hosted payment link
4. User pays → gateway triggers webhook
5. Payment Service verifies & updates status
6. Payment Service publishes `payment.succeeded`
7. Group Service updates member -> PAID

---

## **5.3 Finalization Flow**

1. All required payments become **PAID**
2. Group Service emits `group.finalized`
3. Order Service receives event
4. Order Validator checks items & stock
5. Merchant Integration places order
6. Notification sent to users

---

## **5.4 Timeout / Fallback**

If not all members pay:

- Group expires
- Either:
    - Host pays remaining
    - Or group is cancelled

---

# 🛠 **6. Databases & Tables**

---

## **6.1 Group DB**

Tables:

- `group_orders`
- `group_members`
- `group_payments_state`
- `group_status_history`

## **6.2 Payment DB**

- `payments`
- `provider_payment_map`
- `webhook_events`
- `payment_status_history`

## **6.3 Order DB**

- `orders`
- `order_items`
- `order_events`

---

# 📡 **7. Event Bus (Kafka/RabbitMQ)**

### Topics:

- `group.created`
- `member.added`
- `payment.requested`
- `payment.succeeded`
- `group.fully_paid`
- `group.finalized`
- `order.created`
- `order.failed`

This ensures full **asynchronous, decoupled** microservice communication.

---

# ⚙️ **8. Infrastructure & Scaling**

### Components:

- API Gateway (NGINX/Envoy)
- Microservices (Docker + Kubernetes)
- PostgreSQL (regional)
- Redis Cluster
- Event Bus
- Payment Gateway integrations
- Observability stack (Prometheus, Grafana, ELK)

### Scaling strategies:

- Group Service: scales by user traffic
- Payment Service: scales by webhook load
- Order Service: scales by order volume
- Worker/Scheduler: handles expiry load

---

# 🔐 **9. Security & Compliance**

- Webhook signatures (HMAC)
- Idempotency keys for payment events
- PCI compliance via hosted payment pages
- JWT-based authentication
- Encrypted PII
- Rate limiting at gateway
