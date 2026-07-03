package com.aakash.goalkeeper.goal;

import com.aakash.goalkeeper.goal.dto.DashboardDtos.DashboardStats;
import com.aakash.goalkeeper.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/stats")
    public DashboardStats stats(@AuthenticationPrincipal UserPrincipal principal) {
        return service.stats(principal.id());
    }
}
