package com.devhour.config;

import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

import com.devhour.infrastructure.typehandler.CategoryHoursTypeHandler;
import com.devhour.infrastructure.typehandler.CategoryHoursValueObjectTypeHandler;
import com.devhour.domain.model.valueobject.CategoryHours;

import jakarta.annotation.PostConstruct;

/**
 * MyBatis設定クラス
 * - TypeHandlerの登録
 * - Mapper インターフェースのスキャン設定
 * - カスタム設定の適用
 */
@Configuration
@MapperScan(basePackages = "com.devhour.infrastructure.mapper")
public class MyBatisConfig {

    private final SqlSessionFactory sqlSessionFactory;
    private final CategoryHoursTypeHandler categoryHoursTypeHandler;
    private final CategoryHoursValueObjectTypeHandler categoryHoursValueObjectTypeHandler;

    public MyBatisConfig(SqlSessionFactory sqlSessionFactory, 
                         CategoryHoursTypeHandler categoryHoursTypeHandler,
                         CategoryHoursValueObjectTypeHandler categoryHoursValueObjectTypeHandler) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.categoryHoursTypeHandler = categoryHoursTypeHandler;
        this.categoryHoursValueObjectTypeHandler = categoryHoursValueObjectTypeHandler;
    }

    /**
     * カスタムTypeHandlerを登録
     * アプリケーション起動時にMyBatisにTypeHandlerを登録する
     */
    @PostConstruct
    public void registerTypeHandlers() {
        TypeHandlerRegistry registry = sqlSessionFactory.getConfiguration().getTypeHandlerRegistry();
        
        // CategoryHoursTypeHandlerを登録 (Map<String, BigDecimal>用)
        registry.register(Map.class, categoryHoursTypeHandler);
        
        // CategoryHoursValueObjectTypeHandlerを登録 (CategoryHours値オブジェクト用)
        registry.register(CategoryHours.class, categoryHoursValueObjectTypeHandler);
    }
}