package model;

public class JobOffer extends Offer {

    public JobOffer(String companyName, String email, double salary, String description, boolean hasFee) {
        super(companyName, email, salary, description, hasFee);
    }

    @Override
    public int verifyOffer() {
        // Legacy method — use VerificationEngine instance via AppLauncher for full analysis
        return 0;
    }
}
