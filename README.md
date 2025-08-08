# Spring Batch
## 프로젝트 정보
- [패스트캠퍼스] 실무를 위한 Spring Batch의 모든 것

## 프로젝트 설정
- Docker
  - docker-compose up -d
- 테이블 생성
  - scheme.sql

<hr>

## 개념정리
### Spring Batch 특징 및 장점
- 풍부한 필수 기능 제공
  - 트랜잭션 관리, 로깅/추적, 체크포인트/재시작, 통계 집계, 예외 처리(Skip), 재시도(Retry) 등의 기능을 간편히 활용 가능
  - 실패한 배치 Job을 중단 지점부터 재시작하거나 동일 파라미터로 중복 실행 방지 지원
- Spring 생태계와 쉬운 통합
  - Spring Framework 기반으로 DI, AOP 활용 가능
  - Spring Boot 환경에서 Starter 의존성 추가만으로 자동 설정(DataSource, JobRepository 등) 제공
  - Spring Integration, Spring Cloud(Data Flow, Task)와 연계하여 배치 파이프라인 확장 및 모니터링 가능
- 검증된 성능과 안정성
  - 금융권, 이커머스 등 다양한 산업에서 검증된 신뢰성과 성능 제공
  - 청크 기반 처리, 멀티스레딩, 파티셔닝을 통한 효율적인 대용량 데이터 처리 지원
  - 청크 크기, 스레드 개수 등 세부적인 성능 튜닝 가능
- 다양한 Reader/Writer 지원
  - 파일(CSV, XML, JSON), DB(JDBC, JPA), JMS, MongoDB 등의 다양한 데이터 소스를 기본적으로 지원
  - 커스텀 ItemReader/Writer로 새로운 데이터 소스에 쉽게 대응 가능

### Spring Batch 구조
- 구조
  - ![architecture.png](image/architecture.png)
    - JobLauncher
      - 배치 잡(Job)을 실행하는 진입점
    - Job
      - 하나의 배치 작업 단위
      - 내부에 여러 개의 Step으로 구성
    - Step
      - Chunk 기반 처리나 Tasklet 단위로 비즈니스 로직을 수행
    - JobRepository
      - 배치 메타데이터(실행 기록, 파라미터, 상태 등)를 저장 및 조회하는 저장소
    - Database
      - 배치 메타데이터 테이블이 생성·관리되는 물리적 저장소
    - Tasklet
      - Step 내에서 단일 작업 단위를 수행하는 컴포넌트
    - Chunk Base
      - 데이터를 처리하는 묶음의 크기
    - ItemReader
      - 처리 대상을 읽어오는 단계
    - ItemProcessor
      - 읽어온 아이템을 가공·변환·검증하는 단계
    - ItemWriter
      - 가공된 아이템을 저장하거나 전송

### Job
- Job은 Spring Batch에서 배치 처리의 최상위 단위이다.
- Spring Batch에서 Job은 구성(configuration)과 실행(execution) 두 역할을 동시에 수행한다.
  - Job은 어떤 Step들을 어떤 순서로 실행할지 정의한다.
  - Job은 JobLauncher에 의해 실행되며, 실행 시 전달된 JobParameters를 기반으로 고유한 JobInstance를 생성한다.
  - 이 실행은 JobExecution이라는 실행 이력으로 기록되며, 추적 및 재시작이 가능하다.

### JobInstance
- 특정 Job이 특정 파라미터로 실행된 논리적 인스턴스
- 같은 Job이라도, 실행 파라미터가 다르면 새로운 JobInstance로 간주
  - 즉, Job명 + JobParameters 조합으로 유일하다.
  - 같은 JobName, 같은 JobParameters면 이미 존재하는 JobInstance → 중복 실행 방지

### JobParameters
- Job 실행 시 입력되는 파라미터 집합
- JobInstance의 유일성을 결정짓는 핵심 요소

