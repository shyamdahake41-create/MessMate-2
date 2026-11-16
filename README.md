# MessMate - Mess Membership Management System

MessMate is a desktop application for a college mess office. An administrator can keep member records, assign and renew monthly memberships, record payments, and save or print receipts. Data is stored in a local SQLite file and remains available after the application is closed.

## Technology

- Java 25
- Java Swing
- SQLite through JDBC
- Maven

## Default login

- Username: `admin`
- Password: `admin123`

The password is stored as a SHA-256 hash with a fixed application prefix. This is appropriate for a local college project, not for a public website.

## Prerequisites

- JDK 25, with `JAVA_HOME` pointing at the JDK
- Maven 3.9 or newer, available as `mvn` on the PATH

Check both:

```powershell
java -version
mvn -version
```

## Run on Windows

Open PowerShell in this project folder.

```powershell
cd "C:\Users\SHREYA SHYAM DAHAKE\MessMate2"
mvn test
mvn exec:java
```

To build one runnable jar and start it:

```powershell
mvn -DskipTests package
java -jar target\messmate-1.0.0.jar
```

The database file is created automatically at `data\messmate.db` in the folder from which you start the application.

## What you can demonstrate

1. Log in, then use Logout to return to the login screen.
2. Open Dashboard to see total members, active memberships, expired memberships, collected payments, and recent receipts.
3. Add, search, edit, and delete members. Deleting a member asks for confirmation and also removes that member's memberships and payments.
4. Assign a monthly membership. The expiry date is the day before the same date next month. Status is Active, Expired, or Upcoming from the dates alone.
5. Renew a member for another month. An active membership continues from the next day. An expired membership starts again today.
6. Record a payment. A second payment for the same member and month is rejected. Paid amounts are included in the dashboard total. Pending amounts are stored but not added to the collected total.
7. After a payment is saved, open the receipt and use Save PDF or Print. Receipt numbers start at `RCP-1001` and increase by one.

## Project structure

```text
src/main/java/com/messmate
  MessMateApp.java
  ui/        Swing screens
  model/     Member, membership, payment, and dashboard data
  db/        SQLite connection, schema, and SQL
  service/   Login, members, memberships, payments, and receipts
  util/      Validation, dates, money, and password hashing
src/test/java/com/messmate
  RulesTest.java
  MessMateServiceTest.java
```

## Membership dates

A membership that starts on 16 Nov 2026 expires on 15 Dec 2026. The start and expiry dates are both treated as included in the membership. Renewing that membership creates the next period, 16 Dec 2026 through 15 Jan 2027.

## Limitations

- There is one built-in administrator account and no screen for changing the password.
- The application is single-user. SQLite is stored on this computer only.
- Receipt PDFs use standard Latin fonts. Characters outside that set are replaced with spaces.
- A member can have only one payment record for a given month.
- Money is stored as decimal rupee amounts with two decimal places, shown as `Rs.`.
