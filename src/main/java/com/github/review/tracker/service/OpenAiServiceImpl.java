package com.github.review.tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

public class OpenAiServiceImpl implements OpenAiService{
    @Value("${openai.api.key}")
    private String apiKey;

    // Best practice: Inject RestTemplate as a bean rather than direct instantiation.
    // Ensure you have a @Bean for RestTemplate in your main application class.
    private final RestTemplate restTemplate= new RestTemplate();
    private final ObjectMapper objectMapper =new ObjectMapper(); // For robust JSON handling


    public String getCodeSuggestions(String commentText) {
        // --- CHANGE THE URL ---
        String url = "https://openrouter.ai/api/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // OpenRouter sometimes recommends adding an HTTP-Referer header for tracking
        // headers.set("HTTP-Referer", "YOUR_APP_URL"); // Optional, but good practice if deployed

        String prompt = "Suggest code improvements or snippets based on this review comment: " + commentText;

        ObjectNode requestBody = objectMapper.createObjectNode();
        // --- CHANGE THE MODEL NAME ---
        requestBody.put("model", "mistralai/mistral-7b-instruct:free"); // Example: Use a free Mistral model on OpenRouter
        // Or "google/gemma-7b-it" for Gemma
        // Or "meta-llama/llama-3-8b-instruct" for Llama 3 (check if free tier applies)

        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", prompt); // ObjectMapper handles JSON escaping for 'prompt'
        messages.add(userMessage);
        requestBody.set("messages", messages);

        requestBody.put("max_tokens", 10000);
        requestBody.put("temperature", 0.7); // Add temperature for better control (optional but recommended)

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        try {
            // Using JsonNode.class for robust response parsing
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, entity, JsonNode.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Robustly extract content using JsonNode
                JsonNode contentNode = response.getBody()
                        .path("choices")
                        .path(0)
                        .path("message")
                        .path("content");

                if (contentNode.isTextual()) {
                    return contentNode.asText();
                } else {
                    System.err.println("OpenAI Response: Content node not found or not textual: " + response.getBody().toPrettyString());
                    return "Error: Unexpected OpenAI response format.";
                }
            } else {
                System.err.println("OpenAI API call failed with status: " + response.getStatusCode() + " body: " + (response.hasBody() ? response.getBody().toPrettyString() : "No body"));
                return "Error: OpenAI API call failed.";
            }
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP Client Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return "Error: OpenAI API client error. Status: " + e.getStatusCode() + ". Message: " + e.getResponseBodyAsString();
        } catch (ResourceAccessException e) {
            System.err.println("Network/Connection Error: " + e.getMessage());
            return "Error: Could not connect to OpenAI API.";
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
            return "Error: An exception occurred.";
        }
    }
}
