use jni::JNIEnv;
use jni::objects::{JClass, JString};

use crate::jstring_to_string;
use crate::config::StorageType;
use mysql::prelude::Queryable;
use mysql::{Params, params};
use std::fs;
use crate::jni::Skin;
use std::path::PathBuf;
use std::io::Write;

#[no_mangle]
pub extern "system" fn Java_dev_array21_skinfixer_rust_LibSkinFixer_setSkinProfile(env: JNIEnv, _class: JClass, uuid: JString, value: JString, signature: JString) {
    let uuid = jstring_to_string!(env, uuid);
    let value = jstring_to_string!(env, value);
    let signature = jstring_to_string!(env, signature);

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

            match conn.exec::<usize, &str, Params>("INSERT INTO skins (uuid, value, signature) VALUES (:uuid, :value, :signature)", params! {
                "uuid" => &uuid,
                "value" => &value,
                "signature" => &signature
            }) {
                Ok(_) => {},
                Err(e) => panic!("Failed to insert skin into mysql database: {:?}", e)
            }
        },
        StorageType::Postgres => {
            let mut conn = match config.postgres_conn() {
                Ok(c) => c,
                Err(e) => panic!("{:?}", e)
            };

            match conn.execute("INSERT INTO skins (uuid, value, signature) VALUES ($1, $2, $3)", &[&uuid, &value, &signature]) {
                Ok(_) => {}
                Err(e) => panic!("Failed to insert skin into postgres database: {:?}", e)
            }
        },
        StorageType::Bin => {
            let mut path = PathBuf::from(config.get_path().unwrap());
            path.push("skins.bin");

            let contents = match fs::read(&path) {
                Ok(c) => c,
                Err(e) => panic!("Failed to open storage bin: {:?}", e)
            };

            let mut skins: Vec<Skin> = if contents.is_empty() {
                Vec::new()
            } else {
                match bincode::deserialize(&contents) {
                    Ok(s) => s,
                    Err(e) => panic!("Failed to deserialize storage bin: {:?}", e)
                }
            };

            let new_skin = Skin {
                uuid: (*uuid).to_string(),
                value: (*value).to_string(),
                signature: (*signature).to_string()
            };

            let mut skin_contained = false;
            skins.iter_mut().for_each(|x| {
                if x.uuid.eq(&uuid) {
                    *x = new_skin.clone();
                    skin_contained = true;
                }
            });

            if !skin_contained {
                skins.push(new_skin);
            }

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
        }
    }
}