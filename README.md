# cr-discord-bot [![Java Maven Build](https://github.com/theyellow/cr-discord-bot/actions/workflows/maven.yml/badge.svg)](https://github.com/theyellow/cr-discord-bot/actions/workflows/maven.yml) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=theyellow_cr-discord-bot&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=theyellow_cr-discord-bot) [![Docker Image CI](https://github.com/theyellow/cr-discord-bot/actions/workflows/docker-image.yml/badge.svg)](https://github.com/theyellow/cr-discord-bot/actions/workflows/docker-image.yml) [![Docker GHCR - Publish](https://github.com/theyellow/cr-discord-bot/actions/workflows/docker-publish.yml/badge.svg)](https://github.com/theyellow/cr-discord-bot/actions/workflows/docker-publish.yml)
Discord-Bot in Java for Clash Royal API 

This bot is a proof-of-concept distributed-bot and work in progress.
Technology is based on [discord4j](https://github.com/Discord4J/Discord4J) with its special project [connect](https://github.com/Discord4J/connect).
Springboot and micronaut is used for different parts of this project. Structure of this repository is like:

| directory          | content                                                                                                                    |
|--------------------|----------------------------------------------------------------------------------------------------------------------------|
| common             | classes used by leader and worker                                                                                          |
| connect            | fork of dicord4j-connect, awaiting [pull-request](https://github.com/Discord4J/connect/pull/10) for approval to be deleted |
| kubernetes         | scripts for kubernetes deployment                                                                                          |
| leader             | springboot leader application                                                                                              |
| logs               | for logs, will be created                                                                                                  |
| rsocket-middleware | micronaut middleware for leader and worker, uses connect                                                                   |
| worker             | springboot worker application                                                                                              |


For having some fun this bot plays with Clash Royal API and is inspired by [clash-royale-discord-bot](https://github.com/HZooly/clash-royale-discord-bot) written in java/typescript also here on GitHub. 

===========     -------- 
||Discord|| --> | dd   |
===========     --------




To get it working you need to use

a) GitHub-Actions or other pipelines (GitLab should work as well)

b) build and deploy everything yourself (more difficult)

A 'simple' development-workflow could be:

- push to GitHub
- automatically GitHub starts pipeline which will build docker images and publish it to github docker registry.
- use your own kubernetes-cluster to deploy images with scripts (examples in kubernetes-directory)

Credits to discord4j for the libraries and examples and clash-royal-discord-bot for ideas.
Also credits to developer-teams of official [Clash Royale API](https://developer.clashroyale.com/#/documentation) and [Discord](https://discord.com/developers/).
