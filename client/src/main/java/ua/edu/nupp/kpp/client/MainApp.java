package ua.edu.nupp.kpp.client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ua.edu.nupp.kpp.client.model.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class MainApp extends Application {
    private static final Logger LOG = Logger.getLogger(MainApp.class.getName());
    private final HttpWaterQualityClient client = new HttpWaterQualityClient();

    private final TableView<ObservationPoint> pointTable = new TableView<>();
    private final TableView<Sample> sampleTable = new TableView<>();
    private final TableView<Measurement> measurementTable = new TableView<>();
    private final TableView<Inspection> inspectionTable = new TableView<>();

    @Override
    public void start(Stage stage) {
        TabPane tabs = new TabPane(
                new Tab("Пункти спостереження", createPointPane()),
                new Tab("Проби води", createSamplePane()),
                new Tab("Вимірювання", createMeasurementPane(stage)),
                new Tab("Перевірки", createInspectionPane())
        );
        tabs.getTabs().forEach(t -> t.setClosable(false));

        stage.setScene(new Scene(tabs, 1100, 680));
        stage.setTitle("Моніторинг якості води — Варіант №8 (Горбатенко А. А.)");
        stage.show();
        refreshAll();
    }

    private Pane createPointPane() {
        pointTable.getColumns().setAll(
                col("ID", "id"),
                col("Код", "code"),
                col("Назва пункту", "name"),
                col("Водний об'єкт", "waterBody"),
                col("Чутливість F3 (0-100)", "sensitivityScore")
        );

        TextField code = new TextField();
        TextField name = new TextField();
        TextField waterBody = new TextField();
        TextField sensitivity = new TextField();

        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");

        add.setOnAction(e -> safe(() -> {
            client.addPoint(new ObservationPoint(null, code.getText(), name.getText(),
                    waterBody.getText(), Double.parseDouble(sensitivity.getText())));
            refreshPoints();
        }));

        update.setOnAction(e -> safe(() -> {
            ObservationPoint selected = getSelected(pointTable);
            client.updatePoint(new ObservationPoint(selected.getId(), code.getText(), name.getText(),
                    waterBody.getText(), Double.parseDouble(sensitivity.getText())));
            refreshAll();
        }));

        delete.setOnAction(e -> safe(() -> {
            client.deletePoint(getSelected(pointTable).getId());
            refreshAll();
        }));

        refresh.setOnAction(e -> refreshPoints());

        pointTable.getSelectionModel().selectedItemProperty().addListener((o, old, pt) -> {
            if (pt != null) {
                code.setText(pt.getCode());
                name.setText(pt.getName());
                waterBody.setText(pt.getWaterBody());
                sensitivity.setText(String.valueOf(pt.getSensitivityScore()));
            }
        });

        VBox form = new VBox(8,
                new Label("Код пункту"), code,
                new Label("Назва пункту"), name,
                new Label("Водний об'єкт"), waterBody,
                new Label("Чутливість водокористування F3 (0..100)"), sensitivity,
                new HBox(8, add, update, delete, refresh)
        );
        return layout(pointTable, form);
    }

    private Pane createSamplePane() {
        sampleTable.getColumns().setAll(
                col("ID", "id"),
                col("ID Пункту", "pointId"),
                col("Дата відбору", "sampleDate"),
                col("Стан", "state"),
                col("F1 (Перевищення)", "f1"),
                col("F2 (Крит. параметри)", "f2"),
                col("F3 (Чутливість)", "f3"),
                col("Інтегральний I", "integralScore")
        );

        TextField pointId = new TextField();
        TextField sampleDate = new TextField("2026-10-20");
        ComboBox<SampleState> stateBox = new ComboBox<>(FXCollections.observableArrayList(SampleState.values()));

        Button add = new Button("Зареєструвати");
        Button update = new Button("Оновити / Перерахувати");
        Button changeState = new Button("Змінити стан");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити список");

        add.setOnAction(e -> safe(() -> {
            client.addSample(Long.parseLong(pointId.getText()), sampleDate.getText());
            refreshSamples();
        }));

        update.setOnAction(e -> safe(() -> {
            Sample s = getSelected(sampleTable);
            client.updateSample(s.getId(), Long.parseLong(pointId.getText()), sampleDate.getText());
            refreshSamples();
        }));

        changeState.setOnAction(e -> safe(() -> {
            Sample s = getSelected(sampleTable);
            if (stateBox.getValue() == null) {
                throw new IllegalArgumentException("Оберіть цільовий стан проби");
            }
            client.changeSampleState(s.getId(), stateBox.getValue());
            refreshSamples();
        }));

        delete.setOnAction(e -> safe(() -> {
            client.deleteSample(getSelected(sampleTable).getId());
            refreshAll();
        }));

        refresh.setOnAction(e -> refreshSamples());

        sampleTable.getSelectionModel().selectedItemProperty().addListener((o, old, s) -> {
            if (s != null) {
                pointId.setText(String.valueOf(s.getPointId()));
                sampleDate.setText(s.getSampleDate());
                stateBox.setValue(s.getState());
            }
        });

        VBox form = new VBox(8,
                new Label("ID пункту спостереження"), pointId,
                new Label("Дата відбору (YYYY-MM-DD)"), sampleDate,
                new HBox(8, add, update),
                new Separator(),
                new Label("Життєвий цикл проби (Наступний стан):"),
                stateBox,
                new HBox(8, changeState, delete, refresh)
        );
        return layout(sampleTable, form);
    }

    private Pane createMeasurementPane(Stage stage) {
        measurementTable.getColumns().setAll(
                col("ID", "id"),
                col("ID Проби", "sampleId"),
                col("Показник", "parameterName"),
                col("Виміряне знач.", "measuredValue"),
                col("Норматив (ГДК)", "normValue"),
                col("Од. вим.", "unit")
        );

        TextField sampleId = new TextField();
        TextField paramName = new TextField();
        TextField measuredVal = new TextField();
        TextField normVal = new TextField();
        TextField unit = new TextField("мг/дм3");

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        Label statusLabel = new Label("CSV-імпорт не запускався");

        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");
        Button importDefaultCsv = new Button("Імпорт sample-data/measurements.csv");
        Button chooseCsv = new Button("Обрати інший CSV...");

        add.setOnAction(e -> safe(() -> {
            client.addMeasurement(new Measurement(null, Long.parseLong(sampleId.getText()),
                    paramName.getText(), Double.parseDouble(measuredVal.getText()),
                    Double.parseDouble(normVal.getText()), unit.getText()));
            refreshAll();
        }));

        update.setOnAction(e -> safe(() -> {
            Measurement m = getSelected(measurementTable);
            client.updateMeasurement(new Measurement(m.getId(), Long.parseLong(sampleId.getText()),
                    paramName.getText(), Double.parseDouble(measuredVal.getText()),
                    Double.parseDouble(normVal.getText()), unit.getText()));
            refreshAll();
        }));

        delete.setOnAction(e -> safe(() -> {
            client.deleteMeasurement(getSelected(measurementTable).getId());
            refreshAll();
        }));

        refresh.setOnAction(e -> refreshMeasurements());

        importDefaultCsv.setOnAction(e -> {
            Path direct = Path.of("sample-data", "measurements.csv");
            Path fromRoot = Path.of("client", "sample-data", "measurements.csv");
            Path target = Files.exists(direct) ? direct : fromRoot;
            runCsvImport(target, progressBar, statusLabel);
        });

        chooseCsv.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Оберіть CSV-файл вимірювань");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV файли", "*.csv"));
            File chosen = chooser.showOpenDialog(stage);
            if (chosen != null) {
                runCsvImport(chosen.toPath(), progressBar, statusLabel);
            }
        });

        measurementTable.getSelectionModel().selectedItemProperty().addListener((o, old, m) -> {
            if (m != null) {
                sampleId.setText(String.valueOf(m.getSampleId()));
                paramName.setText(m.getParameterName());
                measuredVal.setText(String.valueOf(m.getMeasuredValue()));
                normVal.setText(String.valueOf(m.getNormValue()));
                unit.setText(m.getUnit());
            }
        });

        VBox form = new VBox(7,
                new Label("ID проби"), sampleId,
                new Label("Назва показника"), paramName,
                new Label("Виміряне значення"), measuredVal,
                new Label("Нормативне значення"), normVal,
                new Label("Одиниця вимірювання"), unit,
                new HBox(8, add, update, delete, refresh),
                new Separator(),
                new Label("Фоновий CSV-імпорт вимірювань:"),
                importDefaultCsv,
                chooseCsv,
                progressBar,
                statusLabel
        );
        return layout(measurementTable, form);
    }

    private void runCsvImport(Path path, ProgressBar progressBar, Label statusLabel) {
        ImportMeasurementsTask task = new ImportMeasurementsTask(path, client);
        progressBar.progressProperty().bind(task.progressProperty());
        statusLabel.textProperty().bind(task.messageProperty());

        task.setOnSucceeded(ev -> {
            progressBar.progressProperty().unbind();
            statusLabel.textProperty().unbind();
            statusLabel.setText("Готово. Імпортовано вимірювань: " + task.getValue());
            refreshAll();
        });

        task.setOnFailed(ev -> {
            progressBar.progressProperty().unbind();
            statusLabel.textProperty().unbind();
            statusLabel.setText("Помилка імпорту!");
            showError(task.getException());
        });

        new Thread(task, "csv-measurements-import").start();
    }

    private Pane createInspectionPane() {
        inspectionTable.getColumns().setAll(
                col("ID", "id"),
                col("ID Проби", "sampleId"),
                col("Інспектор", "inspectorName"),
                col("Дата перевірки", "inspectionDate"),
                col("Висновок", "conclusion")
        );

        TextField sampleId = new TextField();
        TextField inspector = new TextField();
        TextField date = new TextField("2026-10-21");
        TextField conclusion = new TextField();

        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");

        add.setOnAction(e -> safe(() -> {
            client.addInspection(new Inspection(null, Long.parseLong(sampleId.getText()),
                    inspector.getText(), date.getText(), conclusion.getText()));
            refreshInspections();
        }));

        update.setOnAction(e -> safe(() -> {
            Inspection ins = getSelected(inspectionTable);
            client.updateInspection(new Inspection(ins.getId(), Long.parseLong(sampleId.getText()),
                    inspector.getText(), date.getText(), conclusion.getText()));
            refreshInspections();
        }));

        delete.setOnAction(e -> safe(() -> {
            client.deleteInspection(getSelected(inspectionTable).getId());
            refreshInspections();
        }));

        refresh.setOnAction(e -> refreshInspections());

        inspectionTable.getSelectionModel().selectedItemProperty().addListener((o, old, ins) -> {
            if (ins != null) {
                sampleId.setText(String.valueOf(ins.getSampleId()));
                inspector.setText(ins.getInspectorName());
                date.setText(ins.getInspectionDate());
                conclusion.setText(ins.getConclusion());
            }
        });

        VBox form = new VBox(8,
                new Label("ID проби"), sampleId,
                new Label("ПІБ інспектора"), inspector,
                new Label("Дата перевірки (YYYY-MM-DD)"), date,
                new Label("Висновок перевірки"), conclusion,
                new HBox(8, add, update, delete, refresh)
        );
        return layout(inspectionTable, form);
    }

    private void refreshAll() {
        refreshPoints();
        refreshSamples();
        refreshMeasurements();
        refreshInspections();
    }

    private void refreshPoints() {
        safe(() -> pointTable.setItems(FXCollections.observableArrayList(client.getPoints())));
    }

    private void refreshSamples() {
        safe(() -> sampleTable.setItems(FXCollections.observableArrayList(client.getSamples())));
    }

    private void refreshMeasurements() {
        safe(() -> measurementTable.setItems(FXCollections.observableArrayList(client.getMeasurements())));
    }

    private void refreshInspections() {
        safe(() -> inspectionTable.setItems(FXCollections.observableArrayList(client.getInspections())));
    }

    private <T> T getSelected(TableView<T> table) {
        T item = table.getSelectionModel().getSelectedItem();
        if (item == null) {
            throw new IllegalArgumentException("Оберіть запис у таблиці");
        }
        return item;
    }

    private void safe(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            LOG.warning("Помилка в клієнті: " + e.getMessage());
            showError(e);
        }
    }

    private void showError(Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR,
                e == null || e.getMessage() == null ? "Невідома помилка" : e.getMessage(),
                ButtonType.OK);
        alert.setTitle("Помилка виконання");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private Pane layout(TableView<?> table, Pane form) {
        SplitPane split = new SplitPane(table, form);
        split.setDividerPositions(0.67);
        BorderPane root = new BorderPane(split);
        root.setPadding(new Insets(10));
        return root;
    }

    private <S, T> TableColumn<S, T> col(String title, String property) {
        TableColumn<S, T> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(property));
        return c;
    }

    public static void main(String[] args) {
        launch(args);
    }
}