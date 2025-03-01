package com.example.teamcity.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {
    PROJECT_VIEWER("PROJECT_VIEWER"),
    GUEST_ROLE("GUEST_ROLE"),
    USER_ROLE("USER_ROLE"),
    PROJECT_DEVELOPER("PROJECT_DEVELOPER"),
    TOOLS_INTEGRATION("TOOLS_INTEGRATION"),
    PROJECT_ADMIN("PROJECT_ADMIN"),
    AGENT_MANAGER("AGENT_MANAGER");

    private final String roleName;
}
