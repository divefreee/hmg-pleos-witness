# 🚨 Pleos Witness (플레오스 위트니스) — 차안의 AI 안전신문고

> **“운전 중 말 한마디로 북마크하고 정차 후 원터치로 접수하여, 번거로운 신고 절차를 혁신할 뿐 아니라 도로 위의 감정 분쟁·보복운전과 무리한 추격으로 인한 2차 사고 위험까지 원천 차단하는 차안의 AI 안전신문고”**

현대자동차그룹 차량용 앱 개발 공모전 (**Code your App into Pleos App Market**) 출품 프로젝트입니다.

---

## 1. 앱 이름 및 한 줄 소개

- **앱 이름**: **Pleos Witness (플레오스 위트니스) — 차안의 AI 안전신문고**
- **한 줄 소개**: 차량에 내장된 **빌트인 캠 2**, **Pleos Connect SDK 5대 모듈**, **온디바이스 비전 AI**를 결합해, 기존에 블랙박스 SD카드를 빼서 영상을 편집·다운로드해 스마트폰 안전신문고 앱에 수기 입력하느라 **15분 이상 걸리던 교통법규 위반 공익신고 과정을 ‘주행 중 음성 1초 북마크 + 정차 후 10초 원터치 접수’로 단축**하고 도로 위 보복운전·2차 사고 위험을 예방하는 차량 특화 앱입니다.

---

## 2. 개발에 사용한 Pleos Connect 에뮬레이터 버전 및 화면 규격

- **플랫폼 / 에뮬레이터 버전**:
  - **Pleos Connect Emulator (Android Automotive OS, Android API Level 28 ~ 34 호환)**
  - `minSdk = 28`, `compileSdk = 34`, `targetSdk = 34`
- **화면 규격 (Display Specification)**:
  - **Pleos Connect 16:9 와이드 IVI 디스플레이**: **`1920 × 1080 px` (FHD Landscape, `160 dpi` / `mdpi`)**
  - **Pleos 27인치 파노라믹 디스플레이 웹 시뮬레이터**: `1920 × 1080 px` (좌측 주행 클러스터 + 우측 앱 영역 2-Pane Split View 반응형 레이아웃 지원)
- **연동된 현대자동차그룹 공식 Pleos Connect SDK 5종 (공식 Nexus Maven 저장소 연동)**:
  - `ai.pleos.playground:Vehicle:2.3.2`
  - `ai.pleos.playground:FusedLocation:1.1.3`
  - `ai.pleos.playground:NaviHelper:3.0.5`
  - `ai.pleos.playground:SpeechToText:3.3.1`
  - `ai.pleos.playground:TextToSpeech:3.3.1`

---

## 3. APK 파일 위치 및 GitHub Release 안내

- **APK 파일 경로 (저장소 내 `apk/` 폴더)**:
  - [`apk/이동현_PleosWitness_release.apk`](./apk/이동현_PleosWitness_release.apk) *(공식 파일명 규격 `팀명_앱이름_release.apk` 준수, 37MB, v2 서명된 Release APK)*
  - [`apk/PleosWitness_release.apk`](./apk/PleosWitness_release.apk) *(동일 빌드 본)*
- **패키지명 / 버전**: `io.pleos.snapreport` (`versionCode = 1`, `versionName = "1.0.0"`)

---

## 4. 테스트 환경 및 필요한 설정

| 구분 | 환경 및 설정 사양 |
| :--- | :--- |
| **Android 앱 빌드·실행 환경** | • **JDK**: OpenJDK 17 (`17.0.18`)<br>• **Gradle / AGP**: Gradle `8.6` / Android Gradle Plugin `8.2.2`<br>• **Kotlin / Compose**: Kotlin `1.9.22` / Jetpack Compose Material3<br>• **Maven 저장소**: `https://nexus-playground.pleos.ai/repository/maven-releases/` (`android-app/settings.gradle.kts`에 기본 등록 완료, 별도 인증 키 불필요) |
| **웹 시뮬레이터 실행 환경** | • **Python**: Python `3.10` 이상 (`fastapi`, `uvicorn`, `opencv-python`, `numpy`)<br>• **브라우저**: Chrome / Edge / Safari 최신 버전 (`1920×1080` 권장)<br>• **외부 API 키**: 불필요 (모든 비전 AI 번호판 초해상도 복원·차량 텔레메트리·SHA-256 해시 서명이 로컬에서 즉시 동작) |

---

## 5. 실행 방법 (3가지 검증 방식 제공)

상세 화면 캡처와 단계별 가이드는 [`docs/PleosWitness_프로토타입_및_APK_실행가이드.md`](./docs/PleosWitness_프로토타입_및_APK_실행가이드.md) 문서를 함께 참고해 주세요.

### 방법 A. Pleos Connect 에뮬레이터에 Release APK 즉시 설치·실행 (권장)
Pleos Connect 에뮬레이터(Android Automotive OS AVD)가 실행 중인 상태에서 아래 명령어로 서명된 Release APK를 바로 설치·구동할 수 있습니다.

```bash
# 1. 연결된 Pleos Connect 에뮬레이터 확인
adb devices

# 2. Release APK 설치
adb install -r apk/이동현_PleosWitness_release.apk

# 3. Pleos Witness 앱 바로 실행
adb shell am start -n io.pleos.snapreport/.ui.MainActivity
```

