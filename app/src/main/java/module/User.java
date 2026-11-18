package module;
public class User {

    public String uid;
    public String firstName;
    public String lastName;
    public String email;

    public User() {

    }

    public User(String uid, String firstName, String lastName, String email) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
}
