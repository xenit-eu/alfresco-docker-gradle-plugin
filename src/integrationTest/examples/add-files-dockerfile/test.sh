#!/bin/bash -e
stat /opt/build.gradle || exit 1
stat /opt/gradle/test-src/blah/x || exit 1
exit 0
