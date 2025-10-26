package com.devhour.infrastructure.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.junit.jupiter.api.Test;

/**
 * Unit test for WorkCategoryMapper interface
 * Tests annotations, method signatures, and parameter types
 */
class WorkCategoryMapperInterfaceTest {

    @Test
    void testMapperAnnotationPresent() {
        assertTrue(WorkCategoryMapper.class.isAnnotationPresent(Mapper.class), 
                  "WorkCategoryMapper should have @Mapper annotation");
    }

    @Test
    void testFindByIdMethod() throws NoSuchMethodException {
        Method method = WorkCategoryMapper.class.getMethod("findById", String.class);
        
        assertNotNull(method);
        assertEquals(Optional.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Select.class));
        
        // Check parameter annotation
        assertTrue(method.getParameters()[0].isAnnotationPresent(Param.class));
        assertEquals("id", method.getParameters()[0].getAnnotation(Param.class).value());
    }

    @Test
    void testFindByCodeMethod() throws NoSuchMethodException {
        Method method = WorkCategoryMapper.class.getMethod("findByCode", String.class);
        
        assertNotNull(method);
        assertEquals(Optional.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Select.class));
        
        // Check parameter annotation
        assertTrue(method.getParameters()[0].isAnnotationPresent(Param.class));
        assertEquals("code", method.getParameters()[0].getAnnotation(Param.class).value());
    }

    @Test
    void testFindAllMethod() throws NoSuchMethodException {
        Method method = WorkCategoryMapper.class.getMethod("findAll");
        
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Select.class));
    }

    @Test
    void testFindActiveCategoriesMethod() throws NoSuchMethodException {
        Method method = WorkCategoryMapper.class.getMethod("findActiveCategories");
        
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Select.class));
    }

    @Test
    void testExistsByCodeMethod() throws NoSuchMethodException {
        Method method = WorkCategoryMapper.class.getMethod("existsByCode", String.class);
        
        assertNotNull(method);
        assertEquals(boolean.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Select.class));
        
        // Check parameter annotation
        assertTrue(method.getParameters()[0].isAnnotationPresent(Param.class));
        assertEquals("code", method.getParameters()[0].getAnnotation(Param.class).value());
    }

    @Test
    void testExistsByNameMethod() throws NoSuchMethodException {
        Method method = WorkCategoryMapper.class.getMethod("existsByName", String.class);
        
        assertNotNull(method);
        assertEquals(boolean.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Select.class));
        
        // Check parameter annotation
        assertTrue(method.getParameters()[0].isAnnotationPresent(Param.class));
        assertEquals("name", method.getParameters()[0].getAnnotation(Param.class).value());
    }

    @Test
    void testGetMaxDisplayOrderMethod() throws NoSuchMethodException {
        Method method = WorkCategoryMapper.class.getMethod("getMaxDisplayOrder");
        
        assertNotNull(method);
        assertEquals(int.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Select.class));
    }

    @Test
    void testCountMethod() throws NoSuchMethodException {
        Method method = WorkCategoryMapper.class.getMethod("count");
        
        assertNotNull(method);
        assertEquals(long.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Select.class));
    }

    @Test
    void testCountActiveCategoriesMethod() throws NoSuchMethodException {
        Method method = WorkCategoryMapper.class.getMethod("countActiveCategories");
        
        assertNotNull(method);
        assertEquals(long.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Select.class));
    }

    @Test
    void testInsertMethod() throws NoSuchMethodException {
        Method method = WorkCategoryMapper.class.getMethod("insert", 
            String.class, String.class, String.class, String.class, 
            boolean.class, int.class, String.class, LocalDateTime.class, 
            String.class, LocalDateTime.class);
        
        assertNotNull(method);
        assertEquals(void.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Insert.class));
        
        // Check all parameters have @Param annotations
        assertEquals(10, method.getParameterCount());
        for (int i = 0; i < method.getParameterCount(); i++) {
            assertTrue(method.getParameters()[i].isAnnotationPresent(Param.class), 
                      "Parameter " + i + " should have @Param annotation");
        }
    }

    @Test
    void testUpdateMethod() throws NoSuchMethodException {
        Method method = WorkCategoryMapper.class.getMethod("update", 
            String.class, String.class, String.class, int.class, 
            String.class, LocalDateTime.class);
        
        assertNotNull(method);
        assertEquals(int.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Update.class));
        
        // Check all parameters have @Param annotations
        assertEquals(6, method.getParameterCount());
        for (int i = 0; i < method.getParameterCount(); i++) {
            assertTrue(method.getParameters()[i].isAnnotationPresent(Param.class), 
                      "Parameter " + i + " should have @Param annotation");
        }
    }

    @Test
    void testUpdateActiveStatusMethod() throws NoSuchMethodException {
        Method method = WorkCategoryMapper.class.getMethod("updateActiveStatus", 
            String.class, boolean.class, String.class, LocalDateTime.class);
        
        assertNotNull(method);
        assertEquals(int.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Update.class));
        
        // Check all parameters have @Param annotations
        assertEquals(4, method.getParameterCount());
        for (int i = 0; i < method.getParameterCount(); i++) {
            assertTrue(method.getParameters()[i].isAnnotationPresent(Param.class), 
                      "Parameter " + i + " should have @Param annotation");
        }
    }

    @Test
    void testSoftDeleteMethod() throws NoSuchMethodException {
        Method method = WorkCategoryMapper.class.getMethod("softDelete", 
            String.class, LocalDateTime.class, String.class, LocalDateTime.class);
        
        assertNotNull(method);
        assertEquals(int.class, method.getReturnType()); // Returns int, not void
        assertTrue(method.isAnnotationPresent(Update.class));
        
        // Check all parameters have @Param annotations
        assertEquals(4, method.getParameterCount());
        for (int i = 0; i < method.getParameterCount(); i++) {
            assertTrue(method.getParameters()[i].isAnnotationPresent(Param.class), 
                      "Parameter " + i + " should have @Param annotation");
        }
    }

    @Test
    void testAllMethodsHaveProperReturnTypes() {
        Method[] methods = WorkCategoryMapper.class.getDeclaredMethods();
        
        for (Method method : methods) {
            // Ensure no method returns null inappropriately
            assertNotNull(method.getReturnType(), 
                         "Method " + method.getName() + " should have a valid return type");
            
            // Query methods should return appropriate types
            if (method.getName().startsWith("find")) {
                assertTrue(Optional.class.equals(method.getReturnType()) || 
                          List.class.equals(method.getReturnType()),
                          "Find methods should return Optional or List");
            }
            
            if (method.getName().startsWith("exists") || method.getName().startsWith("count")) {
                assertTrue(method.getReturnType().isPrimitive(),
                          "Exists/count methods should return primitive types");
            }
        }
    }

    @Test
    void testNoMethodThrowsCheckedExceptions() {
        Method[] methods = WorkCategoryMapper.class.getDeclaredMethods();
        
        for (Method method : methods) {
            assertEquals(0, method.getExceptionTypes().length,
                        "MyBatis mapper methods should not declare checked exceptions: " + method.getName());
        }
    }
}