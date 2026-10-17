param(
    [string]$BaseUrl = 'http://127.0.0.1:8080',
    [string]$Password = '123456'
)

$ErrorActionPreference = 'Stop'
$script:passed = 0
$script:failed = 0

function Assert-Equal {
    param([string]$Name, $Actual, $Expected)
    if ($Actual -eq $Expected) {
        Write-Host "[PASS] $Name" -ForegroundColor Green
        $script:passed++
    }
    else {
        Write-Host "[FAIL] $Name (expected=$Expected actual=$Actual)" -ForegroundColor Red
        $script:failed++
    }
}

function Invoke-Login {
    param([string]$Account)
    $body = @{ account = $Account; password = $Password } | ConvertTo-Json
    Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post -ContentType 'application/json' -Body $body
}

function Get-HttpStatus {
    param([scriptblock]$Action)
    try {
        & $Action | Out-Null
        return 200
    }
    catch {
        return [int]$_.Exception.Response.StatusCode
    }
}

Write-Host "== API smoke test ==" -ForegroundColor Cyan
Write-Host "target: $BaseUrl" -ForegroundColor DarkGray

$admin = Invoke-Login 'admin'
$staff = Invoke-Login 'staff'
$customer = Invoke-Login 'customer'

Assert-Equal 'admin login returns ADMIN' $admin.data.userInfo.roleCode 'ADMIN'
Assert-Equal 'staff login returns STAFF' $staff.data.userInfo.roleCode 'STAFF'
Assert-Equal 'customer login returns CUSTOMER' $customer.data.userInfo.roleCode 'CUSTOMER'

$wrongPassword = Get-HttpStatus {
    $body = @{ account = 'admin'; password = 'wrong-password' } | ConvertTo-Json
    Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post -ContentType 'application/json' -Body $body
}
Assert-Equal 'wrong password rejected (400)' $wrongPassword 400

$noToken = Get-HttpStatus { Invoke-RestMethod -Uri "$BaseUrl/api/user/me" }
Assert-Equal 'anonymous request rejected (401)' $noToken 401

$customerOnAdminApi = Get-HttpStatus {
    Invoke-RestMethod -Uri "$BaseUrl/api/user/page?pageNum=1&pageSize=10" -Headers @{ Authorization = $customer.data.token }
}
Assert-Equal 'customer blocked from admin api (403)' $customerOnAdminApi 403

$customerProducts = Invoke-RestMethod -Uri "$BaseUrl/api/product/page?pageNum=1&pageSize=50" -Headers @{ Authorization = $customer.data.token }
$customerOffShelf = Invoke-RestMethod -Uri "$BaseUrl/api/product/page?pageNum=1&pageSize=50&status=0" -Headers @{ Authorization = $customer.data.token }
Assert-Equal 'customer cannot expose off-shelf products' $customerOffShelf.data.total $customerProducts.data.total

$staffProducts = Invoke-RestMethod -Uri "$BaseUrl/api/product/page?pageNum=1&pageSize=50" -Headers @{ Authorization = $staff.data.token }
Assert-Equal 'staff sees all products' ($staffProducts.data.total -ge $customerProducts.data.total) $true

$categoryTree = Invoke-RestMethod -Uri "$BaseUrl/api/category/tree" -Headers @{ Authorization = $customer.data.token }
Assert-Equal 'category tree readable' $categoryTree.code 200

$customerCreateCategory = Get-HttpStatus {
    $body = @{ name = 'Smoke Test Category' } | ConvertTo-Json
    Invoke-RestMethod -Uri "$BaseUrl/api/category" -Method Post -ContentType 'application/json' -Headers @{ Authorization = $customer.data.token } -Body $body
}
Assert-Equal 'customer cannot create category (403)' $customerCreateCategory 403

$addressList = Invoke-RestMethod -Uri "$BaseUrl/api/address/list" -Headers @{ Authorization = $customer.data.token }
Assert-Equal 'address list readable' $addressList.code 200

$productDetail = Invoke-RestMethod -Uri "$BaseUrl/api/product/1" -Headers @{ Authorization = $customer.data.token }
Assert-Equal 'product detail readable' $productDetail.code 200

Write-Host ''
Write-Host "Result: passed=$script:passed failed=$script:failed" -ForegroundColor Cyan
if ($script:failed -gt 0) {
    exit 1
}
