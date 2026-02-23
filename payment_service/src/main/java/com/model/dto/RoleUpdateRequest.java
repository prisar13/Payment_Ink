package com.model.dto;

import java.util.Set;
import java.util.UUID;

public class RoleUpdateRequest {
    private UUID userId;
    private Set<String> roles;
}
