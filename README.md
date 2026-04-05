# Badminton Court Booking
A web application for booking badminton courts online, built with Java Spring Boot and SQL Server.

## About
Badminton Court Booking is a web-based system that allows users to search and book badminton courts online. The system also provides court owners with tools to manage schedules and bookings efficiently.

## Features
- User registration and login
- Browse and search available badminton courts
- Book courts by date and time slot
- View and manage personal booking history
- Admin panel for managing courts and schedules
- Booking confirmation and status tracking

## Tech Stack
| Layer      | Technology            |
|------------|-----------------------|
| Backend    | Java, Spring Boot     |
| Frontend   | JavaScript, CSS, HTML |
| Database   | SQL Server (T-SQL)    |
| Build Tool | Maven                 |


## Prerequisites
Make sure you have the following installed:
- Java JDK 17+
- Maven 3.8+
- SQL Server or SQL Server Express
- IDE: IntelliJ IDEA or Eclipse (recommended)

## Getting Started
1. Clone the repository by running `git clone https://github.com/vsrs3/badminton-court-booking.git` and navigate into the project folder.
2. Open `src/main/resources/application.properties` and update the database URL, username, and password to match your local SQL Server setup.
3. Build and run the application with `mvn clean install` followed by `mvn spring-boot:run`.
4. Open your browser and go to `http://localhost:8080`.

## Database Setup
Run the SQL script located in the `database/` folder using SQL Server Management Studio (SSMS) or the `sqlcmd` command line tool to initialize the schema and seed data.

## Project Structure
- database/ — SQL scripts for schema and seed data
- src/main/java/ — Java source code including controllers, services, and models
- src/main/resources/ — Configuration files, templates, and static assets
- pom.xml — Maven dependencies

## Contributing
Contributions are welcome. Feel free to open an issue or submit a pull request.
1. Fork the project
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## License
This project is open source and available under the MIT License.
