package miniproject.excelio;

import java.util.ArrayList;
import java.util.List;

public class UpdateResult {

    private int updatedCount;
    private int unchangedCount;
    private int addedCount;
    private int skippedNewCount;
    private int failedCount;
    private final List<String> failureMessages = new ArrayList<>();

    public void addUpdated() {
        updatedCount++;
    }

    public int getUpdatedCount() {
        return updatedCount;
    }

    public void setUnchangedCount(int unchangedCount) {
        this.unchangedCount = unchangedCount;
    }

    public int getUnchangedCount() {
        return unchangedCount;
    }

    public void addAdded() {
        addedCount++;
    }

    public int getAddedCount() {
        return addedCount;
    }

    public void setSkippedNewCount(int skippedNewCount) {
        this.skippedNewCount = skippedNewCount;
    }

    public int getSkippedNewCount() {
        return skippedNewCount;
    }

    public void addFailure(String message) {
        failedCount++;
        failureMessages.add(message);
    }

    public void addFailure(int rowNumber, String reason) {
        failedCount++;
        failureMessages.add("Row " + rowNumber + ": " + reason);
    }

    public int getFailedCount() {
        return failedCount;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }
}
