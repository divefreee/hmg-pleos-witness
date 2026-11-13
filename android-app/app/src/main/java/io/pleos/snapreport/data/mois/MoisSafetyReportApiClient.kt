package io.pleos.snapreport.data.mois

import io.pleos.snapreport.domain.model.SafetyReportDraft
import kotlinx.coroutines.delay

/**
 * 행정안전부 안전신문고(Safety e-Report) 교통위반 공익신고 오픈 API 클라이언트 (API 제공 가정 설계)
 *
 * 엔드포인트 규격 (가정):
 * - POST https://api.safetyreport.go.kr/v2/safety-report/traffic
 * - Content-Type: multipart/form-data
 * - Headers:
 *   - Authorization: Bearer <MOIS_VEHICLE_OAUTH_TOKEN>
 *   - X-Pleos-Telemetry-Signature: <SHA256_HMAC>
 * - Payload:
 *   - violation_type_code: MOIS-TRF-101
 *   - target_plate_number: "123가 4568"
 *   - occurred_at: "2026-10-29T08:42:19+09:00"
 *   - latitude / longitude / road_address
 *   - report_title / report_body
 *   - evidence_video_clip (30s MP4)
 *   - evidence_plate_crop (Super-Res PNG)
 */
data class MoisSubmissionReceipt(
    val receiptNumber: String,
    val submittedAtIso: String,
    val assignedPoliceAgency: String,
    val statusMessage: String
)

class MoisSafetyReportApiClient {

    suspend fun submitTrafficViolationReport(draft: SafetyReportDraft): Result<MoisSubmissionReceipt> {
        return runCatching {
            // 네트워크 전송 시뮬레이션
            delay(450)
            MoisSubmissionReceipt(
                receiptNumber = "제2026-MOIS-0891422호",
                submittedAtIso = draft.telemetry.timestampIso,
                assignedPoliceAgency = "서울성동경찰서 교통과",
                statusMessage = "안전신문고 공익신고 접수가 완료되었습니다. (빌트인 캠 진본 서명 확인됨)"
            )
        }
    }
}
