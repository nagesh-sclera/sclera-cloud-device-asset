$dest = 'C:\Users\DhanushVasanth\Desktop\AssetManagement POD\Microservice\sclera-cloud-device-asset\src\main\java\io\sclera\models\User.java'
$src  = 'C:\Users\DhanushVasanth\Desktop\AssetManagement POD\Microservice\sclera-vdms-edge-server\src\main\java\io\sclera\models\User.java'
# Read source, strip Bucket-C relations, write BOM-free
$lines = [System.IO.File]::ReadAllLines($src)
$out = [System.Collections.Generic.List[string]]::new()
$skip = $false
$skipCount = 0

foreach ($line in $lines) {
    # Detect Bucket-C relation field blocks to skip
    if ($line -match '@ManyToOne' -and ($line -match 'Customer_Organisation' -or $false)) { $skip = $true; $skipCount = 3; continue }
    if ($line -match 'private Customer_Organisation customer_org') { $out.Add('    // removed: relation to Bucket-C entity Customer_Organisation (CP-2)'); continue }
    if ($line -match 'private Set<ProfileUser> profile_user') { $out.Add('    // removed: relation to Bucket-C entity ProfileUser (CP-2)'); continue }
    if ($line -match 'private UserSettings user_settings') { $out.Add('    // removed: relation to Bucket-C entity UserSettings (CP-2)'); continue }
    if ($line -match 'private Set<Ticket> ticket') { $out.Add('    // removed: relation to Bucket-C entity Ticket (AP-C3)'); continue }
    # Skip annotation lines before stripped fields
    if ($line -match '@OneToOne.*mappedBy.*user.*UserSettings' -or $line -match '@OneToMany.*mappedBy.*user.*ProfileUser' -or $line -match '@OneToMany.*mappedBy.*user.*ticket') { continue }
    # Skip getters/setters for stripped fields
    if ($line -match 'getUser_settings|setUser_settings|getProfile_user|setProfile_user|getCustomer_org|setCustomer_org|getTicket|setTicket') { $skip = $true }
    if ($skip) {
        if ($line -match '^\s*\}') { $skip = $false; continue }
        continue
    }
    $out.Add($line)
}

$content = [string]::Join("`n", $out.ToArray()) + "`n"
[System.IO.File]::WriteAllText($dest, $content, [System.Text.UTF8Encoding]::new($false))
$bytes = [System.IO.File]::ReadAllBytes($dest)
if ($bytes[0] -eq 0xEF) { Write-Error 'BOM!' } else { Write-Host ('User.java written, ' + $out.Count + ' lines, no BOM') }
