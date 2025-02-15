# 🏗️ TeamCity Autotests 🚀  
**Automated tests for TeamCity using Java and Maven**  

## 📌 About the project  
This repository contains automated tests for **TeamCity**, designed to verify the stability and functionality of the CI/CD server.  
The tests cover API, UI, and core functional scenarios of TeamCity.  

## 🛠 Tech Stack  
- **Java 23** – main programming language  
- **Maven** – dependency and build management  
- **JUnit** – test framework  
- **RestAssured** – API testing for TeamCity  
- **Selenide** – UI testing  
- **Allure** – test reporting  
- **Docker** – isolated test environment  

## 📂 Project Structure
```plaintext
teamcity-automation-tests/
├── requests-examples/        # HTTP request examples for future automation
├── src/
│   ├── test/java/org/example/  # Automated tests
│   ├── resources/             # Configurations and test data
├── pom.xml                    # Maven configuration
├── README.md                  # Project documentation
└── .gitignore                 # Ignored files



## 🚀 Installation & Running Tests
### 1️⃣ Clone the repository
```sh
git clone https://github.com/ta-ulyanava/teamcity-automation-tests.git
cd teamcity-automation-tests
```

2️⃣ Run tests using Maven
```sh
mvn clean test
```



3️⃣ Run tests with Allure reporting
```sh
mvn clean test allure:serve
```
⚙ Configuration
Before running tests, set up environment variables or a .env file:
```sh
TEAMCITY_URL=http://localhost:8111
TEAMCITY_USER=admin
TEAMCITY_PASSWORD=admin
```

📊 Viewing Allure Reports
After test execution, generate and view the report:
```sh
allure serve target/allure-results
```