### JobParameters와 JobInstance의 관계
- JobName + JobParameters의 조합이 기존에 존재하면, 동일 JobInstance로 인식
- 파라미터가 하나라도 다르면 새로운 JobInstance 생성

### JobExecution
- JobInstance의 한 번의 실제 실행 이력
- 하나의 JobInstance(특정 JobName+JobParameters)에 대해 여러 번 실행(재시작, 실패 후 재시도 등)이 있을 수 있음
- 실행마다 별도의 JobExecution이 생성되고, 각각 status(성공/실패), 시작/종료 시각, 에러메시지, 종료코드 등이 저장

### Step
- Step은 Spring Batch에서 ‘실제 데이터 처리’가 일어나는 최소 단위이다.
- 하나의 Job은 여러 개의 Step으로 이루어질 수 있고, 각 Step이 순차적 혹은 조건에 따라 실행되면서 전체 배치 작업이 완성된다.
- Step 내부에서는 Reader → Processor → Writer의 구조로 데이터를 읽고(Reader), 가공하고(Processor), 저장(Writer) 한다.
- Step이 실행될 때마다 처리 건수, 성공/실패, 상태 등이 메타테이블에 기록되어 모니터링, 장애 대응, 재시작에 활용된다.
- Step은 Tasklet 기반(단일 작업) 또는 Chunk 기반(데이터 묶음 단위 반복 처리)으로 구현할 수 있다.

### Step 구성방식
- 구성방식에는 크게 두 가지가 존재한다.
  - TaskletStep
    - 하나의 Step에서 ‘특정 로직’을 단일 작업으로 실행할 때 사용.
      - ex) 파일 삭제, 외부 시스템 알림 전송, 단건 처리 등.
    - Tasklet 인터페이스를 구현해서 원하는 로직만 작성하면 된다.
  - ChunkOrientedTasklet (Chunk 기반 Step, 실무에서 가장 많이 사용)
    - 대용량 데이터를 ‘Chunk’(묶음) 단위로 반복 처리.
      - ex) 1000건 데이터를 100건씩 나누어 읽고, 가공, 저장 반복.
    - 내부적으로 ItemReader → ItemProcessor → ItemWriter 구조를 따름.
    - 데이터 집계, 이관, 대량 파일 처리 등 대부분의 배치 처리에 사용.

### StepExecution
- Step의 한 번의 실행을 뜻한다. (즉 실행 이력 단위)
- 언제, 어떤 상태로 실행됐고, 성공/실패/중단 등 결과가 어떻게 됐는지 모두 기록
  - 실행 시작/종료 시간, 상태(성공, 실패 등), 처리/읽은/쓰여진 건수, 에러 메시지 등을 알 수 있다.
  - 장애 발생 시, 어느 Step에서 문제가 발생했는지 추적하는 용도로 사용할 수 있다.
- BATCH_STEP_EXECUTION 테이블에 이력 저장

### StepContribution
- StepExecution 내부에서 한 Chunk(묶음)의 처리 결과를 누적·집계하는 역할.
- 청크 기반 처리에서 각 Chunk별로 Reader/Processor/Writer의 처리 결과를 기록하고, StepExecution에 반영
- 읽은/처리한/쓰여진/스킵된 아이템 수를 알 수 있다.
  - 성능 튜닝, 장애 분석, 청크 사이즈 조정 시 StepContribution 로그가 매우 유용함
- 동작방식
  - 청크 사이즈마다 ItemReader → ItemProcessor → ItemWriter가 실행되고, 그 결과가 StepContribution에 저장된다.
  - 모든 Chunk가 끝나면 StepContribution의 통계 정보가 StepExecution에 합산된다.

### Chunk 기반 처리
- Chunk란?
  - 대용량 데이터를 ‘덩어리(Chunk, 묶음)’ 단위로 잘라서 일정 개수씩 반복 처리하는 방식이다.
  - 예를 들어, 1000건의 데이터를 100건씩 10번에 나누어 처리(읽기 → 가공 → 쓰기)를 반복한다.
