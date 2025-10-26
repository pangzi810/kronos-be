package com.devhour.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.devhour.domain.model.entity.Project;
import com.devhour.domain.model.valueobject.ProjectStatus;
import com.devhour.infrastructure.mapper.ProjectMapper;

@ExtendWith(MockitoExtension.class)
class ProjectRepositoryImplTest {

    @Mock
    private ProjectMapper projectMapper;

    private ProjectRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new ProjectRepositoryImpl(projectMapper);
    }

    @Test
    void testFindById_Found() {
        String projectId = "project123";
        Project expectedProject = createTestProject(projectId, "Test Project");
        
        when(projectMapper.findById(projectId)).thenReturn(Optional.of(expectedProject));
        
        Optional<Project> result = repository.findById(projectId);
        
        assertTrue(result.isPresent());
        assertEquals(expectedProject, result.get());
        verify(projectMapper).findById(projectId);
    }

    @Test
    void testFindById_NotFound() {
        String projectId = "nonexistent";
        
        when(projectMapper.findById(projectId)).thenReturn(Optional.empty());
        
        Optional<Project> result = repository.findById(projectId);
        
        assertFalse(result.isPresent());
        verify(projectMapper).findById(projectId);
    }

    @Test
    void testFindByName_Found() {
        String name = "Test Project";
        Project expectedProject = createTestProject("project123", name);
        
        when(projectMapper.findByName(name)).thenReturn(Optional.of(expectedProject));
        
        Optional<Project> result = repository.findByName(name);
        
        assertTrue(result.isPresent());
        assertEquals(expectedProject, result.get());
        verify(projectMapper).findByName(name);
    }

    @Test
    void testFindByName_NotFound() {
        String name = "Nonexistent Project";
        
        when(projectMapper.findByName(name)).thenReturn(Optional.empty());
        
        Optional<Project> result = repository.findByName(name);
        
        assertFalse(result.isPresent());
        verify(projectMapper).findByName(name);
    }

    @Test
    void testFindAll() {
        List<Project> expectedProjects = Arrays.asList(
            createTestProject("project1", "Project 1"),
            createTestProject("project2", "Project 2")
        );
        
        when(projectMapper.findAll()).thenReturn(expectedProjects);
        
        List<Project> result = repository.findAll();
        
        assertEquals(2, result.size());
        assertEquals(expectedProjects, result);
        verify(projectMapper).findAll();
    }

    @Test
    void testFindAll_Empty() {
        when(projectMapper.findAll()).thenReturn(Arrays.asList());
        
        List<Project> result = repository.findAll();
        
        assertTrue(result.isEmpty());
        verify(projectMapper).findAll();
    }

    @Test
    void testFindByStatus_Draft() {
        ProjectStatus status = ProjectStatus.DRAFT;
        List<Project> expectedProjects = Arrays.asList(
            createTestProject("project1", "Draft Project 1"),
            createTestProject("project2", "Draft Project 2")
        );
        
        when(projectMapper.findByStatus("DRAFT")).thenReturn(expectedProjects);
        
        List<Project> result = repository.findByStatus(status);
        
        assertEquals(2, result.size());
        assertEquals(expectedProjects, result);
        verify(projectMapper).findByStatus("DRAFT");
    }

    @Test
    void testFindByStatus_InProgress() {
        ProjectStatus status = ProjectStatus.IN_PROGRESS;
        List<Project> expectedProjects = Arrays.asList(
            createTestProject("project1", "In Progress Project")
        );
        
        when(projectMapper.findByStatus("IN_PROGRESS")).thenReturn(expectedProjects);
        
        List<Project> result = repository.findByStatus(status);
        
        assertEquals(1, result.size());
        assertEquals(expectedProjects, result);
        verify(projectMapper).findByStatus("IN_PROGRESS");
    }

    @Test
    void testFindActiveProjects() {
        List<Project> expectedProjects = Arrays.asList(
            createTestProject("project1", "Active Project 1"),
            createTestProject("project2", "Active Project 2")
        );
        
        when(projectMapper.findActiveProjects()).thenReturn(expectedProjects);
        
        List<Project> result = repository.findActiveProjects();
        
        assertEquals(2, result.size());
        assertEquals(expectedProjects, result);
        verify(projectMapper).findActiveProjects();
    }

    @Test
    void testFindWorkRecordableProjects() {
        List<Project> expectedProjects = Arrays.asList(
            createTestProject("project1", "Recordable Project 1"),
            createTestProject("project2", "Recordable Project 2")
        );
        
        when(projectMapper.findActiveProjects()).thenReturn(expectedProjects);
        
        List<Project> result = repository.findActiveProjects();
        
        assertEquals(2, result.size());
        assertEquals(expectedProjects, result);
        verify(projectMapper).findActiveProjects();
    }


    @Test
    void testSearchByName() {
        String namePattern = "%Test%";
        List<Project> expectedProjects = Arrays.asList(
            createTestProject("project1", "Test Project 1"),
            createTestProject("project2", "Test Project 2")
        );
        
        when(projectMapper.searchByName(namePattern)).thenReturn(expectedProjects);
        
        List<Project> result = repository.searchByName(namePattern);
        
        assertEquals(2, result.size());
        assertEquals(expectedProjects, result);
        verify(projectMapper).searchByName(namePattern);
    }

    @Test
    void testSearchByName_NoResults() {
        String namePattern = "%NonExistent%";
        
        when(projectMapper.searchByName(namePattern)).thenReturn(Arrays.asList());
        
        List<Project> result = repository.searchByName(namePattern);
        
        assertTrue(result.isEmpty());
        verify(projectMapper).searchByName(namePattern);
    }

    @Test
    void testExistsByName_True() {
        String name = "Existing Project";
        
        when(projectMapper.existsByName(name)).thenReturn(true);
        
        boolean result = repository.existsByName(name);
        
        assertTrue(result);
        verify(projectMapper).existsByName(name);
    }

    @Test
    void testExistsByName_False() {
        String name = "Nonexistent Project";
        
        when(projectMapper.existsByName(name)).thenReturn(false);
        
        boolean result = repository.existsByName(name);
        
        assertFalse(result);
        verify(projectMapper).existsByName(name);
    }

    @Test
    void testExistsById_True() {
        String projectId = "project123";
        
        when(projectMapper.existsById(projectId)).thenReturn(true);
        
        boolean result = repository.existsById(projectId);
        
        assertTrue(result);
        verify(projectMapper).existsById(projectId);
    }

    @Test
    void testExistsById_False() {
        String projectId = "nonexistent";
        
        when(projectMapper.existsById(projectId)).thenReturn(false);
        
        boolean result = repository.existsById(projectId);
        
        assertFalse(result);
        verify(projectMapper).existsById(projectId);
    }

    @Test
    void testSave_NewProject() {
        Project project = createTestProject("newProject", "New Project");
        
        when(projectMapper.existsById("newProject")).thenReturn(false);
        
        Project result = repository.save(project);
        
        assertEquals(project, result);
        verify(projectMapper).existsById("newProject");
        verify(projectMapper).insert(
            eq("newProject"),
            eq("New Project"),
            eq("Test Description"),
            eq("DRAFT"),
            any(LocalDate.class),
            any(LocalDate.class),
            eq("createdBy"),
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            isNull(),
            isNull()
        );
    }

    @Test
    void testSave_ExistingProject() {
        Project project = createTestProject("existingProject", "Existing Project");
        
        when(projectMapper.existsById("existingProject")).thenReturn(true);
        
        Project result = repository.save(project);
        
        assertEquals(project, result);
        verify(projectMapper).existsById("existingProject");
        verify(projectMapper).update(
            eq("existingProject"),
            eq("Existing Project"),
            eq("Test Description"),
            eq("DRAFT"),
            any(LocalDate.class),
            any(LocalDate.class),
            isNull(),
            isNull(),
            any(LocalDateTime.class)
        );
    }

    @Test
    void testSaveAll() {
        List<Project> projects = Arrays.asList(
            createTestProject("project1", "Project 1"),
            createTestProject("project2", "Project 2")
        );
        
        when(projectMapper.existsById("project1")).thenReturn(false);
        when(projectMapper.existsById("project2")).thenReturn(true);
        
        List<Project> result = repository.saveAll(projects);
        
        assertEquals(projects, result);
        verify(projectMapper).existsById("project1");
        verify(projectMapper).existsById("project2");
        verify(projectMapper).insert(
            eq("project1"),
            eq("Project 1"),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any()
        );
        verify(projectMapper).update(
            eq("project2"),
            eq("Project 2"),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any()
        );
    }

    @Test
    void testSaveAll_Empty() {
        List<Project> projects = Arrays.asList();
        
        List<Project> result = repository.saveAll(projects);
        
        assertTrue(result.isEmpty());
        verify(projectMapper, never()).existsById(any());
        verify(projectMapper, never()).insert(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(projectMapper, never()).update(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testDeleteById() {
        String projectId = "project123";
        
        repository.deleteById(projectId);
        
        verify(projectMapper).softDelete(eq(projectId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testCountByStatus_Planning() {
        ProjectStatus status = ProjectStatus.DRAFT;
        
        when(projectMapper.countByStatus("DRAFT")).thenReturn(5L);
        
        long result = repository.countByStatus(status);
        
        assertEquals(5L, result);
        verify(projectMapper).countByStatus("DRAFT");
    }

    @Test
    void testCountByStatus_Closed() {
        ProjectStatus status = ProjectStatus.CLOSED;
        
        when(projectMapper.countByStatus("CLOSED")).thenReturn(10L);
        
        long result = repository.countByStatus(status);
        
        assertEquals(10L, result);
        verify(projectMapper).countByStatus("CLOSED");
    }

    @Test
    void testCountByStatus_Zero() {
        ProjectStatus status = ProjectStatus.CLOSED;
        
        when(projectMapper.countByStatus("CLOSED")).thenReturn(0L);
        
        long result = repository.countByStatus(status);
        
        assertEquals(0L, result);
        verify(projectMapper).countByStatus("CLOSED");
    }

    private Project createTestProject(String id, String name) {
        return Project.restore(
            id,
            name,
            "Test Description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            null,
            ProjectStatus.DRAFT,
            "createdBy",
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }
}