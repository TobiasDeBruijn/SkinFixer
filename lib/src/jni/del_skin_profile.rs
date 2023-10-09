//! JNI bindings for dev.array21.skinfixer.storage.LibSkinFixer#delSkinProfile()

use crate::jstring_to_string;

use crate::database::Profile;
use crate::jni::{DRIVER, TOKIO_RT};
use jni::objects::{JClass, JString};
use jni::JNIEnv;

/// Java JNI function
///
/// dev.array21.skinfixer.storage.LibSkinFixer#delSkinProfile(String uuid)
#[no_mangle]
pub extern "system" fn Java_dev_array21_skinfixer_storage_LibSkinFixer_delSkinProfile(
    env: JNIEnv<'_>,
    _class: JClass<'_>,
    uuid: JString<'_>,
) {
    let uuid = jstring_to_string!(env, uuid);

    let tokio_rt = TOKIO_RT.get().expect("Getting global Tokio runtime");
    let _guard = tokio_rt.enter();

    let driver = DRIVER.get().expect("Getting global storage driver");

    let _ = tokio_rt.block_on(Profile::delete(driver, &uuid));
}
