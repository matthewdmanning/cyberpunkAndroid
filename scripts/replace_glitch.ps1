 = @("*.kt", "*.md", "*.xml", "*.gradle.kts", "*.kt", "*.java", "*.kts")
 = Get-ChildItem -Path C:\Users\mattm\AndroidStudioProjects\cyberpunkAndroid -Recurse -Include  -File

foreach ( in ) {
    # Exclude build directories and git directories just in case
    if (.FullName -match "\\build\\" -or .FullName -match "\\.git\\" -or .FullName -match "\\\.gradle\\") {
        continue
    }

     = [System.IO.File]::ReadAllText(.FullName)
    
    # Simple replace for the 3 main cases
     =  -creplace "Glitch", "Overload"
     =  -creplace "glitch", "overload"
     =  -creplace "GLITCH", "OVERLOAD"
    
    if ( -ne ) {
        [System.IO.File]::WriteAllText(.FullName, )
        Write-Host "Updated "
    }
}
