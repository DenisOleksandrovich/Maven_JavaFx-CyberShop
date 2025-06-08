import java.util.List;

public class Contract {
    private int id;
    private String clientName;
    private String managerName;
    private double productPrice;
    private int productCount;
    private String status;
    private String deliveryMethod;
    private String paymentMethod;
    private List<Integer> productIds;
    private String additionalProducts;
    private String deadline;
    private double totalAmount;

    public Contract(int id, String clientName, String managerName, int productCount, String status, String deliveryMethod, String paymentMethod, List<Integer> productIds, String additionalProducts, String deadline, double totalAmount) {
        this.id = id;
        this.clientName = clientName;
        this.managerName = managerName;
        this.productCount = productCount;
        this.status = status;
        this.deliveryMethod = deliveryMethod;
        this.paymentMethod = paymentMethod;
        this.productIds = productIds;
        this.additionalProducts = additionalProducts;
        this.deadline = deadline;
        this.totalAmount = totalAmount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<Integer> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Integer> productIds) {
        this.productIds = productIds;
    }

    public String getAdditionalProducts() {
        return additionalProducts;
    }

    public void setAdditionalProducts(String additionalProducts) {
        this.additionalProducts = additionalProducts;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Override
    public String toString() {
        return "Contract ID: " + id + " (Date: " + deadline + ")";
    }
}