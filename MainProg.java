import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainProg extends Application {

    private Map<String, String> monitoredProcesses = new HashMap<>();
    private boolean isAfterburnerRunning = false;
    private final String FILENAME = "monitored_processes.txt"; // File to store monitored processes

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Load monitored processes from the file if exists
        loadMonitoredProcesses();

        primaryStage.setTitle("Process Monitor");

        Label processLabel = new Label("Enter Process Name:");
        TextField processField = new TextField();

        Button addButton = new Button("Add Process to Monitor");
        addButton.setOnAction(e -> {
            String processName = processField.getText().trim();
            if (!processName.isEmpty()) {
                monitoredProcesses.put(processName, "D:\\Program Files (x86)\\MSI Afterburner\\MSIAfterburner.exe");
                saveMonitoredProcesses(); // Save updated monitored processes to file
                System.out.println("Added " + processName + " to monitored processes.");
            } else {
                System.out.println("Please enter a process name.");
            }
        });

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.getChildren().addAll(processLabel, processField, addButton);

        Scene scene = new Scene(layout, 300, 150);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Start monitoring in a background thread
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(new MonitorTask(), 0, 5, TimeUnit.SECONDS);
    }

    private void saveMonitoredProcesses() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILENAME))) {
            for (String process : monitoredProcesses.keySet()) {
                writer.println(process);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // For my use case, I want it to open my GPU tweaking software so I can remember to undervolt
    private void loadMonitoredProcesses() {
        File file = new File(FILENAME);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    monitoredProcesses.put(line, "D:\\Program Files (x86)\\MSI Afterburner\\MSIAfterburner.exe");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class MonitorTask implements Runnable {
        @Override
        public void run() {
            for (String processName : monitoredProcesses.keySet()) {
                if (isProcessRunning(processName)) {
                    if (!isAfterburnerRunning) {
                        openMSIAfterburner();
                        isAfterburnerRunning = true;
                    }
                } else {
                    isAfterburnerRunning = false;
                }
            }
        }
    }

    private boolean isProcessRunning(String processName) {
        try {
            Process process = Runtime.getRuntime().exec("tasklist /fo csv /nh");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] processInfo = line.split(",");
                if (processInfo.length > 0 && processInfo[0].replace("\"", "").equals(processName)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Method that will open the actual application
    private void openMSIAfterburner() {
        try {
            // Path of
            String afterburnerPath = "D:\\Program Files (x86)\\MSI Afterburner\\MSIAfterburner.exe";
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "start", "\"\"", afterburnerPath);
            processBuilder.start();
            System.out.println("MSI Afterburner is launched.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Cleanup code if needed
        System.out.println("Application is closing...");
    }
}