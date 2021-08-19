param($rootPath, $toolsPath, $package, $project)

# Bail out if scaffolding is disabled (probably because you're running an incompatible version of T4Scaffolding.dll)
if (-not (Get-Module T4Scaffolding)) { return }

# Enable tab expansion
if (!$global:scaffolderTabExpansion) { $global:scaffolderTabExpansion = @{ } }
$global:scaffolderTabExpansion["MvcScaffolding.RazorView"] = $global:scaffolderTabExpansion["MvcScaffolding.AspxView"] = {
	param($filter, $allTokens)
	$secondLastToken = $allTokens[-2]
	if ($secondLastToken -eq 'Template') {
		return @("Create", "Delete", "Details", "Edit", "Index")
	}
}

# Enable MVC 3 Tools Update "Add Controller" dialog integration
. (Join-Path $toolsPath "registerWithMvcTooling.ps1")

function CountSolutionFilesByExtension($extension) {
	$files = (Get-Project).DTE.Solution `
		| ?{ $_.FileName } `
		| %{ [System.IO.Path]::GetDirectoryName($_.FileName) } `
		| %{ [System.IO.Directory]::EnumerateFiles($_, "*." + $extension, [System.IO.SearchOption]::AllDirectories) }
	($files | Measure-Object).Count
}

function InferPreferredViewEngine() {
	# Assume you want Razor except if you already have some ASPX views and no Razor ones
	if ((CountSolutionFilesByExtension aspx) -eq 0) { return "razor" }
	if (((CountSolutionFilesByExtension cshtml) -gt 0) -or ((CountSolutionFilesByExtension vbhtml) -gt 0)) { return "razor" }
	return "aspx"
}

# Ensure you've got some default settings for each of the included scaffolders
Set-DefaultScaffolder -Name Controller -Scaffolder MvcScaffolding.Controller -SolutionWide -DoNotOverwriteExistingSetting
Set-DefaultScaffolder -Name Views -Scaffolder MvcScaffolding.Views -SolutionWide -DoNotOverwriteExistingSetting
Set-DefaultScaffolder -Name Action -Scaffolder MvcScaffolding.Action -SolutionWide -DoNotOverwriteExistingSetting
Set-DefaultScaffolder -Name UnitTest -Scaffolder MvcScaffolding.ActionUnitTest -SolutionWide -DoNotOverwriteExistingSetting

# Infer which view engine you're using based on the files in your project
$viewScaffolder = if ([string](InferPreferredViewEngine) -eq 'aspx') { "MvcScaffolding.AspxView" } else { "MvcScaffolding.RazorView" }
Set-DefaultScaffolder -Name View -Scaffolder $viewScaffolder -SolutionWide -DoNotOverwriteExistingSetting