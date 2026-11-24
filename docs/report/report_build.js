// 프로젝트 계획서(중간 점검본, 관리자 화면 구현 계획 포함) .docx 생성
// 사용법: node build.js <다이어그램 폴더> <출력 .docx>
const fs = require("fs");
const path = require("path");
const {
  Document, Packer, Paragraph, TextRun, ImageRun, Table, TableRow, TableCell,
  AlignmentType, HeadingLevel, WidthType, ShadingType, BorderStyle, LevelFormat,
  PageBreak, Footer, PageNumber, VerticalAlign, TableLayoutType,
} = require("docx");

const DIAG = process.argv[2];
const OUT = process.argv[3];
const FONT = "Malgun Gothic";
const NAVY = "2F4B7C";
const GREEN = "2E8B57";

// ---------- 공통 헬퍼 ----------
const r = (text, o = {}) => new TextRun({ text, font: FONT, ...o });
const p = (children, o = {}) =>
  new Paragraph({ children: typeof children === "string" ? [r(children)] : children, spacing: { after: 100, line: 300 }, ...o });
const h1 = (t) => new Paragraph({ heading: HeadingLevel.HEADING_1, children: [r(t)] });
const h2 = (t) => new Paragraph({ heading: HeadingLevel.HEADING_2, children: [r(t)] });
const bullet = (children) =>
  new Paragraph({ numbering: { reference: "bul", level: 0 }, spacing: { after: 60, line: 290 },
    children: typeof children === "string" ? [r(children)] : children });
const caption = (t) => new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 60, after: 240 },
  children: [r(t, { size: 18, color: "52606D" })] });
const gap = () => new Paragraph({ spacing: { after: 120 }, children: [] });

function pngSize(file) {
  const b = fs.readFileSync(file);
  return { w: b.readUInt32BE(16), h: b.readUInt32BE(20) };
}
function figure(file, maxWpt = 482, maxHpt = 640) {
  const full = path.join(DIAG, file);
  const { w, h } = pngSize(full);
  let width = maxWpt, height = (h / w) * maxWpt;
  if (height > maxHpt) { height = maxHpt; width = (w / h) * maxHpt; }
  return new Paragraph({ alignment: AlignmentType.CENTER, keepNext: true, spacing: { before: 120 },
    children: [new ImageRun({ type: "png", data: fs.readFileSync(full),
      transformation: { width: Math.round(width * 96 / 72), height: Math.round(height * 96 / 72) } })] });
}

const border = { style: BorderStyle.SINGLE, size: 4, color: "B8C2CC" };
const borders = { top: border, bottom: border, left: border, right: border };
// 행의 마지막 요소가 PLAN 객체면 그 행을 초록 배경(구현 예정)으로 표시
const PLAN = { plan: true };
function table(widths, header, rows, o = {}) {
  const total = widths.reduce((a, b) => a + b, 0);
  const head = o.headColor || NAVY;
  const cell = (content, i, isHead, plan) => new TableCell({
    width: { size: widths[i], type: WidthType.DXA },
    borders,
    verticalAlign: VerticalAlign.CENTER,
    shading: isHead ? { fill: head, type: ShadingType.CLEAR, color: "auto" }
      : plan ? { fill: "EEF9F1", type: ShadingType.CLEAR, color: "auto" } : undefined,
    margins: { top: 50, bottom: 50, left: 90, right: 90 },
    children: (Array.isArray(content) ? content : [content]).map((c) =>
      new Paragraph({ spacing: { after: 0, line: 260 },
        alignment: o.center && o.center.includes(i) ? AlignmentType.CENTER : AlignmentType.LEFT,
        children: [r(c, { size: 17, bold: isHead, color: isHead ? "FFFFFF" : undefined })] })),
  });
  return new Table({
    layout: TableLayoutType.FIXED,
    width: { size: total, type: WidthType.DXA },
    columnWidths: widths,
    rows: [
      new TableRow({ tableHeader: true, children: header.map((h, i) => cell(h, i, true, false)) }),
      ...rows.map((row) => {
        const plan = row[row.length - 1] === PLAN;
        const cells = plan ? row.slice(0, -1) : row;
        return new TableRow({ cantSplit: true, children: cells.map((c, i) => cell(c, i, false, plan)) });
      }),
    ],
  });
}

// ---------- 표지 ----------
const coverRow = (k, v) => new TableRow({ children: [
  new TableCell({ width: { size: 1900, type: WidthType.DXA }, borders,
    shading: { fill: "EEF2F7", type: ShadingType.CLEAR, color: "auto" },
    margins: { top: 90, bottom: 90, left: 140, right: 140 },
    children: [new Paragraph({ children: [r(k, { bold: true, size: 21 })] })] }),
  new TableCell({ width: { size: 6300, type: WidthType.DXA }, borders, margins: { top: 90, bottom: 90, left: 140, right: 140 },
    children: [new Paragraph({ children: [r(v, { size: 21 })] })] }),
] });

