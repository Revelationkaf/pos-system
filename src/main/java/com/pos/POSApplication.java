package com.pos;

import com.pos.util.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class POSApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize database with tables and sample data
        DatabaseConnection.initializeDatabase();

        // Load login view
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/view/login.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("POS System - Login");
        primaryStage.setScene(new Scene(root, 500, 400));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() {
        DatabaseConnection.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
