# Plant Simulator

Simulates the behaviour and control dynamics of continuous processes.

## Project Setup

### Installing Java 

Java 21 must be installed and configured on your machine to build this project:

1. Install [Oracle JDK 21](https://www.oracle.com/ca-en/java/technologies/downloads/#java21) on your machine
2. Add a new system environment variable called `JAVA_HOME` and set it to the installation directory of the JDK
    - Eg. On Windows: `C:\Program Files\Java\jdk-21`
3. Add the binary folder of the JDK to your `PATH` system environment variable
    - Eg. On Windows: `C:\Program Files\Java\jdk-21\bin`

### Building the Project

This project uses Gradle as its build automation tool; the Gradle executable is checked into source control so Gradle installation is not required.
Simply run the following command from the project's root directory to build it and execute all tests:

```angular2html
gradlew build
```