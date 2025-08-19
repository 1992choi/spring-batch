/********************************************* Part 1 강의 전 실행 *********************************************/
INSERT INTO payment_source (discount_amount, final_amount, original_amount, partner_corp_name, payment_date)
VALUES (100.00, 900.00, 1000.00, 'Partner Corp 1', '2025-05-01'),
       (150.00, 1850.00, 2000.00, 'Partner Corp 2', '2025-05-01'),
       (200.00, 2800.00, 3000.00, 'Partner Corp 3', '2025-05-01'),
       (250.00, 3750.00, 4000.00, 'Partner Corp 4', '2025-05-01'),
       (300.00, 4700.00, 5000.00, 'Partner Corp 5', '2025-05-01'),
       (350.00, 5650.00, 6000.00, 'Partner Corp 6', '2025-05-01'),
       (400.00, 6600.00, 7000.00, 'Partner Corp 7', '2025-05-01'),
       (450.00, 7550.00, 8000.00, 'Partner Corp 8', '2025-05-01'),
       (500.00, 8500.00, 9000.00, 'Partner Corp 9', '2025-05-01'),
       (550.00, 9450.00, 10000.00, 'Partner Corp 10', '2025-05-01'),
       (600.00, 10400.00, 11000.00, 'Partner Corp 11', '2025-05-01'),
       (650.00, 11350.00, 12000.00, 'Partner Corp 12', '2025-05-01'),
       (700.00, 12300.00, 13000.00, 'Partner Corp 13', '2025-05-01'),
       (750.00, 13250.00, 14000.00, 'Partner Corp 14', '2025-05-01'),
       (800.00, 14200.00, 15000.00, 'Partner Corp 15', '2025-05-01'),
       (850.00, 15150.00, 16000.00, 'Partner Corp 16', '2025-05-01'),
       (900.00, 16100.00, 17000.00, 'Partner Corp 17', '2025-05-01'),
       (950.00, 17050.00, 18000.00, 'Partner Corp 18', '2025-05-01'),
       (1000.00, 18000.00, 19000.00, 'Partner Corp 19', '2025-05-01'),
       (1050.00, 18950.00, 20000.00, 'Partner Corp 20', '2025-05-01')
;

INSERT INTO payment_source (discount_amount, final_amount, original_amount, partner_corp_name, payment_date)
VALUES (100.00, 900.00, 1000.00, 'Partner Corp 1', '2025-05-02'),
       (150.00, 1850.00, 2000.00, 'Partner Corp 2', '2025-05-02'),
       (200.00, 2800.00, 3000.00, 'Partner Corp 3', '2025-05-02'),
       (250.00, 3750.00, 4000.00, 'Partner Corp 4', '2025-05-02'),
       (300.00, 4700.00, 5000.00, 'Partner Corp 5', '2025-05-02'),
       (350.00, 5650.00, 6000.00, 'Partner Corp 6', '2025-05-02'),
       (400.00, 6600.00, 7000.00, 'Partner Corp 7', '2025-05-02'),
       (450.00, 7550.00, 8000.00, 'Partner Corp 8', '2025-05-02'),
       (500.00, 8500.00, 9000.00, 'Partner Corp 9', '2025-05-02'),
       (550.00, 9450.00, 10000.00, 'Partner Corp 10', '2025-05-02'),
       (600.00, 10400.00, 11000.00, 'Partner Corp 11', '2025-05-02'),
       (650.00, 11350.00, 12000.00, 'Partner Corp 12', '2025-05-02'),
       (700.00, 12300.00, 13000.00, 'Partner Corp 13', '2025-05-02'),
       (750.00, 13250.00, 14000.00, 'Partner Corp 14', '2025-05-02'),
       (800.00, 14200.00, 15000.00, 'Partner Corp 15', '2025-05-02'),
       (850.00, 15150.00, 16000.00, 'Partner Corp 16', '2025-05-02'),
       (900.00, 16100.00, 17000.00, 'Partner Corp 17', '2025-05-02'),
       (950.00, 17050.00, 18000.00, 'Partner Corp 18', '2025-05-02'),
       (1000.00, 18000.00, 19000.00, 'Partner Corp 19', '2025-05-02'),
       (1050.00, 18950.00, 20000.00, 'Partner Corp 20', '2025-05-02')
