package com.videoprocessing.websocket;

import com.videoprocessing.dtos.responseDtos.JobUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class JobStatusWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/job.status")
    @SendTo("/topic/job-updates")
    public String subscribeToJobUpdates(String jobId) {
        log.info("Client subscribed to job updates: {}", jobId);
        return "Subscribed to job: " + jobId;
    }

    // Method to send real-time updates (called from service)
    public void sendJobUpdate(String jobId, String status, Integer progress) {
        JobUpdateMessage message = new JobUpdateMessage(jobId, status, progress);
        messagingTemplate.convertAndSend("/topic/job-updates/" + jobId, message);
    }
}
