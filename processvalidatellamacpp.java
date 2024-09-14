import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ErrorSimulation {

    private static final String DB_URL = "jdbc:sqlite:example.db";
    private static final int BATCH_SIZE = 10000;  // Large batch size to simulate memory issues

    public static void main(String[] args) {
        try {
            List<Activity> activities = generateLargeActivityList();
            processActivities(activities);
        } catch (OutOfMemoryError e) {
            System.err.println("Simulated Segmentation Fault: " + e.getMessage());
        }
    }

    private static List<Activity> generateLargeActivityList() {
        List<Activity> activities = new ArrayList<>();
        // Generate a large number of activities to simulate memory overload
        for (int i = 0; i < 1000000; i++) {
            activities.add(new Activity("Activity " + i, "pending"));
        }
        return activities;
    }

    private static void processActivities(List<Activity> activities) {
        Connection conn = null;
        PreparedStatement insertStmt = null;
        PreparedStatement updateStmt = null;

        try {
            conn = DriverManager.getConnection(DB_URL);
            conn.setAutoCommit(false);

            // Create table with large batch size and updates to cause stress
            conn.createStatement().execute("CREATE TABLE IF NOT EXISTS activities (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "status TEXT)");

            // Prepare statements for insertion and updates
            insertStmt = conn.prepareStatement("INSERT INTO activities (name, status) VALUES (?, ?)");
            updateStmt = conn.prepareStatement("UPDATE activities SET status = ? WHERE id = ?");

            int counter = 0;
            for (int i = 0; i < activities.size(); i++) {
                Activity activity = activities.get(i);
                insertStmt.setString(1, activity.getName());
                insertStmt.setString(2, activity.getStatus());
                insertStmt.addBatch();

                // Stress the system by executing large batch inserts and updates
                if (++counter % BATCH_SIZE == 0) {
                    insertStmt.executeBatch();  // Potential memory issue here
                    conn.commit();

                    // Simulate issues with excessive updates
                    for (int j = i - BATCH_SIZE + 1; j <= i; j++) {
                        updateStmt.setString(1, "processed");
                        updateStmt.setInt(2, j + 1);  // Potential out-of-bounds issue
                        updateStmt.addBatch();
                    }
                    updateStmt.executeBatch();
                    conn.commit();
                }
            }

            // Final batch processing
            if (counter % BATCH_SIZE != 0) {
                insertStmt.executeBatch();
                updateStmt.executeBatch();
                conn.commit();
            }

        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
        } catch (OutOfMemoryError e) {
            System.err.println("Simulated Segmentation Fault: " + e.getMessage());
        } finally {
            try {
                if (insertStmt != null) insertStmt.close();
                if (updateStmt != null) updateStmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("SQL Exception during cleanup: " + e.getMessage());
            }
        }
    }
}

class Activity {
    private String name;
    private String status;

    public Activity(String name, String status) {
        this.name = name;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }
}
