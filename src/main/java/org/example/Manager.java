import java.io.Serializable;

public class Manager extends Account implements Serializable {
    private String employeeStatus;
    private String managersContract;
    private String favorite;

    public Manager(String managerName, String password, String managerPhoneNumber, String managerEmail, String employeeStatus, String managersContract, String favorite) {
        super(managerName, password, managerPhoneNumber, managerEmail);
        this.employeeStatus = employeeStatus;
        this.managersContract = managersContract;
        this.favorite = favorite;
    }

    public String getEmployeeStatus() {
        return employeeStatus;
    }

    public void setEmployeeStatus(String employeeStatus) {
        this.employeeStatus = employeeStatus;
    }

    public String getManagersContract() {
        return managersContract;
    }

    public void setManagersContract(String managersContract) {
        this.managersContract = managersContract;
    }

    public String getFavorite() {
        return favorite;
    }

    public void setFavorite(String favorite) {
        this.favorite = favorite;
    }

    @Override
    public String toString() {
        return "Manager{" +
                "managerName='" + getAccountName() + '\'' +
                ", password='" + getPassword() + '\'' +
                ", managerPhoneNumber='" + getPhoneNumber() + '\'' +
                ", managerEmail='" + getEmail() + '\'' +
                ", employeeStatus='" + employeeStatus + '\'' +
                ", managersContract='" + managersContract + '\'' +
                ", favorite='" + favorite + '\'' +
                '}';
    }

}