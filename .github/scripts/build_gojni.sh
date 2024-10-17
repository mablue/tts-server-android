


#!/bin/bash

cd $GITHUB_WORKSPACE/lib-gojni-aar/tts-server-lib

go install golang.org/x/mobile/cmd/gomobile@latest
gomobile init
gomobile bind -ldflags "-s -w" -v -androidapi=21
# Consider removing this line if using a build system
mv -f *.aar ../
