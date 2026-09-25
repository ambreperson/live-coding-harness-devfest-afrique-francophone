package conf.live.cfp.event.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

/**
 * Web request payload to create a new event.
 */
public record CreateEventRequest(@NotBlank String name) {
}
