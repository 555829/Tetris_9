# Dev 기반 규칙 보완 및 테스트

## 작업 위치와 범위

이 작업은 `origin/Dev`에서 만든 `gyuri/game-rules-dev` 브랜치를 기준으로 한다. 이전 master 기반 작업은 `gyuri/game-rules`의 `8e681f1` 커밋에 그대로 남아 있다. 이전 파일을 통째로 덮어쓰거나 해당 커밋을 그대로 cherry-pick하지 않고 Dev의 생성자, AI 입력, 튕기기, 변형, 스냅샷을 유지하며 필요한 규칙을 옮겼다.

Dev에는 두 엔진이 있으므로 공통 규칙을 양쪽에 적용했다.

- 싱글/AI 대전: `src/game/tetris`
- 온라인 멀티: `src/game/ui/multigame/engine`

## 적용한 규칙

| 항목 | 규칙 |
| --- | --- |
| 고정 지연 | 착지한 상태로 30틱이 지나면 고정 |
| 지연 초기화 | 착지 상태에서 성공한 이동/회전으로 블록당 최대 15회 |
| 공중 이동/실패한 이동 | 초기화 횟수를 소비하지 않음 |
| 줄 삭제 공격 | 1줄=0, 2줄=1, 3줄=2, 4줄=4 |
| 받은 공격 적용 | 현재 블록 고정 → 줄 삭제/점수 계산 → 쓰레기 줄 적용 → 다음 블록 생성 |
| Hold/튕기기/잡기 | 공격을 대기열에 보존; 실제 고정될 때 적용 |
| 쓰레기 줄 구멍 | 공격 한 묶음은 같은 위치, 기본 API는 위치를 무작위 선택 |
| 게임 오버 | 쓰레기 줄 추가 불가/위쪽 넘침 또는 다음 블록 생성 실패 |
| 보드 크기 | 가로·세로 각각 3~20, 기본값 10×20 |

틱은 논리 업데이트 횟수다. 실제 초 단위 시간은 게임 루프 속도에 따라 달라진다. 초기화 횟수와 공격량은 `TetrisFlow`의 `MAX_LOCK_RESETS`, `LINE_ATTACKS`에서 조정할 수 있다.

기존 `Board.addGarbageLines`는 성공하면 true, 실패하면 false를 반환하며 실패 시 보드를 변경하지 않는다. 쓰레기 셀은 Dev의 `ZShape` 표현을 유지한다. 0줄 추가는 성공하는 무동작으로 보완했다.

## 담당자 연결 지점

두 엔진의 `TetrisFlow` 모두 다음 메서드를 제공한다. 게임 상태 변경은 게임 루프 스레드에서 수행한다.

| 메서드 | 용도 |
| --- | --- |
| `consumeOutgoingAttack()` | 누적 공격량을 반환한 뒤 0으로 초기화 |
| `receiveAttack(int count)` | 공격 대기열에 추가, 무작위 구멍 위치 |
| `receiveAttack(int count, int holeColumn)` | 구멍 위치 지정; 동기화/테스트용 |
| `getPendingGarbageLines()` | 대기 줄 수 조회 |
| `applyGarbageLines(int count, int holeColumn)` | 기존 이름 유지, 이제 즉시 보드 변경 대신 대기열에 추가 |

`applyGarbageLines`의 적용 시점이 달라졌다는 점을 온라인 담당자에게 공유해야 한다. 이번 Dev 코드에서는 이 메서드를 직접 호출하는 화면/네트워크 코드는 없었다. 온라인 서버의 공격 전송 프로토콜은 추가하지 않았다.

GUI 파일은 팀 담당 범위를 지키기 위해 원래 Dev 상태로 유지한다. 로컬 대전 연결 담당자는 두 게임의 tick 처리 후 `BattleRules.exchangeAttacks(first.flow, second.flow)`를 호출하면 된다. 한쪽이 일시정지/메뉴/게임 오버 상태이면 공격을 전달하지 않는다. 현재 화면에는 이 호출을 연결하지 않았으므로 AI 대전의 자동 공격 전달은 담당자 연동 후 적용된다. `GameMode.TWO_PLAYER`는 Dev에서 비활성화되어 있고 Keyboard2는 원격 입력용이다.

보드 크기 설정은 기존 생성자를 유지하면서 아래 오버로드를 양쪽 엔진에 추가했다.

```java
new Tetris(inputSource, queue, startLevel, settings, width, height);
```

엔진은 가로·세로 각각 3~20칸을 받으며 범위를 벗어나면 거부한다. 공통 제한값은 `game.rules.BoardDimensions`에 있다. 시작 레벨은 기존 Dev 동작을 유지하며 1 이상만 받는다. 크기 입력 UI, 화면 전달, 재시작 시 크기 유지, 작은 보드의 패널 배치는 GUI 담당자가 연결할 부분이며 이번 변경에는 포함하지 않는다. 현재 게임 화면은 기존 10×20으로 실행된다.

