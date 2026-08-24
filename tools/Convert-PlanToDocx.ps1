param(
    [Parameter(Mandatory = $true)]
    [string]$InputPath,
    [Parameter(Mandatory = $true)]
    [string]$OutputPath
)

$ErrorActionPreference = 'Stop'
$word = $null
$document = $null

try {
    $resolvedInput = (Resolve-Path -LiteralPath $InputPath).Path
    $resolvedOutput = [System.IO.Path]::GetFullPath($OutputPath)
    $outputDirectory = [System.IO.Path]::GetDirectoryName($resolvedOutput)
    if (-not [System.IO.Directory]::Exists($outputDirectory)) {
        [System.IO.Directory]::CreateDirectory($outputDirectory) | Out-Null
    }

    $word = New-Object -ComObject Word.Application
    $word.Visible = $false
    $word.DisplayAlerts = 0
    $document = $word.Documents.Add()

    $normal = $document.Styles.Item(-1)
    $normal.Font.Name = 'Aptos'
    $normal.Font.Size = 10.5
    $normal.ParagraphFormat.SpaceAfter = 6

    $section = $document.Sections.Item(1)
    $section.PageSetup.TopMargin = $word.CentimetersToPoints(2.2)
    $section.PageSetup.BottomMargin = $word.CentimetersToPoints(2.2)
    $section.PageSetup.LeftMargin = $word.CentimetersToPoints(2.4)
    $section.PageSetup.RightMargin = $word.CentimetersToPoints(2.4)

    $selection = $word.Selection
    $inCodeBlock = $false
    $isFirstHeading = $true

    foreach ($rawLine in [System.IO.File]::ReadAllLines($resolvedInput)) {
        $line = $rawLine.TrimEnd()

        if ($line -eq '```text') {
            $inCodeBlock = $true
            continue
        }
        if ($line -eq '```') {
            $inCodeBlock = $false
            $selection.TypeParagraph()
            continue
        }
        if ($inCodeBlock) {
            $selection.Style = $document.Styles.Item(-1)
            $selection.Font.Name = 'Consolas'
            $selection.Font.Size = 9
            $selection.TypeText($line)
            $selection.TypeParagraph()
            $selection.Font.Name = 'Aptos'
            $selection.Font.Size = 10.5
            continue
        }

        if ([string]::IsNullOrWhiteSpace($line)) {
            continue
        }

        if ($line -match '^# (.+)$') {
            if (-not $isFirstHeading) { $selection.InsertBreak(7) }
            $selection.Style = $document.Styles.Item(-63)
            $selection.TypeText($Matches[1])
            $selection.TypeParagraph()
            $isFirstHeading = $false
            continue
        }
        if ($line -match '^## (.+)$') {
            $text = $Matches[1]
            if ($text -match '^Fase \d+\.') {
                $selection.Style = $document.Styles.Item(-3)
            } else {
                $selection.Style = $document.Styles.Item(-2)
            }
            $selection.TypeText($text)
            $selection.TypeParagraph()
            continue
        }
        if ($line -match '^### (.+)$') {
            $selection.Style = $document.Styles.Item(-3)
            $selection.TypeText($Matches[1])
            $selection.TypeParagraph()
            continue
        }
        if ($line -match '^#### (.+)$') {
            $selection.Style = $document.Styles.Item(-4)
            $selection.TypeText($Matches[1])
            $selection.TypeParagraph()
            continue
        }
        if ($line -match '^- (.+)$') {
            $selection.Style = $document.Styles.Item(-49)
            $selection.TypeText(($Matches[1] -replace '`', ''))
            $selection.TypeParagraph()
            continue
        }
        if ($line -match '^\d+\. (.+)$') {
            $selection.Style = $document.Styles.Item(-50)
            $selection.TypeText(($Matches[1] -replace '`', ''))
            $selection.TypeParagraph()
            continue
        }

        $selection.Style = $document.Styles.Item(-1)
        $selection.TypeText(($line -replace '  $', '' -replace '`', ''))
        $selection.TypeParagraph()
    }

    $footer = $section.Footers.Item(1).Range
    $footer.ParagraphFormat.Alignment = 2
    $footer.Text = 'NightcallAudio — Plan de trabajo'
    $footer.Collapse(0)
    $footer.Fields.Add($footer, -1, 'PAGE', $true) | Out-Null

    $document.SaveAs2($resolvedOutput, 16)
}
finally {
    if ($null -ne $document) {
        $document.Close($false)
        [System.Runtime.InteropServices.Marshal]::ReleaseComObject($document) | Out-Null
    }
    if ($null -ne $word) {
        $word.Quit()
        [System.Runtime.InteropServices.Marshal]::ReleaseComObject($word) | Out-Null
    }
    [GC]::Collect()
    [GC]::WaitForPendingFinalizers()
}
