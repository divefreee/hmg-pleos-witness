package io.pleos.snapreport.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.pleos.snapreport.data.mois.MoisSafetyReportApiClient
import io.pleos.snapreport.data.pleos.PleosSdkCoordinator
import io.pleos.snapreport.data.vision.BuiltInCamAiAnalyzer
import io.pleos.snapreport.domain.model.ReportStatus
import io.pleos.snapreport.domain.model.SafetyReportDraft
import kotlinx.coroutines.launch

/**
 * Pleos Connect 차량용 인포테인먼트 와이드 디스플레이 메인 액티비티
 */
class MainActivity : ComponentActivity() {

    private lateinit var pleosCoordinator: PleosSdkCoordinator
    private val camAnalyzer = BuiltInCamAiAnalyzer()
    private val moisClient = MoisSafetyReportApiClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pleosCoordinator = PleosSdkCoordinator(applicationContext)
        pleosCoordinator.initializeAllSdks(
            onSuddenBrakeDetected = {},
            onVoiceBookmarkCommand = {}
        )

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                SnapReportDashboardScreen(
                    pleosCoordinator = pleosCoordinator,
                    camAnalyzer = camAnalyzer,
                    moisClient = moisClient
                )
            }
        }
    }

    override fun onDestroy() {
        pleosCoordinator.releaseAllSdks()
        super.onDestroy()
    }
}

@Composable
fun SnapReportDashboardScreen(
    pleosCoordinator: PleosSdkCoordinator,
    camAnalyzer: BuiltInCamAiAnalyzer,
    moisClient: MoisSafetyReportApiClient
) {
    val scope = rememberCoroutineScope()
    val isParked by pleosCoordinator.isVehicleParked.collectAsState()
    val sdkLogs by pleosCoordinator.sdkLogs.collectAsState()
    var currentDraft by remember { mutableStateOf<SafetyReportDraft?>(null) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B12))
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 좌측: 빌트인 캠 2 + Edge AI 비전 상태 & Pleos Connect SDK 실시간 로그
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF0F1623), RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFF263248), RoundedCornerShape(18.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "📹 BUILT-IN CAM 2 · EDGE AI ANPR",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isParked) "🅿️ 정차 중 (P)" else "🚗 주행 중 (D · 78km/h)",
                            color = if (isParked) Color(0xFF34D399) else Color(0xFFFBBF24),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = currentDraft?.let {
                                "🎯 AI 검출 완료: [${it.aiVision.recognizedPlate}] ${it.aiVision.detectedViolation.titleKo}"
                            } ?: "주행 중 위반 차량 목격 시 [음성 북마크] 버튼을 누르거나 '방금 차 신고 북마크'라고 말씀하세요.",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "📍 위치(FusedLocation+NaviHelper): 서울특별시 성동구 강변북로 (성수대교 북단 2차로)",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                pleosCoordinator.startHandsFreeVoiceBookmark { utterance ->
                                    scope.launch {
                                        val telemetry = pleosCoordinator.captureTelemetrySnapshot(isEmergencyBrake = true)
                                        val vision = camAnalyzer.extractClipAndAnalyzeViolation(telemetry, utterance)
                                        val (title, body) = camAnalyzer.generateOfficialReportDescription(telemetry, vision)
                                        currentDraft = SafetyReportDraft(
                                            reportId = "RPT-20261029-001",
                                            telemetry = telemetry,
                                            aiVision = vision,
                                            autoGeneratedTitle = title,
                                            autoGeneratedBody = body
                                        )
                                        pleosCoordinator.speakGuidance(
                                            "전방 차량 ${vision.recognizedPlate} 진로변경 위반 클립 30초와 위치가 북마크되었습니다. 정차 후 접수해 주세요."
                                        )
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00AAD2))
                        ) {
                            Text("🎙️ 1. 주행 중 음성 북마크 실행 (STT/TTS)", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { pleosCoordinator.updateParkedState(!isParked) }
                        ) {
                            Text(
                                text = if (isParked) "🚗 주행(D) 상태 전환" else "🅿️ 2. 정차(P) 상태 전환 (Safety Lock 해제)",
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 하단: Pleos Connect SDK 실시간 IPC 버스 로그
            Column(
                modifier = Modifier
                    .height(210.dp)
                    .fillMaxWidth()
                    .background(Color(0xFF0B111C), RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "⚡ Pleos Connect SDK (Vehicle · FusedLocation · NaviHelper · STT · TTS) 실시간 로그",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(sdkLogs) { line ->
                        Text(
                            text = line,
                            color = Color(0xFFCBD5E1),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 우측: 행정안전부 안전신문고 AI 자동 작성 카드 & 원터치 제출
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFF0F1623), RoundedCornerShape(18.dp))
                .border(1.dp, Color(0xFF263248), RoundedCornerShape(18.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "🛡️ Pleos Witness · 차안의 AI 안전신문고",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = if (isParked) {
                        "🔓 [Vehicle SDK 정차 확인 완료] 주차 브레이크 체결이 확인되어 원터치 접수가 활성화되었습니다."
                    } else {
                        "🔒 [Vehicle SDK 주행 안전 제한] 운전 중 시선 분산을 막기 위해 정차(P) 후에만 최종 제출이 가능합니다."
                    },
                    color = if (isParked) Color(0xFF34D399) else Color(0xFFFBBF24),
                    fontSize = 12.sp
                )

                Divider(color = Color(0xFF1E293B))

                val draft = currentDraft
                if (draft == null) {
                    Text(
                        text = "북마크된 교통위반 클립이 없습니다.\n좌측 [🎙️ 1. 주행 중 음성 북마크 실행] 버튼을 눌러 테스트하세요.",
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = "• 피신고 차량번호: ${draft.aiVision.recognizedPlate} (초해상도 복원 신뢰도 99.4%)",
                        color = Color(0xFF34D399),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "• 위반 법규: ${draft.aiVision.detectedViolation.titleKo}",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "• 위반 장소: ${draft.telemetry.roadAddress}",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• 첨부 영상: 30초 무편집 클립 + 제3자 번호판 ${draft.aiVision.privacyMaskedObjectsCount}건 자동 모자이크",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = draft.autoGeneratedBody,
                        color = Color(0xFFE2E8F0),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    if (draft.status == ReportStatus.SUBMITTED_COMPLETED) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "✅ 접수 완료: ${draft.moisReceiptNumber} (${draft.assignedAgency})",
                            color = Color(0xFF34D399),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val draft = currentDraft ?: return@Button
                    scope.launch {
                        val result = moisClient.submitTrafficViolationReport(draft)
                        result.onSuccess { receipt ->
                            currentDraft = draft.copy(
                                status = ReportStatus.SUBMITTED_COMPLETED,
                                moisReceiptNumber = receipt.receiptNumber,
                                assignedAgency = receipt.assignedPoliceAgency
                            )
                            pleosCoordinator.speakGuidance(
                                "행정안전부 안전신문고 원터치 접수가 완료되었습니다. 접수번호는 ${receipt.receiptNumber} 입니다."
                            )
                        }
                    }
                },
                enabled = isParked && currentDraft != null && currentDraft?.status != ReportStatus.SUBMITTED_COMPLETED,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text(
                    text = when {
                        currentDraft == null -> "음성 북마크 후 활성화됩니다"
                        !isParked -> "🔒 정차(P) 후 안전신문고 원터치 접수 가능"
                        currentDraft?.status == ReportStatus.SUBMITTED_COMPLETED -> "✅ 안전신문고 접수 완료"
                        else -> "🚀 3. 행정안전부 안전신문고 API 원터치 접수"
                    },
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
