# Emits the server catalog from existing Android assets; does not modify any files.
$ErrorActionPreference = 'Stop'
$appRoot = Join-Path $PSScriptRoot '../../app/src/main'
$formats = (Get-Content -Raw -Encoding utf8 (Join-Path $appRoot 'assets/reading_formats.json') | ConvertFrom-Json).readingFormats
$deck = Get-Content -Raw -Encoding utf8 (Join-Path $appRoot 'assets/tarot_cards.json') | ConvertFrom-Json
$translations = Get-Content -Raw -Encoding utf8 (Join-Path $appRoot 'assets/tarot_card_translations_en.json') | ConvertFrom-Json
$readingTexts = Get-Content -Raw -Encoding utf8 (Join-Path $appRoot 'java/com/denizcan/astrosea/util/ReadingTexts.kt')
$strings = @{}
$arrays = @{}
Get-ChildItem (Join-Path $appRoot 'res/values') -Filter '*.xml' | ForEach-Object {
    [xml]$xml = Get-Content -Raw -Encoding utf8 $_.FullName
    foreach ($item in $xml.SelectNodes('/resources/string')) { $strings[$item.name] = $item.InnerText.Replace("\'", "'") }
    foreach ($item in $xml.SelectNodes('/resources/string-array')) {
        $arrays[$item.name] = @($item.SelectNodes('item') | ForEach-Object { $_.InnerText.Replace("\'", "'") })
    }
}
$names = @{}
$slots = @{}
foreach ($match in [regex]::Matches($readingTexts, '"([^"]+)" -> R\.string\.(reading_name_\w+)')) {
    $names[($match.Groups[1].Value -replace '[\s,–-]+', '_')] = $strings[$match.Groups[2].Value]
}
foreach ($match in [regex]::Matches($readingTexts, '"([^"]+)" -> R\.array\.(slots_\w+)')) {
    $slots[($match.Groups[1].Value -replace '[\s,–-]+', '_')] = $arrays[$match.Groups[2].Value]
}
$exportFormats = @($formats.PSObject.Properties | ForEach-Object {
    $f = $_.Value
    if (!$names[$_.Name] -or $slots[$_.Name].Count -ne $f.cardCount) { throw "Missing names or slots: $($_.Name)" }
    [ordered]@{ id=$f.id; cardCount=$f.cardCount; promptTr=$f.basePrompt; positionsTr=@($f.positions.name); nameEn=$names[$_.Name]; positionsEn=$slots[$_.Name] }
})
$cards = @($deck.cards) + @($deck.minor_arcana.cups) + @($deck.minor_arcana.swords) + @($deck.minor_arcana.wands) + @($deck.minor_arcana.pentacles)
$exportCards = @($cards | ForEach-Object {
    $translation = $translations.($_.id)
    if (!$translation.upright -or !$translation.reversed) { throw "Missing card translation: $($_.id)" }
    [ordered]@{ id=$_.id; name=$_.name; nameTr=$_.turkishName; uprightTr=$_.meaningUpright; keywords=@($_.keywords); uprightEn=$translation.upright; reversedEn=$translation.reversed }
})
[ordered]@{ formats=$exportFormats; cards=$exportCards } | ConvertTo-Json -Depth 12