;







/********************************************* Part 2 강의 전 실행 *********************************************/
-- 기존 테이블 drop
drop table payment_source;

show full columns from payment_source;

-- payment_source 테이블 생성
create table payment_source
(
    id                                   bigint auto_increment primary key,
    payment_date                         date           not null,
    discount_amount                      decimal(38, 2) not null,
    final_amount                         decimal(38, 2) not null,
    original_amount                      decimal(38, 2) not null,
    partner_business_registration_number varchar(100)   not null,
    partner_corp_name                    varchar(100)   not null
);

-- payment_date 컬럼에 대한 인덱스 생성 (조회 성능 향상)
create index idx_payment_date on payment_source (payment_date);

-- SQL 클라이언트가 세미콜론(;)을 만나도 프로시저 정의가 끝나지 않도록 구분자를 변경합니다.
DELIMITER $$

-- 만약 이전에 생성된 프로시저가 있다면 삭제합니다.
DROP PROCEDURE IF EXISTS sp_insert_payment_source_batch;

-- 대량의 테스트 데이터를 배치(batch) 형태로 삽입하는 저장 프로시저를 생성합니다.
CREATE PROCEDURE sp_insert_payment_source_batch(IN p_row_count INT)
BEGIN
    -- 루프 및 배치 관련 변수 선언
    DECLARE i INT DEFAULT 1;
    DECLARE v_batch_size INT DEFAULT 1000; -- 한 번에 INSERT할 데이터 묶음 크기
    DECLARE v_sql_values LONGTEXT DEFAULT '';
    -- INSERT할 값들을 누적할 문자열 변수

    -- 데이터 생성을 위한 변수 선언
    DECLARE v_final_amount DECIMAL(38, 2);
    DECLARE v_discount_amount DECIMAL(38, 2);
    DECLARE v_original_amount DECIMAL(38, 2);
    DECLARE v_payment_date DATE;
    DECLARE v_random_index INT;
    DECLARE v_partner_corp_name VARCHAR(100);
    DECLARE v_partner_biz_reg_num VARCHAR(20);

    -- 요청된 row 개수만큼 루프를 실행합니다.
    WHILE i <= p_row_count
        DO
            -- 랜덤 데이터 생성
            SET v_final_amount = ROUND(RAND() * 1000000, 2);
            SET v_discount_amount = ROUND(RAND() * 100000, 2);
            SET v_original_amount = v_final_amount + v_discount_amount;
            -- 강의 영상 코드와 조금 다름
            -- '2020-01-01'부터 '2025-05-01' 사이의 기간 내에서 랜덤한 결제 날짜를 생성합니다.
            -- 날짜 생성 루프: 2025년 내에서 '2025-05-02'를 제외한 랜덤 날짜를 보장합니다.
