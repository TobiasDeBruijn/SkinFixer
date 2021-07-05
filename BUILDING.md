# Building
Building SkinFixer requires some extra care due to part of it being written in Rust.
This entire document assumes you are using Debian or a Debian derived distro.

## Installing Rust compilation dependencies
1. Add extra archs: `dpkg --add-architecture arm64`, `dpkg --add-architecture armhf`
2. Add GCC compilers & Make: `apt install gcc gcc-aarch64-linux-gnu gcc-mingw-w64-x86-64 gcc-arm-linux-gnueabihf make`
3. Install libssl & pkg-config: `apt install libssl-dev:arm64 pkg-config:arm64 libssl-dev:armhf`
4. Install Rust toolchain: `curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh`
5. Add rustup targets: `rustup target add aarch64-unknown-linux-gnu arm-unknown-linux-gnueabihf x86_64-pc-windows-gnu`

## Java
1. `apt install openjdk-16-jdk-headless`

## Docker
You must also have Docker installed. This is required for building the linux x86_64 target as well as the Apple darwin x86_64 target

## Building
Either `make testjar` or `make releasejar`, depending on which you want