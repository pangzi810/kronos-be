package com.devhour.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 役職値オブジェクト
 * 
 * 承認権限における組織内の役職を表現する列挙型
 */
public enum Position {
    
    /**
     * 一般社員 - 承認権限なし
     */
    EMPLOYEE("EMPLOYEE", "一般社員", 0),
    
    /**
     * マネージャー - 基本的な承認権限あり
     */
    MANAGER("MANAGER", "マネージャー", 1),
    
    /**
     * 部長 - 部門レベルの承認権限
     */
    DEPARTMENT_MANAGER("DEPARTMENT_MANAGER", "部長", 2),
    
    /**
     * 本部長 - 本部レベルの承認権限
     */
    DIVISION_MANAGER("DIVISION_MANAGER", "本部長", 3),
    
    /**
     * 統括本部長 - 最上位の承認権限
     */
    GENERAL_MANAGER("GENERAL_MANAGER", "統括本部長", 4);
    
    private final String value;
    private final String japaneseName;
    private final int hierarchyLevel;
    
    Position(String value, String japaneseName, int hierarchyLevel) {
        this.value = value;
        this.japaneseName = japaneseName;
        this.hierarchyLevel = hierarchyLevel;
    }
    
    /**
     * データベース保存用の値を取得
     * 
     * @return 役職値
     */
    @JsonValue
    public String getValue() {
        return value;
    }
    
    /**
     * 日本語名を取得
     * 
     * @return 日本語での役職名
     */
    public String getJapaneseName() {
        return japaneseName;
    }
    
    /**
     * 階層レベルを取得
     * 
     * @return 階層レベル（0が最下位、数値が大きいほど上位）
     */
    public int getHierarchyLevel() {
        return hierarchyLevel;
    }
    
    /**
     * 文字列からPositionを取得
     * 
     * @param value 役職文字列
     * @return Position
     * @throws IllegalArgumentException 不正な値の場合
     */
    public static Position fromValue(String value) {
        if (value == null) {
            return null;
        }
        
        for (Position position : Position.values()) {
            if (position.value.equals(value)) {
                return position;
            }
        }
        
        throw new IllegalArgumentException(
            "Invalid position value: " + value);
    }
    
    /**
     * 日本語名からPositionを取得
     * 
     * @param japaneseName 日本語での役職名
     * @return Position
     * @throws IllegalArgumentException 不正な値の場合
     */
    public static Position fromJapaneseName(String japaneseName) {
        if (japaneseName == null) {
            return null;
        }
        
        for (Position position : Position.values()) {
            if (position.japaneseName.equals(japaneseName)) {
                return position;
            }
        }
        
        throw new IllegalArgumentException(
            "Invalid Japanese position name: " + japaneseName);
    }
    
    /**
     * 一般社員かを判定
     * 
     * @return 一般社員の場合true
     */
    public boolean isEmployee() {
        return this == EMPLOYEE;
    }
    
    /**
     * マネージャーかを判定
     * 
     * @return マネージャーの場合true
     */
    public boolean isManager() {
        return this == MANAGER;
    }
    
    /**
     * 部長かを判定
     * 
     * @return 部長の場合true
     */
    public boolean isDepartmentManager() {
        return this == DEPARTMENT_MANAGER;
    }
    
    /**
     * 本部長かを判定
     * 
     * @return 本部長の場合true
     */
    public boolean isDivisionManager() {
        return this == DIVISION_MANAGER;
    }
    
    /**
     * 統括本部長かを判定
     * 
     * @return 統括本部長の場合true
     */
    public boolean isGeneralManager() {
        return this == GENERAL_MANAGER;
    }
    
    /**
     * 承認権限を持つ役職かを判定
     * 一般社員以外は承認権限あり
     * 
     * @return 承認権限がある場合true
     */
    public boolean hasApprovalAuthority() {
        return this != EMPLOYEE;
    }
    
    /**
     * 指定した役職より上位かを判定
     * 
     * @param other 比較対象の役職
     * @return 上位の場合true
     */
    public boolean isHigherThan(Position other) {
        return this.hierarchyLevel > other.hierarchyLevel;
    }
    
    /**
     * 指定した役職以上かを判定
     * 
     * @param other 比較対象の役職
     * @return 同じかより上位の場合true
     */
    public boolean isHigherOrEqualTo(Position other) {
        return this.hierarchyLevel >= other.hierarchyLevel;
    }
    
    @Override
    public String toString() {
        return value;
    }
}