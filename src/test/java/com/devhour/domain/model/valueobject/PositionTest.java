package com.devhour.domain.model.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 役職値オブジェクトテスト
 */
@DisplayName("役職値オブジェクト")
class PositionTest {

    @Test
    @DisplayName("各役職の値とレベル確認")
    void positions_ValuesAndLevels_AreCorrect() {
        // Assert
        assertThat(Position.EMPLOYEE.getValue()).isEqualTo("EMPLOYEE");
        assertThat(Position.EMPLOYEE.getJapaneseName()).isEqualTo("一般社員");
        assertThat(Position.EMPLOYEE.getHierarchyLevel()).isEqualTo(0);

        assertThat(Position.MANAGER.getValue()).isEqualTo("MANAGER");
        assertThat(Position.MANAGER.getJapaneseName()).isEqualTo("マネージャー");
        assertThat(Position.MANAGER.getHierarchyLevel()).isEqualTo(1);

        assertThat(Position.DEPARTMENT_MANAGER.getValue()).isEqualTo("DEPARTMENT_MANAGER");
        assertThat(Position.DEPARTMENT_MANAGER.getJapaneseName()).isEqualTo("部長");
        assertThat(Position.DEPARTMENT_MANAGER.getHierarchyLevel()).isEqualTo(2);

        assertThat(Position.DIVISION_MANAGER.getValue()).isEqualTo("DIVISION_MANAGER");
        assertThat(Position.DIVISION_MANAGER.getJapaneseName()).isEqualTo("本部長");
        assertThat(Position.DIVISION_MANAGER.getHierarchyLevel()).isEqualTo(3);

        assertThat(Position.GENERAL_MANAGER.getValue()).isEqualTo("GENERAL_MANAGER");
        assertThat(Position.GENERAL_MANAGER.getJapaneseName()).isEqualTo("統括本部長");
        assertThat(Position.GENERAL_MANAGER.getHierarchyLevel()).isEqualTo(4);
    }

    @Test
    @DisplayName("fromValue - 正常ケース")
    void fromValue_ValidValue_ReturnsCorrectPosition() {
        // Act & Assert
        assertThat(Position.fromValue("EMPLOYEE")).isEqualTo(Position.EMPLOYEE);
        assertThat(Position.fromValue("MANAGER")).isEqualTo(Position.MANAGER);
        assertThat(Position.fromValue("DEPARTMENT_MANAGER")).isEqualTo(Position.DEPARTMENT_MANAGER);
        assertThat(Position.fromValue("DIVISION_MANAGER")).isEqualTo(Position.DIVISION_MANAGER);
        assertThat(Position.fromValue("GENERAL_MANAGER")).isEqualTo(Position.GENERAL_MANAGER);
    }

    @Test
    @DisplayName("fromValue - nullの場合")
    void fromValue_NullValue_ReturnsNull() {
        // Act & Assert
        assertThat(Position.fromValue(null)).isNull();
    }

    @Test
    @DisplayName("fromValue - 不正な値の場合は例外")
    void fromValue_InvalidValue_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> Position.fromValue("INVALID_POSITION"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid position value: INVALID_POSITION");
    }

    @Test
    @DisplayName("fromJapaneseName - 正常ケース")
    void fromJapaneseName_ValidName_ReturnsCorrectPosition() {
        // Act & Assert
        assertThat(Position.fromJapaneseName("一般社員")).isEqualTo(Position.EMPLOYEE);
        assertThat(Position.fromJapaneseName("マネージャー")).isEqualTo(Position.MANAGER);
        assertThat(Position.fromJapaneseName("部長")).isEqualTo(Position.DEPARTMENT_MANAGER);
        assertThat(Position.fromJapaneseName("本部長")).isEqualTo(Position.DIVISION_MANAGER);
        assertThat(Position.fromJapaneseName("統括本部長")).isEqualTo(Position.GENERAL_MANAGER);
    }

    @Test
    @DisplayName("fromJapaneseName - nullの場合")
    void fromJapaneseName_NullName_ReturnsNull() {
        // Act & Assert
        assertThat(Position.fromJapaneseName(null)).isNull();
    }

    @Test
    @DisplayName("fromJapaneseName - 不正な値の場合は例外")
    void fromJapaneseName_InvalidName_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> Position.fromJapaneseName("不正な役職"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid Japanese position name: 不正な役職");
    }

    @Test
    @DisplayName("役職判定メソッド - 各役職の判定")
    void positionChecks_AllPositions_WorkCorrectly() {
        // Employee
        assertThat(Position.EMPLOYEE.isEmployee()).isTrue();
        assertThat(Position.EMPLOYEE.isManager()).isFalse();
        assertThat(Position.EMPLOYEE.isDepartmentManager()).isFalse();
        assertThat(Position.EMPLOYEE.isDivisionManager()).isFalse();
        assertThat(Position.EMPLOYEE.isGeneralManager()).isFalse();

        // Manager
        assertThat(Position.MANAGER.isEmployee()).isFalse();
        assertThat(Position.MANAGER.isManager()).isTrue();
        assertThat(Position.MANAGER.isDepartmentManager()).isFalse();
        assertThat(Position.MANAGER.isDivisionManager()).isFalse();
        assertThat(Position.MANAGER.isGeneralManager()).isFalse();

        // Department Manager
        assertThat(Position.DEPARTMENT_MANAGER.isEmployee()).isFalse();
        assertThat(Position.DEPARTMENT_MANAGER.isManager()).isFalse();
        assertThat(Position.DEPARTMENT_MANAGER.isDepartmentManager()).isTrue();
        assertThat(Position.DEPARTMENT_MANAGER.isDivisionManager()).isFalse();
        assertThat(Position.DEPARTMENT_MANAGER.isGeneralManager()).isFalse();

        // Division Manager
        assertThat(Position.DIVISION_MANAGER.isEmployee()).isFalse();
        assertThat(Position.DIVISION_MANAGER.isManager()).isFalse();
        assertThat(Position.DIVISION_MANAGER.isDepartmentManager()).isFalse();
        assertThat(Position.DIVISION_MANAGER.isDivisionManager()).isTrue();
        assertThat(Position.DIVISION_MANAGER.isGeneralManager()).isFalse();

        // General Manager
        assertThat(Position.GENERAL_MANAGER.isEmployee()).isFalse();
        assertThat(Position.GENERAL_MANAGER.isManager()).isFalse();
        assertThat(Position.GENERAL_MANAGER.isDepartmentManager()).isFalse();
        assertThat(Position.GENERAL_MANAGER.isDivisionManager()).isFalse();
        assertThat(Position.GENERAL_MANAGER.isGeneralManager()).isTrue();
    }

