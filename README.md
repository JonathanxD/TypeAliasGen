# TypeAliasGen

Generate type alias from a class list or source/class file list.

**Important**: TypeAliasGen don't inspect the class code, it only generate type alias from file name.

## Usage

`java -jar TypeAliasGen-1.0-SNAPSHOT-all.jar [dir] <package> <basePackage> <prefix> <suffix> <output> <analyzeCp>`

- Dir
  - Directory (or base package if analyzeCp is defined) to scan for source or class files.

- Package
  - Package of the generated class (.kt).

- Base Package
  - Base package of the analyzed files.

- Prefix
  - Prefix of type alias name.

- Suffix
  - Suffix of type alias name.

- Output
  - Output file.
  
- AnalyzeCP
  - Analyze classpath instead of directory. Dir is the base package to analyze.

If you don't specify neither Suffix or Prefix, TypeAliasGen will generate a TypeAlias with same name of the file.

If you specify `~` (UTF-8) TypeAliasGen will ignore the option.