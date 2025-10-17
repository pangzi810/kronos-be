package com.devhour.domain.exception;

/**
 * 承認権限が存在しない場合の例外
 */
public class NoApprovalAuthorityException extends RuntimeException {

    private final String approverId;
    private final String targetId;

    public NoApprovalAuthorityException(String approverId, String targetId) {
        super(String.format("承認権限が存在しません: approverId=%s, targetId=%s", approverId, targetId));
        this.approverId = approverId;
        this.targetId = targetId;
    }

    public String getApproverId() {
        return approverId;
    }

    public String getTargetId() {
        return targetId;
    }
}