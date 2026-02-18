package io.wanjune.agent.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * @author zjh
 * @since 2026/2/18 14:40
 */
public record RunRequest (@NotBlank String query){
}