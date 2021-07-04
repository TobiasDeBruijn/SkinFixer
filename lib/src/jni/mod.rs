mod init;
mod get_skin_profile;
mod del_skin_profile;
mod set_skin_profile;

use serde::{Serialize, Deserialize};

#[macro_export]
macro_rules! jstring_to_string {
    ($env:expr, $expression:expr) => {
        String::from(match $env.get_string($expression) {
            Ok(jstr) => jstr,
            Err(e) => panic!("Failed to convert JString to String: {:?}", e)
        })
    }
}

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

#[derive(Serialize, Deserialize, Clone)]
pub struct Skin {
    pub uuid:       String,
    pub value:      String,
    pub signature:  String
}