- 처리 구조
  - Step 내부에서 ItemReader → ItemProcessor → ItemWriter 구조로 동작한다.
  - Reader에서 데이터를 Chunk 사이즈만큼 읽고, Processor에서 가공/필터링한 뒤, Writer에서 일괄 저장한다.
- 장점
  - 메모리 효율성
    - 한 번에 모든 데이터를 올리지 않고, Chunk 단위로 나눠서 처리하므로 메모리 부담이 적다.
  - 성능 향상
    - DB/파일 등에 대량의 데이터를 일괄 저장/커밋하여 I/O 성능을 극대화할 수 있다.
  - 트랜잭션 관리
    - 각 Chunk 단위로 트랜잭션이 걸리므로 실패 시 해당 Chunk만 재시도/롤백할 수 있다.
- Chunk 사이즈 조정 시 주의점
  - 데이터베이스 트랜잭션 커밋 문제
    - 대량 커밋 시 DB Lock, Deadlock, Undo/Redo 로그 폭증 등으로 오히려 전체 성능 저하
    - 장애 발생 시 롤백 비용 커짐
  - 외부 서비스 연동 시 네트워크 한계
    - 한 번에 대용량 데이터를 외부 시스템에 전송하면 상대 서버가 수신 거부, 타임아웃 발생 가능
    - 네트워크 대역폭을 초과해 실패하거나, 처리 지연
  - 메모리 사용량 폭증
    - Chunk 사이즈가 클수록 한 번에 처리할 데이터를 메모리에 모두 올려야해서 JVM OutOfMemory 등, 서버 메모리 한계 초과로 배치 실패 위험

### ItemReader
- ItemReader란?
  - Step에서 데이터를 읽어오는 역할
  - 다양한 데이터 소스(JPA, JDBC, 파일, API 등)에서 데이터 추출
- 주요 구현체와 특징
  - JpaPagingItemReader
    - JPA 기반, 페이징 처리로 대용량 데이터 안전하게 읽기
    - JPQL 쿼리 사용, 한 번에 메모리 과부하 방지
    - flush/clear 타이밍, 영속성 컨텍스트 관리가 필요하다.
  - JdbcCursorItemReader
    - DB Cursor 기반, 한 줄씩 직접 읽어오기
    - 커넥션 점유, 트랜잭션 길어질 경우 부하가 발생할 수 있다.
  - FlatFileItemReader
    - CSV/텍스트 파일 등 외부 파일 데이터 읽기
    - 파일 인코딩, 구분자, 헤더/스킵 처리 등이 정확해야한다.
- ItemReader 사용 시 주의점
  - 대량 데이터 처리 시 한 번에 너무 많은 데이터 읽으면 OutOfMemory 등 메모리 이슈 발생
  - 커서 방식 Reader는 DB에 부하, 커넥션 장시간 점유
  - 페이징 Reader는 쿼리 튜닝 필수, 인덱스 유무 확인
  - 파일 Reader는 잘못된 포맷, 줄바꿈 문자 등으로 오류가 발생할 수 있음

### ItemProcessor
- ItemProcessor란?
  - Reader에서 읽은 데이터를 가공, 변환, 필터링(제외)하는 역할.
  - 입력값을 받아 가공 결과를 반환하거나, 필요 없는 데이터는 null로 반환해 Writer 단계로 전달하지 않음.
- ItemProcessor 사용 시 주의점
  - 외부 서비스 호출, 복잡한 연산 등의 무거운 연산이 많으면 전체 배치 성능 저하될 수 있다.
  - 필터링/가공 후 Writer에 넘기는 데이터와 실제 저장될 데이터의 정합성 항상 확인이 필요하다.
  - Processor가 null을 반환하면 Writer로 넘어가지 않으므로 주의가 필요하다.

