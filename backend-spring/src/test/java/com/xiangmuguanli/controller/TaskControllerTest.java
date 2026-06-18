package com.xiangmuguanli.controller;

import com.xiangmuguanli.dto.response.TaskResponse;
import com.xiangmuguanli.service.ProjectService;
import com.xiangmuguanli.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private ProjectService projectService;

    @Test
    @WithMockUser(username = "testuser")
    void getTasksByProject_whenUserHasAccess_returnsTasks() throws Exception {
        when(projectService.checkProjectAccess(eq(1L), eq("testuser"))).thenReturn(true);

        TaskResponse task = new TaskResponse();
        task.setId(1L);
        task.setTitle("Test Task");
        Page<TaskResponse> page = new PageImpl<>(List.of(task));
        when(taskService.getTasksByProject(eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/tasks")
                .param("projectId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].title").value("Test Task"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getTasksByProject_whenUserHasNoAccess_returnsForbidden() throws Exception {
        when(projectService.checkProjectAccess(eq(1L), eq("testuser"))).thenReturn(false);

        mockMvc.perform(get("/api/v1/tasks")
                .param("projectId", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }
}
