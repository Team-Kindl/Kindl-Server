-- 모임 만들기 멱등 키 (앱이 만든 Idempotency-Key, UUID 36자)
-- 버튼 연타·네트워크 재시도에도 모임이 하나만 생기도록 모임 행과 같은 트랜잭션에 저장한다
ALTER TABLE rooms
    ADD COLUMN client_request_id VARCHAR(36) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '앱이 만든 Idempotency-Key' AFTER final_avg_rate,
    ADD UNIQUE KEY uk_rooms_request (client_request_id);
