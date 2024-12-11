package me.oldboy.servlets.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.oldboy.core.dto.JsonFormResponse;
import me.oldboy.core.model.database.audit.Audit;
import me.oldboy.core.model.database.audit.operations.AuditOperationResult;
import me.oldboy.core.model.database.audit.operations.AuditOperationType;
import me.oldboy.core.model.database.entity.options.Role;
import me.oldboy.core.model.service.AuditService;
import me.oldboy.security.JwtAuthUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class GetAllAuditsServletTest {

    @Mock
    private AuditService auditService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private HttpServletRequest req;
    @Mock
    private HttpServletResponse resp;
    @Mock
    private static ServletContext servletContext;
    @Mock
    private static ServletConfig servletConfig;
    @InjectMocks
    private GetAllAuditsServlet getAllAuditsServlet;

    private PrintWriter respPrintWriter;
    private ObjectWriter objectWriter;
    private List<Audit> testAuditReadList, listFromMethod;
    private String listToJson, jsonResponse;
    private Audit firstAudit, secondAudit;
    private JwtAuthUser jwtAuthUserAdmin, jwtAuthSimpleUser;

    @BeforeEach
    public void setUp() throws IOException {
        firstAudit = Audit.builder()
                .auditId(1L)
                .auditTimeStamp(LocalDateTime.of(2034, 11,3, 12,35))
                .auditableRecord("UserUpdateDeleteDto[userId=1, userName=UserUpdate, password=4321End, role=ADMIN]")
                .auditResult(AuditOperationResult.SUCCESS)
                .operationType(AuditOperationType.UPDATE_USER)
                .build();
        secondAudit = firstAudit = Audit.builder()
                .auditId(2L)
                .auditTimeStamp(LocalDateTime.of(2034, 7,3, 12,35))
                .auditableRecord("UserUpdateDeleteDto[userId=5, userName=UserTwo, password=1234, role=USER]")
                .auditResult(AuditOperationResult.FAIL)
                .operationType(AuditOperationType.DELETE_USER)
                .build();
        testAuditReadList = List.of(firstAudit, secondAudit);

        jwtAuthUserAdmin = new JwtAuthUser("WowUser", Role.ADMIN, true);
        jwtAuthSimpleUser = new JwtAuthUser("notWowUser", Role.USER, true);

        when(servletConfig.getServletContext()).thenReturn(servletContext);

        ObjectMapper objectMapperForWriter = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        objectMapperForWriter.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        objectMapperForWriter.registerModule(module);

        objectWriter = objectMapperForWriter.writer().withDefaultPrettyPrinter();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer respWriter = new OutputStreamWriter(outputStream);
        respPrintWriter = new PrintWriter(respWriter);

        when(resp.getWriter()).thenReturn(respPrintWriter);
    }

    @AfterEach
    public void closeAllStream() throws IOException {
        resp.getWriter().close();
    }

    @Test
    void shouldReturnAuditRecordList_doGet() throws IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);
        when(auditService.getAllAudit()).thenReturn(testAuditReadList);

        doAnswer(invocation -> {
            PrintWriter outputPrintWriter = invocation.getArgument(0);
            listFromMethod = invocation.getArgument(1);

            listToJson = objectWriter.writeValueAsString(listFromMethod);
            outputPrintWriter.write(listToJson);

            return null;
        }).when(objectMapper).writeValue(any(PrintWriter.class), anyList());

        getAllAuditsServlet.doGet(req, resp);

        assertThat(listToJson).isEqualTo(objectWriter.writeValueAsString(testAuditReadList));
        assertThat(listFromMethod).contains(firstAudit, secondAudit);

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), anyList());
        verify(auditService, times(1)).getAllAudit();
    }

    @Test
    void shouldThrowException_HaveNoPermission_doGet() throws IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthSimpleUser);

        doAnswer(invocation -> {
            PrintWriter outputPrintWriter = invocation.getArgument(0);
            JsonFormResponse response = invocation.getArgument(1);

            jsonResponse = "{\"message\":\"" + response.message() + "\"}";
            outputPrintWriter.write(jsonResponse);

            return null;
        }).when(objectMapper).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));

        getAllAuditsServlet.doGet(req, resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"You do not have permission to access this page! У вас нет доступа!\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
        verify(auditService, never()).getAllAudit();
    }
}