#!/usr/bin/env python3
"""
Local FastAPI Backend for Pleos Witness Simulator:
- Serves the redesigned Hyundai Pleos Connect 27" IVI Simulator UI (index.html)
- Serves the real Vision AI processed dashcam videos & telemetry (/assets/*)
- Implements the Pleos SDK State Machine (/api/v1/pleos/*)
- Implements the Mock Ministry of the Interior and Safety (행정안전부 안전신문고) Open API (/api/v1/mois/safety-report)
"""
import datetime
import hashlib
import json
import os
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse, JSONResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel

SIM_DIR = os.path.dirname(os.path.abspath(__file__))
ASSET_DIR = os.path.join(SIM_DIR, "assets")

app = FastAPI(title="Pleos Witness IVI & Mock MOIS Safety e-Report Server", version="2.0.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.mount("/assets", StaticFiles(directory=ASSET_DIR), name="assets")

STATE = {
    "gear": "D",
    "speed_kmh": 88,
    "safety_lock": True,
    "bookmarked": False,
    "reported": False,
    "last_receipt": None,
}


class SafetyReportRequest(BaseModel):
    plate_number: str = "123가 4568"
    violation_code: str = "KR_RTA_ART38_1"
    violation_law: str = "도로교통법 제38조 제1항 (제14조 제5항 경합)"
    violation_title: str = "진로변경 방법 위반 (방향지시등 미점등 끼어들기)"
    occurred_at: str = "2026-11-02 14:22:18"
    location_address: str = "인천광역시 남동구 제2경인고속도로 서창분기점 부근 (인천 방향)"
    gps_lat: float = 37.4368
    gps_lng: float = 126.7352
    sha256_hash: str = ""
    third_party_privacy_masked: bool = True


@app.get("/")
def serve_index():
    return FileResponse(os.path.join(SIM_DIR, "index.html"))


@app.get("/api/v1/pleos/telemetry")
def get_telemetry():
    tel_path = os.path.join(ASSET_DIR, "ai_telemetry.json")
    with open(tel_path, encoding="utf-8") as f:
        data = json.load(f)
    return {"state": STATE, "telemetry": data}


@app.post("/api/v1/pleos/voice-bookmark")
def trigger_voice_bookmark():
    STATE["bookmarked"] = True
    STATE["gear"] = "D"
    STATE["speed_kmh"] = 88
    STATE["safety_lock"] = True
    return {
        "status": "BOOKMARKED",
        "voice_command": "플레오스, 방금 차 신고해 줘",
        "tts_response": "방금 끼어든 앞 차량의 전후 30초 영상과 위치 정보를 북마크했습니다. 정차 후 확인 창이 열립니다.",
        "safety_lock": True,
        "clip_duration_sec": 30,
    }


@app.post("/api/v1/pleos/gear/{gear}")
def shift_gear(gear: str):
    gear = gear.upper()
    STATE["gear"] = gear
    STATE["speed_kmh"] = 0 if gear == "P" else 88
    STATE["safety_lock"] = gear != "P"
    return {
        "status": "OK",
        "gear": STATE["gear"],
        "speed_kmh": STATE["speed_kmh"],
        "safety_lock": STATE["safety_lock"],
        "popup_unlocked": gear == "P" and STATE["bookmarked"],
    }


@app.post("/api/v1/mois/safety-report")
def submit_mois_safety_report(req: SafetyReportRequest):
    ai_video_path = os.path.join(ASSET_DIR, "dashcam_ai_analyzed.mp4")
    computed_sha256 = hashlib.sha256(open(ai_video_path, "rb").read()).hexdigest()
    receipt = {
        "status": "ACCEPTED",
        "api_provider": "행정안전부 안전신문고 Open API (Mock Server v1)",
        "receipt_id": "제2026-MOIS-0891422호",
        "submitted_at": datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "assigned_agency": "인천논현경찰서 교통과 (공익신고 전담팀)",
        "plate_number": req.plate_number,
        "violation_code": req.violation_code,
        "violation_law": req.violation_law,
        "violation_title": req.violation_title,
        "location_address": req.location_address,
        "gps": {"lat": req.gps_lat, "lng": req.gps_lng},
        "integrity_verified": True,
        "sha256_hash": computed_sha256,
        "privacy_masked": req.third_party_privacy_masked,
        "expected_disposition": "도로교통법 제38조 제1항 위반 과태료 40,000원 부과 사전통지서 발송 예정",
    }
    STATE["reported"] = True
    STATE["last_receipt"] = receipt
    return JSONResponse(status_code=201, content=receipt)
