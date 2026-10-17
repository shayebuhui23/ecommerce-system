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

function Assert-True {
    param([string]$Name, [bool]$Condition, [string]$Detail = '')
    if ($Condition) {
        Write-Host "[PASS] $Name" -ForegroundColor Green
        $script:passed++
    }
    else {
        Write-Host "[FAIL] $Name $Detail" -ForegroundColor Red
        $script:failed++
    }
}

function Login {
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

Write-Host "== API regression smoke test ==" -ForegroundColor Cyan
Write-Host "target: $BaseUrl" -ForegroundColor DarkGray

# ---------------- 1. 认证与权限 ----------------
$admin = Login 'admin'
$staff = Login 'staff'
$customer = Login 'customer'

$adminToken = $admin.data.token
$staffToken = $staff.data.token
$customerToken = $customer.data.token

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
    Invoke-RestMethod -Uri "$BaseUrl/api/user/page?pageNum=1&pageSize=10" -Headers @{ Authorization = $customerToken }
}
Assert-Equal 'customer blocked from admin api (403)' $customerOnAdminApi 403

$customerOnStatistics = Get-HttpStatus {
    Invoke-RestMethod -Uri "$BaseUrl/api/statistics/overview" -Headers @{ Authorization = $customerToken }
}
Assert-Equal 'customer blocked from statistics (403)' $customerOnStatistics 403

# ---------------- 2. 商品可见性 ----------------
$customerProducts = Invoke-RestMethod -Uri "$BaseUrl/api/product/page?pageNum=1&pageSize=50" -Headers @{ Authorization = $customerToken }
$customerOffShelf = Invoke-RestMethod -Uri "$BaseUrl/api/product/page?pageNum=1&pageSize=50&status=0" -Headers @{ Authorization = $customerToken }
Assert-Equal 'customer cannot expose off-shelf products' $customerOffShelf.data.total $customerProducts.data.total

$staffProducts = Invoke-RestMethod -Uri "$BaseUrl/api/product/page?pageNum=1&pageSize=50" -Headers @{ Authorization = $staffToken }
Assert-True 'staff sees all products' ($staffProducts.data.total -ge $customerProducts.data.total) "staff=$($staffProducts.data.total) customer=$($customerProducts.data.total)"

$categoryTree = Invoke-RestMethod -Uri "$BaseUrl/api/category/tree" -Headers @{ Authorization = $customerToken }
Assert-Equal 'category tree readable' $categoryTree.code 200

$customerCreateCategory = Get-HttpStatus {
    $body = @{ name = 'Smoke Test Category' } | ConvertTo-Json
    Invoke-RestMethod -Uri "$BaseUrl/api/category" -Method Post -ContentType 'application/json' -Headers @{ Authorization = $customerToken } -Body $body
}
Assert-Equal 'customer cannot create category (403)' $customerCreateCategory 403

# ---------------- 3. 收货地址 ----------------
$addressList = Invoke-RestMethod -Uri "$BaseUrl/api/address/list" -Headers @{ Authorization = $customerToken }
if (-not $addressList.data -or $addressList.data.Count -eq 0) {
    $body = @{
        consignee   = 'Smoke Tester'
        phone       = '+1 415 555 0100'
        country     = 'United States'
        provinceState = 'California'
        city        = 'San Francisco'
        postalCode  = '94107'
        addressLine = '1 Market Street'
        isDefault   = 1
    } | ConvertTo-Json
    Invoke-RestMethod -Uri "$BaseUrl/api/address" -Method Post -ContentType 'application/json' -Headers @{ Authorization = $customerToken } -Body $body | Out-Null
    $addressList = Invoke-RestMethod -Uri "$BaseUrl/api/address/list" -Headers @{ Authorization = $customerToken }
}
$addressId = $addressList.data[0].id
Assert-True 'customer has an address' ($null -ne $addressId)

# ---------------- 4. 购物车 ----------------
Invoke-RestMethod -Uri "$BaseUrl/api/cart/items" -Method Delete -Headers @{ Authorization = $customerToken } | Out-Null

$addBody = @{ productId = 4; quantity = 2 } | ConvertTo-Json
$addResult = Invoke-RestMethod -Uri "$BaseUrl/api/cart/items" -Method Post -ContentType 'application/json' -Headers @{ Authorization = $customerToken } -Body $addBody
Assert-Equal 'add to cart succeeds' $addResult.code 200

$cart = Invoke-RestMethod -Uri "$BaseUrl/api/cart" -Headers @{ Authorization = $customerToken }
Assert-Equal 'cart quantity merged' $cart.data.totalQuantity 2
Assert-True 'cart total recalculated by server' ($cart.data.totalAmount -gt 0) "total=$($cart.data.totalAmount)"

