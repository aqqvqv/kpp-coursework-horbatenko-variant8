package ua.edu.nupp.kpp.client;

import javafx.concurrent.Task;
import ua.edu.nupp.kpp.client.model.Measurement;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

public class ImportMeasurementsTask extends Task<Integer> {
    private static final Logger LOG = Logger.getLogger(ImportMeasurementsTask.class.getName());
    private final Path csvPath;
    private final HttpWaterQualityClient client;

    public ImportMeasurementsTask(Path csvPath, HttpWaterQualityClient client) {
        this.csvPath = csvPath;
        this.client = client;
    }

    @Override
    protected Integer call() throws Exception {
        if (!Files.exists(csvPath)) {
            throw new IllegalArgumentException("CSV-файл не знайдено: " + csvPath);
        }
        List<String> lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
        int total = Math.max(1, lines.size() - 1);
        int imported = 0;

        for (int i = 1; i < lines.size(); i++) {
            if (isCancelled()) {
                updateMessage("Імпорт скасовано користувачем");
                break;
            }
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(";");
            if (parts.length != 5) {
                LOG.warning("Пропущено некоректний рядок CSV №" + (i + 1) + ": " + line);
                continue;
            }

            Measurement m = new Measurement(
                    null,
                    Long.parseLong(parts[0].trim()),
                    parts[1].trim(),
                    Double.parseDouble(parts[2].trim()),
                    Double.parseDouble(parts[3].trim()),
                    parts[4].trim()
            );
            client.addMeasurement(m);
            imported++;
            updateProgress(i, total);
            updateMessage("Імпортовано: " + imported + " із " + total);
        }
        return imported;
    }
}
