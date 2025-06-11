package com.example.facialrecognition.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Label totalAccessesLabel;

    @FXML
    private Label successRateLabel;

    @FXML
    private Label recentActivityLabel;

    @FXML
    private PieChart accessStatusChart;

    @FXML
    private BarChart<String, Number> dailyAccessChart;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Button applyFilterButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser les dates par défaut
        startDatePicker.setValue(LocalDate.now().minusDays(7));
        endDatePicker.setValue(LocalDate.now());

        // Charger les données initiales
        loadDashboardData();
        setupCharts();
    }

    private void loadDashboardData() {
        // TODO: Remplacer par de vraies données depuis la base de données
        totalUsersLabel.setText("Total Users: 125");
        totalAccessesLabel.setText("Total Accesses: 1,847");
        successRateLabel.setText("Success Rate: 94.2%");
        recentActivityLabel.setText("Recent Activity: 23 accesses today");
    }

    private void setupCharts() {
        setupPieChart();
        setupBarChart();
    }

    private void setupPieChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Successful Access", 87),
                new PieChart.Data("Failed Access", 8),
                new PieChart.Data("Unauthorized", 5)
        );
        accessStatusChart.setData(pieChartData);
        accessStatusChart.setLegendVisible(true);
    }

    private void setupBarChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Accesses");

        // Données d'exemple pour les 7 derniers jours
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(formatter);
            int accesses = (int) (Math.random() * 50) + 10; // Données simulées
            series.getData().add(new XYChart.Data<>(dateStr, accesses));
        }

        dailyAccessChart.getData().clear();
        dailyAccessChart.getData().add(series);
    }

    @FXML
    private void applyFilter() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                // TODO: Afficher un message d'erreur
                System.out.println("Start date cannot be after end date");
                return;
            }

            // TODO: Filtrer les données selon la période sélectionnée
            updateChartsWithDateRange(startDate, endDate);
            System.out.println("Filtering data from " + startDate + " to " + endDate);
        }
    }

    private void updateChartsWithDateRange(LocalDate startDate, LocalDate endDate) {
        // TODO: Implémenter la logique de filtrage des données
        // Mettre à jour les graphiques avec les données filtrées

        // Pour l'instant, nous rechargeons juste les données
        setupBarChart();
        setupPieChart();
    }

    @FXML
    private void switchToAuthentication() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/facialrecognition/view/MainView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) applyFilterButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("SmartFaso Access - Authentication");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToUserManagementView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/facialrecognition/view/UserManagementView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) applyFilterButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("SmartFaso Access - User Management");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToDashboard() {
        // Déjà sur le dashboard, ne rien faire
        System.out.println("Already on Dashboard");
    }

    // Méthodes utilitaires pour mettre à jour les données
    public void refreshDashboard() {
        loadDashboardData();
        setupCharts();
    }

    public void updateStatistics(int totalUsers, int totalAccesses, double successRate, int recentActivity) {
        totalUsersLabel.setText("Total Users: " + totalUsers);
        totalAccessesLabel.setText("Total Accesses: " + String.format("%,d", totalAccesses));
        successRateLabel.setText("Success Rate: " + String.format("%.1f%%", successRate));
        recentActivityLabel.setText("Recent Activity: " + recentActivity + " accesses today");
    }

    public void updatePieChart(ObservableList<PieChart.Data> data) {
        accessStatusChart.setData(data);
    }

    public void updateBarChart(XYChart.Series<String, Number> series) {
        dailyAccessChart.getData().clear();
        dailyAccessChart.getData().add(series);
    }
}