package com.taxiincome.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ApiKeyFilterTest {

    @Mock
    private FilterChain chain;

    private final FilterJsonResponses json = new FilterJsonResponses(new ObjectMapper());

    @Test
    void matchingKey_invokesNextFilter() throws Exception {
        ApiKeyFilter filter = new ApiKeyFilter("integration-secret", json);
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/ping");
        req.addHeader("X-Api-Key", "integration-secret");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
    }

    @Test
    void wrongKey_sameLength_doesNotInvokeChain_and401() throws Exception {
        ApiKeyFilter filter = new ApiKeyFilter("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", json);
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/ping");
        req.addHeader("X-Api-Key", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, chain);

        verify(chain, never()).doFilter(any(), any());
        assertThat(res.getStatus()).isEqualTo(401);
    }

    @Test
    void missingHeader_doesNotInvokeChain() throws Exception {
        ApiKeyFilter filter = new ApiKeyFilter("secret", json);
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/ping");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, chain);

        verify(chain, never()).doFilter(any(), any());
        assertThat(res.getStatus()).isEqualTo(401);
    }
}
