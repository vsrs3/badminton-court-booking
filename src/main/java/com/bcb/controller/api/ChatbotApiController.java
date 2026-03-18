package com.bcb.controller.api;

import com.bcb.service.ChatbotService;
import com.bcb.service.ChatbotService.ChatbotResponse;
import com.bcb.service.impl.ChatbotServiceImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST API Controller for AI Chatbot.
 * POST /api/chatbot - Send a message and get AI response with optional facility cards.
 */
@WebServlet(name = "ChatbotApiController", urlPatterns = {"/api/chatbot"})
public class ChatbotApiController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ChatbotApiController.class.getName());

    private ChatbotService chatbotService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        super.init();
        this.chatbotService = new ChatbotServiceImpl();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Read request body
            String body = readRequestBody(request);
            if (body == null || body.trim().isEmpty()) {
                sendErrorResponse(response, 400, "Request body is required");
                return;
            }

            // Parse JSON body
            JsonObject jsonBody;
            try {
                jsonBody = JsonParser.parseString(body).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                sendErrorResponse(response, 400, "Invalid JSON format");
                return;
            }

            // Extract and validate user message
            String userMessage = jsonBody.has("message") ? jsonBody.get("message").getAsString() : null;
            if (userMessage == null || userMessage.trim().isEmpty()) {
                sendErrorResponse(response, 400, "Message is required");
                return;
            }

            if (userMessage.trim().length() > 1000) {
                sendErrorResponse(response, 400, "Message too long (max 1000 characters)");
                return;
            }

            // Extract conversation history
            List<Map<String, String>> conversationHistory = new ArrayList<>();
            if (jsonBody.has("history") && jsonBody.get("history").isJsonArray()) {
                JsonArray historyArr = jsonBody.getAsJsonArray("history");
                for (int i = 0; i < historyArr.size(); i++) {
                    JsonObject msgObj = historyArr.get(i).getAsJsonObject();
                    String role = msgObj.has("role") ? msgObj.get("role").getAsString() : null;
                    String content = msgObj.has("content") ? msgObj.get("content").getAsString() : null;
                    if (role != null && content != null) {
                        Map<String, String> msg = new HashMap<>();
                        msg.put("role", role);
                        msg.put("content", content);
                        conversationHistory.add(msg);
                    }
                }
            }

            // Process message through chatbot service
            ChatbotResponse chatResponse = chatbotService.processMessage(userMessage.trim(), conversationHistory);

            // Build response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", !chatResponse.isHasError());
            responseData.put("message", chatResponse.getMessage());
            responseData.put("facilities", chatResponse.getFacilities());

            response.getWriter().write(gson.toJson(responseData));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in chatbot API", e);
            sendErrorResponse(response, 500, "Internal server error");
        }
    }

    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message)
            throws IOException {
        response.setStatus(statusCode);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("facilities", List.of());
        response.getWriter().write(gson.toJson(errorResponse));
    }
}
