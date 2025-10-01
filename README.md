# 🚆 Rail Ninja Test Automation Framework

This repository contains an **automated test framework** for [Rail Ninja](https://rail.ninja), covering both **UI** and **API** testing with **Java 17**.  
It automates passenger form validation, timetable search endpoints, and search history features.

---

## 📌 Project Goals
- ✅ Automate **functional UI tests** for passenger forms.  
- ✅ Automate **API tests** for timetable searches & search history retrieval.  
- ✅ Generate **human-readable test reports**.  

---

## 🛠️ Tech Stack
- [JUnit 5](https://junit.org/junit5/) – test framework  
- [Selenium](https://www.selenium.dev/) / [Selenide](https://selenide.org/) – UI automation  
- [RestAssured](https://rest-assured.io/) – API testing  
- [Allure Reports](https://allurereport.org/) – test reporting 

---

## 📂 Project Structure
```
helpers/      → Utility classes for UI & API interactions
  ├─ PassengerFormHelper.java   # UI actions for passenger forms & timetable
  ├─ ApiHelper.java             # API requests (timetable & history)

task1/        → Tests for timetable API & passenger name in header
task2/        → UI tests for passenger form validation & workflows
task3/        → API tests for search history (integrated with timetable)
```

---

## ⚙️ Prerequisites
- Java **17+**  
- [Maven](https://maven.apache.org/)  
- Chrome browser (for Selenium tests)  
- **Valid API key** for [https://back.rail.ninja](https://back.rail.ninja)
- [Allure CLI](https://allurereport.org/docs/install/) installed (for generating reports)  

---

## 🔑 API Key Configuration
1. Open **`helpers/ApiHelper.java`**.  
2. Locate the field:
   ```java
   private static final String API_KEY = "INSERT_API_KEY";
   ```
3. Replace with your valid API key:
   ```java
   private static final String API_KEY = "your_actual_api_key_here";
   ```

---

## ▶️ Running the Tests

### Build Project
```bash
mvn clean compile
```

### Run All Tests
```bash
mvn test
```

### Run Specific Test Classes
```bash
mvn -Dtest=task1.NameDisplayInHeaderTest test
mvn -Dtest=task1.TimetableApiTest test
mvn -Dtest=task2.PassengerFormTests test
mvn -Dtest=task3.SearchHistoryApiTests test
```

---

## 🧪 Test Coverage

### **Task 1 – Timetable & Name Display**
- Verify passenger name appears in header.  
- Validate `/api/v2/timetable` returns correct train data.  

### **Task 2 – Passenger Form**
- Validate field clearing.  
- Required field validations.  
- "Confirm Email" field behavior.  
- End-to-end form submission.  

### **Task 3 – Search History API**
- Verify `/api/v1/station/history` returns correct data.  
- Validate departure/arrival stations & dates.  
- Passenger counts & multi-search scenarios.  
- Behavior for empty/invalid cookies.  

---

## ⚠️ Notes & Challenges
- Dynamic UI elements → required **custom waits** & **JavaScript execution**.  
- Handled multiple timetable versions:  
  - `/trains/order/timetable`  
  - `/v9/trains/order/timetable`  
- API endpoints required **cookie handling** & **Base64-encoded history data**.  

