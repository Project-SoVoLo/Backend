# 💬 SOVOLO - soul, voice, log 🥯
소보로는 사용자의 감정을 담은 목소리를 기록하고, 기억하는 음성 인식 기반 정신건강 상담 챗봇 서비스 입니다.

## 🎬 최종 발표 자료

발표 자료: [소보로_프로토타입_발표](https://www.miricanvas.com/v/14sbzxd)

## 🚀 실행 방법

간혹 로그인 세션이 갱신되지 않거나, 로그인 후에도 이전 화면이 유지되는 경우가 있습니다.
이럴 때는 **브라우저 캐시를 삭제** 한 후 새로고침 해주세요.

#### ✅ 프론트엔드와 백엔드 따로 실행 - 개발중인 상태, 프로토타입 실행 시

1. 백엔드 실행
```
cd Backend
./gradlew bootRun
```
기본 포트: http://localhost:8080
</br>

2. 프론트엔드 실행
```
cd Frontend/client
npm install
npm start
```
기본 포트: http://localhost:3000

3. 챗봇 서버 실행
> 챗봇 대화 요약
```
cd chatbot/gemini_summary
python3 gemini_server.py
```
> 챗봇 응답 모델
```
cd chatbot/rasa_chatbot
rasa run --enable-api --cors "*" --debug
```
> 텍스트 감정 점수 추출 모델
```
cd phq9-flask-api
python3 app.py
```

## 🔐 환경변수 (.env)

일부 민감 정보를 .env 또는 환경설정 파일로 관리합니다.
로컬 개발 중에는 API KEY를 팀원 간 내부공유하여 사용중입니다.

## ❓ 문의

[✉️ 메일로 문의](mailto:03dayun03@yu.ac.kr)


## 커밋 메세지 작성 규칙
작성 방법 : ```[카테고리] 작업 내용 요약```

|카테고리|설명|
|--|--|
|추가|새로운 기능 추가|
|수정|기존 기능 수정, 버그 수정|
|삭제|불필요한 내용 삭제, 기능 삭제|
|문서|코드 편집X, 관련 문서 업로드 할 때|
|테스트|테스트 코드 작업 시|
|환경|빌드, 설정 파일 등 수정, DB 연결 작업|
