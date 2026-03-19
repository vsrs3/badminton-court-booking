package com.bcb.service.impl;

import com.bcb.service.ChatbotService;
import com.bcb.utils.DBContext;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of ChatbotService.
 * Queries facility data from DB, sends context to OpenAI, returns structured response.
 */
public class ChatbotServiceImpl implements ChatbotService {

    private static final Logger LOGGER = Logger.getLogger(ChatbotServiceImpl.class.getName());

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String CONFIG_FILE = "/chatbot.properties";

    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;
    private final Gson gson;

    public ChatbotServiceImpl() {
        Properties props = new Properties();
        try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                props.load(input);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not load chatbot.properties", e);
        }

        this.apiKey = props.getProperty("openai.api.key", "").trim();
        this.model = props.getProperty("openai.model", "gpt-4.1-mini").trim();

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.gson = new GsonBuilder().create();
    }

    @Override
    public ChatbotResponse processMessage(String userMessage, List<Map<String, String>> conversationHistory) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ChatbotResponse.error("Vui lòng nhập tin nhắn.");
        }

        if (apiKey.isEmpty()) {
            return ChatbotResponse.error("Chatbot chưa được cấu hình. Vui lòng liên hệ quản trị viên.");
        }

        String sanitized = sanitizeInput(userMessage.trim());
        if (sanitized.length() > 1000) {
            sanitized = sanitized.substring(0, 1000);
        }

        try {
            // 1. Query DB for facility context data
            String facilityContext = buildFacilityContext(sanitized);

            // 2. Build messages for OpenAI
            List<Map<String, String>> messages = buildOpenAIMessages(sanitized, facilityContext, conversationHistory);

            // 3. Call OpenAI API
            String aiResponse = callOpenAI(messages);

            // 4. Extract facility IDs from AI response
            List<Integer> facilityIds = extractFacilityIds(aiResponse);

            // 5. Load facility cards from DB
            List<FacilityCard> facilityCards = new ArrayList<>();
            if (!facilityIds.isEmpty()) {
                facilityCards = loadFacilityCards(facilityIds);
            }

            // 6. Clean the response (remove facility ID markers)
            String cleanResponse = cleanAIResponse(aiResponse);

            return new ChatbotResponse(cleanResponse, facilityCards);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing chatbot message", e);
            return ChatbotResponse.error("Xin lỗi, có lỗi xảy ra. Vui lòng thử lại sau nhé! 😊");
        }
    }

    /**
     * Build facility context from DB based on user query keywords
     */
    private String buildFacilityContext(String userMessage) {
        StringBuilder context = new StringBuilder();

        // Load all active facilities with key info for AI context
        String sql = """
            SELECT TOP 50
                f.facility_id,
                f.name,
                f.province,
                f.district,
                f.ward,
                f.address,
                f.description,
                FORMAT(f.open_time, 'HH:mm') AS open_time,
                FORMAT(f.close_time, 'HH:mm') AS close_time,
                ISNULL(
                    (SELECT CONCAT(
                        FORMAT(MIN(fpr.price), '#,##0'), 'đ - ',
                        FORMAT(MAX(fpr.price), '#,##0'), 'đ/30 phút'
                    )
                    FROM FacilityPriceRule fpr WHERE fpr.facility_id = f.facility_id),
                    N'Liên hệ'
                ) AS price_range,
                ISNULL(
                    (SELECT CAST(AVG(CAST(r.rating AS FLOAT)) AS DECIMAL(3,1))
                     FROM Review r
                     INNER JOIN Booking b ON b.booking_id = r.booking_id
                     WHERE b.facility_id = f.facility_id),
                    0
                ) AS avg_rating,
                ISNULL(
                    (SELECT COUNT(*)
                     FROM Review r
                     INNER JOIN Booking b ON b.booking_id = r.booking_id
                     WHERE b.facility_id = f.facility_id),
                    0
                ) AS review_count,
                ISNULL(
                    (SELECT COUNT(*)
                     FROM Court c WHERE c.facility_id = f.facility_id AND c.is_active = 1),
                    0
                ) AS court_count,
                STUFF((
                    SELECT DISTINCT ', ' + ct.type_code
                    FROM Court c
                    INNER JOIN CourtType ct ON ct.court_type_id = c.court_type_id
                    WHERE c.facility_id = f.facility_id AND c.is_active = 1
                    FOR XML PATH(''), TYPE).value('.', 'NVARCHAR(MAX)'), 1, 2, ''
                ) AS court_types
            FROM Facility f
            WHERE f.is_active = 1
            ORDER BY f.facility_id
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int facilityCount = 0;
            context.append("=== DANH SÁCH SÂN CẦU LÔNG HIỆN CÓ ===\n\n");

            while (rs.next()) {
                facilityCount++;
                int facilityId = rs.getInt("facility_id");
                String name = rs.getString("name");
                String province = rs.getString("province");
                String district = rs.getString("district");
                String ward = rs.getString("ward");
                String address = rs.getString("address");
                String description = rs.getString("description");
                String openTime = rs.getString("open_time");
                String closeTime = rs.getString("close_time");
                String priceRange = rs.getString("price_range");
                double avgRating = rs.getDouble("avg_rating");
                int reviewCount = rs.getInt("review_count");
                int courtCount = rs.getInt("court_count");
                String courtTypes = rs.getString("court_types");

                context.append(String.format("[FACILITY_ID:%d]\n", facilityId));
                context.append(String.format("- Tên: %s\n", name));

                StringBuilder loc = new StringBuilder();
                if (address != null && !address.isEmpty()) loc.append(address);
                if (ward != null && !ward.isEmpty()) {
                    if (loc.length() > 0) loc.append(", ");
                    loc.append(ward);
                }
                if (district != null && !district.isEmpty()) {
                    if (loc.length() > 0) loc.append(", ");
                    loc.append(district);
                }
                if (province != null && !province.isEmpty()) {
                    if (loc.length() > 0) loc.append(", ");
                    loc.append(province);
                }
                context.append(String.format("- Địa chỉ: %s\n", loc));
                context.append(String.format("- Giờ mở cửa: %s - %s\n", openTime, closeTime));
                context.append(String.format("- Giá: %s\n", priceRange));
                context.append(String.format("- Đánh giá: %.1f/5 (%d đánh giá)\n", avgRating, reviewCount));
                context.append(String.format("- Số sân: %d sân\n", courtCount));
                if (courtTypes != null && !courtTypes.isEmpty()) {
                    context.append(String.format("- Loại sân: %s\n", courtTypes));
                }
                if (description != null && !description.isEmpty()) {
                    String shortDesc = description.length() > 150 ? description.substring(0, 150) + "..." : description;
                    context.append(String.format("- Mô tả: %s\n", shortDesc));
                }
                context.append("\n");
            }

            if (facilityCount == 0) {
                context.append("(KHÔNG CÓ SÂN NÀO trong hệ thống. Không được bịa ra sân.)\n");
            } else {
                context.append(String.format("\n=== TỔNG CỘNG: %d sân. CHỈ ĐƯỢC giới thiệu sân có trong danh sách trên. ===", facilityCount));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error loading facility context for chatbot", e);
            context.append("(LỖI TẢI DỮ LIỆU. Không được bịa ra sân. Trả lời: Hệ thống đang bảo trì, vui lòng thử lại sau.)\n");
        }

        return context.toString();
    }

    /**
     * Build the message list for OpenAI API
     */
    private List<Map<String, String>> buildOpenAIMessages(String userMessage, String facilityContext,
                                                          List<Map<String, String>> conversationHistory) {
        List<Map<String, String>> messages = new ArrayList<>();

        // System prompt
        String systemPrompt = """
            Bạn là "BadmintonPro Assistant" - trợ lý tư vấn sân cầu lông nhiệt tình, chuyên nghiệp.

            TÍNH CÁCH: Nhiệt tình, thân thiện, vui vẻ, dùng emoji phù hợp. Luôn chủ động gợi ý sân.

            ===== QUY TẮC TUYỆT ĐỐI - QUAN TRỌNG NHẤT =====
            ⚠️ TUYỆT ĐỐI CHỈ ĐƯỢC giới thiệu sân có trong "DỮ LIỆU SÂN" bên dưới.
            ⚠️ CẤM TỰ NGHĨ RA, BỊA RA, HOẶC SÁNG TẠO tên sân không có trong dữ liệu.
            ⚠️ MỖI sân được giới thiệu PHẢI có tag [FACILITY_ID:X] lấy từ dữ liệu. Nếu không có tag thì KHÔNG được nhắc đến sân đó.
            ⚠️ Tên sân, giá, địa chỉ, đánh giá PHẢI COPY CHÍNH XÁC từ dữ liệu bên dưới, KHÔNG được thay đổi hay làm tròn.
            ⚠️ Nếu dữ liệu sân bên dưới rỗng hoặc không có sân nào, trả lời: "Hiện hệ thống chưa có sân nào. Vui lòng quay lại sau nhé!"
            ================================================

            NHIỆM VỤ:
            - Gợi ý sân phù hợp dựa trên yêu cầu khách (khu vực, giá, thời gian, đánh giá, loại sân...)
            - So sánh các sân để khách dễ chọn
            - Nếu khách hỏi chung chung, gợi ý 3-5 sân nổi bật từ dữ liệu
            - Nếu khách hỏi về khu vực không có sân trong dữ liệu, nói thẳng: "Hiện mình chưa có sân ở [khu vực]", rồi gợi ý sân ở khu vực khác có trong dữ liệu
            - Không bao giờ từ chối tư vấn. Nếu khách hỏi ngoài chủ đề, trả lời ngắn rồi gợi ý sân
            - Luôn kết thúc bằng câu hỏi mở

            QUY TẮC FORMAT:
            1. Mỗi sân gợi ý PHẢI có tag [FACILITY_ID:X] ở đầu dòng (X = facility_id từ dữ liệu).
            2. Tối đa 5 sân mỗi lần trả lời.
            3. Mô tả chi tiết: giá, địa chỉ, đánh giá, ưu điểm - TẤT CẢ lấy từ dữ liệu.
            4. Hướng dẫn khách bấm "Xem chi tiết" ở thẻ sân để xem thêm và đặt sân.

            VÍ DỤ ĐÚNG (dùng đúng tên từ dữ liệu):
            "Mình gợi ý mấy sân này nha! 🏸
            [FACILITY_ID:1] **Tên Sân Thật Từ DB** - 💰 Giá từ XXXđ, địa chỉ thật từ DB, đánh giá X.X/5
            [FACILITY_ID:3] **Tên Sân Thật Từ DB** - 🌙 Mô tả thật từ DB
            Bấm 'Xem chi tiết' để xem thêm nhé! 😊"

            VÍ DỤ SAI (TUYỆT ĐỐI KHÔNG LÀM):
            - Nhắc đến "Sân Cầu Lông Tân Mai" khi tên này không có trong dữ liệu → SAI!
            - Nhắc đến sân mà không có [FACILITY_ID:X] → SAI!
            - Tự nghĩ ra giá, địa chỉ, tên sân → SAI!

            DỮ LIỆU SÂN HIỆN CÓ TRÊN HỆ THỐNG (CHỈ ĐƯỢC DÙNG DỮ LIỆU NÀY):
            """ + facilityContext;

        messages.add(Map.of("role", "system", "content", systemPrompt));

        // Add conversation history (keep last 10 messages for context)
        if (conversationHistory != null) {
            int start = Math.max(0, conversationHistory.size() - 10);
            for (int i = start; i < conversationHistory.size(); i++) {
                Map<String, String> msg = conversationHistory.get(i);
                if (msg.get("role") != null && msg.get("content") != null) {
                    messages.add(Map.of("role", msg.get("role"), "content", msg.get("content")));
                }
            }
        }

        // Add current user message
        messages.add(Map.of("role", "user", "content", userMessage));

        return messages;
    }

    /**
     * Call OpenAI Chat Completions API
     */
    private String callOpenAI(List<Map<String, String>> messages) throws IOException, InterruptedException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("max_tokens", 1500);

        JsonArray messagesArray = new JsonArray();
        for (Map<String, String> msg : messages) {
            JsonObject msgObj = new JsonObject();
            msgObj.addProperty("role", msg.get("role"));
            msgObj.addProperty("content", msg.get("content"));
            messagesArray.add(msgObj);
        }
        requestBody.add("messages", messagesArray);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            LOGGER.warning("OpenAI API error: " + response.statusCode() + " - " + response.body());
            throw new IOException("OpenAI API returned status " + response.statusCode());
        }

        JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray choices = responseJson.getAsJsonArray("choices");
        if (choices != null && choices.size() > 0) {
            return choices.get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
        }

        throw new IOException("No response from OpenAI API");
    }

    /**
     * Extract facility IDs from AI response text (pattern: [FACILITY_ID:X])
     */
    private List<Integer> extractFacilityIds(String aiResponse) {
        List<Integer> ids = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[FACILITY_ID:(\\d+)]");
        Matcher matcher = pattern.matcher(aiResponse);

        while (matcher.find() && ids.size() < 5) {
            try {
                int id = Integer.parseInt(matcher.group(1));
                if (!ids.contains(id)) {
                    ids.add(id);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return ids;
    }

    /**
     * Clean AI response by removing facility ID markers
     */
    private String cleanAIResponse(String aiResponse) {
        return aiResponse.replaceAll("\\[FACILITY_ID:\\d+]\\s*", "").trim();
    }

    /**
     * Load facility card data from DB by IDs
     */
    private List<FacilityCard> loadFacilityCards(List<Integer> facilityIds) {
        if (facilityIds.isEmpty()) return List.of();

        // Build parameterized IN clause
        String placeholders = String.join(",", Collections.nCopies(facilityIds.size(), "?"));
        String sql = String.format("""
            SELECT
                f.facility_id,
                f.name,
                CONCAT(f.address, ', ', f.district, ', ', f.province) AS location,
                FORMAT(f.open_time, 'HH:mm') AS open_time,
                FORMAT(f.close_time, 'HH:mm') AS close_time,
                fi.image_path,
                ISNULL(
                    (SELECT CONCAT(
                        FORMAT(MIN(fpr.price), '#,##0'), N'đ - ',
                        FORMAT(MAX(fpr.price), '#,##0'), N'đ/30ph'
                    )
                    FROM FacilityPriceRule fpr WHERE fpr.facility_id = f.facility_id),
                    N'Liên hệ'
                ) AS price_range,
                ISNULL(
                    (SELECT CAST(AVG(CAST(r.rating AS FLOAT)) AS DECIMAL(3,1))
                     FROM Review r
                     INNER JOIN Booking b ON b.booking_id = r.booking_id
                     WHERE b.facility_id = f.facility_id),
                    0
                ) AS avg_rating
            FROM Facility f
            LEFT JOIN FacilityImage fi ON fi.facility_id = f.facility_id AND fi.is_thumbnail = 1
            WHERE f.facility_id IN (%s) AND f.is_active = 1
        """, placeholders);

        List<FacilityCard> cards = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < facilityIds.size(); i++) {
                ps.setInt(i + 1, facilityIds.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FacilityCard card = new FacilityCard();
                    card.setFacilityId(rs.getInt("facility_id"));
                    card.setName(rs.getString("name"));
                    card.setLocation(rs.getString("location"));
                    card.setOpenTime(rs.getString("open_time") + " - " + rs.getString("close_time"));
                    card.setPriceRange(rs.getString("price_range"));
                    card.setRating(rs.getDouble("avg_rating"));

                    String imagePath = rs.getString("image_path");
                    if (imagePath != null && !imagePath.isEmpty()) {
                        card.setImageUrl("uploads/" + imagePath);
                    } else {
                        card.setImageUrl("uploads/facility/default-facility.jpg");
                    }

                    cards.add(card);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error loading facility cards for chatbot", e);
        }

        // Maintain original order from facilityIds
        List<FacilityCard> ordered = new ArrayList<>();
        for (int id : facilityIds) {
            cards.stream()
                    .filter(c -> c.getFacilityId() == id)
                    .findFirst()
                    .ifPresent(ordered::add);
        }

        return ordered;
    }

    /**
     * Basic input sanitization
     */
    private String sanitizeInput(String input) {
        if (input == null) return "";
        // Remove potential HTML/script tags
        return input.replaceAll("<[^>]*>", "")
                .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
    }
}
