package com.guvi.honey_pot.service;

import com.guvi.honey_pot.model.ExtractedIntelligence;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ScamDetector {

    private static final List<String> SCAM_KEYWORDS = Arrays.asList(
            "verify", "urgent", "suspended", "blocked", "confirm", "immediately",
            "account", "expire", "click here", "limited time", "congratulations",
            "won", "prize", "refund", "tax", "customs", "kbc", "lottery",
            "update kyc", "pan card", "aadhaar", "upi", "bank details",
            "otp", "cvv", "card number", "transfer money", "pay now"
    );

    private static final Pattern UPI_PATTERN = Pattern.compile(
            "\\b[a-zA-Z0-9._-]+@[a-zA-Z]+\\b"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "\\+?\\d[\\d\\s-]{8,}\\d"
    );

    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?://[^\\s]+|www\\.[^\\s]+"
    );

    private static final Pattern BANK_ACCOUNT_PATTERN = Pattern.compile(
            "\\b\\d{9,18}\\b"
    );

    public boolean detectScamIntent(String text, List<String> conversationHistory) {
        String lowerText = text.toLowerCase();

        // Check for scam keywords
        long keywordCount = SCAM_KEYWORDS.stream()
                .filter(lowerText::contains)
                .count();

        // Check for urgency patterns
        boolean hasUrgency = lowerText.contains("urgent") ||
                lowerText.contains("immediately") ||
                lowerText.contains("now") ||
                lowerText.contains("today");

        // Check for financial requests
        boolean hasFinancialRequest = lowerText.contains("account") ||
                lowerText.contains("upi") ||
                lowerText.contains("bank") ||
                lowerText.contains("payment");

        // Check for verification requests
        boolean hasVerificationRequest = lowerText.contains("verify") ||
                lowerText.contains("confirm") ||
                lowerText.contains("update");

        // Scam detected if multiple indicators present
        return (keywordCount >= 2) ||
                (hasUrgency && hasFinancialRequest) ||
                (hasVerificationRequest && hasFinancialRequest);
    }

    public ExtractedIntelligence extractIntelligence(String text, ExtractedIntelligence existing) {
        // Extract UPI IDs
        var upiMatcher = UPI_PATTERN.matcher(text);
        while (upiMatcher.find()) {
            String upi = upiMatcher.group();
            if (!existing.getUpiIds().contains(upi)) {
                existing.getUpiIds().add(upi);
            }
        }

        // Extract phone numbers
        var phoneMatcher = PHONE_PATTERN.matcher(text);
        while (phoneMatcher.find()) {
            String phone = phoneMatcher.group().replaceAll("[\\s-]", "");
            if (phone.length() >= 10 && !existing.getPhoneNumbers().contains(phone)) {
                existing.getPhoneNumbers().add(phone);
            }
        }

        // Extract URLs/phishing links
        var urlMatcher = URL_PATTERN.matcher(text);
        while (urlMatcher.find()) {
            String url = urlMatcher.group();
            if (!existing.getPhishingLinks().contains(url)) {
                existing.getPhishingLinks().add(url);
            }
        }

        // Extract potential bank account numbers
        var bankMatcher = BANK_ACCOUNT_PATTERN.matcher(text);
        while (bankMatcher.find()) {
            String account = bankMatcher.group();
            if (account.length() >= 9 && !existing.getBankAccounts().contains(account)) {
                existing.getBankAccounts().add(account);
            }
        }

        // Extract suspicious keywords
        String lowerText = text.toLowerCase();
        SCAM_KEYWORDS.stream()
                .filter(lowerText::contains)
                .filter(keyword -> !existing.getSuspiciousKeywords().contains(keyword))
                .forEach(existing.getSuspiciousKeywords()::add);

        return existing;
    }
}