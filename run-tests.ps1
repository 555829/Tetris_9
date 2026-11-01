$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $testOutput = Join-Path $PSScriptRoot 'target/rule-tests'
    New-Item -ItemType Directory -Force -Path $testOutput | Out-Null
    $javaSources = @(Get-ChildItem -LiteralPath 'src/game', 'tests' -Recurse -Filter '*.java' |
        ForEach-Object { $_.FullName })
    & javac -encoding UTF-8 --release 14 -d $testOutput $javaSources
    if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed' }
    & java '-Djava.awt.headless=true' -cp $testOutput game.RulesTest
    if ($LASTEXITCODE -ne 0) { throw 'Rule tests failed' }
    & java '-Djava.awt.headless=true' -cp $testOutput game.MultiRulesTest
    if ($LASTEXITCODE -ne 0) { throw 'Multiplayer rule tests failed' }
    & java '-Djava.awt.headless=true' -cp $testOutput game.BoardSizeTest
    if ($LASTEXITCODE -ne 0) { throw 'Board size tests failed' }
}
finally {
    Pop-Location
}
