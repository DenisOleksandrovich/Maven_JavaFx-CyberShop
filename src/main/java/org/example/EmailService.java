import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import java.nio.charset.StandardCharsets;

public class EmailService {
    private static final String SERVICE_ID = "service_9d6sf7p";
    private static final String TEMPLATE_ID = "template_52ptzmr";
    private static final String USER_ID = "E5-oShNXmsAcSqbJq";
    private static final String EMAILJS_API = "https://api.emailjs.com/api/v1.0/email/send";

    public static void sendEmail(String managerEmail, String clientName, String productName, int quantity, double totalAmount, String deadline) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(EMAILJS_API);
            httpPost.setHeader("Content-Type", "application/json");

            JsonObject json = new JsonObject();
            json.addProperty("service_id", SERVICE_ID);
            json.addProperty("template_id", TEMPLATE_ID);
            json.addProperty("user_id", USER_ID);

            JsonObject templateParams = new JsonObject();
            templateParams.addProperty("manager_email", managerEmail);
            templateParams.addProperty("client_name", clientName);
            templateParams.addProperty("product_name", productName);
            templateParams.addProperty("quantity", quantity);
            templateParams.addProperty("total_amount", totalAmount);
            templateParams.addProperty("deadline", deadline);

            json.add("template_params", templateParams);

            StringEntity entity = new StringEntity(json.toString(), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                String responseText = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject responseJson = JsonParser.parseString(responseText).getAsJsonObject();
                System.out.println("Email response: " + responseJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
