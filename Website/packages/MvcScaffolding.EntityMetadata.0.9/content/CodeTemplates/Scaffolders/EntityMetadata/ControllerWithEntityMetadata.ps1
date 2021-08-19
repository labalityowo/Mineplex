[T4Scaffolding.Scaffolder(Description = "Generates metadata classes and associated partial classes to pair with EntityFramework-generated entities.")][CmdletBinding()]
param(     
    [parameter(Mandatory = $true, ValueFromPipelineByPropertyName = $true)][string]$Edmx,
    [parameter(Mandatory = $true, ValueFromPipelineByPropertyName = $true)][string]$Model,
    [parameter(Mandatory = $false, ValueFromPipelineByPropertyName = $true)][string]$Context,
    [string]$Project,
    [string]$CodeLanguage,
    [string[]]$TemplateFolders,
    [switch]$Force = $false
)

$modelPath = (Get-Project $Project).Properties.Item("FullPath").Value, 'Models\' -join ""
$outputPath = 'Models\Metadata\', $Model, 'Metadata' -join ""
$namespace = (Get-Project $Project).Properties.Item("DefaultNamespace").Value, '.Models' -join ""
$edmxFullPath =  $modelPath, $Edmx, '.edmx' -join ""

write-host 'Attempting to create metadata for', $Model, 'in', $Edmx , '...'

Add-ProjectItemViaTemplate $outputPath `
    -Template EntityMetadataHelperTemplate `
    -Model @{ Namespace = $namespace; InputFileName = $edmxFullPath ; EntityName=$Model} `
    -SuccessMessage "Created metadata at {0}" `
    -TemplateFolders $TemplateFolders `
    -Project $Project `
    -CodeLanguage $CodeLanguage `
    -Force:$Force

Scaffold Controller $Model -DbContextType $Context