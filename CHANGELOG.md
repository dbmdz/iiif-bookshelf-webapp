# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

...

## [3.1.1](https://github.com/dbmdz/iiif-bookshelf-webapp/releases/tag/3.1.1) - 2019-02-04

### Added

- Add CHANGELOG.md

### Changed

- Add missing german translations

## [3.1.0](https://github.com/dbmdz/iiif-bookshelf-webapp/releases/tag/3.1.0) - 2019-01-30

### Added

- Add openjdk 8 and 11 to build matrix
- Add automatic snapshot deployments to sonatype nexus
- Add git commit plugin
- Add checkstyle plugin
- Add prometheus, javamelody and jolokia
- Add spotbugs plugin

### Changed

- Fix usage and development documentation
- Replace deprecated hashing with apache commons
- Fix badges
- Cleanup spring config classes
- Split spring security configuration into to separate classes
- Reactivate application startup test
- Bump mirador version to 2.7.0
- Bump iiif-apis version to 0.3.6