const cover = [
  new Paragraph({ spacing: { before: 2200, after: 200 }, alignment: AlignmentType.CENTER,
    children: [r("Spring Framework 텀프로젝트 계획서", { size: 26, color: "52606D" })] }),
  new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 160 },
    children: [r("성준대학교 학생정보시스템", { size: 52, bold: true, color: NAVY })] }),
  new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 1600 },
    children: [r("Spring Boot · Spring Data JPA 기반 학사정보 통합포털 및 학사관리 시스템", { size: 24 })] }),
  new Table({
    layout: TableLayoutType.FIXED, width: { size: 8200, type: WidthType.DXA }, columnWidths: [1900, 6300],
    alignment: AlignmentType.CENTER,
    rows: [
      coverRow("Git Repository", "https://github.com/seongjun1011/SpringFrameworkTermProject"),
      coverRow("과목명", "스프링 프레임워크"),
      coverRow("학과", "인공지능소프트웨어과"),
      coverRow("학년", "2학년"),
      coverRow("학번", "2301110329"),
      coverRow("성명", "박성준"),
      coverRow("작성일", "2026년 11월 24일 (중간 점검본)"),
    ],
  }),
  new Paragraph({ children: [new PageBreak()] }),
];

// ---------- 본문 ----------
const toc = [
  "1. 프로젝트 개요", "2. 주제", "3. 개발 환경 및 사용 기술", "4. 유스케이스 다이어그램",
  "5. 기능 명세 (기능별 처리 메서드 및 기술)", "6. 시스템 설계도", "7. DB 설계 (E-R Diagram)",
  "8. 시스템 실행 흐름도", "9. 향후 진행 계획",
];

