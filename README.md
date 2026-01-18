# SwiftCast Spring Native

**Claude Code와 GLM API 간 유연한 전환 및 사용량 모니터링을 위한 프록시 서버**

Spring Boot 3.x + GraalVM Native Image + JavaFX로 구현된 크로스 플랫폼 데스크톱 애플리케이션

---

## 🎯 주요 기능

### 1. **유연한 API 전환**
- Claude Code와 GLM API 간 실시간 전환
- 여러 API 계정 관리 및 전환
- URL 기반 프록시 라우팅

### 2. **사용량 모니터링**
- 요청/응답 로깅
- 토큰 사용량 추적
- 비용 계산 및 통계

### 3. **Claude 설정 백업/복원**
- Claude Code settings.json 자동 백업
- 타임스탬프 기반 백업 관리
- 원클릭 복원 기능

### 4. **네이티브 바이너리**
- GraalVM Native Image 지원
- JRE 설치 불필요
- 5~10초 고속 시작
- 낮은 메모리 사용량

---

## 🛠️ 기술 스택

| 레이어 | 기술 |
|--------|------|
| **백엔드** | Spring Boot 3.2.1 |
| **프록시** | Spring WebFlux (비동기) |
| **데이터베이스** | SQLite + JPA |
| **UI** | JavaFX 21 |
| **빌드** | Maven + GraalVM Native Image |
| **플랫폼** | Windows, macOS, Linux |

---

## 📦 요구사항

### 개발 환경
- Java 21 (GraalVM 권장)
- Maven 3.9+
- GraalVM Native Image (네이티브 빌드 시)

### 실행 환경
- **JVM 모드**: Java 21+
- **네이티브 모드**: 운영체제만 (JRE 불필요)

---

## 🚀 빌드 및 실행

### 1. JVM 모드 (개발)

```bash
# 빌드 및 실행
mvn clean spring-boot:run

# 또는 패키지 후 실행
mvn clean package
java -jar target/swiftcast-native-0.1.0.jar
```

### 2. 네이티브 이미지 빌드 (프로덕션)

```bash
# GraalVM Native Image 설치 확인
gu install native-image

# 네이티브 이미지 빌드
mvn -Pnative native:compile

# 실행 (JRE 불필요!)
./target/swiftcast-native
```

**빌드 시간**: 약 3~5분 (첫 빌드)
**실행 파일 크기**: 약 50~80MB
**시작 시간**: 5~10초

---

## 💻 사용 방법

### 1. 계정 추가
1. "계정 추가" 버튼 클릭
2. 계정 정보 입력:
   - **이름**: 계정 식별 이름 (예: "Claude Prod", "GLM Dev")
   - **Base URL**: API 엔드포인트
     - Claude: `https://api.anthropic.com`
     - GLM: `https://open.bigmodel.cn` (예시)
   - **API Key**: API 키

### 2. 프록시 시작
1. 계정 목록에서 사용할 계정 선택
2. "활성화" 버튼 클릭
3. "프록시 시작" 버튼 클릭
4. 포트 8080에서 프록시 실행

### 3. Claude Code 설정

**Windows**:
```json
// %APPDATA%\Claude\settings.json
{
  "env": {
    "ANTHROPIC_BASE_URL": "http://localhost:8080"
  }
}
```

**macOS**:
```json
// ~/Library/Application Support/Claude/settings.json
{
  "env": {
    "ANTHROPIC_BASE_URL": "http://localhost:8080"
  }
}
```

