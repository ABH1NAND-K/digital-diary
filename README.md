# Digital Diary

A secure and user-friendly desktop application for maintaining personal diaries, built with JavaFX and SQLite.

## Features

- Secure user authentication with BCrypt password hashing
- Create, view, edit, and delete diary entries
- Search entries by title or date
- Simple and intuitive user interface
- Local SQLite database for data storage

## Prerequisites

- Java 17 or higher
- Maven 3.8 or higher

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/digital-diary.git
   cd digital-diary
Build the project:

bash
Copy code
mvn clean package
Run the application:

bash
Copy code
java -jar target/digital-diary-1.0-SNAPSHOT.jar
Usage
Register a new account or log in if you already have one

Click the + button to create a new diary entry

Click on an entry to view or edit it

Use the search bar to find entries by title or date

Right-click on entries for additional options

Project Structure
bash
Copy code
src/
├── main/
│   ├── java/com/loginapp/
│   │   ├── controllers/     # FXML controllers
│   │   ├── models/          # Data models
│   │   └── utils/           # Utility classes
│   └── resources/           # FXML and CSS files
└── test/                    # Test files
Technologies Used
Java 17

JavaFX 21

SQLite

Maven

BCrypt

License
This project is licensed under the MIT License - see the LICENSE file for details.

yaml
Copy code
