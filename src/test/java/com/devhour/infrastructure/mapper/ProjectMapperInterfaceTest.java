package com.devhour.infrastructure.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ProjectMapperインターフェースのユニットテスト
 * アノテーション、メソッドシグネチャ、パラメータタイプをテスト
 */
@DisplayName("ProjectMapperインターフェーステスト")
class ProjectMapperInterfaceTest {

    @Test
    @DisplayName("@Mapperアノテーションが存在する")
    void testMapperAnnotationPresent() {
        assertTrue(ProjectMapper.class.isAnnotationPresent(Mapper.class), 
                  "ProjectMapperは@Mapperアノテーションを持つべき");
    }

    @Test
    @DisplayName("findByIdメソッドのテスト")
    void testFindByIdMethod() throws NoSuchMethodException {
        Method method = ProjectMapper.class.getMethod("findById", String.class);
        
        assertNotNull(method);
        assertEquals(Optional.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Select.class));
        
        // パラメータアノテーションのチェック
        assertTrue(method.getParameters()[0].isAnnotationPresent(Param.class));
        assertEquals("id", method.getParameters()[0].getAnnotation(Param.class).value());
    }

    @Test
    @DisplayName("findByNameメソッドのテスト")
    void testFindByNameMethod() throws NoSuchMethodException {
        Method method = ProjectMapper.class.getMethod("findByName", String.class);
        
        assertNotNull(method);
        assertEquals(Optional.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Select.class));
        
        // パラメータアノテーションのチェック
        assertTrue(method.getParameters()[0].isAnnotationPresent(Param.class));
        assertEquals("name", method.getParameters()[0].getAnnotation(Param.class).value());
    }

    @Test
    @DisplayName("findAllメソッドのテスト")
    void testFindAllMethod() throws NoSuchMethodException {
        Method method = ProjectMapper.class.getMethod("findAll");
        
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Select.class));
    }

    @Test
    @DisplayName("findByStatusメソッドのテスト")
    void testFindByStatusMethod() throws NoSuchMethodException {
        Method method = ProjectMapper.class.getMethod("findByStatus", String.class);
        
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Select.class));
        
        // パラメータアノテーションのチェック
        assertTrue(method.getParameters()[0].isAnnotationPresent(Param.class));
        assertEquals("status", method.getParameters()[0].getAnnotation(Param.class).value());
    }

    @Test
    @DisplayName("findActiveProjectsメソッドのテスト")
    void testFindActiveProjectsMethod() throws NoSuchMethodException {
        Method method = ProjectMapper.class.getMethod("findActiveProjects");
        
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Select.class));
    }

    @Test
    @DisplayName("findWorkRecordableProjectsメソッドのテスト")
    void testFindWorkRecordableProjectsMethod() throws NoSuchMethodException {
        Method method = ProjectMapper.class.getMethod("findWorkRecordableProjects");
        
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Select.class));
    }

    @Test
    @DisplayName("findByStartDateBetweenメソッドのテスト")
    void testFindByStartDateBetweenMethod() throws NoSuchMethodException {
        Method method = ProjectMapper.class.getMethod("findByStartDateBetween", LocalDate.class, LocalDate.class);
        
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Select.class));
        
        // パラメータアノテーションのチェック
        assertEquals(2, method.getParameterCount());
        assertTrue(method.getParameters()[0].isAnnotationPresent(Param.class));
        assertEquals("startDate", method.getParameters()[0].getAnnotation(Param.class).value());
        assertTrue(method.getParameters()[1].isAnnotationPresent(Param.class));
        assertEquals("endDate", method.getParameters()[1].getAnnotation(Param.class).value());
    }

    @Test
    @DisplayName("findByPlannedEndDateBetweenメソッドのテスト")
    void testFindByPlannedEndDateBetweenMethod() throws NoSuchMethodException {
        Method method = ProjectMapper.class.getMethod("findByPlannedEndDateBetween", LocalDate.class, LocalDate.class);
        
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Select.class));
        
        // パラメータアノテーションのチェック
        assertEquals(2, method.getParameterCount());
        assertTrue(method.getParameters()[0].isAnnotationPresent(Param.class));
        assertEquals("startDate", method.getParameters()[0].getAnnotation(Param.class).value());
        assertTrue(method.getParameters()[1].isAnnotationPresent(Param.class));
        assertEquals("endDate", method.getParameters()[1].getAnnotation(Param.class).value());
    }

    @Test
    @DisplayName("searchByNameメソッドのテスト")
    void testSearchByNameMethod() throws NoSuchMethodException {
        Method method = ProjectMapper.class.getMethod("searchByName", String.class);
        
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Select.class));
        
        // パラメータアノテーションのチェック
        assertTrue(method.getParameters()[0].isAnnotationPresent(Param.class));
        assertEquals("namePattern", method.getParameters()[0].getAnnotation(Param.class).value());
    }

    @Test
    @DisplayName("insertメソッドのテスト")
    void testInsertMethod() throws NoSuchMethodException {
        Method method = ProjectMapper.class.getMethod("insert",
            String.class, String.class, String.class, String.class,
            LocalDate.class, LocalDate.class, String.class, LocalDateTime.class,
            LocalDateTime.class, String.class, String.class);

        assertNotNull(method);
        assertEquals(void.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Insert.class));

        // 全パラメータに@Paramアノテーションがあることをチェック
        assertEquals(11, method.getParameterCount());
        for (int i = 0; i < method.getParameterCount(); i++) {
            assertTrue(method.getParameters()[i].isAnnotationPresent(Param.class),
                      "パラメータ " + i + " は@Paramアノテーションを持つべき");
        }
    }

    @Test
    @DisplayName("updateメソッドのテスト")
    void testUpdateMethod() throws NoSuchMethodException {
        Method method = ProjectMapper.class.getMethod("update",
            String.class, String.class, String.class, String.class, LocalDate.class,
            LocalDate.class, String.class, String.class, LocalDateTime.class);

        assertNotNull(method);
        assertEquals(int.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Update.class));

        // 全パラメータに@Paramアノテーションがあることをチェック
        assertEquals(9, method.getParameterCount());
        for (int i = 0; i < method.getParameterCount(); i++) {
            assertTrue(method.getParameters()[i].isAnnotationPresent(Param.class),
                      "パラメータ " + i + " は@Paramアノテーションを持つべき");
        }
    }

    @Test
    @DisplayName("updateStatusメソッドのテスト")
    void testUpdateStatusMethod() throws NoSuchMethodException {
        Method method = ProjectMapper.class.getMethod("updateStatus", 
            String.class, String.class, LocalDateTime.class);
        
        assertNotNull(method);
        assertEquals(int.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Update.class));
        
        // 全パラメータに@Paramアノテーションがあることをチェック
        assertEquals(3, method.getParameterCount());
        for (int i = 0; i < method.getParameterCount(); i++) {
            assertTrue(method.getParameters()[i].isAnnotationPresent(Param.class), 
                      "パラメータ " + i + " は@Paramアノテーションを持つべき");
        }
    }

    @Test
    @DisplayName("softDeleteメソッドのテスト")
    void testSoftDeleteMethod() throws NoSuchMethodException {
        Method method = ProjectMapper.class.getMethod("softDelete", 
            String.class, LocalDateTime.class, LocalDateTime.class);
        
        assertNotNull(method);
        assertEquals(int.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Update.class));
        
        // 全パラメータに@Paramアノテーションがあることをチェック
        assertEquals(3, method.getParameterCount());
        for (int i = 0; i < method.getParameterCount(); i++) {
            assertTrue(method.getParameters()[i].isAnnotationPresent(Param.class), 
                      "パラメータ " + i + " は@Paramアノテーションを持つべき");
        }
    }

    @Test
    @DisplayName("existsByNameメソッドのテスト")
    void testExistsByNameMethod() throws NoSuchMethodException {
        Method method = ProjectMapper.class.getMethod("existsByName", String.class);
        
        assertNotNull(method);
        assertEquals(boolean.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Select.class));
        
        // パラメータアノテーションのチェック
        assertTrue(method.getParameters()[0].isAnnotationPresent(Param.class));
        assertEquals("name", method.getParameters()[0].getAnnotation(Param.class).value());
    }

    @Test
    @DisplayName("existsByIdメソッドのテスト")
    void testExistsByIdMethod() throws NoSuchMethodException {
        Method method = ProjectMapper.class.getMethod("existsById", String.class);
        
        assertNotNull(method);
        assertEquals(boolean.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Select.class));
        
        // パラメータアノテーションのチェック
        assertTrue(method.getParameters()[0].isAnnotationPresent(Param.class));
        assertEquals("id", method.getParameters()[0].getAnnotation(Param.class).value());
    }

    @Test
    @DisplayName("countメソッドのテスト")
    void testCountMethod() throws NoSuchMethodException {
        Method method = ProjectMapper.class.getMethod("count");
        
        assertNotNull(method);
        assertEquals(long.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Select.class));
    }

    @Test
    @DisplayName("countByStatusメソッドのテスト")
    void testCountByStatusMethod() throws NoSuchMethodException {
        Method method = ProjectMapper.class.getMethod("countByStatus", String.class);
        
        assertNotNull(method);
        assertEquals(long.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Select.class));
        
        // パラメータアノテーションのチェック
        assertTrue(method.getParameters()[0].isAnnotationPresent(Param.class));
        assertEquals("status", method.getParameters()[0].getAnnotation(Param.class).value());
    }

    @Test
    @DisplayName("全メソッドが適切な戻り値型を持つ")
    void testAllMethodsHaveProperReturnTypes() {
        Method[] methods = ProjectMapper.class.getDeclaredMethods();
        
        for (Method method : methods) {
            // メソッドが適切でないnullを返さないことを確認
            assertNotNull(method.getReturnType(), 
                         "メソッド " + method.getName() + " は有効な戻り値型を持つべき");
            
            // クエリメソッドは適切な型を返すべき
            if (method.getName().startsWith("find")) {
                assertTrue(Optional.class.equals(method.getReturnType()) || 
                          List.class.equals(method.getReturnType()),
                          "Findメソッドはオプショナルまたはリストを返すべき");
            }
            
            if (method.getName().startsWith("exists") || method.getName().startsWith("count")) {
                assertTrue(method.getReturnType().isPrimitive(),
                          "Exists/countメソッドはプリミティブ型を返すべき");
            }
        }
    }

    @Test
    @DisplayName("メソッドはチェック例外を宣言しない")
    void testNoMethodThrowsCheckedExceptions() {
        Method[] methods = ProjectMapper.class.getDeclaredMethods();
        
        for (Method method : methods) {
            assertEquals(0, method.getExceptionTypes().length,
                        "MyBatisマッパーメソッドはチェック例外を宣言すべきでない: " + method.getName());
        }
    }

    @Test
    @DisplayName("全メソッドに適切なアノテーションが付いている")
    void testAllMethodsHaveProperAnnotations() {
        Method[] methods = ProjectMapper.class.getDeclaredMethods();
        
        for (Method method : methods) {
            // @Deprecatedメソッド、デフォルトメソッド、合成メソッドは除外
            if (method.isAnnotationPresent(Deprecated.class) || method.isDefault() || method.isSynthetic()) {
                continue;
            }
            
            // 全メソッドは@Select、@Insert、@Updateのいずれかを持つべき
            boolean hasProperAnnotation = method.isAnnotationPresent(Select.class) ||
                                        method.isAnnotationPresent(Insert.class) ||
                                        method.isAnnotationPresent(Update.class);
            
            assertTrue(hasProperAnnotation, 
                      "メソッド " + method.getName() + " は適切なMyBatisアノテーションを持つべき");
        }
    }
}