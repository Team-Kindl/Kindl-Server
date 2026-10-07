-- Kindl 1단계 스키마
-- 규칙
--  * PK는 앱이 만든 TSID. API로 나가는 테이블은 VARCHAR(13) ascii_bin, 내부 전용은 BIGINT. AUTO_INCREMENT 없음
--  * 시각은 DATETIME(6) UTC, FK 제약 없음 (참조 컬럼마다 인덱스)
--  * soft delete 테이블의 유니크는 생성 컬럼 active(살아 있으면 1, 지워지면 NULL)를 끝에 붙여 살아 있는 행에만 건다

-- ========== user ==========
CREATE TABLE users (
    id                    VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    nickname              VARCHAR(60)  NOT NULL COMMENT '표시용 NFC. 규칙은 그래핌 15개',
    nickname_key          VARCHAR(60)  NOT NULL COMMENT '중복 판정용 NFKC + 소문자',
    profile_image_file_id VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '2단계',
    timezone              VARCHAR(40)  NOT NULL COMMENT 'IANA, 예: Asia/Seoul',
    next_timezone         VARCHAR(40)  NULL,
    next_timezone_from    DATE         NULL COMMENT '이 날짜부터 timezone으로 승격',
    locale                VARCHAR(35)  NOT NULL COMMENT 'BCP 47, 예: ko-KR. 서버발 문구(푸시)용',
    current_streak        INT          NOT NULL DEFAULT 0,
    best_streak           INT          NOT NULL DEFAULT 0,
    last_streak_date      DATE         NULL,
    onboarded_at          DATETIME(6)  NULL,
    terms_version         VARCHAR(20)  NOT NULL,
    terms_agreed_at       DATETIME(6)  NOT NULL,
    created_at            DATETIME(6)  NOT NULL,
    updated_at            DATETIME(6)  NOT NULL,
    deleted_at            DATETIME(6)  NULL,
    active                TINYINT AS (IF(deleted_at IS NULL, 1, NULL)) STORED,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_nickname (nickname_key, active)
);

CREATE TABLE devices (
    id                      BIGINT       NOT NULL,
    user_id                 VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    installation_id         VARCHAR(64)  NOT NULL COMMENT '앱 설치마다 앱이 만든 UUID',
    platform                VARCHAR(20)  NOT NULL,
    os_version              VARCHAR(20)  NOT NULL,
    app_version             VARCHAR(20)  NOT NULL,
    push_token              VARCHAR(255) NULL COMMENT '3단계',
    last_seen_at            DATETIME(6)  NOT NULL,
    created_at              DATETIME(6)  NOT NULL,
    updated_at              DATETIME(6)  NOT NULL,
    deleted_at              DATETIME(6)  NULL,
    active                  TINYINT AS (IF(deleted_at IS NULL, 1, NULL)) STORED,
    PRIMARY KEY (id),
    UNIQUE KEY uk_device (user_id, installation_id, active)
);

CREATE TABLE user_withdrawals (
    id            BIGINT       NOT NULL,
    user_id       VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    reason        VARCHAR(20)  NOT NULL,
    reason_detail VARCHAR(300) NULL COMMENT 'OTHER일 때',
    created_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY ix_withdrawal_user (user_id)
);

-- ========== auth ==========
CREATE TABLE social_accounts (
    id                BIGINT          NOT NULL,
    user_id           VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    provider          VARCHAR(20)     NOT NULL,
    provider_user_id  VARCHAR(255)    NOT NULL COMMENT 'Kakao 회원번호 / Apple·Google sub',
    email             VARCHAR(320)    NULL COMMENT '받은 경우만. API로 내보내지 않음',
    revoke_credential VARBINARY(1024) NULL COMMENT '탈퇴 시 연결 해제용, 암호화',
    created_at        DATETIME(6)     NOT NULL,
    updated_at        DATETIME(6)     NOT NULL,
    deleted_at        DATETIME(6)     NULL,
    active            TINYINT AS (IF(deleted_at IS NULL, 1, NULL)) STORED,
    PRIMARY KEY (id),
    UNIQUE KEY uk_social (provider, provider_user_id, active),
    KEY ix_social_user (user_id)
);

CREATE TABLE refresh_tokens (
    id             BIGINT      NOT NULL,
    family_id      BIGINT      NOT NULL COMMENT '로그인 한 번에서 이어진 회전 묶음',
    user_id        VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    device_id      BIGINT      NOT NULL,
    token_hash     CHAR(64) CHARACTER SET ascii NOT NULL COMMENT 'SHA-256, 원문 저장 안 함',
    expires_at     DATETIME(6) NOT NULL,
    rotated_at     DATETIME(6) NULL COMMENT '새 토큰으로 바뀐 시각. 재사용 유예 판단 기준',
    replaced_by_id BIGINT      NULL,
    revoked_at     DATETIME(6) NULL,
    created_at     DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_hash (token_hash),
    KEY ix_refresh_family (family_id),
    KEY ix_refresh_user (user_id)
);

