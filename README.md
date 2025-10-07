# Digital Diary

A secure and user-friendly desktop application for maintaining personal diaries, built with JavaFX and SQLite.

## Features

- 🔒 **Secure Authentication**
  - User registration and login with BCrypt password hashing
  - Secure session management

- 📝 **Diary Management**
  - Create, view, edit, and delete diary entries
  - Rich text formatting support
  - Entry organization by date and title
  - Search functionality by content, title, or date

- 🎨 **User Interface**
  - Clean and intuitive design
  - Dark/Light theme support
  - Responsive layout

- 🔍 **Advanced Features**
  - Entry categorization with tags
  - Export entries to PDF/Text
  - Data backup and restore

## Prerequisites

- Java 17 or higher
- Maven 3.8 or higher
- Git (for cloning the repository)

## Getting Started

### Running from IDE (IntelliJ IDEA/Eclipse/VS Code)

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/digital-diary.git
   cd digital-diary
   ```

2. **Import as Maven project** in your preferred IDE

3. **Set up JavaFX** (if not automatically configured):
   - Download JavaFX SDK from [openjfx.io](https://openjfx.io/)
   - Add VM options in your IDE:
     ```
     --module-path "path-to-javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml
     ```

4. **Run the application**:
   - Locate `Main.java` in `src/main/java/com/loginapp/`
   - Right-click and select 'Run Main.main()'

### Running from Command Line

The easiest way to run the application is using the Maven JavaFX plugin:

```bash
# Clone the repository (if not already done)
git clone https://github.com/yourusername/digital-diary.git
cd digital-diary

# Run the application using Maven
mvn clean javafx:run
```

### Building from Source

Alternatively, you can build and run the application in two steps:

```bash
# Build the project
mvn clean package

# Run the application
java -jar target/digital-diary-1.0-SNAPSHOT.jar
```

## User Manual

### 1. Authentication
- **Sign Up**: New users can create an account with a unique username and password
- **Login**: Existing users can log in with their credentials
- **Remember Me**: Option to stay logged in (uses secure token storage)

### 2. Dashboard
- **Navigation**: Sidebar for easy access to different sections
- **Search**: Find entries by title, content, or date range
- **New Entry**: Create a new diary entry with the '+' button

### 3. Managing Entries
- **Create New**: Click '+' and enter your diary content
- **Edit**: Click on any entry to modify its content
- **Delete**: Right-click an entry and select 'Delete'
- **View**: Click on an entry to view in full screen

### 4. Additional Features
- **Categories**: Tag and filter entries by categories
- **Export**: Save entries as PDF or text files
- **Settings**: Customize application appearance and behavior

## Creating an Executable

### Using Maven with jpackage

1. Ensure you have JDK 16+ with jpackage
2. Run the following command:
   ```bash
   mvn clean package
   jpackage --name "DigitalDiary" \
     --module-path "path-to-javafx-sdk" \
     --add-modules javafx.controls,javafx.fxml \
     --input target/ \
     --main-jar digital-diary-1.0-SNAPSHOT.jar \
     --main-class com.loginapp.Main \
     --type app-image \
     --dest release
   ```

### Using Launch4j (Windows)
1. Download Launch4j from [sourceforge](http://launch4j.sourceforge.net/)
2. Configure the executable:
   - Output file: `DigitalDiary.exe`
   - Jar: `target/digital-diary-1.0-SNAPSHOT.jar`
   - Icon: (optional) Add a `.ico` file
   - JRE: Set min/max Java version

## Project Structure

```
src/
├── main/
│   ├── java/com/loginapp/
│   │   ├── controllers/     # FXML controllers
│   │   ├── models/          # Data models
│   │   ├── utils/           # Utility classes
│   │   └── Main.java        # Application entry point
│   └── resources/           # FXML, CSS, and assets
└── test/                    # Unit and integration tests
```

## Technologies Used

- Java 17
- JavaFX 21
- SQLite
- Maven
- BCrypt
- JUnit 5 (Testing)
- Log4j (Logging)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For issues and feature requests, please [open an issue](https://github.com/yourusername/digital-diary/issues).
