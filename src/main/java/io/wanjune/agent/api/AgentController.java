package io.wanjune.agent.api;

import io.wanjune.agent.api.dto.RunRequest;
import io.wanjune.agent.api.dto.RunResponse;
import io.wanjune.agent.service.AgentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zjh
 * @since 2026/2/18 15:18
 */
@RestController
@RequestMapping("/agent")
public class AgentController {

    private final AgentService agentService;
    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/run")
    public RunResponse run(@Valid @RequestBody RunRequest req) {
        var r = agentService.run(req.query());
        return new RunResponse(r.traceId(), r.answer(), r.steps());
    }

}