const body = [
  new Paragraph({ spacing: { after: 200 }, children: [r("목 차", { bold: true, size: 30, color: NAVY })] }),
  ...toc.map((t) => new Paragraph({ spacing: { after: 120 }, indent: { left: 200 }, children: [r(t, { size: 22 })] })),
  new Paragraph({ spacing: { before: 400 }, shading: { fill: "EEF9F1", type: ShadingType.CLEAR, color: "auto" },
    border: { left: { style: BorderStyle.SINGLE, size: 24, color: GREEN, space: 8 } },
    children: [r("표기 안내: 본 문서는 중간 점검본이다. 그림과 표에서 ", { size: 19 }),
      r("* 표시, 초록색 점선, 초록 배경", { size: 19, bold: true, color: "1E6B41" }),
      r("으로 표시한 항목은 최종 제출 전까지 구현할 예정인 교직원(관리자) 기능이며, 그 외 항목은 구현을 완료하였다.", { size: 19 })] }),
  new Paragraph({ children: [new PageBreak()] }),

  // 1. 개요
  h1("1. 프로젝트 개요"),
  h2("1.1 배경"),
  p("1학년 1학기 웹어플리케이션개발 텀프로젝트에서 학교의 학생정보시스템/통합포털을 참고하여 학생정보시스템 화면과 기능을 만들었다. 당시에는 데이터를 메모리(List, Map)에만 보관하여 서버를 재시작하면 수강신청·신청 내역이 모두 사라졌고, 로그인도 고정된 데모 계정만 사용할 수 있었다."),
  p("이번 프로젝트에서는 이 홈페이지를 Spring Boot 프로젝트로 다시 구성하고, Spring Data JPA를 적용하여 계정·강좌·수강신청·각종 신청 내역 등 모든 데이터를 관계형 데이터베이스에 저장한다. 또한 실제 학사정보시스템처럼 학생 계정을 학교가 발급하고, 교직원이 학사 업무와 결재를 처리하는 관리자 화면까지 갖춘 시스템으로 확장한다."),
  h2("1.2 목적"),
  bullet("Spring MVC의 Controller – Service – Repository 계층 구조를 적용해 역할을 분리한다."),
  bullet("Spring Data JPA로 엔티티와 테이블을 매핑하고, 엔티티 간 연관관계(1:N, N:M)를 외래키로 표현한다."),
  bullet("회원가입 없이 학사관리자가 등록한 학적 정보로 로그인하고, 최초 로그인 시 비밀번호를 변경하도록 하여 실제 학사정보시스템의 계정 발급 방식을 따른다."),
  bullet("학생 화면과 교직원(관리자) 화면을 분리하고, 교직원의 역할(학사관리자·결재자)에 따라 사용할 수 있는 기능을 제한한다."),
  bullet("수강신청·결재처럼 여러 테이블을 함께 바꾸는 작업을 트랜잭션으로 묶고, 정원 초과·중복 신청을 DB 제약과 행 잠금으로 막는다."),
  bullet("계정마다 자신의 수강신청·시간표·신청 내역만 조회되도록 데이터를 학생 기준으로 분리한다."),
  h2("1.3 범위 및 진행 현황"),
  table([2200, 5638, 1800], ["구분", "범위", "상태"], [
    ["학생 – 계정", "로그인·로그아웃, 최초 로그인 비밀번호 변경 강제, 5회 실패 잠금, 비밀번호 변경·초기화", "구현 완료"],
    ["학생 – 수업", "수강신청·취소, 수강신청내역, 개인시간표", "구현 완료"],
    ["학생 – 학적·장학", "개인신상정보 수정, 휴학·복학 신청, 장학금 신청(첨부파일, 결재 진행 상태 조회)", "구현 완료"],
    ["학생 – 포털", "통합포털 대시보드(날씨 API), 공지사항·채용정보·학사일정 조회", "구현 완료"],
    ["수강신청 보완", "같은 요일·교시가 겹치는 강좌의 중복 신청 방지(시간 충돌 검사)", "구현 예정", PLAN],
    ["교직원 – 인증", "교직원 계정(staffs), 교직원 로그인, 관리자 접근 제어(AdminInterceptor)", "구현 예정", PLAN],
    ["학사관리자", "학생 학적 등록(초기 비밀번호 발급), 학생 목록, 잠금 해제·비밀번호 초기화, 강좌 개설·수정과 수강 현황, 공지사항·학사일정 등록", "구현 예정", PLAN],
    ["결재자", "결재함 조회, 휴학·장학금 신청 단계별 승인·반려", "구현 예정", PLAN],
  ], { center: [2] }),
  gap(),
  p("사이드바의 증명서발급·등록금·성적조회 메뉴는 화면 구조만 반영한 비활성 메뉴로 범위에서 제외한다. 현재 서버 시작 시 DataInitializer가 넣는 학생 명단과 데모 데이터는, 관리자 화면 구현 후 데모용 초기 데이터(교직원 계정 포함)로만 사용한다."),

  // 2. 주제
  h1("2. 주제"),
  new Paragraph({ spacing: { before: 60, after: 160 }, shading: { fill: "EEF2F7", type: ShadingType.CLEAR, color: "auto" },
    border: { left: { style: BorderStyle.SINGLE, size: 24, color: NAVY, space: 8 } },
    children: [r("1학년 때 제작한 학생정보시스템 홈페이지를 Spring Boot + Spring Data JPA 기반의 학사정보 통합포털 및 학사관리 시스템으로 재구성", { bold: true, size: 22 })] }),
  p("학생은 학사관리자가 발급한 학번과 초기 비밀번호로 로그인하여 비밀번호를 변경한 뒤, 통합포털 대시보드에서 주간 시간표·학사일정·공지사항·채용정보·날씨를 보고 수강신청, 개인시간표 조회, 개인신상정보 수정, 휴학·복학 및 장학금 신청을 할 수 있다."),
  p("교직원은 별도의 관리자 화면에 로그인한다. 학사관리자는 학생 학적 등록, 강좌 개설, 공지사항·학사일정 등록을 하고, 결재자(담당직원·학과장·장학위원회·학생처장)는 자신의 차례가 된 휴학·장학금 신청을 승인하거나 반려한다. 결재 결과는 학생 화면의 결재 진행 상태에 바로 반영된다. 모든 데이터는 H2 파일 데이터베이스에 저장되어 서버를 재시작해도 유지된다."),

  // 3. 개발 환경
  h1("3. 개발 환경 및 사용 기술"),
  table([2300, 7338], ["구분", "내용"], [
    ["언어 / 빌드", "Java 17, Maven"],
    ["프레임워크", "Spring Boot 3.2.5 (Spring MVC, 내장 Tomcat)"],
    ["데이터 접근", "Spring Data JPA (구현체 Hibernate), JpaRepository, 메서드 이름 기반 쿼리, @Query(JPQL), Pageable 페이징(예정)"],
    ["데이터베이스", "H2 Database (파일 모드, MySQL 호환 모드) – data/portaldb.mv.db, ddl-auto=update로 테이블 자동 생성"],
    ["화면", "Thymeleaf 템플릿 엔진 (layout fragment로 헤더·사이드바 공통화), HTML/CSS, 관리자 화면 templates/admin/*.html(예정)"],
    ["인증 / 보안", "HttpSession 기반 로그인(학생 loginStudent / 교직원 loginStaff 분리), HandlerInterceptor로 접근 제어(LoginInterceptor, AdminInterceptor 예정), 역할 기반 메뉴 제한, BCrypt 비밀번호 해시 (spring-security-crypto)"],
    ["외부 연동", "OpenWeatherMap REST API (RestTemplate) – 키 미설정 시 더미 데이터"],
    ["파일 업로드", "MultipartFile – uploads/ 폴더에 저장, DB에는 원본·저장 파일명 기록"],
    ["도구", "IntelliJ IDEA, Git / GitHub, IntelliJ Database 창·H2 Console(DB 조회)"],
  ]),
  gap(),

  // 4. 유스케이스
  h1("4. 유스케이스 다이어그램"),
  figure("5_유스케이스다이어그램.png", 482, 600),
  caption("[그림 1] 유스케이스 다이어그램 (초록 점선: 구현 예정)"),
  table([2000, 7638], ["액터", "설명"], [
    ["학생", "학사관리자가 등록한 학번으로 로그인하여 계정 관리, 수강신청·시간표, 학적·장학 신청, 포털·게시판 조회 기능을 사용한다."],
    ["학사관리자 *", "학생 학적 정보(학번·이름·학과·학년·생년월일)를 등록하여 초기 비밀번호(생년월일 6자리)를 발급하고, 계정 잠금 해제·비밀번호 초기화, 강좌 개설·수정, 공지사항·학사일정 등록을 한다.", PLAN],
    ["결재자 *", "담당직원·학과장·장학위원회·학생처장 역할의 교직원. 결재함에서 자신의 차례가 된 휴학·장학금 신청을 승인하거나 반려한다.", PLAN],
    ["OpenWeatherMap API", "대시보드의 날씨 위젯에 현재 날씨를 제공하는 외부 시스템이다."],
  ]),
  gap(),
  p([r("포함·확장 관계: ", { bold: true }), r("수강신청은 항상 정원·중복 검증(시간 충돌 검증 추가 예정)을 포함(<<include>>)하고, 휴학·장학금 신청은 첨부파일 업로드를 포함한다. 로그인은 5회 실패 시 계정 잠금, 초기 비밀번호 사용 시 비밀번호 변경 강제로 확장(<<extend>>)된다.")]),

  // 5. 기능 명세
  h1("5. 기능 명세 (기능별 처리 메서드 및 기술)"),
  p("각 기능은 Controller가 요청을 받고, Service가 업무 규칙과 트랜잭션을 처리하며, Repository(JpaRepository)가 DB에 접근하는 구조로 구현한다."),
  h2("5.1 학생 기능 (구현 완료)"),
  table([1450, 1750, 3238, 3200], ["기능", "요청 (URL)", "처리 메서드 (Controller → Service)", "사용 기술 · 구현 포인트"], [
    ["로그인 검사", "모든 학생 요청", "LoginInterceptor.preHandle()", "HandlerInterceptor, WebConfig에서 /login·/password/reset·정적 리소스 제외. 초기 비밀번호 사용자는 변경 화면만 허용"],
    ["로그인", "POST /login", "LoginController.login() → StudentService.login()", "BCrypt matches, 실패 횟수 증가·5회 잠금, 로그인 성공 시 세션 재발급, 마지막 로그인 시각·IP 저장(변경 감지)"],
    ["로그아웃", "GET /logout", "LoginController.logout()", "session.invalidate()"],
    ["비밀번호 변경", "GET·POST /password/change", "LoginController.change() → StudentService.changePassword()", "현재 비밀번호 확인, 정규식으로 영문+숫자 8~20자·학번/생년월일 포함 금지 검사, BCrypt encode"],
    ["비밀번호 초기화", "GET·POST /password/reset", "LoginController.reset() → StudentService.resetPassword()", "학번·이름·생년월일 대조 후 생년월일로 초기화, 잠금 해제, 다음 로그인 시 변경 강제"],
    ["통합포털 대시보드", "GET /", "DashboardController.dashboard()", "EnrollmentService·ScheduleService·NoticeService·JobService 조회, WeatherService(RestTemplate) 외부 API"],
    ["강좌 목록", "GET /courses", "CourseController.list() → CourseService.findAll()", "findAllByOrderByIdAsc(), 학생별 신청 여부 표시"],
    ["수강신청", "POST /courses/{id}/enroll", "CourseController.enroll() → EnrollmentService.enroll()", "@Transactional, @Lock(PESSIMISTIC_WRITE) 행 잠금, 중복 신청 검사 + UNIQUE(student_id, course_id), 정원 검사 후 enrolled_count +1"],
    ["수강취소", "POST /courses/{id}/cancel", "CourseController.cancel() → EnrollmentService.cancel()", "enrollments DELETE와 enrolled_count −1을 한 트랜잭션으로 처리"],
    ["수강신청내역 조회", "GET /enrollments", "CourseController.myEnrollments() → EnrollmentService.findEnrolledCourses()", "findByStudentIdOrderByAppliedAtAsc() – 로그인 학생의 id로만 조회"],
    ["개인시간표 조회", "GET /timetable", "TimetableController.timetable()", "신청 과목을 요일×교시 격자로 변환, 과목별 색상·주간 수업일수 계산"],
    ["개인신상정보 수정", "GET·POST /mypage", "MyPageController.updateMyPage() → StudentService.updateProfile()", "연락처·주소·계좌번호만 수정, 변경 감지(dirty checking)로 UPDATE, 세션 정보 갱신"],
    ["휴학·복학 신청", "GET·POST /leave", "LeaveController → LeaveService.apply() / findByStudent()", "MultipartFile 업로드(FileStorageService.store()), 결재단계·첨부파일을 @ElementCollection으로 저장"],
    ["장학금 신청", "GET·POST /scholarship", "ScholarshipController → ScholarshipService.apply() / findByStudent()", "휴학 신청과 동일 구조, 담당직원 → 장학위원회 → 학생처장 3단계 결재"],
    ["학사일정 조회", "GET /schedule", "ScheduleController → ScheduleService.findByMonth()", "기간이 해당 월과 겹치는 일정 조회 (StartDateLessThanEqual…EndDateGreaterThanEqual)"],
    ["공지사항 조회", "GET /notices, /notices/{id}", "NoticeController → NoticeService.findAll() / findById()", "findAllByOrderByCreatedAtDesc() 최신순"],
    ["채용정보 조회", "GET /jobs, /jobs/{id}", "JobController → JobService.findAll() / findById()", "findAllByOrderByRequestedDateDesc() 최신순, 상세 화면"],
    ["초기 데이터 등록", "서버 시작 시", "DataInitializer.run()", "CommandLineRunner, 비어 있는 테이블에만 데모 데이터 입력 (관리자 화면 구현 후 데모용으로만 사용)"],
  ]),
  gap(),
  h2("5.2 교직원(관리자) 기능 (구현 예정)"),
  table([1450, 1750, 3238, 3200], ["기능", "요청 (URL)", "처리 메서드 (Controller → Service)", "사용 기술 · 구현 포인트"], [
    ["관리자 접근 제어", "/admin/** 전체", "AdminInterceptor.preHandle()", "미로그인 → /admin/login. 학사관리자 전용 메뉴(/admin/students, courses, notices, schedules)와 결재자 전용 메뉴(/admin/approvals)를 역할로 구분", PLAN],
    ["교직원 로그인 · 로그아웃", "GET·POST /admin/login, GET /admin/logout", "AdminLoginController.login() → StaffService.login()", "사번 + BCrypt matches, 세션에 loginStaff 저장(학생 세션과 분리), last_login_at 갱신", PLAN],
    ["관리자 홈", "GET /admin", "AdminLoginController.home()", "역할별 메뉴, 대기 중인 결재 수 · 잠긴 학생 계정 수 표시", PLAN],
    ["학생 학적 등록", "GET·POST /admin/students/new", "AdminStudentController.create() → StudentService.register()", "Student.register() 재사용, 생년월일 6자리를 BCrypt로 초기 비밀번호 발급, existsByLoginId()로 학번 중복 검사", PLAN],
    ["학생 목록 조회", "GET /admin/students", "AdminStudentController.list() → StudentService.search()", "학번·이름 검색, Pageable 페이징, 잠금 여부 표시", PLAN],
    ["잠금 해제 · 비밀번호 초기화", "POST /admin/students/{id}/unlock, /reset-password", "AdminStudentController → StudentService.unlock() / resetByAdmin()", "학생이 직접 초기화할 수 없을 때 창구 역할, 변경 감지로 UPDATE", PLAN],
    ["강좌 개설 · 수정", "GET·POST /admin/courses/new, /admin/courses/{id}/edit", "AdminCourseController → CourseService.create() / update()", "요일·교시·정원 입력 검증, 정원을 현재 신청 인원보다 작게 줄이는 수정은 거부", PLAN],
    ["강좌별 수강신청 현황", "GET /admin/courses/{id}/enrollments", "AdminCourseController.enrollments() → EnrollmentService.findByCourse()", "enrollments – students 조인 조회 (findByCourseId…)", PLAN],
    ["공지사항 작성", "GET·POST /admin/notices/new", "AdminBoardController → NoticeService.write()", "작성 교직원을 notices.staff_id FK로 연결", PLAN],
    ["학사일정 등록", "GET·POST /admin/schedules/new", "AdminBoardController → ScheduleService.create()", "시작일 ≤ 종료일 검증", PLAN],
    ["결재함 조회", "GET /admin/approvals", "ApprovalController.inbox() → ApprovalService.findPendingFor(role)", "내 역할의 단계가 '대기'이고 앞 단계가 모두 승인된 휴학·장학금 신청만 조회", PLAN],
    ["승인 · 반려", "POST /admin/approvals/{type}/{id}/approve, /reject", "ApprovalController → ApprovalService.approve() / reject()", "@Transactional, 결재단계에 처리자(staff)·일시·의견 저장, 신청서 recalcStatus()로 상태 재계산", PLAN],
    ["수강신청 시간 충돌 검사", "POST /courses/{id}/enroll (보완)", "EnrollmentService.enroll()", "이미 신청한 강좌와 요일이 같고 교시 범위가 겹치면 신청 거부", PLAN],
  ], { headColor: GREEN }),
  gap(),

  // 6. 시스템 설계도
  h1("6. 시스템 설계도"),
  figure("1_시스템구성도.png"),
  caption("[그림 2] 시스템 구성도 (* 초록 점선: 구현 예정)"),
  bullet([r("요청 흐름: ", { bold: true }), r("브라우저 요청 → 인터셉터(학생: LoginInterceptor / 관리자: AdminInterceptor) → Controller → Service → Repository → Hibernate가 SQL 생성 → H2 DB")]),
  bullet([r("응답 흐름: ", { bold: true }), r("Controller가 Model에 데이터를 담아 Thymeleaf 템플릿 이름을 반환 → 서버에서 HTML을 생성하여 응답")]),
  bullet([r("Controller: ", { bold: true }), r("학생용 10개(구현 완료) + 관리자용 5개(AdminLogin, AdminStudent, AdminCourse, AdminBoard, Approval – 예정)")]),
  bullet([r("Service: ", { bold: true }), r("업무 규칙(정원·중복·비밀번호 정책·결재 순서)과 @Transactional 트랜잭션 경계. StaffService, ApprovalService 추가 예정")]),
  bullet([r("Repository: ", { bold: true }), r("JpaRepository를 상속하여 기본 CRUD와 메서드 이름 기반 쿼리를 자동 구현. StaffRepository 추가 예정")]),
  bullet([r("설정: ", { bold: true }), r("WebConfig(인터셉터 등록, 학생/관리자 경로 분리), PasswordConfig(BCryptPasswordEncoder 빈), DataInitializer(데모 데이터)")]),

  // 7. DB 설계
  h1("7. DB 설계 (E-R Diagram)"),
  figure("2_ERD.png"),
  caption("[그림 3] E-R 다이어그램 (구현 예정 포함: 엔티티 10개 ↔ 테이블 13개)"),
  h2("7.1 테이블 명세 (기본키 · 외래키 · 제약조건)"),
  table([2500, 1500, 2700, 2938], ["테이블 (엔티티)", "기본키 (PK)", "외래키 (FK) → 참조", "주요 컬럼 · 제약조건"], [
    ["students (Student)", "id (자동 증가)", "-", "login_id UNIQUE(학번), password(BCrypt), birth_date, password_change_required, login_fail_count, locked"],
    ["courses (Course)", "id", "-", "name, professor, day_of_week, start_period, end_period, capacity, enrolled_count"],
    ["enrollments (Enrollment)", "id", ["student_id → students.id", "course_id → courses.id"], "UNIQUE(student_id, course_id) – 같은 강좌 중복 신청 방지, applied_at"],
    ["leave_applications (LeaveApplication)", "id", "student_id → students.id", "apply_type(휴학/복학), reason, status, applied_at"],
    ["leave_applications_steps", "(application_id, step_order)", ["application_id → leave_applications.id", "processed_by → staffs.id *"], "approver_role, approver_name, step_status, processed_at, step_comment"],
    ["leave_applications_files", "(application_id, file_order)", "application_id → leave_applications.id", "original_name, stored_name"],
    ["scholarship_applications (ScholarshipApplication)", "id", "student_id → students.id", "apply_type(장학금 종류), reason, status, applied_at"],
    ["scholarship_applications_steps", "(application_id, step_order)", ["application_id → scholarship_applications.id", "processed_by → staffs.id *"], "휴학 결재단계와 동일 구조"],
    ["scholarship_applications_files", "(application_id, file_order)", "application_id → scholarship_applications.id", "original_name, stored_name"],
    ["notices (Notice)", "id", "staff_id → staffs.id *", "title, content, writer, created_at"],
    ["academic_schedules (AcademicSchedule)", "id", "-", "title, start_date, end_date"],
    ["job_postings (JobPosting)", "id", "-", "company, job_role, region, deadline, salary, description, requirements"],
    ["staffs (Staff) *", "id (자동 증가)", "-", "staff_no UNIQUE(사번), password(BCrypt), name, department, role(학사관리자·담당직원·학과장·장학위원회·학생처장), last_login_at", PLAN],
  ]),
  gap(),
  h2("7.2 테이블 간 참조 관계"),
  table([3000, 1100, 5538], ["관계", "카디널리티", "설명"], [
    ["students ─ enrollments ─ courses", "N : M", "학생 한 명은 여러 강좌를, 강좌 하나는 여러 학생을 가질 수 있어 enrollments 연결 테이블로 N:M을 두 개의 1:N으로 분리한다."],
    ["students → enrollments", "1 : N", "enrollments.student_id가 students.id를 참조한다. 시간표·신청내역은 이 FK로 로그인 학생의 데이터만 조회한다."],
    ["courses → enrollments", "1 : N", "enrollments.course_id가 courses.id를 참조한다. 신청 시 courses.enrolled_count를 함께 갱신한다."],
    ["students → leave_applications", "1 : N", "학생 한 명이 여러 번 휴학·복학을 신청할 수 있다."],
    ["students → scholarship_applications", "1 : N", "학생 한 명이 여러 장학금을 신청할 수 있다."],
    ["신청 → _steps / _files", "1 : N", "신청서 1건에 결재단계 3건과 첨부파일 여러 건이 딸린다. 신청서와 생명주기가 같아 별도 엔티티 대신 값 타입 컬렉션으로 저장한다."],
    ["staffs → _steps (processed_by) *", "1 : N", "결재단계를 처리한 교직원을 기록한다. 대기 중인 단계는 NULL이다.", PLAN],
    ["staffs → notices (staff_id) *", "1 : N", "공지사항을 작성한 교직원을 기록한다. 교직원 한 명이 여러 공지를 작성할 수 있다.", PLAN],
    ["academic_schedules / job_postings", "-", "다른 테이블과 연관관계가 없는 독립 테이블이다."],
  ], { center: [1] }),
  gap(),
  h2("7.3 JPA 매핑 방식"),
  table([3300, 6338], ["어노테이션", "적용 위치 · 의미"], [
    ["@Entity, @Table, @Id, @GeneratedValue(IDENTITY)", "모든 엔티티 – 클래스를 테이블에, id 필드를 자동 증가 기본키에 매핑"],
    ["@ManyToOne + @JoinColumn", "Enrollment.student / course, LeaveApplication.student, ScholarshipApplication.student – FK 컬럼 생성"],
    ["@Table(uniqueConstraints)", "enrollments (student_id, course_id) 복합 유니크 제약"],
    ["@Column(unique, nullable, length)", "students.login_id·staffs.staff_no 유일, 필수 컬럼 NOT NULL, 문자열 길이 지정"],
    ["@ElementCollection + @CollectionTable + @OrderColumn", "결재단계(@Embeddable ApprovalStep)·첨부파일(UploadedFile)을 신청별 하위 테이블에 순서대로 저장, (application_id, 순서)가 복합 기본키"],
    ["@Lock(PESSIMISTIC_WRITE) + @Query", "CourseRepository.findByIdForUpdate() – 수강신청 시 SELECT … FOR UPDATE로 정원 동시성 제어"],
    ["@Transactional", "Service 계층 – 기본 readOnly, 변경 메서드만 쓰기 트랜잭션. 엔티티 값 변경 시 변경 감지로 UPDATE 자동 실행"],
    ["@Enumerated(EnumType.STRING) *", "Staff.role – 교직원 역할을 문자열(ACADEMIC_ADMIN, STAFF, DEPT_HEAD, SCHOLARSHIP_COMMITTEE, DEAN)로 저장", PLAN],
    ["@Embeddable 안의 @ManyToOne *", "ApprovalStep.processedBy – 값 타입인 결재단계에서 처리 교직원(staffs)을 FK로 참조", PLAN],
  ]),

  // 8. 실행 흐름도
  h1("8. 시스템 실행 흐름도"),
  h2("8.1 학생 로그인 · 계정 흐름"),
  figure("3_실행흐름도_로그인.png", 482, 560),
  caption("[그림 4] 학생 로그인 실행 흐름도 (회원가입 없음 · 최초 로그인 비밀번호 변경)"),
  p("학생 계정은 학사관리자가 미리 등록하며 초기 비밀번호는 생년월일 6자리이다. 로그인 시 계정 잠금 여부와 비밀번호를 확인하고, 5회 실패하면 계정이 잠겨 비밀번호 초기화(본인 확인) 또는 학사관리자의 잠금 해제를 거쳐야 한다. 초기 비밀번호로 로그인한 학생은 비밀번호를 변경하기 전까지 다른 메뉴를 이용할 수 없다."),
  h2("8.2 학생 주요 기능 흐름"),
  figure("4_실행흐름도_주요기능.png"),
  caption("[그림 5] 학생 주요 기능 실행 흐름도"),
  p("대시보드에서 사이드바 메뉴를 선택하면 각 기능이 실행된다. 수강신청은 강좌 행을 잠근 상태에서 중복·정원을 검사한 뒤 신청 내역 INSERT와 신청 인원 증가를 한 트랜잭션으로 처리하고, 휴학·장학금 신청은 첨부파일 저장 후 신청서와 결재단계 3건을 함께 저장한다."),
  h2("8.3 교직원(관리자) 흐름 – 구현 예정"),
  figure("6_실행흐름도_관리자.png", 482, 560),
  caption("[그림 6] 교직원(관리자) 실행 흐름도"),
  p("교직원은 /admin/login에서 사번으로 로그인하며, 로그인한 교직원의 역할에 따라 메뉴가 나뉜다. 학사관리자는 학적·수업·게시판 관리 기능을, 결재자는 결재함을 사용한다. 결재자는 앞 단계가 모두 승인되어 자신의 차례가 된 신청만 처리할 수 있고, 승인·반려 시 결재단계에 처리자·일시·의견을 저장한 뒤 신청서 전체 상태를 다시 계산한다."),

  // 9. 향후 계획
  new Paragraph({ children: [new PageBreak()] }),
  h1("9. 향후 진행 계획"),
  table([2000, 7638], ["시점", "내용"], [
    ["~ 11월 30일", "Staff 엔티티·StaffRepository, 교직원 로그인과 AdminInterceptor(역할 확인), 데모 교직원 계정, 수강신청 시간 충돌 검사"],
    ["~ 12월 4일", "학생 학적 등록·목록·잠금 해제, 결재함과 승인·반려(ApprovalService)"],
    ["~ 12월 7일", "강좌 개설·수정과 수강 현황, 공지사항·학사일정 등록, 화면 다듬기"],
    ["~ 12월 9일", "대표 실행 화면 캡처, 다이어그램 갱신, 최종 보고서 작성 (표지·주제·설계도·ERD·흐름도·실행 화면)"],
    ["12월 10일(목) 자정 전", "최종 보고서 PDF 제출, 프로젝트 파일 GitHub(SpringFrameworkTermProject) 최종 커밋·푸시"],
  ]),
];