#             date_generation_loop:
#             LOOP
#                 -- 2025년 1월 1일부터 12월 31일 사이의 랜덤 날짜를 생성합니다. (2025년은 365일)
#                 SET v_payment_date = DATE_ADD('2025-01-01', INTERVAL FLOOR(RAND() * 365) DAY);
#
#                 -- 생성된 날짜가 '2025-05-02'가 아니면 루프를 빠져나갑니다.
#                 IF v_payment_date != '2025-05-02' THEN
#                     LEAVE date_generation_loop;
#                 END IF;
#             END LOOP date_generation_loop;
            # 2025-05-02 으로 고정
            SET v_payment_date = '2025-05-02';
            -- ELT 인덱스는 1부터 시작하며, 20개 항목이 있으므로 * 20
            SET v_random_index = FLOOR(1 + RAND() * 20);
            -- v_random_index를 사용하여 미리 정의된 목록에서 파트너사 이름을 랜덤으로 선택합니다.
            SET v_partner_corp_name = ELT(v_random_index, '삼성전자', 'LG전자', '현대자동차', 'SK텔레콤', '네이버', '카카오', '쿠팡', '배달의민족', '토스', '당근마켓', 'KT', '롯데그룹', '포스코', '신한금융그룹', 'KB금융그룹', '농협', '하나금융그룹', '대한항공', '아시아나항공', 'CJ그룹');
            -- 동일한 v_random_index를 사용하여 위에서 선택된 파트너사 이름과 일치하는 사업자 등록 번호를 선택합니다. (데이터 정합성 유지)
            SET v_partner_biz_reg_num = ELT(v_random_index, '000-01-00001', '000-01-00002', '000-01-00003', '000-01-00004', '000-01-00005', '000-01-00006', '000-01-00007', '000-01-00008', '000-01-00009', '000-01-00010', '000-01-00011', '000-01-00012', '000-01-00013', '000-01-00014', '000-01-00015', '000-01-00016', '000-01-00017', '000-01-00018', '000-01-00019', '000-01-00020');

            -- 생성된 데이터를 VALUES 구문 형식의 문자열로 v_sql_values에 추가합니다.
            -- QUOTE() 함수는 SQL 인젝션 공격을 방지하고 문자열 값을 안전하게 처리합니다.
            SET v_sql_values = CONCAT(v_sql_values,
                                      '(',
                                      v_original_amount, ',',
                                      v_discount_amount, ',',
                                      v_final_amount, ',',
                                      QUOTE(v_payment_date), ',',
                                      QUOTE(v_partner_corp_name), ',',
                                      QUOTE(v_partner_biz_reg_num),
                                      '),'
                               );

            -- 배치 크기(1000개)에 도달했을 경우, 모아둔 데이터를 한 번에 INSERT 합니다.
            IF (i % v_batch_size = 0) THEN
                -- TRIM 함수를 사용하여 마지막에 붙은 불필요한 쉼표(,)를 안전하게 제거합니다.
                SET @sql_query = CONCAT(
                        'INSERT INTO payment_source (original_amount, discount_amount, final_amount, payment_date, partner_corp_name, partner_business_registration_number) VALUES ',
                        TRIM(TRAILING ',' FROM v_sql_values)
                                 );

                -- 동적 SQL을 준비하고 실행합니다.
PREPARE stmt FROM @sql_query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 다음 배치를 위해 VALUES 문자열을 초기화합니다.
SET v_sql_values = '';
END IF;

            SET i = i + 1;
END WHILE;

    -- 루프가 끝난 후, 처리되지 않고 남아있는 데이터가 있다면 마지막으로 INSERT 합니다.
    -- (예: 1001개를 요청한 경우, 마지막 1개가 여기에 해당)
    IF v_sql_values != '' THEN
        SET @sql_query = CONCAT(
                'INSERT INTO payment_source (original_amount, discount_amount, final_amount, payment_date, partner_corp_name, partner_business_registration_number) VALUES ',
                TRIM(TRAILING ',' FROM v_sql_values)
                         );

PREPARE stmt FROM @sql_query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
END IF;

END$$

-- 구분자를 다시 기본값인 세미콜론(;)으로 변경합니다.
DELIMITER ;

-- 10,000건 데이터 삽입
CALL sp_insert_payment_source_batch(10000);

-- 50,000건 데이터 삽입
CALL sp_insert_payment_source_batch(50000);

-- 100,000건 데이터 삽입
CALL sp_insert_payment_source_batch(100000);

-- 500,000건 데이터 삽입
CALL sp_insert_payment_source_batch(500000);

-- 1,000,000건 데이터 삽입
CALL sp_insert_payment_source_batch(1000000);

-- 5,000,000건 데이터 삽입
CALL sp_insert_payment_source_batch(5000000);





/********************************************* Part 2 > Ch 2 강의 전 실행 *********************************************/
-- 기존 테이블이 있다면 삭제
DROP TABLE IF EXISTS users;

-- users 테이블 생성
CREATE TABLE users
(
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    grade VARCHAR(50) NOT NULL
);

-- grade 컬럼에 대한 인덱스 생성 (조회 성능 향상)
CREATE INDEX idx_grade ON users (grade);

-- SQL 클라이언트가 세미콜론(;)을 만나도 프로시저 정의가 끝나지 않도록 구분자를 변경합니다.
DELIMITER $$

-- 만약 이전에 생성된 프로시저가 있다면 삭제합니다.
DROP PROCEDURE IF EXISTS sp_insert_user_batch;

