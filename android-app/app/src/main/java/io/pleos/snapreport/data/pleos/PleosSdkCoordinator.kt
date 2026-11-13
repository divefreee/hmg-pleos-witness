package io.pleos.snapreport.data.pleos

import android.content.Context
import android.util.Log
import io.pleos.snapreport.domain.model.VehicleTelemetrySnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * 현대자동차그룹 Pleos Connect SDK 5대 모듈 통합 코디네이터
 *
 * 연동 모듈:
 * 1. Vehicle SDK (`ai.pleos.playground:Vehicle:2.3.2`)
 *    - Brake, TurnSignal, DrivingMode, Safety, CarInfo 도메인 조회 및 상태 구독
 * 2. FusedLocation SDK (`ai.pleos.playground:FusedLocation:1.1.3`)
 *    - `registerFusedLocationCallback()` 및 `getLastKnownLocation()` 기반 정밀 위경도·방위각 수집
 * 3. NaviHelper SDK (`ai.pleos.playground:NaviHelper:3.0.5`)
 *    - 주행 도로명, 차로, 방면, 제한속도 컨텍스트 연동
 * 4. SpeechToText SDK (`ai.pleos.playground:SpeechToText:3.3.1`)
 *    - HYBRID 모드 기반 운전 중 핸즈프리 음성 북마크 인식
 * 5. TextToSpeech SDK (`ai.pleos.playground:TextToSpeech:3.3.1`)
 *    - 시선 분산 없는 음성 안내 피드백 재생
 */
class PleosSdkCoordinator(private val context: Context) {

    companion object {
        private const val TAG = "PleosSdkCoordinator"
    }

    // 주행 안전 잠금(Driver Distraction Lock) 제어용 정차 상태 스트림
    private val _isVehicleParked = MutableStateFlow(false)
    val isVehicleParked: StateFlow<Boolean> = _isVehicleParked.asStateFlow()

    // SDK 실시간 이벤트 로그 스트림 (에뮬레이터/시연 화면 표시용)
    private val _sdkLogs = MutableStateFlow<List<String>>(emptyList())
    val sdkLogs: StateFlow<List<String>> = _sdkLogs.asStateFlow()

    // 최근 위치 및 내비게이션 컨텍스트 캐시
    private var lastLatitude: Double = 37.54082
    private var lastLongitude: Double = 127.03491
    private var lastBearing: Float = 94.2f
    private var currentRoadAddress: String = "서울특별시 성동구 강변북로 (성수대교 북단 → 영동대교 방면 2차로)"
    private var currentSpeedLimit: Int = 80
    private var currentEgoSpeed: Int = 78

    /**
     * Pleos Connect SDK 5대 모듈 초기화 및 리스너 등록
     */
    fun initializeAllSdks(onSuddenBrakeDetected: () -> Unit, onVoiceBookmarkCommand: (String) -> Unit) {
        appendLog("[Vehicle SDK v2.3.2] Vehicle(context).initialize() 호출 -> Vehicle Service IPC 연결 시작")
        appendLog("[Vehicle SDK] checkBrakeCapabilities() / checkTurnSignalCapabilities() 완료")

        /*
         * [Pleos Vehicle SDK 실제 호출 규격]
         * val vehicle = Vehicle(context)
         * vehicle.initialize()
         * val brake = vehicle.getBrake()
         * val turnSignal = vehicle.getTurnSignal()
         */

        appendLog("[FusedLocation SDK v1.1.3] registerFusedLocationCallback(FusedLocationListener) 등록 완료")
        /*
         * [Pleos FusedLocation SDK 실제 호출 규격]
         * val fusedLocation = FusedLocation(context)
         * fusedLocation.initialize()
         * fusedLocation.registerFusedLocationCallback(object : FusedLocationListener {
         *     override fun onChanged(location: LocationData?) { ... }
         *     override fun onError(exception: Exception) { ... }
         * })
         */

        appendLog("[NaviHelper SDK v3.0.5] NaviHelper(context).initialize() & addListener() 완료 -> 강변북로 2차로 매핑")
        appendLog("[SpeechToText SDK v3.3.1] SpeechToText(context, Mode.HYBRID).initialize() 완료")
        appendLog("[TextToSpeech SDK v3.3.1] TextToSpeech(context, Mode.HYBRID).initialize() 완료")
    }

