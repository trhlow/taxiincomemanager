package com.taxiincome.schedule;

import com.taxiincome.common.ApiKeyFilter;
import com.taxiincome.common.BearerTokenFilter;
import com.taxiincome.schedule.dto.ScheduleResponse;
import com.taxiincome.schedule.dto.ScheduleUpsertResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WorkScheduleController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkScheduleControllerTest {

    @MockBean
    private ApiKeyFilter apiKeyFilter;

    @MockBean
    private BearerTokenFilter bearerTokenFilter;

    @MockBean
    private WorkScheduleService workScheduleService;

    @Autowired
    private MockMvc mockMvc;

    private static final String UPSERT_JSON =
            "{\"workDate\":\"2026-05-10\",\"shiftType\":\"MORNING\"}";

    @Test
    void upsert_whenCreated_returns201() throws Exception {
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 5, 10);
        ScheduleResponse body = new ScheduleResponse(id, date, ShiftType.MORNING);
        when(workScheduleService.upsert(any())).thenReturn(new ScheduleUpsertResult(true, body));

        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPSERT_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void upsert_whenAlreadyExists_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 5, 10);
        ScheduleResponse body = new ScheduleResponse(id, date, ShiftType.MORNING);
        when(workScheduleService.upsert(any())).thenReturn(new ScheduleUpsertResult(false, body));

        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPSERT_JSON))
                .andExpect(status().isOk());
    }
}
