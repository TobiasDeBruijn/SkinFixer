.PHONY: testjar releasejar
all: releasejar

RUST_SOURCE_FILES := $(shell find lib/src -type f)

# Release - Linux x86_64 Xenial
lib/target/x86_64-unknown-linux-gnu/release/libskinfixer-xenial.so: ${RUST_SOURCE_FILES}
	docker run -v "${CURDIR}/lib/:/code/" docker-registry.k8s.array21.dev/rust-amd64-xenial-builder release
	mv lib/target/x86_64-unknown-linux-gnu/release/libskinfixer.so lib/target/x86_64-unknown-linux-gnu/release/libskinfixer-xenial.so

# Release - Linux x86_64 Focal
lib/target/x86_64-unknown-linux-gnu/release/libskinfixer-focal.so: ${RUST_SOURCE_FILES}
	docker run -v "${CURDIR}/lib/:/code" docker-registry.k8s.array21.dev/rust-amd64-focal-builder release
	mv lib/target/x86_64-unknown-linux-gnu/release/libskinfixer.so lib/target/x86_64-unknown-linux-gnu/release/libskinfixer-focal.so

# Release - Linux x86_64 Bionic
lib/target/x86_64-unknown-linux-gnu/release/libskinfixer-bionic.so: ${RUST_SOURCE_FILES}
	docker run -v "${CURDIR}/lib/:/code" docker-registry.k8s.array21.dev/rust-amd64-bionic-builder release
	mv lib/target/x86_64-unknown-linux-gnu/release/libskinfixer.so lib/target/x86_64-unknown-linux-gnu/release/libskinfixer-bionic.so

# Release - Linux aarch64
lib/target/aarch64-unknown-linux-gnu/release/libskinfixer.so: ${RUST_SOURCE_FILES}
	docker run -v "${CURDIR}/lib/:/code/" docker-registry.k8s.array21.dev/rust-aarch64-focal-builder release

# Release - Linux armhf
lib/target/arm-unknown-linux-gnueabihf/release/libskinfixer.so: ${RUST_SOURCE_FILES}
	docker run -v "${CURDIR}/lib/:/code/" docker-registry.k8s.array21.dev/rust-armhf-focal-builder release

# Release - Windows x86_64
lib/target/x86_64-pc-windows-gnu/release/libskinfixer.dll: ${RUST_SOURCE_FILES}
	cd lib; \
		cargo build --release --target x86_64-pc-windows-gnu
	cp lib/target/x86_64-pc-windows-gnu/release/skinfixer.dll lib/target/x86_64-pc-windows-gnu/release/libskinfixer.dll

# Release - Darwin x86_64
lib/target/x86_64-apple-darwin/release/libskinfixer.dylib: ${RUST_SOURCE_FILES}
	docker run -v "${CURDIR}/lib/:/code/" docker-registry.k8s.array21.dev/rust-amd64-darwin-builder release

# Debug - Linux x86_64
lib/target/x86_64-unknown-linux-gnu/debug/libskinfixer.so: ${RUST_SOURCE_FILES}
	docker run -v "${CURDIR}/lib/:/code/" docker-registry.k8s.array21.dev/rust-amd64-focal-builder

# Debug - Linux aarch64
lib/target/aarch64-unknown-linux-gnu/debug/libskinfixer.so: ${RUST_SOURCE_FILES}
	docker run -v "${CURDIR}/lib/:/code/" docker-registry.k8s.array21.dev/rust-aarch64-focal-builder

# Debug - Linux armhf
lib/target/arm-unknown-linux-gnueabihf/debug/libskinfixer.so: ${RUST_SOURCE_FILES}
	docker run -v "${CURDIR}/lib/:/code/" docker-registry.k8s.array21.dev/rust-armhf-focal-builder

# Debug - Windows x86_64
lib/target/x86_64-pc-windows-gnu/debug/libskinfixer.dll: ${RUST_SOURCE_FILES}
	cd lib; \
		cargo build --target x86_64-pc-windows-gnu
	cp lib/target/x86_64-pc-windows-gnu/debug/skinfixer.dll lib/target/x86_64-pc-windows-gnu/debug/libskinfixer.dll

# Debug - Darwin x86_64
lib/target/x86_64-apple-darwin/debug/libskinfixer.dylib: ${RUST_SOURCE_FILES}
	docker run -v "${CURDIR}/lib/:/code/" docker-registry.k8s.array21.dev/rust-amd64-darwin-builder

testjar: lib/target/x86_64-unknown-linux-gnu/debug/libskinfixer.so lib/target/aarch64-unknown-linux-gnu/debug/libskinfixer.so lib/target/arm-unknown-linux-gnueabihf/debug/libskinfixer.so lib/target/x86_64-pc-windows-gnu/debug/libskinfixer.dll lib/target/x86_64-apple-darwin/debug/libskinfixer.dylib
	chmod +x gradlew;
	rm -rf ./build/resources
	./gradlew testjar

releasejar: lib/target/x86_64-unknown-linux-gnu/release/libskinfixer-focal.so lib/target/x86_64-unknown-linux-gnu/release/libskinfixer-bionic.so lib/target/x86_64-unknown-linux-gnu/release/libskinfixer-xenial.so lib/target/aarch64-unknown-linux-gnu/release/libskinfixer.so lib/target/arm-unknown-linux-gnueabihf/release/libskinfixer.so lib/target/x86_64-pc-windows-gnu/release/libskinfixer.dll lib/target/x86_64-apple-darwin/release/libskinfixer.dylib
	chmod +x gradlew
	rm -rf ./build/resources
	./gradlew releasejar
