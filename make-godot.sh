#!/bin/bash

# Copy this script in godot's root source code

declare -a android_targets=(release debug) # "release", "debug" "release_debug"
declare -a android_archs=(x86 armv7 arm64v8) # "arm64v8", "armv7", "x86"

function build_editor() {
	scons platform=x11 target=release_debug -j4
}

function build_android() {
	for arch in "${android_archs[@]}"; do
		for target in "${android_targets[@]}"; do
			for run in {1..1}; do
				scons -j12 p=android target=$target android_arch=$arch \
use_static_cpp=yes \
disable_3d=yes \
tools=no \
use_lto=yes \
deprecated=no \
debug_symbols=no \
android_stl=yes \
xml=no \
minizip=no \
xaudio2=no \
module_bullet_enabled=no \
module_csg_enabled=no \
module_dds_enabled=no \
module_enet_enabled=no \
module_hdr_enabled=no \
module_mobile_vr_enabled=no \
module_pvr_enabled=no \
module_recast_enabled=no \
module_regex_enabled=no \
module_squish_enabled=no \
module_thekla_unwrap_enabled=no \
module_ogg_enabled=yes \
module_webm_enabled=no \
module_bmp_enabled=no \
module_svg_enabled=no \
module_tga_enabled=no \
module_webp_enabled=yes \
module_jpg_enabled=yes \
module_theora_enabled=no \
module_tinyexr_enabled=no \
module_vorbis_enabled=yes \
module_openssl_enabled=yes \
module_visual_script_enabled=no \
module_mono_enabled=no \
module_websocket_enabled=yes \
module_gdnative_enabled=no \
module_gridmap_enabled=no \
module_etc_enabled=no \
module_upnp_enabled=no \
module_cvtt_enabled=no \
module_freetype_enabled=yes
module_opensimplex_enabled=no \
module_stb_vorbis_enabled=yes \
module_xatlas_unwrap_enabled=no \
module_mbedtls_enabled=no \
module_opus_enabled=no

#optimize=size \
			done
		done
	done

	build_gradle "$@"
}

function build_all() {
	build_editor "$@"
	build_android "$@"
}

function build_gradle() {
	cd platform/android/java
	rm -r build/
	./gradlew build
}

function clean_all() {
	rm `find . -name "*.o"`
	rm `find . -name "*.a"`
	rm `find . -name "*.pyc"`
	cd platform/android/java
	rm build -rf
	rm libs -rf
}

function main() {
	if [ "$1" == "build" ]
	then
		shift
		if [ "$1" == "editor" ]
		then
			build_editor "$@"
		fi
		if [ "$1" == "android" ]
		then
			build_android "$@"
		fi
		if [ "$1" == "gradle" ]
		then
			build_gradle "$@"
		fi
		if [ "$1" == "all" ]
		then
			build_all "$@"
		fi
		if [ "$1" == "clean" ]
		then
			clean_all
		fi
	fi
}

main "$@"

