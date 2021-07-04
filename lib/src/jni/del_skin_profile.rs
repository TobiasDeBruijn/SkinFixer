use jni::JNIEnv;
use jni::objects::{JClass, JString};
use crate::config::StorageType;
use crate::jstring_to_string;
use mysql::prelude::Queryable;
use mysql::{Params, params};
use crate::jni::Skin;
use std::fs;
use std::io::Write;
use std::path::PathBuf;

#[no_mangle]
pub extern "system" fn Java_dev_array21_skinfixer_rust_LibSkinFixer_delSkinProfile(env: JNIEnv, _class: JClass, uuid: JString) {
    let uuid = jstring_to_string!(env, uuid);
    let config_guard = crate::config::CONFIG.lock().expect("Failed to lock CONFIG");
    let config_ref = match config_guard.try_borrow() {
        Ok(c) => c,
        Err(e) => panic!("{:?}", e)
    };

    let config = config_ref.as_ref().unwrap();

    match config.get_type() {
        StorageType::Mysql => {
            let mut conn = match config.mysql_conn() {
                Ok(c) => c,
                Err(e) => panic!("{:?}", e)
            };

            match conn.exec::<usize, &str, Params>("DELETE FROM skins WHERE uuid = :uuid", params! {
                "uuid" => &uuid
            }) {
                Ok(_) => {},
                Err(e) => panic!("Failed to remove skin from mysql database: {:?}", e)
            };
        },
        StorageType::Postgres => {
            let mut conn = match config.postgres_conn() {
                Ok(c) => c,
                Err(e) => panic!("{:?}", e)
            };

            match conn.execute("DELETE FROM skins WHERE uuid = $1", &[&uuid]) {
                Ok(_) => {},
                Err(e) => panic!("Failed to remove skin from postgre database: {:?}", e)
            }
        },
        StorageType::Bin => {
            let mut path = PathBuf::from(config.get_path().unwrap());
            path.push("skins.bin");

            let contents = match fs::read(&path) {
                Ok(c) => c,
                Err(e) => panic!("Failed to open storage bin: {:?}", e)
            };

            if contents.is_empty() {
                return;
            }

            let skins: Vec<Skin> = match bincode::deserialize(&contents) {
                Ok(s) => s,
                Err(e) => panic!("Failed to deserialize storage bin: {:?}", e)
            };

            let skins = skins.into_iter().flat_map(|x| {
                if x.uuid.eq(&uuid) {
                    Err(())
                } else {
                    Ok(x)
                }
            }).collect::<Vec<Skin>>();

            let bytes = match bincode::serialize(&skins) {
                Ok(b) => b,
                Err(e) => panic!("Failed to deserialize Skins vector into bytes: {:?}", e)
            };

            let mut file = match fs::File::create(&path) {
                Ok(f) => f,
                Err(e) => panic!("Failed to open storage bin: {:?}", e)
            };

            match file.write_all(&bytes) {
                Ok(_) => {},
                Err(e) => panic!("Failed to write to skins.bin file: {:?}", e)
            }
        },
        StorageType::Sqlite => {
            use rusqlite::named_params;

            let conn = match config.sqlite_conn() {
                Ok(c) => c,
                Err(e) => panic!("Failed to create SQLite connection: {:?}", e)
            };

            let mut stmt = conn.prepare("DELETE FROM skins WHERE uuid = :uuid").unwrap();
            match stmt.execute(named_params! {
                ":uuid": uuid
            }) {
                Ok(_) => {},
                Err(e) => panic!("Failed to delete skin from database: {:?}", e)
            }
        }
    }
}