**앱 내 4단계 시연 순서**:
1. 좌측 컨트롤 패널의 **[🎙️ 1. 주행 중 음성 북마크 실행 (STT)]** 버튼 클릭 → 빌트인 캠 전후 15초 클립 추출 + 다중 프레임 초해상도 번호판 복원(`123가 4568`, 신뢰도 99.4%) + GPS/도로명(`강변북로 성수대교 북단 2차로`) 자동 태깅 확인
2. 주행 중(`D`단, 78km/h) 상태에서는 우측 안전신문고 접수 버튼이 **Safety Lock**으로 잠겨 있음을 확인
3. 좌측의 **[🅿️ 2. 정차(P) 상태 전환]** 버튼 클릭 → `Vehicle SDK` 주차 상태 감지 및 Safety Lock 즉시 해제
4. 우측의 **[🚀 행정안전부 안전신문고 원터치 접수]** 버튼 클릭 → 신고 접수 완료(`접수번호: 2026-1029-004812`) 및 `TextToSpeech SDK` 음성 브리핑 확인

---

### 방법 B. Android Studio에서 소스 코드 빌드 및 실행
```bash
cd android-app
./gradlew assembleRelease
```
- Android Studio에서 `android-app/` 폴더를 열고 Pleos Connect Emulator를 타깃으로 지정한 뒤 **Run (`Shift + F10`)** 하면 즉시 빌드·실행됩니다.

---

### 방법 C. 27인치 Pleos IVI 풀스택 시뮬레이터 & 무설치 웹 프로토타입 실행
실제 고속도로 블랙박스 주행 영상 위에서 실시간 OpenCV 객체 추적, 번호판 초해상도(SR) 복원 전후 비교, SHA-256 무결성 검증을 시각적으로 확인할 수 있는 시뮬레이터를 함께 제공합니다.

```bash
# 1) 무설치 단독 프로토타입 (더블클릭 또는 브라우저로 바로 열기)
open prototype/index.html

# 2) FastAPI + OpenCV 비전 AI 분석 백엔드 연동 시뮬레이터 실행
pip install fastapi uvicorn opencv-python numpy
uvicorn simulator.server:app --host 127.0.0.1 --port 8765
# 브라우저에서 http://127.0.0.1:8765 접속
```

---

## 6. 저장소 구조

```text
hmg-pleos-witness/
├── README.md                                                 # 프로젝트 소개·실행 방법·테스트 환경·에뮬레이터 규격 안내
├── apk/
│   ├── 이동현_PleosWitness_release.apk                        # [공식 규격] Pleos Connect SDK 5종 연동 서명된 Release APK
│   ├── PleosWitness_release.apk                              # Release APK 동일 빌드본
│   └── README.md                                             # APK 빌드 및 설치 안내
├── android-app/                                              # Pleos Connect SDK 기반 Android Automotive OS 네이티브 앱
│   ├── settings.gradle.kts                                   # 현대자동차그룹 Pleos Nexus Maven 저장소 설정
│   ├── build.gradle.kts
│   └── app/
│       ├── build.gradle.kts                                  # Android API 28+, Gradle 8+, Kotlin, Pleos SDK 5종 의존성
│       └── src/main/
│           ├── AndroidManifest.xml                           # pleos.car.permission.* 권한 및 Activity 선언
│           ├── res/raw/gleo_app_schema.json                  # Gleo AI 음성 명령 Function Calling 스키마
│           └── java/io/pleos/snapreport/
│               ├── domain/model/ViolationReportModels.kt     # 공익신고 도메인 모델·법규 코드·SHA-256 무결성 메타데이터
│               ├── data/pleos/PleosSdkCoordinator.kt         # Pleos Connect SDK 5대 모듈(Vehicle/Location/Navi/STT/TTS) 코디네이터
│               ├── data/vision/BuiltInCamAiAnalyzer.kt       # 빌트인 캠 클립 추출·다중프레임 초해상도 번호판 복원·무결성 서명
│               ├── data/mois/MoisSafetyReportApiClient.kt    # 행정안전부 안전신문고 Open API 연동 클라이언트 (제공 가정)
│               └── ui/MainActivity.kt                        # Pleos Connect 16:9 와이드 IVI Jetpack Compose 대시보드 UI
├── simulator/                                                # 27인치 Pleos IVI 시뮬레이터 (FastAPI + OpenCV 비전 AI 엔진)
│   ├── server.py
│   ├── vision_ai_engine.py
│   ├── index.html
│   └── assets/                                               # 블랙박스 주행 영상 및 AI 번호판 복원 결과 에셋
├── prototype/
│   └── index.html                                            # 무설치 단독 실행 가능한 1인칭 운전석 인터랙티브 프로토타입
├── docs/
│   ├── PleosWitness_프로토타입_및_APK_실행가이드.md             # 에뮬레이터·시뮬레이터 상세 실행 매뉴얼 (Markdown)
│   └── PleosWitness_프로토타입_및_APK_실행가이드.docx           # 에뮬레이터·시뮬레이터 상세 실행 매뉴얼 (Word)
└── videos/
    ├── PleosWitness_시연영상_16x9.mp4                         # 개발 산출물 시연 영상 (16:9, 1920x1080)
    └── PleosWitness_숏폼챌린지_9x16.mp4                       # 숏폼 챌린지 영상 (9:16, 1080x1920)
```
