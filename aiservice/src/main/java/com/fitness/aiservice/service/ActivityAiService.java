package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.models.Activity;
import com.fitness.aiservice.models.Recommendation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class ActivityAiService {

    private final GeminiService geminiService;

    public Recommendation generateRecommendation(Activity activity) {
        String prompt = createPromptForActivity(activity);
        String aiResponse = geminiService.getRecommendations(prompt);
        log.info("RESPONSE FROM AI {} " + aiResponse);
        return processAiResponse(activity, aiResponse);
    }

    private Recommendation processAiResponse(Activity activity, String aiResponse) {
//        Recommendation recommendation=new Recommendation();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(aiResponse);
            JsonNode textNode = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .get("parts")
                    .get(0)
                    .get("text");
            String jsonContent = textNode.asText().replaceAll("```json\\n", "")
                    .replaceAll("\\n", "")
                    .trim();
//            log.info("RESPONSE FROM CLEANED AI {} " + jsonContent);
            JsonNode analysisJson = mapper.readTree(jsonContent);
            JsonNode analysisNode = analysisJson.path("analysis");
            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysisSection(fullAnalysis, analysisNode, "overall", "Overall");
            addAnalysisSection(fullAnalysis, analysisNode, "pace", "Pace");
            addAnalysisSection(fullAnalysis, analysisNode, "heartRate", "Heart Rate");
            addAnalysisSection(fullAnalysis, analysisNode, "caloriesBurned", "Calories Burned");

            List<String> improvements = extractImprovements(analysisJson.path("improvements"));
            List<String> safety = extractSafety(analysisJson.path("safety"));
            List<String> suggestions = extractSuggestions(analysisJson.path("suggestions"));

            return Recommendation.builder()
                    .userId(activity.getUserId())
                    .activityId(activity.getId())
                    .improvement(improvements)
                    .recommendation(fullAnalysis.toString())
                    .safety(safety)
                    .suggestion(suggestions)
                    .build();
        } catch (Exception e) {
//            throw new RuntimeException(e);
            return createDefaultRecommendation(activity);
        }
    }

    private Recommendation createDefaultRecommendation(Activity activity) {
        return Recommendation.builder()
                .userId(activity.getUserId())
                .activityId(activity.getId())
                .improvement(Collections.singletonList("Continue with your current routine"))
                .recommendation("Unable to generate detailed analysis")
                .safety(Arrays.asList("Always warm up before exercise",
                        "Stay hydrated",
                        "Listen to your body"))
                .suggestion(Collections.singletonList("Consider consulting a fitness consultant"))
                .build();
    }

    private List<String> extractSuggestions(JsonNode suggestionsNode) {
        List<String> suggestions = new ArrayList<>();
        if (suggestionsNode.isArray()) {
            suggestionsNode.forEach(sugg -> {
                String workout = sugg.path("workout").asText();
                String description = sugg.path("description").asText();
                suggestions.add(String.format("%s: %s", workout, description));
            });
        }
        return suggestions.isEmpty() ? Collections.singletonList("No Specific suggestions provided") : suggestions;
    }

    private List<String> extractSafety(JsonNode safetyNode) {
        List<String> safety = new ArrayList<>();
        if (safetyNode.isArray()) {
            safetyNode.forEach(item -> {
                safety.add(item.asText());
            });
        }
        return safety.isEmpty() ? Collections.singletonList("No Specific safety provided") : safety;
    }

    private List<String> extractImprovements(JsonNode improvementsNode) {
        List<String> improvements = new ArrayList<>();
        if (improvementsNode.isArray()) {
            improvementsNode.forEach(imp -> {
                String area = imp.path("area").asText();
                String recommendation = imp.path("recommendation").asText();
                improvements.add(String.format("%s: %s", area, recommendation));
            });
        }
        return improvements.isEmpty() ? Collections.singletonList("No Specific improvements provided") : improvements;
    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
        if (!analysisNode.path(key).isMissingNode()) {
            fullAnalysis.append(prefix)
                    .append(analysisNode.path(key).asText())
                    .append("\n\n");
        }
    }

    private String createPromptForActivity(Activity activity) {
        return String.format("""
                        Analyze this fitness activity and provide detailed recommendations in the following EXACT JSON format:
                        {
                                 "analysis": {
                                     "overall": "Overall analysis here",
                                     "pace": "Pace analysis here",
                                     "heartRate": "Heart rate analysis here",
                                     "caloriesBurned": "Calories analysis here"
                                 },
                                 "improvements": [
                                     {
                                         "area": "Area name",
                                         "recommendation": "Detailed recommendation"
                                     }
                                 ],
                                 "suggestions": [
                                     {
                                         "workout": "Workout name",
                                         "description": "Detailed workout description "
                                     }
                                 ],
                                 "safety": [
                                     "Safety point 1",
                                     "Safety point 2",
                                     "Safety point 3"
                                 ]
                             }
                        
                             Analyze this activity:
                             Activity Type: %s
                             Duration: %d minutes
                             Calories Burned: %d
                             Additional Metrics: %s
                        
                             Provide detailed analysis focusing on performance, improvements, next workout suggestions, and safety guidelines.
                             Ensure the response follows the EXACT JSON format shown above.            
                        """, activity.getActivityType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()
        );
    }
}
