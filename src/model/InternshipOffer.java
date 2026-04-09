package model;

public class InternshipOffer extends Offer {

    public InternshipOffer(String companyName, String email, double salary, String description, boolean hasFee) {
        super(companyName, email, salary, description, hasFee);
    }

    @Override
    public int verifyOffer() {
        // Legacy method — use VerificationEngine instance via AppLauncher for full analysis
        return 0;
    }
}