### 4. 백업 생성
1. "백업 생성" 버튼 클릭
2. 현재 Claude 설정이 자동 백업됨
3. 백업 위치:
   - Windows: `%APPDATA%\swiftcast-backups\`
   - macOS: `~/.config/swiftcast-backups/`

### 5. 백업 복원
1. 백업 목록에서 복원할 백업 선택
2. "복원" 버튼 클릭
3. Claude Code 재시작

---

## 📁 프로젝트 구조

```
swiftcast_springNative/
├── src/main/java/com/swiftcast/
│   ├── SwiftCastApplication.java      # 메인 클래스
│   ├── model/
│   │   ├── Account.java               # 계정 엔티티
│   │   ├── UsageLog.java              # 사용량 로그
│   │   └── BackupInfo.java            # 백업 정보
│   ├── repository/
│   │   ├── AccountRepository.java
│   │   └── UsageLogRepository.java
│   ├── service/
│   │   ├── AccountService.java        # 계정 관리
│   │   └── BackupService.java         # 백업/복원
│   ├── proxy/
│   │   └── ProxyServer.java           # HTTP 프록시
│   ├── ui/
│   │   └── MainWindow.java            # JavaFX UI
│   └── config/
│       └── WebClientConfig.java       # WebClient 설정
├── src/main/resources/
│   └── application.properties         # Spring 설정
├── pom.xml                             # Maven 빌드 파일
└── README.md
```

---

## 🔍 핵심 작동 원리

### 프록시 플로우

```
Claude Code
    ↓ (http://localhost:8080)
SwiftCast Proxy
    ↓ (계정 정보로 라우팅)
선택된 API (Anthropic/GLM/etc)
    ↓ (응답)
Claude Code
```

### 백업 플로우

```
1. 백업 생성
   Claude settings.json → swiftcast-backups/settings_backup_{timestamp}.json

2. 백업 복원
   settings_backup_{timestamp}.json → Claude settings.json
```

---

## 🎨 UI 스크린샷

### 프록시 제어
- 프록시 시작/중지
- 실시간 상태 표시
- 포트 정보

### 계정 관리
- 계정 추가/삭제
- 계정 전환
- 활성 계정 표시

### 백업 관리
- 백업 생성
- 백업 복원
- 백업 삭제
- 타임스탬프 및 크기 표시

---

## ⚙️ 설정

### application.properties

```properties
# 서버 포트 (Spring Boot 자체 포트, 프록시 포트와 별개)
server.port=8081

# 데이터베이스 경로
spring.datasource.url=jdbc:sqlite:${user.home}/.config/swiftcast/data.db

# 로깅 레벨
logging.level.com.swiftcast=DEBUG
```

### 프록시 포트 변경

MainWindow.java에서 수정:
```java
proxyServer.start(8080);  // 원하는 포트로 변경
```

---

## 🐛 트러블슈팅

### 문제 1: 프록시 시작 실패
**원인**: 포트 8080이 이미 사용 중

**해결**:
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# macOS/Linux
lsof -ti:8080 | xargs kill -9
```

### 문제 2: 계정 없음 오류
**원인**: 활성 계정이 설정되지 않음

**해결**:
1. 계정 추가
2. 계정 선택 후 "활성화" 클릭

### 문제 3: 백업 파일 없음
**원인**: Claude settings.json이 존재하지 않음

**해결**:
```bash
# Windows
mkdir %APPDATA%\Claude
echo {} > %APPDATA%\Claude\settings.json

# macOS
mkdir -p ~/Library/Application\ Support/Claude
echo "{}" > ~/Library/Application\ Support/Claude/settings.json
```

### 문제 4: 네이티브 빌드 실패
**원인**: GraalVM Native Image 미설치

**해결**:
```bash
# GraalVM 사용 중인지 확인
java -version

# Native Image 설치
gu install native-image
```

---

## 📊 성능 비교

| 항목 | JVM 모드 | Native 모드 |
|------|----------|-------------|
| **시작 시간** | 3~5초 | 5~10초 |
| **메모리 사용량** | 150~200MB | 50~80MB |
| **파일 크기** | 50MB (JAR) | 60~80MB |
| **JRE 필요** | ✅ 필요 | ❌ 불필요 |

---

## 🔒 보안

### API 키 저장
- SQLite 데이터베이스에 평문 저장
- 로컬 파일 시스템 권한으로 보호
- 향후 암호화 예정

### 데이터 위치
- **Windows**: `%APPDATA%\.config\swiftcast\`
- **macOS**: `~/.config/swiftcast/`
- **백업**: `swiftcast-backups/`

---

## 🚧 향후 계획

- [ ] API 키 암호화
- [ ] 사용량 통계 대시보드
- [ ] 실시간 토큰 카운팅
- [ ] macOS 코드 사이닝
- [ ] Windows installer (MSI)
- [ ] 자동 업데이트

---

## 📝 라이선스

MIT License

---

## 👥 기여

이슈 및 PR 환영합니다!

---

## 🙏 Acknowledgments

- Anthropic Claude API
- Spring Boot Team
- GraalVM Team
- OpenJFX Team

---

**Built with ❤️ and ☕**
