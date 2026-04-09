package model;

// full-time job offer
public class JobOffer extends Offer {

    public JobOffer(String companyName, String email, double salary, String description, boolean requiresFee) {
        super(companyName, email, salary, description, requiresFee);
    }
}
