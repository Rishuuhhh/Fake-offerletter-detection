package store;

import java.io.*;
import java.nio.charset.StandardCharsets;

// creates default keyword/domain files on first run
public class DataInitializer {

    private static final String FAKE_KEYWORDS = String.join("\n",
        "type,phrase",
        "high,pay registration fee",
        "high,security deposit",
        "high,processing fee",
        "high,training fee",
        "high,kit charges",
        "high,joining fee",
        "high,advance payment",
        "high,refundable deposit",
        "high,100 job guarantee",
        "high,guaranteed placement",
        "high,no interview required",
        "high,instant joining",
        "high,send aadhaar",
        "high,aadhaar number",
        "high,aadhaar card",
        "high,bank account details",
        "high,bank details",
        "high,id proof",
        "high,required fees",
        "high,required fee",
        "high,sensitive information",
        "high,share your otp",
        "high,otp verification",
        "high,whatsapp interview",
        "high,network marketing",
        "high,refer and earn",
        "high,passive income",
        "high,direct selling",
        "medium,work from home",
        "medium,no experience required",
        "medium,guaranteed salary",
        "medium,earn from home",
        "medium,easy money",
        "medium,limited seats",
        "medium,western union",
        "medium,money gram",
        "medium,wire transfer",
        "medium,laptop charges",
        "medium,refundable security",
        "rush,act now",
        "rush,urgent hiring",
        "rush,hurry",
        "rush,deadline today",
        "rush,immediate response",
        "rush,last chance",
        "rush,do not delay",
        "rush,limited time offer",
        "rush,apply immediately",
        "rush,today is the last day",
        "rush,urgent",
        "payment,send via upi",
        "payment,via upi",
        "payment,send money",
        "payment,google pay",
        "payment,phonepe",
        "payment,paytm",
        "payment,upi",
        "payment,payment screenshot",
        "payment,transfer money"
    );

    private static final String GENUINE_KEYWORDS = String.join("\n",
        "phrase",
        "background verification",
        "offer letter on company letterhead",
        "probation period",
        "hr department",
        "appointment letter",
        "ctc breakdown",
        "joining date",
        "employee benefits",
        "leave policy",
        "notice period"
    );

    private static final String TRUSTED_DOMAINS = String.join("\n",
        "domain",
        "infosys.com",
        "wipro.com",
        "tcs.com",
        "hcl.com",
        "cognizant.com",
        "accenture.com",
        "amazon.com",
        "google.com",
        "microsoft.com",
        "flipkart.com",
        "hdfc.com",
        "icicibank.com"
    );

    private static final String BLOCKED_DOMAINS = String.join("\n",
        "domain",
        "gmail.com",
        "yahoo.com",
        "outlook.com",
        "hotmail.com",
        "protonmail.com"
    );

    public static void ensureDefaults() {
        new File("data").mkdirs();
        writeIfMissing("data/fake_keywords.csv", FAKE_KEYWORDS);
        writeIfMissing("data/genuine_keywords.csv", GENUINE_KEYWORDS);
        writeIfMissing("data/trusted_domains.csv", TRUSTED_DOMAINS);
        writeIfMissing("data/blocked_domains.csv", BLOCKED_DOMAINS);
    }

    private static void writeIfMissing(String path, String content) {
        File file = new File(path);
        if (file.exists() && file.length() > 0) return;
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write(content);
            w.write("\n");
        } catch (IOException e) {
            System.err.println("can't create " + path + ": " + e.getMessage());
        }
    }
}
