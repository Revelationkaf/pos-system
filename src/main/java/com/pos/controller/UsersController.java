package com.pos.controller;

import com.pos.dao.UserDAO;
import com.pos.model.User;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.Optional;

public class UsersController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colFullName;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, Boolean> colActive;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtFullName;
    @FXML private ComboBox<String> cmbRole;
    @FXML private CheckBox chkActive;
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    private final UserDAO userDAO = new UserDAO();
    private User selectedUser;

    @FXML
    public void initialize() {
        setupTable();
        loadUsers();
        setupRoles();
        setupTableSelection();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        colActive.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                setText(empty || active == null ? null : (active ? "Yes" : "No"));
                if (!empty && active != null) {
                    setTextFill(active ? javafx.scene.paint.Color.GREEN : javafx.scene.paint.Color.RED);
                    setStyle("-fx-font-weight: bold; -fx-alignment: CENTER;");
                }
            }
        });
    }

    private void loadUsers() {
        usersTable.setItems(userDAO.getAllUsers());
    }

    private void setupRoles() {
        cmbRole.getItems().addAll("cashier", "admin");
        cmbRole.setValue("cashier");
    }

    private void setupTableSelection() {
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedUser = newVal;
                populateFields(newVal);
                btnAdd.setText("New");
                btnUpdate.setDisable(false);
                btnDelete.setDisable(false);
                txtPassword.setPromptText("Leave empty to keep current");
            }
        });
    }

    private void populateFields(User user) {
        txtUsername.setText(user.getUsername());
        txtFullName.setText(user.getFullName());
        cmbRole.setValue(user.getRole());
        chkActive.setSelected(user.isActive());
        txtPassword.clear();
    }

    @FXML
    private void handleAdd() {
        if (btnAdd.getText().equals("New")) {
            clearFields();
            btnAdd.setText("Add User");
            btnUpdate.setDisable(true);
            btnDelete.setDisable(true);
            selectedUser = null;
            return;
        }

        if (!validateFields()) return;

        User user = new User();
        user.setUsername(txtUsername.getText().trim());
        user.setPassword(txtPassword.getText());
        user.setFullName(txtFullName.getText().trim());
        user.setRole(cmbRole.getValue());
        user.setActive(chkActive.isSelected());

        userDAO.addUser(user);
        loadUsers();
        clearFields();
        showAlert(Alert.AlertType.INFORMATION, "Success", "User added successfully!");
    }

    @FXML
    private void handleUpdate() {
        if (selectedUser == null) return;
        if (txtUsername.getText().trim().isEmpty() || txtFullName.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Username and full name are required.");
            return;
        }

        selectedUser.setUsername(txtUsername.getText().trim());
        if (!txtPassword.getText().isEmpty()) {
            selectedUser.setPassword(txtPassword.getText());
        }
        selectedUser.setFullName(txtFullName.getText().trim());
        selectedUser.setRole(cmbRole.getValue());
        selectedUser.setActive(chkActive.isSelected());

        userDAO.updateUser(selectedUser);
        loadUsers();
        showAlert(Alert.AlertType.INFORMATION, "Success", "User updated successfully!");
    }

    @FXML
    private void handleDelete() {
        if (selectedUser == null) return;

        if (selectedUser.getUsername().equals("admin")) {
            showAlert(Alert.AlertType.ERROR, "Cannot Delete", "The admin account cannot be deleted.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText("Delete user "" + selectedUser.getUsername() + ""?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            userDAO.deleteUser(selectedUser.getId());
            loadUsers();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully!");
        }
    }

    @FXML
    private void handleClear() {
        clearFields();
        usersTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleClose() {
        ((Stage) usersTable.getScene().getWindow()).close();
    }

    private void clearFields() {
        txtUsername.clear();
        txtPassword.clear();
        txtFullName.clear();
        cmbRole.setValue("cashier");
        chkActive.setSelected(true);
        btnAdd.setText("Add User");
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        selectedUser = null;
        txtPassword.setPromptText("Password");
    }

    private boolean validateFields() {
        if (txtUsername.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Username is required.");
            return false;
        }
        if (txtPassword.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Password is required.");
            return false;
        }
        if (txtFullName.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Full name is required.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
