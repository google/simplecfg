#!/bin/bash

HASH=$(cd git; git rev-parse --short HEAD)

cat <<EOF > README.google
URL: https://bitbucket.org/extendj/extendj/get/${HASH}.tar.gz
Version: ${HASH}
License: Modified BSD
License File: LICENSE

Description:
Extensible Java compiler built using JastAdd (see http://jastadd.org).

Local Modifications:
None
EOF
