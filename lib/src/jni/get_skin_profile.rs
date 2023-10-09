//! JNI bindings for dev.array21.skinfixer.storage.LibSkinFixer#getSkinProfile()

use crate::jstring_to_string;

use crate::database::Profile;
use crate::jni::{DRIVER, TOKIO_RT};
use jni::objects::{JClass, JObject, JString};
use jni::sys::jarray;
use jni::JNIEnv;

/// Java JNI function
///
/// dev.array21.skinfixer.storage.LibSkinFixer#getSkinProfile(String uuid)
#[no_mangle]
pub extern "system" fn Java_dev_array21_skinfixer_storage_LibSkinFixer_getSkinProfile(
    env: JNIEnv<'_>,
    _class: JClass<'_>,
    uuid: JString<'_>,
) -> jarray {
    let uuid = jstring_to_string!(env, uuid);

    let string_class = env.find_class("java/lang/String").unwrap();

    let tokio_rt = TOKIO_RT.get().expect("Getting global Tokio runtime");
    let _guard = tokio_rt.enter();

    let driver = DRIVER.get().expect("Getting global storage driver");

    let profile = tokio_rt
        .block_on(Profile::get_by_uuid(driver, &uuid))
        .expect("Fetching skin");

    match profile {
        Some(profile) => {
            let j_value = env
                .new_string(profile.value)
                .expect("Creating Java string for 'Value'");
            let j_signature = env
                .new_string(profile.signature)
                .expect("Creating Java string for 'Signature'");

            let array = env
                .new_object_array(2, string_class, JObject::null())
                .expect("Creating array for return values");

            match env.set_object_array_element(array, 0, j_value) {
                Ok(_) => {}
                Err(_) => return empty_array(string_class, &env),
            }

            match env.set_object_array_element(array, 1, j_signature) {
                Ok(_) => {}
                Err(_) => return empty_array(string_class, &env),
            }

            array
        }
        None => empty_array(string_class, &env),
    }
}

fn empty_array(string_class: JClass<'_>, env: &JNIEnv<'_>) -> jarray {
    env.new_object_array(0, string_class, JObject::null())
        .expect("Creating empty array")
}