$badQuantity = Get-HttpStatus {
    $body = @{ quantity = 0 } | ConvertTo-Json
    Invoke-RestMethod -Uri "$BaseUrl/api/cart/items/$($cart.data.items[0].id)" -Method Put -ContentType 'application/json' -Headers @{ Authorization = $customerToken } -Body $body
}
Assert-Equal 'zero quantity rejected (400)' $badQuantity 400

# ---------------- 5. 下单 ----------------
$orderBody = @{ addressId = $addressId; currency = 'CNY'; shippingMethod = 'EXPRESS' } | ConvertTo-Json
$order = Invoke-RestMethod -Uri "$BaseUrl/api/order" -Method Post -ContentType 'application/json' -Headers @{ Authorization = $customerToken } -Body $orderBody
$orderId = $order.data.id
Assert-Equal 'order created as PENDING_PAYMENT' $order.data.status 'PENDING_PAYMENT'
Assert-True 'order amount calculated by server' ($order.data.payAmount -gt 0) "pay=$($order.data.payAmount)"

$cartAfterOrder = Invoke-RestMethod -Uri "$BaseUrl/api/cart" -Headers @{ Authorization = $customerToken }
Assert-Equal 'cart cleared after order' $cartAfterOrder.data.items.Count 0

# ---------------- 6. 支付 ----------------
$payFailBody = @{ orderId = $orderId; amount = $order.data.payAmount; success = $false } | ConvertTo-Json
$payFail = Invoke-RestMethod -Uri "$BaseUrl/api/payment/pay" -Method Post -ContentType 'application/json' -Headers @{ Authorization = $customerToken } -Body $payFailBody
Assert-Equal 'simulated payment failure keeps order unpaid' $payFail.data.status 0

$orderAfterFail = Invoke-RestMethod -Uri "$BaseUrl/api/order/$orderId" -Headers @{ Authorization = $customerToken }
Assert-Equal 'order still PENDING_PAYMENT after failure' $orderAfterFail.data.status 'PENDING_PAYMENT'

$payBody = @{ orderId = $orderId; amount = $order.data.payAmount; success = $true } | ConvertTo-Json
$pay = Invoke-RestMethod -Uri "$BaseUrl/api/payment/pay" -Method Post -ContentType 'application/json' -Headers @{ Authorization = $customerToken } -Body $payBody
Assert-Equal 'payment success' $pay.data.status 1

$orderAfterPay = Invoke-RestMethod -Uri "$BaseUrl/api/order/$orderId" -Headers @{ Authorization = $customerToken }
Assert-Equal 'order moves to PENDING_SHIPMENT' $orderAfterPay.data.status 'PENDING_SHIPMENT'

$payAgain = Get-HttpStatus {
    Invoke-RestMethod -Uri "$BaseUrl/api/payment/pay" -Method Post -ContentType 'application/json' -Headers @{ Authorization = $customerToken } -Body $payBody
}
Assert-Equal 'duplicate payment rejected (400)' $payAgain 400

$customerShip = Get-HttpStatus {
    $body = @{ shippingCompany = 'DHL'; trackingNo = 'X1' } | ConvertTo-Json
    Invoke-RestMethod -Uri "$BaseUrl/api/order/$orderId/ship" -Method Post -ContentType 'application/json' -Headers @{ Authorization = $customerToken } -Body $body
}
Assert-Equal 'customer cannot ship (403)' $customerShip 403

# ---------------- 7. 发货与收货 ----------------
$shipBody = @{
    shippingCompany   = 'DHL'
    trackingNo        = "SMOKE$([DateTime]::Now.ToString('HHmmss'))"
    shippingMethod    = 'EXPRESS'
    fromCountry       = 'China'
    toCountry         = 'United States'
    estimatedDelivery = (Get-Date).AddDays(7).ToString('yyyy-MM-dd')
} | ConvertTo-Json
$ship = Invoke-RestMethod -Uri "$BaseUrl/api/order/$orderId/ship" -Method Post -ContentType 'application/json' -Headers @{ Authorization = $staffToken } -Body $shipBody
Assert-Equal 'shipment created' $ship.code 200

$orderAfterShip = Invoke-RestMethod -Uri "$BaseUrl/api/order/$orderId" -Headers @{ Authorization = $customerToken }
Assert-Equal 'order moves to SHIPPED' $orderAfterShip.data.status 'SHIPPED'
Assert-True 'shipment attached to order detail' ($null -ne $orderAfterShip.data.shipment) 'shipment missing'

$receive = Invoke-RestMethod -Uri "$BaseUrl/api/order/$orderId/receive" -Method Post -Headers @{ Authorization = $customerToken }
Assert-Equal 'receive order succeeds' $receive.code 200

