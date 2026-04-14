package com.ptit.socialchat.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Service gọi AI Moderation API (Python FastAPI) để kiểm duyệt nội dung.
 *
 * Response từ AI Service:
 * {
 *     "label": "CLEAN" | "OFFENSIVE" | "HATE",
 *     "label_id": 0 | 1 | 2,
 *     "confidence": 0.9876,
 *     "probabilities": { "CLEAN": ..., "OFFENSIVE": ..., "HATE": ... },
 *     "is_toxic": false | true
 * }
 */
public class ModerationService {

    private static final int TIMEOUT_MS = 10000; // 10 giây timeout
    private final Gson gson = new Gson();

    /**
     * Kết quả kiểm duyệt từ AI.
     */
    public static class ModerationResult {
        private String label;       // CLEAN, OFFENSIVE, HATE
        private int labelId;        // 0, 1, 2
        private double confidence;  // 0.0 - 1.0
        private boolean toxic;      // true nếu OFFENSIVE hoặc HATE
        private boolean success;    // true nếu gọi API thành công
        private String errorMessage;

        // === Getters & Setters ===
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public int getLabelId() { return labelId; }
        public void setLabelId(int labelId) { this.labelId = labelId; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        public boolean isToxic() { return toxic; }
        public void setToxic(boolean toxic) { this.toxic = toxic; }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    /**
     * Gọi AI Service để kiểm duyệt nội dung.
     *
     * @param text         Nội dung cần kiểm duyệt
     * @param aiServiceUrl URL của AI service (vd: "http://localhost:8000")
     * @return ModerationResult chứa kết quả. Kiểm tra result.isSuccess() trước khi dùng.
     */
    public ModerationResult moderate(String text, String aiServiceUrl) {
        ModerationResult result = new ModerationResult();

        if (text == null || text.trim().isEmpty()) {
            result.setSuccess(true);
            result.setLabel("CLEAN");
            result.setLabelId(0);
            result.setConfidence(1.0);
            result.setToxic(false);
            return result;
        }

        HttpURLConnection conn = null;
        try {
            URL url = new URL(aiServiceUrl + "/api/moderate");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);

            // Build JSON body - escape text properly
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("text", text.trim());
            String jsonBody = gson.toJson(requestBody);

            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                // Đọc response
                StringBuilder responseStr = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        responseStr.append(line);
                    }
                }

                // Parse JSON response
                JsonObject json = gson.fromJson(responseStr.toString(), JsonObject.class);
                result.setLabel(json.get("label").getAsString());
                result.setLabelId(json.get("label_id").getAsInt());
                result.setConfidence(json.get("confidence").getAsDouble());
                result.setToxic(json.get("is_toxic").getAsBoolean());
                result.setSuccess(true);

                System.out.println("[ModerationService] AI result: label=" + result.getLabel()
                        + ", confidence=" + result.getConfidence()
                        + ", toxic=" + result.isToxic());
            } else {
                // Lỗi HTTP
                result.setSuccess(false);
                result.setErrorMessage("AI Service trả về HTTP " + responseCode);
                System.err.println("[ModerationService] HTTP error: " + responseCode);
            }

        } catch (java.net.ConnectException e) {
            result.setSuccess(false);
            result.setErrorMessage("Không thể kết nối đến AI Service tại " + aiServiceUrl);
            System.err.println("[ModerationService] Connection refused: " + aiServiceUrl);
        } catch (java.net.SocketTimeoutException e) {
            result.setSuccess(false);
            result.setErrorMessage("AI Service timeout sau " + TIMEOUT_MS + "ms");
            System.err.println("[ModerationService] Timeout calling AI service");
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage("Lỗi gọi AI Service: " + e.getMessage());
            System.err.println("[ModerationService] Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return result;
    }

    /**
     * Kiểm tra AI service có đang chạy không.
     */
    public boolean isServiceAvailable(String aiServiceUrl) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(aiServiceUrl + "/health");
            System.out.println("[ModerationService] Checking AI service at: " + url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            int code = conn.getResponseCode();
            System.out.println("[ModerationService] AI service responded with HTTP " + code);
            return code == 200;
        } catch (java.net.ConnectException e) {
            System.err.println("[ModerationService] AI service check FAILED - Connection refused at " + aiServiceUrl);
            return false;
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("[ModerationService] AI service check FAILED - Timeout at " + aiServiceUrl);
            return false;
        } catch (Exception e) {
            System.err.println("[ModerationService] AI service check FAILED - " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
