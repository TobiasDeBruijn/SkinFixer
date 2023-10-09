//! JNI bindings for dev.array21.skinfixer.storage.LibSkinFixer#init()

use crate::jstring_to_string;

use crate::database::{DatabaseOptions, Driver, DriverType};
use crate::jni::{DRIVER, TOKIO_RT};
use jni::objects::{JClass, JString};
use jni::sys::jchar;
use jni::JNIEnv;
use std::path::PathBuf;

/// Java JNI function
///
/// dev.array21.skinfixer.storage.LibSkinFixer#init(String storageType, String host, String database, String username, String password, String storagePath), char port
#[no_mangle]
pub extern "system" fn Java_dev_array21_skinfixer_storage_LibSkinFixer_init(
    env: JNIEnv<'_>,
    _class: JClass<'_>,
    storage_type: JString<'_>,
    host: JString<'_>,
    database: JString<'_>,
    username: JString<'_>,
    password: JString<'_>,
    storage_path: JString<'_>,
    port: jchar,
) {
    let storage_type = jstring_to_string!(env, storage_type);

    let host = jstring_to_string!(env, host);
    let name = jstring_to_string!(env, database);
    let user = jstring_to_string!(env, username);
    let passw = jstring_to_string!(env, password);
    let storage_path = jstring_to_string!(env, storage_path);

    let driver_type = match storage_type.as_str() {
        "mysql" => DriverType::Mysql(DatabaseOptions {
            host: &host,
            user: &user,
            passw: &passw,
            name: &name,
            port,
        }),
        "postgres" => DriverType::Postgres(DatabaseOptions {
            host: &host,
            user: &user,
            passw: &passw,
            name: &name,
            port,
        }),
        "sqlite" => DriverType::Sqlite(PathBuf::from(storage_path)),
        "bin" => DriverType::Bin(PathBuf::from(storage_path)),
        _ => panic!("Unknown storage type '{storage_type}'"),
    };

    let tokio_rt = tokio::runtime::Builder::new_multi_thread()
        .enable_all()
        .build()
        .unwrap();

    let _guard = tokio_rt.enter();

    let driver = tokio_rt
        .block_on(Driver::new(driver_type))
        .expect("Initializing storage driver");

    DRIVER.set(driver).expect("Setting global driver");
    TOKIO_RT
        .set(tokio_rt)
        .expect("Setting global Tokio runtime");
}
