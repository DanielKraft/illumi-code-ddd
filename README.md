<h1 align="center">
  <br>
  <img src="/gfx/illumi-code-ddd-logo.png" 
       alt="illumi-code-ddd"
       width="400">
  <br>
    illumi-code-ddd
  <br>
</h1>

<h4 align="center">
  Analysis of a Java system using jQAssistant and generating a change proposal based on Domian-Driven Design
</h4>

<p align="center">
  <a href="https://github.com/DanielKraft/illumi-code-ddd/commits">
    <img src="https://badgen.net/github/commits/DanielKraft/illumi-code-ddd"
         alt="Commits">
  </a>
  <a href="https://github.com/DanielKraft/illumi-code-ddd/commits">
    <img src="https://badgen.net/github/last-commit/DanielKraft/illumi-code-ddd"
         alt="Last Commit">
  </a>
  <a href="https://github.com/DanielKraft/illumi-code-ddd/releases">
    <img src="https://badgen.net/github/release/DanielKraft/illumi-code-ddd"
         alt="Release">
  </a>
  <a href="https://github.com/DanielKraft/illumi-code-ddd/blob/master/LICENSE">
    <img src="https://badgen.net/github/license/DanielKraft/illumi-code-ddd"
         alt="License">
  </a>
</p>

<p align="center">
  <a href="https://github.com/DanielKraft/illumi-code-ddd/actions">
    <img src="https://github.com/DanielKraft/illumi-code-ddd/workflows/Java%20CI/badge.svg"
         alt="Actions Status">
  </a>
  <a href="https://travis-ci.org/DanielKraft/illumi-code-ddd">
    <img src="https://travis-ci.org/DanielKraft/illumi-code-ddd.svg?branch=master"
         alt="Trevis CI Status">
  </a>
  <a href="https://sonarcloud.io/dashboard?id=DanielKraft_illumi-code-ddd">
    <img src="https://sonarcloud.io/api/project_badges/measure?project=DanielKraft_illumi-code-ddd&metric=alert_status"
         alt="Sonarcloud Status">
  </a>
  <a href="https://sonarcloud.io/component_measures?id=DanielKraft_illumi-code-ddd&metric=coverage&view=list">
    <img src="https://sonarcloud.io/api/project_badges/measure?project=DanielKraft_illumi-code-ddd&metric=coverage"
         alt="Coverage">
  </a>
  <a href="https://sonarcloud.io/dashboard?id=DanielKraft_illumi-code-ddd">
    <img src="https://sonarcloud.io/api/project_badges/measure?project=DanielKraft_illumi-code-ddd&metric=ncloc"
         alt="Lines of Code">
  </a>
</p>

## Workflow
![Workflow of illumi-code-ddd](/gfx/illumi-code-ddd-workflow-English.png "Workflow")

## Setup
### Needed software
- docker

### jQAssistant
> Scan the artifacts from the directory ${PROJECT_PATH}/${ARTIFACT_DIRECTORY}
```shell
docker run -it -m 5GB -v ${PROJECT_PATH}:/project jensnerche/jqassistant scan -f ${ARTIFACT_DIRECTORY}
```

> Run Neo4j server at port 7474 and 7687
```shell
docker run -it -v ${PROJECT_PATH}:/project -p 7474:7474 -p 7687:7687 jensnerche/jqassistant server -embeddedListenAddress 0.0.0.0
```

### illumi-code-ddd
```shell
git clone https://github.com/DanielKraft/illumi-code-ddd.git
cd illumi-code-ddd
./gradlew run
```

## Getting started
### Reading & Analyzing
> Import the existing system and assign the Domain-Driven Design building blocks
```shell
http://localhost:8040/analyse/${PACKAGE_FQN}
```
### Assessing
> Calculate the metrics for the existing system 
```shell
http://localhost:8040/metric
```
### Improving
> Improve the existing system based on Domain-Driven Design
```shell
http://localhost:8040/refactor
```
### Evaluating
> Calculate the metrics of the improved system
```shell
http://localhost:8040/metric
```

## Author
[**Kraft, Daniel**](https://github.com/DanielKraft)

## Acknowledgments
[**adesso SE**](https://www.adesso.de/de/)
