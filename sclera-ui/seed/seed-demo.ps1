<#
  Seeds demo assets through the REAL gateway API (devicesupsert) so the UI shows
  rich content. Idempotent-ish: each run inserts fresh rows with unique ids/macs.

  Backend quirks handled here (discovered from DeviceService.upsert...):
    * The upsert only INSERTS a new device when virtual_device_type is 0 (or null).
    * It dedups by mac_address, so every asset needs a UNIQUE mac to be inserted.
    * docker name must NOT be a SQL reserved word ("default" crashes the query) —
      the live gateway uses "right_wing".

  Run AFTER the backend stack is up and healthy:
      docker compose up -d            # from repo root
      ./sclera-ui/seed/seed-demo.ps1
#>
param(
  [string]$Gateway = "http://localhost:8080",
  [string]$Vdms    = "VDMS760",
  [string]$Docker  = "right_wing",
  [string]$User    = "admin"
)

$ErrorActionPreference = "Stop"
$url = "$Gateway/asset/api/v1/sclera-cloud-device-asset-service/docker/$Docker/devicesupsert?username=$([uri]::EscapeDataString($User))&vdmsid=$Vdms&assignee=all"

function New-Mac {
  ((1..6 | ForEach-Object { '{0:X2}' -f (Get-Random -Max 256) }) -join ':')
}

# name, type, vendor, category, group, status, monitor
$defs = @(
  @('Conference Room Display','IP Device','Samsung','Hardware','Network',1,1),
  @('HVAC Controller B2','Controller','Honeywell','Appliance','HVAC',1,1),
  @('Floor 3 Access Point','Gateway','Cisco','Hardware','Network',0,1),
  @('Temperature Sensor L1','Sensor','Monnit','Hardware','Security',1,0),
  @('Backup Power Unit','Power Source','APC','Appliance','Network',1,1),
  @('Badge Reader West','Sensor','HID','Hardware','Security',1,1)
)

Write-Host "==> Seeding $($defs.Count) assets via $url" -ForegroundColor Cyan
$ok = 0
for ($i = 0; $i -lt $defs.Count; $i++) {
  $d = $defs[$i]
  $name = $d[0]
  $id = "{0}_{1}_{2}_{3}{4}" -f $Vdms, $Docker, ($name.ToLower() -replace '[^a-z0-9]+','_'), [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds(), $i
  $payload = @(@{
    id = $id; vdms_id = $Vdms; docker_vdms_id = $Vdms; docker_name = $Docker
    mac_address = (New-Mac); name = $name; display_name = $name; user_data_name = $name
    type = $d[1]; vendor = $d[2]; user_data_vendor = $d[2]; status = $d[5]; monitor = $d[6]
    category = $d[3]; sub_category = 'Primary'; asset_group = $d[4]; description = "$name (demo)"
    virtual_device_type = 0; asset_match_status = 0; created_email = $User
  })
  try {
    Invoke-RestMethod -Method Post -Uri $url -ContentType 'application/json' -Body ($payload | ConvertTo-Json -Depth 5) | Out-Null
    Write-Host "    + $name" -ForegroundColor Green; $ok++
  } catch {
    Write-Warning "    ! $name failed: $($_.Exception.Message)"
  }
}

Write-Host ""
if ($ok -gt 0) {
  Write-Host "Seeded $ok assets. Open http://localhost:3000 -> Property List -> open VDMS -> Managed Assets." -ForegroundColor Green
} else {
  Write-Warning "Nothing seeded. Is the backend up? Try: docker compose ps  (gateway on :8080)"
  exit 1
}
