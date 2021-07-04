.PHONY: testjar releasejar
all: releasejar

RUST_SOURCE_FILES := $(shell find lib/src -type f)

# Release - Linux x86_64
lib/target/x86_64-unknown-linux-gnu/release/libskinfixer.so: ${RUST_SOURCE_FILES}
	cd lib; \
		cargo build --release --target x86_64-unknown-linux-gnu

# Release - Windows x86_64
lib/target/x86_64-pc-windows-gnu/release/libskinfixer.dll: ${RUST_SOURCE_FILES}
	cd lib; \
		cargo build --release --target x86_64-pc-windows-gnu
	cp lib/target/x86_64-pc-windows-gnu/release/skinfixer.dll lib/target/x86_64-pc-windows-gnu/release/libskinfixer.dll

# Debug - Linux x86_64
lib/target/x86_64-pc-windows-gnu/debug/libskinfixer.so: ${RUST_SOURCE_FILES}
	cd lib; \
		cargo build --target x86_64-pc-windows-gnu

# Debug - Windows x86_64
lib/target/x86_64-unknown-linux-gnu/debug/libskinfixer.dll: ${RUST_SOURCE_FILES}
	cd lib; \
		cargo build --target x86_64-unknown-linux-gnu
	cp lib/target/x86_64-pc-windows-gnu/debug/skinfixer.dll lib/target/x86_64-pc-windows-gnu/debug/libskinfixer.dll


testjar: lib/target/x86_64-pc-windows-gnu/debug/libskinfixer.so lib/target/x86_64-unknown-linux-gnu/debug/libskinfixer.dll
	chmod +x gradlew;
	rm -rf ./build/resources
	./gradlew testjar

releasejar: lib/target/x86_64-unknown-linux-gnu/release/libskinfixer.so lib/target/x86_64-pc-windows-gnu/release/libskinfixer.dll
	chmod +x gradlew
	rm -rf ./build/resources
	./gradlew testjar1
