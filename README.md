# PVault

PVault is a secure and efficient password management application designed to help users store and manage their passwords safely. Built with Spring Boot, Hibernate, MySQL, HTML, and a little JavaScript, PVault ensures that your credentials are protected and easily accessible when needed.

## Features

- **Secure Storage**: Passwords are encrypted and stored securely to prevent unauthorized access.
- **User-Friendly Interface**: Intuitive design for easy navigation and management of your passwords.
- **Two-Factor Authentication using OTP**: Enhances security by requiring an additional verification step.

## Installation

To set up PVault on your local machine:

1. **Clone the Repository**:

   ```bash
   git clone https://github.com/JeetNarayanChakraborty/PVault.git
   ```

2. **Navigate to the Project Directory**:

   ```bash
   cd PVault
   ```

3. **Build the Project**:

   Use Maven to build the project. If Maven is not installed, download it from the [official website](https://maven.apache.org/).

   ```bash
   mvn clean install
   ```

4. **Run the Application**:

   ```bash
   java -jar target/pvault-1.0.0.jar
   ```

## Usage

Once the application is running:

- **Add a New Password**: Navigate to the 'Add Password' section and enter the required details.
- **View Stored Passwords**: Access the 'View Passwords' section to see all your stored credentials.
- **Delete a Password**: In the 'View Passwords' section, select the password you wish to delete and confirm your action.
- **Restore Deleted Password**: Recover accidentally deleted passwords from the restore section.

## Scalability & Resiliency Enhancements

### 1. Circuit Breaker Implementation (Resilience4J)
To prevent cascading failures, PVault uses Resilience4J's Circuit Breaker to handle failures gracefully. If a service fails, the circuit breaker prevents additional calls, allowing time for recovery.

**Implementation:**
```java
@CircuitBreaker(name = "passwordService", fallbackMethod = "fallbackRetrievePassword")
public String retrievePassword(String userId) {
    return passwordRepository.findByUserId(userId);
}

public String fallbackRetrievePassword(String userId, Throwable t) {
    return "Service temporarily unavailable. Please try again later.";
}
```

### 2. Asynchronous Processing with Message Queues
To improve scalability, operations such as password encryption and database writes are handled asynchronously using Apache Kafka.

**Implementation:**
```java
@Autowired
private KafkaTemplate<String, Password> kafkaTemplate;

public void savePasswordAsync(Password password) {
    kafkaTemplate.send("passwordTopic", password);
}

@KafkaListener(topics = "passwordTopic", groupId = "passwordGroup")
public void processPassword(Password password) {
    passwordRepository.save(password);
}
```

### 3. Load Balancer using NGINX
To distribute traffic efficiently across multiple instances of PVault, NGINX is used as a load balancer.

**Implementation (nginx.conf):**
```nginx
http {
    upstream pvault_backend {
        server backend1.example.com;
        server backend2.example.com;
    }
    
    server {
        listen 80;
        location / {
            proxy_pass http://pvault_backend;
        }
    }
}
```

For any questions or support, please open an issue in this repository.