-- 대량의 테스트 데이터를 배치(batch) 형태로 삽입하는 저장 프로시저를 생성합니다.
CREATE PROCEDURE sp_insert_user_batch(IN p_row_count INT)
BEGIN
    -- 루프 및 배치 관련 변수 선언
    DECLARE i INT DEFAULT 1;
    DECLARE v_batch_size INT DEFAULT 1000; -- 한 번에 INSERT할 데이터 묶음 크기
    DECLARE v_sql_values LONGTEXT DEFAULT '';
    -- INSERT할 값들을 누적할 문자열 변수

    -- 데이터 생성을 위한 변수 선언
    DECLARE v_grade VARCHAR(50);

    -- 요청된 row 개수만큼 루프를 실행합니다.
    WHILE i <= p_row_count
        DO
            -- 등급은 초기 등급 INIT 으로 설정
            SET v_grade = 'INIT';

            -- 생성된 데이터를 VALUES 구문 형식의 문자열로 v_sql_values에 추가합니다.
            SET v_sql_values = CONCAT(v_sql_values,
                                      '(',
                                      QUOTE(v_grade),
                                      '),'
                               );

            -- 배치 크기(1000개)에 도달했을 경우, 모아둔 데이터를 한 번에 INSERT 합니다.
            IF (i % v_batch_size = 0) THEN
                -- TRIM 함수를 사용하여 마지막에 붙은 불필요한 쉼표(,)를 안전하게 제거합니다.
                SET @sql_query = CONCAT(
                        'INSERT INTO users (grade) VALUES ',
                        TRIM(TRAILING ',' FROM v_sql_values)
                                 );

                -- 동적 SQL을 준비하고 실행합니다.
PREPARE stmt FROM @sql_query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 다음 배치를 위해 VALUES 문자열을 초기화합니다.
SET v_sql_values = '';
END IF;

            SET i = i + 1;
END WHILE;

    -- 루프가 끝난 후, 처리되지 않고 남아있는 데이터가 있다면 마지막으로 INSERT 합니다.
    IF v_sql_values != '' THEN
        SET @sql_query = CONCAT(
                'INSERT INTO users (grade) VALUES ',
                TRIM(TRAILING ',' FROM v_sql_values)
                         );

PREPARE stmt FROM @sql_query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
END IF;

END$$

-- 구분자를 다시 기본값인 세미콜론(;)으로 변경합니다.
DELIMITER ;

delete from users where id > 0;
select count(1) from users;
select count(1) from users where grade = 'INIT';

-- 1,000건 데이터 삽입
CALL sp_insert_user_batch(1000);

-- 5,000건 데이터 삽입
CALL sp_insert_user_batch(5000);

-- 10,000건 데이터 삽입
CALL sp_insert_user_batch(10000);

-- 50,000건 데이터 삽입
CALL sp_insert_user_batch(50000);




/********************************************* Part 2 > Ch 3 강의 전 실행 *********************************************/

-- 기존 테이블 drop
DROP TABLE IF EXISTS payment_source;

-- payment_source 테이블 생성
CREATE TABLE payment_source
(
    id                                   BIGINT AUTO_INCREMENT
        PRIMARY KEY,
    payment_date                         DATE           NOT NULL,
    discount_amount                      DECIMAL(38, 2) NOT NULL,
    final_amount                         DECIMAL(38, 2) NOT NULL,
    original_amount                      DECIMAL(38, 2) NOT NULL,
    partner_business_registration_number VARCHAR(100)   NOT NULL,
    partner_corp_name                    VARCHAR(100)   NOT NULL
);

-- payment_date 컬럼에 대한 인덱스 생성 (조회 성능 향상)
CREATE INDEX idx_payment_date
    ON payment_source (payment_date);


-- 구분자 변경
DELIMITER $$