3열에서는 I 블록을 세로로 생성한다. 3×3에는 4칸짜리 I 블록이 들어갈 수 없으므로 크기 대응 생성기에서 I 블록만 제외한다. 이 경우에는 남은 6종을 한 번씩 섞는 방식이고 다른 크기는 기존 7-Bag이다. Hold 및 AI의 Hold 계획도 실제 게임과 동일한 크기별 생성 위치/방향을 사용한다. 외부에서 3×3 엔진에 직접 I 블록을 공급하면 생성 불가로 게임 오버가 된다.

GUI 담당자는 크기 지정 시 생성기도 `new PieceGenerator_7Bag(width, height)`로 만들어야 한다. 연결 예시:

```java
BoardDimensions.validate(width, height);
TetrominoQueue queue = new TetrominoQueue(new PieceGenerator_7Bag(width, height));
Tetris game = new Tetris(inputSource, queue, startLevel, settings, width, height);
```

온라인 방의 크기 설정과 서버 동기화도 담당자 연동 범위다. 온라인 게임은 기존 10×20을 유지한다.

Dev의 AI가 쓰는 `getHoldPiece()`의 비어 있을 때 Next를 반환하는 동작도 그대로 유지했다.

## 테스트 실행

JDK 14 이상이 설치된 PowerShell에서 Tetris_9 폴더를 열고 실행한다. Dev의 AI에 Java 14 switch 표현식이 있으므로 이전 Java 8 대상으로는 전체 소스를 컴파일할 수 없다.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\run-tests.ps1
```

외부 라이브러리 다운로드 없이 전체 `src/game`과 `tests`를 Java 14 대상으로 컴파일하고 테스트한다. 생성물은 Git에서 제외되는 `target/rule-tests`에 저장한다.

- `tests/game/RulesTest.java`: 기본 엔진 30개. 충돌/회전/줄 삭제/Hold/7-Bag/점수/레벨/고정 지연/공격, 튕기기·잡기·변형과 공격 상호작용, 대전 전달, 보드 크기 생성자.
- `tests/game/MultiRulesTest.java`: 멀티 엔진 공통 규칙 22개.
- `tests/game/BoardSizeTest.java`: 크기 설정 검증 6개. 양쪽 엔진의 3~20 전체 324가지 크기에서 블록 생성/고정, 범위 밖 거부, 3×3 생성기, 좁은/낮은 보드 AI 계획, 양쪽 엔진 생성자의 크기 적용을 검증. GUI 테스트는 포함하지 않는다.
- 총 58개. JDK 25에서 전체 컴파일 및 통과 확인.

테스트에서는 Random을 주입해 자동 튕기기·변형 추첨이 테스트 결과를 흔들지 않게 했다. 일반 게임 생성자의 무작위 동작은 유지한다. 특수 동작은 별도 테스트에서 직접 호출한다.

Maven 설정은 바꾸지 않았다. 위 스크립트로 검증했으며 Maven 빌드를 검증한 것은 아니다.

## 직접 플레이 확인

자동 테스트 실행 후 같은 폴더에서:

```powershell
java -cp "target/rule-tests;src" game.Main
```

`src`를 클래스 경로에 넣는 것은 Dev의 글꼴·이미지·설정 리소스를 읽기 위해서다. 또는 IDE에서 `src/game/Main.java`의 main을 실행한다.

확인할 항목:

1. 싱글: 이동, 회전, 소프트/하드 드롭, Hold와 Next.
2. 착지 후 좌우로 계속 움직여도 고정되는지.
3. 튕기기/잡기/변형 후 정상 낙하와 고정.
4. 일시정지/메뉴/게임 오버 뒤 상태가 정상인지.

보드 크기 선택 및 대전 공격 전달은 GUI 담당자가 연결한 뒤 실제 플레이로 확인한다. 연결 전에는 자동 테스트로 엔진 기능을 확인한다.

창 조작을 통한 실제 플레이, 서버 접속, 로그인, 온라인 대전은 이번 자동 검증에 포함하지 않았다. 온라인 공격 전달, 추가 아이템 규칙, 로컬 2P 입력, 공격 상쇄·콤보·T-spin·Back-to-Back 등은 별도 협의 범위다.

## GitHub에 공유

테스트와 직접 플레이 확인 후 이 브랜치에서 변경 파일을 확인하고 커밋한다. `target`은 포함하지 않는다.

```powershell
git status
git add src/game/rules src/game/tetris src/game/ui/multigame/engine tests run-tests.ps1 RULES.md
git commit -m "Dev 기반 게임 규칙 보완 및 회귀 테스트 추가"
git push -u origin gyuri/game-rules-dev
```

GitHub Pull Request의 대상(base)은 `master`가 아닌 `Dev`로 선택한다. 저장소 쓰기 권한이 없다면 협업자 초대를 먼저 수락해야 한다.