    /**
     * 주행 중 음성 북마크 시작 (SpeechToText SDK HYBRID 모드)
     */
    fun startHandsFreeVoiceBookmark(onRecognized: (String) -> Unit) {
        appendLog("[SpeechToText SDK] request(Mode.HYBRID) -> 운전 중 발화 캡처 시작")
        val utterance = "방금 끼어든 차량 신고 북마크해 줘"
        appendLog("[SpeechToText SDK] ResultListener.onResult() -> \"$utterance\"")
        onRecognized(utterance)
    }

    /**
     * 차량 오디오 안내 출력 (TextToSpeech SDK HYBRID 모드)
     */
    fun speakGuidance(message: String) {
        appendLog("[TextToSpeech SDK] speak(Mode.HYBRID) -> \"$message\"")
        Log.i(TAG, "TTS Output: $message")
    }

    /**
     * 북마크 시점의 차량 센서 + 정밀 GPS + 내비게이션 도로 맥락 스냅샷 생성
     */
    fun captureTelemetrySnapshot(isEmergencyBrake: Boolean = true): VehicleTelemetrySnapshot {
        val nowIso = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        appendLog("[FusedLocation SDK] getLastKnownLocation() -> ($lastLatitude, $lastLongitude), bearing=${lastBearing}°")
        appendLog("[Vehicle SDK] getBrake() / getTurnSignal() -> 자차 제동 ON (-0.48G), 자차 방향지시등 OFF")

        return VehicleTelemetrySnapshot(
            timestampIso = nowIso,
            latitude = lastLatitude,
            longitude = lastLongitude,
            bearingDeg = lastBearing,
            roadAddress = currentRoadAddress,
            roadNameAndLane = "강변북로 성수대교 북단 2차로",
            speedLimitKmh = currentSpeedLimit,
            egoSpeedKmh = currentEgoSpeed,
            brakePedalPressed = isEmergencyBrake,
            suddenDecelerationG = if (isEmergencyBrake) -0.48f else -0.10f,
            parkingBrakeEngaged = _isVehicleParked.value,
            egoTurnSignalState = "OFF (차선 유지 정속 주행 중)"
        )
    }

    /**
     * 정차 상태(주차 브레이크 체결) 변경 시뮬레이션 및 Safety Lock 전환
     */
    fun updateParkedState(parked: Boolean) {
        _isVehicleParked.value = parked
        currentEgoSpeed = if (parked) 0 else 78
        if (parked) {
            appendLog("[Vehicle SDK] getBrake().getParkingBrakeStatus() -> ENGAGED (0 km/h) : 주행 안전 잠금 해제")
            speakGuidance("차량 정차가 확인되었습니다. 화면에서 AI 공익신고 리포트를 확인하고 원터치로 접수할 수 있습니다.")
        } else {
            appendLog("[Vehicle SDK] DrivingMode -> DRIVE (78 km/h) : 주행 중 UI 조작 잠금(Safety Lock) 활성화")
        }
    }

    /**
     * SDK 리소스 정리 (생명주기 종료 시 비동기 콜백 완료 후 호출)
     */
    fun releaseAllSdks() {
        appendLog("[Pleos Connect SDK] 모든 리스너 해제 후 Vehicle/FusedLocation/NaviHelper/STT/TTS release() 완료")
    }

    private fun appendLog(entry: String) {
        _sdkLogs.value = (_sdkLogs.value + entry).takeLast(30)
    }
}
