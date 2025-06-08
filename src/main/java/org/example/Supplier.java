public class Supplier {
    private int id;
    private String title;
    private String type;
    private String description;
    private String specialization;
    private String status;
    private String contactNumber;
    private String website;

    public Supplier(int id, String title, String type, String description, String specialization, String status, String contactNumber, String website) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.description = description;
        this.specialization = specialization;
        this.status = status;
        this.contactNumber = contactNumber;
        this.website = website;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}