-- (수정) 기존 프로시저: 날짜를 파라미터로 받도록 변경
DROP PROCEDURE IF EXISTS sp_insert_payment_source_batch;
CREATE PROCEDURE sp_insert_payment_source_batch(IN p_row_count INT, IN p_payment_date DATE)
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE v_batch_size INT DEFAULT 1000;
    DECLARE v_sql_values LONGTEXT DEFAULT '';

    DECLARE v_final_amount DECIMAL(38, 2);
    DECLARE v_discount_amount DECIMAL(38, 2);
    DECLARE v_original_amount DECIMAL(38, 2);
    DECLARE v_random_index INT;
    DECLARE v_partner_corp_name VARCHAR(100);
    DECLARE v_partner_biz_reg_num VARCHAR(20);

    WHILE i <= p_row_count
        DO
            -- 랜덤 데이터 생성
            SET v_final_amount = ROUND(RAND() * 1000000, 2);
            SET v_discount_amount = ROUND(RAND() * 100000, 2);
            SET v_original_amount = v_final_amount + v_discount_amount;
            SET v_random_index = FLOOR(1 + RAND() * 20);
            SET v_partner_corp_name = ELT(v_random_index, '삼성전자', 'LG전자', '현대자동차', 'SK텔레콤', '네이버', '카카오', '쿠팡', '배달의민족', '토스', '당근마켓', 'KT', '롯데그룹', '포스코', '신한금융그룹', 'KB금융그룹', '농협', '하나금융그룹', '대한항공', '아시아나항공', 'CJ그룹');
            SET v_partner_biz_reg_num = ELT(v_random_index, '000-01-00001', '000-01-00002', '000-01-00003', '000-01-00004', '000-01-00005', '000-01-00006', '000-01-00007', '000-01-00008', '000-01-00009', '000-01-00010', '000-01-00011', '000-01-00012', '000-01-00013', '000-01-00014', '000-01-00015', '000-01-00016', '000-01-00017', '000-01-00018', '000-01-00019', '000-01-00020');

            -- VALUES 구문 생성
            SET v_sql_values = CONCAT(v_sql_values,
                                      '(',
                                      v_original_amount, ',',
                                      v_discount_amount, ',',
                                      v_final_amount, ',',
                                      QUOTE(p_payment_date), ',', -- 파라미터로 받은 날짜 사용
                                      QUOTE(v_partner_corp_name), ',',
                                      QUOTE(v_partner_biz_reg_num),
                                      '),'
                               );

            -- 배치 사이즈에 도달하면 INSERT 실행
            IF (i % v_batch_size = 0) THEN
                SET @sql_query = CONCAT(
                        'INSERT INTO payment_source (original_amount, discount_amount, final_amount, payment_date, partner_corp_name, partner_business_registration_number) VALUES ',
                        TRIM(TRAILING ',' FROM v_sql_values)
                                 );
PREPARE stmt FROM @sql_query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET v_sql_values = '';
END IF;

            SET i = i + 1;
END WHILE;

    -- 루프 후 남은 데이터 INSERT
    IF v_sql_values != '' THEN
        SET @sql_query = CONCAT(
                'INSERT INTO payment_source (original_amount, discount_amount, final_amount, payment_date, partner_corp_name, partner_business_registration_number) VALUES ',
                TRIM(TRAILING ',' FROM v_sql_values)
                         );
PREPARE stmt FROM @sql_query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
END IF;

END$$

-- (신규) 2025년 1월 데이터를 생성하는 프로시저
DROP PROCEDURE IF EXISTS sp_populate_january_2025;
CREATE PROCEDURE sp_populate_january_2025()
BEGIN
    DECLARE v_current_date DATE DEFAULT '2025-01-01';
    DECLARE v_end_date DATE DEFAULT '2025-01-31';

    -- 2025-01-01부터 2025-01-31까지 하루씩 증가하며 루프 실행
    WHILE v_current_date <= v_end_date
        DO
            -- 각 날짜마다 1,000건의 데이터를 삽입하는 프로시저 호출
            CALL sp_insert_payment_source_batch(1000, v_current_date);

            -- 날짜를 하루 증가
            SET v_current_date = DATE_ADD(v_current_date, INTERVAL 1 DAY);
END WHILE;
END$$

-- 구분자를 다시 세미콜론으로 변경
DELIMITER ;

-- (최종 실행) 신규 프로시저를 호출하여 1월 한 달 치 데이터(총 31,000건) 생성
CALL sp_populate_january_2025();

select payment_date, count(1)
from payment_source

group by payment_date
order by payment_date asc;