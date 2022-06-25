.PHONY: testjar releasejar
all: releasejar

RUST_SOURCE_FILES := $(shell find lib/src -type f)

# Release - Linux x86_64 centos7
lib/target/x86_64-unknown-linux-gnu/release/libskinfixer.so: ${RUST_SOURCE_FILES}
	mkdir -p lib/target/x86_64-unknown-linux-gnu/release
	docker run -v "${CURDIR}/lib/:/code/" docker-registry.k8s.array21.dev/rust-amd64-centos7 release

# Release - Linux aarch64
lib/target/aarch64-unknown-linux-gnu/release/libskinfixer.so: ${RUST_SOURCE_FILES}
	mkdir -p lib/target/aarch64-unknown-linux-gnu/release
	docker run -v "${CURDIR}/lib/:/code/" docker-registry.k8s.array21.dev/rust-aarch64-xenial release

# Release - Linux armhf
lib/target/arm-unknown-linux-gnueabihf/release/libskinfixer.so: ${RUST_SOURCE_FILES}
	mkdir -p lib/target/arm-unknown-linux-gnueabihf/release
	docker run -v "${CURDIR}/lib/:/code/" docker-registry.k8s.array21.dev/rust-armhf-xenial release

# Release - Windows x86_64
lib/target/x86_64-pc-windows-gnu/release/libskinfixer.dll: ${RUST_SOURCE_FILES}
	sudo chown ${USER}:${USER} -R lib/
	sudo chmod a+rwx -R lib/
	mkdir -p lib/target/x86_64-pc-windows-gnu/release
	cd lib; \
		cargo build --release --target x86_64-pc-windows-gnu
	cp lib/target/x86_64-pc-windows-gnu/release/skinfixer.dll lib/target/x86_64-pc-windows-gnu/release/libskinfixer.dll

# Release - Darwin x86_64
lib/target/x86_64-apple-darwin/release/libskinfixer.dylib: ${RUST_SOURCE_FILES}
	mkdir -p lib/target/x86_64-apple-darwin/release
	docker run -v "${CURDIR}/lib/:/code/" docker-registry.k8s.array21.dev/rust-amd64-darwin release

# Release - Darwin aarch64
lib/target/aarch64-apple-darwin/release/libskinfixer.dylib: ${RUST_SOURCE_FILES}
	mkdir -p lib/target/aarch64-apple-darwin/release
	docker run -v "${CURDIR}/lib/:/code/" docker-registry.k8s.array21.dev/rust-aarch64-darwin release

# Debug - Linux x86_64
lib/target/x86_64-unknown-linux-gnu/debug/libskinfixer.so: ${RUST_SOURCE_FILES}
	docker run -v "${CURDIR}/lib/:/code/" docker-registry.k8s.array21.dev/rust-amd64-xenial

# Debug - Linux aarch64
lib/target/aarch64-unknown-linux-gnu/debug/libskinfixer.so: ${RUST_SOURCE_FILES}
	docker run -v "${CURDIR}/lib/:/code/" docker-registry.k8s.array21.dev/rust-aarch64-xenial

# Debug - Linux armhf
lib/target/arm-unknown-linux-gnueabihf/debug/libskinfixer.so: ${RUST_SOURCE_FILES}
	docker run -v "${CURDIR}/lib/:/code/" docker-registry.k8s.array21.dev/rust-armhf-xenial

# Debug - Windows x86_64
lib/target/x86_64-pc-windows-gnu/debug/libskinfixer.dll: ${RUST_SOURCE_FILES}
	sudo chmod -R a+rwx lib/
	cd lib; \
		cargo build --target x86_64-pc-windows-gnu
	cp lib/target/x86_64-pc-windows-gnu/debug/skinfixer.dll lib/target/x86_64-pc-windows-gnu/debug/libskinfixer.dll

# Debug - Darwin x86_64
lib/target/x86_64-apple-darwin/debug/libskinfixer.dylib: ${RUST_SOURCE_FILES}
	docker run -v "${CURDIR}/lib/:/code/" docker-registry.k8s.array21.dev/rust-amd64-darwin

# Debug - Darwin aarch64
lib/target/aarch64-apple-darwin/debug/libskinfixer.dylib: ${RUST_SOURCE_FILES}
	docker run -v "${CURDIR}/lib/:/code/" docker-registry.k8s.array21.dev/rust-aarch64-darwin

testjar: lib/target/x86_64-unknown-linux-gnu/debug/libskinfixer.so lib/target/aarch64-unknown-linux-gnu/debug/libskinfixer.so lib/target/arm-unknown-linux-gnueabihf/debug/libskinfixer.so lib/target/x86_64-pc-windows-gnu/debug/libskinfixer.dll lib/target/x86_64-apple-darwin/debug/libskinfixer.dylib
	chmod +x gradlew;
	rm -rf ./build/resources
	./gradlew testjar

releasejar: lib/target/x86_64-unknown-linux-gnu/release/libskinfixer.so lib/target/aarch64-unknown-linux-gnu/release/libskinfixer.so lib/target/arm-unknown-linux-gnueabihf/release/libskinfixer.so lib/target/x86_64-pc-windows-gnu/release/libskinfixer.dll lib/target/x86_64-apple-darwin/release/libskinfixer.dylib
	chmod +x gradlew
	rm -rf ./build/resources
	./gradlew releasejar

clean:
	sudo rm -rf build lib/target
