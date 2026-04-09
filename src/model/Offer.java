package model;

public abstract class Offer {
    private String companyName;
    private String email;
    private double salary;
    private String description;
    private boolean hasFee;

    public Offer(String companyName, String email, double salary, String description, boolean hasFee) {
        this.companyName = companyName;
        this.email = email;
        this.salary = salary;
        this.description = description;
        this.hasFee = hasFee;
    }

    public String  getCompanyName() { return companyName; }
    public String  getEmail()       { return email; }
    public double  getSalary()      { return salary; }
    public String  getDescription() { return description; }
    public boolean hasFee()         { return hasFee; }

    public abstract int verifyOffer();
}
