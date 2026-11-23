package gr.aueb.byteme.evaluation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class EvaluationHttpServer {

    private final HttpServer server;
    private final EvaluationService evaluationService;
    private final ObjectMapper objectMapper;

    public EvaluationHttpServer(
            int port,
            EvaluationService evaluationService
    ) throws IOException {

        this.evaluationService = evaluationService;
        this.objectMapper = new ObjectMapper();
        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext(
                "/api/evaluation/events",
                this::handleEvents
        );

        server.createContext(
                "/api/evaluation/metrics",
                this::handleMetrics
        );

        server.setExecutor(null);
    }

    private void handleEvents(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendJson(
                    exchange,
                    405,
                    Map.of(
                            "status", "FAILED",
                            "message", "Method not allowed"
                    )
            );
            return;
        }

        try {
            EvaluationEvent event = objectMapper.readValue(
                    exchange.getRequestBody(),
                    EvaluationEvent.class
            );

            evaluationService.recordEvent(
                    event.elevatorId,
                    event.timestamp,
                    event.eventType,
                    event.floor
            );

            sendJson(
                    exchange,
                    200,
                    Map.of(
                            "status", "SUCCESS",
                            "message",
                            "Event recorded successfully. Total events: "
                                    + evaluationService.getTotalEvents()
                    )
            );

        } catch (IllegalArgumentException exception) {
            sendJson(
                    exchange,
                    400,
                    Map.of(
                            "status", "FAILED",
                            "message", exception.getMessage()
                    )
            );
        } catch (Exception exception) {
            sendJson(
                    exchange,
                    400,
                    Map.of(
                            "status", "FAILED",
                            "message", "Invalid event JSON"
                    )
            );
        }
    }

    private void handleMetrics(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendJson(
                    exchange,
                    405,
                    Map.of(
                            "status", "FAILED",
                            "message", "Method not allowed"
                    )
            );
            return;
        }

        try {
            EvaluationMetric metric = objectMapper.readValue(
                    exchange.getRequestBody(),
                    EvaluationMetric.class
            );

            evaluationService.recordMetric(
                    metric.elevatorId,
                    metric.metricName,
                    metric.value,
                    metric.passengersOnboard
            );

            sendJson(
                    exchange,
                    200,
                    Map.of(
                            "status", "SUCCESS",
                            "message",
                            "Metrics recorded successfully. Total requests: "
                                    + evaluationService.getTotalMetrics()
                    )
            );

        } catch (CapacityExceededException exception) {
            sendJson(
                    exchange,
                    400,
                    Map.of(
                            "status", "FAILED",
                            "violation", "CAPACITY_EXCEEDED",
                            "message", exception.getMessage()
                    )
            );

        } catch (IllegalArgumentException exception) {
            sendJson(
                    exchange,
                    400,
                    Map.of(
                            "status", "FAILED",
                            "message", exception.getMessage()
                    )
            );

        } catch (Exception exception) {
            sendJson(
                    exchange,
                    400,
                    Map.of(
                            "status", "FAILED",
                            "message", "Invalid metric JSON"
                    )
            );
        }
    }

    private void sendJson(
            HttpExchange exchange,
            int statusCode,
            Map<String, String> response
    ) throws IOException {

        byte[] responseBytes = objectMapper
                .writeValueAsString(response)
                .getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }
}