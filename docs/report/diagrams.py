"""보고서용 다이어그램(SVG) 생성기. 각 다이어그램을 SVG/HTML로 쓰고, 이후 Chrome headless로 PNG 변환."""
import html
import os
import sys

OUT = sys.argv[1]
os.makedirs(OUT, exist_ok=True)

FONT = "'Apple SD Gothic Neo','Malgun Gothic','Noto Sans KR',sans-serif"
INK = "#1F2933"
MUTED = "#52606D"
LINE = "#4A5568"

C = {  # (fill, stroke)
    "ctrl": ("#E8F0FE", "#3B6FD8"),
    "svc": ("#E6F4EA", "#2E8B57"),
    "repo": ("#FFF3E0", "#D9822B"),
    "common": ("#F3EAFD", "#8A4FD8"),
    "view": ("#FDE8EF", "#C2185B"),
    "ext": ("#EEF1F4", "#607080"),
    "proc": ("#E8F0FE", "#3B6FD8"),
    "dec": ("#FFF4E0", "#D9822B"),
    "db": ("#E6F4EA", "#2E8B57"),
    "term": ("#E3E8EF", "#4A5568"),
}


class Svg:
    def __init__(self, w, h):
        self.w, self.h = w, h
        self.parts = []

    def add(self, s):
        self.parts.append(s)

    def text(self, x, y, s, size=14, weight=400, fill=INK, anchor="middle", lh=1.38, halo=False, italic=False):
        lines = s.split("\n")
        total = (len(lines) - 1) * size * lh
        y0 = y - total / 2 + size * 0.36
        halo_attr = ' stroke="#FFFFFF" stroke-width="5" stroke-linejoin="round" paint-order="stroke"' if halo else ""
        style = ' font-style="italic"' if italic else ""
        spans = "".join(
            f'<tspan x="{x}" y="{y0 + i * size * lh:.1f}">{html.escape(t)}</tspan>' for i, t in enumerate(lines)
        )
        self.add(f'<text font-size="{size}" font-weight="{weight}" fill="{fill}" text-anchor="{anchor}"{style}{halo_attr}>{spans}</text>')

    def rect(self, x, y, w, h, fill, stroke, rx=8, sw=1.6, dash=None):
        d = f' stroke-dasharray="{dash}"' if dash else ""
        self.add(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{rx}" fill="{fill}" stroke="{stroke}" stroke-width="{sw}"{d}/>')

    def box(self, cx, cy, w, h, label, kind, size=14, weight=400, rx=8):
        fill, stroke = C[kind]
        self.rect(cx - w / 2, cy - h / 2, w, h, fill, stroke, rx=rx)
        self.text(cx, cy, label, size=size, weight=weight)

    def term(self, cx, cy, w, h, label, size=14):
        fill, stroke = C["term"]
        self.rect(cx - w / 2, cy - h / 2, w, h, fill, stroke, rx=h / 2)
        self.text(cx, cy, label, size=size, weight=600)

    def diamond(self, cx, cy, w, h, label, size=14):
        fill, stroke = C["dec"]
        pts = f"{cx},{cy - h / 2} {cx + w / 2},{cy} {cx},{cy + h / 2} {cx - w / 2},{cy}"
        self.add(f'<polygon points="{pts}" fill="{fill}" stroke="{stroke}" stroke-width="1.6"/>')
        self.text(cx, cy, label, size=size)

    def arrow(self, pts, label=None, at=None, anchor="middle", dashed=False, head=True, size=13):
        d = " ".join(f"{x},{y}" for x, y in pts)
        dash = ' stroke-dasharray="6 4"' if dashed else ""
        mk = ' marker-end="url(#arr)"' if head else ""
        self.add(f'<polyline points="{d}" fill="none" stroke="{LINE}" stroke-width="1.6"{dash}{mk}/>')
        if label:
            self.text(at[0], at[1], label, size=size, fill=MUTED, anchor=anchor, halo=True)

    def layer(self, x, y, w, h, title, kind, items=(), body=None, item_h=34, gap=8):
        fill, stroke = C[kind]
        self.rect(x, y, w, h, "#FFFFFF", stroke, rx=10, sw=1.8)
        self.add(f'<path d="M{x},{y + 40} V{y + 10} a10,10 0 0 1 10,-10 H{x + w - 10} a10,10 0 0 1 10,10 V{y + 40} Z" fill="{stroke}"/>')
        self.text(x + w / 2, y + 20, title, size=14, weight=700, fill="#FFFFFF")
        iy = y + 52
        for it in items:
            self.rect(x + 10, iy, w - 20, item_h, fill, stroke, rx=6, sw=1)
            self.text(x + w / 2, iy + item_h / 2, it, size=13)
            iy += item_h + gap
        if body:
            top = iy if items else y + 40
            self.text(x + w / 2, (top + y + h) / 2, body, size=13, fill=MUTED)

    def cylinder(self, x, y, w, h, label):
        fill, stroke = C["ext"]
        ry = 16
        self.add(
            f'<path d="M{x},{y + ry} V{y + h - ry} A{w / 2},{ry} 0 0 0 {x + w},{y + h - ry} V{y + ry}" '
            f'fill="{fill}" stroke="{stroke}" stroke-width="1.8"/>'
        )
        self.add(f'<ellipse cx="{x + w / 2}" cy="{y + ry}" rx="{w / 2}" ry="{ry}" fill="#F7F9FA" stroke="{stroke}" stroke-width="1.8"/>')
        self.text(x + w / 2, y + h / 2 + 10, label, size=13)

    def legend(self, x, y, entries):
        for i, (kind, name) in enumerate(entries):
            fill, stroke = C[kind]
            yy = y + i * 26
            if kind == "dec":
                self.add(f'<polygon points="{x + 14},{yy} {x + 28},{yy + 9} {x + 14},{yy + 18} {x},{yy + 9}" fill="{fill}" stroke="{stroke}" stroke-width="1.4"/>')
            else:
                self.rect(x, yy, 28, 18, fill, stroke, rx=9 if kind == "term" else 4, sw=1.4)
            self.text(x + 38, yy + 9, name, size=13, anchor="start", fill=MUTED)

    def save(self, name):
        svg = (
            f'<svg xmlns="http://www.w3.org/2000/svg" width="{self.w}" height="{self.h}" viewBox="0 0 {self.w} {self.h}" '
            f'font-family="{FONT}">'
            '<defs><marker id="arr" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">'
            f'<path d="M0,0 L10,5 L0,10 z" fill="{LINE}"/></marker></defs>'
            f'<rect width="{self.w}" height="{self.h}" fill="#FFFFFF"/>' + "".join(self.parts) + "</svg>"
        )
        with open(os.path.join(OUT, name + ".svg"), "w") as f:
            f.write(svg)
        with open(os.path.join(OUT, name + ".html"), "w") as f:
            f.write(f'<!doctype html><meta charset="utf-8"><style>html,body{{margin:0;background:#fff}}</style>{svg}')
        print(name, self.w, self.h)


# ---------------------------------------------------------------- 1. 시스템 구성도
s = Svg(1510, 960)
s.box(110, 205, 180, 120, "사용자 브라우저\n(Chrome 등)", "ext", weight=600)
s.rect(250, 80, 980, 720, "#FAFBFC", "#9AA5B1", rx=14, sw=1.5)
s.text(272, 104, "Spring Boot 3.2.5 애플리케이션  ·  내장 Tomcat (포트 8080)", size=15, weight=700, anchor="start")

s.layer(280, 130, 180, 150, "LoginInterceptor", "common", body="로그인 여부 검사\n미로그인 → /login\n(/login, /signup 제외)")
s.layer(280, 310, 180, 130, "HttpSession", "common", body="로그인한 학생 정보\n(loginStudent)")
s.layer(280, 470, 180, 160, "설정 (config)", "common", items=["WebConfig", "PasswordConfig (BCrypt)", "DataInitializer"], item_h=28, gap=7)

ctrls = ["LoginController", "DashboardController", "CourseController", "TimetableController", "MyPageController",
         "LeaveController", "ScholarshipController", "NoticeController", "JobController", "ScheduleController"]
svcs = ["StudentService", "EnrollmentService", "CourseService", "LeaveService", "ScholarshipService",
        "NoticeService", "JobService", "ScheduleService", "FileStorageService", "WeatherService"]
repos = ["StudentRepository", "EnrollmentRepository", "CourseRepository", "LeaveApplicationRepository",
         "ScholarshipApplicationRepository", "NoticeRepository", "JobPostingRepository", "AcademicScheduleRepository"]
s.layer(490, 130, 200, 500, "Controller", "ctrl", items=ctrls)
s.layer(720, 130, 200, 500, "Service", "svc", items=svcs)
s.layer(950, 130, 250, 500, "Repository (Spring Data JPA)", "repo", items=repos,
        body="JpaRepository 상속\n메서드 이름으로 쿼리 자동 생성")

s.layer(490, 670, 200, 100, "View (Thymeleaf)", "view", body="templates/*.html\n서버에서 HTML 생성")
s.layer(950, 670, 250, 100, "Hibernate (JPA 구현체)", "repo", body="@Entity 9개 ↔ 테이블 12개 매핑\nSQL 자동 생성 · 트랜잭션")

s.arrow([(200, 205), (278, 205)], "HTTP 요청", (239, 188))
s.arrow([(460, 205), (488, 205)])
s.arrow([(690, 380), (718, 380)])
s.arrow([(920, 380), (948, 380)])
s.arrow([(590, 630), (590, 668)], "Model 전달", (600, 650), anchor="start")
s.arrow([(1075, 630), (1075, 668)])
s.arrow([(590, 770), (590, 830), (110, 830), (110, 267)], "HTML 응답", (350, 830))
s.arrow([(1200, 720), (1278, 720)], "SQL (JDBC)", (1240, 703))

s.cylinder(1280, 640, 200, 160, "H2 Database\n파일 DB · MySQL 모드\ndata/portaldb.mv.db")
s.box(1380, 515, 200, 90, "IntelliJ Database 창\nH2 Console\n(/h2-console)", "ext", size=13)
s.arrow([(1380, 560), (1380, 638)], "직접 조회", (1392, 600), anchor="start", dashed=True)

s.box(770, 890, 170, 70, "uploads/ 폴더\n첨부파일 저장", "ext", size=13)
s.box(975, 890, 190, 70, "OpenWeatherMap API\n날씨 정보 (REST)", "ext", size=13)
s.arrow([(770, 630), (770, 853)], "FileStorageService\n파일 저장", (758, 830), anchor="end", size=12)
s.arrow([(900, 630), (900, 853)], "WeatherService\nREST 호출", (912, 830), anchor="start", size=12)
s.save("1_시스템구성도")

# ---------------------------------------------------------------- 2. E-R 다이어그램
VC = "VARCHAR"
T = {
    "students": ("학생", [("PK", "id", "BIGINT"), ("UK", "login_id", f"{VC}(20)"), ("", "password", f"{VC}(255)"),
                         ("", "name", f"{VC}(30)"), ("", "department", f"{VC}(255)"), ("", "campus", f"{VC}(255)"),
                         ("", "grade", f"{VC}(255)"), ("", "phone", f"{VC}(255)"), ("", "address", f"{VC}(255)"),
                         ("", "account_number", f"{VC}(255)"), ("", "last_login_at", "TIMESTAMP"),
                         ("", "last_login_ip", f"{VC}(255)")], None),
    "enrollments": ("수강신청", [("PK", "id", "BIGINT"), ("FK", "student_id", "BIGINT"), ("FK", "course_id", "BIGINT"),
                               ("", "applied_at", "TIMESTAMP")], "UNIQUE (student_id, course_id)"),
    "courses": ("강좌", [("PK", "id", "BIGINT"), ("", "name", f"{VC}(50)"), ("", "professor", f"{VC}(30)"),
                       ("", "day_of_week", f"{VC}(5)"), ("", "start_period", "INT"), ("", "end_period", "INT"),
                       ("", "location", f"{VC}(20)"), ("", "capacity", "INT"), ("", "enrolled_count", "INT")], None),
    "leave_applications": ("휴학·복학 신청", [("PK", "id", "BIGINT"), ("FK", "student_id", "BIGINT"),
                                          ("", "apply_type", f"{VC}(30)"), ("", "reason", f"{VC}(1000)"),
                                          ("", "status", f"{VC}(10)"), ("", "applied_at", "TIMESTAMP")], None),
    "scholarship_applications": ("장학금 신청", [("PK", "id", "BIGINT"), ("FK", "student_id", "BIGINT"),
                                             ("", "apply_type", f"{VC}(30)"), ("", "reason", f"{VC}(1000)"),
                                             ("", "status", f"{VC}(10)"), ("", "applied_at", "TIMESTAMP")], None),
    "notices": ("공지사항", [("PK", "id", "BIGINT"), ("", "title", f"{VC}(255)"), ("", "content", f"{VC}(2000)"),
                         ("", "writer", f"{VC}(255)"), ("", "created_at", "TIMESTAMP")], None),
    "academic_schedules": ("학사일정", [("PK", "id", "BIGINT"), ("", "title", f"{VC}(100)"),
                                    ("", "start_date", "DATE"), ("", "end_date", "DATE")], None),
    "job_postings": ("채용정보", [("PK", "id", "BIGINT"), ("", "company", f"{VC}(50)"), ("", "job_role", f"{VC}(100)"),
                              ("", "region", f"{VC}(30)"), ("", "deadline", "DATE"), ("", "requested_date", "DATE"),
                              ("", "employment_type", f"{VC}(30)"), ("", "salary", f"{VC}(50)"),
                              ("", "description", f"{VC}(2000)"), ("", "requirements", f"{VC}(2000)")], None),
}
steps_cols = [("PK FK", "application_id", "BIGINT"), ("PK", "step_order", "INT"), ("", "approver_role", f"{VC}(30)"),
              ("", "approver_name", f"{VC}(30)"), ("", "step_status", f"{VC}(10)"), ("", "processed_at", "TIMESTAMP"),
              ("", "step_comment", f"{VC}(255)")]
files_cols = [("PK FK", "application_id", "BIGINT"), ("PK", "file_order", "INT"), ("", "original_name", f"{VC}(255)"),
              ("", "stored_name", f"{VC}(255)")]
for p, k in (("leave_applications", "휴학"), ("scholarship_applications", "장학")):
    T[p + "_steps"] = (f"결재단계", steps_cols, None)
    T[p + "_files"] = (f"첨부파일", files_cols, None)

TW, HH, RH = 320, 34, 24
e = Svg(1540, 1300)


def table(name, x, y, child=False):
    kor, cols, note = T[name]
    h = HH + RH * (len(cols) + (1 if note else 0))
    head = "#64748B" if child else "#2F4B7C"
    e.rect(x, y, TW, h, "#FFFFFF", head, rx=6, sw=1.6)
    e.add(f'<path d="M{x},{y + HH} V{y + 6} a6,6 0 0 1 6,-6 H{x + TW - 6} a6,6 0 0 1 6,6 V{y + HH} Z" fill="{head}"/>')
    e.text(x + 12, y + HH / 2, name, size=13.5, weight=700, fill="#FFFFFF", anchor="start")
    e.text(x + TW - 12, y + HH / 2, kor, size=12.5, fill="#E2E8F0", anchor="end")
    for i, (key, col, typ) in enumerate(cols):
        ry = y + HH + i * RH
        if i % 2 == 1:
            e.add(f'<rect x="{x + 1}" y="{ry}" width="{TW - 2}" height="{RH}" fill="#F5F7FA"/>')
        kc = "#B7791F" if "PK" in key else ("#2B6CB0" if key else MUTED)
        e.text(x + 10, ry + RH / 2, key, size=11, weight=700, fill=kc, anchor="start")
        e.text(x + 58, ry + RH / 2, col, size=13, weight=600 if "PK" in key else 400, anchor="start")
        e.text(x + TW - 10, ry + RH / 2, typ, size=12, fill=MUTED, anchor="end")
    if note:
        ry = y + HH + len(cols) * RH
        e.add(f'<line x1="{x}" y1="{ry}" x2="{x + TW}" y2="{ry}" stroke="#CBD2D9"/>')
        e.text(x + 10, ry + RH / 2, note, size=11.5, fill="#2B6CB0", anchor="start", italic=True)
    return h


def rel(pts):
    """pts[0] = '1' 쪽, pts[-1] = 'N' 쪽 (crow's foot)."""
    d = " ".join(f"{x},{y}" for x, y in pts)
    e.add(f'<polyline points="{d}" fill="none" stroke="{LINE}" stroke-width="1.6"/>')

    def unit(a, b):
        dx, dy = b[0] - a[0], b[1] - a[1]
        n = (dx * dx + dy * dy) ** 0.5
        return dx / n, dy / n

    # 1 쪽: 수직선 두 개 (필수 1)
    ux, uy = unit(pts[0], pts[1])
    for off in (8, 14):
        cx, cy = pts[0][0] + ux * off, pts[0][1] + uy * off
        e.add(f'<line x1="{cx - uy * 8}" y1="{cy + ux * 8}" x2="{cx + uy * 8}" y2="{cy - ux * 8}" stroke="{LINE}" stroke-width="1.6"/>')
    # N 쪽: 까마귀발 + 수직선 (필수 다수)
    ex, ey = pts[-1]
    ux, uy = unit(pts[-2], pts[-1])
    bx, by = ex - ux * 14, ey - uy * 14
    for s_ in (-1, 1):
        e.add(f'<line x1="{bx}" y1="{by}" x2="{ex - uy * 9 * s_}" y2="{ey + ux * 9 * s_}" stroke="{LINE}" stroke-width="1.6"/>')
    cx, cy = ex - ux * 20, ey - uy * 20
    e.add(f'<line x1="{cx - uy * 8}" y1="{cy + ux * 8}" x2="{cx + uy * 8}" y2="{cy - ux * 8}" stroke="{LINE}" stroke-width="1.6"/>')


c1, c2, c3, c4 = 40, 420, 800, 1180
table("students", c2, 40)
table("enrollments", c3, 40)
table("courses", c4, 40)
table("leave_applications", c1, 440)
table("scholarship_applications", c3, 440)
table("leave_applications_steps", c1, 700, child=True)
table("leave_applications_files", c2, 700, child=True)
table("scholarship_applications_steps", c3, 700, child=True)
table("scholarship_applications_files", c4, 700, child=True)

rel([(740, 110), (800, 110)])
rel([(1180, 134), (1120, 134)])
rel([(420, 300), (200, 300), (200, 440)])
rel([(740, 300), (960, 300), (960, 440)])
rel([(200, 618), (200, 700)])
rel([(360, 580), (580, 580), (580, 700)])
rel([(960, 618), (960, 700)])
rel([(1120, 580), (1340, 580), (1340, 700)])
e.text(770, 92, "1 : N", size=12, fill=MUTED, halo=True)
e.text(1150, 116, "1 : N", size=12, fill=MUTED, halo=True)
e.text(310, 284, "1 : N", size=12, fill=MUTED, halo=True)
e.text(850, 284, "1 : N", size=12, fill=MUTED, halo=True)

e.add(f'<line x1="40" y1="945" x2="1500" y2="945" stroke="#CBD2D9" stroke-dasharray="6 5"/>')
e.text(40, 968, "독립 테이블 (다른 테이블과 연관관계 없음)", size=13, weight=600, fill=MUTED, anchor="start")
table("notices", c1, 990)
table("academic_schedules", c2, 990)
table("job_postings", c3, 990)

# 범례
lx, ly = c4, 990
e.rect(lx, ly, TW, 200, "#FAFBFC", "#CBD2D9", rx=8, sw=1.2)
e.text(lx + 16, ly + 24, "표기법", size=13.5, weight=700, anchor="start")
rel([(lx + 20, ly + 58), (lx + 110, ly + 58)])
e.text(lx + 124, ly + 58, "1 : N  (왼쪽 1, 오른쪽 N)", size=12.5, anchor="start", fill=MUTED)
for i, (k, d_) in enumerate([("PK", "기본키 (Primary Key)"), ("FK", "외래키 (Foreign Key)"), ("UK", "유일키 (Unique)")]):
    yy = ly + 92 + i * 24
    e.text(lx + 20, yy, k, size=11, weight=700, fill="#B7791F" if k == "PK" else "#2B6CB0", anchor="start")
    e.text(lx + 56, yy, d_, size=12.5, fill=MUTED, anchor="start")
e.rect(lx + 20, ly + 160, 22, 14, "#64748B", "#64748B", rx=3, sw=1)
e.text(lx + 52, ly + 167, "@ElementCollection 테이블", size=12.5, fill=MUTED, anchor="start")
e.save("2_ERD")

# ---------------------------------------------------------------- 3. 로그인·회원가입 흐름
f = Svg(1220, 1080)
f.legend(960, 36, [("term", "시작 / 끝"), ("proc", "처리 · 화면"), ("dec", "판단"), ("db", "DB 작업 (JPA)")])
f.term(200, 80, 240, 60, "사용자 접속\nhttp://localhost:8080")
f.arrow([(200, 110), (200, 158)])
f.diamond(200, 220, 240, 120, "로그인 되어 있는가?\n(LoginInterceptor)", size=13)
f.arrow([(320, 220), (438, 220)], "아니오", (380, 204))
f.box(560, 220, 240, 60, "로그인 화면 (/login)", "proc")
f.arrow([(560, 250), (560, 298)])
f.diamond(560, 360, 240, 120, "계정이 있는가?")
f.arrow([(680, 360), (788, 360)], "아니오", (734, 344))
f.box(920, 360, 260, 70, "회원가입 화면 (/signup)\n학번·이름·비밀번호 입력", "proc")
f.arrow([(920, 395), (920, 448)])
f.diamond(920, 520, 260, 140, "입력값 검증\n아이디 4~20자 · 중복 여부\n비밀번호 확인 일치", size=13)
f.arrow([(1050, 520), (1110, 520), (1110, 360), (1052, 360)], "실패", (1080, 504))
f.arrow([(920, 590), (920, 643)], "통과", (932, 617), anchor="start")
f.box(920, 680, 260, 70, "students INSERT\n비밀번호 BCrypt 암호화 저장", "db")
f.arrow([(1050, 680), (1160, 680), (1160, 220), (682, 220)], "가입 완료 → 로그인 화면", (920, 204))
f.arrow([(560, 420), (560, 468)], "예", (572, 444), anchor="start")
f.box(560, 500, 240, 60, "학번 · 비밀번호 입력", "proc")
f.arrow([(560, 530), (560, 583)])
f.diamond(560, 650, 260, 130, "비밀번호 일치?\n(BCrypt matches)")
f.arrow([(430, 650), (390, 650), (390, 280), (500, 280), (500, 252)], "아니오\n(오류 메시지)", (380, 470), anchor="end")
f.arrow([(560, 715), (560, 768)], "예", (572, 742), anchor="start")
f.box(560, 810, 280, 80, "세션에 학생 정보 저장\n마지막 로그인 시각·IP UPDATE", "db")
f.arrow([(560, 850), (560, 940), (342, 940)])
f.arrow([(200, 280), (200, 903)], "예", (212, 304), anchor="start")
f.box(200, 940, 280, 70, "통합포털 대시보드 ( / )\n시간표·학사일정·공지·채용·날씨", "proc")
f.arrow([(200, 975), (200, 1013)])
f.term(200, 1040, 260, 44, "주요 기능 실행 흐름으로", size=13)
f.save("3_실행흐름도_로그인회원가입")

# ---------------------------------------------------------------- 4. 주요 기능 흐름
g = Svg(1540, 840)
g.legend(30, 24, [("term", "시작 / 끝"), ("proc", "처리 · 화면"), ("dec", "판단"), ("db", "DB 작업 (JPA)")])
g.term(770, 50, 280, 50, "통합포털 대시보드")
g.arrow([(770, 75), (770, 108)])
g.box(770, 135, 240, 50, "사이드바 메뉴 선택", "proc")
cols = [170, 470, 770, 1070, 1370]
g.add(f'<polyline points="770,160 770,200" fill="none" stroke="{LINE}" stroke-width="1.6"/>')
g.add(f'<polyline points="170,200 1370,200" fill="none" stroke="{LINE}" stroke-width="1.6"/>')
heads = ["수강신청", "개인시간표 · 신청내역", "휴학 · 장학금 신청", "개인신상정보 수정", "공지 · 채용 · 학사일정 조회"]
for cx, h_ in zip(cols, heads):
    g.arrow([(cx, 200), (cx, 233)])
    g.rect(cx - 130, 235, 260, 44, "#2F4B7C", "#2F4B7C", rx=8)
    g.text(cx, 257, h_, size=14, weight=700, fill="#FFFFFF")
    g.arrow([(cx, 279), (cx, 313)])

ROWS = [347, 441, 535, 629]


def column(cx, specs):
    for i, (label, kind) in enumerate(specs):
        g.box(cx, ROWS[i], 260, 64, label, kind)
        if i:
            g.arrow([(cx, ROWS[i - 1] + 32), (cx, ROWS[i] - 34)])
    last = ROWS[len(specs) - 1] + 32
    g.add(f'<polyline points="{cx},{last} {cx},700" fill="none" stroke="{LINE}" stroke-width="1.6"/>')


# 수강신청 (판단 포함)
g.box(170, 347, 260, 64, "강좌 목록 조회\n(courses SELECT)", "db")
g.arrow([(170, 379), (170, 408)])
g.diamond(170, 470, 250, 120, "이미 신청했거나\n정원이 찼는가?", size=13)
g.arrow([(295, 470), (322, 470), (322, 347), (302, 347)], "예", (322, 412), size=12)
g.text(170, 548, "", size=1)
g.arrow([(170, 530), (170, 563)], "아니오", (182, 547), anchor="start", size=12)
g.box(170, 612, 260, 92, "enrollments INSERT\ncourses 신청인원 +1\n(한 트랜잭션 · 행 잠금)", "db")
g.add(f'<polyline points="170,658 170,700" fill="none" stroke="{LINE}" stroke-width="1.6"/>')

column(470, [("신청 내역 조회\n(enrollments + courses)", "db"), ("요일 × 교시 시간표 생성", "proc"),
             ("개인시간표 · 신청내역\n화면 표시", "proc")])
column(770, [("신청 종류 · 사유 · 첨부파일 입력", "proc"), ("첨부파일 uploads/ 폴더에 저장", "proc"),
             ("신청서 + 결재단계 3건\nINSERT", "db"), ("결재 진행 상태 표시\n담당직원 → 학과장 → 학생처장", "proc")])
column(1070, [("연락처 · 주소 · 계좌번호 입력", "proc"), ("students UPDATE", "db"),
              ("세션 정보 갱신\n완료 메시지 표시", "proc")])
column(1370, [("목록 조회 (SELECT)\n최신순 정렬 · 학사일정은 월별", "db"), ("항목 클릭 → 상세 조회\n(id로 SELECT)", "db")])

g.add(f'<polyline points="170,700 1370,700" fill="none" stroke="{LINE}" stroke-width="1.6"/>')
g.arrow([(770, 700), (770, 738)])
g.term(770, 770, 440, 60, "다른 메뉴 선택 (반복) 또는 로그아웃\n로그아웃 시 세션 삭제 → 로그인 화면", size=13)
g.save("4_실행흐름도_주요기능")
