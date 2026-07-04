package com.pos.util;

import java.sql.*;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:pos_database.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Products table
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "barcode TEXT UNIQUE," +
                "category TEXT," +
                "price REAL NOT NULL," +
                "stock_quantity INTEGER DEFAULT 0," +
                "description TEXT," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // Sales table
            stmt.execute("CREATE TABLE IF NOT EXISTS sales (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "subtotal REAL NOT NULL," +
                "tax REAL NOT NULL," +
                "discount REAL DEFAULT 0," +
                "total REAL NOT NULL," +
                "payment_method TEXT DEFAULT 'Cash'," +
                "cashier TEXT," +
                "receipt_number TEXT UNIQUE)");

            // Sale items table
            stmt.execute("CREATE TABLE IF NOT EXISTS sale_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "sale_id INTEGER NOT NULL," +
                "product_id INTEGER NOT NULL," +
                "quantity INTEGER NOT NULL," +
                "unit_price REAL NOT NULL," +
                "subtotal REAL NOT NULL," +
                "FOREIGN KEY (sale_id) REFERENCES sales(id)," +
                "FOREIGN KEY (product_id) REFERENCES products(id))");

            // Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "full_name TEXT," +
                "role TEXT DEFAULT 'cashier'," +
                "active INTEGER DEFAULT 1)");

            // Insert default admin user if not exists
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE username = 'admin'");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO users (username, password, full_name, role) VALUES ('admin', 'admin123', 'Administrator', 'admin')");
            }

            // Insert sample products if table is empty
            ResultSet prs = stmt.executeQuery("SELECT COUNT(*) FROM products");
            if (prs.next() && prs.getInt(1) == 0) {
                insertSampleProducts(stmt);
            }

            System.out.println("Database initialized successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertSampleProducts(Statement stmt) throws SQLException {
        String[] sampleProducts = {
            "INSERT INTO products (name, barcode, category, price, stock_quantity, description) VALUES ('Apple iPhone 15', 'IPHONE15-001', 'Electronics', 999.99, 50, 'Latest iPhone with A17 chip')",
            "INSERT INTO products (name, barcode, category, price, stock_quantity, description) VALUES ('Samsung Galaxy S24', 'SAMSUNG-S24-001', 'Electronics', 899.99, 40, 'Samsung flagship smartphone')",
            "INSERT INTO products (name, barcode, category, price, stock_quantity, description) VALUES ('MacBook Pro 16', 'MACBOOK-PRO-16', 'Electronics', 2499.99, 20, 'Professional laptop with M3 chip')",
            "INSERT INTO products (name, barcode, category, price, stock_quantity, description) VALUES ('Wireless Mouse', 'MOUSE-WL-001', 'Accessories', 29.99, 100, 'Ergonomic wireless mouse')",
            "INSERT INTO products (name, barcode, category, price, stock_quantity, description) VALUES ('USB-C Cable', 'CABLE-USBC-001', 'Accessories', 12.99, 200, 'Fast charging USB-C cable')",
            "INSERT INTO products (name, barcode, category, price, stock_quantity, description) VALUES ('Coffee Mug', 'MUG-COFFEE-001', 'Kitchen', 14.99, 80, 'Ceramic coffee mug 12oz')",
            "INSERT INTO products (name, barcode, category, price, stock_quantity, description) VALUES ('Water Bottle', 'BOTTLE-WATER-001', 'Kitchen', 24.99, 60, 'Insulated stainless steel bottle')",
            "INSERT INTO products (name, barcode, category, price, stock_quantity, description) VALUES ('Notebook', 'NOTEBOOK-A5-001', 'Stationery', 5.99, 150, 'A5 lined notebook')",
            "INSERT INTO products (name, barcode, category, price, stock_quantity, description) VALUES ('Pen Set', 'PEN-SET-001', 'Stationery', 9.99, 120, 'Ballpoint pen set of 5')",
            "INSERT INTO products (name, barcode, category, price, stock_quantity, description) VALUES ('Desk Lamp', 'LAMP-DESK-001', 'Furniture', 39.99, 30, 'LED desk lamp with adjustable brightness')",
            "INSERT INTO products (name, barcode, category, price, stock_quantity, description) VALUES ('Bluetooth Speaker', 'SPEAKER-BT-001', 'Electronics', 79.99, 45, 'Portable Bluetooth speaker')",
            "INSERT INTO products (name, barcode, category, price, stock_quantity, description) VALUES ('Headphones', 'HEADPHONE-001', 'Electronics', 149.99, 35, 'Noise cancelling headphones')",
            "INSERT INTO products (name, barcode, category, price, stock_quantity, description) VALUES ('T-Shirt', 'TSHIRT-COTTON-001', 'Clothing', 19.99, 100, 'Cotton crew neck t-shirt')",
            "INSERT INTO products (name, barcode, category, price, stock_quantity, description) VALUES ('Jeans', 'JEANS-BLUE-001', 'Clothing', 49.99, 70, 'Blue denim jeans')",
            "INSERT INTO products (name, barcode, category, price, stock_quantity, description) VALUES ('Sneakers', 'SNEAKERS-RUN-001', 'Footwear', 89.99, 50, 'Running sneakers size 10')",
            "INSERT INTO products (name, barcode, category, price, stock_quantity, description) VALUES ('Backpack', 'BACKPACK-001', 'Accessories', 59.99, 40, 'Laptop backpack with USB port')",
            "INSERT INTO products (name, barcode, category, price, stock_quantity, description) VALUES ('Phone Case', 'CASE-IPHONE-001', 'Accessories', 19.99, 90, 'Protective iPhone case')",
            "INSERT INTO products (name, barcode, category, price, stock_quantity, description) VALUES ('Screen Protector', 'SCREEN-PROT-001', 'Accessories', 9.99, 110, 'Tempered glass screen protector')",
            "INSERT INTO products (name, barcode, category, price, stock_quantity, description) VALUES ('Charger', 'CHARGER-FAST-001', 'Electronics', 34.99, 75, '65W fast charging adapter')",
            "INSERT INTO products (name, barcode, category, price, stock_quantity, description) VALUES ('Power Bank', 'POWERBANK-001', 'Electronics', 49.99, 55, '20000mAh portable power bank')"
        };

        for (String sql : sampleProducts) {
            stmt.execute(sql);
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