-- ========== room ==========
CREATE TABLE rooms (
    id             VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    name           VARCHAR(60) NOT NULL COMMENT '규칙은 그래핌 15개',
    image_file_id  VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NULL,
    invite_code    CHAR(8) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '0 O 1 I 제외 32자',
    owner_user_id  VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    member_count   TINYINT     NOT NULL COMMENT '정원 10 검사용. 입장·퇴장 때 행 잠금 후 갱신',
    status         VARCHAR(20) NOT NULL,
    ended_at       DATETIME(6) NULL,
    final_avg_rate TINYINT     NULL COMMENT '종료 시점 모임 평균 스냅샷',
    created_at     DATETIME(6) NOT NULL,
    updated_at     DATETIME(6) NOT NULL,
    deleted_at     DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_rooms_code (invite_code), -- active 없음: 지워진 모임의 코드도 재사용하지 않는다
    KEY ix_rooms_owner (owner_user_id)
);

CREATE TABLE room_members (
    id               BIGINT      NOT NULL,
    room_id          VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    user_id          VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    role             VARCHAR(20) NOT NULL,
    joined_at        DATETIME(6) NOT NULL COMMENT '모임장 승계 순서',
    last_verified_at DATETIME(6) NULL COMMENT '모임 목록 정렬용',
    created_at       DATETIME(6) NOT NULL,
    updated_at       DATETIME(6) NOT NULL,
    deleted_at       DATETIME(6) NULL,
    active           TINYINT AS (IF(deleted_at IS NULL, 1, NULL)) STORED,
    PRIMARY KEY (id),
    UNIQUE KEY uk_member (room_id, user_id, active),
    KEY ix_member_user (user_id, active)
);

CREATE TABLE room_bans (
    id                BIGINT      NOT NULL,
    room_id           VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    user_id           VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    banned_by_user_id VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    created_at        DATETIME(6) NOT NULL,
    updated_at        DATETIME(6) NOT NULL,
    deleted_at        DATETIME(6) NULL,
    active            TINYINT AS (IF(deleted_at IS NULL, 1, NULL)) STORED,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ban (room_id, user_id, active)
);

-- ========== promise ==========
CREATE TABLE promises (
    id                 VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    room_id            VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    user_id            VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    title              VARCHAR(90) NOT NULL COMMENT '규칙은 그래핌 4~30개',
    cycle_type         VARCHAR(20) NOT NULL,
    per_day_count      TINYINT     NOT NULL,
    week_days_count    TINYINT     NULL,
    start_date         DATE        NOT NULL COMMENT '유저 시간대 기준. 주 단위는 보정된 시작일',
    end_date           DATE        NOT NULL COMMENT '포함',
    ends_at            DATETIME(6) NOT NULL COMMENT 'end_date 다음 날 00:00(유저 시간대)의 UTC. 종료 배치 기준',
    methods            VARCHAR(20) NOT NULL COMMENT 'PHOTO,TEXT 집합',
    status             VARCHAR(20) NOT NULL,
    target_count       INT         NOT NULL,
    approved_count     INT         NOT NULL DEFAULT 0,
    current_streak     INT         NOT NULL DEFAULT 0,
    last_streak_period DATE        NULL,
    edit_count         TINYINT     NOT NULL DEFAULT 0,
    edit_limit         TINYINT     NOT NULL DEFAULT 3,
    edit_bonus_granted BOOLEAN     NOT NULL DEFAULT FALSE,
    alarm_enabled      BOOLEAN     NOT NULL DEFAULT TRUE,
    alarm_time         TIME        NOT NULL DEFAULT '08:00:00' COMMENT '유저 시간대의 벽시계 시각',
    alarm_days         TINYINT     NOT NULL DEFAULT 0 COMMENT '요일 비트마스크, 0이면 매일',
    review_id          VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '4단계',
    stopped_at         DATETIME(6) NULL,
    stop_reason        VARCHAR(20) NULL,
    completed_at       DATETIME(6) NULL,
    created_at         DATETIME(6) NOT NULL,
    updated_at         DATETIME(6) NOT NULL,
    deleted_at         DATETIME(6) NULL,
    PRIMARY KEY (id),
    KEY ix_promise_user (user_id, status),
    KEY ix_promise_room (room_id, status),
    KEY ix_promise_ends (status, ends_at) -- 매시 종료 배치
);

