# Coffee Shop Simulation & Ordering System ☕

A full-stack coffee shop application featuring a realistic order simulation engine, adaptive scheduling algorithm, and real-time dashboard. Built to demonstrate high-throughput handling with limited resources (3 baristas vs 100+ orders).

![Login Screen](frontend/src/assets/login-bg-modern.png)

## 🚀 Key Features

### 1. Adaptive Scheduling Engine
- **Throughput Protection**: Automatically prioritizes short jobs (Espresso/Cold Brew) when congestion creates high wait times (> 7.5 mins).
- **Critical Mode**: Switches to Shortest-Job-First (SJF) logic if wait times exceed 9.0 mins to flush the queue.
- **Fairness Guarantees**: Prevents starvation by capping the number of times an order can be skipped.

### 2. Simulation & Analytics
- **Realistic Simulation**: Poisson arrival generation to mimic real-world rush hours (7:00 AM - 10:00 AM).
- **Capacity Planning**: Validated to handle **100 orders** with **3 baristas** while keeping average wait times **under 2.5 minutes**.
- **Real-time Stats**: Live dashboard showing Avg Wait Time, Barista Utilization, and Order Breakdown.

### 3. Modern UI/UX
- **Customer App**: Clean ordering interface with menu, cart, and order status tracking.
- **Barista/Admin View**: Queue board with color-coded wait times (Green/Yellow/Red).
- **Tech Stack**: React + Vite + Tailwind CSS (Frontend), Spring Boot 3 + H2 Database (Backend).

---

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.2, Spring Data JPA, H2 Database
- **Frontend**: React 18, Vite, Tailwind CSS, Axios
- **Tools**: Maven, Git

---

## 📦 Installation & Setup

### Prerequisites
- Java 17+
- Node.js 18+

### 1. Backend Setup
```bash
# Clone the repository
git clone https://github.com/KaushikShinde08/coffee_shop.git
cd coffee_shop

# Run the Spring Boot application
./mvnw.cmd spring-boot:run
```
*The backend will start on `http://localhost:8081`*

### 2. Frontend Setup
```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start the development server
npm run dev
```
*The frontend will start on `http://localhost:5177`*

---

## 🎮 Running Simulations

You can run load tests directly from the dashboard or via API.

### Via API (Curl)
```bash
# 1. Run Simulation (100 Orders, Lambda=0.60)
curl -X POST http://localhost:8081/api/simulation/run

# 2. Process Orders
curl -X POST http://localhost:8081/api/simulation/process

# 3. View Stats
curl http://localhost:8081/api/stats
```

### Via Dashboard
1. Log in as Admin (`admin` / `password`).
2. Navigate to **Stats & Simulation**.
3. Click **Run Simulation**.

---

## 📊 Performance Metrics

| Metric | Target | Actual (100 Orders) |
|--------|--------|----------------------|
| **Avg Wait Time** | < 10 min | **2.45 min** ✅ |
| **Utilization** | < 80% | **50.8%** ✅ |
| **Timeouts** | 0% | **0.0%** ✅ |

---

## 📂 Project Structure

```
coffee_shop/
├── src/main/java/com/example/coffee_shop/  # Backend Source
│   ├── controller/                         # REST Controllers
│   ├── service/                            # Business Logic & Simulation
│   ├── model/                              # JPA Entities
│   └── repository/                         # Data Access
├── frontend/                               # React Frontend
│   ├── src/
│   │   ├── components/                     # Reusable UI Components
│   │   ├── pages/                          # App Pages
│   │   └── context/                        # Auth & State Management
└── pom.xml                                 # Maven Dependencies
```

## 📝 License
This project is open-source and available under the MIT License.
