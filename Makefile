.PHONY: testjar releasejar
all: releasejar

RUST_SOURCE_FILES := $(shell find lib/src -type f)

# Release - Linux x86_64
lib/target/x86_64-unknown-linux-gnu/release/libskinfixer.so: ${RUST_SOURCE_FILES}
	cd lib; \
		docker run -v "${PWD}/lib/:/code/" docker-registry.k8s.array21.dev/rust-xenial-builder release

# Release - Linux aarch64
lib/target/aarch64-unknown-linux-gnu/release/libskinfixer.so: ${RUST_SOURCE_FILES}
	cd lib; \
		PKG_CONFIG_SYSROOT_DIR=/usr/lib/aarch64-linux-gnu/ cargo build --release --target aarch64-unknown-linux-gnu

# Release - Linux armhf
lib/target/arm-unknown-linux-gnueabihf/release/libskinfixer.so: ${RUST_SOURCE_FILES}
	cd lib; \
		PKG_CONFIG_SYSROOT_DIR=/usr/lib/arm-linux-gnueabihf/ cargo build --release --target arm-unknown-linux-gnueabihf

# Release - Windows x86_64
lib/target/x86_64-pc-windows-gnu/release/libskinfixer.dll: ${RUST_SOURCE_FILES}
	cd lib; \
		cargo build --release --target x86_64-pc-windows-gnu
	cp lib/target/x86_64-pc-windows-gnu/release/skinfixer.dll lib/target/x86_64-pc-windows-gnu/release/libskinfixer.dll

# Release - Darwin x86_64
lib/target/x86_64-apple-darwin/release/libskinfixer.dylib: ${RUST_SOURCE_FILES}
	cd lib; \
		docker run -v "${PWD}/lib/:/code/" docker-registry.k8s.array21.dev/rust-macos-builder release

# Debug - Linux x86_64
lib/target/x86_64-unknown-linux-gnu/debug/libskinfixer.so: ${RUST_SOURCE_FILES}
	cd lib; \
		docker run -v "${PWD}/lib/:/code/" docker-registry.k8s.array21.dev/rust-xenial-builder

# Debug - Linux aarch64
lib/target/aarch64-unknown-linux-gnu/debug/libskinfixer.so: ${RUST_SOURCE_FILES}
	cd lib; \
		PKG_CONFIG_SYSROOT_DIR=/usr/lib/aarch64-linux-gnu/ cargo build --target aarch64-unknown-linux-gnu

# Debug - Linux armhf
lib/target/arm-unknown-linux-gnueabihf/debug/libskinfixer.so: ${RUST_SOURCE_FILES}
	cd lib; \
		PKG_CONFIG_SYSROOT_DIR=/usr/lib/arm-linux-gnueabihf/ cargo build --target arm-unknown-linux-gnueabihf

# Debug - Windows x86_64
lib/target/x86_64-pc-windows-gnu/debug/libskinfixer.dll: ${RUST_SOURCE_FILES}
	cd lib; \
		cargo build --target x86_64-pc-windows-gnu
	cp lib/target/x86_64-pc-windows-gnu/debug/skinfixer.dll lib/target/x86_64-pc-windows-gnu/debug/libskinfixer.dll

# Debug - Darwin x86_64
lib/target/x86_64-apple-darwin/debug/libskinfixer.dylib: ${RUST_SOURCE_FILES}
	cd lib; \
		docker run -v "${PWD}/lib/:/code/" docker-registry.k8s.array21.dev/rust-macos-builder

testjar: lib/target/x86_64-unknown-linux-gnu/debug/libskinfixer.so lib/target/aarch64-unknown-linux-gnu/debug/libskinfixer.so lib/target/arm-unknown-linux-gnueabihf/debug/libskinfixer.so lib/target/x86_64-pc-windows-gnu/debug/libskinfixer.dll lib/target/x86_64-apple-darwin/debug/libskinfixer.dylib
	chmod +x gradlew;
	rm -rf ./build/resources
	./gradlew testjar

releasejar: lib/target/x86_64-unknown-linux-gnu/release/libskinfixer.so lib/target/aarch64-unknown-linux-gnu/release/libskinfixer.so lib/target/arm-unknown-linux-gnueabihf/release/libskinfixer.so lib/target/x86_64-pc-windows-gnu/release/libskinfixer.dll lib/target/x86_64-apple-darwin/release/libskinfixer.dylib
	chmod +x gradlew
	rm -rf ./build/resources
	./gradlew releasejar
