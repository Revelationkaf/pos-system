package com.pos.controller;

import com.pos.dao.UserDAO;
import com.pos.model.User;
import com.pos.service.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label lblError;
    @FXML private VBox loginPane;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        lblError.setVisible(false);
        usernameField.setOnAction(e -> handleLogin());
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        User user = userDAO.authenticate(username, password);

        if (user != null) {
            SessionManager.setCurrentUser(user);
            lblError.setVisible(false);
            openMainWindow();
        } else {
            showError("Invalid username or password.");
            passwordField.clear();
        }
    }

    private void openMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/view/pos.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("POS System - Main");
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load main window.");
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}
