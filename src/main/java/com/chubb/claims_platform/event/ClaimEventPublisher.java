package com.chubb.claims_platform.claim.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ClaimEventPublisher {

    private static final String CLAIM_EVENTS_TOPIC = "claim-events";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public ClaimEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(ClaimEvent event) {

        String eventJson = """
                {
                  "claimId": "%s",
                  "claimNumber": "%s",
                  "fromStatus": "%s",
                  "toStatus": "%s",
                  "changedBy": "%s",
                  "estimatedLiability": %s,
                  "occurredAt": "%s"
                }
                """.formatted(
                event.getClaimId(),
                event.getClaimNumber(),
                event.getFromStatus(),
                event.getToStatus(),
                event.getChangedBy(),
                event.getEstimatedLiability(),
                event.getOccurredAt()
        );

        kafkaTemplate.send(
                CLAIM_EVENTS_TOPIC,
                event.getClaimNumber(),
                eventJson
        );
    }
}