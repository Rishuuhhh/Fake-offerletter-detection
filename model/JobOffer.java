package model;

import service.VerificationEngine;

public class JobOffer extends Offer {

    public JobOffer(String companyName, String email, double salary, String description, boolean hasFee) {
        super(companyName, email, salary, description, hasFee);
    }

    @Override
    public int verifyOffer() {
        return VerificationEngine.evaluate(this);
    }
}