package com.example.facialrecognition.view;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;

import com.example.facialrecognition.database.AccessLogRepository;
import com.example.facialrecognition.database.UserRepository;
import com.example.facialrecognition.model.AccessLog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class DashboardView extends BorderPane {

    private TableView<AccessLog> accessLogsTable;
    private PieChart accessStatusChart;
    private BarChart<String, Number> dailyAccessChart;
    private LineChart<String, Number> trendChart;

    private Label totalUsersLabel;
    private Label totalAccessesLabel;
    private Label successRateLabel;
    private Label recentActivityLabel;

    private DatePicker startDatePicker;
    private DatePicker endDatePicker;

    public DashboardView() {
        this.setPadding(new Insets(15));

        // En-tête du tableau de bord
        Label headerLabel = new Label("Tableau de Bord - Statistiques d'Accès");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 20));

        // Paramètres de filtre par date
        HBox filterBox = createDateFilterControls();

        VBox topSection = new VBox(10, headerLabel, filterBox);
        this.setTop(topSection);

        // Section principale avec widgets de statistiques
        GridPane statsGrid = createStatsGrid();
        this.setCenter(statsGrid);

        // Tableau des accès récents
        VBox recentAccessSection = createRecentAccessSection();
        this.setBottom(recentAccessSection);

        // Chargement initial des données
        try {
            loadDashboardData();
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des données du tableau de bord: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private HBox createDateFilterControls() {
        HBox filterBox = new HBox(10);
        filterBox.setPadding(new Insets(10, 0, 10, 0));

        Label filterLabel = new Label("Filtrer par période:");

        // Date de début
        Label startDateLabel = new Label("Du:");
        startDatePicker = new DatePicker(LocalDate.now().minusMonths(1));

        // Date de fin
        Label endDateLabel = new Label("Au:");
        endDatePicker = new DatePicker(LocalDate.now());

        // Bouton pour appliquer le filtre
        javafx.scene.control.Button applyFilterButton = new javafx.scene.control.Button("Appliquer");
        applyFilterButton.setOnAction(e -> {
            try {
                loadDashboardData();
            } catch (SQLException ex) {
                System.err.println("Erreur lors de l'application du filtre: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        filterBox.getChildren().addAll(filterLabel, startDateLabel, startDatePicker,
                endDateLabel, endDatePicker, applyFilterButton);

        return filterBox;
    }

    private GridPane createStatsGrid() {
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(15);
        statsGrid.setVgap(15);
        statsGrid.setPadding(new Insets(10));

        // Création des widgets de statistiques

        // 1. Statistiques générales
        VBox generalStatsBox = createGeneralStatsBox();
        statsGrid.add(generalStatsBox, 0, 0);

        // 2. Graphique en secteurs pour le statut des accès
        accessStatusChart = new PieChart();
        accessStatusChart.setTitle("Répartition des Accès");
        accessStatusChart.setLabelsVisible(true);
        VBox pieChartBox = new VBox(accessStatusChart);
        pieChartBox.setMinSize(300, 300);
        pieChartBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        statsGrid.add(pieChartBox, 1, 0);

        // 3. Graphique à barres pour les accès quotidiens
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        dailyAccessChart = new BarChart<>(xAxis, yAxis);
        dailyAccessChart.setTitle("Accès Quotidiens");
        xAxis.setLabel("Date");
        yAxis.setLabel("Nombre d'accès");
        dailyAccessChart.setLegendVisible(false);
        VBox barChartBox = new VBox(dailyAccessChart);
        barChartBox.setMinSize(400, 300);
        barChartBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        statsGrid.add(barChartBox, 0, 1, 2, 1);

        // 4. Graphique de tendance
        CategoryAxis trendXAxis = new CategoryAxis();
        NumberAxis trendYAxis = new NumberAxis();
        trendChart = new LineChart<>(trendXAxis, trendYAxis);
        trendChart.setTitle("Tendance des Accès");
        trendXAxis.setLabel("Période");
        trendYAxis.setLabel("Nombre d'accès");
        VBox trendChartBox = new VBox(trendChart);
        trendChartBox.setMinSize(400, 300);
        trendChartBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        statsGrid.add(trendChartBox, 0, 2, 2, 1);

        return statsGrid;
    }

    private VBox createGeneralStatsBox() {
        VBox statsBox = new VBox(15);
        statsBox.setPadding(new Insets(15));
        statsBox.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 5;");

        Label statsHeaderLabel = new Label("Statistiques Générales");
        statsHeaderLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        totalUsersLabel = new Label("Utilisateurs Totaux: ...");
        totalAccessesLabel = new Label("Accès Totaux: ...");
        successRateLabel = new Label("Taux de Réussite: ...");
        recentActivityLabel = new Label("Activité Récente: ...");

        statsBox.getChildren().addAll(statsHeaderLabel, totalUsersLabel,
                totalAccessesLabel, successRateLabel, recentActivityLabel);

        return statsBox;
    }

    private VBox createRecentAccessSection() {
        VBox recentAccessBox = new VBox(10);
        recentAccessBox.setPadding(new Insets(15, 0, 0, 0));

        Label recentAccessLabel = new Label("Journaux d'Accès Récents");
        recentAccessLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        // Tableau des accès récents
        accessLogsTable = new TableView<>();

        TableColumn<AccessLog, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<AccessLog, String> userNameColumn = new TableColumn<>("Utilisateur");
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));

        TableColumn<AccessLog, String> timestampColumn = new TableColumn<>("Date/Heure");
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        TableColumn<AccessLog, String> statusColumn = new TableColumn<>("Statut");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<AccessLog, String> detailsColumn = new TableColumn<>("Détails");
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));

        accessLogsTable.getColumns().addAll(idColumn, userNameColumn, timestampColumn, statusColumn, detailsColumn);

        idColumn.setPrefWidth(50);
        userNameColumn.setPrefWidth(150);
        timestampColumn.setPrefWidth(150);
        statusColumn.setPrefWidth(100);
        detailsColumn.setPrefWidth(300);

        VBox.setVgrow(accessLogsTable, Priority.ALWAYS);

        recentAccessBox.getChildren().addAll(recentAccessLabel, accessLogsTable);

        return recentAccessBox;
    }

    /**
     * Charge les données du tableau de bord à partir de la période spécifiée
     */
    public void loadDashboardData() throws SQLException {
        // Récupération des dates pour le filtre
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        // 1. Statistiques générales
        updateGeneralStats(startDate, endDate);

        // 2. Graphique de distribution des accès
        updateAccessStatusChart(startDate, endDate);

        // 3. Graphique des accès quotidiens
        updateDailyAccessChart(startDate, endDate);

        // 4. Graphique de tendance
        updateTrendChart(startDate, endDate);

        // 5. Tableau des accès récents
        updateRecentAccessTable(startDate, endDate);
    }

    private void updateGeneralStats(LocalDate startDate, LocalDate endDate) throws SQLException {
        // Nombre total d'utilisateurs
        int totalUsers = UserRepository.countUsers();
        totalUsersLabel.setText("Utilisateurs Totaux: " + totalUsers);

        // Statistiques d'accès
        int totalAccesses = AccessLogRepository.countAccessLogs(startDate, endDate);
        int successfulAccesses = AccessLogRepository.countAccessLogsByStatus(startDate, endDate, "success");

        totalAccessesLabel.setText("Accès Totaux: " + totalAccesses);

        // Calcul du taux de réussite
        double successRate = (totalAccesses > 0) ? ((double) successfulAccesses / totalAccesses) * 100 : 0;
        successRateLabel.setText(String.format("Taux de Réussite: %.1f%%", successRate));

        // Activité récente (dernière tentative d'accès)
        AccessLog recentLog = AccessLogRepository.getMostRecentAccessLog();
        if (recentLog != null) {
            recentActivityLabel.setText("Activité Récente: " + recentLog.getTimestamp() +
                    " - " + recentLog.getUserName() + " (" + recentLog.getStatus() + ")");
        } else {
            recentActivityLabel.setText("Activité Récente: Aucune activité enregistrée");
        }
    }

    private void updateAccessStatusChart(LocalDate startDate, LocalDate endDate) throws SQLException {
        // Récupération des données pour le graphique en secteurs
        Map<String, Integer> statusCounts = AccessLogRepository.getAccessStatusCounts(startDate, endDate);

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
            pieChartData.add(new PieChart.Data(formatStatusLabel(entry.getKey()), entry.getValue()));
        }

        accessStatusChart.setData(pieChartData);
    }

    private String formatStatusLabel(String status) {
        // Formatage du label pour le graphique
        switch (status.toLowerCase()) {
            case "success":
                return "Succès";
            case "failure":
                return "Échec";
            case "denied":
                return "Refusé";
            default:
                return status;
        }
    }

    private void updateDailyAccessChart(LocalDate startDate, LocalDate endDate) throws SQLException {
        // Récupération des données pour le graphique à barres
        Map<String, Integer> dailyCounts = AccessLogRepository.getDailyAccessCounts(startDate, endDate);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Accès");

        dailyCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));

        dailyAccessChart.getData().clear();
        dailyAccessChart.getData().add(series);
    }

    private void updateTrendChart(LocalDate startDate, LocalDate endDate) throws SQLException {
        // Récupération des données pour le graphique de tendance
        Map<String, Integer> successTrend = AccessLogRepository.getAccessTrendByStatus(startDate, endDate, "success");
        Map<String, Integer> failureTrend = AccessLogRepository.getAccessTrendByStatus(startDate, endDate, "failure");

        XYChart.Series<String, Number> successSeries = new XYChart.Series<>();
        successSeries.setName("Succès");

        XYChart.Series<String, Number> failureSeries = new XYChart.Series<>();
        failureSeries.setName("Échec");

        // Ajout des données pour les succès
        successTrend.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> successSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));

        // Ajout des données pour les échecs
        failureTrend.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> failureSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));

        trendChart.getData().clear();
        trendChart.getData().add(successSeries);
        trendChart.getData().add(failureSeries);
    }

    private void updateRecentAccessTable(LocalDate startDate, LocalDate endDate) throws SQLException {
        // Récupération des journaux d'accès récents
        ObservableList<AccessLog> accessLogs = AccessLogRepository.getAccessLogs(startDate, endDate);
        accessLogsTable.setItems(accessLogs);
    }
}