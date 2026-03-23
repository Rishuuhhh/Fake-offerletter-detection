package service;

import model.Offer;

public class VerificationEngine {

    public static int evaluate(Offer offer) {
        int riskScore = 0;

        if (!RuleChecker.isValidEmail(offer.getEmail()))
            riskScore += 30;

        if (RuleChecker.isUnrealisticSalary(offer.getSalary()))
            riskScore += 25;

        if (offer.hasFee())
            riskScore += 40;

        if (RuleChecker.hasSuspiciousWords(offer.getDescription()))
            riskScore += 20;

        return riskScore;
    }

    public static String classify(int score) {
        if (score >= 60)
            return "FAKE";
        else if (score >= 30)
            return "SUSPICIOUS";
        else
            return "GENUINE";
    }
}
