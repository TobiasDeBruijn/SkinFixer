//! This module contains all JNI related code

mod init;
mod get_skin_profile;
mod del_skin_profile;
mod set_skin_profile;

use serde::{Serialize, Deserialize};

/// Convert a [jni::JString] to a String, panic! if the conversion failed
#[macro_export]
macro_rules! jstring_to_string {
    ($env:expr, $expression:expr) => {
        String::from(match $env.get_string($expression) {
            Ok(jstr) => jstr,
            Err(e) => panic!("Failed to convert JString to String: {:?}", e)
        })
    }
}

/// 'Unwrap' a String to None or Some(string), where the return value is None when the String is empty
#[macro_export]
macro_rules! optional_string {
    ($expression:expr) => {
        if $expression.is_empty() {
            None
        } else {
            Some($expression)
        }
    }
}

/// Struct describing a Skin
#[derive(Serialize, Deserialize, Clone, Debug)]
pub struct Skin {
    /// The UUID of the owner
    pub uuid:       String,
    /// The skin's value
    pub value:      String,
    /// the skin's signature
    pub signature:  String
}