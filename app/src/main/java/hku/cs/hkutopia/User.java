package hku.cs.hkutopia;

/**
 * Model class to store user information
 */
public class User {
    private String name;
    private String email;
    
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * Returns a masked version of the email for privacy
     * Example: j***@example.com
     */
    public String getMaskedEmail() {
        if (email == null || email.isEmpty() || !email.contains("@")) {
            return "";
        }
        
        int atIndex = email.indexOf('@');
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        
        if (username.length() <= 1) {
            return username + "***" + domain;
        } else {
            return username.charAt(0) + "***" + domain;
        }
    }
} 