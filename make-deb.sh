#!/bin/bash

[ "$UID" -eq 0 ] || exec sudo bash "$0" "$@"

PACKAGE="godotx"
VERSION="3.1.1x"
TMP_DIR=$(mktemp -d)
DEB_DIR=$TMP_DIR/debian
EXE_DIR=$DEB_DIR/tmp/usr/bin

echo $TMP_DIR

mkdir -p $DEB_DIR
mkdir -p $EXE_DIR
mkdir -p $DEB_DIR/tmp/DEBIAN

cp bin/godot.x11.opt.tools.64 $EXE_DIR/$PACKAGE

strip $EXE_DIR/$PACKAGE

cat <<EOF > $DEB_DIR/control
Source: $PACKAGE

Package: $PACKAGE
Version: $VERSION
Depends: \${shlibs:Depends}
Architecture: all
Maintainer: cafebazi
Description: godot custom dev build 
EOF

cat <<EOF > $DEB_DIR/changelog
$PACKAGE ($VERSION) unstable; urgency=low
  * placeholder changelog to satisfy dpkg-gencontrol
 -- XYZ <xyz@xyz.com>  Thu, 3 Nov 2019 16:49:00 -0700

EOF

(cd $TMP_DIR; dpkg-shlibdeps -e $EXE_DIR/$PACKAGE; dpkg-gencontrol)

chown root:root -R $DEB_DIR/tmp
chmod 0755 $EXE_DIR/$PACKAGE
dpkg -b $DEB_DIR/tmp .

