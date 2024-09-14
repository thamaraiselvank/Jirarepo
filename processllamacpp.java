import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LargeActivityProcessor {

    private static final String DB_URL = "jdbc:sqlite:example.db";
    private static final int BATCH_SIZE = 10000;  // Large batch size to simulate memory issues

    public static void main(String[] args) {
        List<Activity> activities = generateLargeActivityList();
        processActivities(activities);
    }

    private static List<Activity> generateLargeActivityList() {
        List<Activity> activities = new ArrayList<>();
        // Generate a large number of activities to simulate a large dataset
        for (int i = 0; i < 500000; i++) {
            activities.add(new Activity(
                "Activity " + i, 
                "pending", 
                "category" + (i % 100), 
                "user" + (i % 1000), 
                "extra" + (i % 10000)
            ));
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

            // Create table with 5 columns
            conn.createStatement().execute("CREATE TABLE IF NOT EXISTS activities (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "status TEXT, " +
                "category TEXT, " +
                "user TEXT, " +
                "extra TEXT)");

            // Prepare statements for insertion and updates
            insertStmt = conn.prepareStatement("INSERT INTO activities (name, status, category, user, extra) VALUES (?, ?, ?, ?, ?)");
            updateStmt = conn.prepareStatement("UPDATE activities SET status = ? WHERE id = ?");

            int counter = 0;
            for (int i = 0; i < activities.size(); i++) {
                Activity activity = activities.get(i);
                insertStmt.setString(1, activity.getName());
                insertStmt.setString(2, activity.getStatus());
                insertStmt.setString(3, activity.getCategory());
                insertStmt.setString(4, activity.getUser());
                insertStmt.setString(5, activity.getExtra());
                insertStmt.addBatch();

                // Insert and update in large batches
                if (++counter % BATCH_SIZE == 0) {
                    insertStmt.executeBatch();  // Execute insert batch
                    conn.commit();

                    // Simulate a potential issue with excessive updates
                    for (int j = i - BATCH_SIZE + 1; j <= i; j++) {
                        updateStmt.setString(1, "processed");
                        updateStmt.setInt(2, j + 1);  // Ensure index is within bounds
                        updateStmt.addBatch();
                    }
                    updateStmt.executeBatch();
                    conn.commit();
                }
            }

            // Final batch processing
            if (counter % BATCH_SIZE != 0) {
                insertStmt.executeBatch();  // Execute remaining insert batch
                updateStmt.executeBatch();  // Execute remaining update batch
                conn.commit();
            }

        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
        } catch (OutOfMemoryError e) {
            System.err.println("OutOfMemoryError: " + e.getMessage());
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
    private String category;
    private String user;
    private String extra;

    public Activity(String name, String status, String category, String user, String extra) {
        this.name = name;
        this.status = status;
        this.category = category;
        this.user = user;
        this.extra = extra;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getCategory() {
        return category;
    }

    public String getUser() {
        return user;
    }

    public String getExtra() {
        return extra;
    }
}