$orderAfterReceive = Invoke-RestMethod -Uri "$BaseUrl/api/order/$orderId" -Headers @{ Authorization = $customerToken }
Assert-Equal 'order moves to COMPLETED' $orderAfterReceive.data.status 'COMPLETED'
Assert-Equal 'shipment marked delivered' $orderAfterReceive.data.shipment.status 'DELIVERED'

$receiveAgain = Get-HttpStatus {
    Invoke-RestMethod -Uri "$BaseUrl/api/order/$orderId/receive" -Method Post -Headers @{ Authorization = $customerToken }
}
Assert-Equal 'duplicate receive rejected (400)' $receiveAgain 400

# ---------------- 8. 评价 ----------------
$firstItem = $orderAfterReceive.data.items[0]
$reviewBody = @{ orderItemId = $firstItem.id; rating = 5; content = 'Smoke test review' } | ConvertTo-Json
$review = Invoke-RestMethod -Uri "$BaseUrl/api/review" -Method Post -ContentType 'application/json' -Headers @{ Authorization = $customerToken } -Body $reviewBody
Assert-Equal 'review submitted' $review.code 200

$reviewAgain = Get-HttpStatus {
    Invoke-RestMethod -Uri "$BaseUrl/api/review" -Method Post -ContentType 'application/json' -Headers @{ Authorization = $customerToken } -Body $reviewBody
}
Assert-Equal 'duplicate review rejected (400)' $reviewAgain 400

$summary = Invoke-RestMethod -Uri "$BaseUrl/api/review/product/$($firstItem.productId)/summary" -Headers @{ Authorization = $customerToken }
Assert-True 'review summary counts the review' ($summary.data.reviewCount -ge 1) "count=$($summary.data.reviewCount)"

# ---------------- 9. 售后 ----------------
$afterSaleBody = @{ orderId = $orderId; reason = 'Smoke test refund'; description = 'regression script' } | ConvertTo-Json
$afterSale = Invoke-RestMethod -Uri "$BaseUrl/api/after-sale" -Method Post -ContentType 'application/json' -Headers @{ Authorization = $customerToken } -Body $afterSaleBody
Assert-Equal 'after-sale applied as PENDING' $afterSale.data.status 'PENDING'

$afterSaleAgain = Get-HttpStatus {
    Invoke-RestMethod -Uri "$BaseUrl/api/after-sale" -Method Post -ContentType 'application/json' -Headers @{ Authorization = $customerToken } -Body $afterSaleBody
}
Assert-Equal 'duplicate after-sale rejected (400)' $afterSaleAgain 400

$auditBody = @{ approved = $false; auditRemark = 'Smoke test reject' } | ConvertTo-Json
$audit = Invoke-RestMethod -Uri "$BaseUrl/api/after-sale/$($afterSale.data.id)/audit" -Method Put -ContentType 'application/json' -Headers @{ Authorization = $staffToken } -Body $auditBody
Assert-Equal 'after-sale rejected by staff' $audit.data.status 'REJECTED'

# ---------------- 10. 统计、汇率与审计日志 ----------------
$overview = Invoke-RestMethod -Uri "$BaseUrl/api/statistics/overview" -Headers @{ Authorization = $staffToken }
Assert-True 'statistics overview available' ($overview.data.orderTotal -gt 0) "orders=$($overview.data.orderTotal)"
Assert-True 'completed amount aggregated' ($null -ne $overview.data.completedAmount)

$rates = Invoke-RestMethod -Uri "$BaseUrl/api/exchange-rate/list" -Headers @{ Authorization = $staffToken }
Assert-True 'exchange rates available' ($rates.data.Count -ge 2) "count=$($rates.data.Count)"

$logs = Invoke-RestMethod -Uri "$BaseUrl/api/operation-log/page?pageNum=1&pageSize=20" -Headers @{ Authorization = $adminToken }
Assert-True 'operation logs recorded' ($logs.data.total -ge 1) "total=$($logs.data.total)"

$staffLogs = Get-HttpStatus {
    Invoke-RestMethod -Uri "$BaseUrl/api/operation-log/page?pageNum=1&pageSize=20" -Headers @{ Authorization = $staffToken }
}
Assert-Equal 'staff cannot read audit logs (403)' $staffLogs 403

# ---------------- 11. 清理测试购物车 ----------------
Invoke-RestMethod -Uri "$BaseUrl/api/cart/items" -Method Delete -Headers @{ Authorization = $customerToken } | Out-Null

Write-Host ''
Write-Host "Result: passed=$script:passed failed=$script:failed" -ForegroundColor Cyan
if ($script:failed -gt 0) {
    exit 1
}