### ItemWriter
- ItemWriter란?
  - Processor에서 넘어온 데이터를 실제로 저장(쓰기)하는 역할
- 주요 구현체와 특징
  - JpaItemWriter, JdbcBatchItemWriter
    - DB에 일괄 저장, 대량 데이터 처리에 적합
    - 트랜잭션, 커밋 타이밍, 영속성 컨텍스트 관리가 필요하다.
  - FlatFileItemWriter
    - 파일(CSV, 텍스트 등)로 저장, 대량 로그/리포트 파일 생성에 유용
    - 파일 포맷, 인코딩, 줄바꿈 문자 등에 대한 주의가 필요하다.
  - Custom Writer
    - 외부 API, 메일, 메시지 큐 등 다양한 방식 구현 가능
    - 외부 시스템 장애, 네트워크 타임아웃 등 예외처리를 신경써야한다.
- ItemWriter 사용 시 주의점
  - Writer는 한 번에 Chunk만큼의 데이터를 저장하기 때문에, 너무 크면 트랜잭션 부담이 되고 너무 작으면 I/O 오버헤드가 발생한다.
  - 저장 실패 시 배치 전체가 중단될 수 있으니 롤백, 재시도 등에 대한 전략이 필요하다.
  - 저장 후 데이터의 실제 상태를 검증하는 것이 좋다.

### 내결함성(Fault Tolerance)
- 내결함성이란?
  - 배치 처리 중 작은 데이터 오류나 일시적 장애가 발생해도 Step 전체를 곧바로 실패시키지 않고, 건너뛰기 또는 재시도 로직을 적용해 계속 진행할 수 있도록 하는 기능.
- 필요성
  - 대용량 데이터 처리 시 일부 데이터 오류나 외부 시스템 장애가 항상 발생할 수 있음
  - 전체 Job이 단 한 건의 오류로 멈춘다면 생산성·가용성이 크게 저하
  - Fault-Tolerant를 통해 신뢰성(resilience)과 지속 가용성(availability) 확보
- 설정 메서드
  - faultTolerant()
    - Skip/Retry/BackOff 등의 내결함성 옵션을 사용할 수 있도록 StepBuilder를 FaultTolerantStepBuilder로 전환한다.
  - skip(Class<? extends Throwable> type)
    - 지정한 예외 타입이 발생했을 때 해당 아이템을 건너뛰도록 등록한다.
  - skipLimit(int)
    - Skip 허용 최대 건수를 설정한다.
    - 이 횟수를 초과하면 Step이 실패한다.
  - skipPolicy(SkipPolicy)
    - 사용자 정의 또는 기본 SkipPolicy를 등록할 수 있다.
    - skipPolicy를 등록하면 복합 기준(예: 예외 타입 + 횟수)을 적용할 수 있다.
  - noSkip(Class<? extends Throwable>)
    - Skip 대상에서 제외할 예외 타입을 지정한다.
  - retry(Class<? extends Throwable>)
    - 지정한 예외 타입이 발생했을 때 재시도를 수행하도록 등록한다.
  - retryLimit(int)
    - Retry 허용 최대 횟수를 설정한다.
    - 이 횟수를 초과하면 재시도를 중단한다.
  - retryPolicy(RetryPolicy) 
    - 사용자 정의 또는 기본 RetryPolicy를 등록할 수 있다.
    - RetryPolicy를 등록하면 복합 기준(예: 예외 타입 + 횟수)을 적용할 수 있다.
  - backOffPolicy(BackOffPolicy)
    - 재시도 간격(고정/지수 등)을 조정하는 BackOff 정책을 등록한다.
  - noRetry(Class<? extends Throwable> type)
    - Retry 대상에서 제외할 예외 타입을 지정한다.
  - noRollback(Class<? extends Throwable> type>)
    - 지정한 예외 타입이 발생해도 트랜잭션을 롤백하지 않고 커밋된 데이터를 유지한다.