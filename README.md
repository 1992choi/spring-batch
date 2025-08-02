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