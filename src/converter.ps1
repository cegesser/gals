# forfiles /S /M *.java /C "PowerShell.exe -File C:\Users\supero\Desktop\gals\gals\src\converter.ps1 @path"
$yourfile = $args[0]
echo $yourfile
$content = get-content -path $yourfile 
$content | out-file $yourfile -encoding utf8