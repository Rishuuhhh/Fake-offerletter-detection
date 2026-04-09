package model;

// base class for job/internship offers
public abstract class Offer {

    private final String companyName;
    private final String email;
    private final double salary;
    private final String description;
    private final boolean requiresFee;

    public Offer(String companyName, String email, double salary, String description, boolean requiresFee) {
        this.companyName = companyName;
        this.email = email;
        this.salary = salary;
        this.description = description;
        this.requiresFee = requiresFee;
    }

    public String getCompanyName() { 
        return companyName; 
    }
    
    public String getEmail() { 
        return email; 
    }
    
    public double getSalary() { 
        return salary; 
    }
    
    public String getDescription() { 
        return description; 
    }
    
    public boolean hasFee() { 
        return requiresFee; 
    }
}
