# 一键启动开发环境：MySQL + Spring Boot 后端(8080) + Vue 前端(5173)
# 用法：右键“使用 PowerShell 运行”，或在项目根目录执行 .\start-dev.ps1
# 可选参数：-NoBrowser 启动后不自动打开浏览器

param([switch]$NoBrowser)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Join-Path $root 'src\backend'
$frontendDir = Join-Path $root 'src\frontend'

function Test-Port([int]$port) {
    return Test-NetConnection -ComputerName 127.0.0.1 -Port $port -InformationLevel Quiet -WarningAction SilentlyContinue
}

Write-Host '=== 电商订单管理系统 · 一键启动 ===' -ForegroundColor Cyan

# 1. MySQL（Windows 服务，通常开机自启）
$mysqlService = Get-Service -Name 'MySQL80' -ErrorAction SilentlyContinue
if (-not $mysqlService) {
    Write-Host '未找到 MySQL80 服务，请先安装 MySQL 8 或手动启动数据库。' -ForegroundColor Red
    exit 1
}
if ($mysqlService.Status -ne 'Running') {
    Write-Host '正在启动 MySQL80 ...' -ForegroundColor Yellow
    try {
        Start-Service -Name 'MySQL80'
    } catch {
        Write-Host 'MySQL80 启动失败，请以管理员身份运行本脚本。' -ForegroundColor Red
        exit 1
    }
}

if (-not (Test-Port 3306)) {
    Write-Host 'MySQL 端口 3306 未监听，已终止启动。' -ForegroundColor Red
    exit 1
}
Write-Host '[1/3] MySQL 已就绪 (3306)' -ForegroundColor Green

# 2. 后端
if (Test-Port 8080) {
    Write-Host '[2/3] 后端已在运行 (8080)，跳过启动' -ForegroundColor Green
} else {
    if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
        Write-Host '未找到 mvn，请先安装 Maven 并配置 PATH。' -ForegroundColor Red
        exit 1
    }
    Write-Host '[2/3] 正在新窗口启动后端 ...' -ForegroundColor Yellow
    # 用 cmd 启动，避免 PowerShell 在 Start-Process 嵌套命令时把 -D 参数拆开
    Start-Process -FilePath 'cmd.exe' -WorkingDirectory $backendDir `
        -ArgumentList '/k', 'title Ecommerce Backend 8080 && mvn spring-boot:run -Dspring-boot.run.profiles=local'
}

# 3. 前端
if (Test-Port 5173) {
    Write-Host '[3/3] 前端已在运行 (5173)，跳过启动' -ForegroundColor Green
} else {
    if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
        Write-Host '未找到 npm，请先安装 Node.js 并配置 PATH。' -ForegroundColor Red
        exit 1
    }
    Write-Host '[3/3] 正在新窗口启动前端 ...' -ForegroundColor Yellow
    Start-Process -FilePath 'cmd.exe' -WorkingDirectory $frontendDir `
        -ArgumentList '/k', 'title Ecommerce Frontend 5173 && npm run dev'
}

# 等待端口就绪
Write-Host '正在等待服务就绪（首次启动后端可能需要 20~60 秒）...' -ForegroundColor Yellow
$backendReady = Test-Port 8080
$frontendReady = Test-Port 5173
for ($i = 0; $i -lt 90; $i++) {
    if ($backendReady -and $frontendReady) { break }
    Start-Sleep -Seconds 2
    if (-not $backendReady) { $backendReady = Test-Port 8080 }
    if (-not $frontendReady) { $frontendReady = Test-Port 5173 }
}

Write-Host ''
if ($backendReady -and $frontendReady) {
    Write-Host '启动成功：http://localhost:5173' -ForegroundColor Green
    Write-Host '演示账号：admin / seller000001 / seller000002 / user000001（密码均为 123456）'
    Write-Host '请保持新打开的后端、前端窗口开启；关闭窗口即停止服务。' -ForegroundColor DarkGray
    if (-not $NoBrowser) {
        Start-Process 'http://localhost:5173'
    }
} else {
    Write-Host '服务尚未完全就绪，请查看新窗口中的报错信息。' -ForegroundColor Red
    Write-Host "后端 8080：$backendReady；前端 5173：$frontendReady"
}

Write-Host ''
Read-Host '按回车键关闭本窗口（不会停止已启动的服务）'