CREATE TABLE promise_daily_records (
    id             BIGINT      NOT NULL,
    promise_id     VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    user_id        VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    service_date   DATE        NOT NULL COMMENT '유저 시간대 기준 그날',
    week_start     DATE        NOT NULL COMMENT '그 주 월요일 (ISO 주)',
    approved_count TINYINT     NOT NULL DEFAULT 0,
    created_at     DATETIME(6) NOT NULL,
    updated_at     DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_daily (promise_id, service_date),
    KEY ix_daily_week (promise_id, week_start)
);

-- ========== verification ==========
CREATE TABLE verifications (
    id                VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    promise_id        VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    room_id           VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '모임 화면 조회용 비정규화',
    user_id           VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    client_request_id VARCHAR(36)  NOT NULL COMMENT '앱이 만든 Idempotency-Key',
    status            VARCHAR(20)  NOT NULL,
    service_date      DATE         NULL COMMENT '요청 시각 기준, 유저 시간대',
    requested_at      DATETIME(6)  NOT NULL,
    reviewed_at       DATETIME(6)  NULL,
    note              VARCHAR(120) NULL COMMENT '한 줄 기록, 규칙은 그래핌 40개',
    submit_count      TINYINT      NOT NULL DEFAULT 1,
    appeal_status     VARCHAR(20)  NOT NULL DEFAULT 'NONE' COMMENT '4단계',
    created_at        DATETIME(6)  NOT NULL,
    updated_at        DATETIME(6)  NOT NULL,
    deleted_at        DATETIME(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_verif_request (user_id, client_request_id),
    KEY ix_verif_promise (promise_id, id), -- TSID가 시간순이라 id 커서 = 최신순
    KEY ix_verif_room (room_id, id),
    KEY ix_verif_user_day (user_id, service_date)
);

CREATE TABLE verification_items (
    id              BIGINT       NOT NULL,
    verification_id VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    method          VARCHAR(20)  NOT NULL,
    text_content    VARCHAR(900) NULL COMMENT '규칙은 그래핌 300개',
    file_id         VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '2단계',
    result          VARCHAR(20)  NOT NULL,
    reject_reason   VARCHAR(300) NULL,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    deleted_at      DATETIME(6)  NULL,
    active          TINYINT AS (IF(deleted_at IS NULL, 1, NULL)) STORED,
    PRIMARY KEY (id),
    UNIQUE KEY uk_item (verification_id, method, active)
);

CREATE TABLE cheers (
    id               BIGINT      NOT NULL,
    verification_id  VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    sender_user_id   VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    receiver_user_id VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '받은 응원 집계·알림용 비정규화',
    type             VARCHAR(20) NOT NULL,
    created_at       DATETIME(6) NOT NULL,
    updated_at       DATETIME(6) NOT NULL,
    deleted_at       DATETIME(6) NULL,
    active           TINYINT AS (IF(deleted_at IS NULL, 1, NULL)) STORED,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cheer (verification_id, sender_user_id, active), -- 바꾸기 = type UPDATE
    KEY ix_cheer_receiver (receiver_user_id)
);

CREATE TABLE reports (
    id               BIGINT      NOT NULL,
    verification_id  VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    reporter_user_id VARCHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    reason           VARCHAR(20) NOT NULL,
    status           VARCHAR(20) NOT NULL,
    handled_at       DATETIME(6) NULL,
    handled_by       VARCHAR(64) NULL COMMENT '처리한 운영자',
    created_at       DATETIME(6) NOT NULL,
    updated_at       DATETIME(6) NOT NULL,
    deleted_at       DATETIME(6) NULL,
    active           TINYINT AS (IF(deleted_at IS NULL, 1, NULL)) STORED,
    PRIMARY KEY (id),
    UNIQUE KEY uk_report (verification_id, reporter_user_id, active),
    KEY ix_report_status (status, id)
);

-- ========== support ==========
CREATE TABLE outbox_events (
    id              BIGINT       NOT NULL,
    aggregate_type  VARCHAR(30)  NOT NULL COMMENT '예: USER',
    aggregate_id    VARCHAR(13)  NOT NULL,
    event_type      VARCHAR(40)  NOT NULL COMMENT '예: UNLINK_SOCIAL',
    payload         JSON         NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    attempts        TINYINT      NOT NULL DEFAULT 0,
    next_attempt_at DATETIME(6)  NOT NULL,
    last_error      VARCHAR(500) NULL,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY ix_outbox_due (status, next_attempt_at) -- SELECT … FOR UPDATE SKIP LOCKED
);
