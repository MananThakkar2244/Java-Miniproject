package miniproject.model;

public class Customer {

    private final String uniqueId;
    private final String name;
    private final String address;
    private final String phone;
    private final String email;

    public Customer(String uniqueId, String name, String address, String phone, String email) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "uniqueId: '" + uniqueId + '\'' +
                ", name: '" + name + '\'' +
                ", phone: '" + phone + '\'' +
                ", email: '" + email + '\'' +
                '}';
    }
}
