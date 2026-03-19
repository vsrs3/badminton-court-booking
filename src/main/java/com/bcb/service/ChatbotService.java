package com.bcb.service;

import java.util.List;
import java.util.Map;

/**
 * Service interface for AI Chatbot operations.
 * Handles conversation with OpenAI and facility recommendation from DB.
 */
public interface ChatbotService {

    /**
     * Process a user message and return AI response with optional facility recommendations.
     *
     * @param userMessage      The user's chat message
     * @param conversationHistory Previous messages for context (list of maps with "role" and "content")
     * @return ChatbotResponse containing AI text and optional facility cards
     */
    ChatbotResponse processMessage(String userMessage, List<Map<String, String>> conversationHistory);

    /**
     * Response object from chatbot processing
     */
    class ChatbotResponse {
        private String message;
        private List<FacilityCard> facilities;
        private boolean hasError;

        public ChatbotResponse() {}

        public ChatbotResponse(String message, List<FacilityCard> facilities) {
            this.message = message;
            this.facilities = facilities;
            this.hasError = false;
        }

        public static ChatbotResponse error(String errorMessage) {
            ChatbotResponse r = new ChatbotResponse();
            r.message = errorMessage;
            r.facilities = List.of();
            r.hasError = true;
            return r;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public List<FacilityCard> getFacilities() { return facilities; }
        public void setFacilities(List<FacilityCard> facilities) { this.facilities = facilities; }
        public boolean isHasError() { return hasError; }
        public void setHasError(boolean hasError) { this.hasError = hasError; }
    }

    /**
     * Lightweight facility card data for chatbot display
     */
    class FacilityCard {
        private int facilityId;
        private String name;
        private String location;
        private String imageUrl;
        private String priceRange;
        private double rating;
        private String openTime;

        public int getFacilityId() { return facilityId; }
        public void setFacilityId(int facilityId) { this.facilityId = facilityId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getPriceRange() { return priceRange; }
        public void setPriceRange(String priceRange) { this.priceRange = priceRange; }
        public double getRating() { return rating; }
        public void setRating(double rating) { this.rating = rating; }
        public String getOpenTime() { return openTime; }
        public void setOpenTime(String openTime) { this.openTime = openTime; }
    }
}
