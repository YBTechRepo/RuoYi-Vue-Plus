package org.dromara.insurance.service;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.insurance.config.ApplicationFormExportProperties;
import org.dromara.insurance.domain.InsuranceApplicationExportTask;
import org.dromara.insurance.domain.bo.InsuranceApplicationExportRequest;
import org.dromara.insurance.domain.vo.InsuranceApplyRecordVo;
import org.dromara.insurance.mapper.InsuranceApplicationExportTaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DuplicateKeyException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("dev")
class InsuranceApplicationExportServiceTest {
    private InsuranceApplicationExportTaskMapper taskMapper;
    private IInsuranceProxyOrderService proxyOrderService;
    private TaskExecutor executor;
    private InsuranceApplicationExportService service;
    private ObjectMapper json;

    @BeforeEach
    void setup() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), "test"), InsuranceApplicationExportTask.class);
        taskMapper = mock(InsuranceApplicationExportTaskMapper.class);
        proxyOrderService = mock(IInsuranceProxyOrderService.class);
        executor = mock(TaskExecutor.class);
        json = new ObjectMapper();
        service = new InsuranceApplicationExportService(
            taskMapper,
            proxyOrderService,
            mock(InsuranceApplicationFormService.class),
            mock(ApplicationFormStorage.class),
            new ApplicationFormExportProperties(),
            json,
            mock(ScheduledExecutorService.class),
            executor
        );
    }

    @Test
    void selectedScopeFreezesDistinctIdsAndSubmitsOneTask() throws Exception {
        when(taskMapper.selectCount(any())).thenReturn(0L);
        doAnswer(invocation -> {
            InsuranceApplicationExportTask task = invocation.getArgument(0);
            task.setId(88L);
            return 1;
        }).when(taskMapper).insert(any(InsuranceApplicationExportTask.class));
        InsuranceApplicationExportRequest request = request("SELECTED", List.of(10L, 10L, 20L));

        try (var login = mockStatic(LoginHelper.class)) {
            login.when(LoginHelper::getUserId).thenReturn(7L);
            var result = service.create(request);

            var taskCaptor = org.mockito.ArgumentCaptor.forClass(InsuranceApplicationExportTask.class);
            verify(taskMapper).insert(taskCaptor.capture());
            InsuranceApplicationExportTask task = taskCaptor.getValue();
            assertEquals(List.of(10L, 20L), json.readValue(task.getTargetIdsJson(), new TypeReference<List<Long>>() { }));
            assertEquals(2, task.getTotalCount());
            assertEquals(7L, task.getActiveUserId());
            assertEquals("000000", task.getTenantId());
            assertEquals(88L, result.getId());
            verify(executor).execute(any());
        }
    }

    @Test
    void rejectsMoreThanConfiguredMaximumAndAnExistingActiveTask() {
        List<Long> ids = new ArrayList<>();
        for (long id = 1; id <= 201; id++) ids.add(id);
        InsuranceApplicationExportRequest request = request("SELECTED", ids);

        try (var login = mockStatic(LoginHelper.class)) {
            login.when(LoginHelper::getUserId).thenReturn(7L);
            when(taskMapper.selectCount(any())).thenReturn(0L);
            assertThrows(ServiceException.class, () -> service.create(request));
            verify(taskMapper, never()).insert(any(InsuranceApplicationExportTask.class));

            reset(taskMapper);
            when(taskMapper.selectCount(any())).thenReturn(1L);
            assertThrows(ServiceException.class, () -> service.create(request("SELECTED", List.of(1L))));
            verify(taskMapper, never()).insert(any(InsuranceApplicationExportTask.class));
        }
    }

    @Test
    void repeatedRequestReturnsExistingTaskWithoutCreatingAnother() {
        InsuranceApplicationExportTask existing = new InsuranceApplicationExportTask();
        existing.setId(99L);
        existing.setStatus("PENDING");
        existing.setTotalCount(1);
        existing.setSuccessCount(0);
        existing.setSkippedCount(0);
        when(taskMapper.selectOne(any())).thenReturn(existing);

        try (var login = mockStatic(LoginHelper.class)) {
            login.when(LoginHelper::getUserId).thenReturn(7L);
            assertEquals(99L, service.create(request("SELECTED", List.of(1L))).getId());
            verify(taskMapper, never()).insert(any(InsuranceApplicationExportTask.class));
            verifyNoInteractions(executor);
        }
    }

    @Test
    void concurrentDuplicateInsertFallsBackToTheIdempotentTask() {
        InsuranceApplicationExportTask existing = new InsuranceApplicationExportTask();
        existing.setId(100L);
        existing.setStatus("PENDING");
        existing.setTotalCount(1);
        existing.setSuccessCount(0);
        existing.setSkippedCount(0);
        when(taskMapper.selectOne(any())).thenReturn(null, existing);
        when(taskMapper.selectCount(any())).thenReturn(0L);
        when(taskMapper.insert(any(InsuranceApplicationExportTask.class))).thenThrow(new DuplicateKeyException("duplicate"));

        try (var login = mockStatic(LoginHelper.class)) {
            login.when(LoginHelper::getUserId).thenReturn(7L);
            assertEquals(100L, service.create(request("SELECTED", List.of(1L))).getId());
            verifyNoInteractions(executor);
        }
    }

    @Test
    void manifestUsesUtf8BomCrlfAndStableTabColumns() {
        var item = new InsuranceApplicationExportService.ExportItem(
            1, "000001", "测试\t租户", 1001L, "ORDER\r\n001", "学生险", "READY", true,
            "投保单/000001_ORDER_1001_投保单.pdf", ""
        );

        byte[] bytes = InsuranceApplicationExportService.buildManifest(List.of(item));

        assertEquals((byte) 0xEF, bytes[0]);
        assertEquals((byte) 0xBB, bytes[1]);
        assertEquals((byte) 0xBF, bytes[2]);
        String text = new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        String[] lines = text.split("\\r\\n");
        assertEquals(2, lines.length);
        assertEquals(10, lines[0].split("\\t", -1).length);
        assertEquals(10, lines[1].split("\\t", -1).length);
        assertTrue(lines[1].contains("测试 租户"));
        assertTrue(lines[1].contains("ORDER  001"));
        assertFalse(text.replace("\r\n", "").contains("\n"));
    }

    @Test
    void safePartBlocksPathTraversalAndLimitsLength() {
        String safe = InsuranceApplicationExportService.safePart("../租户:A\\B?*");

        assertFalse(safe.contains("/"));
        assertFalse(safe.contains("\\"));
        assertFalse(safe.contains(":"));
        assertFalse(safe.contains("?"));
        assertFalse(safe.contains("*"));
        assertTrue(InsuranceApplicationExportService.safePart("x".repeat(150)).length() <= 100);
    }

    private static InsuranceApplicationExportRequest request(String scope, List<Long> ids) {
        InsuranceApplicationExportRequest request = new InsuranceApplicationExportRequest();
        request.setRequestId("request-001");
        request.setScope(scope);
        request.setOrderIds(ids);
        return request;
    }
}
