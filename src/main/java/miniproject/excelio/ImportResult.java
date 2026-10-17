package miniproject.excelio;

import java.util.ArrayList;
import java.util.List;

public class ImportResult {

    private int importedCount;
    private int duplicateCount;
    private int failedCount;
    private final List<String> failureMessages = new ArrayList<>();

    public void addImported() {
        importedCount++;
    }

    public void addDuplicate() {
        duplicateCount++;
    }

    public void addFailure(int rowNumber, String reason) {
        failedCount++;
        failureMessages.add("Row " + rowNumber + ": " + reason);
    }

    public int getImportedCount() {
        return importedCount;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }
}
