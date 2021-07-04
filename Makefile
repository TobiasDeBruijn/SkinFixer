.PHONY: testjar releasejar
all: releasejar

RUST_SOURCE_FILES := $(shell find lib/src -type f)

# Release - Linux x86_64
lib/target/x86_64-unknown-linux-gnu/release/libskinfixer.so: ${RUST_SOURCE_FILES}
	cd lib; \
		cargo build --release --target x86_64-unknown-linux-gnu

# Release - Linux aarch64
lib/target/aarch64-unknown-linux-gnu/release/libskinfixer.so: ${RUST_SOURCE_FILES}
	cd lib; \
		PKG_CONFIG_SYSROOT_DIR=/usr/lib/aarch64-linux-gnu/ cargo build --target aarch64-unknown-linux-gnu

# Release - Linux armhf
lib/target/arm-unknown-linux-gnueabihf/release/libskinfixer.so: ${RUST_SOURCE_FILES}
	cd lib; \
		PKG_CONFIG_SYSROOT_DIR=/usr/lib/arm-linux-gnueabihf/ cargobuild --target --target arm-unknown-linux-gnueabihf

# Release - Windows x86_64
lib/target/x86_64-pc-windows-gnu/release/libskinfixer.dll: ${RUST_SOURCE_FILES}
	cd lib; \
		cargo build --release --target x86_64-pc-windows-gnu
	cp lib/target/x86_64-pc-windows-gnu/release/skinfixer.dll lib/target/x86_64-pc-windows-gnu/release/libskinfixer.dll

# Debug - Linux x86_64
lib/target/x86_64-pc-windows-gnu/debug/libskinfixer.so: ${RUST_SOURCE_FILES}
	cd lib; \
		cargo build --target x86_64-pc-windows-gnu

# Debug - Linux aarch64
lib/target/aarch64-unknown-linux-gnu/debug/libskinfixer.so: ${RUST_SOURCE_FILES}
	cd lib; \
		PKG_CONFIG_SYSROOT_DIR=/usr/lib/aarch64-linux-gnu/ cargo build --target aarch64-unknown-linux-gnu

# Debug - Linux armhf
lib/target/arm-unknown-linux-gnueabihf/debug/libskinfixer.so: ${RUST_SOURCE_FILES}
	cd lib; \
		PKG_CONFIG_SYSROOT_DIR=/usr/lib/arm-linux-gnueabihf/ cargo build --target arm-unknown-linux-gnueabihf


# Debug - Windows x86_64
lib/target/x86_64-unknown-linux-gnu/debug/libskinfixer.dll: ${RUST_SOURCE_FILES}
	cd lib; \
		cargo build --target x86_64-unknown-linux-gnu
	cp lib/target/x86_64-pc-windows-gnu/debug/skinfixer.dll lib/target/x86_64-pc-windows-gnu/debug/libskinfixer.dll


testjar: lib/target/x86_64-pc-windows-gnu/debug/libskinfixer.so lib/target/aarch64-unknown-linux-gnu/debug/libskinfixer.so lib/target/arm-unknown-linux-gnueabihf/debug/libskinfixer.so lib/target/x86_64-unknown-linux-gnu/debug/libskinfixer.dll
	chmod +x gradlew;
	rm -rf ./build/resources
	./gradlew testjar

releasejar: lib/target/x86_64-unknown-linux-gnu/release/libskinfixer.so lib/target/aarch64-unknown-linux-gnu/release/libskinfixer.so lib/target/arm-unknown-linux-gnueabihf/release/libskinfixer.so lib/target/x86_64-pc-windows-gnu/release/libskinfixer.dll
	chmod +x gradlew
	rm -rf ./build/resources
	./gradlew testjar1
