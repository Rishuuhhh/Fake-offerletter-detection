package main;

import model.*;
import service.VerificationEngine;

public class MainApp {

    public static void main(String[] args) {

        Offer offer = new JobOffer(
                "Fake Company",
                "hr.fake@gmail.com",
                1500000,
                "Urgent hiring! Pay registration fee now!",
                true
        );

        VerificationResult result = VerificationEngine.evaluateDetailed(
            offer,
            true,
            false,
            "Software Engineer",
            "Job Offer"
        );

        System.out.println("Risk Score: " + result.getRiskScore());
        System.out.println("NLP Score: " + result.getNlpRisk());
        System.out.println("Result: " + result.getVerdict());
    }
}