use crate::config::StorageType;
use jni::JNIEnv;
use jni::objects::{JClass, JString, JObject};
use mysql::Params;
use mysql::prelude::Queryable;
use jni::sys::jarray;

use crate::jstring_to_string;
use mysql::params;
use std::path::PathBuf;
use std::fs;
use crate::jni::Skin;

#[no_mangle]
pub extern "system" fn Java_dev_array21_skinfixer_storage_LibSkinFixer_getSkinProfile(env: JNIEnv, _class: JClass, uuid: JString) -> jarray {
    let uuid = jstring_to_string!(env, uuid);
    let config_guard = crate::config::CONFIG.lock().expect("Failed to lock CONFIG");
    let config_ref = match config_guard.try_borrow() {
        Ok(c) => c,
        Err(e) => panic!("{:?}", e)
    };

    let config = config_ref.as_ref().unwrap();

    let string_class = env.find_class("java/lang/String").unwrap();

    match config.get_type() {
        StorageType::Mysql => {
            let mut conn = match config.mysql_conn() {
                Ok(c) => c,
                Err(e) => panic!("{:?}", e)
            };

            let row = match conn.exec_first::<mysql::Row, &str, Params>("SELECT value,signature FROM skins WHERE uuid = :uuid", params! {
                "uuid" => &uuid
            }) {
                Ok(rows) => rows,
                Err(e) => panic!("Failed to query table 'skins': {:?}", e)
            };

            match row {
                Some(row) => {
                    let value = row.get::<String, &str>("value").unwrap();
                    let signature = row.get::<String, &str>("signature").unwrap();

                    let j_value = env.new_string(value).unwrap();
                    let j_signature = env.new_string(signature).unwrap();

                    let array = env.new_object_array(2, string_class, JObject::null()).unwrap();
                    env.set_object_array_element(array, 0, j_value).unwrap();
                    env.set_object_array_element(array, 1, j_signature).unwrap();

                    array
                },
                None => {
                    env.new_object_array(0, string_class, JObject::null()).unwrap()
                }
            }
        },
        StorageType::Postgres => {
            let mut conn = match config.postgres_conn() {
                Ok(c) => c,
                Err(e) => panic!("{:?}", e)
            };

            let rows = match conn.query("SELECT value,signature FROM skins WHERE uuid = $1", &[&uuid]) {
                Ok(r) => r,
                Err(e) => panic!("Failed to query table 'skins': {:?}", e)
            };

            if rows.is_empty() {
                env.new_object_array(0, string_class, JObject::null()).unwrap()
            } else {
                let row = rows.get(0).unwrap();
                let value = row.get::<&str, String>("value");
                let signature = row.get::<&str, String>("signature");

                let j_value = env.new_string(value).unwrap();
                let j_signature = env.new_string(signature).unwrap();

                let array = env.new_object_array(2, string_class, JObject::null()).unwrap();
                env.set_object_array_element(array, 0, j_value).unwrap();
                env.set_object_array_element(array, 1, j_signature).unwrap();

                array
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
                return env.new_object_array(0, string_class, JObject::null()).unwrap();
            }

            let skins: Vec<Skin> = match bincode::deserialize(&contents) {
                Ok(s) => s,
                Err(e) => panic!("Failed to deserialize storage bin: {:?}", e)
            };

            let skins_match = skins.into_iter().flat_map(|x| {
                if x.uuid.eq(&uuid) {
                    Ok(x)
                } else { Err(()) }
            }).collect::<Vec<Skin>>();

            if skins_match.is_empty() {
                env.new_object_array(0, string_class, JObject::null()).unwrap()
            } else {
                let skin = skins_match.get(0).unwrap();
                let j_value = env.new_string(&skin.value).unwrap();
                let j_signature = env.new_string(&skin.signature).unwrap();

                let array = env.new_object_array(2, string_class, JObject::null()).unwrap();
                env.set_object_array_element(array, 0, j_value).unwrap();
                env.set_object_array_element(array, 1, j_signature).unwrap();

                array
            }
        },
        StorageType::Sqlite => {
            use rusqlite::named_params;

            let conn = match config.sqlite_conn() {
                Ok(c) => c,
                Err(e) => panic!("Failed to create SQLite connection: {:?}", e)
            };

            let mut stmt = conn.prepare("SELECT value,signature FROM skins WHERE uuid = :uuid").unwrap();
            let mut rows = match stmt.query(named_params! {
                ":uuid": uuid
            }) {
                Ok(r) => r,
                Err(e) => panic!("Failed to query table 'skins': {:?}", e)
            };

            while let Ok(Some(row)) = rows.next() {
                let value = row.get::<&str, String>("value").unwrap();
                let signature = row.get::<&str, String>("signature").unwrap();

                let j_value = env.new_string(&value).unwrap();
                let j_signature = env.new_string(&signature).unwrap();

                let array = env.new_object_array(2, string_class, JObject::null()).unwrap();
                env.set_object_array_element(array, 0, j_value).unwrap();
                env.set_object_array_element(array, 1, j_signature).unwrap();

                return array;
            }
            env.new_object_array(0, string_class, JObject::null()).unwrap()
        }
    }
}