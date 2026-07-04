# POS System - Point of Sale Application

A complete Point of Sale (POS) System built with JavaFX and SQLite.

## Features

- **Login System** - Secure authentication with role-based access (admin/cashier)
- **Product Management** - Add, edit, delete products with barcode support
- **Shopping Cart** - Real-time cart with quantity controls
- **Barcode Scanning** - Quick product lookup by barcode
- **Checkout** - Multiple payment methods, discounts, change calculation
- **Receipt Generation** - Auto-generated receipt numbers
- **Sales History** - View all transactions with date filtering
- **Reports & Analytics** - Bar charts with revenue tracking
- **User Management** - Admin-only user CRUD with roles

## Default Login

- **Username:** `admin`
- **Password:** `admin123`

## Requirements

- Java JDK 17 or higher
- JavaFX SDK 19 or higher
- Maven 3.8+
- SQLite (included via JDBC)

## How to Run

### Option 1: Using Maven (Recommended)

```bash
cd pos-system
mvn clean javafx:run
```

### Option 2: Using Eclipse IDE

1. Import as Maven project
2. Install e(fx)clipse plugin
3. Add JavaFX library to build path
4. Set VM arguments: `--module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics`
5. Run `POSApplication.java`

### Option 3: Build JAR

```bash
mvn clean package
java -jar target/pos-system-1.0-SNAPSHOT.jar
```

## Project Structure

```
pos-system/
├── pom.xml
├── src/
│   └── main/
│       ├── java/com/pos/
│       │   ├── POSApplication.java
│       │   ├── controller/
│       │   ├── dao/
│       │   ├── model/
│       │   ├── service/
│       │   └── util/
│       └── resources/com/pos/view/
│           ├── login.fxml
│           ├── pos.fxml
│           ├── products.fxml
│           ├── sales_history.fxml
│           ├── reports.fxml
│           └── users.fxml
```

## Database

SQLite database (`pos_database.db`) is auto-created on first run with:
- 20 sample products across 6 categories
- Default admin user
- Sales and inventory tracking

## License

MIT License