const margin = { top: 1134, bottom: 1134, left: 1134, right: 1134 };
const doc = new Document({
  creator: "박성준",
  title: "성준대학교 학생정보시스템 – 프로젝트 계획서",
  styles: {
    default: { document: { run: { font: FONT, size: 20 } } },
    paragraphStyles: [
      { id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { font: FONT, size: 30, bold: true, color: NAVY },
        paragraph: { spacing: { before: 360, after: 160 }, keepNext: true, outlineLevel: 0,
          border: { bottom: { style: BorderStyle.SINGLE, size: 8, color: NAVY, space: 4 } } } },
      { id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { font: FONT, size: 24, bold: true, color: "1F2933" },
        paragraph: { spacing: { before: 240, after: 100 }, keepNext: true, outlineLevel: 1 } },
    ],
  },
  numbering: { config: [{ reference: "bul", levels: [
    { level: 0, format: LevelFormat.BULLET, text: "•", alignment: AlignmentType.LEFT,
      style: { paragraph: { indent: { left: 400, hanging: 240 } } } },
  ] }] },
  sections: [
    { properties: { page: { margin }, titlePage: true }, children: cover },
    { properties: { page: { margin, pageNumbers: { start: 1 } } },
      footers: { default: new Footer({ children: [new Paragraph({ alignment: AlignmentType.CENTER,
        children: [r("- ", { size: 17, color: "8A94A3" }),
          new TextRun({ children: [PageNumber.CURRENT], font: FONT, size: 17, color: "8A94A3" }),
          r(" -", { size: 17, color: "8A94A3" })] })] }) },
      children: body },
  ],
});

Packer.toBuffer(doc).then((buf) => { fs.writeFileSync(OUT, buf); console.log("written", OUT); });
