# Pleos Witness — Release APK 안내

이 폴더에는 현대자동차그룹 공식 Maven 저장소(`https://nexus-playground.pleos.ai/repository/maven-releases/`)의 **Pleos Connect SDK 5대 모듈**을 연동하여 빌드·서명(v2 Scheme)한 Android Automotive OS 실행용 Release APK가 포함되어 있습니다.

## 파일 목록
- **`이동현_PleosWitness_release.apk`** (`팀명_앱이름_release.apk` 공식 제출 규격 파일명)
- **`PleosWitness_release.apk`** (동일 Release APK 빌드본)

## 에뮬레이터 설치 및 실행
```bash
adb install -r apk/이동현_PleosWitness_release.apk
adb shell am start -n io.pleos.snapreport/.ui.MainActivity
```

## 빌드 환경 및 사양
- **Package Name**: `io.pleos.snapreport`
- **Target Platform**: Pleos Connect Emulator (Android Automotive OS, `minSdk 28` / `targetSdk 34`, `1920×1080` 16:9 Landscape)
- **연동된 Pleos Connect SDK**:
  - `ai.pleos.playground:Vehicle:2.3.2`
  - `ai.pleos.playground:FusedLocation:1.1.3`
  - `ai.pleos.playground:NaviHelper:3.0.5`
  - `ai.pleos.playground:SpeechToText:3.3.1`
  - `ai.pleos.playground:TextToSpeech:3.3.1`
