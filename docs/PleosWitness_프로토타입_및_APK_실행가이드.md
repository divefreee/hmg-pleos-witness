# 🚨 Pleos Witness (플레오스 위트니스) — 프로토타입 및 APK 실행 가이드

현대자동차그룹 차량용 앱 개발 공모전(**Code your App into Pleos App Market**) 출품작 **Pleos Witness (차안의 AI 안전신문고)**의 산출물 구성 및 실행 방법 안내서입니다.

---

## 1. 제출 산출물 목록

| 구분 | 파일 / 디렉터리명 | 설명 |
| :--- | :--- | :--- |
| **기획서** | `현대자동차그룹_차량용앱공모전_기획서_PleosWitness.docx` | 공모전 공식 양식 기획서 완성본 |
| **실행 APK** | `PleosWitness_release.apk` (`apk/PleosWitness_release.apk`) | Pleos Connect SDK 5종 연동 Android Automotive OS(API 28+) 실행용 서명된 Release APK |
| **시연 영상 (16:9)** | `PleosWitness_시연영상_16x9.mp4` | Pleos 27인치 와이드 디스플레이 시뮬레이터 4단계 전체 동작 녹화 영상 (1920×1080) |
| **숏폼 영상 (9:16)** | `PleosWitness_숏폼챌린지_9x16.mp4` | 숏폼 챌린지 홍보 영상 (1080×1920, Spot→Solve→Scene) |
| **웹 시뮬레이터** | `simulator/` 및 `prototype/` | 로컬 PC 브라우저에서 즉시 조작·검증 가능한 2종의 인터랙티브 시뮬레이터 |

---

## 2. 프로토타입 실행 방법 (총 3가지 방식 지원)

심사 및 테스트 환경에 따라 **① 풀스택 IVI 웹 시뮬레이터(FastAPI + OpenCV 비전 분석 영상 연동)**, **② 1인칭 운전석 단독 HTML 프로토타입**, **③ Android Automotive APK(Pleos Connect Emulator)** 중 편한 방식으로 즉시 실행할 수 있습니다.

---

### 방법 A. 27인치 Pleos Connect IVI + FastAPI 통합 시뮬레이터 실행 (권장)

실제 고속도로 블랙박스 영상을 OpenCV로 분석한 결과물(`dashcam_ai_analyzed.mp4`, 번호판 다중 프레임 초해상도 복원 전·후 이미지, 실시간 SHA-256 무결성 해시 계산 API)이 연동된 시뮬레이터입니다.

#### 1) 서버 실행
터미널에서 프로젝트 내 `simulator/` 폴더로 이동한 뒤 FastAPI 서버를 실행합니다.

```bash
cd simulator
pip install fastapi uvicorn opencv-python numpy
uvicorn server:app --host 127.0.0.1 --port 18765
```

#### 2) 브라우저 접속
웹 브라우저(Chrome/Safari/Edge)에서 아래 주소로 접속합니다.
- **접속 주소**: `http://127.0.0.1:18765`

#### 3) 시뮬레이터 조작 방법 (4단계 상태 전환)
화면 하단 중앙의 **시연 컨트롤 바** 버튼(또는 키보드 숫자키 `1` ~ `4`, 화면 내 기어 `D`/`P` 및 음성 배너 직접 클릭)으로 동작을 테스트합니다.

1. **[① 주행 감시 (D)]** (단축키 `1`)
   - 좌측 3D 내비게이션 뷰 + 우측 빌트인 캠 2 원본 주행 영상(`dashcam_raw.mp4`)이 재생됩니다.
   - 상단 기어가 `D`로 표시되며, 운전 중 시선 분산을 막는 **`Pleos Safety Lock: 주행 중(D) 팝업 차단`**이 활성화됩니다.
2. **[② 음성 북마크·AI분석]** (단축키 `2` 또는 우측 상단 음성 배너 클릭)
   - `POST /api/v1/pleos/voice-bookmark` API가 호출되며 *"헤이 플레오스, 방금 끼어든 차 신고해 줘"* 음성 명령이 트리거됩니다.
   - 우측 화면이 OpenCV 비전 AI 분석 영상(`dashcam_ai_analyzed.mp4`)으로 전환되어 **위반 차량(흰색 BMW) 실시간 바운딩 박스 추적, 방향지시등 점멸 주파수(`0.0Hz` 미점등) 판정, 제3자 차량 개인정보 블러 마스킹**이 표시됩니다.
   - 주행 중(`D`단)이므로 화면을 가리는 신고 팝업은 띄우지 않고 백그라운드 증거 패키징 완료 배지만 표시합니다.
