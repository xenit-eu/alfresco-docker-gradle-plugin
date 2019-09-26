#!/bin/bash -ex
stat /opt/build.gradle || exit 1
stat /opt/gradle/test-src-1/blah/x || exit 1
stat /opt/gradle/test-src-1/zz || exit 1
stat /opt/gradle/test-src-2/blah/x || exit 1
stat /opt/gradle/test-src-2/zz || exit 1
stat /opt/gradle/test-src-3/blah/x || exit 1
stat /opt/gradle/test-src-3/zz || exit 1
exit 0
