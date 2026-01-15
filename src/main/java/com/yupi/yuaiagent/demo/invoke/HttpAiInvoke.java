package com.yupi.yuaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpAiInvoke {
    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private static final String API_KEY = "sk-bff0430e210a4afe9c0c20e021a30193";  // Replace with your actual DASHSCOPE_API_KEY

    public static void main(String[] args) {
        try {
            HttpAiInvoke.invokeAi();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void invokeAi() throws IOException {
        // Create the URL object
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set the request method and headers
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);  // Enable output stream to send data

        // JSON data to send in the body of the request
        String jsonInputString = "{\n" +
                "    \"model\": \"qwen-plus\",\n" +
                "    \"input\":{\n" +
                "        \"messages\":[\n" +
                "            {\n" +
                "                \"role\": \"system\",\n" +
                "                \"content\": \"You are a helpful assistant.\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"role\": \"user\",\n" +
                "                \"content\": \"你是谁？\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    \"parameters\": {\n" +
                "        \"result_format\": \"message\"\n" +
                "    }\n" +
                "}";

        // Send the request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);  // Write the input JSON to the output stream
        }

        // Get the response from the server
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        // Read the response
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            // Print the response content
            System.out.println("Response: " + content.toString());
        }
    }
}
