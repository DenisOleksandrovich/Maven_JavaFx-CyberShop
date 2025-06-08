import java.io.Serializable;

public class User extends Account implements Serializable {
    private String favorite;
    private String userContacts;

    public User(String userName, String password, String userEmail, String userPhoneNumber, String favorite, String userContacts) {
        super(userName, password, userPhoneNumber, userEmail);
        this.favorite = favorite;
        this.userContacts = userContacts;
    }

    public String getFavorite() {
        return favorite;
    }

    public void setFavorite(String favorite) {
        this.favorite = favorite;
    }

    public String getUserContacts() {
        return userContacts;
    }

    public void setUserContacts(String userContacts) {
        this.userContacts = userContacts;
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + getAccountName() + '\'' +
                ", password='" + getPassword() + '\'' +
                ", userEmail='" + getEmail() + '\'' +
                ", userPhoneNumber='" + getPhoneNumber() + '\'' +
                ", favorite='" + favorite + '\'' +
                ", userContacts='" + userContacts + '\'' +
                '}';
    }

}