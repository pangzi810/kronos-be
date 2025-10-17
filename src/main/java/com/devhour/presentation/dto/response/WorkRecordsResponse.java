package com.devhour.presentation.dto.response;

import com.devhour.domain.model.entity.WorkRecord;
import com.devhour.domain.model.entity.WorkRecordApproval;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 工数記録と承認ステータスを含むレスポンスDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkRecordsResponse {
    /**
     * 工数記録のリスト
     */
    private List<WorkRecord> workRecords;
    
    /**
     * 対象日付の承認ステータス（存在しない場合はnull）
     */
    private WorkRecordApproval workRecordApproval;
}