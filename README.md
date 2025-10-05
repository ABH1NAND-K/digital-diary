#Digital Diary
A secure and user-friendly desktop application for maintaining personal diaries, built with JavaFX and SQLite.

✨ Features
🔒 Secure user authentication with BCrypt password hashing
📝 Create, view, edit, and delete diary entries
🔍 Search entries by title or date
📅 Simple and intuitive user interface
💾 Local SQLite database for data storage
🚀 Getting Started
Prerequisites
Java 17 or higher
Maven 3.8 or higher
Installation
Clone the repository:
bash
git clone https://github.com/yourusername/digital-diary.git
cd digital-diary
Build the project:
bash
mvn clean package
Run the application:
bash
java -jar target/digital-diary-1.0-SNAPSHOT.jar
🖥️ Usage
Register a new account or Login if you already have one
Use the + button to create a new diary entry
Click on an entry to view or edit it
Use the search bar to find entries by title or date
Right-click on entries for additional options
🛠️ Technologies Used
Java 17: Core programming language
JavaFX 21: For the desktop GUI
SQLite: For local database storage
Maven: For dependency management
BCrypt: For password hashing
📂 Project Structure
src/
├── main/
│   ├── java/com/loginapp/
│   │   ├── controllers/     # FXML controllers
│   │   ├── models/          # Data models
│   │   └── utils/           # Utility classes
│   └── resources/           # FXML and CSS files
└── test/                    # Test files
📝 License
This project is licensed under the MIT License - see the LICENSE file for details.
