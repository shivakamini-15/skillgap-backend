# SkillGap Backend

Spring Boot 3 REST API for the SkillGap application. Uses Java 17+, Maven,
Spring Security with JWT, and MySQL.

## Configure and run

Ensure MySQL is running, then set the database connection and JWT signing
secret in PowerShell. Use your own private JWT secret with at least 32
characters:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/skillgap_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your-mysql-password"
$env:JWT_SECRET = "replace-with-a-private-random-secret-of-at-least-32-characters"
mvn spring-boot:run
```

Leave `DB_PASSWORD` empty if your local MySQL account has no password. Run
these commands from the backend directory. The API listens on port 8081, and
the database and tables are created or updated on startup.

The first application startup seeds the catalog and demo accounts. The demo
student login is `demo.student@skillgap.com` / `Student@123`. New registrations
are student accounts.

## Build

```powershell
mvn clean package
```
