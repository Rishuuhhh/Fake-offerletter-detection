package store;

import java.util.ArrayList;
import java.util.List;

// provides seed fake offer descriptions
public class SeedLoader {

    private static final String[][] SEED_OFFERS = {
        {"QuickHire Solutions",
         "Congratulations! You have been selected. Pay a registration fee of Rs.2000 to confirm your seat. Send money via UPI immediately. Limited seats available. Act now!"},
        {"Global Recruiters Pvt Ltd",
         "You are selected for the position. Please pay a security deposit of Rs.5000 to process your joining kit. Bank details will be shared. Urgent response required."},
        {"FastTrack Careers",
         "Instant joining offer! Pay processing fee of Rs.1500 to receive your offer letter. Wire transfer accepted. Guaranteed placement after payment."},
        {"EasyJobs India",
         "Work from home opportunity. Pay training fee of Rs.3000 to get started. No interview required. Earn Rs.50000 per month guaranteed. Pay now to confirm."},
        {"TopRecruit Agency",
         "Selected candidate must pay kit charges of Rs.2500 before joining. Send payment via western union or money gram. Immediate response needed."},
        {"HR Connect Services",
         "Please send your Aadhaar number, PAN card, and bank account details immediately to process your appointment letter. OTP verification required. Urgent."},
        {"National Hiring Bureau",
         "To complete your selection process, submit your Aadhaar number and bank details within 24 hours. Id proof required immediately for background check."},
        {"Premier Staffing Co",
         "Kindly share your OTP verification code and bank account number to activate your employment profile. Sensitive information needed urgently."},
        {"MegaEarnings Corp",
         "Earn Rs.1,50,000 per month working from home. No experience required. Guaranteed salary of Rs.1.5 lakh monthly. 100% job guarantee. Apply immediately."},
        {"WealthyWork India",
         "Get paid Rs.2,00,000 monthly for part time work. Income of Rs.2 lakh guaranteed. Easy money opportunity. No qualification needed. Earn from home today."},
        {"RichQuick Enterprises",
         "Stipend of Rs.80,000 per month for internship. Guaranteed placement with salary of Rs.1,20,000 after 3 months. No interview. Instant joining available."},
        {"UrgentHire Now",
         "URGENT HIRING! Last chance to apply. Deadline today. Hurry — only 2 seats left. Immediate response required. Do not miss this opportunity. Act now!"},
        {"FlashRecruit Ltd",
         "Urgent requirement for 10 positions. Apply immediately. Deadline is today. Hurry up! Selected candidates must respond within 2 hours or lose the offer."},
        {"InstantJob Portal",
         "Limited seats available. Immediate response needed. Today is the last day. Urgency: respond now or your application will be cancelled. Hurry!"},
        {"Amazon HR Department",
         "This is amazon-hr.com official recruitment. You have been selected by amazon-careers-apply.net. Pay registration fee to confirm. Send Aadhaar details urgently."},
        {"Google Careers India",
         "Official offer from google.careers-apply.net. Congratulations on your selection. Pay processing fee of Rs.3000 to google-hr.com to activate your offer letter."},
        {"TCS Recruitment Cell",
         "Selected by tcs-hiring.net for immediate joining. Pay security deposit to tcs-careers.org. Send bank details and OTP to confirm your appointment."},
        {"Infosys Talent Acquisition",
         "Offer from infosys-jobs.com. Pay joining fee of Rs.4000 to infosys-hr.net. No interview required. Guaranteed placement. Urgent response needed today."},
        {"Dream Career Hub",
         "You are selected! Pay advance payment of Rs.2000 via UPI. No experience required. Work from home. Earn Rs.60,000 monthly. 100% job guarantee. Act now!"},
        {"National Employment Bureau",
         "Urgent hiring! Pay refundable deposit of Rs.1000. Send Aadhaar number and bank details. Guaranteed salary of Rs.75,000. Limited seats. Deadline today. Hurry!"},
        {"SwiftJobs Consultancy",
         "Congratulations on your selection. Pay registration fee immediately. No interview needed. Earn from home. Send OTP and bank account details to confirm joining."},
        {"BrightFuture Placements",
         "Selected for immediate joining. Pay training fee of Rs.1800. Wire transfer or UPI accepted. Guaranteed placement. Id proof required immediately. Act now!"},
        {"Global TechVentures Inc",
         "You have been SELECTED for an exciting opportunity. 100% genuine work from home. No interview required — instant hire! Salary Rs.5,00,000 per month plus UNLIMITED Incentives. Refer and earn — 5 referrals. Passive income from downline. Pyramid network grows. Become rich, be your own boss. Respond immediately. Limited seats. Act now before someone else takes your spot!"},
        {"EasyIncome Network",
         "Join our network marketing and direct selling programme. Unlimited earning potential. No experience needed. Financial freedom guaranteed. Pay one-time registration fee of Rs.2499 as refundable security deposit. Send via UPI / Google Pay / PhonePe to 9876543210@paytm immediately. Forward payment screenshot to WhatsApp. Do NOT delay — transfer money today to avoid cancellation."},
        {"WorkFromHome Ventures",
         "Send Aadhaar card front and back, PAN card, passport copy, bank account number, IFSC code, credit card details and debit card details for verification purposes immediately on WhatsApp. This is a 100% genuine company. Passive income model through network marketing has helped many people become rich. Join our pyramid of success today! Last chance, limited time offer, don't wait!"}
    };

    public SeedLoader() {}

    public List<String> loadSeedDescriptions() {
        List<String> descriptions = new ArrayList<>(SEED_OFFERS.length);
        for (String[] offer : SEED_OFFERS) {
            descriptions.add(offer[1]);
        }
        return descriptions;
    }
}