3. **[③ 정차(P) 팝업 해제]** (단축키 `3` 또는 상단 기어 `P` 클릭)
   - `POST /api/v1/pleos/gear/P` API가 호출되어 차량이 정차(`P`단) 상태로 전환되고 `Safety Lock`이 해제됩니다.
   - 화면 중앙에 **원터치 공익신고 검증 모달 팝업**이 열리며, **다중 프레임 초해상도 번호판 복원 전·후 비교(`123가 4568`, 신뢰도 `99.4%`)**, 위반 법규(`도로교통법 제38조 제1항 진로변경 신호불이행`), 위반 위치·시각을 한눈에 확인할 수 있습니다.
4. **[④ 안전신문고 원터치 접수]** (단축키 `4` 또는 팝업 내 파란색 접수 버튼 클릭)
   - `POST /api/v1/mois/safety-report` API가 호출되어 서버가 실제 증거 영상 파일의 **SHA-256 무결성 해시**를 계산해 검증하고, 행정안전부 안전신문고 접수 번호(`제2026-MOIS-0891422호`)를 발급합니다.
- **[▶ 전체 자동 시연]** 버튼을 누르면 위 `① → ② → ③ → ④` 전 과정이 3.5초 간격으로 자동 시연됩니다.

---

### 방법 B. 1인칭 운전석(Cockpit + AR-HUD) 단독 웹 프로토타입 실행 (무설치)

별도의 Python 서버 실행 없이 단일 HTML 파일만으로 1인칭 운전석 스티어링 휠, 전면 윈드실드 AR-HUD, 27인치 Pleos 디스플레이를 함께 체험할 수 있는 프로토타입입니다.

```bash
# macOS
open prototype/index.html

# Windows
start prototype/index.html
```
- 화면 상단 컨트롤 바의 **[▶ 25초 전체 시나리오 자동 시연]** 버튼 또는 **[Step 1] ~ [Step 4]** 버튼을 클릭해 시나리오를 확인합니다.

---

### 방법 C. Android Automotive OS APK 설치 및 실행 (`PleosWitness_release.apk`)

현대자동차그룹 공식 Maven 저장소(`https://nexus-playground.pleos.ai/repository/maven-releases/`)의 **Pleos Connect SDK 5대 모듈**(`Vehicle 2.3.2`, `FusedLocation 1.1.3`, `NaviHelper 3.0.5`, `SpeechToText 3.3.1`, `TextToSpeech 3.3.1`)과 Jetpack Compose로 빌드된 실제 안드로이드 앱입니다.

#### 1) 실행 환경 요구사항
- **대상 OS**: Pleos Connect Emulator (Android Automotive OS) 또는 Android 9.0 (API 28) 이상 에뮬레이터/단말
- **패키지명**: `io.pleos.snapreport`

#### 2) APK 바로 설치 (ADB 사용 시)
에뮬레이터가 실행된 상태에서 아래 명령어로 `PleosWitness_release.apk`를 즉시 설치 및 실행합니다.

```bash
adb install -r apk/PleosWitness_release.apk
adb shell am start -n io.pleos.snapreport/.ui.MainActivity
```
*(또는 실행 중인 Android 에뮬레이터 화면 위로 `PleosWitness_release.apk` 파일을 마우스로 드래그 앤 드롭하면 자동 설치됩니다.)*

#### 3) 앱 내 동작 검증 순서
1. 앱 실행 시 하단 콘솔에 **Pleos Connect SDK 5종(`Vehicle`, `FusedLocation`, `NaviHelper`, `SpeechToText`, `TextToSpeech`) 초기화 로그**가 실시간으로 출력됩니다.
2. 좌측의 **[🎙️ 1. 주행 중 음성 북마크 실행 (STT/TTS)]** 버튼을 터치하면 음성 발화 캡처 → 차량 텔레메트리 스냅샷 수집 → 빌트인 캠 2 AI 분석(`123가 4568`, 진로변경 방법 위반) 및 신고서 초안 자동 생성이 수행됩니다. (주행 중이므로 우측 제출 버튼은 🔒 잠금 상태 유지)
3. 좌측의 **[🅿️ 2. 정차(P) 상태 전환 (Safety Lock 해제)]** 버튼을 터치하면 주차 브레이크 체결이 확인되어 우측의 제출 버튼 잠금이 해제됩니다.
4. 우측 하단의 **[🚀 3. 행정안전부 안전신문고 API 원터치 접수]** 버튼을 터치하면 최종 접수가 완료되고 접수번호(`제2026-MOIS-0891422호`)와 음성 안내(TTS)가 출력됩니다.

#### 4) 소스코드 직접 빌드 방법 (선택)
```bash
cd android-app
./gradlew assembleRelease
# 빌드 산출물 경로: android-app/app/build/outputs/apk/release/app-release.apk
```
