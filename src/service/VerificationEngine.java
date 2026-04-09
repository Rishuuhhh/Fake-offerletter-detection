package service;

import model.Offer;
import model.VerificationResult;
import java.util.ArrayList;
import java.util.List;

/**
 * Main scoring engine - combines basic checks with NLP text analysis.
 * 
 * Scoring breakdown:
 *   Email domain: +30 if sketchy, -10 if trusted
 *   Salary too high: +25
 *   Asks for money: +35
 *   Urgency checkbox: +15
 *   Wants personal info: +30
 *   Weak company name: +12
 *   NLP text score: up to +45
 *   Known scam phrase: +10
 * 
 * Final verdict:
 *   0-29 = GENUINE
 *   30-59 = SUSPICIOUS  
 *   60+ = FAKE
 */
public class VerificationEngine {
    
    private NlpSignalAnalyzer textAnalyzer;
    
    public VerificationEngine(NlpSignalAnalyzer analyzer) {
        this.textAnalyzer = analyzer;
    }
    
    public VerificationResult evaluate(Offer offer, boolean urgentFlag, 
                                       boolean personalInfoFlag, String role, String type) {
        int totalRisk = 0;
        List<String> reasons = new ArrayList<>();
        
        // check email domain first
        if(!RuleChecker.isValidEmail(offer.getEmail())) {
            totalRisk += 30;
            reasons.add("Email looks suspicious - free provider or blocked domain");
        } else if(RuleChecker.isTrustedDomain(offer.getEmail())) {
            totalRisk -= 10; // bonus for known companies
            reasons.add("Email from trusted company domain");
        } else {
            reasons.add("Email domain seems okay");
        }
        
        // salary check - different limits for internships vs jobs
        boolean isIntern = RuleChecker.isInternshipOffer(type, role);
        if(checkSalary(offer.getSalary(), isIntern)) {
            totalRisk += 25;
            reasons.add("Salary way too high for this role");
        }
        
        // registration fee = huge red flag
        if(offer.hasFee()) {
            totalRisk += 35;
            reasons.add("Asking for registration/security fee - major warning sign");
        }
        
        // user-selected flags
        if(urgentFlag) {
            totalRisk += 15;
            reasons.add("Uses pressure/urgency language");
        }
        
        if(personalInfoFlag) {
            totalRisk += 30;
            reasons.add("Wants Aadhaar/OTP/bank details upfront");
        }
        
        // company name check
        if(RuleChecker.hasWeakCompanyName(offer.getCompanyName())) {
            totalRisk += 12;
            reasons.add("Company name looks incomplete or fake");
        }
        
        // run text analysis
        NlpSignalAnalyzer.NlpResult textResult = textAnalyzer.analyze(offer.getDescription());
        int nlpRisk = textResult.getRisk();
        totalRisk += Math.round(nlpRisk * 0.45f); // NLP contributes 45%
        reasons.addAll(textResult.getNotes());
        
        // direct phrase match
        if(RuleChecker.hasSuspiciousWords(offer.getDescription())) {
            totalRisk += 10;
            reasons.add("Found known scam phrase in description");
        }
        
        // clamp to 0-100
        totalRisk = Math.max(0, Math.min(totalRisk, 100));
        
        return new VerificationResult(totalRisk, nlpRisk, getVerdict(totalRisk), reasons);
    }
    
    public static String getVerdict(int score) {
        if(score >= 60) return "FAKE";
        if(score >= 30) return "SUSPICIOUS";
        return "GENUINE";
    }
    
    private boolean checkSalary(double sal, boolean intern) {
        if(sal <= 0) return false;
        // internships: flag if over 1.5L/month, jobs: flag if over 10L/month
        return intern ? sal > 150000 : RuleChecker.isUnrealisticSalary(sal);
    }
}
