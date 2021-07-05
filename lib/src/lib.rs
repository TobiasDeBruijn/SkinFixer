//! LibSkinFixer
//! This library is a companion library to the Java project [SkinFixer](https://github.com/TheDutchMC/SkinFixer). SkinFixer is a Minecraft Spigot plugin focused on making skins better

#![deny(deprecated)]
//#![deny(clippy::panic)]

#![deny(rust_2018_idioms)]
#![deny(clippy::decimal_literal_representation)]
#![deny(clippy::if_not_else)]
#![deny(clippy::large_digit_groups)]
#![deny(clippy::missing_docs_in_private_items)]
#![deny(clippy::missing_errors_doc)]
#![deny(clippy::needless_continue)]

pub mod jni;
pub mod config;