    @Test
    @DisplayName("hasApprovalAuthority - 一般社員は承認権限なし")
    void hasApprovalAuthority_Employee_ReturnsFalse() {
        // Act & Assert
        assertThat(Position.EMPLOYEE.hasApprovalAuthority()).isFalse();
    }

    @Test
    @DisplayName("hasApprovalAuthority - マネージャー以上は承認権限あり")
    void hasApprovalAuthority_ManagerOrAbove_ReturnsTrue() {
        // Act & Assert
        assertThat(Position.MANAGER.hasApprovalAuthority()).isTrue();
        assertThat(Position.DEPARTMENT_MANAGER.hasApprovalAuthority()).isTrue();
        assertThat(Position.DIVISION_MANAGER.hasApprovalAuthority()).isTrue();
        assertThat(Position.GENERAL_MANAGER.hasApprovalAuthority()).isTrue();
    }

    @Test
    @DisplayName("isHigherThan - 階層比較")
    void isHigherThan_HierarchyComparison_WorksCorrectly() {
        // Act & Assert
        // Manager > Employee
        assertThat(Position.MANAGER.isHigherThan(Position.EMPLOYEE)).isTrue();
        assertThat(Position.EMPLOYEE.isHigherThan(Position.MANAGER)).isFalse();

        // Department Manager > Manager > Employee
        assertThat(Position.DEPARTMENT_MANAGER.isHigherThan(Position.MANAGER)).isTrue();
        assertThat(Position.DEPARTMENT_MANAGER.isHigherThan(Position.EMPLOYEE)).isTrue();
        assertThat(Position.MANAGER.isHigherThan(Position.DEPARTMENT_MANAGER)).isFalse();

        // Division Manager > Department Manager
        assertThat(Position.DIVISION_MANAGER.isHigherThan(Position.DEPARTMENT_MANAGER)).isTrue();
        assertThat(Position.DEPARTMENT_MANAGER.isHigherThan(Position.DIVISION_MANAGER)).isFalse();

        // General Manager > Division Manager
        assertThat(Position.GENERAL_MANAGER.isHigherThan(Position.DIVISION_MANAGER)).isTrue();
        assertThat(Position.DIVISION_MANAGER.isHigherThan(Position.GENERAL_MANAGER)).isFalse();

        // 同じ階層の場合
        assertThat(Position.MANAGER.isHigherThan(Position.MANAGER)).isFalse();
    }

    @Test
    @DisplayName("isHigherOrEqualTo - 階層比較（同等含む）")
    void isHigherOrEqualTo_HierarchyComparison_WorksCorrectly() {
        // Act & Assert
        // Manager >= Employee
        assertThat(Position.MANAGER.isHigherOrEqualTo(Position.EMPLOYEE)).isTrue();
        assertThat(Position.EMPLOYEE.isHigherOrEqualTo(Position.MANAGER)).isFalse();

        // 同じ階層の場合
        assertThat(Position.MANAGER.isHigherOrEqualTo(Position.MANAGER)).isTrue();
        assertThat(Position.EMPLOYEE.isHigherOrEqualTo(Position.EMPLOYEE)).isTrue();

        // General Manager >= all others
        assertThat(Position.GENERAL_MANAGER.isHigherOrEqualTo(Position.EMPLOYEE)).isTrue();
        assertThat(Position.GENERAL_MANAGER.isHigherOrEqualTo(Position.MANAGER)).isTrue();
        assertThat(Position.GENERAL_MANAGER.isHigherOrEqualTo(Position.DEPARTMENT_MANAGER)).isTrue();
        assertThat(Position.GENERAL_MANAGER.isHigherOrEqualTo(Position.DIVISION_MANAGER)).isTrue();
        assertThat(Position.GENERAL_MANAGER.isHigherOrEqualTo(Position.GENERAL_MANAGER)).isTrue();
    }

    @Test
    @DisplayName("toString - 値を返すこと")
    void toString_ReturnsValue() {
        // Act & Assert
        assertThat(Position.EMPLOYEE.toString()).isEqualTo("EMPLOYEE");
        assertThat(Position.MANAGER.toString()).isEqualTo("MANAGER");
        assertThat(Position.DEPARTMENT_MANAGER.toString()).isEqualTo("DEPARTMENT_MANAGER");
        assertThat(Position.DIVISION_MANAGER.toString()).isEqualTo("DIVISION_MANAGER");
        assertThat(Position.GENERAL_MANAGER.toString()).isEqualTo("GENERAL_MANAGER");
    }
}