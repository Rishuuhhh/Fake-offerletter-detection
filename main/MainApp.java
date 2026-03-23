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

        int score = offer.verifyOffer();
        String result = VerificationEngine.classify(score);

        System.out.println("Risk Score: " + score);
        System.out.println("Result: " + result);
    }
}