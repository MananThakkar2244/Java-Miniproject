package miniproject.model;

import java.time.LocalDate;

public interface Trackable {

    void calculateDpd(LocalDate today);

    String getBucket();

    String getPaidStatus();
}
