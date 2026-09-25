package conf.live.cfp.event.adapter.in.web;

import tools.jackson.databind.ObjectMapper;
import conf.live.cfp.event.application.port.in.CreateEventCommand;
import conf.live.cfp.event.application.port.in.CreateEventUseCase;
import conf.live.cfp.event.domain.exception.InvalidEventException;
import conf.live.cfp.event.domain.model.Event;
import conf.live.cfp.event.domain.model.EventId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateEventUseCase createEventUseCase;

    @Test
    void should_return_201_with_the_created_event() throws Exception {
        Event created = new Event(
                EventId.fromString("11111111-1111-1111-1111-111111111111"),
                "DevFest Afrique Francophone");
        when(createEventUseCase.createEvent(any(CreateEventCommand.class))).thenReturn(created);

        mockMvc.perform(post("/api/events")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CreateEventRequest("DevFest Afrique Francophone"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.name").value("DevFest Afrique Francophone"));
    }

    @Test
    void should_return_400_when_name_is_blank() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CreateEventRequest(""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_400_when_use_case_rejects_the_event() throws Exception {
        when(createEventUseCase.createEvent(any(CreateEventCommand.class)))
                .thenThrow(new InvalidEventException("Event name must not be blank"));

        mockMvc.perform(post("/api/events")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CreateEventRequest("Some name"))))
                .andExpect(status().isBadRequest());
    }
}
