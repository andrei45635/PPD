$javaFile = $args[0] # Nume fisier java
#Write-Host $param1
$countryID = $args[1] # Country ID
#Write-Host $param2
Write-Host $javaFile
Write-Host $countryID
Write-Host $pwd
$a = java -cp ../../ org.example.$($javaFile) $countryID