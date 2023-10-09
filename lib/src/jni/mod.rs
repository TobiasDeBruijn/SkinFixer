//! This module contains all JNI related code

mod del_skin_profile;
mod get_skin_profile;
mod init;
mod set_skin_profile;

use crate::database::Driver;
use std::sync::OnceLock;
use tokio::runtime::Runtime;

static TOKIO_RT: OnceLock<Runtime> = OnceLock::new();
static DRIVER: OnceLock<Driver> = OnceLock::new();

/// Convert a [jni::JString] to a String, panic! if the conversion failed
#[macro_export]
macro_rules! jstring_to_string {
    ($env:expr, $expression:expr) => {
        String::from(match $env.get_string($expression) {
            Ok(jstr) => jstr,
            Err(e) => panic!("Failed to convert JString to String: {:?}", e),
        })
    };
}
