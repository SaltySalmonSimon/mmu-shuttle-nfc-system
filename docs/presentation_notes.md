# MMU FCI CPT6216 Industrial Training — Official Guideline Presentation Script

* **Project**: MMU Shuttle Bus (Version 2) — NFC Passenger Verification & Telemetry Subsystem
* **Integration**: Merging **Ka Sheng's Bus Shuttle Fleet** & **Teoh Xuan Xuan's NFC/RFID System**
* **Course Code**: CPT6216 Industrial Training
* **Faculty**: Faculty of Computing and Informatics (FCI), Multimedia University
* **Trainee / Presenter**: Simon
* **Evaluation Alignment**: 100% matched to the official MMU FCI Presentation Guidelines
* **Interactive Slide Deck**: [presentation.html](file:///C:/Users/samie/.gemini/antigravity/brain/d8351b3e-674c-49dd-b4ca-0352a07d6895/presentation.html)

---

## ⏱️ Precise Timing Breakdown (Target: 10 – 15 Minutes)

| Slide # | Exact Guideline Section Title | Pacing | Cumulative |
| :---: | :--- | :---: | :---: |
| **Slide 1** | Cover Slide & Metadata | 0:45 min | 0:45 |
| **Slide 2** | **1. Company Background** | 1:00 min | 1:45 |
| **Slide 3** | **2. Objectives of the Internship** | 1:15 min | 3:00 |
| **Slide 4** | **3. Training Projects / Tasks / Assignments** | 1:15 min | 4:15 |
| **Slide 5** | **4. Description / Methods** (The Adapter Strategy) | 1:30 min | 5:45 |
| **Slide 6** | **5. Achievement of the Tasks / Implementation** | 1:30 min | 7:15 |
| **Slide 7** | **6. Problems Encountered and Ways to Improve** | 1:30 min | 8:45 |
| **Slide 8** | **7. Training Analysis** (Skills, Strengths, Weaknesses) | 1:15 min | 10:00 |
| **Slide 9** | **8. Application of Subject Knowledge to Industrial Environment** | 1:00 min | 11:00 |
| **Slide 10** | **9. Recommendation to Others** | 1:00 min | 12:00 |
| **Slide 11** | **10. Conclusion** & Immediate Next Step | 1:00 min | 13:00 |
| **Demo** | **Live Simulator Demonstration & Examiner Q&A** | 2:00 min | 15:00 max |

---

## 🎙️ Spoken Script (Follow Along Slide by Slide)

### Slide 1: Title Slide (0:00 – 0:45)
> *"Good morning to my faculty supervisor, company supervisor, and respected examiners. My name is Simon, and today I am delivering my industrial training presentation for CPT6216. My project focuses on the **MMU Shuttle Bus NFC Passenger Integration**, which merges two foundational MMU engineering systems: **Ka Sheng's campus shuttle bus fleet** and **Teoh Xuan Xuan's smart card verification system**."*

---

### Slide 2: 1. Company Background (0:45 – 1:45)
> *"Section 1: Company Background. Multimedia University (MMU), established under Universiti Telekom Sdn. Bhd. (UTSB), is a leading digital technology university in Malaysia. I was attached to the Faculty of Computing and Informatics (FCI) software engineering team at the Cyberjaya Campus. Our department actively incubates smart campus transit solutions to modernize campus mobility and automate manual logistics."*

---

### Slide 3: 2. Objectives of the Internship (1:45 – 3:00)
> *"Section 2: Objectives of the Internship. Our primary mandate was bridging Ka Sheng's bus tracking fleet with Teoh Xuan Xuan's NFC card reading logic. Our core objectives were:  
> 1. Bridge both standalone projects into a unified transit platform.  
> 2. Automate passenger boarding counts to eliminate manual headcounts.  
> 3. Synchronize real-time bus crowding metrics to student web users and drivers.  
> 4. Apply enterprise software engineering practices in Spring Boot, PostgreSQL, Docker, and Flutter."*

---

### Slide 4: 3. Training Projects / Tasks / Assignments (3:00 – 4:15)
> *"Section 3 outlines the five core tasks executed during my training:  
> - **Phase 1: Card Logic & Specs**: Studied Teoh Xuan Xuan's card reading logic and drafted the project requirements.  
> - **Phase 2: Database Setup**: Created tables in PostgreSQL to store student cards, student profiles, and boarding history.  
> - **Phase 3: Backend Adapter**: Built the Spring Boot API with a 5-second cooldown to block accidental duplicate taps.  
> - **Phase 4: Hardware Simulator**: Created a software tool to simulate on-bus card taps so testing was not blocked by hardware delays.  
> - **Phase 5: Client Apps**: Added crowd level indicators to the Student Web app and a passenger counter HUD to the Driver app."*

---

### Slide 5: 4. Description / Methods (4:15 – 5:45)
> *"Section 4 describes our technical methodology. We adopted the **Adapter Design Pattern**, driven by four key reasons:  
> 1. **No Hardware Lock-In**: The backend accepts standard JSON (`cardUid` + `GPS`). Whether the bus runs a USB reader, a Raspberry Pi, or phone NFC, we can swap hardware anytime without touching backend code.  
> 2. **5-Second Sliding Debounce Filter**: An in-memory concurrent cooldown window eliminates accidental duplicate counts when students linger at the scanner.  
> 3. **Zero Driver Overhead**: Drivers must focus on the road. The adapter automatically calculates the Haversine distance from the bus GPS to the nearest station to log stops automatically.  
> 4. **Preserving Fleet Stability**: It integrates smart card reading seamlessly without altering Ka Sheng's existing GPS routes or tracking."*

---

### Slide 6: 5. Achievement of the Tasks / Implementation (5:45 – 7:15)
> *"Section 5 covers our implementation achievements:  
> - In **PostgreSQL**, we created `student_cards` and `shuttle_boarding_logs` with B-Tree indexes for fast queries.  
> - In **Spring Boot**, REST endpoints and STOMP WebSockets broadcast live occupancy in under 50 milliseconds.  
> - In **Testing**, our Node.js edge simulator verified 5 operational test cases (valid cards, double taps, second students, unregistered cards, and bad inputs), achieving a **100% pass rate** at 32.4ms latency.  
> - On client apps, the **Student Web App** shows dynamic crowding badges (🟢 Available, 🟡 Moderate, 🔴 Crowded), and the **Driver App** features a sticky HUD counter with a trip reset button."*

---

### Slide 7: 6. Problems Encountered and Ways to Improve (7:15 – 8:45)
> *"Section 6 details the problems encountered and how they were solved:  
> 1. **Absence of Physical Machine (Primary Constraint)**: Physical ACR122U USB readers from Teoh Xuan Xuan's setup were not yet installed on buses due to university procurement. We solved this by building the automated Node.js edge simulator and preparing a mobile NFC fallback, ensuring the software pipeline was 100% tested.  
> 2. **Duplicate Scans**: Solved by implementing an in-memory concurrent sliding-window debounce cache.  
> 3. **One-Way Tap In**: Currently, students tap when entering, but there is no exit reader. We added a one-touch HUD trip reset button for drivers, and recommended dual-beam infrared sensors for future upgrades."*

---

### Slide 8: 7. Training Analysis (8:45 – 10:00)
> *"Section 7 provides the Training Analysis:  
> - **Skills Developed**: Spring Boot 3 enterprise microservices, Hibernate JPA relational modeling, STOMP WebSockets, Docker multi-container networking, and Flutter state management.  
> - **Core Strengths**: Proactive problem-solving—engineering the mock simulator when hardware was delayed; and decoupled architecture avoiding vendor lock-in.  
> - **Area for Improvement**: Deepening familiarity with embedded Linux IoT operating systems (systemd daemons on Raspberry Pi OS) for long-term on-bus maintenance."*

---

### Slide 9: 8. Application of Subject Knowledge to Industrial Environment (10:00 – 11:00)
> *"Section 8 illustrates how MMU academic coursework directly empowered this project:  
> - **Object-Oriented Programming & Design Patterns**: Applied the Adapter and Singleton patterns in Spring Boot to isolate external reader inputs from core domain logic.  
> - **Database Systems**: Applied Third Normal Form (3NF) relational normalization, foreign key cascading, and B-Tree indexing in PostgreSQL for sub-10ms queries.  
> - **Computer Networks & Distributed Systems**: Applied REST principles, HTTP status codes (200, 400, 404, 429), and full-duplex STOMP WebSockets for push notifications."*

---

### Slide 10: 9. Recommendation to Others (11:00 – 12:00)
> *"Section 9 covers Recommendations to Others:  
> - **For Future Interns**:  
>   1. Build a software simulator first: Hardware takes weeks to arrive. A simulator lets you code and test your backend immediately without waiting.  
>   2. Always protect against double-taps: In real life, students tap twice or hold cards too long. Always put a simple cooldown timer so nobody gets counted twice.  
> - **For MMU Management**:  
>   1. Install fixed 4G card scanners: Mount permanent Raspberry Pi readers with 4G on the buses so drivers don't have to configure anything.  
>   2. Add exit door sensors: Simple sensors at the back door can automatically count students getting off, keeping the seat count 100% accurate."*

---

### Slide 11: 10. Conclusion (12:00 – 13:00)
> *"In conclusion, Section 10: We have successfully established a robust software foundation bridging Ka Sheng's shuttle bus fleet with Teoh Xuan Xuan's card reading logic. All database schemas, debounce filters, REST endpoints, and client UIs are functioning smoothly and validated via automated simulation.  
> **Our Immediate Next Step:** I am now going to acquire the physical NFC reader device and continue working on connecting the physical hardware with the campus shuttle bus fleet.  
> Thank you very much, and I would now be delighted to answer any questions or demonstrate the live simulator."*

---

## ⚡ Live Demo (Switch to Terminal & Browser)
1. In PowerShell: run `node onbus-edge-bridge/mock-tap-simulator.js` (shows 5 test cases passing).
2. In Browser: open `http://localhost` (shows announcement with Oct 5 launch date).
3. In PostgreSQL: run `docker exec -i postgres-db psql -U admin -d shuttle_bus -c "SELECT id, card_uid, matrix_id, boarding_time, status FROM shuttle_boarding_logs ORDER BY boarding_time DESC LIMIT 3;"`.
