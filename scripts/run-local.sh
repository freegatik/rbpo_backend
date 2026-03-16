#!/usr/bin/env sh
export SPRING_PROFILES_ACTIVE=local
exec ./gradlew bootRun
