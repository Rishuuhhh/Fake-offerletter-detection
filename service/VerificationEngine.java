package service;

import model.Offer;
import model.VerificationResult;

import java.util.ArrayList;
import java.util.List;

public class VerificationEngine {

    public static int evaluate(Offer offer) {
        return evaluateDetailed(offer, false, false, "", "").getRiskScore();
    }

    public static VerificationResult evaluateDetailed(
            Offer offer,
            boolean usesUrgency,
            boolean asksPersonalInfo,
            String position,
            String offerType
    ) {
        int riskScore = 0;
        List<String> findings = new ArrayList<>();

        if (!RuleChecker.isValidEmail(offer.getEmail())) {
            riskScore += 30;
            findings.add("Email domain is not an official company domain.");
        } else {
            findings.add("Email domain appears corporate.");
        }

        boolean internshipOffer = RuleChecker.isLikelyInternshipOffer(offerType, position);
        if (isUnrealisticForRole(offer.getSalary(), internshipOffer)) {
            riskScore += 25;
            findings.add("Compensation appears unusually high for the selected role.");
        }

        if (offer.hasFee()) {
            riskScore += 35;
            findings.add("Offer requests registration/security fee.");
        }

        if (usesUrgency) {
            riskScore += 15;
            findings.add("Urgency language flag was selected.");
        }

        if (asksPersonalInfo) {
            riskScore += 30;
            findings.add("Sensitive personal information is requested upfront.");
        }

        if (RuleChecker.hasWeakCompanyName(offer.getCompanyName())) {
            riskScore += 12;
            findings.add("Company name format looks weak or incomplete.");
        }

        NlpSignalAnalyzer.NlpAnalysis nlp = NlpSignalAnalyzer.analyze(offer.getDescription());
        int nlpRisk = nlp.getRisk();
        riskScore += Math.round(nlpRisk * 0.45f);
        findings.addAll(nlp.getNotes());

        if (RuleChecker.hasSuspiciousWords(offer.getDescription())) {
            riskScore += 10;
            findings.add("Known high-risk phrase pattern was matched.");
        }

        riskScore = Math.max(0, Math.min(riskScore, 100));
        String verdict = classify(riskScore);
        return new VerificationResult(riskScore, nlpRisk, verdict, findings);
    }

    public static String classify(int score) {
        if (score >= 60)
            return "FAKE";
        else if (score >= 30)
            return "SUSPICIOUS";
        else
            return "GENUINE";
    }

    private static boolean isUnrealisticForRole(double salary, boolean internshipOffer) {
        if (salary <= 0) {
            return false;
        }
        if (internshipOffer) {
            return salary > 150000;
        }
        return RuleChecker.isUnrealisticSalary(salary);
    }
}
